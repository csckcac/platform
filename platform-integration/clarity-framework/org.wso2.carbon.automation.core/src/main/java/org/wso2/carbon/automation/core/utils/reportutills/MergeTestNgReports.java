/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.utils.reportutills;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.wso2.carbon.automation.core.ProductConstant;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class MergeTestNgReports extends AbstractMojo {


    public void execute() throws MojoExecutionException, MojoFailureException {
        File xslFile = new File("/home/dharshana/automation/testNgIntegration1/system-test-framework/core/org.wso2.automation.platform.core/src/main/java/org/wso2/platform/test/core/utils/reportutills/mergeResult.xsl");
        File srcDir = new File(ProductConstant.REPORT_REPOSITORY);

        File destDir = new File(ProductConstant.REPORT_REPOSITORY);
        try {
            // do some input validation
            if (!xslFile.exists()) {
                getLog().error("XSL file does not exist: " + xslFile);
                return;
            }
            if (!srcDir.exists()) {
                getLog().error("Source directory does not exist: " + srcDir);
                return;
            }

            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslFile));

            String[] xmlFiles = getIncludedFiles(srcDir, "**/testng-results.xml");
            getLog().info("# of XML files: " + xmlFiles.length);
            for (int i = 0; i < xmlFiles.length; i++) {
                if (xmlFiles[i].equals("testng-results.xml")) {
                    break;
                }
                File srcFile = new File(srcDir, xmlFiles[i]);

                //  File srcFile = new File(  "/home/dharshana/automation/testNgIntegration1/system-test-framework/reports/BPSScenariosSuite/","testng-results.xml");

                File destFile = new File(destDir, "Report.xml");

                if (destFile.exists() && srcFile.lastModified() < destFile.lastModified()) {
                    getLog().info("file up-to-date: " + destFile);
                    //  continue;
                }
                getLog().info("transform, srcFile: " + srcFile + ", destFile: " + destFile);
                transformer.transform(new StreamSource(srcFile), new StreamResult(destFile));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getIncludedFiles(File directory, String includes) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(directory);
        scanner.setIncludes(new String[]{includes});
        scanner.scan();
        String[] filesToFormat = scanner.getIncludedFiles();
        return filesToFormat;
    }
}
