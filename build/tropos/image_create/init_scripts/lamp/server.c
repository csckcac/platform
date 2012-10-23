/* tcpserver.c */

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <time.h>

#define PORT 9330

int main()
{
        time_t now;
        time(&now);

        int sock, connected, bytes_recieved , true = 1;  
        char send_data [1024] , recv_data[1024];       
        char logfile[128] = "/var/log/wso2-openstack.log";
        FILE *logfp = NULL;
        
        logfp = fopen(logfile, "a");

        struct sockaddr_in server_addr,client_addr;    
        int sin_size;
        
        if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            perror("Socket");
            fprintf(logfp, "%s[server.c]  ERROR:Socket error\n", ctime(&now));
            fclose(logfp);
            exit(1);
        }

        if (setsockopt(sock,SOL_SOCKET,SO_REUSEADDR,&true,sizeof(int)) == -1) {
            perror("Setsockopt");
            fprintf(logfp, "%s[server.c] ERROR:Set socketopt error\n", ctime(&now));
            fclose(logfp);
            exit(1);
        }
        
        server_addr.sin_family = AF_INET;         
        server_addr.sin_port = htons(PORT);     
        server_addr.sin_addr.s_addr = INADDR_ANY; 
        bzero(&(server_addr.sin_zero),8); 

        if (bind(sock, (struct sockaddr *)&server_addr, sizeof(struct sockaddr))
                                                                       == -1) {
            perror("Unable to bind");
            fprintf(logfp, "%s[server.c]  ERROR:Unable to bind to socket\n", ctime(&now));
            fclose(logfp);
            exit(1);
        }

        if (listen(sock, 5) == -1) {
            perror("Listen");
            fprintf(logfp, "%s[server.c]  ERROR:Socket listen error\n", ctime(&now));
            fclose(logfp);
            exit(1);
        }
        
        printf("\nTCPServer Waiting for client on port %d \n", PORT);
        fprintf(logfp, "%s[server.c]  INFO:TCP server waiting for client on port %d \n", ctime(&now), PORT);
        fflush(stdout);


        while(1)
        {  

            sin_size = sizeof(struct sockaddr_in);

            connected = accept(sock, (struct sockaddr *)&client_addr,&sin_size);

            printf("\n Got a connection from (%s , %d) \n",
                    inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));
            fprintf(logfp, "%s[server.c]  INFO:Got a connection from (%s , %d) \n", 
                    ctime(&now), inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));

            while (1)
            {
                bytes_recieved = recv(connected,recv_data,1024,0);
                recv_data[bytes_recieved] = '\0';
                if(recv_data && strncmp("", recv_data, 2) != 0) {
                    printf("\n received_data = %s \n" , recv_data);
                    if(strcmp(recv_data, "changed") == 0)
                    {
                        runsvncommand(logfp);
                    }
                    fflush(stdout);
                    sprintf(send_data, "%s", "successful");
                    send(connected, send_data,strlen(send_data), 0);  
                }
                else {
                    break;
                }
              
            }
        }       

      fclose(logfp);
      close(sock);
      return 0;
} 

int runsvncommand(FILE *logfp)
{
    pid_t child_pid;
    int child_status;
    time_t now;
    time(&now);

    child_pid = fork();
    if(child_pid < 0) 
    {
        printf("*** ERROR: forking child process failed\n");
        fprintf(logfp, "%s[server.c] ERROR: Forking child process failed \n", ctime(&now));
        fclose(logfp);
        exit(1);
    }
    if(child_pid == 0) {
        /* This is done by the child process. */
        if(execvp("/opt/svn_client_y.sh", NULL) < 0) 
        {
            printf("*** ERROR: exec failed\n");
            fprintf(logfp, "%s[server.c] ERROR: child process execution failed \n", ctime(&now));
            fclose(logfp);
            exit(1);
        }
        fprintf(logfp, "%s[server.c] INFO: Executed child process \n", ctime(&now));
        exit(0);
    }
}

