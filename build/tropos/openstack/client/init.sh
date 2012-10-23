#!/bin/bash
rm ./demorc
rm ./demo.pem
rm ./wso2.pem
cp -f ../OpenStackInstaller/demorc ./
source ./demorc
euca-add-keypair demo > demo.pem
chmod 0600 demo.pem
nova keypair-add wso2 > wso2.pem
chmod 0600 wso2.pem

nova secgroup-add-rule default icmp -1 -1  0.0.0.0/0
nova secgroup-add-rule default tcp 22 22  0.0.0.0/0
nova secgroup-add-rule default tcp 80 80  0.0.0.0/0
nova secgroup-add-rule default tcp 8080 8080  0.0.0.0/0

#euca-authorize default -P tcp -p 22 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 80 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 8080 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9763 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9764 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9765 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9443 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9444 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9445 -s 0.0.0.0/0
#euca-authorize default -P tcp -p 9330 -s 0.0.0.0/0
#euca-authorize default -P icmp -t -1:-1
