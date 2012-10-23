#!/bin/bash

# ----------------------------------------------------------------------------
#  Copyright 2005-20012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
#!/usr/bin/env bash
work=$1
LOG=$2
webroot=/var/www

# Variables taken from the payload passed to the instance
export TENANT=""
export APP_PATH=""
export CRON_DURATION=5
export CONTROLLER_IP=""
echo ---------------------------- >> $LOG


pushd $work


ls ./|grep "zip"|grep -v ".svn" > ./app_list
m=`cat ./app_list`

if [ ! -e ./app_list.prev ]; then
    for a in $m;
    do
        if [ -e $a ]; then
            unzip -q $a;
        fi
    	b=${a%".zip"}

        if [ -d $webroot/$b ]; then
    	    rm -rf $webroot/$b
        fi
        echo "`date`[server] INFO:Deploying application********************:$b" >> $LOG
        mv $b $webroot/
        echo "`date`[server] INFO:Deployed application $webroot/$b" >> $LOG
    done
    if [ -z "$m" ]; then
    	echo "`date`[server] INFO:app list is empty******************" >> $LOG
    else
    	mv ./app_list ./app_list.prev
    fi
    exit 0
else
    x=`cat ./app_list.prev`
    y=`cat ./app_list`
    z=0
    for f in $x;
    do
        for g in $y;
        do
            if [ $g == $f ]; then
                z=1; break;

            fi
        done
        if [ $z == 0 ]; then
            h=${f%".zip"}
            if [ -d $webroot/$h ]; then
                rm -rf $webroot/$h
                echo "`date`[server] INFO:Undeployed $webroot/$h" >> $LOG
            fi
        fi
        z=0
    done
fi

if [ -f ./app_list.prev ]; then
    mv ./app_list.prev ./app_list.prev.prev
fi
mv ./app_list ./app_list.prev

find ./ -mmin -$CRON_DURATION | grep "zip"|grep -v ".svn" > ./updated_app_list
w=`cat ./updated_app_list`
for i in $w; 
do 
    unzip -q $i;
    j=${i%".zip"}
    if [ -d $webroot/$j ]; then
        rm -rf $webroot/$j
    fi
    echo "`date`[server] INFO:Re-deploying web application ********************:$b" >> $LOG
    mv $j $webroot/
    echo "`date`[server] INFO:Re-deployed application $webroot/$j" >> $LOG
done
popd

