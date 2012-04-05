#!/bin/bash

# By default, build all. So set the build task to all. 
# This can be overriden by command line option '-t'. 
# For more information, run the script with '-h' option. 
export BUILD_TASK="all"

# Get command line options. 
while getopts "hot:" flag
do
  case $flag in
    o) export BUILD_OPTS="-o";;
    t) export BUILD_TASK=$OPTARG;;
    h) export BUILD_TASK="help"; 
	echo "Usage: $0 -o -h -t[all|orbit|core|components|features|products|p2]"; 
	exit
        ;;
  esac
done
	
export CARBON_SRC_HOME=/home/carbontrunktrunk/carbon

export CARBON_VERSION=3.2.1
export CARBON_BIN_HOME=/home/carbontrunk/public_html/releases/carbon/$CARBON_VERSION

export JAVA_HOME=/opt/jdk1.6

export MAVEN_HOME=/opt/maven2
export MAVEN_OPTS="-Xmx2048m -Xms1024m -XX:PermSize=512m -XX:MaxPermSize=512m"

export BUILD_COMMAND="mvn clean install $BUILD_OPTS"

if [ "$BUILD_OPTS" = "deploy" ]; then
    export DEPLOY_COMMAND="mvn deploy";
fi

export MAIL_LIST="xxxx@xxx.xxx"
export BUILD_FAILED="WSO2 Carbon Continuous Build has FAILED"
export BUILD_PASSED="WSO2 Carbon Continuous Build was SUCCESSFUL"

export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

# Function that sends mail. 
function send_mail() {

(cat <<EOCAT
From:  "Builder" <xyz@xyz.org>
To: $2
Subject: "$1"
MIME-Version: 1.0
EOCAT
echo -e "$3") | /usr/sbin/sendmail "$2"

}

# Function that checks for errors, fires a mail if error and starts a new build.
check_errors_n_send_mail() {
    set +v
    BUILD_STATUS=`tail -n50 $CARBON_SRC_HOME/build.log | grep "BUILD SUCCESSFUL"`
    if [ "$BUILD_STATUS" = "" ]; then
        rm $CARBON_SRC_HOME/build.lock
        MESSAGE_BODY=`egrep -A 40 -B 30 "( FAILURE$)|( ERROR$)|(svn[:] Failed)" $CARBON_SRC_HOME/build.log`
        send_mail "$BUILD_FAILED" "$MAIL_LIST" "$SVN_INFO $MESSAGE_BODY"
	
        # from 2100 to 0400 build online
	HOUR=`date +%H`
	if test $HOUR -gt 20; then
	    $0 &
	elif test $HOUR -lt 5; then
	    $0 &
	else
	    $0 &
	fi

        exit;
    fi
    set -v
}

# Function that checks for errors, fires a mail if error and continues with the same build.
check_errors_n_send_mail_n_continue() {
    set +v
    BUILD_STATUS=`tail -n50 $CARBON_SRC_HOME/build.log | grep "BUILD SUCCESSFUL"`
    if [ "$BUILD_STATUS" = "" ]; then
        MESSAGE_BODY=`egrep -A 40 -B 30 "( FAILURE$)|( ERROR$)|(svn[:] Failed)" $CARBON_SRC_HOME/build.log`
        send_mail "$BUILD_FAILED `pwd`" "$MAIL_LIST" "$SVN_INFO $MESSAGE_BODY"
    fi
    set -v
}

# Function that checks for errors, fires a mail if error and continues with the same build.
# If no error, it copies the P2 repo.
check_errors_in_p2_n_continue(){
	set +v
    BUILD_STATUS=`tail -n50 $CARBON_SRC_HOME/build.log | grep "BUILD SUCCESSFUL"`
    if [ "$BUILD_STATUS" = "" ]; then
        MESSAGE_BODY=`egrep -A 40 -B 30 "( FAILURE$)|( ERROR$)|(svn[:] Failed)" $CARBON_SRC_HOME/build.log`
        send_mail "$BUILD_FAILED `pwd`" "$MAIL_LIST" "$SVN_INFO $MESSAGE_BODY"
	else
		cp -r target/p2-repo "$CARBON_BIN_HOME/$DATE_TIME"
    fi
    set -v
}

if test -e $CARBON_SRC_HOME/build.lock
then
    echo "Another build is already in progress."
    echo "Another build is already in progress."  | mail -s "$BUILD_FAILED" $MAIL_SENDER $MAIL_LIST 
    exit
fi


if test -d "$1"
then
  CARBON_SRC_HOME=$1
fi

echo "$$" > $CARBON_SRC_HOME/build.lock
exec &> $CARBON_SRC_HOME/build.log


carbon_svn_up() {
        cd $CARBON_SRC_HOME
        svn up
	export SVN_INFO=`svn info`
	echo "$SVN_INFO"
        date
}

build_carbon_all() {
	cd $CARBON_SRC_HOME
	bash -c "$BUILD_COMMAND -Dprofile=nightly-build"
	check_errors_n_send_mail
}

