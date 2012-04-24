#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <string.h>

//container_action.sh action=create jail-user=yang jail-password=yang jail-ip=192.168.254.2 jail-mask=255.255.255.0 jail-gateway=192.168.254.1 bridge=br-lxc jail-root=/mnt/lxc template=template-ubuntu-lucid-lamp memory=512M swap=1G cpu-shares=1024 cpuset-cpus=0-7

int main(int argc, char *argv[])
{
    char *stratos_server_path = NULL;
    char *jail_action = NULL;
    char *jail_user = NULL;
    char *jail_password = NULL;
    char *jail_ip = NULL;
    char *jail_mask = NULL;
    char *jail_gateway = NULL;
    char *bridge = NULL;
    char *jail_root = NULL;
    char *keys_file = NULL;
    char *template = NULL;
    char *memory = NULL;
    char *swap = NULL;
    char *cpu_shares = NULL;
    char *cpuset_cpus = NULL;
    char *svn_url = NULL;
    char *svn_dir = NULL;
    char *jail_command = NULL;


    if ( argc < 5 )
    {
        printf( "\nusage: %s <stratos_server_path> <create|destroy|start|stop> <jail user> <jail password> <jail ip> <jail mask> <jail gateway> \
                <bridge> <jail root> <keys file> <template> <memory> <swap> <cpu shares> <cpuset cpus> <svn url> <svn dir>\n", argv[0] );
        exit(1);
    } else {
        stratos_server_path = malloc( strlen( argv[1] ) + 1 );
        strcpy( stratos_server_path, argv[1] );
        jail_action = malloc( strlen( argv[2] ) + 1 );
        strcpy( jail_action, argv[2] );
        jail_user = malloc( strlen( argv[3] ) + 1 );
        strcpy(jail_user, argv[3]);
        printf("action:%s\n", jail_action);
    }
    if (strcmp(jail_action, "create") == 0) {
        if ( argc < 16 ) {
            printf( "\nusage: %s <stratos_server_path> <create> <jail user> <jail password> <jail ip> <jail mask> <apache2 gateway> <bridge> \
                    <jail root> <keys file> <template> <memory> <swap> <cpu shares> <cpuset cpus> <svn url> <svn dir>\n", argv[0] );
            exit(1);
        }
        
        jail_password = malloc( strlen( argv[4] ) + 1 );
        strcpy(jail_password, argv[4]);

        jail_ip = malloc( strlen( argv[5] ) + 1 );
        strcpy( jail_ip, argv[5] );
        
        jail_mask = malloc( strlen( argv[6] ) + 1 );
        strcpy( jail_mask, argv[6] );
        
        jail_gateway = malloc( strlen( argv[7] ) + 1 );
        strcpy( jail_gateway, argv[7] );

        bridge = malloc( strlen( argv[8] ) + 1 );
        strcpy(bridge, argv[8]);
        
        jail_root = malloc( strlen( argv[9] ) + 1 );
        strcpy(jail_root, argv[9]);
        
        keys_file = malloc( strlen( argv[10] ) + 1 );
        strcpy(keys_file, argv[10]);

        template = malloc( strlen( argv[11] ) + 1 );
        strcpy(template, argv[11]);

        memory = malloc( strlen( argv[12] ) + 1 );
        strcpy(memory, argv[12]);

        swap = malloc( strlen( argv[13] ) + 1 );
        strcpy(swap, argv[13]);

        cpu_shares = malloc( strlen( argv[14] ) + 1 );
        strcpy(cpu_shares, argv[14]);

        cpuset_cpus = malloc( strlen( argv[15] ) + 1 );
        strcpy(cpuset_cpus, argv[15]);
        
        if(argv[16]) {
            svn_url = malloc( strlen( argv[16] ) + 1 );
            strcpy(svn_url, argv[16]);
        }
       
        if(argv[17]) {
            svn_dir = malloc( strlen( argv[17] ) + 1 );
            strcpy(svn_dir, argv[17]);
        }

        jail_command = malloc(2048);
        strcat(jail_command, stratos_server_path);
        strcat(jail_command, "/container_action.sh action=create");
        strcat(jail_command, " jail-user=");
        strcat(jail_command, jail_user);
        strcat(jail_command, " jail-password=");
        strcat(jail_command, jail_password);
        strcat(jail_command, " jail-ip=");
        strcat(jail_command, jail_ip);
        strcat(jail_command, " jail-mask=");
        strcat(jail_command, jail_mask);
        strcat(jail_command, " jail-gateway=");
        strcat(jail_command, jail_gateway);
        strcat(jail_command, " bridge=");
        strcat(jail_command, bridge);
        strcat(jail_command, " jail-root=");
        strcat(jail_command, jail_root);
        strcat(jail_command, " jail-keys-file=");
        strcat(jail_command, keys_file);
        strcat(jail_command, " template=");
        strcat(jail_command, template);
        strcat(jail_command, " memory=");
        strcat(jail_command, memory);
        strcat(jail_command, " swap=");
        strcat(jail_command, swap);
        strcat(jail_command, " cpu-shares=");
        strcat(jail_command, cpu_shares);
        strcat(jail_command, " cpuset-cpus=");
        strcat(jail_command, cpuset_cpus);
        if(svn_url) {
            strcat(jail_command, " svn-url=");
            strcat(jail_command, svn_url);
        }
        if(svn_dir) {
            strcat(jail_command, " svn-dir=");
            strcat(jail_command, svn_dir);
        }

    } else if(strcmp(jail_action, "destroy") == 0) {
        jail_root = malloc( strlen( argv[4] ) + 1 );
        strcpy(jail_root, argv[4]);
        jail_command = malloc(1024);
        strcat(jail_command, stratos_server_path);
        strcat(jail_command, "/container_action.sh action=destroy");
        strcat(jail_command, " jail-user=");
        strcat(jail_command, jail_user);
        strcat(jail_command, " jail-root=");
        strcat(jail_command, jail_root);

    } else if(strcmp(jail_action, "start") == 0) {
        jail_root = malloc( strlen( argv[4] ) + 1 );
        strcpy(jail_root, argv[4]);
        jail_command = malloc(1024);
        strcat(jail_command, stratos_server_path);
        strcat(jail_command, "/container_action.sh action=start");
        strcat(jail_command, " jail-user=");
        strcat(jail_command, jail_user);
        strcat(jail_command, " jail-root=");
        strcat(jail_command, jail_root);
    
    } else if(strcmp(jail_action, "stop") == 0) {
        jail_root = malloc( strlen( argv[4] ) + 1 );
        strcpy(jail_root, argv[4]);
        jail_command = malloc(1024);
        strcat(jail_command, stratos_server_path);
        strcat(jail_command, "/container_action.sh action=stop");
        strcat(jail_command, " jail-user=");
        strcat(jail_command, jail_user);
        strcat(jail_command, " jail-root=");
        strcat(jail_command, jail_root);
    }

    printf("jail_command:\n%s\n", jail_command);
    setuid( 0 );
    system(jail_command);
    /*FILE *fd = fopen ("/tmp/temp", "wt");
    fprintf (fd, "%s\n", jail_command);
    fprintf (fd, "%s\n", "**********************done*******************************");
    fclose(fd);*/

    if(stratos_server_path) free(stratos_server_path);
    if(jail_action) free(jail_action);
    if(jail_user) free(jail_user);
    if(jail_password) free(jail_password);
    if(jail_ip) free(jail_ip);
    if(jail_mask) free(jail_mask);
    if(jail_gateway) free(jail_gateway);
    if(jail_root) free(jail_root);
    if(keys_file) free(keys_file);
    if(template) free(template);
    if(memory) free(memory);
    if(swap) free(swap);
    if(cpu_shares) free(cpu_shares);
    if(cpuset_cpus) free(cpuset_cpus);
    if(svn_url) free(svn_url);
    if(svn_dir) free(svn_dir);
    if(jail_command) free(jail_command);

    return 0;
}
