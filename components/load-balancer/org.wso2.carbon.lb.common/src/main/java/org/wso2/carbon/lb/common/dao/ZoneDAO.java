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
public class ZoneDAO extends AbstractDAO{
    protected Log log = LogFactory.getLog(HostMachineDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";
    Statement statement = null;

    /**
     * This is called when new Host machine is registering. Zone and domains relevant to that zone are
     * inserted to database.
     * @param zone
     * @param domains
     * @return whether creation successful
     * @throws SQLException
     */
    public boolean create(Zone zone, String[] domains) throws SQLException, ClassNotFoundException {
        boolean successfullyAdded = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "INSERT INTO zone VALUES('" + zone.getName() +  "', true)";
            statement.executeUpdate(sql);
            for (String domain : domains) {
                sql = "INSERT INTO domain VALUES('" + domain + "', '" + zone.getName() + "')";
                statement.executeUpdate(sql);
            }
            successfullyAdded = true;  //Adding domains is also succeeded
        }catch (SQLException s){
            String msg = "Error while inserting zone data";
            log.error(msg + s.getMessage());
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    /**
     * This is for checking availability of zone
     * @param zone
     * @return whether the zone is in the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
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
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return isExist;
    }


}
