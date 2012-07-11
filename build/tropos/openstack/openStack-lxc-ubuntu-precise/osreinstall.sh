#!/bin/bash
rabbitmqctl start_app
./wso2osreinstall.sh -T controller -F 172.15.1.0/24 -f 12.1.0.0/16 -s 512 -P eth1 -p eth2 -t demo -v lxc
./startservices.sh
