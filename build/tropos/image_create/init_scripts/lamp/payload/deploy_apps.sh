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

tenant=$1
controller_ip=$2
app_path=$3
docroot=$4
duration=$5
work=/opt/payload/work

if [ ! -d $work ]; then
	mkdir $work
fi


pushd $work
ssh -i ../wso2-key root@$controller_ip "cd $app_path/$tenant; ls ./" | grep "zip" > ./app_list
if [ ! -e ./app_list.prev ]; then
    scp -i ../wso2-key root@$controller_ip:$app_path/$tenant/*.zip ./
    for a in *.zip;
    do
        if [ -e $a ]; then
            unzip -q $a;rm -f $a;
        fi
    	b=${a%".zip"}
        if [ -d $docroot/$b ]; then
    	    rm -rf $docroot/$b
        fi
    	mv $b $docroot/
        echo "added $docroot/$b"
    done
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
            if [ -d $docroot/$h ]; then
                rm -rf $docroot/$h
                echo "deleted $docroot/$h"
            fi
        fi
        z=0
    done
fi

cp -f ./app_list ./app_list.prev

ssh -i ../wso2-key root@$controller_ip "cd $app_path/$tenant;find ./ -mmin $duration" | grep "zip" > ./updated_app_list
w=`cat ./updated_app_list`
for i in $w; 
do 
    scp -i ../wso2-key root@$controller_ip:$app_path/$tenant/$i ./
    unzip -q $i;rm -f $i;
    j=${i%".zip"}
    if [ -d $docroot/$j ]; then
    	rm -rf $docroot/$j
    fi
    mv $j $docroot/
    echo "added $docroot/$j"
done
popd

