package org.wso2.carbon.lb.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *    This class is implemented for provide caching support for EC2 stuff. Uuid of ec2 instance to ec2 id mapping is kept in
 *    a in emory map.
 */
public class Ec2DAO extends AbstractDAO{
     //protected Log log = LogFactory.getLog(ContainerDAO.class);
    private Connection con = null;
    private String url = "jdbc:mysql://localhost:3306/";
    private String db = "hosting_mgt_db";
    private String driver = "com.mysql.jdbc.Driver";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private Statement statement = null;

    /**
     * Uuid  to ec2 id mapping will be retrieved from persistence database. Those details will be cached in a
     * 'in memory map' at ec2 adapter level.
     *
     * @return a map which include all the Uuid  to ec2 id mapping details.
     * @throws java.sql.SQLException
     */
    public HashMap<String, String> getUuidToEc2IdMap() throws SQLException {
            HashMap uuidToEc2IdMap = new HashMap<String, String>();
            ResultSet resultSet = null;
            try{
                Class.forName(driver);
                con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
                statement = con.createStatement();
                String sql =  "SELECT * FROM ec2_instance" ;
                resultSet = statement.executeQuery(sql);
                while(resultSet.next()){
                    String uuid = resultSet.getString("uuid");
                    String ec2Id = resultSet.getString("ec2_id");
                    uuidToEc2IdMap.put(uuid, ec2Id);
                }
            }catch (SQLException s){
                String msg = "Error while getting Uuid  to ec2 map " + s.getMessage();
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
            return uuidToEc2IdMap;
        }

        public boolean add(String uuid, String ec2Id) throws SQLException {
            boolean successfullyAdded = false;
            ResultSet resultSet = null;
            try{
                Class.forName(driver);
                con = DriverManager.getConnection(url + db, dbUsername, dbPassword);
                statement = con.createStatement();
                String sql = "INSERT INTO ec2_instance VALUES('" + uuid +  "', '" + ec2Id + "')";
                statement.executeUpdate(sql);
                successfullyAdded = true;  //Adding ec instance details is succeeded
            }catch (SQLException s){
                String msg = "Error while inserting ec2 instance data";
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
