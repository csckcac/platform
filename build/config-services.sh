#!/bin/bash
set -e
#PATH to the place where you checked out carbon
CARBON_HOME="PATH TO CARBON"
#Directory to extract services and deploy
STRATOS_HOME="PATH TO SERVICES"
STRATOS_VERSION=1.1.0
SSO_ENABLED="false"

cd setup/dbscripts
mysql -u root -proot < init_quaries.sql
mysql -u root -proot stratos_db < ./mysql.sql
mysql -u root -proot stratos_db < ./billing-mysql.sql

cd bam/sql
mysql -u root -proot stratos_db < ./bam_schema_mysql.sql
cd ../../../..


echo "Setting up the Cloud Manager"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/manager/modules/distribution/service/target/wso2stratos-manager-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Changing configuration files"

echo ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/billing-config.xml

sed -i "s/<url>jdbc:mysql:\/\/localhost:3306\/billing<\/url>/<url>jdbc:mysql:\/\/localhost:3306\/stratos_db<\/url>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/billing-config.xml
sed -i "s/<userName>billing<\/userName>/<userName>root<\/userName>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/billing-config.xml
sed -i "s/<password>billing<\/password>/<password>root<\/password>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/billing-config.xml
sed -i "s/<!--HostName>www.wso2.org<\/HostName-->/<HostName>cloud-test.wso2.com<\/HostName>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/carbon.xml
sed -i "s/identity.cloud.wso2.com/cloud-test.wso2.com:9444/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/identity.xml
sed -i "s/<IdentityProviderURL>https:\/\/localhost:9443\/samlsso<\/IdentityProviderURL>/<IdentityProviderURL>https:\/\/cloud-test.wso2.com:9444\/samlsso<\/IdentityProviderURL>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/identity.xml
sed -i "s/<url>jdbc:h2:repository\/database\/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE<\/url>/<url>jdbc:mysql:\/\/localhost:3306\/stratos_db<\/url>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/registry.xml
sed -i "s/<userName>wso2carbon<\/userName>/<userName>root<\/userName>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/registry.xml
sed -i "s/<password>wso2carbon<\/password>/<password>root<\/password>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/registry.xml
sed -i "s/<driverName>org.h2.Driver<\/driverName>/<driverName>com.mysql.jdbc.Driver<\/driverName>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/registry.xml
sed -i "s/<Property name=\"url\">jdbc:h2:repository\/database\/WSO2CARBON_DB<\/Property>/<Property name="url">jdbc:mysql:\/\/localhost:3306\/stratos_db<\/Property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/user-mgt.xml
sed -i "s/<Property name=\"userName\">wso2carbon<\/Property>/<Property name="userName">root<\/Property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/user-mgt.xml
sed -i "s/<Property name=\"password\">wso2carbon<\/Property>/<Property name="password">root<\/Property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/user-mgt.xml
sed -i "s/<Property name=\"driverName\">org.h2.Driver<\/Property>/<Property name="driverName">com.mysql.jdbc.Driver<\/Property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf/user-mgt.xml

sed -i "s/<property name=\"org.wso2.ws.dataservice.driver\">org.h2.Driver<\/property>/<property name=\"org.wso2.ws.dataservice.driver\">com.mysql.jdbc.Driver<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/*.dbs
sed -i "s/<property name=\"org.wso2.ws.dataservice.protocol\">jdbc:h2:repository\/database\/WSO2BAM_DB<\/property>/<property name=\"org.wso2.ws.dataservice.protocol\">jdbc:mysql:\/\/localhost:3306\/stratos_db<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/*.dbs
sed -i "s/<property name=\"org.wso2.ws.dataservice.user\">wso2bam<\/property>/<property name=\"org.wso2.ws.dataservice.user\">root<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/*.dbs
sed -i "s/<property name=\"org.wso2.ws.dataservice.password\">wso2bam<\/property>/<property name=\"org.wso2.ws.dataservice.password\">root<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/*.dbs

