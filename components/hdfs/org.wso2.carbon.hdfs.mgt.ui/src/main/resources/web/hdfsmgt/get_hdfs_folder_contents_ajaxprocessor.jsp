<%@ page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminHDFSServerManagementException" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String fsObjectPath = request.getParameter("fs_object_path");

    JSONArray fsObjectList = new JSONArray();
    HDFSAdminClient hdfsAdminClient = null;

    try {
        hdfsAdminClient = new HDFSAdminClient(config.getServletContext(), session);
        if (hdfsAdminClient != null) {
            FolderInformation[] folderList = null;
            try {
                folderList = hdfsAdminClient.getCurrentUserFSObjects(fsObjectPath);
            } catch (HDFSAdminHDFSServerManagementException e) {
                e.getFaultMessage();
            }
            for (int i = 0; i < folderList.length; i++) {
                //out.print(folderList[i].getName());
                if (folderList[i].getFolder()) {
                    JSONObject folder = new JSONObject();
                    JSONObject dataList = new JSONObject();
                    dataList.put("title",folderList[i].getName());
                    dataList.put("icon","images/folder.gif");
                    folder.put("data", dataList);
                    JSONObject attrList = new JSONObject();
                    attrList.put("path",folderList[i].getFolderPath());
                    attrList.put("id",folderList[i].getFolderPath());
                    folder.put("attr", attrList);
                    folder.put("state", "closed");
//                    folder.put("icon", "folder");
                    folder.put("type","folder");
                    fsObjectList.add(folder);
                } else {
                    JSONObject file = new JSONObject();
                    file.put("data", folderList[i].getName());
                    JSONObject filePath = new JSONObject();
                    filePath.put("path",folderList[i].getFolderPath());
                    file.put("attr", filePath);
                    file.put("attr", folderList[i].getFolderPath());
                    file.put("state", "closed");
                    file.put("icon", "file");
                    file.put("type","file");
                    fsObjectList.add(file);
                }
            }
            //System.out.println("JSON String : " + fsObjectList.toJSONString());
            out.print(fsObjectList);
            out.flush();
        }

    } catch (Exception e) {
    }
%>