#!/bin/sh
START=$1
COUNT=$2
SERVER=http://localhost:9763
curl -X POST -c cookies $SERVER/apiprovider/services/provider.jag -d 'action=login&username=provider1&password=provider1';
curl -X POST -b cookies $SERVER/apiprovider/services/provider.jag -d 'action=getUser';

curl -X POST -b cookies $SERVER/apiprovider/services/provider.jag -d "action=saveAPI&apiName=PlaceFinder&version=1.0.0&description=Place Finder&endpoint=http://where.yahooapis.com/geocode&wsdl=&tags=finder,open,social&tier=Silver&thumbUrl=http://images.ientrymail.com/webpronews/article_pics/yahoo-placefinder.jpg&context=/placeFinder&resourceCount=0&resourceMethod-0=POST&uriTemplate-0=/*";

curl -X POST -b cookies $SERVER/apiprovider/services/provider.jag -d "action=updateAPI&apiName=PlaceFinder&version=1.0.0&description=Place Finder&endpoint=http://where.yahooapis.com/geocode&wsdl=&tags=finder,open,social&tier=Silver&thumbUrl=http://images.ientrymail.com/webpronews/article_pics/yahoo-placefinder.jpg&context=/placeFinder&status=PUBLISHED&resourceCount=0&resourceMethod-0=GET&uriTemplate-0=/*";




