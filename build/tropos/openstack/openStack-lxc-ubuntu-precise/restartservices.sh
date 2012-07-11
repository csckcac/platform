#!/bin/bash
KEYSTONE_INSTALL=1
GLANCE_INSTALL=1
LOGFILE=/var/log/nova/nova-restart.log

echo "Restarting services ..."

if [ ! -z ${KEYSTONE_INSTALL} ]
then
        stop keystone 2>&1 >> ${LOGFILE}
        start keystone 2>&1 >> ${LOGFILE}
fi

if [ ! -z ${GLANCE_INSTALL} ]
then
        stop glance-api 2>&1 >> ${LOGFILE}
        stop glance-registry 2>&1 >> ${LOGFILE}
        start glance-api 2>&1 >> ${LOGFILE}
        start glance-registry 2>&1 >> ${LOGFILE}
fi

for P in $(ls /etc/init/nova* | cut -d'/' -f4 | cut -d'.' -f1)
do
        stop ${P} 2>&1 >> ${LOGFILE}
        start ${P} 2>&1 >> ${LOGFILE}
done
