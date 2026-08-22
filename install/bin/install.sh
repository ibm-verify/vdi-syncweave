#!/bin/sh

#
# Copyright contributors to the SyncWeave project
set -e

src=`dirname $0`

#############################################################################
# Display a message 

Echo() {
    name=$1; shift

    # Work out the name of the catalog file which is to be used.
    cat_lang=en

    case "$LANG" in
        *de*) cat_lang=de ;;
        *es*) cat_lang=es ;;
        *fr*) cat_lang=fr ;;
        *it*) cat_lang=it ;;
        *ja*) cat_lang=ja ;;
        *ko*) cat_lang=ko ;;
        *pt_BR*) cat_lang=pt_BR ;;
        *zh_CN*) cat_lang=zh_CN ;;
        *zh_TW*) cat_lang=zh_TW ;;
    esac

    cat_file=$src/NLS/install_$cat_lang.json

    if [ ! -f "$cat_file" ] ; then
        echo "Error> the catalog file, $cat_file, is missing!"
        exit 1
    fi

    msg=`grep \"$name\" $cat_file | cut -f 4 -d '"'`

    if [ -z "$msg" ] ; then
        echo "Error> an unknown message was specified: $name!"
        exit 1
    fi

    printf "$msg" $*
    printf "\n"
}

#############################################################################
# Main line.

if [ $# -ne 1 ] ; then
    Echo usage "$0"
    exit 1
fi

dst=$1

if [ -d $dst ] ; then
    Echo already_exists "$dst"
    exit 1
fi

# Check to ensure that this installation is being run on the correct 
# platform.  We do this by checking the platform, and ensuring that the
# corresponding eclipse zip file is present.

file_to_check=

case `uname -a` in
    *Darwin*x86_64*) 
        file_to_check=$src/eclipsece-macosx.cocoa.x86_64.tar.gz
        ;;
    *Darwin*arm64*) 
        file_to_check=$src/eclipsece-macosx.cocoa.aarch64.tar.gz
        ;;
    *Linux*x86_64*) 
        file_to_check=$src/eclipsece-linux.gtk.x86_64.tar.gz
        ;;
    *Linux*s390x*)
        set +e
        file_to_check=`ls $src/ibm-semeru-open-jre_s390x_linux_*.tar.gz 2>/dev/null`
        set -e
        ;;
    *AIX*)
        set +e
        file_to_check=`ls $src/ibm-semeru-open-jre_ppc64_aix_*.tar.gz 2>/dev/null`
        set -e
        ;;
esac

if [ -z "$file_to_check" -o ! -f "$file_to_check" ] ; then
    Echo bad_platform
    exit 1
fi

mkdir -p $dst

if [ `uname` = "AIX" ] ; then
    PATH="$PATH:/usr/lib/instl"
fi

dst=`realpath $dst`

# Extract the TDI zips to the destination directory...
for i in `ls $src/*.zip 2>/dev/null`; do
    extract=0

    if [ `basename $0` = "install_ce.sh" ] ; then
        case `basename $i` in
            TDI_*) extract=1 ;;
            eclipsece-*) extract=1 ;;
            ibm-semeru-*) extract=1 ;;
        esac
    else
        case `basename $i` in
            TDI_Base*) extract=1 ;;
            TDI_Server*) extract=1 ;;
            TDI_Plugins*) extract=1 ;;
            TDI_LUM*) extract=1 ;;
            ibm-semeru-*) extract=1 ;;
        esac
    fi

    if [ $extract -eq 1 ] ; then
        Echo extracting "`basename $i`"
        unzip -q -o $i -d $dst
    fi
done

# Extract the TDI compressed tar files to the destination directory...
for i in `ls $src/*.tar.gz 2>/dev/null`; do
    extract=0

    if [ `basename $0` = "install_ce.sh" ] ; then
        case `basename $i` in
            TDI_*) extract=1 ;;
            eclipsece-*) extract=1 ;;
            ibm-semeru-*) extract=1 ;;
        esac
    else
        case `basename $i` in
            TDI_Base*) extract=1 ;;
            TDI_Server*) extract=1 ;;
            TDI_Plugins*) extract=1 ;;
            TDI_LUM*) extract=1 ;;
            ibm-semeru-*) extract=1 ;;
        esac
    fi

    if [ $extract -eq 1 ] ; then
        Echo extracting "`basename $i`"
        gzip -d -c $i | tar -x -f - -C $dst
    fi
