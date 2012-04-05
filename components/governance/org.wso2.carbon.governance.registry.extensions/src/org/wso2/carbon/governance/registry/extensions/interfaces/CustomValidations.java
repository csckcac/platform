package org.wso2.carbon.governance.registry.extensions.interfaces;

import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

public interface CustomValidations {
    void init(Map parameterMap);
    boolean validate(RequestContext context);
}
