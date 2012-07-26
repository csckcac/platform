JAVA_HOME=/opt/jdk1.6.0_24
export JAVA_HOME
PATH=$PATH:/usr/bin:$JAVA_HOME/bin
export PATH

M3_HOME=/opt/maven
export M3_HOME
export PATH=$M3_HOME/bin:$PATH

ANT_HOME=/usr/share/ant
export ANT_HOME
export PATH=$ANT_HOME/bin:$PATH

cd /home/appfactory/wso2appfactory_m3

APPFACTORY_HOME=`pwd`/setup/appfactory/wso2appfactory-1.0.0-SNAPSHOT
CONTROLLER_HOME=`pwd`/setup/controller/wso2stratos-manager-2.0.0-SNAPSHOT
DEV_CLOUD_AS_HOME=`pwd`/setup/dev-cloud/wso2as-5.0.0-SNAPSHOT
TEST_CLOUD_AS_HOME=`pwd`/setup/test-cloud/wso2as-5.0.0-SNAPSHOT
PROD_CLOUD_AS_HOME=`pwd`/setup/prod-cloud/wso2as-5.0.0-SNAPSHOT
echo "*******Starting to set up Appfactory************"
#resources maven
#unzip resources/apache-maven-2.2.1-bin.zip
#resources java
#sh resources/jdk-6u25-linux-x64.bi
SETUP_DIR=`pwd`/setup;
echo $SETUP_DIR
if [ ! -d "$SETUP_DIR" ];then
echo "Setting up the deployment first time"
#cd /home/appfactory/wso2appfactory_m3
/usr/bin/unzip  -q resources/m2-repo.zip -d /home/appfactory
mvn clean install
mkdir setup
#configure appfactory
mkdir setup/appfactory
echo "Setting up Appfactory........"
cp -r  appfactory/target/wso2appfactory-1.0.0-SNAPSHOT  setup/appfactory

cp resources/wso2server.sh $APPFACTORY_HOME/bin
cp resources/appfactory.xml $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml
cp resources/appfactory-user-mgt.xml $APPFACTORY_HOME/repository/conf/user-mgt.xml
cp resources/appfactory-registry.xml $APPFACTORY_HOME/repository/conf/registry.xml
cp resources/appfactory.xml $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml
cp resources/appfactory-carbon.xml $APPFACTORY_HOME/repository/conf/carbon.xml
cp resources/appfactory-axis2.xml $APPFACTORY_HOME/repository/conf/axis2/axis2.xml
cp resources/appfactory-confirmation-email-config.xml $APPFACTORY_HOME/repository/conf/email/confirmation-email-config.xml
cp resources/appfactory-sso-idp-config.xml $APPFACTORY_HOME/repository/conf/sso-idp-config.xml

cp resources/mysql-connector-java-5.1.12-bin.jar $APPFACTORY_HOME/repository/components/lib
cp resources/org.wso2.carbon.appfactory.events.notification-1.0.0-SNAPSHOT.jar $APPFACTORY_HOME/repository/components/dropins

#configure controller
mkdir setup/controller
echo "Setting up Controller........"
cp -r controller/target/wso2stratos-manager-2.0.0-SNAPSHOT setup/controller

cp resources/cloud-manager-user-mgt.xml $CONTROLLER_HOME/repository/conf/user-mgt.xml
cp resources/cloud-manager-registry.xml $CONTROLLER_HOME/repository/conf/registry.xml
cp resources/cloud-manager-carbon.xml $CONTROLLER_HOME/repository/conf/carbon.xml
cp resources/cloud-manager-tenant-mgt.xml $CONTROLLER_HOME/repository/conf/tenant-mgt.xml
cp resources/cloud-manager-stratos.xml $CONTROLLER_HOME/repository/conf/multitenancy/stratos.xml

mkdir $CONTROLLER_HOME/repository/conf/appfactory
cp $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml $CONTROLLER_HOME/repository/conf/appfactory

cp resources/org.wso2.carbon.appfactory.common-1.0.0-SNAPSHOT.jar $CONTROLLER_HOME/repository/components/dropins
cp resources/org.wso2.carbon.appfactory.tenant.roles-1.0.0-SNAPSHOT.jar $CONTROLLER_HOME/repository/components/dropins
cp resources/org.wso2.carbon.appfactory.userstore-1.0.0-SNAPSHOT.jar $CONTROLLER_HOME/repository/components/dropins
cp resources/mysql-connector-java-5.1.12-bin.jar $CONTROLLER_HOME/repository/components/lib

