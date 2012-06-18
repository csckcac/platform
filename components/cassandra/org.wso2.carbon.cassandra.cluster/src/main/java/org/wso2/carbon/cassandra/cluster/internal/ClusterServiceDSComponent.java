package org.wso2.carbon.cassandra.cluster.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;


public class ClusterServiceDSComponent {

    private static Log log = LogFactory.getLog(ClusterServiceDSComponent.class);

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Starting the cluster service component for Cassandra");
        }
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the cluster service component for Cassandra");
        }
    }

}