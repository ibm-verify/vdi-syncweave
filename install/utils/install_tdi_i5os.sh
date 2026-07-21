#
#  install_tdi_i5OS.sh - Issue java command to install Tivoli Directory Integrator on OS/400
#
# In IA, we dont have to use the console argument...it will run in console mode by default.

JAVA160_DIR="/QOpenSys/QIBM/ProdData/JavaVM/jdk60/32bit/bin"
JAR_FILES="/QIBM/ProdData/OS/OSGi/LWI81/native/iasadmin.jar"
TDI_JAR_DIR=`(/usr/bin/dirname $0)`

echo ${TDI_JAR_DIR}

if [ ! -e ${JAVA160_DIR} ] ; then  
   echo "SDI installer could not detect JVM 1.6.0"
   echo "Please make sure one is available before trying again."
   exit 1
fi

$JAVA160_DIR/java -cp ${TDI_JAR_DIR}/SDIV72.jar:${JAR_FILES} install $@
