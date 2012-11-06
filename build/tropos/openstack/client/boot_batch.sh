#!/bin/bash
source ./demorc
image=$1
count=$2

for I in `seq 1 $count`; do ./boot_instance.sh $image test$I; done
