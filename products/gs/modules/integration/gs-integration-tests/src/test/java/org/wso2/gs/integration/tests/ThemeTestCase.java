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

package org.wso2.gs.integration.tests;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.dashboard.mgt.theme.stub.GSThemeMgtServiceStub;
import org.wso2.carbon.dashboard.mgt.theme.stub.types.carbon.Theme;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.rmi.RemoteException;


import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.integration.framework.utils.FrameworkSettings.getFrameworkPath;

/*
 Tests for Themes in GS
 */

public class ThemeTestCase {

    private static final Log log = LogFactory.getLog(ThemeTestCase.class);
    String activeThemePath = File.separator + "repository" + File.separator + "gadget-server" +
            File.separator + "themes" + File.separator + "gs-dark" +
            File.separator + "localstyles" + File.separator + "gadget-server.css";

    String methodName = null;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    String userID = "admin";
    String mediaType = "application/vnd.wso2.gs.theme";
    String description = "Added a new theme";
    String symLocation = null;
    String themePath = File.separator + "user-themes" + File.separator + "gs-new-theme.zip";
    String gadgetFile;
    FileDataSource gadgetFileDataSource;
    DataHandler content;
    GSThemeMgtServiceStub themeMgtServiceStub;

    @BeforeMethod(groups = {"wso2.gs"})
    public void init() throws java.lang.Exception {
        String loggedInSessionCookie = util.login();
        themeMgtServiceStub = ThemeTestUtils.getThemeMgtServiceStub(loggedInSessionCookie);
        gadgetFile = ThemeTestUtils.getGadgetResourcePath(getFrameworkPath());
        gadgetFileDataSource = new FileDataSource(gadgetFile);
        content = new DataHandler(gadgetFileDataSource);
    }

    //Adds resources which are required for theme management
    @Test(groups = {"wso2.gs"}, description = "Adds resources which are required for theme management")
    public void testAddResource() throws RemoteException, Exception {
        themeMgtServiceStub.addResource(themePath, mediaType, description, content, null);
    }


    //This method tests setThemeForUser() method of ThemeMgtService.
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddResource"}, description = "This method sets the theme for the user")
    public void testSetThemeForUser() throws RemoteException, Exception {
        boolean setTheme = themeMgtServiceStub.setThemeForUser(activeThemePath, userID);
        assertTrue(setTheme, "Fails set Theme for");
        log.info("Set theme for the logged user successfully.");
    }

    //This method tests getDefaultThemeForUser() method of ThemeMgtService.
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testSetThemeForUser"}, description = "This method gets the default theme for the user")
    public void testGetDefaultThemeForUser() throws RemoteException, Exception {
        String loggedInSessionCookie = util.login();
        String defaultTheme = themeMgtServiceStub.getDefaultThemeForUser(userID);
        boolean check = activeThemePath.equals(defaultTheme);
        assertTrue(check, "Fails retrieving default theme for" + userID);
        log.info("Retrieved default theme for the logged user successfully.");
    }

    // This method tests getThemes() of ThemeMgtService.
    @Test(groups = {"wso2.gs"}, description = "This method tests getThemes() of ThemeMgtService")
    public void testGetTheme() throws RemoteException, Exception {
        String defaultThemeName = "GS Dark";
        //Get themes from registry.
        Theme[] themes = themeMgtServiceStub.getThemes(userID);
        for (Theme theme : themes) {
            if (theme != null) {
                if (defaultThemeName.
                        equalsIgnoreCase(theme.getThemeName())) {
                    assertTrue(defaultThemeName.
                            equalsIgnoreCase(theme.getThemeName()));
                    log.info("Themes could retrieve successfully.");
                    break;
                }
            }
        }

    }


}
