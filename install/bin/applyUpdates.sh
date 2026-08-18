#!/bin/sh

#
# Copyright contributors to the SyncWeave project
set -euo pipefail
src=`dirname $0`
#set -vx

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
CURRENT_VERSION="1.0.0"
dst=$1
BASE_ARCHIVE=""
ECLIPSECE_ARCHIVE=""

if [ ! -f $dst/etc/build.properties ] ; then
    echo "Error - Invalid product install folder. Enter valid product install location."
    exit 1
fi
	version=`grep version $dst/etc/build.properties | cut -f 2 -d '='`
    PREV_VERSION=$version
	if [ "$CURRENT_VERSION" \> "$version" ]; then
		#echo $CURRENT_VERSION " is lexicographically greater then $version"
		echo "Please wait... upgrade will start."
	elif [ "$CURRENT_VERSION" \< "$version" ]; then
		#echo $version " is lexicographically greater than $CURRENT_VERSION"
		echo "IVDI " $version " is found installed. Upgraded version is already installed. Exiting install."
		exit 0
	else
		echo "Installation is up to date. Exiting install."
		exit 0
	fi

        mkdir -p $dst/maintenance/BACKUP/$version
	set +e
	cp -r $dst/bin $dst/maintenance/BACKUP/$version
	cp -r $dst/etc $dst/maintenance/BACKUP/$version
	if [ -d $dst/jscript ] ; then
		cp -r $dst/jscript $dst/maintenance/BACKUP/$version
	fi
	if [ -d $dst/examples ] ; then
		cp -r $dst/examples $dst/maintenance/BACKUP/$version
	fi
	cp -r $dst/L* $dst/maintenance/BACKUP/$version
	cp -r $dst/l* $dst/maintenance/BACKUP/$version
	cp -r $dst/osgi $dst/maintenance/BACKUP/$version
	cp -r $dst/p* $dst/maintenance/BACKUP/$version
	cp -r $dst/jars/ $dst/maintenance/BACKUP/$version
	cp -r $dst/t* $dst/maintenance/BACKUP/$version
	cp -r $dst/S* $dst/maintenance/BACKUP/$version
	cp -r $dst/s* $dst/maintenance/BACKUP/$version
	cp -r $dst/x* $dst/maintenance/BACKUP/$version
	cp -r $dst/X* $dst/maintenance/BACKUP/$version
	cp -r $dst/i* $dst/maintenance/BACKUP/$version

# Check to ensure that this installation is being run on the correct
# platform.  We do this by checking the platform, and ensuring that the
# corresponding eclipse zip file is present.

file_to_check=

case `uname -a` in
    *Darwin*x86_64*)
        #file_to_check=$src/eclipsece-macosx.cocoa.x86_64.tar.gz
        ;;
    *Darwin*arm64*)
        #file_to_check=$src/eclipsece-macosx.cocoa.aarch64.tar.gz
        ;;
    *Linux*x86_64*)
        #file_to_check=$src/eclipsece-linux.gtk.x86_64.tar.gz
        ;;
    *Linux*s390x*)
        #file_to_check=`ls $src/ibm-semeru-certified-jre_s390x_linux_*.tar.gz 2>/dev/null`
        ;;
    *AIX*)
        #file_to_check=`ls $src/ibm-semeru-certified-jre_ppc64_aix_*.tar.gz 2>/dev/null`
        ;;
esac

#if [ -z "$file_to_check" -o ! -f "$file_to_check" ] ; then
#    Echo bad_platform
#    exit 1
#fi

mkdir -p $dst

if [ `uname` = "AIX" ] ; then
    PATH="$PATH:/usr/lib/instl"
fi

dst=`realpath $dst`

# Add the license files.
#cp -R $src/license $dst/

# Extract the TDI zips to the destination directory...
for i in `ls $src/*.zip 2>/dev/null`; do
    extract=0

    if [ `basename $0` = "install_ce.sh" ] ; then
        case `basename $i` in
            TDI_*) extract=1 ;;
            #eclipsece-*) extract=1 ;;
            #ibm-semeru-*) extract=1 ;;
        esac
    else
        case `basename $i` in
            TDI_Base*) extract=1 ;;
            TDI_Server*) extract=1 ;;
            TDI_Plugins*) extract=1 ;;
            TDI_LUM*) extract=1 ;;
			eclipsece-*) extract=1 ;;
            #ibm-semeru-*) extract=1 ;;
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
            #eclipsece-*) extract=1 ;;
            #ibm-semeru-*) extract=1 ;;
        esac
    else
        case `basename $i` in
            TDI_Base*)
                extract=1
                if [[ `basename $i` = "TDI_Base.tar.gz" ]] ; then
                   BASE_ARCHIVE=`basename $i`
                fi
                ;;
            TDI_Server*) extract=1 ;;
            TDI_Plugins*) extract=1 ;;
            TDI_LUM*) extract=1 ;;
			eclipsece-*)
                extract=1
                ECLIPSECE_ARCHIVE=`basename $i`
                ;;
            #ibm-semeru-*) extract=1 ;;
        esac
    fi

    if [ $extract -eq 1 ] ; then
        Echo extracting "`basename $i`"
        if [[ `basename $i` = "${BASE_ARCHIVE}" ]] ; then
            gzip -d -c $i | tar -x -f - -C $dst --exclude='etc/build.properties'
        elif [[ `basename $i` = "${ECLIPSECE_ARCHIVE}" ]] ; then
            rm -rf ${dst}/ce/eclipsece/plugins/com.ibm.di.*.jar
            rm -rf ${dst}/ce/eclipsece/plugins/com.ibm.tdi.*.jar
            gzip -d -c $i | tar -x -f - -C $dst/ce
        else
            gzip -d -c $i | tar -x -f - -C $dst
        fi
    fi
