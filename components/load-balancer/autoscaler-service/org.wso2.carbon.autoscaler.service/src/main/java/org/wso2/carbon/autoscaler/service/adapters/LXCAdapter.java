/**
 * 
 */
package org.wso2.carbon.autoscaler.service.adapters;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.agent.clients.AgentServiceClient;
import org.wso2.carbon.autoscaler.service.internal.AgentManagementDSHolder;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService;
import org.wso2.carbon.autoscaler.service.recovery.JVMAgentServiceRecoverer;
import org.wso2.carbon.autoscaler.service.registry.JVMAdapterRegistry;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.ContainerInformation;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

/**
 * Container specific Adapter, handles spawning / terminating instances in LXContainers
 * 
 *
 */
public class LXCAdapter extends Adapter {

	/**
	 * Name of the Adapter.
	 */
	private static final String ADAPTER_NAME = "container";
	
	private JVMAdapterRegistry registry = JVMAdapterRegistry.getInstance();
		
	private IAgentManagementService agentManagementService = AgentManagementDSHolder.getInstance().getAgentMgtService();
	
	private static final Log log = LogFactory.getLog(LXCAdapter.class);
	
	private AgentPersistenceManager agentPersistenceManager  = AgentPersistenceManager.getPersistenceManager();
	
	public LXCAdapter(){
		//registry.setDomainNameToInstanceCountMap(domainNameToInstanceCountMap);
		registry.setInstanceIdToAgentEprMap(getInstanceidEprMap());
		registry.setEprToContainerRootMap(getEprtoContainerRootMap());
		//registry.setTemporarilySkippedAgentEprList(temporarilySkippedAgentEprList);
	}

