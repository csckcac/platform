package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Zone;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *  This class handles the database access relevant to zone resource plan data
 *
 */
public class ZoneDAO {
    protected Log log = LogFactory.getLog(WorkerNodeDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";
    Statement statement = null;


    public boolean create(Zone zone) throws SQLException, ClassNotFoundException {
        boolean successfullyAdded = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "INSERT INTO zone_resource_plan VALUES('" + zone.getName() +  "')";
            statement.executeUpdate(sql);
            successfullyAdded = true;
        }catch (SQLException s){
            String msg = "Error while inserting zone data";
            log.error(msg);
            throw new SQLException(s);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    public boolean isZoneExist(String zone) throws SQLException, ClassNotFoundException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "SELECT 1 FROM zone WHERE name='" + zone + "'";
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for zone exist !";
            log.error(msg);
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(s);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
        }
        return isExist;
    }
    


}
