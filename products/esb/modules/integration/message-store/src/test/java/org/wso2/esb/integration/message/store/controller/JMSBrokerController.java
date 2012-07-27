/**
 * 
 */
package org.wso2.esb.integration.message.store.controller;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.esb.integration.message.store.controller.config.JMSBrokerConfiguration;

import java.io.File;
import java.net.URI;

/**
 * @author wso2
 * 
 */
public class JMSBrokerController {

	private static final Log log = LogFactory.getLog(JMSBrokerController.class);

	private String serverName;
	private JMSBrokerConfiguration configuration;
	private BrokerService broker;

	public JMSBrokerController(String serverName,
			JMSBrokerConfiguration configuration) {
		this.serverName = serverName;
		this.configuration = configuration;
	}

	public String getServerName() {
		return serverName;
	}

	public boolean start() {
		try {

			log.info("JMSServerController: Preparing to start JMS Broker: " + serverName);
			broker = new BrokerService();

			// configure the broker
			TransportConnector connector = new TransportConnector();
			connector.setUri(new URI("tcp://localhost:61616"));
			broker.setBrokerName("testBroker");
            System.out.println(broker.getBrokerDataDirectory());
           broker.setDataDirectory(System.getProperty("carbon.home")+ File.separator+ broker.getBrokerDataDirectory());
           // broker.setDataDirectory(System.getProperty("/home/ishara/Desktop/sample"+ File.separator+ broker.getBrokerDataDirectory()));
			broker.addConnector(connector);
			broker.start();
			log.info("JMSServerController: Broker is Successfully started. continuing tests");
			return true;
		} catch (Exception e) {
			log.error(
					"JMSServerController: There was an error starting JMS broker: "
							+ serverName, e);
			return false;
		}
	}

	public boolean stop() {
		try {
			log.info(" ************* Stopping **************");
			if(broker.isStarted()){
				broker.stop(); 
			}
			return true;
		} catch (Exception e) {
			log.error("Error while shutting down the broker", e);
			return false;
		}
	}

}
