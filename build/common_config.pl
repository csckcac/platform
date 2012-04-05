#!/usr/bin/perl -w

use XML::LibXML;

sub stratos_config_common {
    my $config_file = $_[0];
    my $parser = XML::LibXML->new();

    my $config = $parser->parse_file($config_file);
    my $stratos_dir = $_[1];
    my $stratos_target_dir = $_[2];
    my $dbuser = $_[3];
    my $dbpasswd = $_[4];
    if(defined($stratos_dir)) {
        $common_user_mgt_file = $stratos_dir."/repository/conf/user-mgt.xml";
        $common_axis2_client_file = $stratos_dir."/repository/conf/axis2_client.xml";
        $common_broker_config_file = $stratos_dir."/repository/conf/broker-config.xml";
        $common_cache_file = $stratos_dir."/repository/conf/cache.xml";
        $common_jgroups_s3_ping_aws_file = $stratos_dir."/repository/conf/jgroups-s3_ping-aws.xml";
        $common_cloud_services_desc_file = $stratos_dir."/repository/conf/cloud-services-desc.xml";
        $common_identity_file = $stratos_dir."/repository/conf/identity.xml";
        $common_throttling_agent_config_file = $stratos_dir."/repository/conf/throttling-agent-config.xml";
        $common_mgt_transports_file = $stratos_dir."/repository/conf/mgt-transports.xml";
    }
     

    if(defined($stratos_target_dir)) {
        $common_user_mgt_target_file = $stratos_target_dir."/repository/conf/user-mgt.xml";
        $common_axis2_client_target_file = $stratos_target_dir."/repository/conf/axis2_client.xml";
        $common_broker_config_target_file = $stratos_target_dir."/repository/conf/broker-config.xml";
        $common_cache_target_file = $stratos_target_dir."/repository/conf/cache.xml";
        $common_jgroups_s3_ping_aws_target_file = $stratos_target_dir."/repository/conf/jgroups-s3_ping-aws.xml";
        $common_cloud_services_desc_target_file = $stratos_target_dir."/repository/conf/cloud-services-desc.xml";
        $common_identity_target_file = $stratos_target_dir."/repository/conf/identity.xml";
        $common_throttling_agent_config_target_file = $stratos_target_dir."/repository/conf/throttling-agent-config.xml";
        $common_mgt_transports_target_file = $stratos_target_dir."/repository/conf/mgt-transports.xml";
    }
    #----------------------------------------------------------------------------- Commong Config ----------------------------------------
    #-------------- user-mgt.xml -----------------------------------------------------------------------------------------
    my $common_user_mgt = $parser->parse_file($common_user_mgt_file);
    open COMMON_USER_MGT, ">", $common_user_mgt_target_file or die $!;

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="url"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="url"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="userName"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="userName"]/text()');
        if(defined($node)) {
            if(defined($dbuser)) {
                $node->setData($dbuser);
            } else {
                $node->setData($config_node->getData());
            }
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="password"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="password"]/text()');
        if(defined($node)) {
            if(defined($dbpasswd)) {
                $node->setData($dbpasswd);
            } else {
                $node->setData($config_node->getData());
            }
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="driverName"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="driverName"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="maxActive"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="maxActive"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="maxWait"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="maxWait"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="minIdle"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="minIdle"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//common/user-mgt/Realm/Configuration/Property[@name="MultiTenantRealmConfigBuilder"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_user_mgt->findnodes('//Realm/Configuration/Property[@name="MultiTenantRealmConfigBuilder"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    print COMMON_USER_MGT $common_user_mgt->toString;
    close(COMMON_USER_MGT);
    #----------------end user-mgt.xml--------------------------------------------------------------------------------------------
    #
    #---------------  axis2_client.xml -----------------------------------------------------------------------------------------
    my $common_axis2_client = $parser->parse_file($common_axis2_client_file);
    open COMMON_AXIS2_CLIENT, ">", $common_axis2_client_target_file or die $!;

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="http"]/parameter[@name="PROTOCOL"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="http"]/parameter[@name="PROTOCOL"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="http"]/parameter[@name="Transfer-Encoding"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="http"]/parameter[@name="Transfer-Encoding"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="http"]/parameter[@name="SO_TIMEOUT"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="http"]/parameter[@name="SO_TIMEOUT"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="http"]/parameter[@name="CONNECTION_TIMEOUT"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="http"]/parameter[@name="CONNECTION_TIMEOUT"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="https"]/parameter[@name="PROTOCOL"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="https"]/parameter[@name="PROTOCOL"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="https"]/parameter[@name="Transfer-Encoding"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="https"]/parameter[@name="Transfer-Encoding"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="https"]/parameter[@name="SO_TIMEOUT"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="https"]/parameter[@name="SO_TIMEOUT"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//axis2_client/transportSender[@name="https"]/parameter[@name="CONNECTION_TIMEOUT"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_axis2_client->findnodes('//transportSender[@name="https"]/parameter[@name="CONNECTION_TIMEOUT"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    print COMMON_AXIS2_CLIENT $common_axis2_client->toString;
    close(COMMON_AXIS2_CLIENT);
    #---------------  end axis2_client.xml -----------------------------------------------------------------------------------------
    #
    #---------------  broker-config.xml -----------------------------------------------------------------------------------------
    my $common_broker_config = $parser->parse_file($common_broker_config_file);
    open COMMON_BROKER_CONFIG, ">", $common_broker_config_target_file or die $!;

    my $xpc = XML::LibXML::XPathContext->new($common_broker_config);
    $xpc->registerNs('ns', 'http://wso2.org/ns/2009/09/eventing');
            
    ($config_node)  = $config->findnodes('//broker-config/eventStream[@name="RegistryEventBroker"]/notificationManager[@class="org.wso2.carbon.event.broker.CarbonNotificationManager"]/parameter[@name="maxQueuedRequests"]/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:eventStream[@name="RegistryEventBroker"]/ns:notificationManager[@class="org.wso2.carbon.event.broker.CarbonNotificationManager"]/ns:parameter[@name="maxQueuedRequests"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    print COMMON_BROKER_CONFIG $common_broker_config->toString;
    close(COMMON_BROKER_CONFIG);
    #----------------end broker-config.xml--------------------------------------------------------------------------------------------

    #---------------  cache.xml -----------------------------------------------------------------------------------------
    my $common_cache = $parser->parse_file($common_cache_file);
    open COMMON_CACHE, ">", $common_cache_target_file or die $!;

    $xpc = XML::LibXML::XPathContext->new($common_cache);
    $xpc->registerNs('ns', 'http://wso2.org/ns/2010/09/caching');
            
    ($config_node)  = $config->findnodes('//cache/configuration/clustering/enabled/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:clustering/ns:enabled/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/clustering/clusterName/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:clustering/ns:clusterName/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/ec2/configFile/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:ec2/ns:configFile/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/cacheMode/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:cacheMode/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/sync/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:sync/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/l1/enabled/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:l1/ns:enabled/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cache/configuration/l1/lifespan/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:configuration/ns:l1/ns:lifespan/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }
    print COMMON_CACHE $common_cache->toString;
    close(COMMON_CACHE);
    #----------------end cache.xml--------------------------------------------------------------------------------------------

    #----------------jgroups-s3_ping-aws.xml--------------------------------------------------------------------------------------------

    my $common_jgroups = $parser->parse_file($common_jgroups_s3_ping_aws_file);
    open COMMON_JGROUP, ">", $common_jgroups_s3_ping_aws_target_file or die $!;

    ($config_node)  = $config->findnodes('//jgroups-s3_ping-aws/S3_PING/@secret_access_key');
    if(defined($config_node)) {
        ($node)  = $common_jgroups->findnodes('//S3_PING/@secret_access_key');
        if(defined($node)) {
            $node->setValue($config_node->nodeValue());
        }
    }

    ($config_node)  = $config->findnodes('//jgroups-s3_ping-aws/S3_PING/@access_key');
    if(defined($config_node)) {
        ($node)  = $common_jgroups->findnodes('//S3_PING/@access_key');
        if(defined($node)) {
            $node->setValue($config_node->nodeValue());
        }
    }

    ($config_node)  = $config->findnodes('//jgroups-s3_ping-aws/S3_PING/@location');
    if(defined($config_node)) {
        ($node)  = $common_jgroups->findnodes('//S3_PING/@location');
        if(defined($node)) {
            $node->setValue($config_node->nodeValue());
        }
    }

    print COMMON_JGROUP $common_jgroups->toString;
    close(COMMON_JGROUP);
    #----------------end jgroups-s3_ping-aws.xml--------------------------------------------------------------------------------------------

    #---------------  cloud-services-desc.xml -----------------------------------------------------------------------------------------
    my $common_cloud_services = $parser->parse_file($common_cloud_services_desc_file);
    open COMMON_CLOUD_SERVICES, ">", $common_cloud_services_desc_target_file or die $!;

    $xpc = XML::LibXML::XPathContext->new($common_cloud_services);
    $xpc->registerNs('ns', 'http://wso2.com/carbon/cloud/mgt/services');
            
    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Manager"]/link/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Manager"]/ns:link/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Governance"]/label/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Governance"]/ns:label/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Governance"]/link/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Governance"]/ns:link/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Governance"]/description/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Governance"]/ns:description/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Governance"]/icon/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Governance"]/ns:icon/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Governance"]/productPageURL/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Governance"]/ns:productPageURL/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Identity"]/label/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Identity"]/ns:label/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Identity"]/link/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Identity"]/ns:link/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Identity"]/description/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Identity"]/ns:description/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Identity"]/icon/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Identity"]/ns:icon/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//cloud-services-desc/cloudService[@name="WSO2 Stratos Identity"]/productPageURL/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:cloudService[@name="WSO2 Stratos Identity"]/ns:productPageURL/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    print COMMON_CLOUD_SERVICES $common_cloud_services->toString;
    close(COMMON_CLOUD_SERVICES);
    #---------------  end cloud-services-desc.xml -----------------------------------------------------------------------------------------

    #---------------  identity.xml -----------------------------------------------------------------------------------------
    my $common_identity = $parser->parse_file($common_identity_file);
    open COMMON_IDENTITY, ">", $common_identity_target_file or die $!;

    $xpc = XML::LibXML::XPathContext->new($common_identity);
    $xpc->registerNs('ns', 'http://wso2.org/projects/carbon/carbon.xml');
            
    ($config_node)  = $config->findnodes('//identity/OpenIDServerUrl/text()');
    ($node)  = $xpc->findnodes('//ns:OpenIDServerUrl/text()');
    $node->setData($config_node->getData());

    ($config_node)  = $config->findnodes('//identity/OpenIDUserPattern/text()');
    ($node)  = $xpc->findnodes('//ns:OpenIDUserPattern/text()');
    $node->setData($config_node->getData());

    ($config_node)  = $config->findnodes('//identity/RequestClaimsFromIdP/text()');
    if(defined($config_node)) {
        ($node)  = $xpc->findnodes('//ns:RequestClaimsFromIdP/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//identity/SSOService/IdentityProviderURL/text()');
    ($node)  = $xpc->findnodes('//ns:SSOService/ns:IdentityProviderURL/text()');
    $node->setData($config_node->getData());

    print COMMON_IDENTITY $common_identity->toString;
    close(COMMON_IDENTITY);
    #---------------  end identity.xml -----------------------------------------------------------------------------------------

    #---------------  throttling-agent-config.xml -----------------------------------------------------------------------------------------
    my $common_throttling = $parser->parse_file($common_throttling_agent_config_file);
    open COMMON_THROTTLING, ">", $common_throttling_agent_config_target_file or die $!;

    $xpc = XML::LibXML::XPathContext->new($common_throttling);
    $xpc->registerNs('ns', 'http://wso2.com/carbon/multitenancy/throttling/agent/config');
            
    ($config_node)  = $config->findnodes('//throttling-agent-config/parameters/parameter[@name="managerServiceUrl"]/text()');
    ($node)  = $xpc->findnodes('//ns:parameters/ns:parameter[@name="managerServiceUrl"]/text()');
    $node->setData($config_node->getData());

    ($config_node)  = $config->findnodes('//throttling-agent-config/parameters/parameter[@name="userName"]/text()');
    ($node)  = $xpc->findnodes('//ns:parameters/ns:parameter[@name="userName"]/text()');
    $node->setData($config_node->getData());

    ($config_node)  = $config->findnodes('//throttling-agent-config/parameters/parameter[@name="password"]/text()');
    ($node)  = $xpc->findnodes('//ns:parameters/ns:parameter[@name="password"]/text()');
    $node->setData($config_node->getData());

    print COMMON_THROTTLING $common_throttling->toString;
    close(COMMON_THROTTLING);
    #---------------  end throttling-agent-config.xml -----------------------------------------------------------------------------------------

    #-------------- mgt-transports.xml -----------------------------------------------------------------------------------------
    my $common_mgt_transports = $parser->parse_file($common_mgt_transports_file);
    open COMMON_MGT_TRANSPORTS, ">", $common_mgt_transports_target_file or die $!;

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="maxHttpHeaderSize"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="maxHttpHeaderSize"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="acceptorThreadCount"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="acceptorThreadCount"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="maxThreads"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="maxThreads"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="minSpareThreads"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="minSpareThreads"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="disableUploadTimeout"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="disableUploadTimeout"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="connectionUploadTimeout"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="connectionUploadTimeout"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="maxKeepAliveRequests"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="maxKeepAliveRequests"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="http"]/parameter[@name="acceptCount"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="http"]/parameter[@name="acceptCount"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="maxHttpHeaderSize"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="maxHttpHeaderSize"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="acceptorThreadCount"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="acceptorThreadCount"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="maxThreads"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="maxThreads"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="minSpareThreads"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="minSpareThreads"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="disableUploadTimeout"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="disableUploadTimeout"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="connectionUploadTimeout"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="connectionUploadTimeout"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="maxKeepAliveRequests"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="maxKeepAliveRequests"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }

    ($config_node)  = $config->findnodes('//mgt-transports/transport[@name="https"]/parameter[@name="acceptCount"]/text()');
    if(defined($config_node)) {
        ($node)  = $common_mgt_transports->findnodes('//transport[@name="https"]/parameter[@name="acceptCount"]/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }
    }


    print COMMON_MGT_TRANSPORTS $common_mgt_transports->toString;
    close(COMMON_MGT_TRANSPORTS);
    #--------------  end mgt-transports.xml -----------------------------------------------------------------------------------------
    #
    #------------------------------------------------------------------------------End Commong Config-----------------------------------
}
1;

