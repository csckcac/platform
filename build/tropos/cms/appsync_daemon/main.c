/* tcpclient.c */

#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <my_global.h>
#include <mysql.h>
#include <string.h>

#include "client_sql.h"
#include "notify.h"

int main(int argc, char *argv[])
{
    char logfile[128] = "/tmp/wso2-openstack.log";
    char appfile[128] = "/tmp/appfile";
    char *svn_repo = NULL;
    char *svn_user = NULL;
    char *svn_pass = NULL;
    char *mysqlserver = NULL;
    char *mysqluser = NULL;
    char *mysqlpass = NULL;
    char *mysqldb = NULL;
    char *repodir = NULL;
    int retries = 5; // Number of retries trying to notify of a change in the repository to the lxc instance
    int duration = 5; // Number of minutes, during which it will be checked, whether the application folder has changed.
    int status = -1;

    if ( argc < 7 )
    {
        printf( "\nusage: %s <mysqlserver> <mysqluser> <mysqlpass> <mysqldb>, <retries>, <duration>, <repodir> \n", argv[0] );
        exit(1);
    } 


    mysqlserver = malloc( strlen( argv[1] ) + 1 );
    strcpy( mysqlserver, argv[1] );
    printf("mysqlserver:%s\n", mysqlserver);
    mysqluser = malloc( strlen( argv[2] ) + 1 );
    strcpy( mysqluser, argv[2] );
    printf("mysqluser:%s\n", mysqluser);
    mysqlpass = malloc( strlen( argv[3] ) + 1 );
    strcpy( mysqlpass, argv[3] );
    mysqldb = malloc( strlen( argv[4] ) + 1 );
    strcpy( mysqldb, argv[4] );
    printf("mysqldb:%s\n", mysqldb);
    repodir = malloc( strlen( argv[5] ) + 1 );
    strcpy( repodir, argv[5] );
    printf("repodir:%s\n", repodir);
    retries = atoi(argv[6]);
    printf("retries:%d\n", retries);
    duration = atoi(argv[7]);
    printf("duration:%d\n", duration);

    status = retrieve_active_instances(logfile, appfile, mysqlserver, mysqluser, mysqlpass, mysqldb, repodir, retries, duration);

    if(mysqlserver) free(mysqlserver);
    if(mysqluser) free(mysqluser);
    if(mysqlpass) free(mysqlpass);
    if(mysqldb) free(mysqldb);
    if(repodir) free(repodir);
    exit(status);
}

