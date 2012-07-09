package org.wso2.carbon.automation.core.utils.frameworkutils;

import org.wso2.carbon.automation.core.utils.dashboardutils.DashboardVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.CoverageSettings;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.DataSource;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.EnvironmentSettings;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.Ravana;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.Selenium;

public class FrameworkSettings {


    private DataSource dataSource;
    private EnvironmentSettings environmentSettings;
    private EnvironmentVariables environmentVariables;
    private Selenium selenium;
    private Ravana ravana;
    private DashboardVariables dashboardVariables;
    private CoverageSettings coverageSettings;

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

    public CoverageSettings getCoverageSettings() {
        return coverageSettings;
    }

    public void setFrameworkSettings(DataSource dataSource, EnvironmentSettings environmentSettings,
                                     EnvironmentVariables environmentVariables, Selenium selenium,
                                     Ravana ravana, DashboardVariables dshVariables,
                                     CoverageSettings coverage) {
        this.dataSource = dataSource;
        this.environmentSettings = environmentSettings;
        this.environmentVariables = environmentVariables;
        this.selenium = selenium;
        this.ravana = ravana;
        this.dashboardVariables = dshVariables;
        this.coverageSettings = coverage;
    }
}
