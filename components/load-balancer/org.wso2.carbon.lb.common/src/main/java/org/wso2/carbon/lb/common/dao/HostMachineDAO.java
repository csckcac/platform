
package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.HostMachine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class handles the database access relevant to worker node data
 *
 */
public class HostMachineDAO extends AbstractDAO{

    protected Log log = LogFactory.getLog(HostMachineDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";
    Statement statement = null;

/* Register a host machine when it is physically added to a zone. When a host machine is first
 * created it will first check whether the zone is already exist. If not this method will call
 * methods to register zone first.
*/

    public boolean create(HostMachine hostMachine) throws SQLException, ClassNotFoundException {
        boolean successfullyAdded = false;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "INSERT INTO host_machine VALUES('" + hostMachine.getEpr() + "','"
                         + hostMachine.getIp()+ "',"
                         + hostMachine.isAvailable() + ",'" + hostMachine.getContainerRoot()+ "','"
                         + hostMachine.getZone() + "')";

            statement.executeUpdate(sql);
            Bridge[] bridges = hostMachine.getBridges();
            for (Bridge bridge : bridges) {
                String sqlForBridge = "INSERT INTO bridge VALUES(" + bridge.isAvailable() + ",'"
                                      + bridge.getHostMachine() + "',"
                                      + bridge.getMaximumCountIps() + ","
                                      + bridge.getCurrentCountIps() + ",'"
                                      + bridge.getBridgeIp()+ "','"
                                      + bridge.getNetMask() + "','"
                                      + bridge.getNetGateway() + "')";
                statement.executeUpdate(sqlForBridge);
                String sqlForStartingIp = "INSERT INTO available_ip (ip, bridge) VALUES('"
                                          + incrementIp(bridge.getBridgeIp()) + "','"
                                          + bridge.getBridgeIp() + "')";
                statement.executeUpdate(sqlForStartingIp);
            }
            successfullyAdded = true;

        }catch (SQLException s){
            String msg = "Error while inserting worker node plan data" + s.getMessage();
            log.error(msg);
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    /**
     * Make the host machine unavailable when the resources of the host machine is not enough for
     * creating the largest possible container. This request comes if container creation call
     * to agent manager returns false.
     * @param endPoint
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void makeUnavailable(String endPoint) throws SQLException, ClassNotFoundException {
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "UPDATE host_machine SET available=false WHERE epr='" + endPoint + "'";
                            statement.executeUpdate(sql);
                            //make host machine unavailable
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
    }

    /**
     * This returns true if the input epr is exist in the database. This will be called to check if
     * the input epr is already there before inserting a new host machine.
     * @param endPoint
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isHostMachineExist(String endPoint) throws SQLException, ClassNotFoundException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT 1 FROM host_machine WHERE epr='" + endPoint + "'";
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for worker node exist !";
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
     * This is to check whether there are any available host machines in input domain and will be
     * called when container creation request come.
     * @param domain
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isAvailableInDomain(String domain)
            throws SQLException, ClassNotFoundException {
        boolean isAvailable = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT zone FROM domain WHERE domain_name='" + domain + "'";
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){ //get the first result as only one zone is per domain
                String zone = resultSet.getString("zone");
                sql =  "SELECT 1 FROM host_machine WHERE zone='" + zone + "'";
                resultSet = statement.executeQuery(sql);
                isAvailable = resultSet.next();
            }
        }catch (SQLException s){
            String msg = "SQL statement is not executed for worker node exist !";
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
        return isAvailable;
    }

    /**
     * This method will delete host machine from database when host machine is removed from the zone.
     * (Host machine is no longer available for any operation)
     * @param epr
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean delete(String epr) throws SQLException, ClassNotFoundException {
        boolean successfullyDeleted = false;
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "DELETE FROM host_machine WHERE epr='" + epr + "'";

           statement.executeUpdate(sql);
            successfullyDeleted = true;
       }catch (SQLException s){
           String msg = "Error while deleting worker node data " + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
        return successfullyDeleted;
    }





}

package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.HostMachine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class handles the database access relevant to worker node data
 *
 */
public class HostMachineDAO extends AbstractDAO{

    protected Log log = LogFactory.getLog(HostMachineDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";
    Statement statement = null;

/* Register a host machine when it is physically added to a zone. When a host machine is first
 * created it will first check whether the zone is already exist. If not this method will call
 * methods to register zone first.
*/

    public boolean create(HostMachine hostMachine) throws SQLException, ClassNotFoundException {
        boolean successfullyAdded = false;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "INSERT INTO host_machine VALUES('" + hostMachine.getEpr() + "','"
                         + hostMachine.getIp()+ "',"
                         + hostMachine.isAvailable() + ",'" + hostMachine.getContainerRoot()+ "','"
                         + hostMachine.getZone() + "')";

            statement.executeUpdate(sql);
            Bridge[] bridges = hostMachine.getBridges();
            for (Bridge bridge : bridges) {
                String sqlForBridge = "INSERT INTO bridge VALUES(" + bridge.isAvailable() + ",'"
                                      + bridge.getHostMachine() + "',"
                                      + bridge.getMaximumCountIps() + ","
                                      + bridge.getCurrentCountIps() + ",'"
                                      + bridge.getBridgeIp()+ "','"
                                      + bridge.getNetMask() + "','"
                                      + bridge.getNetGateway() + "')";
                statement.executeUpdate(sqlForBridge);
                String sqlForStartingIp = "INSERT INTO available_ip (ip, bridge) VALUES('"
                                          + incrementIp(bridge.getBridgeIp()) + "','"
                                          + bridge.getBridgeIp() + "')";
                statement.executeUpdate(sqlForStartingIp);
            }
            successfullyAdded = true;

        }catch (SQLException s){
            String msg = "Error while inserting worker node plan data" + s.getMessage();
            log.error(msg);
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    /**
     * Make the host machine unavailable when the resources of the host machine is not enough for
     * creating the largest possible container. This request comes if container creation call
     * to agent manager returns false.
     * @param endPoint
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void makeUnavailable(String endPoint) throws SQLException, ClassNotFoundException {
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "UPDATE host_machine SET available=false WHERE epr='" + endPoint + "'";
                            statement.executeUpdate(sql);
                            //make host machine unavailable
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
    }

    /**
     * This returns true if the input epr is exist in the database. This will be called to check if
     * the input epr is already there before inserting a new host machine.
     * @param endPoint
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isHostMachineExist(String endPoint) throws SQLException, ClassNotFoundException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT 1 FROM host_machine WHERE epr='" + endPoint + "'";
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for worker node exist !";
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
     * This is to check whether there are any available host machines in input domain and will be
     * called when container creation request come.
     * @param domain
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean isAvailableInDomain(String domain)
            throws SQLException, ClassNotFoundException {
        boolean isAvailable = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT zone FROM domain WHERE domain_name='" + domain + "'";
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){ //get the first result as only one zone is per domain
                String zone = resultSet.getString("zone");
                sql =  "SELECT 1 FROM host_machine WHERE zone='" + zone + "'";
                resultSet = statement.executeQuery(sql);
                isAvailable = resultSet.next();
            }
        }catch (SQLException s){
            String msg = "SQL statement is not executed for worker node exist !";
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
        return isAvailable;
    }

    /**
     * This method will delete host machine from database when host machine is removed from the zone.
     * (Host machine is no longer available for any operation)
     * @param epr
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean delete(String epr) throws SQLException, ClassNotFoundException {
        boolean successfullyDeleted = false;
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "DELETE FROM host_machine WHERE epr='" + epr + "'";

           statement.executeUpdate(sql);
            successfullyDeleted = true;
       }catch (SQLException s){
           String msg = "Error while deleting worker node data " + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
        return successfullyDeleted;
    }





}