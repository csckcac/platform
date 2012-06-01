#!/usr/bin/perl -w
use File::Copy;
require 'service_config.pl';
#use strict;
no warnings 'all' ;

my $packs_dir;
my $stratos_dir;
my $manager_version='1.1.0-SNAPSHOT';
my $as_version='4.5.0-SNAPSHOT';
my $stratos_version;
my $sso_enabled;
my $create_db;
my $config_file; 
my $base_dir;
my $product_list;
my $config_target_dir;
my $dbuser;
my $dbpasswd;

use File::Path;
use File::Basename;
use Getopt::Std;

getopts("hp:t:v:s:d:l:c:u:w:");

if ($opt_h) {
    usage();
    exit 0;
}

if ($opt_p) {
    $packs_dir = $opt_p;
}

if ($opt_t) {
    $stratos_dir = $opt_t;
}

if ($opt_v) {
    $stratos_version = $opt_v;
}

if ($opt_s) {
    $sso_enabled = $opt_s;
}

if ($opt_d) {
    $create_db = $opt_d;
}

if ($opt_l) {
    $product_list = $opt_l;
}

if ($opt_c) {
    $config_file = $opt_c;
}

if ($opt_u) {
    $dbuser = $opt_u;
}

if ($opt_w) {
    $dbpasswd = $opt_w;
}

if (!defined($stratos_dir) || $stratos_dir eq '') {
    usage();
    die "Please set a value for the option -t <stratos dir> \n";
}


if (!defined($sso_enabled) || $sso_enabled eq '') {
    usage();
    die "Please set a value for the option -s <sso enabled> true/false \n";
}

if (!defined($create_db) || $create_db eq '') {
    usage();
    die "Please set a value for the option -d <create db> true/false \n";
}

if (!defined($product_list) || $product_list eq '') {
    usage();
    die "Please set a value for the option -l <product list> \n";
}

if(!defined($config_file)) {
    usage();
    die "Please set a value for the option -c <stratos config file> \n";
}

if(!defined($dbuser)) {
    usage();
    die "Please set a value for the option -u <db user> \n";
}

if(!defined($dbpasswd)) {
    usage();
    die "Please set a value for the option -w <db passwd> \n";
}

sub usage {
    print STDERR  'Usage:./stratos-setup.pl -p <packs dir> -t <stratos dir> -v <stratos version> -s <sso enabled> -d <create db> -l <product list> -c <stratos config file> -u <db user> -w <db passwd> ';
    print STDERR "\n";
    print STDERR  'example:./stratos-setup.pl -p "/opt/stratos/packs" -t "/opt/stratos/deploy" -v "1.5.1" -s false -d false -l "manager is greg" -c ./stratos_config_min.xml -u root -w root';
    print STDERR "\n";
}
1;

#Check if the packs needs to be get from a single directory or <product name>/modules/distribution/service/target
if ($packs_dir ne '') {
	print "Using the packs in $packs_dir \n";	
}

$manager_enabled="false";
$is_enabled="false";
$greg_enabled="false";
$esb_enabled="false";
$bps_enabled="false";
$bam_enabled="false";
$brs_enabled="false";
$cep_enabled="false";
$ms_enabled="false";
$mb_enabled="false";
$gs_enabled="false";
$as_enabled="false";
$dss_enabled="false";


if(-d $stratos_dir) {
    if(-d "./stratos_deploy_backup") {
        system "rm -rf ./stratos_deploy_backup";
    }
    system "$stratos_dir/stratos.sh stop all";
    system "mv $stratos_dir ./stratos_deploy_backup";
    mkdir $stratos_dir or die "Cannot create $stratos_dir";
} else {
    mkdir $stratos_dir or die "Cannot create $stratos_dir";
}

copy("stratos.sh","$stratos_dir/stratos.sh") or die "Copy stratos.sh failed";

if ($product_list eq '') {
   $product_list="manager is greg as bam dss bps brs cep esb gs mb ms";
}