done

# Fix the path for the CE.
if [ -d $dst/eclipsece ] ; then
    mkdir -p $dst/ce
    mv $dst/eclipsece $dst/ce/
fi

# On MacOS we also need to massage the location of the miadmin binary.
eclipse_root=$dst/ce/eclipsece
if [ `uname` = "Darwin" -a -d $eclipse_root ] ; then
    mv $eclipse_root/MacOS/miadmin $eclipse_root/miadmin.app/Contents/MacOS
    rm -rf $eclipse_root/MacOS
    chmod 755 $eclipse_root/miadmin.app/Contents/MacOS/miadmin

    # We also want to correct the default workspace location.
    echo "osgi.instance.area.default=$dst/workspace" \
            >> $eclipse_root/configuration/config.ini
fi

# Fix the permissions.
chmod 755 $dst/bin/*
chmod 755 $dst/ibm*
chmod 755 $dst/serverapi/*.sh

if [ -f $dst/ce/eclipsece/miadmin ] ; then
    chmod 755 $dst/ce/eclipsece/miadmin
fi

# Set up Java.
Echo extracting_java

jvm_dir=$dst/jvm

if [ `uname` = "Darwin" ] ; then
    ln -sf $dst/jdk-21.*-jre/Contents/Home $jvm_dir
else
    mkdir -p $jvm_dir

    mv $dst/jdk-21.*-jre $jvm_dir/jre
fi

# Fix up the global.properties file.  We use sed for this.  It would be
# nice to be able to use the 'sed -i' for inplace editing, but this doesn't
# work on MAC systems.

prop=$dst/etc/global.properties

sed "s|\\\$TDI_SYSTEM_STORE_PORT\\$|1527|g" $prop > $prop.tmp
mv $prop.tmp $prop

sed "s|\\\$TDI_REST_API_PORT\\$|1098|g" $prop > $prop.tmp
mv $prop.tmp $prop

sed "s|\\\$TDI_SERVER_PORT\\$|1099|g" $prop > $prop.tmp
mv $prop.tmp $prop

# Set up TDI
$dst/bin/tdiSetJavaHome.sh $dst/jvm
$dst/bin/setDefaultSolDir.sh $dst
$dst/bin/tdiSetBackupDir.sh default

# Update the pwsync.props files.
for dir in sun tds pam; do
    pwsync=$dst/pwd_plugins/$dir/pwsync.props

    if [ -f $pwsync ] ; then
        sed "s|\\\$change\\$|$dst|g" $pwsync > $pwsync.tmp
        mv $pwsync.tmp $pwsync
    fi
done

# We also want to protect the properties within the global.properties file.
export PATH="$dst/serverapi:$PATH"

cryptoutils.sh -input $prop -output $prop -mode encrypt_props \
    -keystore $dst/testserver.jks -storepass server -alias server

# On a MacOS box the application is marked as quarantined.  We need to
# remove the quarantine, otherwise the application won't start.

if [ `uname` = "Darwin" -a -d $dst/ce ] ; then
    xattr -r -d com.apple.quarantine $dst/ce/eclipsece/miadmin.app
fi

# Create the registry file.
cat > $dst/.registry << EOF
<FIXES>
</FIXES>
<EDITION>
   Identity
</EDITION>
<LICENSE>
   Full
</LICENSE>
<LEVEL>
   `grep version $src/build.properties | cut -f 2 -d '='`
</LEVEL>
<BASE>
</BASE>
<SERVER>
</SERVER>
EOF

if [ `basename $0` = "install_ce.sh" ] ; then
cat >> $dst/.registry << EOF
<CE>
</CE>
<EXAMPLES>
</EXAMPLES>
<PLUGINS>
</PLUGINS>
EOF
fi

# Finished.
Echo complete

exit 0

