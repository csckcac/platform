package org.wso2.platform.test.core.utils.environmentutils;

import java.util.Map;

public class ManageEnvironment {
    private EnvironmentVariables as;
    private EnvironmentVariables esb;
    private EnvironmentVariables is;
    private EnvironmentVariables bps;
    private EnvironmentVariables greg;
    private EnvironmentVariables dss;
    private EnvironmentVariables bam;
    private EnvironmentVariables brs;
    private EnvironmentVariables gs;
    private EnvironmentVariables mb;
    private EnvironmentVariables ms;
    private EnvironmentVariables cep;
    private EnvironmentVariables manager;
    private EnvironmentVariables cluster;
    private Map<String,EnvironmentVariables> clusterMap;

    public EnvironmentVariables getAs() {
        return as;
    }

    public EnvironmentVariables getEsb() {
        return esb;
    }

    public EnvironmentVariables getIs() {
        return is;
    }

    public EnvironmentVariables getBps() {
        return bps;
    }

    public EnvironmentVariables getGreg() {
        return greg;
    }

    public EnvironmentVariables getBam() {
        return bam;
    }

    public EnvironmentVariables getBrs() {
        return brs;
    }

    public EnvironmentVariables getGs() {
        return gs;
    }

    public EnvironmentVariables getMb() {
        return mb;
    }

    public EnvironmentVariables getMs() {
        return ms;
    }

    public EnvironmentVariables getCep() {
        return cep;
    }

    public EnvironmentVariables getDss() {
        return dss;
    }

    public EnvironmentVariables getManager() {
        return manager;
    }
    public EnvironmentVariables getClusterNode(String node) {
        cluster=clusterMap.get(node);
        return cluster;
    }

    ManageEnvironment(EnvironmentBuilder builder) {
        this.as = builder.as;
        this.esb = builder.esb;
        this.is = builder.is;
        this.bps = builder.bps;
        this.greg = builder.greg;
        this.dss = builder.dss;
        this.bam = builder.bam;
        this.brs = builder.brs;
        this.gs = builder.gs;
        this.mb = builder.mb;
        this.ms = builder.ms;
        this.cep = builder.cep;
        this.manager = builder.manager;
        this.clusterMap=builder.clusterMap;
    }
}

