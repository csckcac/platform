package org.wso2.automation.customxpath;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.function.StringFunction;

import javax.xml.namespace.QName;
import java.util.List;


public class CustomHelloWorldFunction implements Function {
    private MessageContext synCtx;

    private Log log = LogFactory.getLog(CustomHelloWorldFunction.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    private String NULL_STRING = "";
    public static final QName HELLO_WORLD = new QName("HELLO_WORLD");
    private OMElement helloWorld;

    public CustomHelloWorldFunction(MessageContext ctxt) {
        this.synCtx = ctxt;
        //create hello world message
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement helloWorldElement = factory.createOMElement(HELLO_WORLD);

        OMElement nameElement = factory.createOMElement(new QName("name"));
        nameElement.setText("synapse");
        OMElement versionElem = factory.createOMElement(new QName("version"));
        versionElem.setText("3.1.0");
        OMElement releaseDateElem = factory.createOMElement(new QName("release_date"));
        releaseDateElem.setText("12/12/2010");

        helloWorldElement.addChild(nameElement);
        helloWorldElement.addChild(versionElem);
        helloWorldElement.addChild(releaseDateElem);
        this.helloWorld = helloWorldElement;
    }

    /**
     * Returns the string value of the hello world message for the corresponding child
     * proeprty
     * arguments are hello-world(name | version | release_date)
     *
     * @param context the context at the point in the expression when the function is called
     * @param args    arguments of the functions
     * @return The string value of the property
     * @throws org.jaxen.FunctionCallException
     *
     */
    public Object call(Context context, List args) throws FunctionCallException {

        if (synCtx == null) {
            if (log.isDebugEnabled()) {
                log.debug("Synapse message context has not been set for the " +
                          "XPath extension function 'synapse:hello-world(arg_name)'");
            }
            return null;
        }

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (args == null || args.size() == 0) {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn, "argument key value for lookup is not specified");
            }
            return NULL_STRING;
        } else {
            int size = args.size();
            if (size == 1) {
                String argument = StringFunction.evaluate(args.get(0), context.getNavigator());
                if ("name".equals(argument) || "version".equals(argument) ||
                    "release_date".equals(argument)) {
                    return helloWorld.getFirstChildWithName(new QName(argument));
                } else {
                    return helloWorld;
                }
            }
        }
        return NULL_STRING;
    }

    private void traceOrDebug(boolean traceOn, String msg) {
        if (traceOn) {
            trace.info(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

}
