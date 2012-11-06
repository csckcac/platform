#!/bin/bash
source ./demorc
image=$1
fromCount=$2
toCount=$3

if [ -z $fromCount ]; then
    fromCount=1
fi
if [ -z $toCount ]; then
    toCount=1
fi


for I in `seq $fromCount $toCount`; do ./boot_instance.sh $image test$I; done
