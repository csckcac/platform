/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;

import java.io.File;

public class SCMUpdateTask implements Runnable {

    private static final Log log = LogFactory.getLog(SCMUpdateTask.class);

    private String checkOutURL;
    private String checkInURL;
    private File workingDir;
    private boolean readOnly;

    public SCMUpdateTask(File workingDir, String checkOutURL, String checkInURL, boolean readOnly) {
        this.workingDir = workingDir;
        this.checkOutURL = checkOutURL;
        this.checkInURL = checkInURL;
        this.readOnly = readOnly;
    }

    public void run() {
        Embedder plexus = null;

        try {
            plexus = new Embedder();
            plexus.start();
            ScmManager scmManager = (ScmManager) plexus.lookup(ScmManager.ROLE);
            ScmRepository scmRepository = scmManager.makeScmRepository(checkOutURL);
            if (workingDir.list() == null) {
                log.error("A directory was not found in the given path: " +
                        workingDir.getAbsolutePath());
                return;
            } else if (workingDir.list().length == 0) {
                scmManager.checkOut(scmRepository, new ScmFileSet(workingDir));
            } else {
                scmManager.update(scmRepository, new ScmFileSet(workingDir));
            }
            if (!readOnly) {
                if (checkInURL == null) {
                    scmManager.checkIn(scmRepository, new ScmFileSet(workingDir), "");
                } else {
                    scmRepository = scmManager.makeScmRepository(checkInURL);
                    scmManager.checkIn(scmRepository, new ScmFileSet(workingDir), "");
                }
            }
        } catch (PlexusContainerException e) {
            log.error("Unable to start Plexus Container", e);
        } catch (ComponentLookupException e) {
            log.error("Unable to obtain instance of SCM Manager", e);
        } catch (ScmRepositoryException e) {
            log.error("Unable to create an instance of a SCM repository", e);
        } catch (NoSuchScmProviderException e) {
            log.error("A provider was not found for the specified SCM URL", e);
        } catch (ScmException e) {
            log.error("The SCM operation failed", e);
        }


        try {
            plexus.stop();
        } catch (RuntimeException ignore) {
            // Exceptions can be ignored as in the example from Maven.
        }
    }
}
