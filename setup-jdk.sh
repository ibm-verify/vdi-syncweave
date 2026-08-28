#!/bin/bash
# Downloads IBM Semeru JDKs needed for building
mkdir -p adks/ibm/jdk adks/ibm/jre
jdk_x64_linux=ibm-semeru-open-jdk_x64_linux_21.0.12.0.tar.gz

echo "Downloading JDK 21..."
cd adks/ibm/jdk
wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/${jdk_x64_linux}
tar -xf ${jdk_x64_linux}
rm -rf ${jdk_x64_linux}
cd -

echo "Done. JDK is ready."

echo "Downloading JREs..."
cd adks/ibm/jre
wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_x64_linux_21.0.12.0.tar.gz

wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_x64_windows_21.0.12.0.zip

wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_s390x_linux_21.0.12.0.tar.gz

wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_ppc64_aix_21.0.12.0.tar.gz

wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_aarch64_mac_21.0.12.0.tar.gz

wget https://github.com/ibmruntimes/semeru21-binaries/releases/download/jdk-21.0.12.0/ibm-semeru-open-jre_x64_mac_21.0.12.0.tar.gz

echo "Done. JREs are ready."
cd -

