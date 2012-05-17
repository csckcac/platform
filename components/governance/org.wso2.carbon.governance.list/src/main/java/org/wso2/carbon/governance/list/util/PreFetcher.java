package org.wso2.carbon.governance.list.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;

import java.util.concurrent.ExecutorService;

public class PreFetcher implements Runnable {

    private static final Log log = LogFactory.getLog(PreFetcher.class);

    protected Registry governanceRegistry;
    protected static ExecutorService artifactFetcherExecutor;
    private String[] paths;

    int poolSize = 10;
    int servicePoolSize = 10;


    public void setGovernanceRegistry(Registry governanceRegistry) {
        this.governanceRegistry = governanceRegistry;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    @Override
    public void run() {
        ListServiceUtil.populateServiceInfoMap(governanceRegistry, paths, ListServiceUtil.getServiceListMap());
    }
}
