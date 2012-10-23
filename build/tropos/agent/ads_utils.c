#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include <stdint.h>
#include <amqp.h>
#include <amqp_framing.h>

#include "ads_utils.h"

void ads_utils_error(int x, char const *context) {
    if (x < 0) {
        char *errstr = amqp_error_string(-x);
        fprintf(stderr, "%s: %s\n", context, errstr);
        free(errstr);
        exit(1);
  }
}

void ads_utils_amqp_error(amqp_rpc_reply_t x, char const *context) {
    switch (x.reply_type) {
        case AMQP_RESPONSE_NORMAL:
            return;

        case AMQP_RESPONSE_NONE:
            fprintf(stderr, "%s: missing RPC reply type!\n", context);
            break;

        case AMQP_RESPONSE_LIBRARY_EXCEPTION:
            fprintf(stderr, "%s: %s\n", context, amqp_error_string(x.library_error));
            break;

        case AMQP_RESPONSE_SERVER_EXCEPTION:
            switch (x.reply.id) {
                case AMQP_CONNECTION_CLOSE_METHOD: {
                    amqp_connection_close_t *m = (amqp_connection_close_t *) x.reply.decoded;
                    fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
                        context,
                        m->reply_code,
                        (int) m->reply_text.len, (char *) m->reply_text.bytes);
                    break;
                }
                case AMQP_CHANNEL_CLOSE_METHOD: {
                    amqp_channel_close_t *m = (amqp_channel_close_t *) x.reply.decoded;
                    fprintf(stderr, "%s: server channel error %d, message: %.*s\n",
                        context,
                        m->reply_code,
                        (int) m->reply_text.len, (char *) m->reply_text.bytes);
                    break;
                }
                default:
                fprintf(stderr, "%s: unknown server error, method id 0x%08X\n", context, x.reply.id);
                break;
            }
            break;
    }

    exit(1);
}

static char * dump_row(long count, int numinrow, int *chs) {
    int i;
    char *buf = NULL;
    char *tempbuf = NULL;
    int len = 0;
    if (numinrow > 0) {
        for (i = 0; i < numinrow; i++) {
           if(buf) {
                len = strlen(buf);
                tempbuf = strdup(buf);
                free(buf);
                buf = malloc(len + sizeof(int));
            } else {
                buf = malloc(sizeof(int));
            }
            if (isprint(chs[i])) {
                if(tempbuf) {
                    sprintf(buf, "%s%c", tempbuf, chs[i]);
                    free(tempbuf);
                } else
                    sprintf(buf, "%c", chs[i]);
            }
        }
    }
    return buf;
}

char *ads_utils_amqp_dump(void const *buffer, size_t len) {
    char *result = NULL;
    unsigned char *buf = (unsigned char *) buffer;
    long count = 0;
    int numinrow = 0;
    int chs[16];
    int oldchs[16] = {0};
    int showed_dots = 0;
    size_t i;

    for (i = 0; i < len; i++) {
        int ch = buf[i];

        count++;
        chs[numinrow++] = ch;
    }

    result = dump_row(count, numinrow, chs);
    return result;
}

