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

package org.wso2.carbon.appfactory.svn.repository.mgt.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.util.Util;

/**
 *
 *
 */
public class RepositoryManagerHolder {
    private static final Log log= LogFactory.getLog(RepositoryManagerHolder.class);
    private static RepositoryManagerHolder holder;
    private static RepositoryManager manger;
    private RepositoryManagerHolder(){

    }
    public static RepositoryManagerHolder getInstance(){
        if(holder==null){
            holder=new RepositoryManagerHolder();
        }
        if(holder.getRepositoryManager()==null){
            buildRepositoryManager();
        }
        return holder;
    }

    private static void buildRepositoryManager()  {
        RepositoryManager repositoryManager;
        AppFactoryConfiguration configuration= Util.getConfiguration();
        if(configuration!=null) {
        RepositoryManagerBuilder builder=new RepositoryManagerBuilder(configuration);
        repositoryManager=builder.buildRepositoryManager();
            if (repositoryManager==null){
                String msg="Could not get any repository manager";
                log.error(msg);
            } else {
                RepositoryManagerHolder.manger=repositoryManager;
            }
        }else {
            String msg="Could not get AppFactory configuration to build a repository manager";
            log.error(msg);

        }
    }

    public  RepositoryManager getRepositoryManager(){
        return   manger;
    }
}
