#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include "notify.h"

#define PORT 9330
#define SLEEP 5

int notify(FILE *logfp, char *statfile, char *hostname, int retries)

{

    int status=-1;
    int sock, bytes_recieved;  
    char send_data[1024],recv_data[1024];
    struct hostent *host;
    struct sockaddr_in server_addr;  

    host = gethostbyname(hostname);

    fprintf(logfp, "[client.c] INFO:Start notify function");
    while(1==1)
    {
        if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            perror("Socket");
            fprintf(logfp, "[client.c] ERROR:Could not create socket:%s\n", hostname);
            status = 1;
            sleep(SLEEP);
            continue;
        }

        server_addr.sin_family = AF_INET;     
        server_addr.sin_port = htons(PORT);   
        server_addr.sin_addr = *((struct in_addr *)host->h_addr);
        bzero(&(server_addr.sin_zero),8); 

        if (connect(sock, (struct sockaddr *)&server_addr,
                    sizeof(struct sockaddr)) == -1) 
        {
            perror("Connect");
            fprintf(logfp, "[client.c] ERROR:Could not connect to server:%s:%d\n", hostname, PORT);
            status = 1;
            sleep(SLEEP);
            continue;
        }

        sprintf(send_data, "%s", "changed");
        send(sock,send_data,strlen(send_data), 0);   
        
        bytes_recieved=recv(sock,recv_data,1024,0);
        recv_data[bytes_recieved] = '\0';

        if (strcmp(recv_data , "successful") == 0)
        {
            fprintf(logfp, "[client.c] INFO:sent and received success");
            status=0;
            break;
        }
        else
        {
            status=1;
            sleep(SLEEP);
            continue;
        }

    } 

    if(status == 0) 
    {
        FILE *fp = NULL;
        int *y = malloc(sizeof(int));
        *y = 0;
        fp = fopen(statfile, "wb");
        if(fp != NULL)
        {
            fwrite(y, sizeof(int), 1, fp);
            fclose(fp);
        }
        if(y) free(y);
    }
    if(status == 1) 
    {
        FILE *fp = NULL;
        int *y = NULL;
        fp = fopen(statfile, "rb");
        if(fp != NULL)
        {
            void *ptr = malloc(sizeof(int));

            fread(ptr, sizeof(int), 1, fp);
            y = (int *) ptr;
            *y = *y+1;
            fclose(fp);
        }
        if(y == NULL)
        {
            y = malloc(sizeof(int));
            *y = 1;
        }
        fp = fopen(statfile, "wb");
        if(fp != NULL)
        {
            fwrite(y, sizeof(int), 1, fp);
            fclose(fp);
        }
        if(y) free(y);
    }
    close(sock);
    //printf("Return status of notify funtion:%d\n", status);
    fprintf(logfp, "[client.c] INFO:Return status of notify funtion:%d\n", status);
    return status;
}

/*int main(void)
{
    char logfile[128] = "/tmp/wso2-openstack.log";
    char statfile[128] = "/tmp/stats";
    char hostname[64] = "172.20.1.2";
                
    FILE *logfp = NULL;
    logfp = fopen(logfile, "a");
    int status = 0;

    status = notify(logfp, statfile, hostname);
    fclose(logfp);
}*/

