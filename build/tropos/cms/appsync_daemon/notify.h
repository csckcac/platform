#ifndef NOTIFY_H
#define NOTIFY_H

#include <string.h>

int notify(FILE *logfp, char *statfile, char *hostname, int retries);

#endif                          /* NOTIFY_H */
