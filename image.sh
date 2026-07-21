#!/usr/bin/bash
## source the userinput.data.src file to read the environment variable "BUILD_TYPE"
## Use this variable to call the respective image.sh script i.e. either image_dev.sh or image_docs.sh

echo "Build Type?:" $BUILD_TYPE

if [ ${BUILD_TYPE} = "DOCS" ] ; then
chmod +x image_docs.sh
./image_docs.sh
fi

if [ ${BUILD_TYPE} = "DEV" ] ; then
chmod +x image_dev.sh
./image_dev.sh
fi

