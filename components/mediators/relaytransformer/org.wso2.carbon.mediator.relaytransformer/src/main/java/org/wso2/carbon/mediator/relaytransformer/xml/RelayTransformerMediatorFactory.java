package org.wso2.carbon.mediator.relaytransformer.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.Value;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.relaytransformer.RelayTransformerMediator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Properties;

/**
 * Factory for {@link org.wso2.carbon.mediator.relaytransformer.RelayTransformerMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;relayTransformer key="property-key" [input="xml|stream"] [source="xpath"] [target="string"]&gt;
 *   &lt;property name="string" (value="literal" | expression="xpath")/&gt;*
 *   &lt;feature name="string" value="true| false" /&gt;*
 *   &lt;attribute name="string" value="string" /&gt;*
 *   &lt;resource location="..." key="..."/&gt;*
 * &lt;/relayTransformer&gt;
 * </pre>
 */
public class RelayTransformerMediatorFactory extends AbstractMediatorFactory {
    private static final QName TAG_NAME
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "relayTransformer");
    private static final QName ATTRIBUTE_Q
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");
    private static final QName ATT_INPUT  = new QName("input");

    public QName getTagQName() {
        return TAG_NAME;
    }

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        RelayTransformerMediator transformMediator = new RelayTransformerMediator();

        OMAttribute attXslt   = elem.getAttribute(ATT_KEY);
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);
        OMAttribute attTarget = elem.getAttribute(ATT_TARGET);
        OMAttribute attInput = elem.getAttribute(ATT_INPUT);

        if (attXslt != null) {
            // ValueFactory for creating dynamic or static Value
            ValueFactory keyFac = new ValueFactory();
            // create dynamic or static key based on OMElement
            Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, elem);

            // set generated key as the Value
            transformMediator.setXsltKey(generatedKey);
        } else {
            handleException("The '" + XMLConfigConstants.KEY + "' " +
                    "attribute is required for the relayTransformer mediator");
        }

        if (attSource != null) {
            try {
                transformMediator.setSourceXPathString(attSource.getAttributeValue());
                transformMediator.setSource(SynapseXPathFactory.getSynapseXPath(elem, ATT_SOURCE));

            } catch (JaxenException e) {
                handleException("Invalid XPath specified for the source attribute : " +
                    attSource.getAttributeValue());
            }
        }

        if (attTarget != null) {
            transformMediator.setTargetPropertyName(attTarget.getAttributeValue());
        }

        if (attInput != null) {
            transformMediator.setInputType(attInput.getAttributeValue());
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(transformMediator, elem);
        // set the features
        for (Map.Entry<String,String> entry : collectNameValuePairs(elem, FEATURE_Q).entrySet()) {
            String value = entry.getValue();
            boolean isFeatureEnabled;
            if ("true".equals(value)) {
                isFeatureEnabled = true;
            } else if ("false".equals(value)) {
                isFeatureEnabled = false;
            } else {
                handleException("The feature must have value true or false");
                break;
            }
            transformMediator.addFeature(entry.getKey(), isFeatureEnabled);
        }
        for (Map.Entry<String,String> entry : collectNameValuePairs(elem, ATTRIBUTE_Q).entrySet()) {
            transformMediator.addAttribute(entry.getKey(), entry.getValue());
        }
        transformMediator.addAllProperties(
            MediatorPropertyFactory.getMediatorProperties(elem));

        transformMediator.setResourceMap(ResourceMapFactory.createResourceMap(elem));

        return transformMediator;
    }
}
