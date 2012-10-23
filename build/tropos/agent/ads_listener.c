#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <stdint.h>
#include <amqp.h>
#include <amqp_framing.h>

#include <assert.h>

#include "ads_utils.h"

int main(int argc, char const * const *argv) {
    char const *hostname;
    int port;
    char const *queuename;
    char const *amqp_user;
    char const *amqp_passwd;
    amqp_frame_t frame;
    int result;
    char *ret = NULL;

    amqp_basic_deliver_t *d;
    amqp_basic_properties_t *p;
    size_t body_target;
    size_t body_received;

    int sockfd;
    amqp_connection_state_t conn;

    if (argc < 6) {
        fprintf(stderr, "Usage: amqp_listenq host port queuename user password\n");
        return 1;
    }

    hostname = argv[1];
    port = atoi(argv[2]);
    queuename = argv[3];
    amqp_user = argv[4];
    amqp_passwd = argv[5];

    conn = amqp_new_connection();

    ads_utils_error(sockfd = amqp_open_socket(hostname, port), "Opening socket");
    amqp_set_sockfd(conn, sockfd);
    ads_utils_amqp_error(amqp_login(conn, "/", 0, 131072, 0, AMQP_SASL_METHOD_PLAIN, amqp_user, 
                amqp_passwd), "Logging in");
    amqp_channel_open(conn, 1);
    ads_utils_amqp_error(amqp_get_rpc_reply(conn), "Opening channel");

    amqp_basic_consume(conn, 1, amqp_cstring_bytes(queuename), AMQP_EMPTY_BYTES, 0, 0, 0, AMQP_EMPTY_TABLE);
    ads_utils_amqp_error(amqp_get_rpc_reply(conn), "Consuming");

    amqp_maybe_release_buffers(conn);
    result = amqp_simple_wait_frame(conn, &frame);
    if (result < 0)
        exit(0);

    if (frame.frame_type != AMQP_FRAME_METHOD) 
        exit(0);

    if (frame.payload.method.id != AMQP_BASIC_DELIVER_METHOD)
    exit(0);

    d = (amqp_basic_deliver_t *) frame.payload.method.decoded;

    result = amqp_simple_wait_frame(conn, &frame);
    if (result < 0)
        exit(0);

    if (frame.frame_type != AMQP_FRAME_HEADER) {
        fprintf(stderr, "Expected header!");
        return 1;
    }
    p = (amqp_basic_properties_t *) frame.payload.properties.decoded;

    body_target = frame.payload.properties.body_size;
    body_received = 0;

    while (body_received < body_target) {
        result = amqp_simple_wait_frame(conn, &frame);
        if (result < 0)
            break;

        if (frame.frame_type != AMQP_FRAME_BODY) {
            fprintf(stderr, "Expected body!");
            return 1;
        }

        body_received += frame.payload.body_fragment.len;
        assert(body_received <= body_target);

        ret = ads_utils_amqp_dump(frame.payload.body_fragment.bytes,
        frame.payload.body_fragment.len);
        printf("%s\n", ret);
        free(ret);
    }

    if (body_received != body_target) {
        /* Can only happen when amqp_simple_wait_frame returns <= 0 */
        /* We break here to close the connection */
        exit(0);
    }

    amqp_basic_ack(conn, 1, d->delivery_tag, 0);

    ads_utils_amqp_error(amqp_channel_close(conn, 1, AMQP_REPLY_SUCCESS), "Closing channel");
    ads_utils_amqp_error(amqp_connection_close(conn, AMQP_REPLY_SUCCESS), "Closing connection");
    ads_utils_error(amqp_destroy_connection(conn), "Ending connection");

    return 0;
}
