package org.wso2.automation.customxpath;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.ext.SynapseXpathVariableResolver;

import javax.xml.namespace.QName;

public class HelloWorldXpathVariableResolver implements SynapseXpathVariableResolver {

    public static final QName HELLO_WORLD = new QName("HELLO_WORLD");

    public Object resolve(MessageContext messageContext) {
        //create Hello world message
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

        return helloWorldElement;
    }

    public QName getResolvingQName() {
        //to support xpath expression="$HELLO_WORLD"
        return HELLO_WORLD;
    }

}