if ($product_list ne '') {

	use List::MoreUtils qw{ any };

	my @values = split(' ', $product_list);

    if (any { $_ eq 'manager'} @values) {
		$manager_enabled="true";
    }

    if (any { $_ eq 'is'} @values) {
		$is_enabled="true";
    }

    if (any { $_ eq 'greg'} @values) {
		$greg_enabled="true";
    }

    if (any { $_ eq 'esb'} @values) {
		$esb_enabled="true";
    }

    if (any { $_ eq 'bps'} @values) {
		$bps_enabled="true";
    }

    if (any { $_ eq 'bam'} @values) {
		$bam_enabled="true";
    }

    if (any { $_ eq 'brs'} @values) {
		$brs_enabled="true";
    }

    if (any { $_ eq 'cep'} @values) {
		$cep_enabled="true";
    }

    if (any { $_ eq 'ms'} @values) {
		$ms_enabled="true";
    }

    if (any { $_ eq 'mb'} @values) {
		$mb_enabled="true";
    }

    if (any { $_ eq 'gs'} @values) {
		$gs_enabled="true";
    }

    if (any { $_ eq 'as'} @values) {
		$as_enabled="true";
    }

    if (any { $_ eq 'dss'} @values) {
		$dss_enabled="true";
    }
}

# Common Configuration for all services

print "Changing common configuration files\n";
my $manager_base_dir = "setup/stratos";
my $manager_target_dir = "setup_target/stratos";
my $ret = config_common($config_file, $manager_base_dir, $manager_target_dir, $dbuser, $dbpasswd);
if($ret == 0) {
    die "Common cofniguratin initialization failed \n";
}	