	@Override
	public boolean spawnInstance(String domainName, String instanceId) {

		log.info("Trying to spawn instance in LXC Adapter");
		
		if(agentManagementService != null) {
			
			log.info("agentManagementService is not null");
			// Check whether we can spawn any 
			// instance in this adapter
			// TODO need a database query or?
			try {
				if (agentPersistenceManager.isHostMachinesAvailableInDomain(domainName)) {
	
					ContainerInformation container = agentManagementService.pickAContainer(domainName);
					container.setContainerId(instanceId);
					
					int numOfPossibleAgents = 1; // TODO get this parameter from the database?
					return tryToStartInstance(domainName, instanceId, numOfPossibleAgents , container);
					
				} else {
					log.info(" No workernode available in given domain");
					log.error(" No further instances can be spwn");
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return false;
	}

	private boolean tryToStartInstance(String domainName,
			String instanceId, int i, ContainerInformation containerInfo) throws Exception{

		AgentServiceClient agentServiceClient = null;
		
		// Try to connect to an Agent
		try {
			agentServiceClient = new AgentServiceClient(containerInfo.getEpr()); 
			boolean canCreateMoreLXCs = agentServiceClient.startContainerInstance(domainName, containerInfo);
						
			if(!canCreateMoreLXCs){
				// This assumes that in that particular host machine
				// no further containers can be created.
				// Hence making that as an unavailable host machine.
				agentPersistenceManager.changeHostMachineAvailability(containerInfo.getEpr(), false);
			}
			
			// Now the LXC is started
			// adding container details to maps and the database
			addToInstanceIdToAgentEprMap(instanceId, containerInfo.getEpr());
			addToEprToContainerRootMap(containerInfo.getEpr(), containerInfo.getContainerRoot());
			agentPersistenceManager.addContainer(populateContainer(containerInfo));
			return true;
			
		} catch (RemoteException e) {
			
			// couldn't connect to AgentService, hence try to recover
            JVMAgentServiceRecoverer recoverer = new JVMAgentServiceRecoverer(containerInfo.getEpr());
            recoverer.start();

            // we're not waiting till recovery is finished. We look for another agent.
            ContainerInformation anotherContainer = agentManagementService.pickAContainer(domainName); // TODO invoke this with a flag
            tryToStartInstanceInAnotherAgent(domainName, instanceId, 1, anotherContainer);  // TODO instanceCount ??
            
		} catch (Exception e) {
			
			log.error("Failed to spawn the instance in Agent (" + containerInfo.getEpr() + "). Hence trying on" + " another Agent.", e);

                // We look for another agent.
			ContainerInformation anotherContainer = agentManagementService.pickAContainer(domainName); // TODO invoke this with a flag
            tryToStartInstanceInAnotherAgent(domainName, instanceId, 1, anotherContainer);  // TODO instanceCount ??

		}
		
		return false;
	}

	
	
	private void addToEprToContainerRootMap(String epr, String containerRoot) {
		
		// load map from registry
        Map<String, String> eprToContainerRootMap = registry.getEprToContainerRootMap();

        if (!eprToContainerRootMap.containsKey(epr)) {
            // add to the map
        	eprToContainerRootMap.put(epr, containerRoot);

            // add it back to registry
            registry.setEprToContainerRootMap(eprToContainerRootMap);
        } 
	}

	private void tryToStartInstanceInAnotherAgent(String domainName,
			String instanceId, int numberOfAgents, ContainerInformation pickAContainer) throws Exception {

		// Failed when starting the instance, thus try in another Agent.
        // tracking the position of the round
        numberOfAgents--;

        // We need to throw the exception caught, thus doing this check.
        if (numberOfAgents > 0) {

            // try to pass the job to another Agent
            this.tryToStartInstance(domainName, instanceId, numberOfAgents, pickAContainer);

        } else {
            String msg = "Tried to spawn the instance in all registered agents, but failed.";
            log.error(msg);
            throw new Exception(msg);
        }

	}

	/**
	 * Populates a Container object from a ContainerInformation object
	 * 
	 * @param containerInfo
	 * @return
	 */
	private Container populateContainer(ContainerInformation containerInfo) {
		Container container = new Container();
		container.setBridge(containerInfo.getBridge());
		container.setContainerId(containerInfo.getContainerId());
		container.setContainerKeysFile(containerInfo.getContainerKeysFile());
		container.setDescription("test-description");
		container.setIp(containerInfo.getIp());
		container.setLabel("test-label");
		container.setStarted(true);
		//container.setTemplate(containerInfo.)
		container.setTenant("default");
		container.setType(containerInfo.getType());		
		return container;
	}

	private Map<String, String> getInstanceidEprMap() {
		Map<String, String> instanceIdEprMap = null;
		try {
			instanceIdEprMap = agentPersistenceManager.retrieveContainerIdToAgentMap();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return instanceIdEprMap;
		//return new HashMap<String, String>();
	}
	

	private Map<String, String> getEprtoContainerRootMap() {
		Map<String, String> eprContainerRootMap = null;
		try {
			eprContainerRootMap = agentPersistenceManager.retrieveAgentToContainerRootMap();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return eprContainerRootMap;
		//return new HashMap<String, String>();
	}

	@Override
	public boolean terminateInstance(String instanceId) {	
		
		// load from registry
        Map<String, String> instanceIdToAgentEprMap = registry.getInstanceIdToAgentEprMap();
        List<String> temporarilySkippedAgentEprList = registry.getTemporarilySkippedAgentEprList();
        Map<String, String> eprToContainerRootMap = registry.getEprToContainerRootMap();

        //get the Agent EPR corresponds to this instanceId
        String agentEpr = instanceIdToAgentEprMap.get(instanceId); 
        String containerRoot = eprToContainerRootMap.get(agentEpr);
        
        //agentPersistenceManager.
        
        if (agentEpr == null) {
            String msg =
                "Instance (" + instanceId + ") requested to terminate cannot be found in " +
                    "the Adapter (" + getName() + ").";
            log.error(msg);
            return false;
        }
        
      	//check whether the agentEpr is added to skipped agents list, if so return false.
        if (temporarilySkippedAgentEprList.contains(agentEpr)) {
            
            String msg = "Agent (" + agentEpr + ") is currently unreachable.";
            log.error(msg);
            return false;
        }
        
        
        AgentServiceClient agentClient = null;

        // now all set, we try to terminate the instance 
        try {
            agentClient = new AgentServiceClient(agentEpr);

            if (agentClient.terminateContainerInstance(instanceId, containerRoot)) {
                // remove relevant entry
                instanceIdToAgentEprMap.remove(instanceId);
                
                //add it back to registry
                registry.setInstanceIdToAgentEprMap(instanceIdToAgentEprMap);
                registry.setEprToContainerRootMap(eprToContainerRootMap);
                
                // Delete container from database
                agentPersistenceManager.deleteContainer(instanceId);
                
                return true;
            }
            
        } catch (RemoteException e) {
        	
            // adds to temporarily skipped agent epr list, since this agent is unreachable
            // and shouldn't be wasted by calling it.
            temporarilySkippedAgentEprList.add(agentEpr);

            // add it back to registry
            registry.setTemporarilySkippedAgentEprList(temporarilySkippedAgentEprList);
            
            // TODO update database accordingly

            // couldn't connect to AgentService, hence try to recover
            JVMAgentServiceRecoverer recoverer = new JVMAgentServiceRecoverer(agentEpr);
            recoverer.start();
            String msg = "Failed to connect to the Agent (" + agentEpr + ").";
            log.error(msg, e);
            // no need to throw, since it's no use
            return false;
            
        } catch (Exception e) {

            String msg = "Failed to terminate the instance in Agent (" + agentEpr + ").";
            log.error(msg, e);
            // no need to throw, since it's no use

            return false;
        }
		return false;
	}

	
	@Override
	public String getName() {
		return ADAPTER_NAME;
	}
	
	/**
	 * Add this pair to the {@link #instanceIdToAgentEprMap}.
	 * @param instanceId  
	 * @param agentEpr
	 */
    private void addToInstanceIdToAgentEprMap(String instanceId, String agentEpr) {

        // load map from registry
        Map<String, String> instanceIdToAgentEprMap = registry.getInstanceIdToAgentEprMap();

        if (!instanceIdToAgentEprMap.containsKey(instanceId)) {
            // add to the map
            instanceIdToAgentEprMap.put(instanceId, agentEpr);

            // add it back to registry
            registry.setInstanceIdToAgentEprMap(instanceIdToAgentEprMap);

        } else {
            String msg =
                "This instance id (" + instanceId + ") is already in the Map. " +
                    "This is something exceptional.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    
	@Override
	public int getRunningInstanceCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRunningInstanceCount(String domainName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPendingInstanceCount(String domainName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean sanityCheck() {
		// TODO Auto-generated method stub
		return false;
	}

}
