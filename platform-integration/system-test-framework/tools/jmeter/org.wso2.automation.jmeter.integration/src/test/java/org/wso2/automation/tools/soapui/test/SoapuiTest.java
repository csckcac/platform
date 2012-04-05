package org.wso2.automation.tools.soapui.test;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.testng.annotations.Test;
import tools.soapui.core.SoapUICore;
import tools.soapui.core.SoapUIProperties;

public class SoapuiTest {

    @Test
    public void testSoapUIScripts() throws MojoExecutionException, MojoFailureException {
        SoapUICore mojo = new SoapUICore();
        SoapUIProperties properties = new SoapUIProperties();
        properties.setProject("/home/dharshana/soapui/AS-Simple-test-soapui-project.xml", "/home/dharshana/soapui/");
        mojo.runner(properties);
    }
}
