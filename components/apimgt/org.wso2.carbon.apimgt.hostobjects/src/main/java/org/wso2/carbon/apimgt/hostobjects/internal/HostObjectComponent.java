package org.wso2.carbon.apimgt.hostobjects.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.hostobjects.utils.APIHostObjectUtil;

/**
 * @scr.component name="org.wso2.apimgt.hostobjects" immediate="true"
 */
public class HostObjectComponent {

    static Log log = LogFactory.getLog(HostObjectComponent.class);
    private APIHostObjectUtil hostObjectUtil;

    protected void activate(ComponentContext componentContext) {
       if(log.isDebugEnabled()){
           log.debug("HostObjectComponent activated");
       }
        hostObjectUtil = APIHostObjectUtil.getApiHostObjectUtils();
    }

    protected void dedactivate(ComponentContext componentContext) {
       if(log.isDebugEnabled()){
           log.debug("HostObjectComponent deactivated");
       }
       hostObjectUtil.cleanup();
    }
}
