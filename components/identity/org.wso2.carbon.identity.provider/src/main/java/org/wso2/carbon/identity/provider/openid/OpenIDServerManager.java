/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.provider.openid;

import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.association.DiffieHellmanSession;
import org.openid4java.message.AssociationError;
import org.openid4java.message.AssociationRequest;
import org.openid4java.message.AssociationResponse;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.wso2.carbon.identity.base.IdentityConstants;

public class OpenIDServerManager extends ServerManager {

    /**
     * 
     */
    public Message associationResponse(ParameterList requestParams) {
        boolean isVersion2 = requestParams.hasParameter(IdentityConstants.OpenId.ATTR_NS);

        try {
            AssociationRequest assocReq = AssociationRequest
                    .createAssociationRequest(requestParams);

            isVersion2 = assocReq.isVersion2();

            AssociationSessionType type = assocReq.getType();

            // is supported / allowed ?
            if (!Association.isHmacSupported(type.getAssociationType())
                    || !DiffieHellmanSession.isDhSupported(type)
                    || getMinAssocSessEnc().isBetter(type)) {
                throw new AssociationException("Unable create association for: "
                        + type.getSessionType() + " / " + type.getAssociationType());
            } else {
                Association assoc = getPrivateAssociations().generate(type.getAssociationType(),
                        getExpireIn());
                return AssociationResponse.createAssociationResponse(assocReq, assoc);
            }
        } catch (Exception e) {
            // association failed, respond accordingly
            if (isVersion2) {
                return AssociationError.createAssociationError(e.getMessage(),
                        getPrefAssocSessEnc());
            } else {

                try {
                    // generate dummy association & no-encryption response
                    // for compatibility mode
                    Association dummyAssoc = getPrivateAssociations().generate(
                            Association.TYPE_HMAC_SHA1, 0);

                    AssociationRequest dummyRequest = AssociationRequest
                            .createAssociationRequest(AssociationSessionType.NO_ENCRYPTION_COMPAT_SHA1MAC);

                    return AssociationResponse.createAssociationResponse(dummyRequest, dummyAssoc);
                } catch (OpenIDException ex) {
                    return null;
                }
            }
        }
    }
}