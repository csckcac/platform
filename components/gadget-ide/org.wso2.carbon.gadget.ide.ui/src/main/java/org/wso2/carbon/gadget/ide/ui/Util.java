package org.wso2.carbon.gadget.ide.ui;


import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

public class Util {
    public static String toJson(String[] strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            builder.append("\"");
            builder.append(String.valueOf(string));
            builder.append("\"");
            if (i != strings.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static String generateHtmlFromOperationSig(String sig) throws TransformerException {
        return transform(sig, "xslt/operation_to_html.xslt", null);

    }

    public static String generateResponseXMLFromSig(String sig) throws TransformerException {
        return transform(sig, "xslt/operation_to_response.xslt", null);
    }

    public static String transform(String xmlIn, String xslResourcePath, Map paramMap) throws TransformerException {
        InputStream xsltInput = Util.class.getResourceAsStream(xslResourcePath);
        Source xslt = new StreamSource(xsltInput);
        Source xml = new StreamSource(new StringReader(xmlIn));
        StringWriter writer = new StringWriter();
        Result out = new StreamResult(writer);
        transform(xml, xslt, out, paramMap);
        return writer.getBuffer().toString();
    }

    public static void transform(Source xmlIn, Source xslIn, Result result, Map paramMap)
            throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setURIResolver(new XSLTURIResolver());
        Transformer transformer = transformerFactory.newTransformer(xslIn);
        if (paramMap != null) {
            Set set = paramMap.keySet();
            for (Object aSet : set) {
                if (aSet != null) {
                    String key = (String) aSet;
                    String value = (String) paramMap.get(key);
                    transformer.setParameter(key, value);
                }
            }
        }
        transformer.transform(xmlIn, result);
    }

    static class XSLTURIResolver implements URIResolver {
        public Source resolve(String href, String base) {
            InputStream is = Util.class.getResourceAsStream("xslt/" + href);
            return new StreamSource(is);
        }
    }
}
