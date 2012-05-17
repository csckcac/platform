package org.wso2.carbon.autoscaler.service.adapters;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: lahiru
 * Date: 4/16/12
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContainerAdapter extends Adapter{
    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean spawnInstance(String domainName, String instanceId)
            throws ClassNotFoundException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean terminateInstance(String instanceId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRunningInstanceCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRunningInstanceCount(String domainName) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPendingInstanceCount(String domainName) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sanityCheck() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