if ($manager_enabled eq 'true') {
	print "\nSetting up the Stratos Manager...\n\n";
	print "Removing old files\n";
	system "rm -rf $stratos_dir/wso2stratos-manager-$manager_version";
	print "done\n";

	print "Unzipping\n";
	if ($packs_dir eq '') {
		system "unzip -qu $carbon_dir/products/manager/modules/distribution/service/target/wso2stratos-manager-$manager_version.zip -d $stratos_dir";
	}
	else {
		system "unzip -qu $packs_dir/wso2stratos-manager-$manager_version.zip -d $stratos_dir";
	}
	print "unzipping done\n";
    
	
	print "Copying mysql driver\n";
	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-manager-$manager_version/repository/components/lib";
	print "done\n";
	
	print "Changing manager configuration files\n";
    my $manager_base_dir = "setup/manager";
    my $manager_target_dir = "setup_target/manager";
    my $ret = config_manager($config_file, $manager_base_dir, $manager_target_dir, $sso_enabled, $dbuser, $dbpasswd);
    if($ret == 0) {
        die "Manager cofniguratin initialization failed \n";
    }	
	print "done\n";
	
	print "Copying Manager configuration files\n";
	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-manager-$manager_version/repository/conf";
	system "cp setup_target/manager/repository/conf/*.xml  $stratos_dir/wso2stratos-manager-$manager_version/repository/conf";
	system "cp setup_target/manager/repository/conf/security/*.xml  $stratos_dir/wso2stratos-manager-$manager_version/repository/conf/security";
	system "cp setup_target/manager/bin/*  $stratos_dir/wso2stratos-manager-$manager_version/bin";
    if(-e "setup_target/manager/repository/conf/datasources.properties") {
	    system "cp setup_target/manager/repository/conf/datasources.properties  $stratos_dir/wso2stratos-manager-$manager_version/repository/conf";
    }
	system "cp setup/manager/lib/home/index.html  $stratos_dir/wso2stratos-manager-$manager_version/repository/deployment/server/webapps/STRATOS_ROOT/";
	print "done\n";
	if ($sso_enabled eq 'true') {
	    print "SSO provider functionality is enabled\n";
	}
	else {
	    system "rm $stratos_dir/wso2stratos-manager-$manager_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
		print "SSO provider functionality is disabled\n";
	}
}

	
#if ($is_enabled eq 'true') {
#	print "\n\nSetting up Stratos Identity...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-is-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	if ($packs_dir eq '') {
#		system "unzip -qu $carbon_dir/products/is/modules/distribution/service/target/wso2stratos-is-$stratos_version.zip -d $stratos_dir";
#	}
#	else {
#	system "unzip -qu $packs_dir/wso2stratos-is-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-is-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing Identity configuration files\n";
#	my $is_base_dir = "setup/is";
#	my $is_target_dir = "setup_target/is";
#    my $ret = config_identity($config_file, $is_base_dir, $is_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "Identity cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#
#	print "Copying Identity configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-is-$stratos_version/repository/conf";
#	system "cp setup_target/is/repository/conf/*.xml  $stratos_dir/wso2stratos-is-$stratos_version/repository/conf";
#	system "cp setup_target/is/repository/conf/security/*.xml  $stratos_dir/wso2stratos-is-$stratos_version/repository/conf/security";
#	system "cp setup_target/is/bin/*  $stratos_dir/wso2stratos-is-$stratos_version/bin";
#    if(-e "setup_target/is/repository/conf/datasources.properties") {
#	    system "cp setup_target/is/repository/conf/datasources.properties  $stratos_dir/wso2stratos-is-$stratos_version/repository/conf";
#    }
#	print "done\n";
#
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-is-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#	
#
#
#
#if ($greg_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Governance...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-governance-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/greg/modules/distribution/service/target/wso2stratos-governance-$stratos_version.zip -d $stratos_dir";
#	}
#	else 
#	{
#	system "unzip -qu $packs_dir/wso2stratos-governance-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-governance-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing Governance configuration files\n";
#	my $governance_base_dir = "setup/governance";
#	my $governance_target_dir = "setup_target/governance";
#    my $ret = config_governance($config_file, $governance_base_dir, $governance_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "Governance cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	print "Copying Governance configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-governance-$stratos_version/repository/conf";
#	system "cp setup_target/governance/repository/conf/*.xml  $stratos_dir/wso2stratos-governance-$stratos_version/repository/conf";
#	system "cp setup_target/governance/repository/conf/security/*.xml  $stratos_dir/wso2stratos-governance-$stratos_version/repository/conf/security";
#	system "cp setup_target/governance/bin/*  $stratos_dir/wso2stratos-governance-$stratos_version/bin";
#    if(-e "setup_target/governance/repository/conf/datasources.properties") {
#	    system "cp setup_target/governance/repository/conf/datasources.properties  $stratos_dir/wso2stratos-governance-$stratos_version/repository/conf";
#    }
#	print "done\n";
#
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-governance-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#	
#
#
if ($as_enabled eq 'true') {
	print "\n\nSetting up the Appserver...\n\n";
	print "Removing old files\n";
	system "rm -rf $stratos_dir/wso2as-$as_version";
	print "done\n";
	
	print "Unzipping\n";
	
	if ($packs_dir eq '') 
	{
		system "unzip -qu $carbon_dir/products/as/modules/distribution/service/target/wso2as-$as_version.zip -d $stratos_dir";
	}
	else
	{
		system "unzip -qu $packs_dir/wso2as-$as_version.zip -d $stratos_dir";
	}
	print "done\n";
	
	print "Copying mysql driver\n";
	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2as-$as_version/repository/components/lib";
	print "done\n";
	
	print "Changing Application Server configuration files\n";
	my $as_base_dir = "setup/as";
	my $as_target_dir = "setup_target/as";
    my $ret = config_appserver($config_file, $as_base_dir, $as_target_dir, $sso_enabled, $dbuser, $dbpasswd);
    if($ret == 0) {
        die "Application Server cofniguratin initialization failed \n";
    }	
	print "done\n";
	
	print "Copying Application Server configuration files\n";

	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2as-$as_version/repository/conf";
	system "cp setup_target/as/repository/conf/*.xml  $stratos_dir/wso2as-$as_version/repository/conf";
	system "cp setup_target/as/repository/conf/security/*.xml  $stratos_dir/wso2as-$as_version/repository/conf/security";
	system "cp setup_target/as/bin/*  $stratos_dir/wso2as-$as_version/bin";
    if(-e "setup_target/as/repository/conf/datasources.properties") {
	    system "cp setup_target/as/repository/conf/datasources.properties  $stratos_dir/wso2as-$as_version/repository/conf";
    }
	print "done\n";
	
	if ($sso_enabled eq 'true') {
		print "SSO provider functionality is enabled\n";
	}
	else
	{
	    system "rm $stratos_dir/wso2as-$as_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
		print "SSO provider functionality is disabled\n";
	}
}

