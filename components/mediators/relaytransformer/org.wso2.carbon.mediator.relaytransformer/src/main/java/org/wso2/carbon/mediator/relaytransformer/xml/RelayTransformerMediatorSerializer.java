package org.wso2.carbon.mediator.relaytransformer.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.MediatorProperty;
import org.wso2.carbon.mediator.relaytransformer.RelayTransformerMediator;

import javax.xml.namespace.QName;
import java.util.List;

public class RelayTransformerMediatorSerializer extends AbstractMediatorSerializer {
    private static final QName ATTRIBUTE_Q
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof RelayTransformerMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        RelayTransformerMediator mediator = (RelayTransformerMediator) m;
        OMElement relayTransformer = fac.createOMElement("relayTransformer", synNS);

        if (mediator.getXsltKey() != null) {
            // Serialize Value using ValueSerializer
            ValueSerializer keySerializer =  new ValueSerializer();
            keySerializer.serializeValue(mediator.getXsltKey(), XMLConfigConstants.KEY, relayTransformer);
        } else {
            handleException("Invalid relayTransformer mediator. XSLT registry key is required");
        }
        saveTracingState(relayTransformer,mediator);

        if (mediator.getSource() != null) {

            SynapseXPathSerializer.serializeXPath(mediator.getSource(), relayTransformer, "source");
        }
        if (mediator.getTargetPropertyName() != null) {
            relayTransformer.addAttribute(fac.createOMAttribute(
                    "target", nullNS, mediator.getTargetPropertyName()));
        }
        if (mediator.getInputType() != null) {
            relayTransformer.addAttribute(fac.createOMAttribute(
                    "input", nullNS, mediator.getInputType()));
        }
        serializeProperties(relayTransformer, mediator.getProperties());
        List<MediatorProperty> features = mediator.getFeatures();
        if (!features.isEmpty()) {
            for (MediatorProperty mp : features) {
                OMElement prop = fac.createOMElement("feature", synNS, relayTransformer);
                if (mp.getName() != null) {
                    prop.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
                } else {
                    handleException("The Feature name is missing");
                }
                if (mp.getValue() != null) {
                    prop.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));
                }  else {
                    handleException("The Feature value is missing");
                }
            }
        }
        serializeMediatorProperties(relayTransformer, mediator.getAttributes(), ATTRIBUTE_Q);

        ResourceMapSerializer.serializeResourceMap(relayTransformer, mediator.getResourceMap());

        return relayTransformer;
    }

    public String getMediatorClassName() {
        return RelayTransformerMediator.class.getName();
    }
}
