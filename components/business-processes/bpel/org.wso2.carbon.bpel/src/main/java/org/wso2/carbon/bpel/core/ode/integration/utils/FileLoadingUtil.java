/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.lang.reflect.Method;


/**
 * This class is implemented based on Apache Commons Configuration module's AbstractFileConfiguration class and some
 * other help classes used by that class.
 */
public class FileLoadingUtil {
    private static Log log = LogFactory.getLog(FileLoadingUtil.class);

    /**
     * Constant for Java version 1.4.
     */
    private static final float JAVA_1_4 = 1.4f;

    /**
     * Constant for the file URL protocol.
     */
    static final String PROTOCOL_FILE = "file";

    /**
     * Stores the base path.
     */
    private String basePath;

    public FileLoadingUtil(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Locate the specfied file and returns java.io.Reader.
     *
     * @param fileName file to load
     * @return InputStream
     *
     * @throws IllegalArgumentException  when error occurred during creation of URL
     * @throws FileLoadingUtilException when file loading error occurr
     */
    public InputStream load(String fileName) throws FileLoadingUtilException, IllegalArgumentException {
        try {
            URL url = locate(basePath, fileName);
            if (url == null) {
                throw new IllegalArgumentException("Cannot locate configuration source " + fileName);
            }

            return load(url);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            String errMsg = "Unable to load the configuration file " + fileName;
            log.error(errMsg, e);
            throw new FileLoadingUtilException(errMsg, e);
        }
    }

    /**
     * Load the configuration from the specified URL. This does not change the
     * source of the configuration (i.e. the internally maintained file name).
     * Use on of the setter methods for this purpose.
     *
     * @param url the URL of the file to be loaded
     * @return input stream
     * @throws FileLoadingUtilException if an error occurs
     */
    public InputStream load(URL url) throws FileLoadingUtilException {


        InputStream in = null;

        try {
            in = getInputStream(url);
            return in;
        }
        catch (FileLoadingUtilException e) {
            throw e;
        }
        catch (Exception e) {
            throw new FileLoadingUtilException("Unable to load the configuration from the URL " + url, e);
        }         
    }

    static InputStream getInputStream(URL url) throws FileLoadingUtilException {
        File file = fileFromURL(url);
        if (file != null && file.isDirectory()) {
            throw new FileLoadingUtilException("Cannot load a configuration from a directory");
        }

        try {
            return url.openStream();
        }
        catch (Exception e) {
            throw new FileLoadingUtilException("Unable to load the configuration from the URL " + url, e);
        }
    }

    /**
     * Tries to convert the specified URL to a file object. If this fails,
     * <b>null</b> is returned.
     *
     * @param url the URL
     * @return the resulting file object
     */
    static File fileFromURL(URL url) {
        if (PROTOCOL_FILE.equals(url.getProtocol())) {
            return new File(URLDecoder.decode(url.getPath()));
        } else {
            return null;
        }
    }

    /**
     * Return the location of the specified resource by searching the user home
     * directory, the current classpath and the system classpath.
     *
     * @param basePath the base path of the resource
     * @param fileName the name of the resource
     * @return the location of the actual file
     */
    static URL locate(String basePath, String fileName) {

        if (log.isDebugEnabled()) {
            log.debug("Locate file: " + fileName + " using base path: " + basePath);
        }

        if (fileName == null) {
            // undefined, always return null
            return null;
        }

        // attempt to create an URL directly
        URL url = locateFromURL(basePath, fileName);

        // attempt to load from an absolute path
        if (url == null) {
            File file = new File(fileName);
            if (file.isAbsolute() && file.exists()) // already absolute?
            {
                try {
                    url = toURL(file);
                    log.debug("Loading configuration from the absolute path " + fileName);
                }
                catch (MalformedURLException e) {
                    log.warn("Could not obtain URL from file", e);
                }
            }
        }

        // attempt to load from the base directory
        if (url == null) {
            try {
                File file = constructFile(basePath, fileName);
                if (file != null && file.exists()) {
                    url = toURL(file);
                }

                if (url != null) {
                    log.debug("Loading configuration from the path " + file);
                }
            }
            catch (MalformedURLException e) {
                log.warn("Could not obtain URL from file", e);
            }
        }

        // attempt to load from the user home directory
        if (url == null) {
            try {
                File file = constructFile(System.getProperty("user.home"), fileName);
                if (file != null && file.exists()) {
                    url = toURL(file);
                }

                if (url != null) {
                    log.debug("Loading configuration from the home path " + file);
                }

            }
            catch (MalformedURLException e) {
                log.warn("Could not obtain URL from file", e);
            }
        }

        return url;
    }

    static URL locateFromURL(String basePath, String fileName) {
        try {
            URL url;
            if (basePath == null) {
                return new URL(fileName);
                //url = new URL(name);
            } else {
                URL baseURL = new URL(basePath);
                url = new URL(baseURL, fileName);

                // check if the file exists
                InputStream in = null;
                try {
                    in = url.openStream();
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }
                return url;
            }
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Convert the specified file into an URL. This method is equivalent
     * to file.toURI().toURL() on Java 1.4 and above, and equivalent to
     * file.toURL() on Java 1.3. This is to work around a bug in the JDK
     * preventing the transformation of a file into an URL if the file name
     * contains a '#' character. See the issue CONFIGURATION-300 for
     * more details.
     *
     * @param file the file to be converted into an URL
     * @return URL
     * @throws java.net.MalformedURLException when error occurred
     */
    static URL toURL(File file) throws MalformedURLException {
        if (new SystemUtils().isJavaVersionAtLeast(JAVA_1_4)) {
            try {
                Method toURI = file.getClass().getMethod("toURI", (Class[]) null);
                Object uri = toURI.invoke(file, (Class[]) null);
                Method toURL = uri.getClass().getMethod("toURL", (Class[]) null);

                return (URL) toURL.invoke(uri, (Class[]) null);
            }
            catch (Exception e) {
                throw new MalformedURLException(e.getMessage());
            }
        } else {
            return file.toURL();
        }
    }

    /**
     * Helper method for constructing a file object from a base path and a
     * file name. This method is called if the base path passed to
     * <code>getURL()</code> does not seem to be a valid URL.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    static File constructFile(String basePath, String fileName) {
        File file;

        File absolute = null;
        if (fileName != null) {
            absolute = new File(fileName);
        }

        if (StringUtils.isEmpty(basePath) || (absolute != null && absolute.isAbsolute())) {
            file = new File(fileName);
        } else {
            StringBuffer fName = new StringBuffer();
            fName.append(basePath);

            // My best friend. Paranoia.
            if (!basePath.endsWith(File.separator)) {
                fName.append(File.separator);
            }

            //
            // We have a relative path, and we have
            // two possible forms here. If we have the
            // "./" form then just strip that off first
            // before continuing.
            //
            if (fileName.startsWith("." + File.separator)) {
                fName.append(fileName.substring(2));
            } else {
                fName.append(fileName);
            }

            file = new File(fName.toString());
        }

        return file;
    }


}
