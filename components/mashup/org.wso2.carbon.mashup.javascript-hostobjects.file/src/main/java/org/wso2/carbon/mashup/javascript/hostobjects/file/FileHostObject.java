/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mashup.javascript.hostobjects.file;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axis2.description.AxisService;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.javascript.xmlimpl.XML;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.CarbonException;

import javax.activation.DataHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * <p/>
 * This is a JavaScript Rhino host object to provide the ability for the users
 * to manipulate with Files inside the WSO2 Mashup environment.
 * </p>
 * <p/>
 * For more information refer to <a
 * href="http://www.wso2.org/wiki/display/mashup/File+Host+Object">JavaScript
 * File Host Object</a>.
 */
public class FileHostObject extends ScriptableObject {
    File file;

    private DataHandler dataHandler;

    private BufferedWriter writer;

    private BufferedReader reader;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {
        FileHostObject result = new FileHostObject();

        // Falling back to use AxisService as MessageContext is not available in deployment time.
        // This is a fix for MASHUP-209
        Object object = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);
        AxisService axisService;
        if (object instanceof AxisService) {
            axisService = (AxisService) object;
        } else {
            throw new CarbonException("Error obtaining the AxisService.");
        }

        Object resourceFileObject = axisService
                .getParameterValue(MashupConstants.RESOURCES_FOLDER);
        File resourceFolder;
        if (resourceFileObject != null && resourceFileObject instanceof File) {
            resourceFolder = (File) resourceFileObject;
        } else {
            throw new CarbonException("Resources folder not found.");
        }

