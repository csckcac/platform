/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.hdfs.mgt;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Todo Doc
 */
public class HDFSAdmin extends AbstractAdmin {

    //private Configuration configuration = new Configuration(false);
    //set FS default user home directory.
    private static final String USER_HOME = "/user";


    /**
     * Mgt service return file and folder list of the give HDFS path
     *
     * @param fsObjectPath file system path which user need info about files and folders
     * @return list with files and folders in the given path
     * @throws HDFSServerManagementException
     */
    public FolderInformation[] getCurrentUserFSObjects(String fsObjectPath)
            throws HDFSServerManagementException {

        if (fsObjectPath == null) {
            fsObjectPath = "/user/" + fsObjectPath;
        }

        //DataAccessComponentManager dataAccessComponentManager = new DataAccessComponentManager();
        //Configuration configuration = dataAccessComponentManager.getClusterConfiguration();
        //HDFSAdminComponentManager hdfsAdminComponentManager = new HDFSAdminComponentManager();

        //DataAccessService dataAccessService =
        //        hdfsAdminComponentManager.getDataAccessService();
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();

        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        FileStatus[] fileStatus = null;
        //FolderInformation[] folderInfo;
        List<FolderInformation> folderInfo = new ArrayList<FolderInformation>();
        try {
            if (hdfsFS != null) {
                fileStatus = hdfsFS.listStatus(new Path(fsObjectPath));
            }

            for (int i = 0; i < fileStatus.length; i++) {
                System.out.println(fileStatus[i].getPath().getName());

                FolderInformation folder = new FolderInformation();
                folder.setFolder(fileStatus[i].isDir());
                folder.setName(fileStatus[i].getPath().getName());
                folder.setFolderPath(fileStatus[i].getPath().toUri().getPath());
                folder.setOwner(fileStatus[i].getOwner());
                folder.setGroup(fileStatus[i].getGroup());
                folder.setPermissions(fileStatus[i].getPermission().toString());
                folderInfo.add(folder);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(folderInfo.toString());
        return folderInfo.toArray(new FolderInformation[folderInfo.size()]);
    }

    public void copy(String srcPath, String dstPath) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();

        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Path[] srcs = new Path[0];
        if (hdfsFS != null) {
            try {
                srcs = FileUtil.stat2Paths(hdfsFS.globStatus(new Path(srcPath)), new Path(srcPath));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            if (srcs.length > 1 && !hdfsFS.isDirectory(new Path(dstPath))) {
                throw new IOException("When copying multiple files, "
                                      + "destination should be a directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Configuration configuration = new Configuration();
        configuration.set("io.file.buffer.size", Integer.toString(4096));
        for (int i = 0; i < srcs.length; i++) {
            try {
                FileUtil.copy(hdfsFS,srcs[i], hdfsFS, new Path(dstPath), false, configuration);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * Delete the HDFS file in the given path
     *
     * @param filePath File path for the file to be deleted
     * @return return true if the file deletetion is a success
     */
    public boolean deleteFile(String filePath) throws HDFSServerManagementException {

        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();

        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            /**
             * HDFS delete with recursive delete off
             */
            System.out.println("File Path " + filePath);
            //System.out.println(hdfsFS.delete(new Path(filePath)));
            //return hdfsFS.delete(new Path("filePath"),false);
            return hdfsFS.delete(new Path(filePath));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }


    /**
     * Delete the HDFS folder in the given path
     *
     * @param folderPath Path  Folder path for the folder to be deleted
     * @return return true if folder deletion is a success
     * @throws
     */
    public boolean deleteFolder(String folderPath) throws HDFSServerManagementException {

        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();

        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            /**
             * HDFS delete with recursive delete on to delete folder and the content
             */
            //return hdfsFS.delete(new Path("folderPath"),true);
            return hdfsFS.delete(new Path(folderPath));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    /**
     * Rename file or a folder using source and the destination of the give FS Object
     *
     * @param srcPath Current path and the file name of the file to be renamed
     * @param dstPath new  pathe and the file name
     * @return success if rename is successful
     * @throws HDFSServerManagementException
     */

    public boolean renameFile(String srcPath, String dstPath) throws HDFSServerManagementException {

        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            return hdfsFS.rename(new Path(srcPath), new Path(dstPath));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    /**
     * Rename file or a folder using source and the destination of the give FS Object
     *
     * @param srcPath Current path and the file name of the file to be renamed
     * @param dstPath new  pathe and the file name
     * @return success if rename is successful
     * @throws HDFSServerManagementException
     */

    public boolean renameFolder(String srcPath, String dstPath)
            throws HDFSServerManagementException {

        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            return hdfsFS.rename(new Path(srcPath), new Path(dstPath));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    public boolean moveFile(String srcPath, String dstPath) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            return hdfsFS.rename(new Path(srcPath), new Path(dstPath));

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    public boolean makeDirectory(String folderPath) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            return hdfsFS.mkdirs(new Path(folderPath));

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public boolean createFile(String filePath, byte[] fileContent)
            throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            if (!hdfsFS.exists(new Path(filePath))) {
                FSDataOutputStream outputStream = hdfsFS.create(new Path(filePath));
                outputStream.write(fileContent);
                outputStream.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

//    public byte downloadFile(String filePath) throws HDFSServerManagementException {
//         DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
//        FileSystem hdfsFS = null;
//        try {
//            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
//            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
//            if(hdfsFS.exists(new Path(filePath))){
//                FSDataInputStream inputStream = hdfsFS.open(new Path(filePath));
//                return  inputStream.readByte();
//
//                FSDataInputStream in = fs.open(tenantFileName);
//            String messageIn = in.readUTF();
//            System.out.print(messageIn);
//            in.close();
//                FSDataOutputStream outputStream =  hdfsFS.create(new Path(filePath));
//                outputStream.write(fileContent);
//                outputStream.close();
//                return true;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        return false;
//
//    }

    public String getPermission(String fsPath) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            return hdfsFS.getFileStatus(new Path(fsPath)).getPermission().toString();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public void setPermission(String fsPath, String fsPermission)
            throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            hdfsFS.setPermission(new Path(fsPath), new FsPermission(fsPermission));

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void setGroup(String fsPath, String group) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();

        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            hdfsFS.setOwner(new Path(fsPath), null, group);  //TO DO: validate the group / role

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void setOwner(String fsPath, String owner) throws HDFSServerManagementException {
        DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
            hdfsFS.setOwner(new Path(fsPath), owner, null);  //TO DO: validate the group / role

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
