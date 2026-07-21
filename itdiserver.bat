@echo off
setlocal
rem command line utility for :
rem  1> start/ stop / reload config
rem  2> start / stop AL/EH
rem  3> shutdown server
rem  4> list loaded configs
rem  type either "itdiserver" or "itdiserver ?" for help on options

set PATH="$change$\_jvm\jre\bin";"$change$\libs"

rem use the trustore and keystore properties if server is running with 
rem property "api.remote.ssl.on" set to true
rem for eg :
rem use -D -Djavax.net.ssl.trustStore=serverapi\testadmin.jks -Djavax.net.ssl.keyStore=serverapi\testadmin.jks -Djavax.net.ssl.keyStorePassword=administrator -Djavax.net.ssl.trustStorePassword=administrator

"$change$\_jvm\jre\bin\java" -jar "$change$\IDILoader.jar" com.ibm.di.cli.itdiserver %*


endlocal
