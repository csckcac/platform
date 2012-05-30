#!/bin/sh
START=$1
COUNT=$2
SERVER=http://localhost:9763
curl -X POST -c cookies $SERVER/apiprovider/site/blocks/user/login/ajax/login.jag -d 'action=login&username=admin&password=admin';

curl -X POST -b cookies $SERVER/apiprovider/site/blocks/item-add/ajax/add.jag -d "action=addAPI&name=TwitterSearch&version=1.0.0&description=Twitter Search&endpoint=http://search.twitter.com&wsdl=&tags=twitter,open,social&tier=Silver&thumbUrl=https://lh6.ggpht.com/RNc8dD2hXG_rWGzlj09ZwAe1sXVvLWkeYT3ePx7zePCy4ZVV2XMGIxAzup4cKM85NFtL=w124&context=/twitter&tiersCollection=Gold&resourceCount=0&resourceMethod-0=POST&uriTemplate-0=/*";

curl -X POST -b cookies $SERVER/apiprovider/site/blocks/life-cycles/ajax/update-status.jag -d "name=TwitterSearch&version=1.0.0&provider=admin&status=PUBLISHED&publishToGateway=true&action=updateStatus";
