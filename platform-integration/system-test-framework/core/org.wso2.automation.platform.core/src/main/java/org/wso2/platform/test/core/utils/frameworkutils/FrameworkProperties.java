package org.wso2.platform.test.core.utils.frameworkutils;

import org.wso2.platform.test.core.utils.dashboardutils.DashboardVariables;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.*;

public class FrameworkProperties {

    private DataSource dataSource;
    private EnvironmentSettings environmentSettings;
    private EnvironmentVariables environmentVariables;
    private ProductVariables productVariables;
    private Selenium selenium;
    private Ravana ravana;
    private DashboardVariables dashboardVariables;
    private CoverageSettings coverageSettings;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    public void setEnvironmentSettings(EnvironmentSettings environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public ProductVariables getProductVariables() {
        return productVariables;
    }

    public void setProductVariables(ProductVariables productVariables) {
        this.productVariables = productVariables;
    }


    public Ravana getRavana() {
        return ravana;
    }

    public void setRavana(Ravana ravana) {
        this.ravana = ravana;
    }


    public Selenium getSelenium() {
        return selenium;
    }

    public void setSelenium(Selenium selenium) {
        this.selenium = selenium;
    }

    public DashboardVariables setDashboardVariables(DashboardVariables dashboardVariable) {
        return dashboardVariable;
    }

    public void setCoverageSettings(CoverageSettings coverage)
    {
        this.coverageSettings=coverage;
    }
    public void setFrameworkProperties(DataSource dataSource,
                                       EnvironmentSettings environmentSettings,
                                       EnvironmentVariables environmentVariables,
                                       ProductVariables productVariables, Selenium selenium,
                                       Ravana ravana, DashboardVariables dbVariables,CoverageSettings coverage) {
        this.dataSource = dataSource;
        this.environmentSettings = environmentSettings;
        this.environmentVariables = environmentVariables;
        this.productVariables = productVariables;
        this.selenium = selenium;
        this.ravana = ravana;
        this.dashboardVariables = dbVariables;
        this.coverageSettings =coverage;
    }

}
