package org.wso2.carbon.mashup.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class MashupReader extends Reader {

    protected AxisService service = null;
    protected StringReader sourceReader = null;
    private boolean isBuilt = false;

    public MashupReader(AxisService service) {
        this.service = service;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (!isBuilt) {
            build();
            isBuilt = true;
        }
        return sourceReader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        sourceReader.close();
    }

    protected void build() throws IOException {
        InputStream jsFileStream = null;
        Parameter implInfoParam = service.getParameter(MashupConstants.SERVICE_JS);
        if (implInfoParam == null) {
            throw new AxisFault("Parameter 'ServiceJS' not specified");
        }
        Object value = implInfoParam.getValue();
        //We are reading the stream from the axis2 parameter
        jsFileStream = new ByteArrayInputStream((
                (String) service.getParameter(MashupConstants.SERVICE_JS_STREAM).getValue()).getBytes());
        //We are reading the stream from the axis2 parameter
        try {
            sourceReader = new StringReader(HostObjectUtil.streamToString(jsFileStream));
        } catch (ScriptException e) {
            throw new IOException(e);
        }
    }
}
