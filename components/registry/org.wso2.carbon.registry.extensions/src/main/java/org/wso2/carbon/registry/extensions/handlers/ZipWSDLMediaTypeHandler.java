/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.beans.BusinessServiceInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.*;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.wso2.carbon.user.core.UserRealm;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

@SuppressWarnings({"unused", "UnusedAssignment"})
public class ZipWSDLMediaTypeHandler extends WSDLMediaTypeHandler {

//    <handler class="org.wso2.carbon.registry.extensions.handlers.ZipWSDLMediaTypeHandler">
//        <property name="wsdlMediaType">application/wsdl+xml</property>
//        <property name="schemaMediaType">application/xsd+xml</property>
//        <property name="threadPoolSize">50</property>
//        <property name="useOriginalSchema">true</property>
//        <!--property name="disableWSDLValidation">true</property>
//        <property name="disableSchemaValidation">true</property>
//        <property name="wsdlExtension">.wsdl</property>
//        <property name="schemaExtension">.xsd</property>
//        <property name="archiveExtension">.gar</property>
//        <property name="tempFilePrefix">wsdl</property-->
//        <property name="schemaLocationConfiguration" type="xml">
//            <location>/governance/schemas/</location>
//        </property>
//        <property name="wsdlLocationConfiguration" type="xml">
//            <location>/governance/wsdls/</location>
//        </property>
//        <filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher">
//            <property name="mediaType">application/vnd.wso2.governance-archive</property>
//        </filter>
//    </handler>

    private String wsdlMediaType = "application/wsdl+xml";

    private String wsdlExtension = ".wsdl";

    private String xsdMediaType = "application/xsd+xml";

    private String xsdExtension = ".xsd";

    private String archiveExtension = ".gar";

    private String tempFilePrefix = "wsdl";

    private boolean disableWSDLValidation = false;
    private boolean disableSchemaValidation = false;
    private boolean useOriginalSchema = false;

    private boolean disableSymlinkCreation = false;
    private static int numberOfRetry = 5;

    public void setNumberOfRetry(String numberOfRetry) {
        ZipWSDLMediaTypeHandler.numberOfRetry = Integer.parseInt(numberOfRetry);
    }

    public boolean isDisableSymlinkCreation() {
        return disableSymlinkCreation;
    }

    public void setDisableSymlinkCreation(String disableSymlinkCreation) {
        this.disableSymlinkCreation = Boolean.toString(true).equals(disableSymlinkCreation);
    }


    private int threadPoolSize = 50;

    private static final Log log = LogFactory.getLog(ZipWSDLMediaTypeHandler.class);

