#gcc client.c -o client `mysql_config --cflags --libs`
gcc client_sql.c notify.c main.c -g -o client `mysql_config --cflags --libs`
