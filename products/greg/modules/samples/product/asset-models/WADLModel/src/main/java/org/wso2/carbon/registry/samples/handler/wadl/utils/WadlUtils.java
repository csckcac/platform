package org.wso2.carbon.registry.samples.handler.wadl.utils;

public class WadlUtils {
    public static final java.lang.String ARTIFACT_ID_PROP_KEY = "registry.artifactId";
    public static final java.lang.String DEPENDS = "depends";
    public static final java.lang.String USED_BY = "usedBy";
    public static final String OWNS = "depends";
    public static final String OWNED_BY = "ownedBy";
    public static final java.lang.String ACTUAL_PATH = "registry.actualpath";

    // handling the possibility that handlers are not called within each other.
    private static ThreadLocal<Boolean> updateInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isUpdateLockAvailable() {
        return !updateInProgress.get();
    }

    public static void acquireUpdateLock() {
        updateInProgress.set(true);
    }

    public static void releaseUpdateLock() {
        updateInProgress.set(false);
    }
}
