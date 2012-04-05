#!/bin/bash
set -e
STARTUP_DELAY=60
STRATOS_DIR=`pwd`
if [ -n "$STRATOS_DIR" ] ; then
	echo "STRATOS_DIR environment variable is set to $STRATOS_DIR"
else
	echo "Please set the STRATOS_DIR environment variable"
	exit
fi

if [ -n "$STARTUP_DELAY" ] ; then
	echo "STARTUP_DELAY environment variable is set to $STARTUP_DELAY"
else
	echo "STARTUP DELAY environment variable is not set, using the default value of 50 seconds"
fi

if [ "$#" -lt 2 ]; then
        echo ""
        echo "pass one of the follwoing parameters"
        echo ""
        echo "start <product-names separated by a space>    	: to start particular set of products"
        echo "stop <product-names separated by a space>     	: to stop particular set of products"
        echo "start all  	                  		: to start all servers"
        echo "stop all     	                		: to stop all servers"

        echo ""
	echo "following products are available"
        echo ""
	cd $STRATOS_DIR
	for i in wso2stratos*
	do
		echo "${i}"
	done
        echo ""
        exit
fi

if [ ${1} == "start" ]
then
	if [ ${2} == "all" ]
	then
		cd ${STRATOS_DIR}
		for i in wso2stratos-*
		do
			#cd /${STRATOS_DIR}/${i}/bin
			#./wso2server.sh start
			echo "Starting ${i}"
			${STRATOS_DIR}/${i}/bin/wso2server.sh > /dev/null &2>1
#			nohup ./wso2server.sh &
			sleep $STARTUP_DELAY
			echo "Done"
		done
	else
		for (( i = 2 ; i <= $#; i++ )) 
		do
    		eval var=\${$i}
    		echo $var
			cd ${STRATOS_DIR}/${var}/bin
			#sh wso2server.sh start
			${STRATOS_DIR}/${var}/bin/wso2server.sh > /dev/null &2>1
			sleep $STARTUP_DELAY
		done
	fi

		
elif [ ${1} == "stop" ] 
then

	if [ ${2} == "all" ]
	then
		cd ${STRATOS_DIR}
		for i in wso2stratos-*
		do
			cd /${STRATOS_DIR}/${i}/bin
			pkill -9 java
#			/${STRATOS_DIR}/bin/wso2server.sh > /dev/null &2>1
#			./wso2server.sh stop
			sleep 1
		done
	else
		for (( i = 2 ; i <= $#; i++ )) 
		do
    		eval var=\${$i}
    		echo $var
			cd ${STRATOS_DIR}/${var}/bin
			sh wso2server.sh stop
		done
	fi
else 
	echo "Error"
fi

