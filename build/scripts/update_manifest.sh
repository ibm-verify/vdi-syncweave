#!/bin/sh

#
# Copyright IBM Corp. 2001,2023

#
# This script is to remove the javax.xml.bind dependencies from the 
# META-INF/MANIFEST.MF file within the specified jar file.  This is required as
# javax.xml.bind is loaded by the TDI class loader and is already present
# in the Java runtime.  If a dependency is present in the manifest file the
# OSGI framework expects this dependency to be provided by an OSGI plugin,
# which in turn causes class loader issues/conflicts.
#

set -e

#
# Check the command line usage.
#

if [ $# -ne 1 ] ; then
    echo "usage: $0 [jar file]"
    exit 1
fi

jar=$1

if [ ! -f $jar ] ; then
    echo "Error> $jar does not exist!"
    exit 1
fi

echo "Updating the manifest for: $jar"

#
# The following function is used to massage the import package statement.
#

massage_import_package()
{
    echo $1 | sed "s|javax.xml.bind,||g" | sed "s|javax.xml.bind.annotation,||g" | sed "s|javax.xml.bind.annotation.adapters,||g" | sed "s|,|,\n |g"
}

#
# We want to massage the MANIFEST.MF file, removing references to
# javax.xml from the 'Import-Package:' declaration.
#

inImportPackage=0
importPackage=

IFS=''

cr="
"

tmpdir=/tmp/update
mkdir -p $tmpdir/META-INF

unzip -p $jar META-INF/MANIFEST.MF | sed "s|$cr||g" |
while read line
do
    if [ "`echo $line | cut -f 1 -d ':'`" = "Import-Package" ] ; then
        inImportPackage=1
        importPackage="$line"

    elif [ $inImportPackage -eq 1 ] ; then
        if [ -z "`echo \"$line\" | awk '/^[[:blank:]]/'`" ] ; then
            inImportPackage=0

            massage_import_package "$importPackage"

            echo "$line"
        else
            importPackage="${importPackage}`echo ${line} | sed "s|^ ||g"`"
        fi
    else
        echo "$line"
    fi
done > $tmpdir/META-INF/MANIFEST.MF

jar --update --no-manifest --file $jar -C $tmpdir META-INF/MANIFEST.MF

