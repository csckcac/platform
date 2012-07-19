
CREATE TABLE RSS_INSTANCE (
  name VARCHAR(128) NOT NULL,
  server_url VARCHAR(1024) NOT NULL,
  dbms_type VARCHAR(128) NOT NULL,
  instance_type VARCHAR(128) NOT NULL,
  server_category VARCHAR(128) NOT NULL,
  admin_username VARCHAR(128),
  admin_password VARCHAR(128),
  tenant_id INTEGER NOT NULL,
  UNIQUE (name, tenant_id),
  PRIMARY KEY (name)
);

CREATE TABLE RSS_DATABASE (
  name VARCHAR(128) NOT NULL,
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  UNIQUE (name, rss_instance_name),
  PRIMARY KEY (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RSS_INSTANCE (name)
);

CREATE TABLE RSS_DATABASE_USER (
  username VARCHAR(16) NOT NULL,
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  UNIQUE (username, rss_instance_name, tenant_id),
  PRIMARY KEY (username),
  FOREIGN KEY (rss_instance_name) REFERENCES RSS_INSTANCE (name)
);

CREATE TABLE RSS_DATABASE_PROPERTY (
  name VARCHAR(128) NOT NULL,
  value TEXT,
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  UNIQUE (name, database_name, rss_instance_name),
  PRIMARY KEY (name),
  FOREIGN KEY (database_name) REFERENCES RSS_DATABASE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RSS_INSTANCE (name)
);

CREATE TABLE RSS_USER_DATABASE_ENTRY (
  username VARCHAR(16),
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  PRIMARY KEY (username, database_name, rss_instance_name),
  FOREIGN KEY (username) REFERENCES RSS_DATABASE_USER (username),
  FOREIGN KEY (database_name) REFERENCES RSS_DATABASE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RSS_INSTANCE (name)
);

CREATE TABLE RSS_USER_DATABASE_PERMISSION (
  username VARCHAR(16),
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  perm_name VARCHAR(128) NOT NULL,
  perm_value VARCHAR(128),
  PRIMARY KEY (username, database_name, perm_name),
  FOREIGN KEY (username) REFERENCES RSS_DATABASE_USER (username),
  FOREIGN KEY (database_name) REFERENCES RSS_DATABASE_INSTANCE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RSS_INSTANCE (name)
  
);

CREATE TABLE RSS_SYSTEM_DATABASE_COUNT (
  count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE RSS_DATABASE_PRIVILEGE_TEMPLATE (
  name VARCHAR(128),
  tenant_id INTEGER,
  PRIMARY KEY (name, tenant_id)
);

CREATE TABLE RSS_DATABASE_PRIVILEGE_TEMPLATE_ENTRY (
  template_name VARCHAR(128),
  perm_name VARCHAR(128),
  perm_value CHAR(1),
  PRIMARY KEY (template_name, perm_name),
  FOREIGN KEY (template_name) REFERENCES RSS_DATABASE_PRIVILEGE_TEMPLATE (name)
);
