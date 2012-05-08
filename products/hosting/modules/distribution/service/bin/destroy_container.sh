#!/bin/bash
container_home=$1
id=$2
./container_action $container_home destroy $id "/mnt/lxc"
