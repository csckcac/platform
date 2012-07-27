set logs_column_family = ? ;
set file_path= ? ;
drop table LogStats;
set mapred.output.compress=true;
set hive.exec.compress.output=true;
set mapred.output.compression.codec=org.apache.hadoop.io.compress.GzipCodec;
set io.compression.codecs=org.apache.hadoop.io.compress.GzipCodec;

CREATE EXTERNAL TABLE IF NOT EXISTS LogStats (key STRING,
	payload_tenantID STRING,payload_serverName 
	STRING,
	payload_message STRING,payload_stacktrace STRING,
	payload_appName STRING,payload_logger 
	STRING,
	payload_priority STRING,payload_logTime BIGINT) STORED BY 
	'org.apache.hadoop.hive.cassandra.CassandraStorageHandler' WITH SERDEPROPERTIES ( "cassandra.host" = 
	"127.0.0.1",
	"cassandra.port" = "9160","cassandra.ks.name" = "EVENT_KS",
	"cassandra.ks.username" 
	= "admin","cassandra.ks.password" = "admin",
	"cassandra.cf.name" = 
	${hiveconf:logs_column_family},
	"cassandra.columns.mapping" = 
	":key,payload_tenantID,
	payload_serverName,payload_appName,payload_message,
	payload_stacktrace,payload_logger,payload_priority,
	payload_logTime" 
	);
INSERT OVERWRITE LOCAL DIRECTORY 'file:///${hiveconf:file_path}' select payload_tenantID,
	payload_serverName,payload_appName,payload_message,
	payload_stacktrace,payload_logger,payload_priority,
	(from_unixtime(payload_logTime,
	'yyyy-MM-dd HH:mm:ss.SSS' )) as logTime from LogStats ORDER BY 
	logTime;

                                    