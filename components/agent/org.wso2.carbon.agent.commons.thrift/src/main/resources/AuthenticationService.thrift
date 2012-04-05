namespace java org.wso2.carbon.agent.commons.thrift.authentication.service
include "AuthenticationException.thrift"

service ThriftAuthenticatorService {
   string connect(1:string userName, 2:string password) throws
                                           (1:AuthenticationException.ThriftAuthenticationException ae)

   void disconnect(1:string sessionId)
}