Accessing Sample Dataservice
=============================

Testing with cURL
-----------------

Get Customer Details:
---------------------

direct call to real endpoint:
curl http://data.stratoslive.wso2.com/services/t/apimanagertest.com/GSpreadSample/getCustomers

through gateway: 
curl -k -H "OAuth :Bearer 1234foo" https://localhost:8243/gs/customers

