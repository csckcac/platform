#!/usr/bin/perl -w

use File::Path;
use File::Basename;
use Getopt::Std;

require 'common_config.pl';
require 'specific_config.pl';

sub config_common {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $dbuser = $_[3];
    my $dbpasswd = $_[4];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        #system("perl common_config.pl $config_file $base_dir $target_dir");
        stratos_config_common($config_file, $base_dir, $target_dir, $dbuser, $dbpasswd);
        return 1;
    } else {
        print STDERR "Initialization failed for Stratos", "\n";
        return 0;
    }
}
1;

sub config_manager {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        #system("perl manager_config.pl $config_file $base_dir $target_dir $sso_enabled");
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "manager");
        return 1;
    } else {
        print STDERR "Initialization failed for manager", "\n";
        return 0;
    }
}
1;

sub config_identity {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "identity");
        return 1;
    } else {
        print STDERR "Initialization failed for identity", "\n";
        return 0;
    }
}
1;

sub config_governance {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "governance");
        return 1;
    } else {
        print STDERR "Initialization failed for governance", "\n";
        return 0;
    }
}
1;

sub config_esb {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "esb");
        return 1;
    } else {
        print STDERR "Initialization failed for esb", "\n";
        return 0;
    }
}
1;

sub config_bps {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "bps");
        return 1;
    } else {
        print STDERR "Initialization failed for bps", "\n";
        return 0;
    }
}
1;

sub config_bam {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "bam");
        return 1;
    } else {
        print STDERR "Initialization failed for bam", "\n";
        return 0;
    }
}
1;

sub config_brs {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "brs");
        return 1;
    } else {
        print STDERR "Initialization failed for brs", "\n";
        return 0;
    }
}
1;

sub config_mashup {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "mashup");
        return 1;
    } else {
        print STDERR "Initialization failed for mashup", "\n";
        return 0;
    }
}
1;

sub config_appserver {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "appserver");
        return 1;
    } else {
        print STDERR "Initialization failed for appserver", "\n";
        return 0;
    }
}
1;

sub config_dss {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "dss");
        return 1;
    } else {
        print STDERR "Initialization failed for dss", "\n";
        return 0;
    }
}
1;

sub config_gs {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "gs");
        return 1;
    } else {
        print STDERR "Initialization failed for gs", "\n";
        return 0;
    }
}
1;

sub config_mb {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "mb");
        return 1;
    } else {
        print STDERR "Initialization failed for mb", "\n";
        return 0;
    }
}
1;

sub config_cep {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "cep");
        return 1;
    } else {
        print STDERR "Initialization failed for cep", "\n";
        return 0;
    }
}
1;

sub config_summarizer {
    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];
    my $sso_enabled = $_[3];
    my $dbuser = $_[4];
    my $dbpasswd = $_[5];

    my $ret = stratos_config_init($config_file, $base_dir, $target_dir);
    if($ret > 0) {
        stratos_config_service($config_file, $base_dir, $target_dir, $sso_enabled, $dbuser, $dbpasswd, "summarizer");
        return 1;
    } else {
        print STDERR "Initialization failed for summarizer", "\n";
        return 0;
    }
}
1;

sub stratos_config_init {

    my $config_file = $_[0];
    my $base_dir = $_[1];
    my $target_dir = $_[2];

    if(!defined($config_file)) {
        print STDERR  "configu file is not given\n";
        return 0;
    }

    if(!defined($base_dir)) {
        print STDERR  "base directory is not defined\n";
        return 0;
    }

    if(!defined($target_dir)) {
        $target_dir = $base_dir;
    }

    my $stratos_conf_dir = "$target_dir/repository/conf";
    my $stratos_conf_advanced_dir = "$target_dir/repository/conf/advanced";
    my $stratos_conf_security_dir = "$target_dir/repository/conf/security";
    my $stratos_conf_axis2_dir = "$target_dir/repository/conf/axis2";
    #my $stratos_conf_email_dir = "$target_dir/repository/conf/email";
    my $stratos_conf_etc_dir = "$target_dir/repository/conf/etc";
    my $stratos_conf_tomcat_dir = "$target_dir/repository/conf/tomcat";
    my $stratos_bin_dir = "$target_dir/bin";
    unless(-d $stratos_conf_dir) {
        mkpath $stratos_conf_dir or die;
    }
    unless(-d $stratos_conf_advanced_dir) {
        mkpath $stratos_conf_advanced_dir or die;
    }
    unless(-d $stratos_bin_dir) {
        mkpath $stratos_bin_dir or die;
    }
    unless(-d $stratos_conf_security_dir) {
        mkpath $stratos_conf_security_dir or die;
    }
    unless(-d $stratos_conf_axis2_dir) {
        mkpath $stratos_conf_axis2_dir or die;
    }
    #unless(-d $stratos_conf_email_dir) {
     #   mkpath $stratos_conf_email_dir or die;
    #}
    unless(-d $stratos_conf_etc_dir) {
        mkpath $stratos_conf_etc_dir or die;
    }
    unless(-d $stratos_conf_tomcat_dir) {
        mkpath $stratos_conf_tomcat_dir or die;
    }
}
1;

