package org.apache.hadoop.hive.jdbc.storage.utils;


import org.apache.commons.dbcp.BasicDataSource;
import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Commons {


    public static Object getObjectFromWritable(Writable w) {
        if (w instanceof IntWritable) {
            // int
            return ((IntWritable) w).get();
        } else if (w instanceof ShortWritable) {
            // short
            return ((ShortWritable) w).get();
        } else if (w instanceof ByteWritable) {
            // byte
            return ((ByteWritable) w).get();
        } else if (w instanceof BooleanWritable) {
            // boolean
            return ((BooleanWritable) w).get();
        } else if (w instanceof LongWritable) {
            // long
            return ((LongWritable) w).get();
        } else if (w instanceof FloatWritable) {
            // float
            return ((FloatWritable) w).get();
        } else if (w instanceof DoubleWritable) {
            // double
            return ((DoubleWritable) w).get();
        } else if (w instanceof NullWritable) {
            //null
            return null;
        } else {
            // treat as string
            return w.toString();
        }

    }


    /**
     * Set values for the prepared statement
     *
     * @param value     value
     * @param position
     * @param statement prepared statement
     * @return
     */
    public static PreparedStatement assignCorrectObjectType(Object value, int position,
                                                            PreparedStatement statement) {
        try {
            if (value instanceof Integer) {
                statement.setInt(position, (Integer) value);
            } else if (value instanceof Short) {
                statement.setShort(position, (Short) value);
            } else if (value instanceof Byte) {
                statement.setByte(position, (Byte) value);
            } else if (value instanceof Boolean) {
                statement.setBoolean(position, (Boolean) value);
            } else if (value instanceof Long) {
                statement.setLong(position, (Long) value);
            } else if (value instanceof Float) {
                statement.setFloat(position, (Float) value);
            } else if (value instanceof Double) {
                statement.setDouble(position, (Double) value);
            } else {
                statement.setString(position, value.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;

    }

    //If user has given the query for creating table, we don't need to ask the table name again. Get the table name from the query
    public final static String extractingTableNameFromQuery(String createTableQuery) {
        String tableName = null;
        if (createTableQuery != null) {
            List<String> queryList = Arrays.asList(createTableQuery.split(" "));
            Iterator<String> iterator = queryList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equalsIgnoreCase("create")) {
                    while (iterator.hasNext()) {
                        if (iterator.next().equalsIgnoreCase("table")) {
                            while (iterator.hasNext()) {
                                String nextString = iterator.next();
                                if (!nextString.equalsIgnoreCase("")) {
                                    tableName = nextString;
                                    return tableName;
                                }
                            }
                        }
                    }
                }

            }
        } else {
            throw new IllegalArgumentException("You should provide at least " +
                                               DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY + " or " +
                                               ConfigurationUtils.HIVE_JDBC_TABLE_CREATE_QUERY + " property.");
        }
        return tableName;
    }

    public static final String extractFieldNames(String selectQuery) {
        String fields = "";
        List<String> queryList = Arrays.asList(selectQuery.split(" "));
        Iterator<String> iterator = queryList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equalsIgnoreCase("select")) {
                while (iterator.hasNext()) {
                    String nextString = iterator.next();
                    if (nextString.equalsIgnoreCase("from")) {
                        return fields;
                    }
                    fields += nextString;
                }
            }
        }
        return fields;
    }

    // Configuring basicDataSource
    public static BasicDataSource configureBasicDataSource(JobConf conf) {
        BasicDataSource basicDataSource = new BasicDataSource();
        String connectionUrl = ConfigurationUtils.getConnectionUrl(conf);
        connectionUrl=  connectionUrl.replaceAll(" ", "");
        basicDataSource.setUrl(connectionUrl);
        basicDataSource.setDriverClassName(ConfigurationUtils.getDriverClass(conf));
        basicDataSource.setUsername(ConfigurationUtils.getDatabaseUserName(conf));
        basicDataSource.setPassword(ConfigurationUtils.getDatabasePassword(conf));


        //Set connection pool properties

        if (ConfigurationUtils.getWso2CarbonDataSourceName(conf) != null) {
            String defaultAutoCommit = ConfigurationUtils.isDefaultAutoCommit(conf);
            if (defaultAutoCommit != null) {
                basicDataSource.setDefaultAutoCommit(Boolean.parseBoolean(defaultAutoCommit));
            }

            String defaultReadOnly = ConfigurationUtils.isDefaultReadOnly(conf);
            if (defaultReadOnly != null) {
                basicDataSource.setDefaultReadOnly(Boolean.parseBoolean(defaultReadOnly));
            }

            String defaultCatalog = ConfigurationUtils.getDefaultCatalog(conf);
            if (defaultCatalog != null) {
                basicDataSource.setDefaultCatalog(defaultCatalog);
            }

            String defaultTransactionIsolation = ConfigurationUtils.getDefaultTransactionIsolation(conf);
            if (defaultTransactionIsolation != null) {
                basicDataSource.setDefaultTransactionIsolation(Integer.parseInt(defaultTransactionIsolation));
            }

            String testOnBorrow = ConfigurationUtils.isTestOnBorrow(conf);
            if (testOnBorrow != null) {
                basicDataSource.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
            }

            String testOnReturn = ConfigurationUtils.isTestOnReturn(conf);
            if (testOnReturn != null) {
                basicDataSource.setTestOnReturn(Boolean.parseBoolean(testOnReturn));
            }

            String timeBetweenEvictionRunsMillis = ConfigurationUtils.getTimeBetweenEvictionRunsMillis(conf);
            if (timeBetweenEvictionRunsMillis != null) {
                basicDataSource.setTimeBetweenEvictionRunsMillis(Long.parseLong(timeBetweenEvictionRunsMillis));
            }

            String numTestsPerEvictionRun = ConfigurationUtils.getNumTestsPerEvictionRun(conf);
            if (numTestsPerEvictionRun != null) {
                basicDataSource.setNumTestsPerEvictionRun(Integer.parseInt(numTestsPerEvictionRun));
            }

            String minEvictableIdleTimeMillis = ConfigurationUtils.getMinEvictableIdleTimeMillis(conf);
            if (minEvictableIdleTimeMillis != null) {
                basicDataSource.setMinEvictableIdleTimeMillis(Long.parseLong(minEvictableIdleTimeMillis));
            }

            String testWhileIdle = ConfigurationUtils.isTestWhileIdle(conf);
            if (testWhileIdle != null) {
                basicDataSource.setTestWhileIdle(Boolean.parseBoolean(testWhileIdle));
            }

            String validationQuery = ConfigurationUtils.getValidationQuery(conf);
            if (validationQuery != null) {
                basicDataSource.setValidationQuery(validationQuery);
            }

            String maxActive = ConfigurationUtils.getMaxActive(conf);
            if (maxActive != null) {
                basicDataSource.setMaxActive(Integer.parseInt(maxActive));
            }

            String maxIdle = ConfigurationUtils.getMaxIdle(conf);
            if (maxIdle != null) {
                basicDataSource.setMaxIdle(Integer.parseInt(maxIdle));
            }

            String maxWait = ConfigurationUtils.getMaxWait(conf);
            if (maxWait != null) {
                basicDataSource.setMaxWait(Long.parseLong(maxWait));
            }

            String minIdle = ConfigurationUtils.getMinIdle(conf);
            if (minIdle != null) {
                basicDataSource.setMinIdle(Integer.parseInt(minIdle));
            }

            String initialSize = ConfigurationUtils.getInitialSize(conf);
            if (initialSize != null) {
                basicDataSource.setInitialSize(Integer.parseInt(initialSize));
            }

            String accessToUnderlyingConnectionAllowed = ConfigurationUtils.isAccessToUnderlyingConnectionAllowed(conf);
            if (accessToUnderlyingConnectionAllowed != null) {
                basicDataSource.setAccessToUnderlyingConnectionAllowed(Boolean.parseBoolean(
                        accessToUnderlyingConnectionAllowed));
            }

            String removeAbandoned = ConfigurationUtils.isRemoveAbandoned(conf);
            if (removeAbandoned != null) {
                basicDataSource.setRemoveAbandoned(Boolean.parseBoolean(removeAbandoned));
            }

            String removeAbandonedTimeout = ConfigurationUtils.getRemoveAbandonedTimeout(conf);
            if (removeAbandonedTimeout != null) {
                basicDataSource.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimeout));
            }

            String logAbandoned = ConfigurationUtils.isLogAbandoned(conf);
            if (logAbandoned != null) {
                basicDataSource.setLogAbandoned(Boolean.parseBoolean(logAbandoned));
            }

        }
        return basicDataSource;
    }

}
