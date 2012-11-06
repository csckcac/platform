#!/bin/bash
source ./demorc
count=$1
for I in `seq 1 $count`; do nova delete test$I; done