done

arch=`uname -m`
case `uname -a` in
*Linux*x86_64*)
    set -e
    Echo extracting TDI_CEUNIX.tar.gz
    gzip -d -c $src/TDI_CEUNIX.tar.gz | tar -x -f - -C $dst
    Echo extracting TDI_Plugins_linux_x86_64.tar.gz
    gzip -d -c $src/TDI_Plugins_linux_${arch}.tar.gz | tar -x -f - -C $dst
    Echo extracting TDI_ServerLinuxX86_64.tar.gz
    gzip -d -c $src/TDI_ServerLinuxX86_64.tar.gz | tar -x -f - -C $dst
    ;;
*Linux*s390x*)
    set -e
	echo "s390x*"
	Echo extracting TDI_BaseI5OS.tar.gz
	gzip -d -c $src/TDI_BaseI5OS.tar.gz | tar -x -f - -C $dst
	Echo extracting TDI_ServerI5OS.tar.gz
	gzip -d -c $src/TDI_ServerI5OS.tar.gz | tar -x -f - -C $dst
	;;
*AIX*)
    set -e
	Echo extracting TDI_Plugins_aix_ppc.tar.gz
	gzip -d -c $src/TDI_Plugins_aix_ppc.tar.gz | tar -x -f - -C $dst
	Echo extracting TDI_ServerAixPPC.tar.gz
	gzip -d -c $src/TDI_ServerAixPPC.tar.gz | tar -x -f - -C $dst
	;;
*Darwin*x86_64*)
    set -e
	Echo extracting eclipsece-macosx.cocoa.x86_64.tar.gz
	gzip -d -c $src/eclipsece-macosx.cocoa.x86_64.tar.gz | tar -x -f - -C $dst
	Echo extracting TDI_CEUNIX.tar.gz
	gzip -d -c $src/TDI_CEUNIX.tar.gz | tar -x -f - -C $dst
	;;
*Darwin*arm64*)
    set -e
	Echo extracting eclipsece-macosx.cocoa.aarch64.tar.gz
    gzip -d -c $src/eclipsece-macosx.cocoa.aarch64.tar.gz | tar -x -f - -C $dst
	Echo extracting TDI_CEUNIX.tar.gz
	gzip -d -c $src/TDI_CEUNIX.tar.gz | tar -x -f - -C $dst
	;;
esac

# Fix the path for the CE.
#if [ -d $dst/eclipsece ] ; then
 #   mkdir -p $dst/ce
 #   mv $dst/eclipsece $dst/ce/
#fi

# On MacOS we also need to massage the location of the miadmin binary.
eclipse_root=$dst/ce/eclipsece
if [ `uname` = "Darwin" -a -d $eclipse_root ] ; then
    #mv $eclipse_root/MacOS/miadmin $eclipse_root/miadmin.app/Contents/MacOS
    #rm -rf $eclipse_root/MacOS
    chmod 755 $eclipse_root/miadmin.app/Contents/MacOS/miadmin

    # We also want to correct the default workspace location.
    echo "osgi.instance.area.default=$dst/workspace" \
            >> $eclipse_root/configuration/config.ini
fi

# Fix the permissions.
chmod 755 $dst/bin/*
chmod 755 $dst/ibm*
chmod 755 $dst/serverapi/*.sh
#chmod 755 $dst/ibmditk
chmod 755 $dst/ibmdisrv

if [ -f $dst/ce/eclipsece/miadmin ] ; then
    chmod 755 $dst/ce/eclipsece/miadmin
fi

# Set up Java.
Echo extracting_java

jvm_dir=$dst/jvm

#if [ `uname` = "Darwin" ] ; then
#    ln -sf $dst/jdk-17.*-jre/Contents/Home $jvm_dir
#else
#    mkdir -p $jvm_dir

#    mv $dst/jdk-17.*-jre $jvm_dir/jre
#fi

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
<CE>
</CE>
<EXAMPLES>
</EXAMPLES>
<PLUGINS>
</PLUGINS>
EOF

#version=`grep version $src/build.properties | cut -f 2 -d '='`
cp  $dst/maintenance/BACKUP/$PREV_VERSION/etc/global.properties $dst/etc/global.properties
cp  $dst/maintenance/BACKUP/$PREV_VERSION/etc/log4j2.xml $dst/etc/log4j2.xml
cp  $dst/maintenance/BACKUP/$PREV_VERSION/etc/di_castor_mapping.xml $dst/etc/di_castor_mapping.xml
cp  $dst/maintenance/BACKUP/$PREV_VERSION/etc/derby.properties $dst/etc/derby.properties
rm -rf $dst/jars/3rdparty/others/castor-core-1.4.1.jar
rm -rf $dst/jars/3rdparty/others/castor-xml-1.4.1.jar
rm -rf $dst/etc/TDI1000.SYS2

# Version change at the last step if everything is fine
gzip -d -c ${src}/${BASE_ARCHIVE} | tar -x -f - -C $dst etc/build.properties

# Finished.
Echo complete

exit 0

