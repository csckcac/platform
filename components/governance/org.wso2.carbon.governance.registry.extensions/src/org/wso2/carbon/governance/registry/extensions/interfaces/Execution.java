package org.wso2.carbon.governance.registry.extensions.interfaces;

import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

public interface Execution {
    void init(Map parameterMap);
    boolean execute(RequestContext context,String currentState,String targetState);
}
