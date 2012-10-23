#!/bin/bash
./stopservices.sh
./OSuninstall.sh
rm -rf /var/lib/nova/instances/instance-*
rm -f /var/log/nova/*.log
rabbitmqctl stop_app
rabbitmqctl reset
