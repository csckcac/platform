
Samples -  WSO2 Complex Event Processing Server
------------------------------------------------


There are five samples in WSO2 CEP Server.

 1. Fusion CEP Engine used with Local Broker
 2. Fusion CEP Engine used with JMS Broker
 3. Esper CEP Engine used with Local Broker
 4. Esper CEP Engine used with JMS Broker
 5. Fusion CEP Engine using custom event objects with Local Broker


  Before running all these samples, it needs to have the required
  buckets deployed in the CEP Server.

  To deploy required buckets ant services follow the steps bellow;

  Step 01: Start the CEP server.

  Step 02: Install the Esper Feature from GPL repository (Follow the user guide on CEP)

  Step 03: Stop the server.

  Step 04 : Make sure server is stopped

  Step 05 : Run "ant" from here.

  Step 06 : Start the server.

  Step 07 : run "ant fusionLocal" for the sample with Fusion Engine with Local Broker
            run "ant fusionJMS" for the sample with Fusion Engine with JMS Broker
            run "ant esperLocal" for the sample with Esper Engine with Local Broker
            run "ant esperJMS" for the sample with Esper Engine with JMS Broker.
            run "ant fusionLocalCustomEvent" for the sample with Fusion Engine using custom event objects with Local Broker


