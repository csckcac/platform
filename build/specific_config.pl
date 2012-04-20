#!/usr/bin/perl -w
# This script will configure all parameters related to all services. 
# Files common to all services but may have different parameter values - carbon.xml, axis2.xml, registry.xml and jmx.xml, authenticators.xml
# Files specific to Manager - tenant-reg-agent.xml, billing-config.xml, stratos.xml
# Files specific to manager, bam and summarizer - bam.xml
# Files specific to manager, bam and bps - datasources.xml
# Files specific to bps - bps.xml
# Files specific to identity - sso-idp-config.xml
# Files specific to dss - wso2-rss-config.xml, cassandra-auth.xml, cassandra-component.xml
#
use XML::LibXML;
sub stratos_config_service {
    my $config_file = $_[0];
    my $parser = XML::LibXML->new();

    my $config = $parser->parse_file($config_file);
    my $stratos_dir=$_[1];
    my $stratos_target_dir=$_[2];
    my $sso_enabled=$_[3];
    my $dbuser=$_[4];
    my $dbpasswd=$_[5];
    my $service_name=$_[6];
    if(defined($stratos_dir)) {
        $carbon_config_file = $stratos_dir."/repository/conf/carbon.xml";
        $authenticators_config_file = $stratos_dir."/repository/conf/security/authenticators.xml";
        $registry_config_file = $stratos_dir."/repository/conf/registry.xml";
        $axis2_config_file = $stratos_dir."/repository/conf/axis2/axis2.xml";
        $stratos_config_file = $stratos_dir."/repository/conf/stratos.xml";
        $bam_config_file = $stratos_dir."/repository/conf/bam.xml";
        $billing_config_file = $stratos_dir."/repository/conf/billing-config.xml";
        $datasources_config_file = $stratos_dir."/repository/conf/datasources.properties";
        $jmx_config_file = $stratos_dir."/repository/conf/etc/jmx.xml";
        $identity_sso_idp_file = $stratos_dir."/repository/conf/sso-idp-config.xml";
        $bps_bps_file = $stratos_dir."/repository/conf/bps.xml";
        $dss_cassandra_auth_file = $stratos_dir."/repository/conf/advanced/cassandra-auth.xml";
        $dss_cassandra_component_file = $stratos_dir."/repository/conf/advanced/cassandra-component.xml";
        $dss_rss_config_file = $stratos_dir."/repository/conf/advanced/wso2-rss-config.xml";
        $wso2server_config_file = $stratos_dir."/bin/wso2server.sh";
    } 

    if(defined($stratos_target_dir)) {
        $carbon_config_target_file = $stratos_target_dir."/repository/conf/carbon.xml";
        $authenticators_config_target_file = $stratos_target_dir."/repository/conf/security/authenticators.xml";
        $registry_config_target_file = $stratos_target_dir."/repository/conf/registry.xml";
        $axis2_config_target_file = $stratos_target_dir."/repository/conf/axis2/axis2.xml";
        $stratos_config_target_file = $stratos_target_dir."/repository/conf/stratos.xml";
        $bam_config_target_file = $stratos_target_dir."/repository/conf/bam.xml";
        $billing_config_target_file = $stratos_target_dir."/repository/conf/billing-config.xml";
        $manager_tenant_target_file = $stratos_target_dir."/repository/conf/tenant-reg-agent.xml";
        $datasources_config_target_file = $stratos_target_dir."/repository/conf/datasources.properties";
        $jmx_config_target_file = $stratos_target_dir."/repository/conf/etc/jmx.xml";
        $identity_sso_idp_target_file = $stratos_target_dir."/repository/conf/sso-idp-config.xml";
        $bps_bps_target_file = $stratos_target_dir."/repository/conf/bps.xml";
        $dss_cassandra_auth_target_file = $stratos_target_dir."/repository/conf/advanced/cassandra-auth.xml";
        $dss_cassandra_component_target_file = $stratos_target_dir."/repository/conf/advanced/cassandra-component.xml";
        $dss_rss_config_target_file = $stratos_target_dir."/repository/conf/advanced/wso2-rss-config.xml";
        $wso2server_config_target_file = $stratos_target_dir."/bin/wso2server.sh";
    }

    #----------------------------------------------------------------------------- Start Service Configuration ----------------------------------------

    #-------------- carbon.xml -----------------------------------------------------------------------------------------

    my $carbon_config;
    if(-e $carbon_config_file) {
        $carbon_config = $parser->parse_file($carbon_config_file);
        open CARBON_CONFIG, ">", $carbon_config_target_file or die $!;

        $xpc = XML::LibXML::XPathContext->new($carbon_config);
        $xpc->registerNs('ns', 'http://wso2.org/projects/carbon/carbon.xml');
         
        
        ($config_node)  = $config->findnodes("//$service_name".'/carbon/HostName/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:HostName');
            if(!defined($node)) {
                ($node)  = $xpc->findnodes('//ns:Server');
                if(defined($node)) {
                    my $hostname = $config_node->getData();
                    my $fragment = $parser->parse_balanced_chunk(
                        "<HostName>$hostname</HostName>"
                    );
                    $node->appendChild($fragment);
                }
            } else {
                ($node)  = $xpc->findnodes('//ns:HostName/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/ServerURL/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:ServerURL/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/BamServerURL/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:BamServerURL');
            if(!defined($node)) {
                ($node)  = $xpc->findnodes('//ns:Server');
                if(defined($node)) {
                    my $bamserverurl = $config_node->getData();
                    my $fragment = $parser->parse_balanced_chunk(
                        "<BamServerURL>$bamserverurl</BamServerURL>"
                    );
                    $node->appendChild($fragment);
                }
            } else {
                ($node)  = $xpc->findnodes('//ns:BamServerURL/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/FileUploadConfig/TotalFileSizeLimit/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:FileUploadConfig/ns:TotalFileSizeLimit/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/Ports/Offset/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Ports/ns:Offset/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/Ports/ServletTransports/HTTPS/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Ports/ns:ServletTransports/ns:HTTPS/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/carbon/Ports/ServletTransports/HTTP/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Ports/ns:ServletTransports/ns:HTTP/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print CARBON_CONFIG $carbon_config->toString;
        close(CARBON_CONFIG);
        system("xmllint --format $carbon_config_target_file > ./temp.xml;mv temp.xml $carbon_config_target_file");
    }
    #-------------- end carbon.xml -----------------------------------------------------------------------------------------

    #-------------- authenticators.xml -----------------------------------------------------------------------------------------

    if($sso_enabled eq 'true') {
        my $authenticators_config = $parser->parse_file($authenticators_config_file);
        open AUTHENTICATORS_CONFIG, ">", $authenticators_config_target_file or die $!;

        $xpc = XML::LibXML::XPathContext->new($authenticators_config);
        $xpc->registerNs('ns', 'http://wso2.org/projects/carbon/authenticators.xml');
                
        ($config_node)  = $config->findnodes("//$service_name".'/authenticators/Authenticator[@name="SAML2SSOAuthenticator"]/Priority/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Authenticator[@name="SAML2SSOAuthenticator"]/ns:Priority/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/authenticators/Authenticator[@name="SAML2SSOAuthenticator"]/Config/Parameter[@name="LoginPage"]/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Authenticator[@name="SAML2SSOAuthenticator"]/ns:Config/ns:Parameter[@name="LoginPage"]/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/authenticators/Authenticator[@name="SAML2SSOAuthenticator"]/Config/Parameter[@name="ServiceProviderID"]/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Authenticator[@name="SAML2SSOAuthenticator"]/ns:Config/ns:Parameter[@name="ServiceProviderID"]/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/authenticators/Authenticator[@name="SAML2SSOAuthenticator"]/Config/Parameter[@name="IdentityProviderSSOServiceURL"]/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:Authenticator[@name="SAML2SSOAuthenticator"]/ns:Config/ns:Parameter[@name="IdentityProviderSSOServiceURL"]/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print AUTHENTICATORS_CONFIG $authenticators_config->toString;
        close(AUTHENTICATORS_CONFIG);
    }
    #-------------- end authenticators.xml -----------------------------------------------------------------------------------------

    #-------------- registry.xml -----------------------------------------------------------------------------------------
    my $registry_config;
    if(-e $registry_config_file) {
        $registry_config = $parser->parse_file($registry_config_file);
        open REGISTRY_CONFIG, ">", $registry_config_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/url/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/url/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/userName/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/userName/text()');
            if(defined($node)) {
                if(defined($dbuser)) {
                    $node->setData($dbuser);
                } else {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/password/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/password/text()');
            if(defined($node)) {
                if(defined($dbpasswd)) {
                    $node->setData($dbpasswd);
                } else {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/driverName/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/driverName/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/maxActive/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/maxActive/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/maxWait/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/maxWait/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="wso2registry"]/minIdle/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="wso2registry"]/minIdle/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/url/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/url/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/userName/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/userName/text()');
            if(defined($node)) {
                if(defined($dbuser)) {
                    $node->setData($dbuser);
                } else {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/password/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/password/text()');
            if(defined($node)) {
                if(defined($dbpasswd)) {
                    $node->setData($dbpasswd);
                } else {
                    $node->setData($config_node->getData());
                }
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/driverName/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/driverName/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/maxActive/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/maxActive/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/maxWait/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/maxWait/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/dbConfig[@name="governance"]/minIdle/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//dbConfig[@name="governance"]/minIdle/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/remoteInstance[@url="https://governance.stratoslive.wso2.com"]/id/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//remoteInstance[@url="https://governance.stratoslive.wso2.com"]/id/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/remoteInstance[@url="https://governance.stratoslive.wso2.com"]/dbConfig/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//remoteInstance[@url="https://governance.stratoslive.wso2.com"]/dbConfig/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/remoteInstance[@url="https://governance.stratoslive.wso2.com"]/readOnly/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//remoteInstance[@url="https://governance.stratoslive.wso2.com"]/readOnly/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/remoteInstance[@url="https://governance.stratoslive.wso2.com"]/registryRoot/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//remoteInstance[@url="https://governance.stratoslive.wso2.com"]/registryRoot/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/remoteInstance[@url="https://governance.stratoslive.wso2.com"]/enableCache/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//remoteInstance[@url="https://governance.stratoslive.wso2.com"]/enableCache/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/mount[@path="/_system/governance"]/instanceId/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//mount[@path="/_system/governance"]/instanceId/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/mount[@path="/_system/governance"]/targetPath/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//mount[@path="/_system/governance"]/targetPath/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/mount[@path="/_system/config"]/instanceId/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//mount[@path="/_system/config"]/instanceId/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/registry/mount[@path="/_system/config"]/targetPath/text()');
        if(defined($config_node)) {
            ($node)  = $registry_config->findnodes('//mount[@path="/_system/config"]/targetPath/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print REGISTRY_CONFIG $registry_config->toString;
        close(REGISTRY_CONFIG);
    }
    #-------------- end registry.xml -----------------------------------------------------------------------------------------


    #-------------- axis2.xml -----------------------------------------------------------------------------------------
    my $axis2_config;
    if(-e $axis2_config_file) {
        $axis2_config = $parser->parse_file($axis2_config_file);
        open AXIS2_CONFIG, ">", $axis2_config_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.host"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.host"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.port"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.port"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.starttls.enable"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.starttls.enable"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.auth"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.auth"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.from"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.from"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.user"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.user"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/transportSender[@name="mailto"]/parameter[@name="mail.smtp.password"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//transportSender[@name="mailto"]/parameter[@name="mail.smtp.password"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="membershipScheme"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="membershipScheme"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="domain"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="domain"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="synchronizeAll"]/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/parameter[@name="synchronizeAll"]/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/members/member/hostName/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/members/member/hostName/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/members/member/port/text()');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/members/member/port/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/axis2/clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/stateManager[@class="org.apache.axis2.clustering.state.DefaultStateManager"]/@enable');
        if(defined($config_node)) {
            ($node)  = $axis2_config->findnodes('//clustering[@class="org.apache.axis2.clustering.tribes.TribesClusteringAgent"]/stateManager[@class="org.apache.axis2.clustering.state.DefaultStateManager"]/@enable');
            if(defined($node)) {
                $node->setValue($config_node->nodeValue());
            }
        }

        print AXIS2_CONFIG $axis2_config->toString;
        close(AXIS2_CONFIG);
    }
    #----------------end axis2.xml--------------------------------------------------------------------------------------------

    #-------------- stratos.xml -----------------------------------------------------------------------------------------

    my $stratos_config;
    if(-e $stratos_config_file) {
        $stratos_config = $parser->parse_file($stratos_config_file);
        open STRATOS_CONFIG, ">", $stratos_config_target_file or die $!;
        $xpc = XML::LibXML::XPathContext->new($stratos_config);
        $xpc->registerNs('ns', 'http://wso2.com/cloud/stratos');
            
        ($config_node)  = $config->findnodes("//$service_name".'/stratos/NotificationEmail/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:NotificationEmail/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/stratos/FinanceNotificationEmail/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:FinanceNotificationEmail/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/stratos/PaypalUrl/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:PaypalUrl/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/stratos/PaypalAPIUsername/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:PaypalAPIUsername/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/stratos/PaypalAPIPassword/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:PaypalAPIPassword/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/stratos/PaypalAPISignature/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:PaypalAPISignature/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print STRATOS_CONFIG $stratos_config->toString;
        close(STRATOS_CONFIG);
    }
    #-------------- end stratos.xml -----------------------------------------------------------------------------------------

    #-------------- bam.xml -----------------------------------------------------------------------------------------
    my $bam_config;
    if(-e $bam_config_file) {
        $bam_config = $parser->parse_file($bam_config_file);
        open BAM_CONFIG, ">", $bam_config_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/bam/summaryGeneration/initial-delay/text()');
        if(defined($config_node)) {
            ($node)  = $bam_config->findnodes('//summaryGeneration/initial-delay/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/bam/summaryGeneration/interval/text()');
        if(defined($config_node)) {
            ($node)  = $bam_config->findnodes('//summaryGeneration/interval/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/bam/summaryGeneration/taskBreakDownLength/text()');
        if(defined($config_node)) {
            ($node)  = $bam_config->findnodes('//summaryGeneration/taskBreakDownLength/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/bam/summaryGeneration/sleepTimeBetweenTasks/text()');
        if(defined($config_node)) {
            ($node)  = $bam_config->findnodes('//summaryGeneration/sleepTimeBetweenTasks/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print BAM_CONFIG $bam_config->toString;
        close(BAM_CONFIG);
    }
    #-------------- end bam.xml -----------------------------------------------------------------------------------------

    #---------------  billing-config.xml -----------------------------------------------------------------------------------------
    my $billing_config;
    if(-e $billing_config_file) {
        $billing_config = $parser->parse_file($billing_config_file);
        open BILLING_CONFIG, ">", $billing_config_target_file or die $!;

        $xpc = XML::LibXML::XPathContext->new($billing_config);
        $xpc->registerNs('ns', 'http://wso2.com/carbon/multitenancy/billing/config');
                
        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/url/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:url/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/userName/text()');
        ($node)  = $xpc->findnodes('//ns:dbConfig/ns:userName/text()');
        if(defined($node)) {
            if(defined($dbuser)) {
                $node->setData($dbuser);
            } else {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/password/text()');
        ($node)  = $xpc->findnodes('//ns:dbConfig/ns:password/text()');
        if(defined($node)) {
            if(defined($dbpasswd)) {
                $node->setData($dbpasswd);
            } else {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/driverName/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:driverName/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/maxActive/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:maxActive/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/maxWait/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:maxWait/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/minIdle/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:minIdle/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/billing-config/dbConfig/validationQuery/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:dbConfig/ns:validationQuery/text()');
            if(defined($node)) {
                $node->setData($config_node->nodeValue());
            }
        }

        my ($task_node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]');
        if(defined($task_node))
        {
            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/@scheduleHelperClass');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/@scheduleHelperClass');
                if(defined($node)) {
                    $node->setValue($config_node->nodeValue());
                }
            }

            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/parameter[@name="dayToTriggerOn"]/text()');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/ns:parameter[@name="dayToTriggerOn"]/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }

            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/parameter[@name="hourToTriggerOn"]/text()');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/ns:parameter[@name="hourToTriggerOn"]/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }

            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/parameter[@name="minuteToTriggerOn"]/text()');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/ns:parameter[@name="minuteToTriggerOn"]/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }

            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/parameter[@name="timeZone"]/text()');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/ns:parameter[@name="timeZone"]/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }

            ($config_node)  = $config->findnodes("//$service_name".'/billing-config/tasks/task[@id="multitenancyScheduledTask"]/schedule/parameter[@name="cron"]/text()');
            if(defined($config_node)) {
                ($node)  = $xpc->findnodes('//ns:tasks/ns:task[@id="multitenancyScheduledTask"]/ns:schedule/ns:parameter[@name="cron"]/text()');
                if(defined($node)) {
                    $node->setData($config_node->getData());
                }
            }
        }


        print BILLING_CONFIG $billing_config->toString;
        close(BILLING_CONFIG);
    }
    #----------------end billing-config.xml--------------------------------------------------------------------------------------------

    #---------------  tenant-reg-agent.xml -----------------------------------------------------------------------------------------
        
    my $manager_tenants  = $config->findnodes("//$service_name".'/tenant-reg-agent/tenantRegListenerServers');
    if(defined($manager_tenants) && ! $manager_tenants eq '') {
        ($node) = $manager_tenants->get_nodelist();

        open MANAGET_TENANT, ">", $manager_tenant_target_file or die $!;
        print MANAGET_TENANT $node->toString(1, 0);
        close(MANAGET_TENANT);
    }
    #---------------  end tenant-reg-agent.xml -----------------------------------------------------------------------------------------

    #-------------- datasources.properties -----------------------------------------------------------------------------------------
    my $regds_username;
    my $regds_password;
    my $bamds_username;
    my $bamds_password;
    my $ic_factory;
    my $regds_registry;
    my $regds_type;
    my $regds_driver;
    my $regds_url;
    my $regds_ds_name;
    my $regds_max_active;
    my $regds_min_idle;
    my $regds_max_wait;
    my $regds_auto_commit;
    
    my $bamds_registry;
    my $bamds_type;
    my $bamds_driver;
    my $bamds_url;
    my $bamds_ds_name;
    my $bamds_max_active;
    my $bamds_min_idle;
    my $bamds_max_wait;
    my $bamds_auto_commit;

    my $bpsds_registry;
    my $bpsds_type;
    my $bpsds_driver;
    my $bpsds_url;
    my $bpsds_ds_name;
    my $bpsds_max_active;
    my $bpsds_min_idle;
    my $bpsds_max_wait;
    my $bpsds_auto_commit;

    my $provider_port;
    if(-e $datasources_config_file) {
        open(DATASOURCES_CONFIG_IN, $datasources_config_file) or die("Cannot Open File");
        my(@fcont) = <DATASOURCES_CONFIG_IN>;
        close DATASOURCES_CONFIG_IN;

        open DATASOURCES_CONFIG, ">", $datasources_config_target_file or die $!;

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/providerPort/text()');
        if(defined($node)) {
            $provider_port = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/icFactory/text()');
        if(defined($node)) {
            $ic_factory = $node->getData();
        }

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/registry/text()');
        if(defined($node)) {
            $regds_registry = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/type/text()');
        if(defined($node)) {
            $regds_type = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/driverClassName/text()');
        if(defined($node)) {
            $regds_driver_class_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/url/text()');
        if(defined($node)) {
            $regds_url = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/username/text()');
        if(defined($dbuser)) {
            $regds_username = $dbuser;
        } else {
            $regds_username = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/password/text()');
        if(defined($dbpasswd)) {
            $regds_password = $dbpasswd;
        } else {
            $regds_password = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/dsName/text()');
        if(defined($node)) {
            $regds_ds_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/maxActive/text()');
        if(defined($node)) {
            $regds_max_active = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/minIdle/text()');
        if(defined($node)) {
            $regds_min_idle = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/maxWait/text()');
        if(defined($node)) {
            $regds_max_wait = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/stratos_ds/autoCommit/text()');
        if(defined($node)) {
            $regds_auto_commit = $node->getData();
        }

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/registry/text()');
        if(defined($node)) {
            $bamds_registry = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/type/text()');
        if(defined($node)) {
            $bamds_type = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/driverClassName/text()');
        if(defined($node)) {
            $bamds_driver_class_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/url/text()');
        if(defined($node)) {
            $bamds_url = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/username/text()');
        if(defined($dbuser)) {
            $bamds_username = $dbuser;
        } else {
            $bamds_username = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/password/text()');
        if(defined($dbpasswd)) {
            $bamds_password = $dbpasswd;
        } else {
            $bamds_password = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/dsName/text()');
        if(defined($node)) {
            $bamds_ds_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/maxActive/text()');
        if(defined($node)) {
            $bamds_max_active = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/minIdle/text()');
        if(defined($node)) {
            $bamds_min_idle = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/maxWait/text()');
        if(defined($node)) {
            $bamds_max_wait = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="stratos_ds,bam_datasource"]/bam_datasource/autoCommit/text()');
        if(defined($node)) {
            $bamds_auto_commit = $node->getData();
        }

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/providerPort/text()');
        if(defined($node)) {
            $provider_port = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/icFactory/text()');
        if(defined($node)) {
            $ic_factory = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/registry/text()');
        if(defined($node)) {
            $bamds_registry = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/type/text()');
        if(defined($node)) {
            $bamds_type = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/driverClassName/text()');
        if(defined($node)) {
            $bamds_driver_class_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/url/text()');
        if(defined($node)) {
            $bamds_url = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/username/text()');
        if(defined($dbuser)) {
            $bamds_username = $dbuser;
        } else {
            $bamds_username = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/password/text()');
        if(defined($dbpasswd)) {
            $bamds_password = $dbpasswd;
        } else {
            $bamds_password = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/dsName/text()');
        if(defined($node)) {
            $bamds_ds_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/maxActive/text()');
        if(defined($node)) {
            $bamds_max_active = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/maxIdle/text()');
        if(defined($node)) {
            $bamds_max_idle = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/maxWait/text()');
        if(defined($node)) {
            $bamds_max_wait = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bam_datasource"]/bam_datasource/autoCommit/text()');
        if(defined($node)) {
            $bamds_auto_commit = $node->getData();
        }

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/providerPort/text()');
        if(defined($node)) {
            $provider_port = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/icFactory/text()');
        if(defined($node)) {
            $ic_factory = $node->getData();
        }

        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/registry/text()');
        if(defined($node)) {
            $bpsds_registry = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/type/text()');
        if(defined($node)) {
            $bpsds_type = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/driverClassName/text()');
        if(defined($node)) {
            $bpsds_driver_class_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/url/text()');
        if(defined($node)) {
            $bpsds_url = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/username/text()');
        if(defined($dbuser)) {
            $bpsds_username = $dbuser;
        } else {
            $bpsds_username = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/password/text()');
        if(defined($dbpasswd)) {
            $bpsds_password = $dbpasswd;
        } else {
            $bpsds_password = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/dsName/text()');
        if(defined($node)) {
            $bpsds_ds_name = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/maxActive/text()');
        if(defined($node)) {
            $bpsds_max_active = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/maxIdle/text()');
        if(defined($node)) {
            $bpsds_min_idle = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/maxWait/text()');
        if(defined($node)) {
            $bpsds_max_wait = $node->getData();
        }
        ($node)  = $config->findnodes("//$service_name".'/datasources[@name="bpsds"]/bpsds/autoCommit/text()');
        if(defined($node)) {
            $bpsds_auto_commit = $node->getData();
        }

        foreach $line (@fcont) {
            if(defined($provider_port)) {
                $line =~ s/^(synapse.datasources.providerPort=).*/$1$provider_port/g;
            }
            if(defined($ic_factory)) {
                $line =~ s/^(synapse.datasources.icFactory=).*/$1$ic_factory/g;   
            }
            
            if(defined($regds_registry)) {
                $line =~ s/^(synapse.datasources.stratos_ds.registry=).*/$1$regds_registry/g; 
            }
            if(defined($regds_type)) {
                $line =~ s/^(synapse.datasources.stratos_ds.type=).*/$1$regds_type/g;   
            }
            if(defined($regds_driver_class_name)) {
                $line =~ s/^(synapse.datasources.stratos_ds.driverClassName=).*/$1$regds_driver_class_name/g;   
            }
            if(defined($regds_url)) {
                $line =~ s/^(synapse.datasources.stratos_ds.url=).*/$1$regds_url/g;   
            }
            if(defined($regds_username)) {
                $line =~ s/^(synapse.datasources.stratos_ds.username=).*/$1$regds_username/g;   
            }
            if(defined($regds_password)) {
                $line =~ s/^(synapse.datasources.stratos_ds.password=).*/$1$regds_password/g;   
            }
            if(defined($regds_ds_name)) {
                $line =~ s/^(synapse.datasources.stratos_ds.dsName=).*/$1$regds_ds_name/g;   
            }
            if(defined($regds_max_active)) {
                $line =~ s/^(synapse.datasources.stratos_ds.maxActive=).*/$1$regds_max_active/g;   
            }
            if(defined($regds_min_idle)) {
                $line =~ s/^(synapse.datasources.stratos_ds.minIdle=).*/$1$regds_min_idle/g;   
            }
            if(defined($regds_max_wait)) {
                $line =~ s/^(synapse.datasources.stratos_ds.maxWait=).*/$1$regds_max_wait/g;   
            }
            if(defined($regds_auto_commit)) {
                $line =~ s/^(synapse.datasources.stratos_ds.autoCommit=).*/$1$regds_auto_commit/g;   
            }
            
            if(defined($bamds_registry)) {
                $line =~ s/^(synapse.datasources.bam_datasource.registry=).*/$1$bamds_registry/g;   
            }
            if(defined($bamds_type)) {
                $line =~ s/^(synapse.datasources.bam_datasource.type=).*/$1$bamds_type/g;   
            }
            if(defined($bamds_driver_class_name)) {
                $line =~ s/^(synapse.datasources.bam_datasource.driverClassName=).*/$1$bamds_driver_class_name/g;   
            }
            if(defined($bamds_url)) {
                $line =~ s/^(synapse.datasources.bam_datasource.url=).*/$1$bamds_url/g;   
            }
            if(defined($bamds_username)) {
                $line =~ s/^(synapse.datasources.bam_datasource.username=).*/$1$bamds_username/g;   
            }
            if(defined($bamds_password)) {
                $line =~ s/^(synapse.datasources.bam_datasource.password=).*/$1$bamds_password/g;   
            }
            if(defined($bamds_ds_name)) {
                $line =~ s/^(synapse.datasources.bam_datasource.dsName=).*/$1$bamds_ds_name/g;   
            }
            if(defined($bamds_max_active)) {
                $line =~ s/^(synapse.datasources.bam_datasource.maxActive=).*/$1$bamds_max_active/g;   
            }
            if(defined($bamds_min_idle)) {
                $line =~ s/^(synapse.datasources.bam_datasource.minIdle=).*/$1$bamds_min_idle/g;   
            }
            if(defined($bamds_max_wait)) {
                $line =~ s/^(synapse.datasources.bam_datasource.maxWait=).*/$1$bamds_max_wait/g;   
            }
            if(defined($bamds_auto_commit)) {
                $line =~ s/^(synapse.datasources.bam_datasource.autoCommit=).*/$1$bamds_auto_commit/g;   
            }
     
            
            if(defined($bpsds_registry)) {
                $line =~ s/^(synapse.datasources.bpsds.registry=).*/$1$bpsds_registry/g;   
            }
            if(defined($bpsds_type)) {
                $line =~ s/^(synapse.datasources.bpsds.type=).*/$1$bpsds_type/g;   
            }
            if(defined($bpsds_driver_class_name)) {
                $line =~ s/^(synapse.datasources.bpsds.driverClassName=).*/$1$bpsds_driver_class_name/g;   
            }
            if(defined($bpsds_url)) {
                $line =~ s/^(synapse.datasources.bpsds.url=).*/$1$bpsds_url/g;   
            }
            if(defined($bpsds_username)) {
                $line =~ s/^(synapse.datasources.bpsds.username=).*/$1$bpsds_username/g;   
            }
            if(defined($bpsds_password)) {
                $line =~ s/^(synapse.datasources.bpsds.password=).*/$1$bpsds_password/g;   
            }
            if(defined($bpsds_ds_name)) {
                $line =~ s/^(synapse.datasources.bpsds.dsName=).*/$1$bpsds_ds_name/g;   
            }
            if(defined($bpsds_max_active)) {
                $line =~ s/^(synapse.datasources.bpsds.maxActive=).*/$1$bpsds_max_active/g;   
            }
            if(defined($bpsds_min_idle)) {
                $line =~ s/^(synapse.datasources.bpsds.maxIdle=).*/$1$bpsds_min_idle/g;   
            }
            if(defined($bpsds_max_wait)) {
                $line =~ s/^(synapse.datasources.bpsds.maxWait=).*/$1$bpsds_max_wait/g;   
            }
            if(defined($bpsds_auto_commit)) {
                $line =~ s/^(synapse.datasources.bpsds.autoCommit=).*/$1$bpsds_auto_commit/g;   
            }
            
            print DATASOURCES_CONFIG $line;
        }
        close DATASOURCES_CONFIG;
    }
    #-------------- end datasources.properties -----------------------------------------------------------------------------------------

    #-------------- sso-idp-config.xml -----------------------------------------------------------------------------------------
    my $identity_sso_idp;
    if(-e $identity_sso_idp_file) {
        $identity_sso_idp = $parser->parse_file($identity_sso_idp_file);
        open IDENTITY_SSO_IDP, ">", $identity_sso_idp_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/LoginPageBannerBaseURL/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//LoginPageBannerBaseURL/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/TenantRegistrationPage/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//TenantRegistrationPage/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Identity"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Identity"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Governance"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Governance"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Manager"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Manager"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Mashup Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Mashup Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Gadget Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Gadget Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Application Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Application Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Activity Monitor"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Activity Monitor"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Rules Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Rules Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Enterprise Service Bus"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Enterprise Service Bus"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Data Services Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Data Services Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Complex Event Processing Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Complex Event Processing Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Message Broker"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Message Broker"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/sso-idp-config/ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Process Server"]/AssertionConsumerService/text()');
        if(defined($config_node)) {
            ($node)  = $identity_sso_idp->findnodes('//ServiceProviders/ServiceProvider[Issuer="WSO2 Stratos Business Process Server"]/AssertionConsumerService/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print IDENTITY_SSO_IDP $identity_sso_idp->toString;
        close(IDENTITY_SSO_IDP);
    }
    #-------------- end sso-idp-config.xml -----------------------------------------------------------------------------------------


    #---------------  jmx.xml -----------------------------------------------------------------------------------------
    my $jmx_config;
    if(-e $jmx_config_file) {
        $jmx_config = $parser->parse_file($jmx_config_file);
        open JMX_CONFIG, ">", $jmx_config_target_file or die $!;

        $xpc = XML::LibXML::XPathContext->new($jmx_config);
        $xpc->registerNs('ns', 'http://wso2.org/projects/carbon/jmx.xml');
                
        ($config_node)  = $config->findnodes("//$service_name".'/jmx/StartRMIServer/text()');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:StartRMIServer/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print JMX_CONFIG $jmx_config->toString;
        close(JMX_CONFIG);
    }
    #----------------end jmx.xml--------------------------------------------------------------------------------------------

    #-------------- bps.xml -----------------------------------------------------------------------------------------
    my $bps_bps;
    if(-e $bps_bps_file) {
        my $bps_bps = $parser->parse_file($bps_bps_file);
        open BPS_BPS, ">", $bps_bps_target_file or die $!;

        $xpc = XML::LibXML::XPathContext->new($bps_bps);
        $xpc->registerNs('ns', 'http://wso2.org/bps/config');
                
        ($config_node)  = $config->findnodes("//$service_name".'/DataBaseConfig/DataSource/JNDI/@contextFactory');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:DataBaseConfig/ns:DataSource/ns:JNDI/@contextFactory');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/DataBaseConfig/DataSource/JNDI/@providerURL');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:DataBaseConfig/ns:DataSource/ns:JNDI/@providerURL');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/MultithreadedHttpConnectionManagerConfig/maxConnectionsPerHost/@value');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:MultithreadedHttpConnectionManagerConfig/ns:maxConnectionsPerHost/@value');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/MultithreadedHttpConnectionManagerConfig/maxTotalConnections/@value');
        if(defined($config_node)) {
            ($node)  = $xpc->findnodes('//ns:MultithreadedHttpConnectionManagerConfig/ns:maxTotalConnections/@value');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        print BPS_BPS $bps_bps->toString;
        close(BPS_BPS);
    }
    #-------------- end bps.xml -----------------------------------------------------------------------------------------

    #-------------- wso2-rss-config.xml -----------------------------------------------------------------------------------------
    my $dss_rss;
    if(-e $dss_rss_config_file) {
        $dss_rss = $parser->parse_file($dss_rss_config_file);
        open DSS_RSS, ">", $dss_rss_config_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/name/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/name/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/server-url/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/server-url/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/dbms-type/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/dbms-type/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/admin-username/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/admin-username/text()');
        if(defined($node)) {
            if(defined($dbuser)) {
                $node->setData($dbuser);
            } else {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/admin-password/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/admin-password/text()');
        if(defined($node)) {
            if(defined($dbpasswd)) {
                $node->setData($dbpasswd);
            } else {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/wso2-rss/wso2-rss-instances/wso2-rss-instance/server-category/text()');
        ($node)  = $dss_rss->findnodes('//wso2-rss-instances/wso2-rss-instance/server-category/text()');
        if(defined($node)) {
            $node->setData($config_node->getData());
        }

        print DSS_RSS $dss_rss->toString;
        close(DSS_RSS);
    }
    #-------------- end wso2-rss-config.xml -----------------------------------------------------------------------------------------

    #-------------- cassandra-auth.xml -----------------------------------------------------------------------------------------
    my $dss_cassandra_auth;
    if(-e $dss_cassandra_auth_file) {
        $dss_cassandra_auth = $parser->parse_file($dss_cassandra_auth_file);
        open DSS_CASSANDRA_AUTH, ">", $dss_cassandra_auth_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-auth/EPR/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_auth->findnodes('//Cassandra/EPR/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-auth/User/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_auth->findnodes('//Cassandra/User/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-auth/Password/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_auth->findnodes('//Cassandra/Password/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        print DSS_CASSANDRA_AUTH $dss_cassandra_auth->toString;
        close(DSS_CASSANDRA_AUTH);
    }
    #-------------- end cassandra-auth.xml -----------------------------------------------------------------------------------------

    #-------------- cassandra-component.xml -----------------------------------------------------------------------------------------
    my $dss_cassandra_component;
    if(-e $dss_cassandra_component_file) {
        $dss_cassandra_component = $parser->parse_file($dss_cassandra_component_file);
        open DSS_CASSANDRA_AUTH, ">", $dss_cassandra_component_target_file or die $!;

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-component/Cluster/Name/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_component->findnodes('//Cassandra/Cluster/Name/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-component/Cluster/DefaultPort/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_component->findnodes('//Cassandra/Cluster/DefaultPort/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-component/Cluster/Nodes/text()');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_component->findnodes('//Cassandra/Cluster/Nodes/text()');
            if(defined($node)) {
                $node->setData($config_node->getData());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-component/Cluster/AutoDiscovery/@disable');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_component->findnodes('//Cassandra/Cluster/AutoDiscovery/@disable');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        ($config_node)  = $config->findnodes("//$service_name".'/cassandra-component/Cluster/AutoDiscovery/@delay');
        if(defined($config_node)) {
            ($node)  = $dss_cassandra_component->findnodes('//Cassandra/Cluster/AutoDiscovery/@delay');
            if(defined($node)) {
                $node->setValue($config_node->getValue());
            }
        }

        print DSS_CASSANDRA_AUTH $dss_cassandra_component->toString;
        close(DSS_CASSANDRA_AUTH);
    }
    #-------------- end cassandra-component.xml -----------------------------------------------------------------------------------------

    #--------------  wso2server.sh -----------------------------------------------------------------------------------------
    my $xms;
    my $xmx;
    my $perm_size;
    my $max_perm_size;
    open(WSO2SERVER_CONFIG_IN, $wso2server_config_file) or die("Cannot Open File");
    my(@fcont) = <WSO2SERVER_CONFIG_IN>;
    close WSO2SERVER_CONFIG_IN;

    open WSO2SERVER_CONFIG, ">", $wso2server_config_target_file or die $!;
    ($node)  = $config->findnodes("//$service_name".'/wso2server/xms/text()');
    if(defined($node)) {
        $xms = $node->getData();
    }

    open WSO2SERVER_CONFIG, ">", $wso2server_config_target_file or die $!;
    ($node)  = $config->findnodes("//$service_name".'/wso2server/xmx/text()');
    if(defined($node)) {
        $xmx = $node->getData();
    }
    
    ($node)  = $config->findnodes("//$service_name".'/wso2server/perm_size/text()');
    if(defined($node)) {
        $perm_size = $node->getData();
    }

    ($node)  = $config->findnodes("//$service_name".'/wso2server/max_perm_size/text()');
    if(defined($node)) {
        $max_perm_size = $node->getData();
    }

    foreach $line (@fcont) {
        if(defined($xms)) {
            $line =~ s/(-Xms)([0-9]*m)/$1$xms/g;
        }
        if(defined($max_perm_size)) {
            $line =~ s/(-XX:MaxPermSize=)([0-9]*m)/$1$max_perm_size/g;
        }
        if(defined($perm_size)) {
            $line =~ s/(-XX:PermSize=)([0-9]*m)/$1$perm_size/g;
        }
        if(defined($xmx)) {
            $line =~ s/(-Xmx)([0-9]*m)/$1$xmx/g;
        }

        print WSO2SERVER_CONFIG $line;
    }
    close WSO2SERVER_CONFIG;

    #--------------  end wso2server.sh -----------------------------------------------------------------------------------------

    #------------------------------------------------------------------------------End Service Configuration-----------------------------------
}
1;


