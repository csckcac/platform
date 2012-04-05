package org.wso2.carbon.hosting.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.dto.ZoneResourcePlan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 *
 */
public class ZoneResourcePlanDAO {
    protected Log log = LogFactory.getLog(WorkerNodeDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";

    public ZoneResourcePlanDAO(){
        connectDB();
    }
    private void connectDB(){
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            Statement statement = con.createStatement();
        }catch (SQLException s){
            System.out.println("Connection!");
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void create(ZoneResourcePlan zoneResourcePlan){
        try{
            Statement statement = con.createStatement();
            String sql = "INSERT INTO zone_resource_plan VALUES('" + zoneResourcePlan.getZone() + "',"
                         + zoneResourcePlan.isAvailable() + ",'" + zoneResourcePlan.getMemory()+ "','"
                         + zoneResourcePlan.getSwap() + "','" + zoneResourcePlan.getCpuShares() + "','"
                         + zoneResourcePlan.getCpuSetCpus() + "','" + zoneResourcePlan.getStorage() + "','"
                         + zoneResourcePlan.getNetMask() + "','" + zoneResourcePlan.getNetGateway() +  "')";

            int val = statement.executeUpdate(sql);
        }catch (SQLException s){
            System.out.println("SQL statement is not executed for create zone!");
            s.printStackTrace();
        }
    }

    public boolean isZoneExist(String zone){
        boolean isExist = false;
        try{
            Statement statement = con.createStatement();
            String sql =  "SELECT 1 FROM zone_resource_plan WHERE zone='" + zone + "'";
            ResultSet s = statement.executeQuery(sql);
            isExist = s.next();
        }catch (SQLException s){
            System.out.println("SQL statement is not executed for zone exist!");
            s.printStackTrace();
        }
        return isExist;
    }
    


}
