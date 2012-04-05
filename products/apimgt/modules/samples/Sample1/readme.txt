Accessing Delicious API
=======================


Test Delicious account details
------------------------------

user name: apimanager
password: apimanager@123


Testing with cURL
-----------------

Get posts:
----------

direct call to real endpoint:
curl -u apimanager:apimanager@123 https://api.del.icio.us/v1/posts/get

through gateway: 
curl -k -H "OAuth :Bearer 1234foo"  -u apimanager:apimanager@123 https://localhost:8243/deli/posts/get


Get Tags:
---------

direct call to real endpoint:
curl -k -u apimanager:apimanager@123 https://api.del.icio.us/v1/tags/get

through gateway:
curl -k -H "OAuth :Bearer 1234foo" -u apimanager:apimanager@123 https://localhost:8243/deli/tags/get

