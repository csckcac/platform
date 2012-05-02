package org.wso2.carbon.lb.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class InstanceDAO extends AbstractDAO{
    //protected Log log = LogFactory.getLog(ContainerDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;
    /**
     * This method is implemented for provide caching support in this component. Instance  to domain
     * mapping will be retrieved from persistence database. Those details will be cached in a
     * 'in memory map' at adapter level.
     *
     * @return a map which include all the instance to domain mapping details.
     * @throws SQLException
     */
    public HashMap<String, ArrayList<String>> getDomainToInstanceIdsMap() throws SQLException {
        HashMap instanceToDomainMap = new HashMap<String, ArrayList<String>>();
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT instance_id, domain FROM instance" ;
            resultSet = statement.executeQuery(sql);
            ArrayList<String> instanceList = null;
            while(resultSet.next()){
                String domain = resultSet.getString("domain");
                String instanceId = resultSet.getString("instance_id");
                instanceList = new ArrayList<String>();
                if(!instanceToDomainMap.containsKey(domain)){
                    instanceList.add(instanceId);
                }else {
                    Object instanceListObject = instanceToDomainMap.get(domain);
                    if ( instanceListObject instanceof ArrayList){
                        instanceList = (ArrayList<String>) instanceListObject;
                        instanceList.add(instanceId);
                    }else {
                        String msg = "Unable to retrieve instance array";
                        throw new ClassCastException(msg);
                    }
                }
                instanceToDomainMap.put(domain, instanceList);
            }
        }catch (SQLException s){
            String msg = "Error while getting available ip " + s.getMessage();
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
        return instanceToDomainMap;
    }


    /**
     * This method is implemented for provide caching support in this component. Instance  to adapter
     * mapping will be retrieved from persistence database. Those details will be cached in a
     * 'in memory map' at adapter level.
     *
     * @return a map which include all the instance to adapter   mapping details.
     * @throws SQLException
     */
    public HashMap<String, String> getInstanceIdToAdapterMap() throws SQLException {
        HashMap instanceToAdapterMap = new HashMap<String, String>();
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql =  "SELECT instance_id, adapter FROM instance" ;
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                String adapter = resultSet.getString("adapter");
                String instanceId = resultSet.getString("instance_id");
                instanceToAdapterMap.put(instanceId, adapter);
            }
        }catch (SQLException s){
            String msg = "Error while getting adapter map " + s.getMessage();
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
        return instanceToAdapterMap;
    }
    
    public boolean add(String instance, String adapter, String domain) throws SQLException {
        boolean successfullyAdded = false;
        ResultSet resultSet = null;
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
            statement = con.createStatement();
            String sql = "INSERT INTO instance VALUES('" + instance +  "', '" + adapter + "', '" + domain + "')";
            statement.executeUpdate(sql);
            successfullyAdded = true;  //Adding instance details is succeeded
        }catch (SQLException s){
            String msg = "Error while inserting instance data";
            log.error(msg + s.getMessage());
            throw new SQLException(s + msg);
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

}
