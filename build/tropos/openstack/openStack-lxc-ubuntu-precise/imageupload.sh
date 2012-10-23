#!/bin/bash

# Simple script to publish an image to cloud environment for use

TMPAREA=/tmp/__upload

# Process Command Line
while getopts a:p:t:C:x:y:z:n:w: opts
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
    n)
        IMAGENAME=${OPTARG}
        ;;
    w)
        VERSION=${OPTARG}
        ;;
    *)
        echo "Syntax: $(basename $0) -u USER -p KEYSTONE -t TENANT -C CONTROLLER_IP -x ARCH -y DISTRO -w VERSION -z ARCHFILE -n IMAGENAME"
        exit 1
        ;;
  esac
done

IMAGE=`basename $ARCHFILE`
EXTENSION="${IMAGE##*.}"

# You must supply the API endpoint
if [[ ! $ENDPOINT ]]
then
        echo "Syntax: $(basename $0) -a admin -p PASSWORD -t TENANT -C CONTROLLER_IP"
        exit 1
fi



mkdir -p ${TMPAREA}
if [ ! -f ${TMPAREA}/${IMAGE} ]
then
        cp ${ARCHFILE} ${TMPAREA}/${IMAGE}
fi

if [ -f ${TMPAREA}/${IMAGE} ]
then
	cd ${TMPAREA}
    if [[ ${EXTENSION} == "gz" || ${EXTENSION} == "tgz" ]]; then
	    tar zxf ${IMAGE}
	    DISTRO_IMAGE=$(ls *.img)
	elif [[ ${EXTENSION} == "img" ]]; then
	    DISTRO_IMAGE=${IMAGE}
    fi

	AMI=$(glance -I ${ADMIN} -K ${PASSWORD} -T ${TENANT} -N http://${ENDPOINT}:5000/v2.0 add name="${IMAGENAME}" disk_format=ami container_format=ami distro="${DISTRO} ${VERSION}" kernel_id=${KERNEL} is_public=true < ${DISTRO_IMAGE} | awk '/ ID/ { print $6 }')

	echo "${DISTRO} ${VERSION} ${ARCH} now available in Glance (${AMI})"

	rm -f /tmp/__upload/*{.img,-vmlinuz-virtual,loader,floppy}
else
	echo "Tarball not found!"
fi
