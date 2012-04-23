package org.wso2.csg.integration.tests.util;

import java.io.IOException;

public interface BackendServer {
    public void start() throws IOException;

    public void stop() throws IOException;

    public boolean isStarted();

    public void deployService(Object service) throws IOException;

}
