package org.wso2.carbon.statistics.synchronize;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;

public class StatisticsSynchronizeRequest extends ClusteringMessage {
    
    private int tenantId;
    public StatisticsSynchronizeRequest(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Get the response for this message
     *
     * @return the response for this message
     */
    @Override
    public ClusteringCommand getResponse() {
//        System.out.println("XXX StatisticsSynchronizeRequest.getResponse " + tenantId);
        return new StatisticsSynchronizeResponse();
    }

    @Override
    public void execute(ConfigurationContext configContext) throws ClusteringFault {
//        System.out.println("XXX StatisticsSynchronizeRequest.execute" + tenantId);
        //To change body of implemented methods use File | Settings | File Templates.
    }
}