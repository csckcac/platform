#!/bin/bash
mkdir -p deploy
./stratos-setup.pl   -p `pwd` -t `pwd`/deploy -v "1.5.1" -d true -l "manager is greg as bam dss bps brs cep esb gs mb ms" -c stratos_config_min.xml -u root -w root -s true
chmod +x deploy/stratos.sh
chmod +x deploy/wso2stratos-as-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-bam-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-bps-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-brs-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-cep-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-dss-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-esb-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-governance-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-gs-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-is-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-manager-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-mb-1.5.1/bin/wso2server.sh
chmod +x deploy/wso2stratos-ms-1.5.1/bin/wso2server.sh
