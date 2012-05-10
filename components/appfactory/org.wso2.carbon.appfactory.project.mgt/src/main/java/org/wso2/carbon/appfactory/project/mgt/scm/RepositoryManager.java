package org.wso2.carbon.appfactory.project.mgt.scm;

/**
 * Created by IntelliJ IDEA.
 * User: aja
 * Date: 4/30/12
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RepositoryManager {
    String createRepository(String name);
    boolean deleteRepository(String name);
    String getRepository(String name);
}