        if (args.length == 1 & !(args[0] == Context.getUndefinedValue())) {
            if (args[0] instanceof String) {
                String filePath = FilenameUtils.normalizeNoEndSeparator((String) args[0]);
                if (filePath == null) {
                    throw new CarbonException(
                            "FileHostObject : Illegal file path, Cannot navigate away from resources directory");
                }
                result.file = new File(resourceFolder, filePath);
                if (result.file.isDirectory()) {
                    throw new CarbonException(
                            "Given path is not a File. This object does not support directories.");
                }
            } else if (args[0] instanceof XML) {
                //TODO: Initial code bits to handle MTOM using the file object. Yet to be completed.
                XML xml = (XML) args[0];
                OMNode node = xml.getAxiomFromXML();
                if (node instanceof OMText) {
                    OMText textNode = (OMText) node;
                    if (textNode.isBinary()) {
                        result.dataHandler = (DataHandler) textNode.getDataHandler();
                    } else {
                        // TODO support directly writing XML to the file
                        throw new CarbonException(
                                "XML content given for the File does not contain base64Binary.");
                    }

                } else {
                    // TODO support directly writing XML to the file
                    throw new CarbonException(
                            "XML content given for the File does not contain base64Binary.");
                }
            } else {
                throw new CarbonException("Invalid parameters.");
            }
        }
        return result;
    }

    public String getClassName() {
        return "File";
    }

    /**
     * <p>Open the file for reading.</p>
     *
     * @throws IOException if the file is already open for either appending or reading, if the file does not exist, if the file cannot be opened for any other file system specific reason.
     */
    public void jsFunction_openForReading() throws CarbonException {
        if (writer != null)
            throw new CarbonException(
                    "Cannot read from the already writing file. Please close the file beforehand by calling close().");
        if (reader == null) {
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new CarbonException(e);
            }
        }
    }

    /**
     * <p>Open the file for writing. Using write() or writeLine() on a file which is open for writing will
     * write those content to the beginning of the file overwriting the content of the file.</p>
     * <p>If the file does not exist, this will create the file.</p>
     *
     * @throws IOException if the file is already open for either appending or reading, if the file cannot be opened for any other file system specific reason.
     */
    public void jsFunction_openForWriting() throws CarbonException {
        getWriter(false);
    }

    /**
     * <p>Open the file for appending. Using write() or writeLine() on a file which is open for appending will
     * write those content to the end of the file rather than the beginning.</p>
     * <p>If the file does not exist, this will create the file.</p>
     *
     * @throws IOException if the file is already open for either writing or reading, if the file cannot be opened for any other file system specific reason.
     */
    public void jsFunction_openForAppending() throws CarbonException {
        getWriter(true);
    }

    /**
     * <p>Writes the String representation of the object to the file. Users are required to open the file
     * for writing or for appending before writing to the file. </p>
     * <p>If the file is not open for either reading or writing or appending,
     * then calling this will automatically opens the file for writing(Overwrites the current content of the file).</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForWriting();
     *    //file.openForAppending();
     *    file.write("Hello World!");
     *    file.close();
     * </pre>
     *
     * @param object
     * @throws IOException
     */
    public void jsFunction_write(Object object) throws CarbonException {
        if (writer == null && reader == null) {
            getWriter(false);
        }
        if (writer != null) {
            try {
                writer.write(Context.toString(object));
                writer.flush();
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        } else {
            throw new CarbonException(
                    "File not open for writing. Please call openFileFor{Writing|Appending}() accordingly beforehand. ");
        }
    }

    /**
     * <p>Writes the String representation of the object to the file together with a line separator at the end. Users are required to open the file
     * for writing or for appending before writing to the file. </p>
     * <p>If the file is not open for either reading or writing or appending,
     * then calling this will automatically opens the file for writing(Overwrites the current content of the file).</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForWriting();
     *    //file.openForAppending();
     *    file.writeLine("Hello World!");
     *    file.close();
     * </pre>
     *
     * @param object
     * @throws IOException
     */
    public void jsFunction_writeLine(Object object) throws CarbonException {
        jsFunction_write(object);
        try {
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new CarbonException(e);
        }
    }

    /**
     * <p>Reads the given number of characters from the file and return a string representation of those characters. Users are required to open the file
     * for reading before reading from the file. </p>
     * <p>If the file is not open for either reading or writing or appending,
     * then calling this will automatically opens the file for reading.</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForReading();
     *    var x = file.read(5);
     *    print(x);
     *    file.close();
     * </pre>
     *
     * @param noOfCharacters of characters to be read
     * @throws IOException
     */
    public String jsFunction_read(int noOfCharacters) throws CarbonException {
        if (writer == null && reader == null) {
            jsFunction_openForReading();
        }
        if (reader != null) {
            char[] buffer = new char[noOfCharacters];
            int index = 0;
            StringBuffer buffer2 = new StringBuffer();
            int count;
            try {
                while (((count = reader.read(buffer)) > 0) & index < noOfCharacters) {
                    buffer2.append(buffer);
                    index += count;
                    if (index < noOfCharacters) {
                        buffer = new char[noOfCharacters - index];
                    }
                }
            } catch (IOException e) {
                throw new CarbonException(e);
            }
            return buffer2.toString();
        }
        throw new CarbonException(
                "File not open for reading. Please call openFileForReading() beforehand. ");
    }

    /**
     * <p>Reads a line from the file and return a string representation of the line. Users are required to open the file
     * for reading before reading from the file. </p>
     * <p>If the file is not open for either reading or writing or appending,
     * then calling this will automatically opens the file for reading.</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForReading();
     *    var x = file.readLine();
     *    print(x);
     *    file.close();
     * </pre>
     *
     * @throws IOException
     */
    public String jsFunction_readLine() throws CarbonException {
        if (writer == null && reader == null) {
            jsFunction_openForReading();
        }
        if (reader != null) {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        }
        throw new CarbonException(
                "File not open for reading. Please call openFileForReading() beforehand. ");
    }

    /**
     * <p>Reads all the content in the file and return a string representation of the content. Users are required to open the file
     * for reading before reading from the file. </p>
     * <p>If the file is not open for either reading or writing or appending,
     * then calling this will automatically opens the file for reading.</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForReading();
     *    var x = file.readAll();
     *    print(x);
     *    file.close();
     * </pre>
     *
     * @throws IOException
     */
    public String jsFunction_readAll() throws CarbonException {
        if (writer == null && reader == null) {
            jsFunction_openForReading();
        }
        try {
            if (reader != null) {
                StringBuffer stringBuffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String lineText;
                while ((lineText = reader.readLine()) != null) {
                    stringBuffer.append(lineText);
                }
                reader.close();
                return stringBuffer.toString();
            }
        } catch (IOException e) {
            throw new CarbonException(e);
        }
        throw new CarbonException(
                "File not open for reading. Please call openFileForReading() beforehand. ");
    }

    /**
     * <p>Users are expected to close the file after reading, writing & appending to the file. </p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.openForReading();
     *    var x = file.readLine();
     *    file.close();
     *    file.openForAppending();
     *    file.write("Hello world!");
     *    file.close();
     * </pre>
     *
     * @throws IOException
     */
    public void jsFunction_close() throws CarbonException {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            } else if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            throw new CarbonException(e);
        }
    }

    /**
     * <p>Creates the file if it does not exist. Also creates the parent directories if they are not present.</p>
     * <pre>
     *    var file = new File("readme.txt");
     *    file.createFile();
     * </pre>
     *
     * @throws IOException
     */
    public boolean jsFunction_createFile() throws CarbonException {
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                throw new CarbonException("Unable to create directory " + parentFile.getName());
            }
            try {
                if(file.createNewFile()) {
                    return true;
                } else {
                    throw new CarbonException("Unable to create file " + file.getName());
                }
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        }
        return false;
    }

    /**
     * Move the file to the given target file.
     *
     * @param arguments {String} The destination file name
     * @return true if the file was successfully moved.
     * @throws IOException
     */
    public static boolean jsFunction_move(Context cx, Scriptable thisObj, Object[] arguments,
                                          Function funObj) throws CarbonException {
        boolean result;
        FileHostObject fileHostObject = (FileHostObject) thisObj;
        String fileName;
        try {

            if (arguments[0] instanceof String) {
                fileName = FilenameUtils.normalizeNoEndSeparator((String) arguments[0]);
                if (fileName == null) {
                    throw new CarbonException(
                            "FileHostObject : Illegal file path, Cannot navigate away from resources directory");
                }
            } else {
                throw new CarbonException(
                        "Invalid parameter. Expecting destination file location as a string.");
            }

            BufferedReader source = new BufferedReader(new FileReader(fileHostObject.file));

            //Creating the new file
            Object object = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);
            AxisService axisService;
            if (object instanceof AxisService) {
                axisService = (AxisService) object;
            } else {
                throw new CarbonException("Error obtaining the AxisService.");
            }

            Object resourceFileObject = axisService
                    .getParameterValue(MashupConstants.RESOURCES_FOLDER);
            File resourceFolder;
            if (resourceFileObject != null && resourceFileObject instanceof File) {
                resourceFolder = (File) resourceFileObject;
            } else {
                throw new CarbonException("Resources folder not found.");
            }

            File newFile = new File(resourceFolder, fileName);
            if (newFile.isDirectory()) {
                throw new CarbonException(
                        "Given path is not a File. This object does not support directories.");
            }

            //Creating the file and directories
            if (!newFile.exists()) {
                File parentFile = newFile.getParentFile();
                if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                    throw new CarbonException("Unable to create directory " + parentFile.getName());
                }
            }

            BufferedWriter destination = new BufferedWriter(new FileWriter(newFile));

            String str;

            // Copying contents to new location
            while ((str = source.readLine()) != null) {
                destination.write(str);
                destination.newLine();
            }
            source.close();

            destination.flush();
            destination.close();

            //Delete the source file
            if(!fileHostObject.file.delete()) {
                throw new CarbonException("Unable to delete file " + fileHostObject.file.getName());
            }

            //Point to the new file
            fileHostObject.file = newFile;

            result = true;
        } catch (IOException e) {
            throw new CarbonException(e);
        }

        return result;
    }

    /**
     * Deletes this file from the file system.
     *
     * @return true if the file was successfully deleted.
     */
    public boolean jsFunction_deleteFile() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public String jsFunction_toString() throws CarbonException {
        return jsFunction_readAll();
    }

    /**
     * @return the length of the file in bytes
     */
    public long jsGet_length() {
        return file.length();
    }

    /**
     * @return the last modified time of this file.
     */
    public String jsGet_lastModified() {
        Date date = new Date(file.lastModified());
        return date.toString();
    }

    /**
     * @return the path name of the file.
     */
    public String jsGet_path() {
        return file.getPath();
    }

    /**
     * @return the name of the file without the path.
     */
    public String jsGet_name() {
        return file.getName();
    }

    /**
     * Checks whether this file actually exists.
     *
     * @return true if the file exists.
     */
    public boolean jsGet_exists() {
        return file.exists();
    }

    private Writer getWriter(boolean append) throws CarbonException {
        jsFunction_createFile();
        if (reader != null) {
            throw new CarbonException(
                    "Cannot write to the already reading file. Please close the file beforehand by calling close().");
        }
        if (writer == null) {
            try {
                writer = new BufferedWriter(new FileWriter(file, append));
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        }
        return writer;
    }

    /**
     * To access the file from java classes
     */
    public File getFile() {
        return file;
    }
}