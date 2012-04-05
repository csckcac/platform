package org.wso2.platform.test.core.utils.frameworkutils;

import org.wso2.platform.test.core.utils.frameworkutils.productvariables.*;

public interface Framework {
    public DataSource getDataSource();

    public EnvironmentSettings getEnvironmentSettings();

    public EnvironmentVariables getEnvironmentVariables();

   // public EnvironmentVariables getProductVariables();

    public Ravana getRavana();

    public Selenium getSelenium();

    public FrameworkProperties getFrameworkProperties();
}
