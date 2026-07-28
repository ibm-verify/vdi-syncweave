#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2006, 2010
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

export _BPX_SHAREAS=YES
if [ -z "${TDI_DIR}" ] ; then
        CMDFINDER=whence
        BIN_DIR=`$CMDFINDER $0`
        BIN_DIR=`dirname $BIN_DIR`
        TDI_DIR="$BIN_DIR/../"
fi

export LIBPATH=$TDI_DIR:${LIBPATH}
if [ -z "${JAVA_HOME}" ] ; then
        export JAVA_HOME=/usr/lpp/java/J5.0
fi

$TDI_DIR/ibmdisrv $*
