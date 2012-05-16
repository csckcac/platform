/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.hosting.wnagent.util.AgentServiceConstants;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.HostMachine;
import org.wso2.carbon.utils.CarbonUtils;

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
	//private static final String CONFIG_FILENAME = "/home/wso2/work/projects/source/platform/trunk/components/hosting-mgt/org.wso2.carbon.hosting.wnagent/conf/agent-config.xml";
	private static AgentConfig singletonAgentConfig = null;
		
	private HostMachine hostMachine;
	
	private ResourcePlanConfig resourcePlanConf;
	
	private String serviceHostUrl;
	
	private String defaultPassword;
	
	private String defaultContainerUser;
	
	private Map<String, String> domainToTemplateMap;
	
	private List<String> domainsList;
	
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

		Element resourcePlanElement = getElementForRootNode(AgentServiceConstants.RESOURCE_PLAN_ELEMENT_NAME);
		
		NodeList planList = resourcePlanElement.getElementsByTagName(AgentServiceConstants.PLAN_ELEMENT_NAME);

		for (int i = 0; i < planList.getLength(); i++) {

			PlanConfig planConfig = new PlanConfig();
			Node planNode = planList.item(i);
			String planName = planNode.getAttributes().getNamedItem(AgentServiceConstants.NAME_ELEMENT).getNodeValue();

			Element planElement = (Element) planNode;
			planConfig.setName(planName);
			planConfig.setMemory(getValueForElement(planElement, AgentServiceConstants.MEMORY_ELEMENT_NAME));
			planConfig.setCpuSets(getValueForElement(planElement, AgentServiceConstants.CPU_SETS_ELEMENT_NAME));
			planConfig.setCpuShares(getValueForElement(planElement, AgentServiceConstants.CPU_SHARES_ELEMENT_NAME));
			planConfig.setStorage(getValueForElement(planElement, AgentServiceConstants.STORAGE_ELEMENT_NAME));
			planConfig.setSwap(getValueForElement(planElement, AgentServiceConstants.SWAP_ELEMENT_NAME));

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

		hostMachine = new HostMachine();

		Element workerNodeElement = getElementForRootNode(AgentServiceConstants.HOST_MACHINE_ELEMENT_NAME);

		hostMachine.setAvailable(Boolean.
		                         parseBoolean(getValueForElement(workerNodeElement,AgentServiceConstants.IS_AVAILABLE_ELEMENT_NAME)));
		hostMachine.setContainerRoot(getValueForElement(workerNodeElement, AgentServiceConstants.TEMPLATE_PATH));
		hostMachine.setZone(getValueForElement(workerNodeElement, AgentServiceConstants.ZONE_ELEMENT_NAME));
		hostMachine.setIp(getValueForElement(workerNodeElement, AgentServiceConstants.IP_ELEMENT_NAME));	
		hostMachine.setBridges(getBridges(workerNodeElement));
		
		
		setDomainsList(workerNodeElement);		
		setTemplateMap(workerNodeElement);		    
    }

	
	private void setDomainsList(Element workerNodeElement) {
	   
		domainsList = new ArrayList<String>();
		NodeList domainsNodeList = workerNodeElement.getElementsByTagName(AgentServiceConstants.DOMAINS_ELEMENT_NAME);
	    Node domainsNode = domainsNodeList.item(0);
	    Element domainElement = (Element) domainsNode;
	    NodeList domainList = domainElement.getElementsByTagName(AgentServiceConstants.DOMAIN_ELEMENT_NAME);
	    
	    for (int i = 0; i < domainList.getLength(); i++) {
	        
	    	Node domainNode = domainList.item(i);
			String domainName = domainNode.getAttributes().getNamedItem(AgentServiceConstants.NAME_ELEMENT).getNodeValue();
			domainsList.add(domainName);			
        }
    }

	/**
	 * 
	 * Reads other common information
	 * 
	 */
	private void readOtherProperties() {

		serviceHostUrl = getElementForRootNode("agentManagementHost").getAttribute("url");
		defaultPassword = getElementForRootNode("defaultPassword").getAttribute("value");
		defaultContainerUser = getElementForRootNode("defaultUsername").getAttribute("value"); 
		//agentServiceEpr = getElementForRootNode("agentServiceEpr").getAttribute("url");		
    }
	
	/**
	 * Returns a map of templates, with domain as the key and name as the value
	 * @param workerNodeElement
	 * @return
	 */
	private void setTemplateMap(Element workerNodeElement) {	
		
		domainToTemplateMap = new HashMap<String, String>();
		
		NodeList templateList =	workerNodeElement.getElementsByTagName("template");
		for (int i = 0; i < templateList.getLength(); i++) {
	       Node templateNode = templateList.item(i);	       
	       String templateName = templateNode.getAttributes().getNamedItem("name").getNodeValue();
	       String domain = templateNode.getAttributes().getNamedItem("domain").getNodeValue();	     
	       domainToTemplateMap.put(domain, templateName);	       
        }		
	    
    }

	/**
	 * Reads and returns bridge information
	 * 
     * @param workerNodeElement
     * @return
     */
    private Bridge[] getBridges(Element workerNodeElement) {
    	
	    NodeList bridgesList = workerNodeElement.getElementsByTagName("bridges");
	    Node bridgesNode = bridgesList.item(0);
	    Element bridgeElement = (Element) bridgesNode;
	    NodeList bridgeList = bridgeElement.getElementsByTagName("bridge");
	    			
	    List<Bridge> bridgeArrayList = new ArrayList<Bridge>();
	    
	    for (int i = 0; i < bridgeList.getLength(); i++) {
	    	
	    	Bridge bridge = new Bridge();
	    	
	    	Node bridgeNode = bridgeList.item(i);				
	        
	    	bridge.setAvailable(Boolean.parseBoolean(getValueForElement((Element) bridgeNode, "isAvailable")));
	    	bridge.setCurrentCountIps(Integer.parseInt(getValueForElement((Element) bridgeNode, "currentIpCount")));
	    	bridge.setMaximumCountIps(Integer.parseInt(getValueForElement((Element) bridgeNode, "maxIpCount")));
	    	bridge.setBridgeIp(getValueForElement((Element) bridgeNode, "bridgeIp"));
	    	//bridge.setWorkerNode(workerNode); <!--  -->
	    	bridge.setNetGateway(getValueForElement((Element) bridgeNode, "gateway"));
	    	bridge.setNetMask(getValueForElement((Element) bridgeNode, "netmask"));
	    	bridgeArrayList.add(bridge);				
	    }
	    return bridgeArrayList.toArray(new Bridge[bridgeArrayList.size()]);
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
	
	public HostMachine getHostMachine() {
		return hostMachine;
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
	
	public Map<String, String> getDomainToTemplateMap() {
		return domainToTemplateMap;
	}
	
	public List<String> getDomainsList() {
		return domainsList;
	}

	public String getDefaultContainerUser() {
	   return defaultContainerUser;
    }
	
}
