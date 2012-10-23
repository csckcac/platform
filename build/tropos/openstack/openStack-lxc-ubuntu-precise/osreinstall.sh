#!/bin/bash
rabbitmqctl start_app
./reinstallos.sh -T controller -F 172.16.1.0/24 -f 11.1.0.0/16 -s 512 -P eth1 -p eth2 -t demo -v lxc
./startservices.sh
