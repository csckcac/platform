#!/bin/bash
container_home=$1
id=$2
./container_action $container_home stop $id "/mnt/lxc"
