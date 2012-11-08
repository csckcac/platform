#!/bin/bash
source ./demorc
num_ips=$1

if [ -z $num_ips ]; then
    num_ips=1
fi

for I in `seq 1 $num_ips`; do nova floating-ip-create; sleep 10; done

