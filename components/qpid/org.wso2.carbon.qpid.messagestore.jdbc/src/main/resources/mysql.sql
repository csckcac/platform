CREATE TABLE  IF NOT EXISTS QPID_DB_VERSION (
              version int not null ) ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_EXCHANGE (
              name varchar(255) not null,
              type varchar(255) not null,
              autodelete SMALLINT not null,
              PRIMARY KEY ( name )
              ) ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_QUEUE (
              name varchar(255) not null,
              owner varchar(255),
              exclusive SMALLINT not null,
              arguments blob,
              PRIMARY KEY ( name )
              ) ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_BINDINGS (
              exchange_name varchar(255) not null,
              queue_name varchar(255) not null,
              binding_key varchar(255) not null,
              arguments blob ,
              PRIMARY KEY ( exchange_name, queue_name, binding_key )
              )ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_QUEUE_ENTRY (
              queue_name varchar(255) not null,
              message_id bigint not null,
              PRIMARY KEY (queue_name, message_id)
              )ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_META_DATA (
              message_id bigint not null,
              meta_data blob, PRIMARY KEY ( message_id )
              )ENGINE INNODB;

CREATE TABLE IF NOT EXISTS QPID_MESSAGE_CONTENT (
              message_id bigint not null,
              offset int not null,
              last_byte int not null,
              content blob ,
              PRIMARY KEY (message_id, offset)
              )ENGINE INNODB;

INSERT INTO QPID_DB_VERSION ( version ) VALUES ( 3 );