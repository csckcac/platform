#!/bin/bash
DOCROOT="/var/www"
pushd $DOCROOT/deploy
svn --no-auth-cache --username=$1 --password=$2 update
echo "updated **************" >> /tmp/test1.txt
popd
