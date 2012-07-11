#!/bin/bash
KEYSTONE_INSTALL=1
GLANCE_INSTALL=1
LOGFILE=/var/log/nova/nova-restart.log

echo "Starting services ..."

if [ ! -z ${KEYSTONE_INSTALL} ]
then
        start keystone 2>&1 >> ${LOGFILE}
fi

if [ ! -z ${GLANCE_INSTALL} ]
then
        start glance-api 2>&1 >> ${LOGFILE}
        start glance-registry 2>&1 >> ${LOGFILE}
fi

for P in $(ls /etc/init/nova* | cut -d'/' -f4 | cut -d'.' -f1)
do
        start ${P} 2>&1 >> ${LOGFILE}
done
