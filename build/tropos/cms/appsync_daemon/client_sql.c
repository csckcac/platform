#include <my_global.h>
#include <mysql.h>
#include <string.h>
#include "client_sql.h"
#include "notify.h"

int retrieve_active_instances(char *logfile, char *appfile, char *mysqlserver, char *mysqluser, 
        char *mysqlpass, char *mysqldb, char *repodir, int retries, int duration)
{


    MYSQL *conn;
    MYSQL_RES *result;
    MYSQL_ROW row;
    int i;
    char query[1024];
    FILE *logfp = NULL;

    logfp = fopen(logfile, "a");
    fprintf(logfp, "[client.c] INFO: entry: retrieve_active_instances");

    conn = mysql_init(NULL);
    //mysql_real_connect(conn, "172.20.0.1", "wso2", "openstack", "nova", 0, NULL, 0);
    mysql_real_connect(conn, mysqlserver, mysqluser, mysqlpass, mysqldb, 0, NULL, 0);

    sprintf(query, "SELECT hostname, floating_ips.address FROM instances, fixed_ips, floating_ips WHERE "\
            "fixed_ips.id=floating_ips.fixed_ip_id AND "\
            "fixed_ips.instance_id=instances.id AND instances.vm_state='active';");

    printf("query:%s\n", query);
    fprintf(logfp, "[client.c] INFO: entry: query:%s\n", query);
    mysql_query(conn, query);
    result = mysql_store_result(conn);

    if(result == NULL) 
    {
        printf("result is empty\n");
        mysql_close(conn);
        fclose(logfp);
        exit(1);
    }

    while ((row = mysql_fetch_row(result)))
    {
        char *appdir = NULL;
        int i = 0, j = 0;
        int toksize = 0;
        char *token[5];
        int lines = 0;
        FILE *appfp = NULL;
        char buf[512];
        char *findcommand = NULL;
        pid_t child_pid;

        for(j = 0; j < 5; j++) {
            token[j] = NULL;
        }
        
        printf("hostname:%s\n", row[0]);
        printf("hostip:%s\n", row[1]);
        //String to be tokenized is as this wso2-php-domain-tenantname-693
        token[0] = strtok(row[0], "-"); //get pointer to first token found and store in 0
                                           //place in array
        while(token[i] != NULL) {   //ensure a pointer was found
            i++;
            token[i] = strtok(NULL, "-"); //continue to tokenize the string
        }
        if(token[3] == NULL) {
            continue;
        }
       
        toksize = strlen(repodir) + strlen(token[1]) + strlen(token[3]);
        appdir = malloc(toksize + 3);
        sprintf(appdir, "%s/%s/%s", repodir, token[3], token[1]);
        printf("appdir:%s\n", appdir);

        
        char *failfile = NULL;
        FILE *failfp = NULL;
        int *y = NULL;
        failfile = malloc(strlen(token[1]) + strlen(token[3]) + strlen(row[1]) + 8);
        sprintf(failfile, "/tmp/%s-%s-%s", token[3], token[1], row[1]);
        failfp = fopen(failfile, "rb");
        if(failfp != NULL)
        {
            void *ptr = malloc(sizeof(int));

            fread(ptr, sizeof(int), 1, failfp);
            y = (int *) ptr;
            (*y)++;
            printf("came1 y:%d\n", *y);
            if(*y > 0)
            {
                duration = (*y) * duration;
                printf("duration:%d\n", duration);
            }
            if(ptr) free(ptr);
            fclose(failfp);
        }
        if(failfile) free(failfile);

        findcommand = malloc(strlen(appdir) + strlen(appfile) + 64);
        sprintf(findcommand, "find %s -type d -mmin -%d|grep -v .svn > %s", appdir, duration, appfile);
        if(appdir) free(appdir);
        printf("findcommand:%s\n", findcommand);
        fprintf(logfp, "[client.c] INFO: findcommand:%s\n", findcommand);
        system(findcommand);
        if(findcommand) free(findcommand);
        appfp = fopen(appfile, "rw");
        buf == NULL;
        lines = 0;
        if(appfp != NULL)
        {
            if (fgets(buf,sizeof(buf), appfp) != NULL)
            {
                lines++;
            }
            fclose(appfp);
        }
        if(lines > 0)
        {
            printf("repo changed\n");
            fprintf(logfp, "[client.c] INFO: repo changed. call notify\n");
            child_pid = fork();
            if(child_pid < 0) 
            {
                printf("ERROR: forking child process failed\n");
                fprintf(logfp, "[client.c] ERROR: forking child process failed\n");
                fclose(logfp);
                exit(1);
            }
            if(child_pid == 0) {
                char *hostip = NULL;
                char *statfile = NULL;
                char *logfile_c = NULL;
                FILE *logptr = NULL;
                int status = -1;

                sleep(10); // Wait till svn folder is synchronized
                hostip = strdup(row[1]);
                statfile = malloc(strlen(token[1]) + strlen(token[3]) + strlen(hostip) + 8);
                sprintf(statfile, "/tmp/%s-%s-%s", token[3], token[1], hostip);
                logfile_c = malloc(strlen(token[1]) + strlen(token[3]) + strlen(hostip) + 18);
                sprintf(logfile_c, "/tmp/wso2-log-%s-%s-%s", token[3], token[1], hostip);
                logptr = fopen(logfile_c, "a");
              
                status = notify(logptr, statfile, hostip, retries);
                if(hostip) free(hostip);
                if(statfile) free(statfile);
                if(logfile_c) free(logfile_c);
                fprintf(logptr, "[client.c] INFO:Return status of retrieve_ips:%d\n", status);
                fclose(logptr);
            }
        }
        else
        {
            printf("repo has no change\n");
            fprintf(logfp, "[client.c] INFO: repo has no change\n");
        }
    }

    fclose(logfp);
    mysql_free_result(result);
    mysql_close(conn);

}


/*int main(void)
{
    char logfile[128] = "/tmp/wso2-openstack.log";
    char appfile[80] = "/tmp/appfile";
    int retries = 5;
    char repodir[128] = "/opt/wso2stratos-cms-1.0.0-SNAPSHOT/repository/deployment/server";
        
    int status = retrieve_active_instances(logfile, appfile, "172.20.0.1", "wso2", "openstack", "nova", repodir, 5, 5);
}*/
