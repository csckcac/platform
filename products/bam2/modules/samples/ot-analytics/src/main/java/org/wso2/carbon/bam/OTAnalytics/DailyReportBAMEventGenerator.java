package org.wso2.carbon.bam.OTAnalytics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import javax.security.sasl.AuthenticationException;

import com.wso2.analytics.utils.*;

public class DailyReportBAMEventGenerator {
	
	private DataPublisher dataPublisher;
	public static final String STREAM_NAME = "org.wso2.analytics.data";
    	public static final String VERSION = "1.3.0";
	private static String currentDir;
	
	
public static SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy hh:mmaa"); 
    
    
    public static void main(String[] args) throws Exception, InterruptedException {        
        DailyReportBAMEventGenerator agentClient = new DailyReportBAMEventGenerator();
        currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        agentClient.processDailyReport(currentDir + "/src/main/resources/bam_sample.csv");
    }

    public void processDailyReport(String reportFile) throws Exception {
    	MonthsPassedLoader mpl = new MonthsPassedLoader();
    	OTArticle2TagLoader otArticle2TagLoader = new OTArticle2TagLoader(currentDir + "/src/main/resources/ottags.data");
        int recordCount = 0;
        int faluireCount = 0;

        //////event generation for BAM////////        
        String streamId=configureBAM();
        
        BufferedReader reader = new BufferedReader(new FileReader(reportFile));       
        String line = null;        
        //skip first line
        reader.readLine();        
        
        while ((line = reader.readLine()) != null) {
            try {
                String[] tokens = CSVLineParser.tokenizeCSV(line).toArray(new String[0]); 
                String userID = tokens[3];
                String timestampAsStr = (tokens[1]);
                String month = timestampAsStr.split("\\s+")[0];
                int monthsPassed = mpl.getMonthsPassed(timestampAsStr);
                String monthsPasseds = Integer.toString(monthsPassed);
                String userOrg = tokens[0];
                String document = tokens[4];                
                String name = tokens[3];                
                String tag = otArticle2TagLoader.findBestMatchingProduct(tokens[4]);                
                
                if(userID!=null && userOrg!=null && monthsPassed!=0 && document!=null){
                Object[] eventData = new Object[] { userID, month, monthsPasseds, userOrg, document,  name, tag};
                recordCount++;
                if(!streamId.isEmpty()){
                dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), createMetaData(), createCorrelationData(), eventData));                
                }else{
                	System.out.println("Stream Id is empty");
                }
                }
            } catch (Exception e) {
                faluireCount++;
                System.out.println("Error processing "+ line + " caused by "+ e.getMessage());
                
            }
        }
        
        
        
        reader.close(); 
              
        dataPublisher.stop();
        System.out.println(recordCount + " events generated and processing failed for " + faluireCount + "recrods");
             
        
    }



    private String configureBAM() throws AgentException,
                                                  MalformedStreamDefinitionException,
                                                  StreamDefinitionException,
                                                  DifferentStreamDefinitionAlreadyDefinedException,
                                                  MalformedURLException,
                                                  AuthenticationException,
                                                  NoStreamDefinitionExistException,
                                                  TransportException, SocketException,
                                                  org.wso2.carbon.databridge.commons.exception.AuthenticationException,SocketException {
    	String host;
    	host = getLocalAddress().getHostAddress();
    	dataPublisher = new DataPublisher("tcp://" + host + ":7611", "admin", "admin");
    	String streamId=null;
    	try{
    		streamId = dataPublisher.findStream(STREAM_NAME, VERSION);
    		System.out.println("Stream already defined");
    	}catch(NoStreamDefinitionExistException e){
    		streamId = dataPublisher.defineStream("{" +
                    "  'name':'" + STREAM_NAME + "'," +
                    "  'version':'" + VERSION + "'," +
                    "  'nickName': 'OTAnalytics'," +
                    "  'description': 'Page View'," +
                    "  'metaData':[" +
                    "          {'name':'ipAdd','type':'STRING'}" +
                    "  ]," +
                    "  'payloadData':[" +
                    "          {'name':'userID','type':'STRING'}," +                    
                    "          {'name':'month','type':'STRING'}," +
                    "          {'name':'monthsPassed','type':'STRING'}," +
                    "          {'name':'userOrg','type':'STRING'}," +
                    "          {'name':'document','type':'STRING'}," +                    
                    "          {'name':'name','type':'STRING'}," +                       
                    "          {'name':'tag','type':'STRING'}" +                     
                    "  ]" +
                    "}");
    	}
    	
    	return streamId;
		
	}



    private Object[] createMetaData() {
        Object[] objects = new Object[1];
        objects[0] = "127.0.0.1";
        return objects;
    }

    private Object[] createCorrelationData() {
        return null;
    }
    
    public static InetAddress getLocalAddress() throws SocketException
    {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while( ifaces.hasMoreElements() )
        {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while( addresses.hasMoreElements() )
            {
                InetAddress addr = addresses.nextElement();
                if( addr instanceof Inet4Address && !addr.isLoopbackAddress() )
                {
                    return addr;
                }
            }
        }

        return null;
    }
}
