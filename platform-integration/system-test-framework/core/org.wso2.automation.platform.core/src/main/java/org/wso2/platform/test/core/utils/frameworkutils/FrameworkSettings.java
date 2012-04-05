package org.wso2.platform.test.core.utils.frameworkutils;

import org.wso2.platform.test.core.utils.dashboardutils.DashboardVariables;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.*;

public class FrameworkSettings {


    private DataSource dataSource;
    private EnvironmentSettings environmentSettings;
    private EnvironmentVariables environmentVariables;
    private Selenium selenium;
    private Ravana ravana;
    private DashboardVariables dashboardVariables;

    public DataSource getDataSource() {
        return dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public Ravana getRavana() {
        return ravana;
    }

    public Selenium getSelenium() {
        return selenium;
    }

    public DashboardVariables getDashboardVariables() {
        return dashboardVariables;
    }

    public void setFrameworkSettings(DataSource dataSource, EnvironmentSettings environmentSettings,
                                     EnvironmentVariables environmentVariables, Selenium selenium,
                                     Ravana ravana,DashboardVariables dshVariables ) {
        this.dataSource = dataSource;
        this.environmentSettings = environmentSettings;
        this.environmentVariables = environmentVariables;
        this.selenium = selenium;
        this.ravana = ravana;
        this.dashboardVariables = dshVariables;
    }
}
