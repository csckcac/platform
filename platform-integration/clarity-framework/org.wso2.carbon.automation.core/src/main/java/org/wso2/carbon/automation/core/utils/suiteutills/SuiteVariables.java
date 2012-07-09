package org.wso2.carbon.automation.core.utils.suiteutills;

public class SuiteVariables {
    private String testName;
    private String[] includeGroups;
    private String[] excludeGrops;
    private Class testClass;

    public SuiteVariables(String test1, Class xmlClass) {
        this.testName = test1;
        this.includeGroups = new String[]{"excludedGroup}"};
        this.excludeGrops = new String[]{"excludedGroup}"};
        this.testClass = xmlClass;
    }


    public String geTestName() {
        return testName;
    }

    public String[] getIncludeGroups() {
        return includeGroups;
    }

    public String[] getExcludeGrops() {
        return excludeGrops;
    }

    public Class getTestClass() {
        return testClass;
    }


    public void setSuiteVariales(String testName, String includeGroups, String excludeGroups,
                                 Class testClass) {
        this.testName = testName;
        this.includeGroups = includeGroups.split(",");
        this.excludeGrops = excludeGroups.split(",");
        this.testClass = testClass;
    }

    public void SuiteVariables(String testName, Class testClass) {
        this.testName = testName;
        this.includeGroups = new String[]{"excludedGroup}"};
        this.excludeGrops = new String[]{"excludedGroup}"};
        this.testClass = testClass;
    }

}
