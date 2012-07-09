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
import org.codehaus.plexus.util.DirectoryScanner;
import org.wso2.carbon.automation.core.ProductConstant;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class HtmlReporterMojo extends AbstractMojo {

    /**
     * Sets the XSL stylesheet to use.
     *
     * @parameter
     * @required
     */
    private File xslFile;

    /**
     * The directory containing the XML files.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File srcDir;

    /**
     * For Source Directory. Specifies a fileset source file to format. This is a comma- or space-separated list of patterns of files.
     *
     * @parameter default-value="**\/*.xml"
     */
    private String srcIncludes;

    /**
     * For Source Directory. Source files excluded from format. This is a comma- or space-separated list of patterns of files.
     */
    private String srcExcludes;

    /**
     * The destination directory to write the XML files.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File destDir;

    /**
     * A regular expression that will may match part of the XML file name for replacement.
     *
     * @parameter
     */
    private String fileNameRegex;

    /**
     * The replacement for the matched regular expression of the XML file name.
     *
     * @parameter
     */
    private String fileNameReplacement;

    public void execute()
            throws MojoExecutionException {
        xslFile = new File("/home/dharshana/automation/testNgIntegration1/system-test-framework/core/org.wso2.automation.platform.core/src/main/java/org/wso2/platform/test/core/utils/reportutills/testng-results.xsl");
        srcDir = new File(ProductConstant.REPORT_REPOSITORY);
        srcIncludes = "*.Test.xml";
        destDir = new File(ProductConstant.REPORT_REPOSITORY);
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

                //File srcFile = new File(  "/home/dharshana/automation/testNgIntegration1/system-test-framework/reports/BPSScenariosSuite/","testng-results.xml");

                File destFile = new File(destDir, "Report.html");

                if (destFile.exists() && srcFile.lastModified() < destFile.lastModified()) {
                    getLog().info("file up-to-date: " + destFile);
                    //  continue;
                }
                getLog().info("transform, srcFile: " + srcFile + ", destFile: " + destFile);
                transformer.transform(new StreamSource(srcFile), new StreamResult(destFile));
            }

        } catch (Exception ignored) {
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