    public void setThreadPoolSize(String threadPoolSize) {
        this.threadPoolSize = Integer.parseInt(threadPoolSize);
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Resource resource = requestContext.getResource();
            String path = requestContext.getResourcePath().getPath();
            try {
                // If the WSDL is already there, we don't need to re-run this handler unless the content is changed.
                // Re-running this handler causes issues with downstream handlers and other behaviour (ex:- lifecycles).
                // If you need to do a replace programatically, delete-then-replace.
                if (requestContext.getRegistry().resourceExists(path)) {
                    // TODO: Add logic to compare content, and return only if the content didn't change.
                    return;
                }
            } catch (Exception ignore) { }
            try {
                if (resource != null) {
                    Object resourceContent = resource.getContent();
                    InputStream in = new ByteArrayInputStream((byte[]) resourceContent);
                    Stack<File> fileList = new Stack<File>();
                    List<String> uriList = new LinkedList<String>();
                    List<UploadTask> tasks = new LinkedList<UploadTask>();

                    int threadPoolSize = this.threadPoolSize;

                    int wsdlPathDepth = Integer.MAX_VALUE;
                    int xsdPathDepth = Integer.MAX_VALUE;
                    File tempFile = File.createTempFile(tempFilePrefix, archiveExtension);
                    File tempDir = new File(tempFile.getAbsolutePath().substring(0,
                            tempFile.getAbsolutePath().length() - archiveExtension.length()));
                    try {
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                        try {
                            byte[] contentChunk = new byte[1024];
                            int byteCount;
                            while ((byteCount = in.read(contentChunk)) != -1) {
                                out.write(contentChunk, 0, byteCount);
                            }
                            out.flush();
                        } finally {
                            out.close();
                        }
                        ZipEntry entry;

                        makeDir(tempDir);
                        ZipInputStream zs;
                        List<String> wsdlUriList = new LinkedList<String>();
                        List<String> xsdUriList = new LinkedList<String>();
                        zs = new ZipInputStream(new FileInputStream(tempFile));
                        try {
                            entry = zs.getNextEntry();
                            while (entry != null) {
                                String entryName = entry.getName();
                                FileOutputStream os;
                                File file = new File(tempFile.getAbsolutePath().substring(0,
                                        tempFile.getAbsolutePath().length() -
                                                archiveExtension.length()) + File.separator + entryName);
                                if (entry.isDirectory()) {
                                    if (!file.exists()) {
                                        makeDirs(file);
                                        fileList.push(file);
                                    }
                                    entry = zs.getNextEntry();
                                    continue;
                                }
                                File parentFile = file.getParentFile();
                                if (!parentFile.exists()) {
                                    makeDirs(parentFile);
                                }
                                os = new FileOutputStream(file);
                                try {
                                    fileList.push(file);
                                    byte[] contentChunk = new byte[1024];
                                    int byteCount;
                                    while ((byteCount = zs.read(contentChunk)) != -1) {
                                        os.write(contentChunk, 0, byteCount);
                                    }
                                } finally {
                                    os.close();
                                }
                                zs.closeEntry();
                                entry = zs.getNextEntry();
                                if (entryName != null &&
                                        entryName.toLowerCase().endsWith(wsdlExtension)) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    int uriPathDepth = uri.split("/").length;
                                    if (uriPathDepth < wsdlPathDepth) {
                                        wsdlPathDepth = uriPathDepth;
                                        wsdlUriList = new LinkedList<String>();
                                    }
                                    if (wsdlPathDepth == uriPathDepth) {
                                        wsdlUriList.add(uri);
                                    }
                                } else if (entryName != null &&
                                        entryName.toLowerCase().endsWith(xsdExtension)) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    int uriPathDepth = uri.split("/").length;
                                    if (uriPathDepth < xsdPathDepth) {
                                        xsdPathDepth = uriPathDepth;
                                        xsdUriList = new LinkedList<String>();
                                    }
                                    if (xsdPathDepth == uriPathDepth) {
                                        xsdUriList.add(uri);
                                    }
                                } else if (entryName != null) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    uriList.add(uri);
                                }
                            }
                        } finally {
                            zs.close();
                        }
                        Map<String, String> localPathMap = null;
                        if (CurrentSession.getLocalPathMap() != null) {
                            localPathMap =
                                    Collections.unmodifiableMap(CurrentSession.getLocalPathMap());
                        }
                        if (wsdlUriList.isEmpty() && xsdUriList.isEmpty()) {
                            throw new RegistryException(
                                    "No WSDLs or Schemas found in the given WSDL archive");
                        }
                        if (wsdlPathDepth < Integer.MAX_VALUE) {
                            for (String uri : wsdlUriList) {
                                tasks.add(new UploadWSDLTask(requestContext, uri,
                                        CurrentSession.getTenantId(),
                                        CurrentSession.getUserRegistry(), CurrentSession.getUserRealm(),
                                        CurrentSession.getUser(), CurrentSession.getCallerTenantId(),
                                        localPathMap));
                            }
                        }
                        if (xsdPathDepth < Integer.MAX_VALUE) {
                            for (String uri : xsdUriList) {
                                tasks.add(new UploadXSDTask(requestContext, uri,
                                        CurrentSession.getTenantId(),
                                        CurrentSession.getUserRegistry(), CurrentSession.getUserRealm(),
                                        CurrentSession.getUser(), CurrentSession.getCallerTenantId(),
                                        localPathMap));
                            }
                        }

                        // calculate thread pool size for efficient use of resources in concurrent
                        // update scenarios.
                        int toAdd = wsdlUriList.size() + xsdUriList.size();
                        if (toAdd < threadPoolSize) {
                            if (toAdd < (threadPoolSize / 8)) {
                                threadPoolSize = 0;
                            } else if (toAdd < (threadPoolSize / 2)) {
                                threadPoolSize = (threadPoolSize / 8);
                            } else {
                                threadPoolSize = (threadPoolSize / 4);
                            }
                        }
                    } finally {
                        in.close();
                        resourceContent = null;
                        resource.setContent(null);
                    }
                    uploadFiles(tasks, tempFile, fileList, tempDir, threadPoolSize, path, uriList,
                            requestContext);
                }
            } catch (IOException e) {
                throw new RegistryException("Error occurred while unpacking Governance Archive", e);
            }
            if (Transaction.isRollbacked()) {
                throw new RegistryException("A nested transaction was rollbacked and therefore " +
                        "cannot proceed with this action.");
            }
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method that runs the WSDL upload procedure.
     *
     * @param requestContext the request context for the import/put operation
     * @param uri the URL from which the WSDL is imported
     *
     * @return the path at which the WSDL was uploaded to
     *
     * @throws RegistryException if the operation failed.
     */
    protected String addWSDLFromZip(RequestContext requestContext, String uri)
            throws RegistryException {
        if (uri != null) {
            Resource local = requestContext.getRegistry().newResource();
            local.setMediaType(wsdlMediaType);
            requestContext.setSourceURL(uri);
            requestContext.setResource(local);
            String path = requestContext.getResourcePath().getPath();
            if (path.lastIndexOf("/") != -1) {
                path = path.substring(0, path.lastIndexOf("/"));
            } else {
                path = "";
            }
            String wsdlName = uri;
            if (wsdlName.lastIndexOf("/") != -1) {
                wsdlName = wsdlName.substring(wsdlName.lastIndexOf("/"));
            } else {
                wsdlName = "/" + wsdlName;
            }
            path = path + wsdlName;
            requestContext.setResourcePath(new ResourcePath(path));
            WSDLProcessor wsdlProcessor = buildWSDLProcessor(requestContext, this.useOriginalSchema);
            String addedPath = wsdlProcessor.addWSDLToRegistry(requestContext, uri, local, false,
                    true, disableWSDLValidation);
            if (CommonConstants.ENABLE.equals(System.getProperty(
                    CommonConstants.UDDI_SYSTEM_PROPERTY))) {
                BusinessServiceInfo businessServiceInfo = new BusinessServiceInfo();
                WSDLInfo wsdlInfo = wsdlProcessor.getMasterWSDLInfo();
                businessServiceInfo.setServiceWSDLInfo(wsdlInfo);
                UDDIPublisher publisher = new UDDIPublisher(businessServiceInfo);
                publisher.publishBusinessService();
            }
            log.debug("WSDL : " + addedPath);
            return addedPath;
        }
        return null;
    }

    /**
     * Method that runs the Schema upload procedure.
     *
     * @param requestContext the request context for the import/put operation
     * @param uri the URL from which the Schema is imported
     *
     * @return the path at which the schema was uploaded to
     *
     * @throws RegistryException if the operation failed.
     */
    protected String addSchemaFromZip(RequestContext requestContext, String uri)
            throws RegistryException {
        if (uri != null) {
            Resource local = requestContext.getRegistry().newResource();
            local.setMediaType(xsdMediaType);
            requestContext.setSourceURL(uri);
            requestContext.setResource(local);
            String path = requestContext.getResourcePath().getPath();
            if (path.lastIndexOf("/") != -1) {
                path = path.substring(0, path.lastIndexOf("/"));
            } else {
                path = "";
            }
            String xsdName = uri;
            if (xsdName.lastIndexOf("/") != -1) {
                xsdName = xsdName.substring(xsdName.lastIndexOf("/"));
            } else {
                xsdName = "/" + xsdName;
            }
            path = path + xsdName;
            requestContext.setResourcePath(new ResourcePath(path));
            WSDLValidationInfo validationInfo = null;
            try {
                if (!disableSchemaValidation) {
                    validationInfo = SchemaValidator.validate(new XMLInputSource(null, uri, null));
                }
            } catch (Exception e) {
                throw new RegistryException("Exception occured while validating the schema" , e);
            }
            SchemaProcessor schemaProcessor =
                    buildSchemaProcessor(requestContext, validationInfo, this.useOriginalSchema);

            String savedName = schemaProcessor
                    .importSchemaToRegistry(requestContext, path,
                            getChrootedLocation(requestContext.getRegistryContext()), true);

            String parentPath = RegistryUtils.getParentPath(requestContext.getResourcePath().getPath());

            if (parentPath.startsWith("//")) {
                parentPath = parentPath.substring(1);
            }
            if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                requestContext.setActualPath(parentPath + savedName);
            } else {
                requestContext.setActualPath(
                        parentPath + RegistryConstants.PATH_SEPARATOR + savedName);
            }
            String addedPath = requestContext.getActualPath();
            log.debug("XSD : " + addedPath);
            return addedPath;
        }
        return null;
    }

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + schemaLocation);
    }

    public void importResource(RequestContext context) {
        // We don't support importing .gar files. This is meant only for uploading WSDL files
        // and imports from the local filesystem.
        log.warn("The imported Governance Web Archive will not be extracted. To extract the content"
                + " upload the archive from the file system.");
    }

    public void setWsdlMediaType(String wsdlMediaType) {
        this.wsdlMediaType = wsdlMediaType;
    }

    public void setWsdlExtension(String wsdlExtension) {
        this.wsdlExtension = wsdlExtension;
    }

    public void setSchemaMediaType(String xsdMediaType) {
        this.xsdMediaType = xsdMediaType;
    }

    public void setSchemaExtension(String xsdExtension) {
        this.xsdExtension = xsdExtension;
    }

    public void setArchiveExtension(String archiveExtension) {
        this.archiveExtension = archiveExtension;
    }

    public void setTempFilePrefix(String tempFilePrefix) {
        this.tempFilePrefix = tempFilePrefix;
    }

    public void setDisableWSDLValidation(String disableWSDLValidation) {
        this.disableWSDLValidation = Boolean.toString(true).equals(disableWSDLValidation);
    }

    public void setDisableSchemaValidation(String disableSchemaValidation) {
        this.disableSchemaValidation = Boolean.toString(true).equals(disableSchemaValidation);
    }

    public void setUseOriginalSchema(String useOriginalSchema) {
        this.useOriginalSchema = Boolean.toString(true).equals(useOriginalSchema);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPutCompleted(String path, Map<String, String> addedResources,
                                  List<String> otherResources, RequestContext requestContext)
    //Final result printing in console.
            throws RegistryException {
        log.info("Total Number of Files Uploaded: " + addedResources.size());
        List<String> failures = new LinkedList<String>();
        for (Map.Entry<String, String> e : addedResources.entrySet()) {
            if (e.getValue() == null) {
                failures.add(e.getKey());
                log.info("Failure " + failures.size() + ": " + e.getKey());
            }
        }
        log.info("Total Number of Files Failed to Upload: " + failures.size());
        if (otherResources.size() > 0) {
            log.info("Total Number of Files Not-Uploaded: " + otherResources.size());
        }
    }

    protected void uploadFiles(List<UploadTask> tasks,
                           File tempFile, Stack<File> fileList, File tempDir, int poolSize,
                           String path, List<String> uriList, RequestContext requestContext)
            throws RegistryException {
        CommonUtil.loadImportedArtifactMap();
        try {
            if (poolSize <= 0) {
                boolean updateLockAvailable = CommonUtil.isUpdateLockAvailable();
                if (!updateLockAvailable) {
                    CommonUtil.releaseUpdateLock();
                }
                try {
                    for (UploadTask task : tasks) {
                        task.run();
                    }
                } finally {
                    if (!updateLockAvailable) {
                        CommonUtil.acquireUpdateLock();
                    }
                }
            } else {
                ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
                if (!CommonUtil.isArtifactIndexMapExisting()) {
                    CommonUtil.createArtifactIndexMap();
                }
                if (!CommonUtil.isSymbolicLinkMapExisting()) {
                    CommonUtil.createSymbolicLinkMap();
                }
                for (UploadTask task : tasks) {
                    executorService.submit(task);
                }
                executorService.shutdown();
                while (!executorService.isTerminated()) {

                }
            }
        } finally {
            CommonUtil.clearImportedArtifactMap();
        }
        try {
            if (CommonUtil.isArtifactIndexMapExisting()) {
                Map<String, String> artifactIndexMap =
                        CommonUtil.getAndRemoveArtifactIndexMap();

                if (log.isDebugEnabled()) {
                    for (Map.Entry<String, String> entry : artifactIndexMap.entrySet()) {
                        log.debug("Added Artifact Entry: " + entry.getKey());
                    }
                }

//                CommonUtil.addGovernanceArtifactEntriesWithRelativeValues(
//                        CommonUtil.getUnchrootedSystemRegistry(requestContext), artifactIndexMap);
            }
            Registry registry = requestContext.getRegistry();
            if (!isDisableSymlinkCreation() && CommonUtil.isSymbolicLinkMapExisting()) {
                Map<String, String> symbolicLinkMap =
                        CommonUtil.getAndRemoveSymbolicLinkMap();

                for (Map.Entry<String, String> entry : symbolicLinkMap.entrySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Added Symbolic Link: " + entry.getKey());
                    }
                    try {
                        if (registry.resourceExists(entry.getKey())) {
                            registry.removeLink(entry.getKey());
                        }
                    } catch (RegistryException ignored) {
                        // we are not bothered above errors in getting rid of symbolic links.
                    }
                    requestContext.getSystemRegistry().createLink(entry.getKey(), entry.getValue());
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to build artifact index.", e);
        }
        Map<String, String> taskResults = new LinkedHashMap<String, String>();
        for (UploadTask task : tasks) {
            if (task.getFailed()) {
                taskResults.put(task.getUri(), null);
            } else {
                taskResults.put(task.getUri(), task.getResult());
            }
        }
        onPutCompleted(path, taskResults, uriList, requestContext);
        try {
            delete(tempFile);
            while (!fileList.isEmpty()) {
                delete(fileList.pop());
            }
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            log.error("Unable to cleanup temporary files", e);
        }
        log.info("Completed uploading files from archive file");
    }

    protected static abstract class UploadTask implements Runnable {

        private String uri;
        private RequestContext requestContext;
        private int tenantId = -1;
        private UserRegistry userRegistry;
        private UserRealm userRealm;
        private String userId;
        private int callerTenantId;
        private Map<String, String> localPathMap;
        private Random random = new Random(10);

        protected String result = null;
        protected boolean failed = false;
        protected int retries = 0;

        public UploadTask(RequestContext requestContext, String uri, int tenantId,
                          UserRegistry userRegistry, UserRealm userRealm, String userId,
                          int callerTenantId, Map<String, String> localPathMap) {
            this.userRegistry = userRegistry;
            this.userRealm = userRealm;
            this.tenantId = tenantId;
            this.requestContext = requestContext;
            this.uri = uri;
            this.userId = userId;
            this.callerTenantId = callerTenantId;
            this.localPathMap = localPathMap;
        }

        public void run() {
            // File is already uploaded via wsdl or xsd imports those are skip
            if (CommonUtil.isImportedArtifactExisting(new File(uri).toString())) {
                failed = false;
                result = "added from import";
                return;
            }
            doWork();
        }

        protected void retry() {
            //Number of retry can be configurable via handler configuration (<property name="numberOfRetry">1</property>)
            if (retries < ZipWSDLMediaTypeHandler.numberOfRetry) {
                ++retries;
                log.info("Retrying to upload resource: " + uri);
                int i = random.nextInt(10);
                if (log.isDebugEnabled()) {
                    log.debug("Waiting for " + i + " seconds");
                }
                try {
                    Thread.sleep(1000 * i);
                } catch (InterruptedException ignored) {
                }
                doWork();
            } else {
                failed = true;
            }
        }

        private void doWork() {
            CurrentSession.setTenantId(tenantId);
            CurrentSession.setUserRegistry(userRegistry);
            CurrentSession.setUserRealm(userRealm);
            CurrentSession.setUser(userId);
            CurrentSession.setCallerTenantId(callerTenantId);
            if (localPathMap != null) {
                CurrentSession.setLocalPathMap(localPathMap);
            }
            try {
                if (CommonUtil.isUpdateLockAvailable()) {
                    CommonUtil.acquireUpdateLock();
                    try {
                        RequestContext requestContext =
                                new RequestContext(this.requestContext.getRegistry(),
                                        this.requestContext.getRepository(),
                                        this.requestContext.getVersionRepository());
                        requestContext.setResourcePath(this.requestContext.getResourcePath());
                        requestContext.setResource(this.requestContext.getResource());
                        requestContext.setOldResource(this.requestContext.getOldResource());
                        doProcessing(requestContext, uri);
                    } finally {
                        CommonUtil.releaseUpdateLock();
                    }
                }
            } catch (RegistryException e) {
                log.error("An error occurred while  uploading "+uri, e);
                retry();
            } catch (RuntimeException e) {
                log.error("An unhandled exception occurred while  uploading " + uri, e);
                retry();
            } finally {
                CurrentSession.removeUser();
                CurrentSession.removeUserRealm();
                CurrentSession.removeUserRegistry();
                CurrentSession.removeTenantId();
                CurrentSession.removeCallerTenantId();
                if (localPathMap != null) {
                    CurrentSession.removeLocalPathMap();
                }
                // get rid of the reference to the request context at the end.
                requestContext = null;
            }
        }

        protected abstract void doProcessing(RequestContext requestContext, String uri)
                throws RegistryException;

        public String getUri() {
            return uri;
        }

        public String getResult() {
            return result;
        }

        public boolean getFailed() {
            return failed;
        }
    }

    protected class UploadXSDTask extends UploadTask {

        public UploadXSDTask(RequestContext requestContext, String uri, int tenantId,
                          UserRegistry userRegistry, UserRealm userRealm, String userId,
                          int callerTenantId, Map<String, String> localPathMap) {
            super(requestContext, uri, tenantId, userRegistry, userRealm, userId, callerTenantId,
                    localPathMap);
        }

        protected void doProcessing(RequestContext requestContext, String uri)
                throws RegistryException {
            result = addSchemaFromZip(requestContext, uri);
        }
    }

    protected class UploadWSDLTask extends UploadTask {

        public UploadWSDLTask(RequestContext requestContext, String uri, int tenantId,
                          UserRegistry userRegistry, UserRealm userRealm, String userId,
                          int callerTenantId, Map<String, String> localPathMap) {
            super(requestContext, uri, tenantId, userRegistry, userRealm, userId, callerTenantId,
                    localPathMap);
        }

        protected void doProcessing(RequestContext requestContext, String uri)
                throws RegistryException {
            result = addWSDLFromZip(requestContext, uri);
        }
    }
}
