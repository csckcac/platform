/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.dto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

/**
 * @author wso2
 *
 * Singleton class for reading configuration information 
 * from agent-config.xml file
 * 
 */
public class AgentConfig {

	private static final Log log = LogFactory.getLog(AgentConfig.class);
	
	private static final String CONFIG_FILENAME = CarbonUtils.getCarbonConfigDirPath() + File.separator + "agent-config.xml";
	
	private static AgentConfig singletonAgentConfig = null;
	
	private WorkerNodeConfig workerNodeConf;
	
	private ResourcePlanConfig resourcePlanConf;
	
	private String serviceHostUrl;
	
	private String defaultPassword;
	
	private Document rootNode = null;
	
	private AgentConfig() throws Exception {
	    init();
	    readProperties();
    }
	
	private void readProperties() {		
		readWorkerNode();
		readResourcePlan();
		readOtherProperties();
    }	

	private void init() throws Exception {
		try {
	        rootNode = getRootNode(CONFIG_FILENAME);
        } catch (Exception e) {
        	log.error(" Exception is occurred in reading config file. Reason : " + e.getMessage());
	        throw e;
        }	
    }

	public static AgentConfig getAgentConfigInstance() throws Exception {
		
		if(singletonAgentConfig == null ){
			singletonAgentConfig = new AgentConfig();
		}
		return singletonAgentConfig;		
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 * 
	 */
	private static Document getRootNode(String fileName) throws Exception {
		File configfile = new File(fileName);
		if (!configfile.exists()) {
			//StringBuilder msg = new StringBuilder("Configuration File is not present at: ").append(b)
			//log.error( + fileName);
			throw new Exception();
		}
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder =null;
		Document doc = null;
		
		docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.parse (new File(fileName));
		
		return doc;
	}
	
	/**
	 * Reads resource plan related information
	 * 
	 */
	private void readResourcePlan() {    

		resourcePlanConf = new ResourcePlanConfig();

		Map<String, PlanConfig> resourcePlanMap = new HashMap<String, PlanConfig>();

		Element resourcePlanElement = getElementForRootNode("resourcePlan");
		
		NodeList planList = resourcePlanElement.getElementsByTagName("plan");

		for (int i = 0; i < planList.getLength(); i++) {

			PlanConfig planConfig = new PlanConfig();
			Node planNode = planList.item(i);
			String planName = planNode.getAttributes().getNamedItem("name").getNodeValue();

			Element planElement = (Element) planNode;
			planConfig.setName(planName);
			planConfig.setMemory(getValueForElement(planElement, "memory"));
			planConfig.setCpuSets(getValueForElement(planElement, "cpuSets"));
			planConfig.setCpuShares(getValueForElement(planElement, "cpuShares"));
			planConfig.setStorage(getValueForElement(planElement, "storage"));
			planConfig.setSwap(getValueForElement(planElement, "swap"));

			resourcePlanMap.put(planName, planConfig);
		}
		resourcePlanConf.setResourcePlanMap(resourcePlanMap);
	    
    }

	/**
	 * 
	 * Reads worker node related information
	 * 
	 */
	private void readWorkerNode() {

		workerNodeConf = new WorkerNodeConfig();

		Element workerNodeElement = getElementForRootNode("workerNode");

		workerNodeConf.setAvailable(Boolean.parseBoolean(getValueForElement(workerNodeElement,
		                                                                    "isAvailable")));
		workerNodeConf.setName(getValueForElement(workerNodeElement, "name"));
		workerNodeConf.setContainerRoot(getValueForElement(workerNodeElement, "templatePath"));
		workerNodeConf.setZone(getValueForElement(workerNodeElement, "zone"));
		workerNodeConf.setIp(getValueForElement(workerNodeElement, "ip"));
		//getValueForElement(workerNodeElement,"containerRoot");
		workerNodeConf.setTemplateMap(getTemplateMap(workerNodeElement));
		workerNodeConf.setBridges(getBridges(workerNodeElement));
		    
    }

	
	/**
	 * 
	 * Reads other common information
	 * 
	 */
	private void readOtherProperties() {

		serviceHostUrl = getElementForRootNode("agentManagementHost").getAttribute("url");
		defaultPassword = getElementForRootNode("defaultPassword").getAttribute("value");
		System.out.println(defaultPassword);
		
    }
	
	/**
	 * Returns a map of templates, with domain as the key and name as the value
	 * @param workerNodeElement
	 * @return
	 */
	private Map<String, String> getTemplateMap(Element workerNodeElement) {	
		
		Map<String, String> templateMap = new HashMap<String, String>();
		
		NodeList templateList =	workerNodeElement.getElementsByTagName("template");
		for (int i = 0; i < templateList.getLength(); i++) {
	       Node templateNode = templateList.item(i);	       
	       String templateName = templateNode.getAttributes().getNamedItem("name").getNodeValue();
	       String domain = templateNode.getAttributes().getNamedItem("domain").getNodeValue();	     
	       templateMap.put(domain, templateName);	       
        }		
	    return templateMap;
    }

	/**
	 * Reads and returns bridge information
	 * 
     * @param workerNodeElement
     * @return
     */
    private List<BridgeConfig> getBridges(Element workerNodeElement) {
    	
	    NodeList bridgesList = workerNodeElement.getElementsByTagName("bridges");
	    Node bridgesNode = bridgesList.item(0);
	    Element bridgeElement = (Element) bridgesNode;
	    NodeList bridgeList = bridgeElement.getElementsByTagName("bridge");
	    			
	    List<BridgeConfig> bridgeArrayList = new ArrayList<BridgeConfig>();
	    
	    for (int i = 0; i < bridgeList.getLength(); i++) {
	    	
	    	BridgeConfig bridge = new BridgeConfig();
	    	
	    	Node bridgeNode = bridgeList.item(i);				
	        
	    	bridge.setName(getValueForElement((Element) bridgeNode, "bridgeIp"));
	    	bridge.setAvailable(Boolean.parseBoolean(getValueForElement((Element) bridgeNode, "isAvailable")));
	    	bridge.setCurrentCountIps(Integer.parseInt(getValueForElement((Element) bridgeNode, "currentIpCount")));
	    	bridge.setMaximumCountIps(Integer.parseInt(getValueForElement((Element) bridgeNode, "maxIpCount")));
	    	bridge.setIp(getValueForElement((Element) bridgeNode, "bridgeIp"));
	    	//bridge.setWorkerNode(workerNode);
	    	bridgeArrayList.add(bridge);				
	    }
	    return bridgeArrayList;
    }




	/**
     * @param workerNodeElement
     */
    private static String getValueForElement(Element workerNodeElement, String name) {
    	
	    NodeList availableList = workerNodeElement.getElementsByTagName(name);
	    Element availableElement = (Element) availableList.item(0);
	    NodeList txtAvailableList = availableElement.getChildNodes();
	    return txtAvailableList.item(0).getNodeValue();
    }


	/**
     * @return
     */
    private Element getElementForRootNode(String tagName) {
	    NodeList resourcePlanList = rootNode.getElementsByTagName(tagName);
		Node resourcePlan = resourcePlanList.item(0);
		Element resourcePlanElement = (Element) resourcePlan;
	    return resourcePlanElement;
    }
    
	public WorkerNodeConfig getWorkerNodeConf() {
    	return workerNodeConf;
    }

	public ResourcePlanConfig getResourcePlanConf() {
    	return resourcePlanConf;
    }

	public String getServiceHostUrl() {
    	return serviceHostUrl;
    }

	public String getDefaultPassword() {
    	return defaultPassword;
    }
	
}
