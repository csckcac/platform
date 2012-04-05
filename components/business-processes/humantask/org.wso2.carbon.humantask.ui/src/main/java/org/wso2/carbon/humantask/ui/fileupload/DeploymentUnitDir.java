/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.humantask.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Container providing various functions on the deployment directory.
 */
class DeploymentUnitDir {
    private static Log __log = LogFactory.getLog(DeploymentUnitDir.class);

    private String _name;
    private File _duDirectory;
    private File _descriptorFile;

//    private volatile HIConfigDocument _dd;

    private long _version = -1;

    private static final FileFilter _wsdlFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".wsdl") && path.isFile();
        }
    };

    private static final FileFilter _cbpFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".cbp") && path.isFile();
        }
    };

    private static final FileFilter _hiFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".ht") && path.isFile();
        }
    };

/*    private static final FileFilter _endpointFilter = new FileFilter() {
        public boolean accept(File path) {
            // endpoint-configuration.properties is deprecated, keep it for backward compatibility
            return (path.getName().endsWith(".endpoint") || path.getName().equals("endpoint-configuration.properties")) && path.isFile();
        }
    };*/

    DeploymentUnitDir(File dir) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory " + dir + " does not exist!");
        }

        _duDirectory = dir;
        _name = dir.getName();
        _descriptorFile = new File(_duDirectory, "htconfig.xml");

        if (!_descriptorFile.exists()) {
            throw new IllegalArgumentException("Directory " + dir + " does not contain a htconfig.xml file!");
        }
    }


    String getName() {
        return _duDirectory.getName();
    }

    /**
     * Checking for each BPEL file if we have a corresponding compiled process. If we don't,
     * starts compilation.
     */
    void compile() {
        ArrayList<File> his = listFilesRecursively(_duDirectory, DeploymentUnitDir._hiFilter);
        if (his.size() == 0) {
            throw new IllegalArgumentException("Directory " + _duDirectory.getName() + " does not contain any process!");
        }
        for (File hi : his) {
            compile(hi);
        }
    }

    void scan() {
        //TODO
        /*HashMap<QName, CBPInfo> processes = new HashMap<QName, CBPInfo>();
        ArrayList<File> cbps = listFilesRecursively(_duDirectory, DeploymentUnitDir._cbpFilter);
        for (File file : cbps) {
            CBPInfo cbpinfo = loadCBPInfo(file);
            processes.put(cbpinfo.processName, cbpinfo);
        }
        _processes = processes;

        HashMap<QName, TDeployment.Process> processInfo = new HashMap<QName, TDeployment.Process>();
        for (TDeployment.Process p : getDeploymentDescriptor().getDeploy().getProcessList()) {
            processInfo.put(p.getName(), p);
        }
        _processInfo = processInfo;
          */
    }

    boolean isRemoved() {
        return !_duDirectory.exists();
    }

    private void compile(File hiFile) {
        //TODO
    }

    public int hashCode() {
        return _duDirectory.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DeploymentUnitDir)) {
            return false;
        }
        return ((DeploymentUnitDir) obj).getDeployDir().getAbsolutePath().equals(getDeployDir().getAbsolutePath());
    }

    public File getDeployDir() {
        return _duDirectory;
    }

    /**
     * @return the list of endpoint configuration files. the list is built on each call to handle changes.
     */
    /*  public TreeSet<File> getEndpointConfigFiles() {
        File[] files = getDeployDir().listFiles(_endpointFilter);
        TreeSet<File> set = new TreeSet<File>();
        set.addAll(Arrays.asList(files));
        return set;
    }*/

//    public HIConfigDocument getDeploymentDescriptor() {
//        if (_dd == null) {
//            File ddLocation = new File(_duDirectory, "htconfig.xml");
//            try {
//                /*XmlOptions options = new XmlOptions();
//                HashMap otherNs = new HashMap();
//                otherNs.put("http://wso2.org/ht/schema/hi-config", "http://wso2.org/ht/schema/hi-config");
//                options.setLoadSubstituteNamespaces(otherNs);*/
//
//                _dd = HIConfigDocument.Factory.parse(ddLocation);
//            } catch (Exception e) {
//                throw new RuntimeException("Couldn't read htconfig.xml at location "
//                        + ddLocation.getAbsolutePath(), e);
//            }
//
//        }
//        return _dd;
//    }
    public Definition getDefinitionForService(QName name) {
        //TODO
        return null;
    }

    public Definition getDefinitionForPortType(QName name) {
        //TODO
        return null;
    }

    public Collection<Definition> getDefinitions() {
        //TODO
        return null;
    }

    /*   public Set<QName> getProcessNames() {
        return _processInfo.keySet();
    }*/

    public String toString() {
        return "{DeploymentUnit " + _name + "}";
    }

    public List<File> allFiles() {
        return allFiles(_duDirectory);
    }

    private List<File> allFiles(File dir) {
        ArrayList<File> result = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(allFiles(file));
            }
            if (file.isHidden()) {
                continue;
            }
            if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }

    private ArrayList<File> listFilesRecursively(File root, FileFilter filter) {
        ArrayList<File> result = new ArrayList<File>();
        // Filtering the files we're interested in in the current directory
        File[] select = root.listFiles(filter);
        for (File file : select) {
            result.add(file);
        }
        // Then we can check the directories
        File[] all = root.listFiles();
        for (File file : all) {
            if (file.isDirectory()) {
                result.addAll(listFilesRecursively(file, filter));
            }
        }
        return result;
    }

    public long getVersion() {
        return _version;
    }

    public void setVersion(long version) {
        _version = version;
    }

    /*public void setExtensionValidators(Map<QName, ExtensionValidator> extensionValidators) {
    	_extensionValidators = extensionValidators;
    }*/
}
