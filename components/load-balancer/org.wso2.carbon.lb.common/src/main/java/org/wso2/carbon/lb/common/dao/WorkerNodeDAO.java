package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.dto.Bridge;
import org.wso2.carbon.lb.common.dto.WorkerNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class handles the database access relevant to worker node data
 *
 */
public class WorkerNodeDAO extends AbstractDAO{

    protected Log log = LogFactory.getLog(WorkerNodeDAO.class);
    Connection con = null;
    String url = "jdbc:mysql://localhost:3306/";
    String db = "hosting_mgt_db";
    String driver = "com.mysql.jdbc.Driver";
    String dbUsername = "root";
    String dbPassword = "root";
    Statement statement = null;


    public boolean create(WorkerNode workerNode, String epr) throws SQLException, ClassNotFoundException {
        boolean successfullyAdded = false;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql = "INSERT INTO worker_node VALUES('" + epr + "'," + workerNode.getIp()+ "','"
                         + workerNode.isAvailable() + ",'" + workerNode.getContainerRoot()+ "','"
                         + workerNode.getZone() + "')";

            statement.executeUpdate(sql);
            Bridge[] bridges = workerNode.getBridges();
            for (Bridge bridge : bridges) {
                String sqlForBridge = "INSERT INTO bridge VALUES(" + bridge.isAvailable() + ",'"
                                      + bridge.getWorkerNode() + "',"
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
            throw new ClassNotFoundException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    public void makeUnavailable(String endPoint) throws SQLException, ClassNotFoundException {
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            String sql =  "UPDATE worker_node SET available=false WHERE end_point='" + endPoint + "'";
                            statement.executeUpdate(sql);
                            //make zone unavailable
        }catch (SQLException s){
           String msg = "Error while deleting container data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
    }

    public boolean isWorkerNodeExist(String endPoint) throws SQLException, ClassNotFoundException {
        boolean isExist = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT 1 FROM worker_node WHERE end_point='" + endPoint + "'";
            resultSet = statement.executeQuery(sql);
            isExist = resultSet.next();
        }catch (SQLException s){
            String msg = "SQL statement is not executed for worker node exist !";
            log.error(msg);
            throw new SQLException(s);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new ClassNotFoundException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
        }
        return isExist;
    }

    public boolean isAvailableInDomain(String domain)
            throws SQLException, ClassNotFoundException {
        boolean isAvailable = false;
        ResultSet resultSet = null;
        try{
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            Class.forName(driver);
            statement = con.createStatement();
            Statement statement = con.createStatement();
            String sql =  "SELECT zone FROM domain WHERE domain='" + domain + "'";
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                String zone = resultSet.getString("zone");
                sql =  "SELECT 1 FROM worker_node WHERE zone='" + zone + "'";
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
            throw new ClassNotFoundException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }

        return isAvailable;
    }
    

    public boolean delete(String epr) throws SQLException, ClassNotFoundException {
        boolean successfullyDeleted = false;
        try{
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           Class.forName(driver);
           statement = con.createStatement();
           String sql = "DELETE FROM worker_node WHERE epr='" + epr + "'";

           statement.executeUpdate(sql);
            successfullyDeleted = true;
       }catch (SQLException s){
           String msg = "Error while deleting worker node data" + s.getMessage();
           log.error(msg);
           throw new SQLException(s);
       }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new ClassNotFoundException(msg);
       }
       finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
       }
        return successfullyDeleted;
    }
}