#configure dev cloud
mkdir setup/dev-cloud
echo "Setting up Dev Cloud........"
cp -r as/target/wso2as-5.0.0-SNAPSHOT  setup/dev-cloud

cp resources/cloud-manager-user-mgt.xml $DEV_CLOUD_AS_HOME/repository/conf/user-mgt.xml
cp resources/cloud-manager-registry.xml $DEV_CLOUD_AS_HOME/repository/conf/registry.xml
cp resources/dev-cloud-carbon.xml $DEV_CLOUD_AS_HOME/repository/conf/carbon.xml
cp resources/org.wso2.carbon.appfactory.userstore-1.0.0-SNAPSHOT.jar $DEV_CLOUD_AS_HOME/repository/components/dropins
cp resources/mysql-connector-java-5.1.12-bin.jar $DEV_CLOUD_AS_HOME/repository/components/lib

#configure test cloud
mkdir setup/test-cloud
echo "Setting up Testing Cloud........"
cp -r  as/target/wso2as-5.0.0-SNAPSHOT  setup/test-cloud

cp resources/cloud-manager-user-mgt.xml $TEST_CLOUD_AS_HOME/repository/conf/user-mgt.xml
cp resources/cloud-manager-registry.xml $TEST_CLOUD_AS_HOME/repository/conf/registry.xml
cp resources/test-cloud-carbon.xml $TEST_CLOUD_AS_HOME/repository/conf/carbon.xml
cp resources/org.wso2.carbon.appfactory.userstore-1.0.0-SNAPSHOT.jar $TEST_CLOUD_AS_HOME/repository/components/dropins
cp resources/mysql-connector-java-5.1.12-bin.jar $TEST_CLOUD_AS_HOME/repository/components/lib

#configure prod cloud
mkdir setup/prod-cloud
echo "Setting up Prod Cloud........"
cp -r  as/target/wso2as-5.0.0-SNAPSHOT  setup/prod-cloud

cp resources/cloud-manager-user-mgt.xml $PROD_CLOUD_AS_HOME/repository/conf/user-mgt.xml
cp resources/cloud-manager-registry.xml $PROD_CLOUD_AS_HOME/repository/conf/registry.xml
cp resources/prod-cloud-carbon.xml $PROD_CLOUD_AS_HOME/repository/conf/carbon.xml
cp resources/org.wso2.carbon.appfactory.userstore-1.0.0-SNAPSHOT.jar $PROD_CLOUD_AS_HOME/repository/components/dropins
cp resources/mysql-connector-java-5.1.12-bin.jar $PROD_CLOUD_AS_HOME/repository/components/lib


chmod +x $APPFACTORY_HOME/bin/wso2server.sh
chmod +x $CONTROLLER_HOME/bin/wso2server.sh
chmod +x $DEV_CLOUD_AS_HOME/bin/wso2server.sh 
chmod +x $TEST_CLOUD_AS_HOME/bin/wso2server.sh
chmod +x $PROD_CLOUD_AS_HOME/bin/wso2server.sh
MYSQL=`which mysql`
 
Q1="DROP DATABASE IF EXISTS userstore;"
Q2="DROP DATABASE IF EXISTS registry;"
Q3="CREATE DATABASE userstore;"
Q4="USE userstore;"
Q5="SOURCE $APPFACTORY_HOME/dbscripts/mysql.sql;"
Q6="CREATE DATABASE registry;"
Q7="USE registry;"

SQL="${Q1}${Q2}${Q3}${Q4}${Q5}${Q6}${Q7}${Q5}"

$MYSQL -uroot -proot -A -e "$SQL";
fi


cd  $APPFACTORY_HOME/bin
nohup ./wso2server.sh  &
sleep 1m
cd $CONTROLLER_HOME/bin
nohup ./wso2server.sh &

cd $DEV_CLOUD_AS_HOME/bin
nohup ./wso2server.sh & 

cd $TEST_CLOUD_AS_HOME/bin
nohup ./wso2server.sh & 

cd $OROD_CLOUD_AS_HOME/bin
nohup ./wso2server.sh & 

#configure scm manager
#echo "setting up scm manager........"

#unzip -q resources/scm-server-app.zip 
#cp resources/scm-server-config.xml scm-server/conf/server-config.xml
#cp resources/carbon-auth.xml scm-server/repository/config

#echo "add 0.0.0.0 svn.appfactory.wso2.com to /etc/hosts"
echo "*******setting up finished sucessfully************"