exit;
#	
#
#
#if ($bam_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Business Activity Monitor...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-bam-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/bam/modules/distribution/service/target/wso2stratos-bam-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-bam-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-bam-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing BAM configuration files\n";
#	my $bam_base_dir = "setup/bam";
#	my $bam_target_dir = "setup_target/bam";
#    my $ret = config_bam($config_file, $bam_base_dir, $bam_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "BAM cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	print "Copying BAM configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf";
#	system "cp setup_target/bam/repository/conf/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf";
#	system "cp setup_target/bam/repository/conf/security/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf/security";
#	system "cp setup_target/bam/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf/etc";
#	system "cp setup_target/bam/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/bam/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/bam/bin/*  $stratos_dir/wso2stratos-bam-$stratos_version/bin";
#    if(-e "setup_target/bam/repository/conf/datasources.properties") {
#	    system "cp setup_target/bam/repository/conf/datasources.properties  $stratos_dir/wso2stratos-bam-$stratos_version/repository/conf";
#    }
#	print "done\n";
#
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#    }
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-bam-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#	print "SSO provider functionality is disabled\n";
#	}
#}	
#
#
#if ($bps_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Business Process Server...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-bps-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/bps/modules/distribution/service/target/wso2stratos-bps-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-bps-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-bps-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing BPS configuration files\n";
#	my $bps_base_dir = "setup/bps";
#	my $bps_target_dir = "setup_target/bps";
#    my $ret = config_bps($config_file, $bps_base_dir, $bps_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "BPS cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	print "Copying BPS configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf";
#	system "cp setup_target/bps/repository/conf/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf";
#	system "cp setup_target/bps/repository/conf/security/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf/security";
#	system "cp setup_target/bps/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf/etc";
#	system "cp setup_target/bps/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/bps/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/bps/bin/*  $stratos_dir/wso2stratos-bps-$stratos_version/bin";
#    if(-e "setup_target/bps/repository/conf/datasources.properties") {
#	    system "cp setup_target/bps/repository/conf/datasources.properties  $stratos_dir/wso2stratos-bps-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-bps-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#
#	
#if ($brs_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Business Rules Server...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-brs-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/brs/modules/distribution/service/target/wso2stratos-brs-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#	system "unzip -qu $packs_dir/wso2stratos-brs-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-brs-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing BRS configuration files\n";
#	my $brs_base_dir = "setup/brs";
#	my $brs_target_dir = "setup_target/brs";
#    my $ret = config_brs($config_file, $brs_base_dir, $brs_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "BRS cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#    
#    print "Copying BRS configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-brs-$stratos_version/repository/conf";
#	system "cp setup_target/brs/repository/conf/*.xml  $stratos_dir/wso2stratos-brs-$stratos_version/repository/conf";
#	system "cp setup_target/brs/repository/conf/security/*.xml  $stratos_dir/wso2stratos-brs-$stratos_version/repository/conf/security";
#	system "cp setup_target/brs/bin/*  $stratos_dir/wso2stratos-brs-$stratos_version/bin";
#    if(-e "setup_target/brs/repository/conf/datasources.properties") {
#        system "cp setup_target/brs/repository/conf/datasources.properties  $stratos_dir/wso2stratos-brs-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-brs-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#	
#
#if ($cep_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Complex Event Processing Server...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-cep-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/cep/modules/distribution/service/target/wso2stratos-cep-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-cep-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-cep-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing CEP configuration files\n";
#	my $cep_base_dir = "setup/cep";
#	my $cep_target_dir = "setup_target/cep";
#    my $ret = config_cep($config_file, $cep_base_dir, $cep_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "CEP cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	
#	print "Copying CEP configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf";
#	system "cp setup_target/cep/repository/conf/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf";
#	system "cp setup_target/cep/repository/conf/security/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf/security";
#	system "cp setup_target/cep/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf/etc";
#	system "cp setup_target/cep/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/cep/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/cep/bin/*  $stratos_dir/wso2stratos-cep-$stratos_version/bin";
#    if(-e "setup_target/cep/repository/conf/datasources.properties") {
#	    system "cp setup_target/cep/repository/conf/datasources.properties  $stratos_dir/wso2stratos-cep-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-cep-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#	
#
#if ($dss_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Data Serviecs Server...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-dss-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/dss/modules/distribution/service/target/wso2stratos-dss-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-dss-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-dss-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing DSS configuration files\n";
#	my $dss_base_dir = "setup/dss";
#	my $dss_target_dir = "setup_target/dss";
#    my $ret = config_dss($config_file, $dss_base_dir, $dss_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "DSS cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	
#	print "Copying DSS configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-dss-$stratos_version/repository/conf";
#	system "cp setup_target/dss/repository/conf/*.xml  $stratos_dir/wso2stratos-dss-$stratos_version/repository/conf";
#	system "cp setup_target/dss/repository/conf/security/*.xml  $stratos_dir/wso2stratos-dss-$stratos_version/repository/conf/security";
#	system "cp setup_target/dss/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-dss-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/dss/bin/*  $stratos_dir/wso2stratos-dss-$stratos_version/bin";
#    if(-e "setup_target/dss/repository/conf/datasources.properties") {
#        system "cp setup_target/dss/repository/conf/datasources.properties  $stratos_dir/wso2stratos-dss-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-dss-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#
#	
#
#if ($esb_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Enterprice Service Bus...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-esb-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/esb/modules/distribution/service/target/wso2stratos-esb-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-esb-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-esb-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing ESB configuration files\n";
#	my $esb_base_dir = "setup/esb";
#	my $esb_target_dir = "setup_target/esb";
#    my $ret = config_esb($config_file, $esb_base_dir, $esb_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "ESB cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	
#	print "Copying ESB configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-esb-$stratos_version/repository/conf";
#	system "cp setup_target/esb/repository/conf/*.xml  $stratos_dir/wso2stratos-esb-$stratos_version/repository/conf";
#	system "cp setup_target/esb/repository/conf/security/*.xml  $stratos_dir/wso2stratos-esb-$stratos_version/repository/conf/security";
#	system "cp setup_target/esb/bin/*  $stratos_dir/wso2stratos-esb-$stratos_version/bin";
#    if(-e "setup_target/esb/repository/conf/datasources.properties") {
#        system "cp setup_target/esb/repository/conf/datasources.properties  $stratos_dir/wso2stratos-esb-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-esb-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#
#	
#
#if ($gs_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Gadgets...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-gs-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/gs/modules/distribution/service/target/wso2stratos-gs-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-gs-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-gs-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing GS configuration files\n";
#	my $gs_base_dir = "setup/gs";
#	my $gs_target_dir = "setup_target/gs";
#    my $ret = config_gs($config_file, $gs_base_dir, $gs_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "GS cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	print "Copying GS configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf";
#	system "cp setup_target/gs/repository/conf/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf";
#	system "cp setup_target/gs/repository/conf/security/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf/security";
#	system "cp setup_target/gs/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf/etc";
#	system "cp setup_target/gs/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/gs/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/gs/bin/*  $stratos_dir/wso2stratos-gs-$stratos_version/bin";
#    if(-e "setup_target/gs/repository/conf/datasources.properties") {
#	    system "cp setup_target/gs/repository/conf/datasources.properties  $stratos_dir/wso2stratos-gs-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-gs-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
#	
#
#if ($mb_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Message Broker...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-mb-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#	system "unzip -qu $carbon_dir/products/mb/modules/distribution/service/target/wso2stratos-mb-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-mb-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-mb-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing MB configuration files\n";
#	my $mb_base_dir = "setup/mb";
#	my $mb_target_dir = "setup_target/mb";
#    my $ret = config_mb($config_file, $mb_base_dir, $mb_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "MB cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	print "Copying MB configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf";
#	system "cp setup_target/mb/repository/conf/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf";
#	system "cp setup_target/mb/repository/conf/security/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf/security";
#	system "cp setup_target/mb/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf/etc";
#	system "cp setup_target/mb/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/mb/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/mb/bin/*  $stratos_dir/wso2stratos-mb-$stratos_version/bin";
#    if(-e "setup_target/mb/repository/conf/datasources.properties") {
#	    system "cp setup_target/mb/repository/conf/datasources.properties  $stratos_dir/wso2stratos-mb-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-mb-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}	
#
#	
#if ($ms_enabled eq 'true') {
#	print "\n\nSetting up the Stratos Mashup Server...\n\n";
#	print "Removing old files\n";
#	system "rm -rf $stratos_dir/wso2stratos-ms-$stratos_version";
#	print "done\n";
#	
#	print "Unzipping\n";
#	
#	if ($packs_dir eq '') 
#	{
#		system "unzip -qu $carbon_dir/products/ms/modules/distribution/service/target/wso2stratos-ms-$stratos_version.zip -d $stratos_dir";
#	}
#	else
#	{
#		system "unzip -qu $packs_dir/wso2stratos-ms-$stratos_version.zip -d $stratos_dir";
#	}
#	print "done\n";
#	
#	print "Copying mysql driver\n";
#	system "cp setup/stratos/jars/mysql-connector-java-5.1.12-bin.jar $stratos_dir/wso2stratos-ms-$stratos_version/repository/components/lib";
#	print "done\n";
#	
#	print "Changing MS configuration files\n";
#	my $ms_base_dir = "setup/ms";
#	my $ms_target_dir = "setup_target/ms";
#    my $ret = config_mashup($config_file, $ms_base_dir, $ms_target_dir, $sso_enabled, $dbuser, $dbpasswd);
#    if($ret == 0) {
#        die "MS cofniguratin initialization failed \n";
#    }	
#	print "done\n";
#	
#	
#	print "Copying MS configuration files\n";
#	system "cp setup_target/stratos/repository/conf/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf";
#	system "cp setup_target/ms/repository/conf/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf";
#	system "cp setup_target/ms/repository/conf/security/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf/security";
#	system "cp setup_target/ms/repository/conf/etc/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf/etc";
#	system "cp setup_target/ms/repository/conf/tomcat/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf/tomcat";
#	system "cp setup_target/ms/repository/conf/advanced/*.xml  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf/advanced";
#	system "cp setup_target/ms/bin/*  $stratos_dir/wso2stratos-ms-$stratos_version/bin";
#    if(-e "setup_target/ms/repository/conf/datasources.properties") {
#	    system "cp setup_target/ms/repository/conf/datasources.properties  $stratos_dir/wso2stratos-ms-$stratos_version/repository/conf";
#    }
#	print "done\n";
#	
#	if ($sso_enabled eq 'true') {
#		print "SSO provider functionality is enabled\n";
#	}
#	else
#	{
#	    system "rm $stratos_dir/wso2stratos-ms-$stratos_version/repository/components/plugins/org.wso2.carbon.identity.authenticator.saml2.sso*";
#		print "SSO provider functionality is disabled\n";
#	}
#}
	
