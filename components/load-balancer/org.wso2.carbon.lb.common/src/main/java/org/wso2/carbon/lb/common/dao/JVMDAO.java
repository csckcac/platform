package org.wso2.carbon.lb.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class JVMDAO extends AbstractDAO{
    //protected Log log = LogFactory.getLog(ContainerDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;


    public boolean addToEprToInstanceCountTable(String epr, int instanceCount) throws SQLException {
        boolean successfullyAdded = false;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "INSERT INTO epr_to_instance_count VALUES('" + epr +  "', '" + instanceCount + "')";
            statement.executeUpdate(sql);
            successfullyAdded = true;  //Adding instance details is succeeded
        }catch (SQLException s){
            String msg = "Error while inserting JVM instance data";
            log.error(msg + s.getMessage());
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyAdded;
    }

    public boolean updateLastPickedAgentTable(int lastPickedAgent) throws SQLException {
        boolean successfullyUpdated = false;
        try{
            Class.forName(driver);
           con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
           statement = con.createStatement();
           String sql = "UPDATE last_picked_agent_table SET last_picked_agent='"+ lastPickedAgent + "'";
           statement.executeUpdate(sql);
            successfullyUpdated = true;
        }catch (SQLException s){
           String msg = "Error while updating JVM last picked agent" + s.getMessage();
           log.error(msg);
           throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
           String msg = "Error while sql connection :" + s.getMessage();
           log.error(msg);
           throw new SQLException(msg);
        }
        finally {
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return successfullyUpdated;
    }

    public List<String> getEprList() throws SQLException {//: related query would be "select epr from epr_to_instanceCount;"
        List<String> eprList = new ArrayList<String>();
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT epr FROM epr_to_instance_count" ;
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                String epr = resultSet.getString("epr");
                eprList.add(epr);
            }
        }catch (SQLException s){
            String msg = "Error while getting epr list " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return eprList;
    }

    public Map<String, Integer> getEprToInstanceCountMap() throws SQLException {
        HashMap eprToInstanceCountMap = new HashMap<String, Integer>();
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT epr, instance_count FROM epr_to_instance_count" ;
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                String epr = resultSet.getString("epr");
                int instanceCount = resultSet.getInt("instance_count");
                eprToInstanceCountMap.put(epr, instanceCount);
            }
        }catch (SQLException s){
            String msg = "Error while getting epr To Instance Count map " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return eprToInstanceCountMap;
    }

    public int getTotalInstanceCount() throws SQLException {//: sum up all the instanceCount entries of table epr_to_instanceCount and return.
        int totalCount = 0;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT instance_count FROM epr_to_instance_count" ;
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                int instanceCount = resultSet.getInt("instance_count");
                totalCount += instanceCount;
            }
        }catch (SQLException s){
            String msg = "Error while getting epr To Instance Count map " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return totalCount;
    }

    public int getLastPickedAgent() throws SQLException { //: return the last entry (most recent) of the table lastPickedAgent
        int lastPickedAgent = 0;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT last_picked_agent FROM last_picked_agent_table" ;
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                lastPickedAgent = resultSet.getInt("last_picked_agent");
            }
        }catch (SQLException s){
            String msg = "Error while getting last picked agent " + s.getMessage();
            log.error(msg);
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "Error while sql connection :" + s.getMessage();
            log.error(msg);
            throw new SQLException(msg);
        }
        finally {
            try { if (resultSet != null) resultSet.close(); } catch(Exception e) {}
            try { if (statement != null) statement.close(); } catch(SQLException e) {}
            try { if (con != null) con.close(); } catch(Exception e) {}
        }
        return lastPickedAgent;
    }

    public boolean removeEpr(String epr) throws SQLException {
        boolean successfullyDeleted = false;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "DELETE FROM epr_to_instance_count WHERE epr='" + epr + "'";
            statement.executeUpdate(sql);
            successfullyDeleted = true;  //Deleting instance details is succeeded
        } catch (SQLException s){
            String msg = "Error while deleting JVM epr";
            log.error(msg + s.getMessage());
            throw new SQLException(s + msg);
        }catch (ClassNotFoundException s){
            String msg = "DB connection not successful !";
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