sed -i "s/<property name=\"org.wso2.ws.dataservice.protocol\">jdbc:h2:repository\/database\/WSO2CARBON_DB<\/property>/<property name=\"org.wso2.ws.dataservice.protocol\">jdbc:mysql:\/\/localhost:3306\/stratos_db<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/MeteringSummaryGenerationDS.dbs
sed -i "s/<property name=\"org.wso2.ws.dataservice.user\">wso2carbon<\/property>/<property name=\"org.wso2.ws.dataservice.user\">root<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/MeteringSummaryGenerationDS.dbs
sed -i "s/<property name=\"org.wso2.ws.dataservice.password\">wso2carbon<\/property>/<property name=\"org.wso2.ws.dataservice.password\">root<\/property>/g" ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/deployment/server/dataservices/MeteringSummaryGenerationDS.dbs



echo "Copying configuration files"
cp setup/cloud-manager/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/cloud-manager/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-manager-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Identity Server"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-is-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/is/modules/distribution/service/target/wso2stratos-is-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-is-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/is/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-is-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/is/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-is-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-is-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up Governance Registry"
echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-governance-${STRATOS_VERSION}
echo "done"

echo "Unzipping "
unzip -qu $CARBON_HOME/products/greg/modules/distribution/service/target/wso2stratos-governance-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-governance-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/greg/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-governance-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/greg/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-governance-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-governance-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Appserver"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-as-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/appserver/modules/distribution/service/target/wso2stratos-as-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-as-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/as/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-as-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/as/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-as-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-as-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Business Activity Monitor"
echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/bam/modules/distribution/service/target/wso2stratos-bam-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/bam/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}/repository/conf
cp setup/bam/repository/deployment/server/dataservices/*.dbs  ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}/repository/deployment/server/dataservices/
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/bam/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-bam-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Data Services Server"
echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-dss-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/dss/modules/distribution/service/target/wso2stratos-dss-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-dss-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/dss/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-dss-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/dss/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-dss-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-dss-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Business Process Server"
echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-bps-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/bps/modules/distribution/service/target/wso2stratos-bps-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-bps-${STRATOS_VERSION}/repository/components/lib
echo "done"
echo "Copying configuration files"
cp setup/bps/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-bps-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/bps/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-bps-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-bps-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Business Rules Server"
echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-brs-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/brs/modules/distribution/service/target/wso2stratos-brs-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-brs-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/brs/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-brs-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/brs/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-brs-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-brs-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Complex Event Processor"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-cep-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/cep/modules/distribution/service/target/wso2stratos-cep-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-cep-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/cep/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-cep-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/cep/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-cep-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-cep-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Enterprise Service Bus"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-esb-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/esb/modules/distribution/service/target/wso2stratos-esb-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-esb-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/esb/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-esb-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/esb/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-esb-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-esb-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Gadget Server"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-gs-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/gs/modules/distribution/service/target/wso2stratos-gs-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-gs-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/gs/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-gs-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/gs/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-gs-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-gs-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi


echo "Setting up the Message Broker"

echo "Removing old files"
rm -rf ${STRATOS_HOME}/wso2stratos-mb-${STRATOS_VERSION}
echo "done"

echo "Unzipping"
unzip -qu $CARBON_HOME/products/mb/modules/distribution/service/target/wso2stratos-mb-${STRATOS_VERSION}.zip -d ${STRATOS_HOME}
echo "Done"

echo "Copying mysql driver"
cp setup/stratos/jars//mysql-connector-java-5.1.12-bin.jar ${STRATOS_HOME}/wso2stratos-mb-${STRATOS_VERSION}/repository/components/lib
echo "done"

echo "Copying configuration files"
cp setup/mb/repository/conf/*.xml  ${STRATOS_HOME}/wso2stratos-mb-${STRATOS_VERSION}/repository/conf
echo "done"

if [ "$SSO_ENABLED" = "true" ]; then
	echo "SSO provider functionality is enabled"
	cp setup/mb/repository/conf/sso/user-mgt.xml  ${STRATOS_HOME}/wso2stratos-mb-${STRATOS_VERSION}/repository/conf
else
	echo "SSO provider functionality is disabled"
	rm ${STRATOS_HOME}/wso2stratos-mb-${STRATOS_VERSION}/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*
fi




