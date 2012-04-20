/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.mgt.services;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.ode.integration.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageInfo;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageRepository;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.BPELPackageManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.PackageManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.*;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * BPEL Package management admin service.
 */
public class BPELPackageManagementServiceSkeleton extends AbstractAdmin
        implements BPELPackageManagementServiceSkeletonInterface {
    private static Log log = LogFactory.getLog(BPELPackageManagementServiceSkeleton.class);

    public ProcessesInPackage listProcessesInPackage(String packageName) throws PackageManagementException {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();
        ProcessesInPackage processInPackage = new ProcessesInPackage();
        processInPackage.setPackageName(packageName);
        
        try {
            List<QName> processIds = tenantProcessStore.getProcessesInPackage(packageName);
                for (QName pid : processIds) {
                    processInPackage.addProcess(pid.toString());
                }
        } catch (Exception e) {
            String errMsg = "List processes in BPEL package: " + packageName + " failed.";
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        }

        return processInPackage;
    }

    public UndeployStatus_type0 undeployBPELPackage(String packageName) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        
        if(log.isDebugEnabled()) {
            log.debug("Starting undeployment of BPEL package " + packageName);
        }
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();
        try{
            tenantProcessStore.undeploy(packageName);
        } catch (Exception e) {
            log.error("Undeploying BPEL package " + packageName + " failed.", e);
           return UndeployStatus_type0.FAILED;
        }

        return UndeployStatus_type0.SUCCESS;
    }

    public DeployedPackagesPaginated listDeployedPackagesPaginated(int page) throws org.wso2.carbon.
            bpel.skeleton.ode.integration.mgt.services.PackageManagementException {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        int tPage = page;
        List<BPELPackageInfo> packages;
        DeployedPackagesPaginated paginatedPackages = new DeployedPackagesPaginated();
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();

        BPELPackageRepository packageRepo = tenantProcessStore.getBPELPackageRepository();
        try {
            packages = packageRepo.getBPELPackages();   // Can return null and we should handle that
        } catch (Exception e) {
            String errorMessage = "Cannot get the BPEL Package list from repository.";
            log.error(errorMessage, e);
            throw new PackageManagementException(errorMessage, e);
        }

        if (packages != null) {
            // Calculating pagination information
            if (tPage < 0 || tPage == Integer.MAX_VALUE) {
                tPage = 0;
            }
            int startIndex = tPage * BPELConstants.ITEMS_PER_PAGE;
            int endIndex = (tPage + 1) * BPELConstants.ITEMS_PER_PAGE;

            int numberOfPackages = packages.size();
            int pages = (int) Math.ceil((double) numberOfPackages / BPELConstants.ITEMS_PER_PAGE);
            paginatedPackages.setPages(pages);

            BPELPackageInfo[] packagesArray =
                    packages.toArray(new BPELPackageInfo[numberOfPackages]);
            for (int i = startIndex; i < endIndex && i < numberOfPackages; i++) {
                paginatedPackages.add_package(getPackageInfo(packagesArray[i]));
            }

            return paginatedPackages;
        }

        // Returning empty result set with pages equal to zero for cases where null is returned from
        // BPEL repo.
        paginatedPackages.setPages(0);
        return paginatedPackages;
    }

    private Package_type0 getPackageInfo(BPELPackageInfo packageInfo) {
        Package_type0 bpelPackage = new Package_type0();
        bpelPackage.setName(packageInfo.getName());
        bpelPackage.setState(convertToPackageStatusType(packageInfo.getStatus()));
        bpelPackage.setVersions(getAllVersionsOfPackage(packageInfo));
        bpelPackage.setErrorLog(packageInfo.getCauseForDeploymentFailure());
        return bpelPackage;
    }

    private Versions_type0 getAllVersionsOfPackage(BPELPackageInfo packageInfo) {
        List<String> versions = packageInfo.getAvailableVersions();
        Versions_type0 versionsOfBPELPackage = new Versions_type0();
        for(String version : versions) {
            versionsOfBPELPackage.addPackageName(version);
        }

        return versionsOfBPELPackage;
    }

    private PackageStatusType convertToPackageStatusType(BPELPackageInfo.Status status) {
        if(status.equals(BPELPackageInfo.Status.DEPLOYED)) {
            return PackageStatusType.DEPLOYED;
        } else if(status.equals(BPELPackageInfo.Status.UNDEPLOYED)) {
            return PackageStatusType.UNDEPLOYED;
        } else if(status.equals(BPELPackageInfo.Status.FAILED)) {
            return PackageStatusType.FAILED;
        } else if(status.equals(BPELPackageInfo.Status.UPDATED)) {
            return PackageStatusType.UPDATED;
        }
         return PackageStatusType.UNDEFINED;
    }

    private TenantProcessStoreImpl getTenantProcessStore() {
        ConfigurationContext configContext = getConfigContext();
        Integer tenantId = MultitenantUtils.getTenantId(configContext);
        BPELServerImpl bpelServer = BPELServerImpl.getInstance();

        return (TenantProcessStoreImpl) bpelServer.getMultiTenantProcessStore().
                getTenantsProcessStore(tenantId);
    }

}
    
