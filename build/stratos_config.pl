#!/usr/bin/perl -w

my $config_file; 
my $base_dir;
my $product_list;
my $config_target_dir;

use File::Path;
use File::Basename;
use Getopt::Std;

require 'service_config.pl';

getopts("c:b:p:v:t:s:");

if ($opt_c) {
    $config_file = $opt_c;
}

if ($opt_b) {
    $config_base_dir = $opt_b;
}

if ($opt_p) {
    $product_list = $opt_p;
}
else {
    $product_list = "manager identity governance"
}

if ($opt_t) {
    $config_target_dir = $opt_t;
} 

if ($opt_s) {
    $sso_enabled = $opt_s;
} else {
    $sso_enabled = 'true';
}

if(!defined($config_file)) {
    print STDERR  'Usage:./stratos_config.pl -c <stratos_config_file> -b <stratos_dir> [-s <product list>] [-t <stratos_target_dir>] [-s <sso_enabled>]';
    print STDERR "\n";
    print STDERR  'example:./stratos_config.pl -c ./stratos_config.xml -b ./stratoslive -s "manager identity" -t ./stratoslive_new -s true';
    print STDERR "\n";
    die;
}

if(!defined($config_base_dir)) {
    print STDERR  'Usage:./stratos_config.pl -c <stratos_config_file> -b <stratos_dir> [-s <product list>] [-t <stratos_target_dir>] [-s <sso_enabled>]';
    print STDERR "\n";
    print STDERR  'example:./stratos_config.pl -c ./stratos_config.xml -b ./stratoslive -s "manager identity" -t ./stratoslive_new -s true';
    print STDERR "\n";
    die;
}

if(!defined($config_target_dir)) {
    $config_target_dir = $config_base_dir;
}

config_stratos($config_file, $config_base_dir, $product_list, $config_target_dir, $sso_enabled);

sub config_stratos {
    my $config_file = $_[0];
    my $config_base_dir = $_[1];
    my $product_list = $_[2];
    my $config_target_dir = $_[3];
    my $sso_enabled = $_[4];
    my $base_dir;
    my $target_dir;
    my $ret;

    {
        $base_dir = "$config_base_dir/stratos";
        $target_dir = "$config_target_dir/stratos";
        $ret = config_common($config_file, $base_dir, $target_dir);
        if($ret == 0) {
            die "Cofniguratin initialization failed \n";
        }
    }

    if ($product_list ne '') {

        use List::MoreUtils qw{ any };

        my @values = split(' ', $product_list);

        if (any { $_ eq 'manager'} @values) {
            $base_dir = "$config_base_dir/manager";
            $target_dir = "$config_target_dir/manager";
            $ret = config_manager($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'is'} @values) {
            $base_dir = "$config_base_dir/identity";
            $target_dir = "$config_target_dir/identity";
            $ret = config_identity($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'greg'} @values) {
            $base_dir = "$config_base_dir/governance";
            $target_dir = "$config_target_dir/governance";
            $ret = config_governance($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'esb'} @values) {
            $base_dir = "$config_base_dir/esb";
            $target_dir = "$config_target_dir/esb";
            $ret = config_esb($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'bps'} @values) {
            $base_dir = "$config_base_dir/bps";
            $target_dir = "$config_target_dir/bps";
            $ret = config_bps($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'bam'} @values) {
            $base_dir = "$config_base_dir/bam";
            $target_dir = "$config_target_dir/bam";
            $ret = config_bam($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'brs'} @values) {
            $base_dir = "$config_base_dir/brs";
            $target_dir = "$config_target_dir/brs";
            $ret = config_brs($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'cep'} @values) {
            $base_dir = "$config_base_dir/cep";
            $target_dir = "$config_target_dir/cep";
        }

        if (any { $_ eq 'ms'} @values) {
            $base_dir = "$config_base_dir/mashup";
            $target_dir = "$config_target_dir/mashup";
            $ret = config_mashup($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'mb'} @values) {
            $base_dir = "$config_base_dir/mb";
            $target_dir = "$config_target_dir/mb";
        }

        if (any { $_ eq 'gs'} @values) {
            $base_dir = "$config_base_dir/gs";
            $target_dir = "$config_target_dir/gs";
        }
        if (any { $_ eq 'as'} @values) {
            $base_dir = "$config_base_dir/appserver";
            $target_dir = "$config_target_dir/appserver";
            $ret = config_appserver($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

        if (any { $_ eq 'dss'} @values) {
            $base_dir = "$config_base_dir/dss";
            $target_dir = "$config_target_dir/dss";
            $ret = config_dss($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }
        if (any { $_ eq 'summarizer'} @values) {
            $base_dir = "$config_base_dir/summarizer";
            $target_dir = "$config_target_dir/summarizer";
            $ret = config_summarizer($config_file, $base_dir, $target_dir, $sso_enabled);
            if($ret == 0) {
                die "Cofniguratin initialization failed \n";
            }
        }

    }
}
1;


