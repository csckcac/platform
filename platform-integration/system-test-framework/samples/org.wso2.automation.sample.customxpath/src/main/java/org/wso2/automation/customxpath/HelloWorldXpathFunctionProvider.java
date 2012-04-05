package org.wso2.automation.customxpath;

import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.jaxen.Function;
import javax.xml.namespace.QName;

public class HelloWorldXpathFunctionProvider implements SynapseXpathFunctionContextProvider {

    public Function getInitializedExtFunction(MessageContext messageContext) {
        CustomHelloWorldFunction helloWorldFunction = new CustomHelloWorldFunction(messageContext);
        return helloWorldFunction;
    }

    public QName getResolvingQName() {
        //letting know synapse new hello-world extension function
        return new QName("hello-world");
    }
}
