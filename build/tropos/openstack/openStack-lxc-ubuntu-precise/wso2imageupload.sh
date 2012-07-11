#!/bin/bash
# ----------------------------------------------------------------------------
#  Copyright 2005-20012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------

# Simple script to publish an image to cloud environment for use

TMPAREA=/tmp/__upload

# Process Command Line
while getopts a:p:t:C:x:y:z:w: opts
do
  case $opts in
    a)
        ADMIN=${OPTARG}
        ;;
    p)
        PASSWORD=${OPTARG}
        ;;
    t)
        TENANT=${OPTARG}
        ;;
    C)
        ENDPOINT=${OPTARG}
        ;;
    x)
        ARCH=${OPTARG}
        ;;
    y)
        DISTRO=${OPTARG}
        ;;
    z)
        ARCHFILE=${OPTARG}
        ;;
    w)
        VERSION=${OPTARG}
        ;;
    *)
        echo "Syntax: $(basename $0) -u USER -p KEYSTONE -t TENANT -C CONTROLLER_IP -x ARCH -y DISTRO -w VERSION -z ARCHFILE"
        exit 1
        ;;
  esac
done

TARBALL=${ARCHFILE}
TARBALL_PATH=/root/upload

# You must supply the API endpoint
if [[ ! $ENDPOINT ]]
then
        echo "Syntax: $(basename $0) -a admin -p PASSWORD -t TENANT -C CONTROLLER_IP"
        exit 1
fi



mkdir -p ${TMPAREA}
if [ ! -f ${TMPAREA}/${TARBALL} ]
then
        cp -f ${TARBALL_PATH}/${TARBALL} ${TMPAREA}/${TARBALL}
fi

if [ -f ${TMPAREA}/${TARBALL} ]
then
	cd ${TMPAREA}
	tar zxf ${TARBALL}
	DISTRO_IMAGE=$(ls *.img)


	AMI=$(glance -I ${ADMIN} -K ${PASSWORD} -T ${TENANT} -N http://${ENDPOINT}:5000/v2.0 add name="${DISTRO} ${VERSION} ${ARCH} Server" disk_format=ami container_format=ami distro="${DISTRO} ${VERSION}" kernel_id=${KERNEL} is_public=true < ${DISTRO_IMAGE} | awk '/ ID/ { print $6 }')

	echo "${DISTRO} ${VERSION} ${ARCH} now available in Glance (${AMI})"

	rm -f /tmp/__upload/*{.img,-vmlinuz-virtual,loader,floppy}
else
	echo "Tarball not found!"
fi
