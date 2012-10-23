#!/bin/bash
username=$1
svnpasswd=$2
appfolder=$3
svnurl=$4
LOG=$5
#echo "username:$username"
#echo "passwd:$svnpasswd"
#echo "appfolder:$appfolder"
#echo "svnurl:$svnurl"
#echo "logfile:$LOG"

work=/opt/work/$username/$appfolder

svnurl=$svnurl/$username/$appfolder
if [ -d /opt/work/$username/$appfolder ]; then
    cd /opt/work/$username/$appfolder
    output=`svn update --no-auth-cache --username=$username --password=$svnpasswd ./`
    output=${output:0:11}
    if [ "$output" = "At revision" ]; then
        echo "`date`[server] INFO:update successful" >> $LOG
        /opt/deployer.sh $work $LOG
    else
        echo "`date`[server] INFO:update failed. Retrying" >> $LOG
        for i in {1..5}
        do
            sleep 5
            output=`svn update --no-auth-cache --username=$username --password=$svnpasswd ./`
            output=${output:0:11}
            if [ "$output" = "At revision" ]; then
                echo "`date`[server] INFO:update successful" >> $LOG
                /opt/deployer.sh $work $LOG
                break;
            else
                echo "`date`[server] INFO:update failed" >> $LOG
                continue
            fi
        done
    fi
    cd ..
else
    output=`svn co --no-auth-cache --username=$username --password=$svnpasswd $svnurl /opt/work/$username/$appfolder`
    output=${output:0:11}
    if [ "$output" = "Checked out" ]; then
        echo "`date`[server] INFO:checkout successful" >> $LOG
        /opt/deployer.sh $work $LOG
    else
        echo "`date`[server] INFO:checkout failed. Retrying" >> $LOG
        for i in {1..5}
        do
            sleep 5
            output=`svn co --no-auth-cache --username=$username --password=$svnpasswd $svnurl /opt/work/$username/$appfolder`
            output=${output:0:11}
            echo $output
            if [ "$output" = "Checked out" ]; then
                echo "`date`[server] INFO:checkout successful" >> $LOG
                /opt/deployer.sh $work $LOG
                break;
            else
                echo "`date`[server] INFO:checkout failed" >> $LOG
                continue
            fi
        done
    fi
fi


