Twitter Search
==============

Testing with cURL
-----------------

Search for string "wso2", limit results for 5 entries:
-----------------------------------------------------

direct call to real endpoint:
curl -d "q=wso2&count=5" http://search.twitter.com/search.atom

through gateway: 
curl -k -d "q=wso2&count=5" -H "OAuth :Bearer 123foo" https://localhost:8243/twitter/search




