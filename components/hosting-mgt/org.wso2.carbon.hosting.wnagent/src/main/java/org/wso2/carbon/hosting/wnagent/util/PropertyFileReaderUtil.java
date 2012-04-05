/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.wnregistration.stub.services.xsd.dto.Bridge;
import org.wso2.carbon.hosting.wnregistration.stub.services.xsd.dto.WorkerNode;
import org.wso2.carbon.hosting.wnregistration.stub.services.xsd.dto.ZoneResourcePlan;


/**
 * 
 * @author wso2
 * 
 */
public class PropertyFileReaderUtil {

	private static final String WORKER_NODE_FILENAME =
	                                                   "/home/wso2/work/projects/source/platform/trunk/components/hosting-mgt/org.wso2.carbon.hosting.wnagent/src/main/java/org/wso2/carbon/hosting/wnagent/util/worker-node.xml";

	private static final String ZONE_RESOURCE_FILE_NAME = "";

	private static final Log log = LogFactory.getLog(PropertyFileReaderUtil.class);

	/**
	 * This util method returns Reads the worker node config file and returns a
	 * WorkerNode object
	 * 
	 * @return {@link WorkerNode} object
	 */
	public static WorkerNode readWorkerNodeFromFile() {

		WorkerNode workerNode = new WorkerNode();

		// Read property file and create the workerNode object

		try {

			OMElement documentElement = getRootNode(WORKER_NODE_FILENAME);

			Iterator it = documentElement.getChildElements();
			while (it.hasNext()) {

				OMElement element = (OMElement) it.next();
				if ("IsAvailable".equals(element.getLocalName())) {
					workerNode.setAvailable(Boolean.parseBoolean(element.getText()));
				} else if ("Name".equals(element.getLocalName())) {
					workerNode.setName(element.getText());
				} else if ("ContainerRoot".equals(element.getLocalName())) {
					workerNode.setContainerRoot(element.getText());
				} else if ("EndpointReference".equals(element.getLocalName())) {
					workerNode.setEndPoint(element.getText());
				} else if ("Zone".equals(element.getLocalName())) {
					workerNode.setZone(element.getText());
				} else if ("Bridges".equals(element.getLocalName())) {

					List<Bridge> bridgeList = new ArrayList<Bridge>();

					Iterator bridgesItr = element.getChildElements();

					while (bridgesItr.hasNext()) {

						OMElement bridgesElement = (OMElement) bridgesItr.next();

						// get Bridge information
						Iterator bridgeItr = bridgesElement.getChildElements();

						Bridge bridge = new Bridge();

						while (bridgeItr.hasNext()) {

							readBridgeElement(bridgeItr, bridge);
						}
						bridgeList.add(bridge);
					}
					workerNode.setBridges(bridgeList.toArray(new Bridge[bridgeList.size()]));

				}
			}
			return workerNode;

		} catch (Exception e) {
			String msg =
			             "Error in loading configuration for configuring the admin user: "
			                     + "_file_name_" + ".";
			System.out.println(msg + " :: " + e.getMessage());
			// log.error(msg, e);
			return null;
		}

	}

	/**
	 * @return
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws FileNotFoundException
	 */
	private static OMElement getRootNode(String fileName) throws XMLStreamException,
	                                                     FactoryConfigurationError,
	                                                     FileNotFoundException {
		File configfile = new File(fileName);
		if (!configfile.exists()) {
			log.error("Configuration File is not present at: " + fileName);
			return null;
		}

		XMLStreamReader parser =
		                         XMLInputFactory.newInstance()
		                                        .createXMLStreamReader(new FileInputStream(
		                                                                                   configfile));
		StAXOMBuilder builder = new StAXOMBuilder(parser);
		OMElement documentElement = builder.getDocumentElement();
		return documentElement;
	}

	public static ZoneResourcePlan readZoneResourcePlanFromFile() {

		ZoneResourcePlan zonePlan = new ZoneResourcePlan();

		OMElement documentElement;
		try {
			documentElement = getRootNode(ZONE_RESOURCE_FILE_NAME);
			Iterator it = documentElement.getChildElements();
			while (it.hasNext()) {

				OMElement element = (OMElement) it.next();
				if ("IsAvailable".equals(element.getLocalName())) {
					zonePlan.setAvailable(Boolean.parseBoolean(element.getText()));
				} else if ("Name".equals(element.getLocalName())) {
					zonePlan.setZone(element.getText());
				} else if ("CpuSets".equals(element.getLocalName())) {
					zonePlan.setCpuSetCpus(element.getText());
				} else if ("CpuShares".equals(element.getLocalName())) {
					zonePlan.setCpuShares(element.getText());
				} else if ("Memory".equals(element.getLocalName())) {
					zonePlan.setMemory(element.getText());
				} else if ("NetGateway".equals(element.getLocalName())) {
					zonePlan.setNetGateway(element.getText());
				} else if ("NetMask".equals(element.getLocalName())) {
					zonePlan.setNetMask(element.getText());
				} else if ("Storage".equals(element.getLocalName())) {
					zonePlan.setStorage(element.getText());
				} else if ("Swap".equals(element.getLocalName())) {
					zonePlan.setSwap(element.getText());
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}

		return zonePlan;

	}

	/**
	 * 
	 * @param bridgeItr
	 * @param bridge
	 * 
	 */
	private static void readBridgeElement(Iterator bridgeItr, Bridge bridge) {
		OMElement bridgeElement = (OMElement) bridgeItr.next();

		if ("IsAvailable".equals(bridgeElement.getLocalName())) {
			bridge.setAvailable(Boolean.parseBoolean(bridgeElement.getText()));
		} else if ("Name".equals(bridgeElement.getLocalName())) {
			bridge.setName(bridgeElement.getText());
		} else if ("WorkerNode".equals(bridgeElement.getLocalName())) {
			bridge.setWorkerNode(bridgeElement.getText());
		} else if ("MaxIpCount".equals(bridgeElement.getLocalName())) {
			bridge.setMaximumCountIps(Integer.parseInt(bridgeElement.getText()));
		} else if ("CurrentIpCount".equals(bridgeElement.getLocalName())) {
			bridge.setCurrentCountIps(Integer.parseInt(bridgeElement.getText()));
		}
	}
}
