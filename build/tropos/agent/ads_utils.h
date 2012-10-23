#ifndef ADS_UTILS_H
#define ADS_UTILS_H

extern void ads_utils_error(int x, char const *context);
extern void ads_utils_amqp_error(amqp_rpc_reply_t x, char const *context);

extern char *ads_utils_amqp_dump(void const *buffer, size_t len);

#endif /* ADS_UTILS_H */
