package org.wso2.carbon.jaggery.core.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hostobjects.file.JavaScriptFile;
import org.wso2.carbon.jaggery.core.manager.WebAppManager;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.activation.FileTypeMap;
import javax.servlet.ServletContext;
import java.io.*;

public class WebAppFile implements JavaScriptFile {

    private static final Log log = LogFactory.getLog(WebAppFile.class);

    private ServletContext context = null;
    private BufferedReader bufferedReader = null;
    private String path = null;

    private boolean opened = false;
    private boolean readable = false;

    public WebAppFile(String path, ServletContext context) {
        this.path = path.startsWith("/") ? path.substring(1) : path;
        this.context = context;
    }

    @Override
    public void construct() throws ScriptException {

    }

    @Override
    public void open(String mode) throws ScriptException {
        if ("r".equals(mode)) {
            InputStream inputStream = context.getResourceAsStream(path);
            if (inputStream == null) {
                String msg = "Unable to read the content of file : " + path;
                log.error(msg);
                throw new ScriptException(msg);
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            readable = true;
        } else {
            String msg = "Invalid or unsupported file mode, path : " + path + ", mode : " + mode;
            log.error(msg);
            throw new ScriptException(msg);
        }
        opened = true;
    }

    @Override
    public void close() throws ScriptException {
        if (!opened) {
            return;
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public String readLine() throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if (!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void writeLine(String data) throws ScriptException {
        log.warn("Writing is not implemented for webapp resources");
    }

    @Override
    public String read(long count) throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if (!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        try {
            StringBuffer buffer = new StringBuffer();
            int ch = bufferedReader.read();
            for (long i = 0; (i < count) && (ch != -1); i++) {
                buffer.append((char) ch);
                ch = bufferedReader.read();
            }
            return buffer.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void write(String data) throws ScriptException {
        log.warn("Writing is not implemented for webapp resources");
    }

    @Override
    public String readAll() throws ScriptException {
        if(!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if(!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        return HostObjectUtil.streamToString(context.getResourceAsStream(path));
    }

    @Override
    public boolean move(String data) throws ScriptException {
        log.warn("Moving is not implemented for webapp resources");
        return false;
    }

    @Override
    public boolean del() throws ScriptException {
        log.warn("Deleting is not implemented for webapp resources");
        return false;
    }

    @Override
    public long getLength() throws ScriptException {
        InputStream in = context.getResourceAsStream(path);
        long length = 0L;
        if (in == null) {
            return length;
        }
        try {
            int ch = in.read();
            while (ch != -1) {
                length++;
                ch = in.read();
            }
            return length;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public long getLastModified() throws ScriptException {
        return WebAppManager.getScriptLastModified(context, path);
    }

    @Override
    public String getName() throws ScriptException {
        int index = path.lastIndexOf(File.separator);
        return index < path.length() ? path.substring(index + 1) : null;
    }

    @Override
    public boolean isExist() throws ScriptException {
        return context.getResourceAsStream(path) != null;
    }

    @Override
    public InputStream getInputStream() throws ScriptException {
        return context.getResourceAsStream(path);
    }

    @Override
    public OutputStream getOutputStream() throws ScriptException {
       return null;
    }

    @Override
    public String getContentType() throws ScriptException {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(getName());
    }
}
