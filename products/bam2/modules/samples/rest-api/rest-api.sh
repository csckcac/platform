echo ""
echo "Saving stream definition 1 with POST: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/ --data @streamdefn1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Loading stream definition 1: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/org.wso2.bam.restapi/1.0.2 -X GET 
echo ""
echo "Posting events to stream definition 1: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/org.wso2.bam.restapi/1.0.2 --data @events1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST 
echo ""
echo "Saving stream definition 2 with POST: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/ --data @streamdefn2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Loading All event streams: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/ -X GET 
echo ""
echo "Posting events to stream definition 2: "
echo ""
curl -k --user admin:admin https://localhost:9443/eventstreams/org.wso2.bam.newrestapi/2.0.0 --data @events2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST 
