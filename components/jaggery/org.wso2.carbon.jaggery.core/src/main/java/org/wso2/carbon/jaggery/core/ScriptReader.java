package org.wso2.carbon.jaggery.core;

import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class ScriptReader extends Reader {

    private InputStream sourceIn = null;
    private StringReader sourceReader = null;
    private boolean isBuilt = false;

    public ScriptReader(InputStream sourceIn) {
        this.sourceIn = sourceIn;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if(!isBuilt) {
            build();
            isBuilt = true;
        }
        return sourceReader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        sourceReader.close();
    }

    private void build() throws IOException {
        try {
            sourceReader = new StringReader(ScriptParser.parse(sourceIn));
        } catch (ScriptException e) {
            throw new IOException(e);
        }
    }
}
