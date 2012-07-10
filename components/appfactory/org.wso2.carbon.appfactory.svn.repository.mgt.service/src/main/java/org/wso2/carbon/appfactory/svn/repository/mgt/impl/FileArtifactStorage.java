/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package org.wso2.carbon.appfactory.svn.repository.mgt.impl;


import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ArtifactStorage;

import java.io.File;
import java.util.List;

public class FileArtifactStorage implements ArtifactStorage {
    private static final Log log = LogFactory.getLog(FileArtifactStorage.class);

    @Override
    public File retrieveArtifact(String applicationId, String version, String revision) {
        File targetDir = null;
        List<File> artifactFiles = null;
        try {
            File workDir = AppFactoryUtil.getApplicationWorkDirectory(applicationId, version, revision);
            File targetDirPath = new File(workDir.getAbsolutePath() + File.separator + "target");
            String[] fileExtension = {"car"};
            artifactFiles = (List<File>) FileUtils.listFiles(targetDirPath, fileExtension, false);

        } catch (AppFactoryException e) {
            log.error("Error in retrieving the artifact" + e);
        }
        targetDir= artifactFiles.get(0);
        return targetDir;
    }

    @Override
    public void storeArtifact(String applicationId, String version, String revision, File file) {
        // artifact will be stored from the build process
    }
}
