/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.provider.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.server.ServerAssociationStore;
import org.wso2.carbon.identity.provider.openid.cache.AssociationCache;

import java.util.Date;

/**
 *  Carbon implementation of ServerAssociationStore 
 */
public class CarbonServerAssociationStore implements ServerAssociationStore {

    private String timeStamp;
    private int counter;
    private AssociationCache associationCache = null;

    private static Log log = LogFactory.getLog(CarbonServerAssociationStore.class);

    public CarbonServerAssociationStore() {
        timeStamp = Long.toString(new Date().getTime());
        counter   = 0;
        associationCache = AssociationCache.getCacheInstance();
    }

    @Override
    public Association generate(String type, int expiryIn) throws AssociationException {

        String handle = timeStamp + "-" + counter++;

        Association association = Association.generate(type, handle, expiryIn);
        associationCache.addToCache(association);

        if(log.isDebugEnabled()){
            log.debug("Generated association, handle: " + handle +
                                  " type: " + type +
                                  " expires in: " + expiryIn + " seconds.");
        }
        return association;
    }

    @Override
    public Association load(String handle) {
        return associationCache.getFromCache(handle);
    }

    @Override
    public void remove(String handle) {

        if(log.isDebugEnabled()){
            log.debug("Removing association, handle: " + handle);
        }
        associationCache.removeCacheEntry(handle);
    }       
}