build_carbon_dependencies() {
	#removed axiom savan - add them later if needed
        for f in XmlSchema axis2 wss4j carbon-p2-plugin commons/caching commons/eventing commons/throttle commons/xkms rampart sandesha httpcore transports synapse shindig ode webharvest jsr107cache wsdl4j commons/pwprovider qpid cassandra;
        do
            cd $CARBON_SRC_HOME/dependencies/$f
            bash -c "$BUILD_COMMAND"
            #set +v
            #BUILD_STATUS=`tail -n50 $CARBON_SRC_HOME/build.log | grep "BUILD SUCCESSFUL"`
            #if [ "$BUILD_STATUS" = "" ]; then
            #    break;
            #fi
            #set -v
        done
}

build_carbon_orbit() {
	cd $CARBON_SRC_HOME/orbit
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail
}


# Deploy all SNAPSHOT bundles in orbit.
deploy_carbon_orbit() {
	for f in axiom axis2 axis2-client smooks axis2-json axis2-jaxbri axis2-jibx;
	do
	    cd $CARBON_SRC_HOME/orbit/$f
	    bash -c "$DEPLOY_COMMAND"
	    check_errors_n_send_mail
	done
}

build_carbon_stubs() {
        cd $CARBON_SRC_HOME/service-stubs
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}


build_carbon_core() {
	cd $CARBON_SRC_HOME/core
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail
}

build_carbon_components() {
	cd $CARBON_SRC_HOME/components
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail
}

build_carbon_features() {
	cd $CARBON_SRC_HOME/features
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail
}

build_carbon_samples() {
        cd $CARBON_SRC_HOME/samples/shopping-cart
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail
}


build_carbon_products() {
	cd $CARBON_SRC_HOME/products
	mvn clean

	cd $CARBON_SRC_HOME/products/carbon
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/greg
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/is
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/appserver
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/gs
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/ms
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/dss
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/bam
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/esb
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/bps
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/brs
	bash -c "$BUILD_COMMAND"
	check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/cep
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail_n_continue

	cd $CARBON_SRC_HOME/products/mb
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail_n_continue

	#stratos manager
	cd $CARBON_SRC_HOME/products/manager
        bash -c "$BUILD_COMMAND"
        check_errors_n_send_mail_n_continue

}

copy_carbon_products() {
	cd $CARBON_SRC_HOME/products
	find . -name "wso2*.zip" | xargs ls -l
	find . -name "wso2*.zip" > zips

	export DATE_TIME=`date '+%Y-%m-%d_%H-%M-%S'`

	echo $DATE_TIME
	mkdir "$CARBON_BIN_HOME/$DATE_TIME"
	while read x; do  cp $x "$CARBON_BIN_HOME/$DATE_TIME"; done < zips

        $CARBON_SRC_HOME/build/md5sum_integrity.sh "$CARBON_BIN_HOME/$DATE_TIME"

	rm  "$CARBON_BIN_HOME/latest"
        ln -s "$CARBON_BIN_HOME/$DATE_TIME" "$CARBON_BIN_HOME/latest"
}

build_carbon_p2() {
	cd $CARBON_SRC_HOME/features/repository
	bash -c "$BUILD_COMMAND"
	check_errors_in_p2_n_continue
	#cp -r target/p2-repo "$CARBON_BIN_HOME/$DATE_TIME"
	echo $DATE_TIME
	cp $CARBON_SRC_HOME/build.log "$CARBON_BIN_HOME/$DATE_TIME"

	echo "Carbon  packs are available at : http://10.100.1.43/~carbon/releases/carbon/3.2.0/latest/"
	MESSAGE_BODY=`tail -n50 $CARBON_SRC_HOME/build.log`
	send_mail "$BUILD_PASSED" "$MAIL_LIST" "$SVN_INFO $MESSAGE_BODY"
}

run_all() {
	carbon_svn_up
	build_carbon_all
	run_from_carbon_products
}

run_from_carbon_orbit() {
	build_carbon_orbit
	run_from_carbon_stubs
}

run_from_carbon_stubs() {
        build_carbon_stubs
        run_from_carbon_core
}

run_from_carbon_core() {
	build_carbon_core
	run_from_carbon_components
}


run_from_carbon_components() {
	build_carbon_components
	run_from_carbon_features
}

run_from_carbon_features() {
	build_carbon_features
	run_from_carbon_samples
}

run_from_carbon_samples() {
        build_carbon_samples
        run_from_carbon_products
}


run_from_carbon_products() {
	build_carbon_products
	copy_carbon_products
	run_from_carbon_p2
}

run_from_carbon_p2() {
	build_carbon_p2
}

set -v

case "$BUILD_TASK" in
'all')
run_all
;;
'orbit')
run_from_carbon_orbit
;;
'core')
run_from_carbon_core
;;
'components')
run_from_carbon_components
;;
'features')
run_from_carbon_features
;;
'products')
run_from_carbon_products
;;
'p2')
run_from_carbon_p2
;;
'help')
echo "Usage: $0 -o -h -t[all|orbit|core|components|features|products|p2]"
;;
esac

set +v

check_errors_n_send_mail

rm $CARBON_SRC_HOME/build.lock

# contine with next round of building, if not instructed to pause
if test -e $CARBON_SRC_HOME/pause.lock
then
    exit
fi

# from 2100 to 0400 build online
HOUR=`date +%H`
if test $HOUR -gt 20; then
    $0 &
elif test $HOUR -lt 5; then
    $0 &
else
    $0 -o &
fi

exit
