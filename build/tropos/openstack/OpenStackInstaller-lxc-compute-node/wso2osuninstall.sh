#!/bin/bash
./wso2stopservices.sh
mysql -uroot -popenstack -e "drop database nova;"
mysql -uroot -popenstack -e "drop database glance;"
mysql -uroot -popenstack -e "drop database keystone;"
rm -rf /var/lib/nova/instances/instance-*
rm -f /var/log/nova/*.log
rabbitmqctl stop_app
rabbitmqctl reset
