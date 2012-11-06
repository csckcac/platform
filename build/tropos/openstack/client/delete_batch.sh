#!/bin/bash
source ./demorc
fromCount=$1
toCount=$2

if [ -z $fromCount ]; then
    fromCount=1
fi
if [ -z $toCount ]; then
    toCount=1
fi

for I in `seq $fromCount $toCount`; do nova delete test$I; done
