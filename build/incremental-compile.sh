#!/bin/bash

export JAVA_HOME=/opt/java/jdk1.6/
export MAVEN_HOME=/opt/java/maven/
#export ANT_HOME=/opt/apache-ant-1.8.0
#export PATH=$ANT_HOME/bin:$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
export MAVEN_OPTS="-Xmx2048m -Xms1024m -XX:PermSize=512m -XX:MaxPermSize=512m"
export BUILD_COMMAND="mvn clean install -Dmaven.test.skip=true -Dbuild.incremental -o $1"
export DEPLOY_COMMAND="mvn clean deploy -Dmaven.test.skip=true $1"

STRATOS_SRC_HOME=/home/stratos/stratos
STRATOS_BIN_HOME=/home/carbon/public_html/releases/stratos/1.0.0
STRATOS_MAIL_LIST=nightly-build-notify-group@wso2.com,senaka@wso2.com,samisa@wso2.com,azeez@wso2.com,isuru@wso2.com,miyuru@wso2.com,waruna@wso2.com,milinda@wso2.com,amila@wso2.com,heshan@wso2.com,sumedha@wso2.com,supun@wso2.com,amilaj@wso2.com,thilinab@wso2.com,shankar@wso2.com
STRATOS_BUILD_FAILED="WSO2 Stratos Continuous Build has FAILED"
STRATOS_BUILD_PASSED="WSO2 Stratos Continuous Build was SUCCESSFUL"
STRATOS_BUILD_CANNOT_PROCEED="WSO2 Stratos Continuous Build Cannot Proceed - Another build already in progress"

check_errors_n_send_mail() {
    set +v
    BUILD_STATUS=`tail -n50 $STRATOS_SRC_HOME/build.log | grep "BUILD SUCCESSFUL"`
    if [ "$BUILD_STATUS" = "" ]; then
        ERROR_MESSAGE=`egrep -A 40 -B 30 "( FAILURE$)|( ERROR$)|(svn[:] Failed)" $STRATOS_SRC_HOME/build.log`
        if [ "$ERROR_MESSAGE" != "" ]; then
            rm $STRATOS_SRC_HOME/build.lock
            echo "$ERROR_MESSAGE" | mail -s "$STRATOS_BUILD_FAILED" $STRATOS_MAIL_LIST
           exit;
        fi
    fi
    set -v
}
build_carbon_core() {
        cd $STRATOS_SRC_HOME/carbon/core
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

build_carbon_components() {
        cd $STRATOS_SRC_HOME/carbon/components
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

build_carbon_features() {
        cd $STRATOS_SRC_HOME/carbon/features
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

build_stratos_components() {
        cd $STRATOS_SRC_HOME/stratos/components
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

build_stratos_features() {
        cd $STRATOS_SRC_HOME/stratos/features
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

build_stratos_samples() {
        cd $STRATOS_SRC_HOME/stratos/samples/shopping-cart
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}

if test -e $STRATOS_SRC_HOME/build.lock
then
    echo "Another build is already in progress."
    echo "Another build is already in progress."  | mail -s "$STRATOS_BUILD_CANNOT_PROCEED" $STRATOS_MAIL_LIST 
    exit
fi

set -v

if test -d "$1"
then
  STRATOS_SRC_HOME=$1
fi

echo "" > $STRATOS_SRC_HOME/build.lock
exec &> $STRATOS_SRC_HOME/build.log

cd $STRATOS_SRC_HOME/carbon
svn up > $STRATOS_SRC_HOME/carbon_svn.log
svn info
date

egrep '^U   ' $STRATOS_SRC_HOME/carbon_svn.log | cut -dU -f2 | cut -d/ -f1-2 | sort -u > $STRATOS_SRC_HOME/carbon_compile.list
while read TOC; do cd $STRATOS_SRC_HOME/carbon/$TOC;bash -c "$BUILD_COMMAND"; done < $STRATOS_SRC_HOME/carbon_compile.list

#build_carbon_core
#build_carbon_components
#build_carbon_features

#cd $STRATOS_SRC_HOME/carbon/features
#bash -c "$BUILD_COMMAND"
#check_errors_n_send_mail

cd $STRATOS_SRC_HOME/stratos
svn up > $STRATOS_SRC_HOME/stratos_svn.log
svn info
date

egrep '^U   ' $STRATOS_SRC_HOME/stratos_svn.log | cut -dU -f2 | cut -d/ -f1-2 | sort -u > $STRATOS_SRC_HOME/stratos_compile.list
while read TOC; do cd $STRATOS_SRC_HOME/stratos/$TOC;bash -c "$BUILD_COMMAND"; done < $STRATOS_SRC_HOME/stratos_compile.list
#build_stratos_components
#build_stratos_features
#build_stratos_samples

export DATE_TIME=`date '+%Y-%m-%d_%H-%M-%S'`

echo $DATE_TIME

set +v
tail -n50 $STRATOS_SRC_HOME/build.log | mail -s "$STRATOS_BUILD_PASSED" $STRATOS_MAIL_LIST
rm $STRATOS_SRC_HOME/build.lock
