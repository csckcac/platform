package org.wso2.carbon.gadget.ide.sigdom;

import org.apache.axis2.description.AxisService;
import org.wso2.carbon.wsdl2form.Util;

import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class HTTPDomProvider implements DomProvider {

    public DOMSource getSigDom(String uri) {
        try {
            URL url = new URL(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // This is memory consuming. Better change getSigStream to accept an Input Stream.
            InputStream is = url.openStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, nRead);
            }
             //this was added since the wsdl to form trunk version doesn't have the intended method
            AxisService service = new AxisService(uri);

            /*actual return should be,
              return Util.getSigStream(baos, null, null);
            */
            return Util.getSigStream(service, null, null);
        } catch (Exception e) {
            return null;
        }
    }
}
