package org.wso2.carbon.hostobjects.file;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.*;
import org.wso2.carbon.hostobjects.web.RequestHostObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FileUploadHostObject extends ScriptableObject {

    public static final String HOSTOBJECT_NAME = "FileUpload";

    private HttpServletRequest request;
    private DiskFileItemFactory factory;
    private List<FileItem> fileItems;

    //Provide the HTTPRequest as an argument
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        FileUploadHostObject result = new FileUploadHostObject();

        if (!(args[0] == Context.getUndefinedValue())) {
            if (args[0] instanceof RequestHostObject) { /*logic for file upload case*/
                HttpServletRequest httpRequest = ((RequestHostObject) args[0]).getHttpServletRequest();
                if (ServletFileUpload.isMultipartContent(httpRequest)) {
                    result.request = httpRequest;
                    result.factory = new DiskFileItemFactory();
                    result.fileItems = new ArrayList<FileItem>();
                } else {
                    throw new ScriptException("Invalid form request. Request does not contain a file");
                }
                result.processFile(result);
            } else {
                throw new ScriptException("Invalid parameters.");
            }
        }
        return result;
    }

    @Override
    public String getClassName() {
        return HOSTOBJECT_NAME;
    }

    //Adding the fileItems to the object
    private void processFile(FileUploadHostObject result) throws ScriptException {
        ServletFileUpload upload = new ServletFileUpload(result.factory);
        try {
            List items = upload.parseRequest(result.request);
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    result.fileItems.add(item);
                }
            }
        } catch (FileUploadException e) {
            throw new ScriptException("File processing error : " + e.getMessage(), e);
        }
    }

    //Set tempDir
    public void jsSet_tmpDir(Object object) throws ScriptException {
        if (!(object instanceof String)) {
            throw new ScriptException("tmpDir should be provided as a dir path (string)");
        }

        String path = (String) object;
        //TODO we need to fix this once dir based deployment is done
        //String realPath = request.getServletContext().getRealPath(path);
        String realPath = path;
        File tmpDir = new File(realPath);
        if (!tmpDir.isDirectory()) {
            throw new ScriptException(path + " is not a directory");
        }

        factory.setRepository(tmpDir);
    }

    public void jsSet_sizeThreshold(Object object) throws ScriptException {
        if (!(object instanceof Integer)) {
            throw new ScriptException("sizeThreshold needs to be an Integer value");
        }
        Integer threshold = (Integer) object;
        factory.setSizeThreshold(threshold);
    }

    //Get all the fileItems in the request
    public static NativeArray jsFunction_getFileItems(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws ScriptException {
        FileUploadHostObject fupHo = checkInstance(thisObj);
        NativeArray fileArray = new NativeArray(0);

        Iterator iter = fupHo.fileItems.iterator();
        int count = 0;
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (!item.isFormField()) {
                fileArray.put(count, fileArray, item.getFieldName());
                count++;
            }
        }
        return fileArray;
    }

    //Save all the files or a file item(s)
    public static void jsFunction_save(Context cx, Scriptable thisObj, Object[] arguments,
                                Function funObj) throws ScriptException {
        FileUploadHostObject fupHo = checkInstance(thisObj);
        int argsCount = arguments.length;

        String destinationDirPath;

        if (argsCount == 0 || argsCount > 2) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, "Save", argsCount, true);
        }

        if (!(arguments[0] instanceof String)) {
            throw new ScriptException("destinationDir should be provided as a dir path (string)");
        }

        destinationDirPath = FilenameUtils.normalizeNoEndSeparator((String) arguments[0]);
        //TODO we need to fix this once dir based deployment is done
        //String realPath = request.getServletContext().getRealPath(path);
        File destinationDir = new File(destinationDirPath);
        if (!destinationDir.isDirectory()) {
            throw new ScriptException(destinationDirPath + " is not a directory");
        }

        if (argsCount == 2) {
            if (!(arguments[1] instanceof NativeArray)) {
                throw new ScriptException("fileItems are not provided as an Array");
            }
            NativeArray fields = (NativeArray) arguments[1];

            String[] fieldsArr = new String[(int) fields.getLength()];
            for (Object o : fields.getIds()) {
                int index = (Integer) o;
                fieldsArr[index] = (String) fields.get(index, null);
            }

            List<FileItem> fileItemsToSave = new ArrayList<FileItem>();

            for (FileItem itm : fupHo.fileItems) {
                for (String s : fieldsArr) {
                    if (itm.getFieldName().equals(s)) {
                        fileItemsToSave.add(itm);
                    }
                }
            }

            for (FileItem i : fileItemsToSave) {
                File f = new File(destinationDirPath + File.separator + i.getName());
                try {
                    i.write(f);
                } catch (Exception e) {
                    throw new ScriptException("file save failed :" + e.getMessage(), e);
                }
            }

        } else if (argsCount == 1) {
            for (FileItem i : fupHo.fileItems) {
                File f = new File(destinationDirPath + File.separator + i.getName());
                try {
                    i.write(f);
                } catch (Exception e) {
                    throw new ScriptException("file save failed :" + e.getMessage(), e);
                }
            }
        }

    }

    private static FileUploadHostObject checkInstance(Scriptable obj) {
        if (obj == null || !(obj instanceof FileUploadHostObject)) {
            throw Context.reportRuntimeError("called on incompatible object");
        }
        return (FileUploadHostObject) obj;
    }

}
