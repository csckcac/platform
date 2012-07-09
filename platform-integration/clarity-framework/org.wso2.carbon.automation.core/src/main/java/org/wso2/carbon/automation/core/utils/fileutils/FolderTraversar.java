/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.utils.fileutils;

import java.io.File;
import java.io.FilenameFilter;

public class FolderTraversar {

    private String indent = "";
    private File originalFileObject;
    private File fileObject;

    public FolderTraversar(File fileObject) {
        this.originalFileObject = fileObject;
        this.fileObject = fileObject;
    }

    public void traverse() {
        recursiveTraversal(fileObject);
    }

    public void recursiveTraversal(File fileObject) {
        if (fileObject.isDirectory()) {
            indent = getIndent(fileObject);

            File allFiles[] = fileObject.listFiles();
            for (File aFile : allFiles) {
                recursiveTraversal(aFile);
            }
        } else if (fileObject.isFile()) {
            System.out.println(indent + "  " + fileObject.getName());
        }
    }

    public String[] getConfigDirectories() {

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.contains(".xml");
            }
        };
        return fileObject.list(filter);
    }

    public String[] getConfigFiles(File fileObject) {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains(".xml");
            }
        };
        for (String aProxyServiceFiles : fileObject.list(filter)) {
            System.out.println(aProxyServiceFiles);
        }
        return fileObject.list(filter);
    }

    private String getIndent(File fileObject) {
        String original = originalFileObject.getAbsolutePath();
        String fileStr = fileObject.getAbsolutePath();
        String subString =
                fileStr.substring(original.length(), fileStr.length());

        String indent = "";
        for (int index = 0; index < subString.length(); index++) {
            char aChar = subString.charAt(index);
            if (aChar == File.separatorChar) {
                indent = indent + "  ";
            }
        }
        return indent;
    }
}

