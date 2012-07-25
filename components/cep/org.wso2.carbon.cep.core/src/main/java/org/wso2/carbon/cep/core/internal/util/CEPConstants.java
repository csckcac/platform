/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.core.internal.util;

public interface CEPConstants {

    String CEP_CONF = "cep-config.xml";

    // config file elements
    String CEP_CONF_NAMESPACE = "http://wso2.org/carbon/cep";
    String CEP_CONF_ELE_ROOT = "cepConfiguration";
    String CEP_CONF_ELE_CEP_ENGINE_PROVIDER = "cepEngineProvider";
    String CEP_CONF_ELE_CEP_BUCKET_OWNER = "owner";
    String CEP_CONF_ELE_CEP_ENGINE_PROVIDERS = "cepEngineProviders";
    String CEP_CONF_ELE_DESCRIPTION = "description";
    String CEP_CONF_ELE_PROVIDER_CONFIG = "engineProviderConfiguration";
    String CEP_CONF_ELE_BUCKET = "bucket";
    String CEP_CONF_ELE_BUCKETS = "buckets";
    String CEP_CONF_ELE_QUERY = "query";
    String CEP_CONF_ELE_NAME = "name";
    String CEP_CONF_ELE_TYPE = "type";
    String CEP_CONF_ELE_XPATH = "xpath";
    String CEP_CONF_ELE_TOPIC = "topic";
    String CEP_CONF_ELE_BROKER_NAME = "brokerName";
    String CEP_CONF_ELE_INPUT = "input";
    String CEP_CONF_ELE_MAPPING = "mapping";
    String CEP_CONF_ELE_XPATH_DEFINITON = "xpathDefinition";
    String CEP_CONF_ELE_PROPERTY = "property";
    String CEP_CONF_ELE_EXPRESSION = "expression";
    String CEP_CONF_ELE_OUTPUT = "output";
    String CEP_CONF_ELE_EMAPPING = "elementMapping";
    String CEP_CONF_ELE_XML_MAPPING = "xmlMapping";
    String CEP_CONF_ELE_TUPLE_MAPPING = "tupleMapping";
    String CEP_CONF_ELE_MAP_MAPPING = "mapMapping";
    String CE_CONF_ELE_XML_FIELD_NAME = "xmlFieldName";
    String CE_CONF_ELE_XML_FIELD_TYPE = "xmlFieldType";
    String CEP_CONF_ELE_CEP_BUCKETS = "CEPBuckets";
    String CEP_CONF_ELE_TUPLE_DATA_TYPE ="dataType" ;
    String CEP_CONF_ELE_TUPLE_DATA_TYPE_META ="metaData" ;
    String CEP_CONF_ELE_TUPLE_DATA_TYPE_CORRELATION ="correlationData" ;
    String CEP_CONF_ELE_TUPLE_DATA_TYPE_PAYLOAD ="payloadData" ;
    String CEP_CONF_ATTR_EVENT_CLASS = "eventClass";
    String CEP_CONF_ATTR_NAME = "name";
    String CEP_CONF_ATTR_ENGINE_PROVIDER = "engineProvider";
    String CEP_CONF_ATTR_NAMESPACE = "namespace";
    String CEP_CONF_ATTR_PREFIX = "prefix";
    String CEP_CONF_ATTR_TOPIC = "topic";
    String CEP_CONF_ATTR_DEFAULT = "default";
    String CEP_CONF_ATTR_STREAM = "stream";
    String CEP_CONT_ATTR_DOC_ELEMENT = "documentElement";
    String CEP_CONT_ATTR_NAME = "name";
    String CEP_CONT_ATTR_TYPE = "type";
    String CEP_CONT_ATTR_LISTENER_NAME = "listenerName";
    String CEP_CONF_ATTR_OVER_WRITE_REGISTRY = "overWriteRegistryStoredBucket";
    String CEP_CONF_CLASS_NAME_TUPLE = "Tuple";
    String CEP_CONF_CLASS_NAME_MAP = "Map";
    String CEP_CONF_CEP_NAME_SPACE_PREFIX="cep";


    String CEP_CONF_WS_PROP_URI = "uri";
    String CEP_CONF_WS_PROP_USERNAME = "username";
    String CEP_CONF_WS_PROP_PASSWORD = "password";

    String CEP_BROKER_TYPE_WS = "ws";
    String CEP_BROKER_TYPE_JMS = "jms";


    String CEP_EVENT_DESCRIPTION_TYPE_OMELEMENT = "omelement";
    String CEP_EVENT_DESCRIPTION_TYPE_POJO = "pojo";

    String CEP_CONF_XML_FIELD_TYPE_ELEMENT = "element";
    String CEP_CONF_XML_FIELD_TYPE_ATTRIBUTE = "attribute";

    String CEP_CONF_EXPRESSION_INLINE = "inline";
    String CEP_CONF_EXPRESSION_REGISTRY = "registry";

    String CEP_REGISTRY_PROPERTIES = "properties";
    String CEP_REGISTRY_INPUTS = "inputs";
    String CEP_REGISTRY_DETAILS = "details";
    String CEP_REGISTRY_MAPPING = "mapping";
    String CEP_REGISTRY_MAPPING_XML = "xml";
    String CEP_REGISTRY_MAPPING_TUPLE = "tuple";
    String CEP_REGISTRY_MAPPING_MAP = "map";
    String CEP_REGISTRY_STREAM = "stream";
    String CEP_REGISTRY_NAME = "name";
    String CEP_REGISTRY_TYPE = "type";
    String CEP_REGISTRY_PROVIDER_CONFIG = "engineProviderConfiguration";
    String CEP_REGISTRY_POSITION = "position";
    String CEP_REGISTRY_LISTENER_NAME = "listenerName";
    String CEP_REGISTRY_XPATH = "xpath";
    String CEP_REGISTRY_XPATH_DEFS = "xpathDefinitions";
    String CEP_REGISTRY_QUERIES = "queries";
    String CEP_REGISTRY_EXPRESSION = "expression";
    String CEP_REGISTRY_OUTPUT = "output";
    String CEP_REGISTRY_TOPIC = "topic";
    String CEP_REGISTRY_XML_MAPPING = "xmlMapping";
    String CEP_REGISTRY_ELEMENT_MAPPING = "elementMapping";
    String CEP_REGISTRY_DOC_ELEMENT = "documentElement";
    String CEP_REGISTRY_NS = "namespace";
    String CEP_REGISTRY_XML_FIELD_NAME = "xmlFieldName";
    String CEP_REGISTRY_XML_FIELD_TYPE = "xmlFieldType";
    String CEP_REGISTRY_TEXT = "text";
    String CEP_REGISTRY_BS = "/";
    String CEP_REGISTRY_KEY = "key";
    String CEP_REGISTRY_VALUE = "value";
    String CEP_REGISTRY_QUERY_INDEX = "queryIndex";
    String CEP_REGISTRY_EVENT_CLASS = "eventClass";
    String CEP_REGISTRY_DATA_TYPE = "dataType";
    String CEP_REGISTRY_MAP_MAPPING = "mapMapping";
    String CEP_REGISTRY_TUPLE_MAPPING = "tupleMapping";
    String CEP_REGISTRY_TUPLE_MAPPING_META = "metaData";
    String CEP_REGISTRY_TUPLE_MAPPING_CORRELATION = "correlationData";
    String CEP_REGISTRY_TUPLE_MAPPING_PAYLOAD = "payloadData";
    String CEP_REGISTRY_EXPRESSION_LOOK_UP_KEY="expressionLookUpKey";
    
    
}
