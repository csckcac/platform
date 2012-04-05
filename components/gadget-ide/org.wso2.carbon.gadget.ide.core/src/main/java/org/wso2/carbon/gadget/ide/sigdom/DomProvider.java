package org.wso2.carbon.gadget.ide.sigdom;

import javax.xml.transform.dom.DOMSource;

public interface DomProvider {

    /**
     * given a URI of a WSDL, providers the Signature Model as a DOMSource.
     * @param uri URI to the WSDL.
     * @return sig model. null if not available.
     */
    public DOMSource getSigDom(String uri);
}