if ($create_db eq 'true') {
	if ($manager_enabled eq 'true') {
        #system "find setup/dbscripts -name \"*.sql\"|xargs rm -f";
		system "cp $stratos_dir/wso2stratos-manager-$stratos_version/dbscripts/mysql.sql setup/dbscripts/";
		system "cp $stratos_dir/wso2stratos-manager-$stratos_version/dbscripts/billing-mysql.sql setup/dbscripts/";
		system "cp $stratos_dir/wso2stratos-manager-$stratos_version/dbscripts/bam/bam_schema_mysql.sql setup/dbscripts";
		system "cp $stratos_dir/wso2stratos-manager-$stratos_version/dbscripts/metering_mysql.sql setup/dbscripts";
		system "cp $stratos_dir/wso2stratos-manager-$stratos_version/dbscripts/wso2_rss.sql setup/dbscripts";
		system "cp setup/init_quaries.sql setup/dbscripts";
		print "Dropping the existing database...\n";
		chdir('setup/dbscripts') or die "$!";
		system "mysql -u $dbuser --password=$dbpasswd < init_quaries.sql";
		print "Creating a fresh database...\n";
		system "mysql -u $dbuser --password=$dbpasswd registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd manager_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd appserver_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd bam_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd bps_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd brs_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd dss_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd esb_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd gadget_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd identity_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd mashup_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd cep_registry < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd mb_registry < ./mysql.sql";

		system "mysql -u $dbuser --password=$dbpasswd bps < bps/bpel/mysql.sql";


		system "mysql -u $dbuser --password=$dbpasswd userstore < ./mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd billing < ./billing-mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd billing < ./bam_schema_mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd billing < ./metering_mysql.sql";
		system "mysql -u $dbuser --password=$dbpasswd rss_database < ./wso2_rss.sql";
		chdir('platform_samples') or die "$!";
		system "mysql -u $dbuser --password=$dbpasswd < ./Init_Sample_Query.sql";
		chdir('../../../') or die "$!";
	    print "done\n";
	}
	else {
		print "\n WARNING: Cloud Manager should be enabled to create new databases\n";
	}
}
else {
	print "Existing database used\n"
}
	
	print "\nplease add following lines to your /etc/hosts file\n\n";
	print "127.0.0.1 cloud-test.wso2.com\n";
	print "127.0.0.1 identity.cloud-test.wso2.com\n";
	print "127.0.0.1 governance.cloud-test.wso2.com\n";
	print "127.0.0.1 appserver.cloud-test.wso2.com\n";
	print "127.0.0.1 monitor.cloud-test.wso2.com\n";
	print "127.0.0.1 data.cloud-test.wso2.com\n";
	print "127.0.0.1 process.cloud-test.wso2.com\n";
	print "127.0.0.1 rule.cloud-test.wso2.com\n";
	print "127.0.0.1 cep.cloud-test.wso2.com\n";
	print "127.0.0.1 esb.cloud-test.wso2.com\n";
	print "127.0.0.1 gadget.cloud-test.wso2.com\n";
	print "127.0.0.1 messaging.cloud-test.wso2.com\n";
        print "127.0.0.1 mashup.cloud-test.wso2.com\n";

