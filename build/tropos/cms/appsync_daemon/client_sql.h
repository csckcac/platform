#ifndef CLIENT_SQL_H
#define CLIENT_SQL_H

#include <my_global.h>
#include <mysql.h>
#include <string.h>

int retrieve_active_instances(char *logfile, char *appfile, char *mysqlserver, char *mysqluser, 
        char *mysqlpass, char *mysqldb, char *repodir, int retries, int duration);

#endif                          /* CLIENT_SQL_H */
