package org.wso2.carbon.hosting.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.dto.Bridge;
import org.wso2.carbon.hosting.mgt.dto.WorkerNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 */
public class WorkerNodeDAO{
    protected Log log = LogFactory.getLog(WorkerNodeDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";

    public WorkerNodeDAO(){
        connectDB();
    }

    private void connectDB(){
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
        }catch (SQLException s){
            System.out.println("Connected to DB successfully!");
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void create(WorkerNode workerNode){
        try{
            Statement statement = con.createStatement();
            String sql = "INSERT INTO worker_node VALUES('" + workerNode.getName() + "',"
                         + workerNode.isAvailable() + ",'" + workerNode.getContainerRoot()+ "','"
                         + workerNode.getEndPoint() + "','"
                         + workerNode.getZone() + "')";

            statement.executeUpdate(sql);
            Bridge[] bridges = workerNode.getBridges();
            for(int i=0; i < bridges.length; i++){
                Bridge bridge = bridges[i];
                String sqlForBridge = "INSERT INTO bridge VALUES('" + bridge.getName() + "',"
                                         + bridge.isAvailable() + ",'" + bridge.getWorkerNode()+ "',"
                                         + bridge.getMaximumCountIps() + ","
                                         + bridge.getCurrentCountIps() + ")";
                statement.executeUpdate(sqlForBridge);
            }

        }catch (SQLException s){
            System.out.println("SQL statement is not executed for insert work node!");
            s.printStackTrace();
        }
    }



}
