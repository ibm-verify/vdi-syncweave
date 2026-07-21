#
#  uninstaller.sh - Issue java command to uninstall Tivoli Directory Integrator on OS/400
#
#  In IA, we dont have to use the console argument, it will run in console mode by default.
#

JAVA160_DIR="/QOpenSys/QIBM/ProdData/JavaVM/jdk60/32bit/bin"
TDI_JAR_DIR=`(/usr/bin/dirname $0)`

"$JAVA160_DIR/java" -jar "${TDI_JAR_DIR}/uninstaller.jar" $@
