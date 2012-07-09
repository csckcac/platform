echo ""
echo "Saving stream definition 1 with POST: "
echo ""
curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/streams/ --data @streamdefn1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 1: "
echo ""
curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/stream/stockquote.stream/1.0.2/ --data @events1.json -H "Accept: application/json" -H "Content-type: application/json" -X POST 
echo ""
echo "Saving stream definition 2 with POST: "
echo ""
curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/streams/ --data @streamdefn2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST
echo ""
echo "Posting events to stream definition 2: "
echo ""
curl -k --user admin:admin https://localhost:9443/datareceiver/1.0.0/stream/stockquote.stream/2.0.0 --data @events2.json -H "Accept: application/json" -H "Content-type: application/json" -X POST 
