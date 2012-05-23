package org.wso2.carbon.governance.registry.extensions.executors;

import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

public class DemoteActionExecutor implements Execution{
    public void init(Map parameterMap) {
//        do nothing
    }

    public boolean execute(RequestContext context, String currentState, String targetState) {
        String mediaType = context.getResource().getMediaType();
        if (mediaType != null && mediaType.equals("application/vnd.wso2-service+xml")) {
            context.setResource(null);
        }
        return true;
    }
}
