package org.wso2.carbon.mediator.relaytransformer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.jaxp.*;
import org.apache.synapse.util.resolver.CustomJAXPURIResolver;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.util.xpath.SourceXPathSupport;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class RelayTransformerMediator extends AbstractMediator {
    /**
     * The feature for which deciding switching between DOM and Stream during the
     * transformation process
     */
    public static final String USE_DOM_SOURCE_AND_RESULTS =
            "http://ws.apache.org/ns/synapse/transform/feature/dom";

    /**
     * Name of the attribute to specifying streaming
     */
    public static final String STREAM = "stream";

    /**
     * The type of the resource that will be passed into the XSLT transformation. This either
     * can be a XML or a Stream. By default setting it to xml.
     */
    private String inputType = "xml";

    /**
     * QName for binary content
     */
    public static final QName BINARY_CONTENT_QNAME =
            new QName("http://ws.apache.org/commons/ns/payload", "binary");


    /**
     * The name of the attribute that allows to specify the {@link org.apache.synapse.util.jaxp.SourceBuilderFactory}.
     */
    public static final String SOURCE_BUILDER_FACTORY =
            "http://ws.apache.org/ns/synapse/transform/attribute/sbf";

    /**
     * The name of the attribute that allows to specify the {@link org.apache.synapse.util.jaxp.ResultBuilderFactory}.
     */
    public static final String RESULT_BUILDER_FACTORY =
            "http://ws.apache.org/ns/synapse/transform/attribute/rbf";

    /**
     * The resource key which refers to the XSLT to be used for the transformation
     * supports both static and dynamic(xpath) keys
     */
    private Value xsltKey = null;

    /**
     * The (optional) XPath expression which yields the source element for a transformation
     */
    private final SourceXPathSupport source = new SourceXPathSupport();

    /**
     * The name of the message context property to store the transformation result
     */
    private String targetPropertyName = null;

    /**
     * Any parameters which should be passed into the XSLT transformation
     */
    private final List<MediatorProperty> properties = new ArrayList<MediatorProperty>();

    /**
     * Any features which should be set to the TransformerFactory explicitly
     */
    private final List<MediatorProperty> transformerFactoryFeatures = new ArrayList<MediatorProperty>();

    /**
     * Any attributes which should be set to the TransformerFactory explicitly
     */
    private final List<MediatorProperty> transformerFactoryAttributes
            = new ArrayList<MediatorProperty>();

    /**
     * A resource map used to resolve xsl:import and xsl:include.
     */
    private ResourceMap resourceMap;

    /**
     * Cache multiple templates
     * Unique string used as a key for each template
     * The Template instance used to create a Transformer object. This is  thread-safe
     */
    private Map<String, Templates> cachedTemplatesMap = new Hashtable<String, Templates>();

    /**
     * The TransformerFactory instance which use to create Templates. This is not thread-safe.
     * @see javax.xml.transform.TransformerFactory
     */
    private final TransformerFactory transFact = TransformerFactory.newInstance();

    /**
     * Lock used to ensure thread-safe creation and use of the above Transformer
     */
    private final Object transformerLock = new Object();

    /**
     * The source builder factory to use.
     */
    private SourceBuilderFactory sourceBuilderFactory = new StreamSourceBuilderFactory();

    /**
     * The result builder factory to use.
     */
    private ResultBuilderFactory resultBuilderFactory = new StreamResultBuilderFactory();

    /**
     * Transforms this message (or its element specified as the source) using the
     * given XSLT transformation
     *
     * @param synCtx the current message where the transformation will apply
     * @return true always
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        synLog.traceOrDebug("Start : Relay-Transformer mediator");
        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Message : " + synCtx.getEnvelope());
        }

        try {
            performXSLT(synCtx, synLog);

        } catch (Exception e) {
            handleException("Unable to perform XSLT transformation using : " + xsltKey +
                    " against source XPath : " + source, e, synCtx);

        }

        synLog.traceOrDebug("End : Relay-Transformer mediator");

        return true;
    }

    /**
     * Perform actual XSLT transformation
     * @param synCtx current message
     * @param synLog the logger to be used
     */
    private void performXSLT(MessageContext synCtx, SynapseLog synLog) {

        OMNode sourceNode = source.selectOMNode(synCtx, synLog);
        boolean isSoapEnvelope = (sourceNode == synCtx.getEnvelope());
        boolean isSoapBody = (sourceNode == synCtx.getEnvelope().getBody());

        // Derive actual key from message context
        String generatedXsltKey = xsltKey.evaluateValue(synCtx);

        // get templates from generatedXsltKey
        Templates cachedTemplates = null;

        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Transformation source : " + sourceNode.toString());
        }

        OMElement contentEle = synCtx.getEnvelope().getBody().
                getFirstChildWithName(BINARY_CONTENT_QNAME);
        InputStream in = null;
        // If the xslt is processed as a stream
        if (contentEle != null) {
            in = getInputStream(synCtx, synLog, contentEle);
        }

        // determine if it is needed to create or create the template
        if (isCreationOrRecreationRequired(synCtx)) {
            // many threads can see this and come here for acquiring the lock
            synchronized (transformerLock) {
                // only first thread should create the template
                if (isCreationOrRecreationRequired(synCtx)) {
                    cachedTemplates = createTemplate(synCtx, synLog, generatedXsltKey);
                }
            }
        }
        else{
            //If already cached template then load it from cachedTemplatesMap
            synchronized (transformerLock){
                cachedTemplates = cachedTemplatesMap.get(generatedXsltKey);
            }
        }

        try {
            // perform transformation
            Transformer transformer = null;
            try {
                transformer = cachedTemplates.newTransformer();
            } catch (NullPointerException ex) {
                handleException("Unable to create Transformer using cached template", ex, synCtx);
            }
            if (!properties.isEmpty()) {
                // set the parameters which will pass to the Transformation
                applyProperties(transformer, synCtx, synLog);
            }

            // transformer.setErrorListener(new ErrorListenerImpl(synLog, "XSLT transformation"));

            String outputMethod = transformer.getOutputProperty(OutputKeys.METHOD);
            String encoding = transformer.getOutputProperty(OutputKeys.ENCODING);

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("output method: " + outputMethod
                        + "; encoding: " + encoding);
            }

            ResultBuilderFactory.Output output;
            if ("text".equals(outputMethod)) {
                synLog.traceOrDebug("Processing non SOAP/XML (text) transformation result");
                output = ResultBuilderFactory.Output.TEXT;
            } else if (isSoapEnvelope) {
                output = ResultBuilderFactory.Output.SOAP_ENVELOPE;
            } else {
                output = ResultBuilderFactory.Output.ELEMENT;
            }

            SynapseEnvironment synEnv = synCtx.getEnvironment();
            ResultBuilder resultBuilder =
                    resultBuilderFactory.createResultBuilder(synEnv, output);
            SourceBuilder sourceBuilder = sourceBuilderFactory.createSourceBuilder(synEnv);

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Using " + sourceBuilder.getClass().getName());
                synLog.traceOrDebug("Using " + resultBuilder.getClass().getName());
            }

            if (in != null && inputType.equalsIgnoreCase(STREAM)) {
                try {
                    transformer.transform(new StreamSource(in), resultBuilder.getResult());
                } catch (Exception e) {
                    handleException("Unable transform the XML stream", e, synCtx);
                }
            }
            if (inputType.equalsIgnoreCase("xml")) {
                try {
                    transformer.transform(sourceBuilder.getSource((OMElement)sourceNode),
                            resultBuilder.getResult());
                } finally {
                    sourceBuilder.release();
                }
            }

            synLog.traceOrDebug("Transformation completed - processing result");

            // get the result OMElement
            OMElement result =
                    resultBuilder.getNode(encoding == null ? null : Charset.forName(encoding));

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Transformation result : " + result.toString());
            }

            if (targetPropertyName != null) {
                // add result XML as a message context property to the message
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Adding result as message context property : " +
                            targetPropertyName);
                }
                synCtx.setProperty(targetPropertyName, result);
            } else {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Replace " +
                            (isSoapEnvelope ? "SOAP envelope" : isSoapBody ? "SOAP body" : "node")
                            + " with result");
                }

                if (isSoapEnvelope) {
                     try {
                        if (in != null && inputType.equalsIgnoreCase(STREAM)) {
                            // Special case handling for streams
                            Object contentType = ((Axis2MessageContext) synCtx).
                                    getAxis2MessageContext().
                                    getProperty(Constants.Configuration.CONTENT_TYPE);
                            SOAPEnvelope envelope = null;
                            if ((contentType != null) && (contentType.toString().
                                    equalsIgnoreCase("text/xml; charset=UTF-8"))) {
                                envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
                            } else if ((contentType != null) && (contentType.toString().
                                    equalsIgnoreCase("application/soap+xml; charset=UTF-8"))) {
                                envelope = OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
                            }
                            envelope.getBody().addChild(result);
                            synCtx.setEnvelope(envelope);
                        } else {

                            synCtx.setEnvelope((SOAPEnvelope) result);
                        }
                    } catch (AxisFault ex) {
                        handleException("Unable to replace SOAP envelope with result", ex, synCtx);
                    }

                } else if (isSoapBody) {
                    for (Iterator itr = synCtx.getEnvelope().getBody().getChildElements();
                         itr.hasNext(); ) {
                        OMElement child = (OMElement) itr.next();
                        child.detach();
                    }

                    for (Iterator itr = result.getChildElements(); itr.hasNext(); ) {
                        OMElement child = (OMElement) itr.next();
                        synCtx.getEnvelope().getBody().addChild(child);
                    }

                } else {
                    if (inputType.equalsIgnoreCase("xml")) {
                        sourceNode.insertSiblingAfter(result);
                        sourceNode.detach();


                    } else {
                        Object contentType = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                                getProperty(Constants.Configuration.CONTENT_TYPE);
                        SOAPEnvelope envelope = null;
                        if ((contentType != null) && (contentType.toString().
                                equalsIgnoreCase("text/xml; charset=UTF-8"))) {
                            envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
                        } else if ((contentType != null) && (contentType.toString().
                                equalsIgnoreCase("application/soap+xml; charset=UTF-8"))) {
                            envelope = OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
                        }

                        envelope.getBody().addChild(result);
                        try {
                            synCtx.setEnvelope(envelope);
                        } catch (AxisFault axisFault) {
                            handleException("Unable to replace SOAP envelope with result",
                                    axisFault, synCtx);
                        }

                    }
                }
            }

        } catch (TransformerException e) {
            handleException("Error performing XSLT transformation using : " + xsltKey, e, synCtx);
        }
    }

    /**
     * Create a XSLT template object and assign it to the cachedTemplates variable
     * @param synCtx current message
     * @param synLog logger to use
     * @param generatedXsltKey evaluated xslt key(real key value) for dynamic or static key
     * @return cached template
     */
    private Templates createTemplate(MessageContext synCtx, SynapseLog synLog,
                                     String generatedXsltKey) {
        // Assign created template
        Templates cachedTemplates = null;

        // Allow xsl:import and xsl:include resolution
        transFact.setURIResolver(new CustomJAXPURIResolver(resourceMap,
                synCtx.getConfiguration()));

        try {
            cachedTemplates = transFact.newTemplates(
                    SynapseConfigUtils.getStreamSource(synCtx.getEntry(generatedXsltKey)));
            if (cachedTemplates == null) {
                // if cached template creation failed
                handleException("Error compiling the XSLT with key : " + xsltKey, synCtx);
            } else {
                // if cached template is created then put it in to cachedTemplatesMap
                cachedTemplatesMap.put(generatedXsltKey, cachedTemplates);
            }
        } catch (Exception e) {
            handleException("Error creating XSLT transformer using : " + xsltKey, e, synCtx);
        }
        return cachedTemplates;
    }

    /**
     * Utility method to determine weather it is needed to create a XSLT template
     *
     * @param synCtx current message
     * @return true if it is needed to create a new XSLT template
     */
    private boolean isCreationOrRecreationRequired(MessageContext synCtx) {

        // Derive actual key from message context
        String generatedXsltKey = xsltKey.evaluateValue(synCtx);

        // if there are no cachedTemplates inside cachedTemplatesMap or
        // if the template related to this generated key is not cached
        // then it need to be cached
        if (cachedTemplatesMap.isEmpty() || !cachedTemplatesMap.containsKey(generatedXsltKey)) {
            // this is a creation case
            return true;
        } else {
            // build transformer - if necessary
            Entry dp = synCtx.getConfiguration().getEntryDefinition(generatedXsltKey);
            // if the xsltKey refers to a dynamic resource, and if it has been expired
            // it is a recreation case
            return dp != null && dp.isDynamic() && (!dp.isCached() || dp.isExpired());
        }
    }

    public SynapseXPath getSource() {
        return source.getXPath();
    }

    public void setSource(SynapseXPath source) {
        this.source.setXPath(source);
    }

    public Value getXsltKey() {
        return xsltKey;
    }

    public void setXsltKey(Value xsltKey) {
        this.xsltKey = xsltKey;
    }

    public void addProperty(MediatorProperty p) {
        properties.add(p);
    }

    /**
     * Returns the incoming input stream
     *
     * @param synCtx SynapseContext
     * @param synLog SynapseLog
     * @param contentEle content element
     * @return InputStream
     */
    private InputStream getInputStream(MessageContext synCtx, SynapseLog synLog,
                                       OMElement contentEle) {
        OMNode node = contentEle.getFirstOMChild();

        if (node != null && (node instanceof OMText)) {
            OMText binaryDataNode = (OMText) node;
            DataHandler dh = (DataHandler) binaryDataNode.getDataHandler();

            if (dh == null) {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.auditWarn("Message has the Binary content element. " +
                            "But doesn't have binary content embedded within it");
                }
                return null;
            }

            //DataSource dataSource = dh.getDataSource();

            // TODO: check whether this is necessary
            //Ask the data source to stream, if it has not alredy cached the request
            /*if (dataSource instanceof StreamingOnRequestDataSource) {
                ((StreamingOnRequestDataSource) dataSource).setLastUse(true);
            }*/

            try {
                return dh.getInputStream();
            } catch (IOException e) {
                handleException("Error retrieving InputStream from data handler", e, synCtx);
            }
        }

        return null;
    }


    /**
     * Set the properties defined in the mediator as parameters on the stylesheet.
     *
     * @param transformer Transformer instance
     * @param synCtx MessageContext instance
     * @param synLog SynapseLog instance
     */
    private void applyProperties(Transformer transformer, MessageContext synCtx,
                                 SynapseLog synLog) {
        for (MediatorProperty prop : properties) {
            if (prop != null) {
                String value;
                if (prop.getValue() != null) {
                    value = prop.getValue();
                } else {
                    value = prop.getExpression().stringValueOf(synCtx);
                }
                if (synLog.isTraceOrDebugEnabled()) {
                    if (value == null) {
                        synLog.traceOrDebug("Not setting parameter '" + prop.getName() + "'");
                    } else {
                        synLog.traceOrDebug("Setting parameter '" + prop.getName() + "' to '"
                                + value + "'");
                    }
                }
                if (value != null) {
                    transformer.setParameter(prop.getName(), value);
                }
            }
        }
    }

    /**
     * Add a feature to be set on the {@link TransformerFactory} used by this mediator instance.
     * This method can also be used to enable some Synapse specific optimizations and
     * enhancements as described in the documentation of this class.
     *
     * @param featureName The name of the feature
     * @param isFeatureEnable the desired state of the feature
     *
     * @see TransformerFactory#setFeature(String, boolean)
     * @see RelayTransformerMediator
     */
    public void addFeature(String featureName, boolean isFeatureEnable) {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(featureName);
        if (isFeatureEnable) {
            mp.setValue("true");
        } else {
            mp.setValue("false");
        }
        transformerFactoryFeatures.add(mp);
        if (USE_DOM_SOURCE_AND_RESULTS.equals(featureName)) {
            if (isFeatureEnable) {
                sourceBuilderFactory = new DOOMSourceBuilderFactory();
                resultBuilderFactory = new DOOMResultBuilderFactory();
            }
        } else {
            try {
                transFact.setFeature(featureName, isFeatureEnable);
            } catch (TransformerConfigurationException e) {
                String msg = "Error occurred when setting features to the TransformerFactory";
                log.error(msg, e);
                throw new SynapseException(msg, e);
            }
        }
    }

    /**
     * Add an attribute to be set on the {@link TransformerFactory} used by this mediator instance.
     * This method can also be used to enable some Synapse specific optimizations and
     * enhancements as described in the documentation of this class.
     *
     * @param name The name of the feature
     * @param value should this feature enable?
     *
     * @see TransformerFactory#setAttribute(String, Object)
     * @see RelayTransformerMediator
     */
    public void addAttribute(String name, String value) {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(name);
        mp.setValue(value);
        transformerFactoryAttributes.add(mp);
        if (SOURCE_BUILDER_FACTORY.equals(name) || RESULT_BUILDER_FACTORY.equals(name)) {
            Object instance;
            try {
                instance = Class.forName(value).newInstance();
            } catch (ClassNotFoundException e) {
                String msg = "The class specified by the " + name + " attribute was not found";
                log.error(msg, e);
                throw new SynapseException(msg, e);
            } catch (Exception e) {
                String msg = "The class " + value + " could not be instantiated";
                log.error(msg, e);
                throw new SynapseException(msg, e);
            }
            if (SOURCE_BUILDER_FACTORY.equals(name)) {
                sourceBuilderFactory = (SourceBuilderFactory)instance;
            } else {
                resultBuilderFactory = (ResultBuilderFactory)instance;
            }
        } else {
            try {
                transFact.setAttribute(name, value);
            } catch (IllegalArgumentException e) {
                String msg = "Error occurred when setting attribute to the TransformerFactory";
                log.error(msg, e);
                throw new SynapseException(msg, e);
            }
        }
    }

    /**
     * @return Return the features explicitly set to the TransformerFactory through this mediator.
     */
    public List<MediatorProperty> getFeatures(){
        return transformerFactoryFeatures;
    }

    /**
     * @return Return the attributes explicitly set to the TransformerFactory through this mediator.
     */
    public List<MediatorProperty> getAttributes(){
        return transformerFactoryAttributes;
    }

    public void addAllProperties(List<MediatorProperty> list) {
        properties.addAll(list);
    }

    public List<MediatorProperty> getProperties() {
        return properties;
    }

    public void setSourceXPathString(String sourceXPathString) {
        this.source.setXPathString(sourceXPathString);
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public void setTargetPropertyName(String targetPropertyName) {
        this.targetPropertyName = targetPropertyName;
    }

    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getInputType() {
        return inputType;
    }

    public void setResourceMap(ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
    }

}
