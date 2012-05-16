/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.mediator.autoscale.lbautoscale.mediators;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;

@SuppressWarnings("unused")
public class AutoscaleInMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator mediator) {
        AutoscaleInMediator autoscaleInMediator = (AutoscaleInMediator) mediator;
        OMElement autoscaleIn = fac.createOMElement("autoscaleIn", synNS);
        autoscaleIn.addAttribute(OMAbstractFactory.getOMFactory().createOMAttribute("configuration", null,
                                                                                    autoscaleInMediator.getConfiguration()));
        saveTracingState(autoscaleIn, mediator);

        return autoscaleIn;
    }

    public String getMediatorClassName() {
        return AutoscaleInMediator.class.getName();
    }
}
