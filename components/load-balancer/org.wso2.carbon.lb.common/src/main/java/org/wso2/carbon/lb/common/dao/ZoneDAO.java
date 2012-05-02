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
 *  This class handles the database access relevant to zone and domain
 *  data
 *
 */
public class ZoneDAO extends AbstractDAO{
    //protected Log log = LogFactory.getLog(HostMachineDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;

    /**
     * This is called when new Host machine is registering. Zone and domains relevant to that zone are
     * inserted to database.
     * @param zone
     * @param domains
     * @return whether creation successful
     * @throws SQLException
     */
    public boolean create(Zone zone, String[] domains) throws SQLException {
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
     * This is called when configuration update call came. It will update the domains.
     * @param zone
     * @param domains
     * @return whether update request successful
     * @throws SQLException
     */
    public boolean update(String zone, String[] domains) throws SQLException {
        boolean successfullyUpdated = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "DELETE FROM domain WHERE zone=" + zone;
            //delete all the current domain entries for zone
            statement.executeUpdate(sql);
            for(String domain : domains){  //insert new set of domains
                sql = "INSERT INTO domain VALUES('" + domain + "', '" + zone + "')";
                statement.executeUpdate(sql);
            }
            successfullyUpdated = true;
        }catch (SQLException s){
            String msg = "Error while updating ";
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
        return successfullyUpdated;
    }

    /**
     * This is for checking availability of zone
     * @param zone
     * @return whether the zone is in the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isZoneExist(String zone) throws SQLException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
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


    /**
     * This is for checking availability of domain
     * @param domain
     * @return whether the domain is in the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isDomainExist(String domain) throws SQLException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT 1 FROM domain WHERE domain_name='" + domain + "'";
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for domain exist !";
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
