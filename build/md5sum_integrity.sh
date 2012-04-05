#!/bin/bash

#For removal of all *.md5 files, execute: find -name "*.md5" -exec rm {} \;
#Usage:
#       chk_integrity.sh "/path/to/directory"


DATE=`date +%Y.%m.%d_%H.%M.%S.%N`

SHIFT=$[`tput cols`-10]
MOVE="\\033["$SHIFT"G"
DEFAULT="\\033[0;39m"
RED="\\033[1;31m"
GREEN="\\033[1;32m"
YELLOW="\\033[1;33m"
BLUE="\\033[1;34m"

logError_end() {
    echo -e "$1"
}

logOk_end() {
    echo -e "$1"
}

logWarning_end() {
    echo -e "$1"
#    echo -e "$MOVE$GREEN$1$DEFAULT"
}

checkSum() {
    #$1=file
    echo -en CheckSum:\\t$1
    file=`basename $1`
    dir=`dirname $1`
    (cd $dir && md5sum -c -- $file >/dev/null 2>&1 && logOk_end OK || logError_end ERROR)
}

calcSum() {
    #$1=file
    echo -en CalcSum:\\t$1
    file=`basename $1`
    dir=`dirname $1`
    (cd $dir && md5sum -b -- $file >$file.md5 2>/dev/null && logWarning_end CALCULATED || logError_end ERROR)
}


fin_process() {
    while read; do
        #echo $REPLY
        if test -n "`echo $REPLY | grep '\.md5$'`"; then    
            checkSum $REPLY
        else
            if test ! -f "$REPLY.md5"; then
                calcSum $REPLY
            fi
        fi
    done
}


find $1 -type f | fin_process;

exit $?

