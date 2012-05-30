#!/bin/sh
START=$1
COUNT=$2
SERVER=http://localhost:9763
curl -X POST -c cookies $SERVER/apiprovider/site/blocks/user/login/ajax/login.jag -d 'action=login&username=admin&password=admin';

curl -X POST -b cookies $SERVER/apiprovider/site/blocks/item-add/ajax/add.jag -d "action=addAPI&name=PlaceFinder&version=1.0.0&description=Place Finder&endpoint=http://where.yahooapis.com/geocode&wsdl=&tags=finder,open,social&tier=Silver&thumbUrl=http://images.ientrymail.com/webpronews/article_pics/yahoo-placefinder.jpg&context=/placeFinder&tiersCollection=Gold&resourceCount=0&resourceMethod-0=POST&uriTemplate-0=/*";

curl -X POST -b cookies $SERVER/apiprovider/site/blocks/life-cycles/ajax/update-status.jag -d "name=PlaceFinder&version=1.0.0&provider=admin&status=PUBLISHED&publishToGateway=true&action=updateStatus";




