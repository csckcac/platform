namespace java org.wso2.carbon.agent.commons.thrift.service

include "Data.thrift"
include "Exception.thrift"

service ThriftEventReceiverService {
    string defineType(1: string streamDefinition) throws (1:Exception.ThriftDifferentTypeDefinitionAlreadyDefinedException ade, 2:Exception.ThriftMalformedTypeDefinitionException mtd, 3:Exception.ThriftTypeDefinitionException tde,4:Exception.ThriftSessionExpiredException se ),
    void publish(1:Data.ThriftEventBundle eventBundle) throws (1:Exception.ThriftUndefinedEventTypeException ue,2:Exception.ThriftSessionExpiredException se),
}