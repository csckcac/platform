package org.wso2.platform.test.core.utils.frameworkutils;

import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.AsSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.BamSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.BpsSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.BrsSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.CepSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.ClusterSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.DssSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.EsbSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.GregSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.GsSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.IsSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.ManagerSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.MbSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productsetters.MsSetter;

public class FrameworkFactory {
    static FrameworkProperties properties = new FrameworkProperties();

    public static FrameworkProperties getFrameworkProperties(String product) {
        if (product.equals(ProductConstant.BPS_SERVER_NAME)) {
            BpsSetter bpsSetter = new BpsSetter();
            properties.setDataSource(bpsSetter.getDataSource());
            properties.setEnvironmentSettings(bpsSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(bpsSetter.getEnvironmentVariables());
            properties.setRavana(bpsSetter.getRavana());
            properties.setSelenium(bpsSetter.getSelenium());
            properties.setProductVariables(bpsSetter.getProductVariables());
        }


        if (product.equals(ProductConstant.APP_SERVER_NAME)) {

            AsSetter asSetter = new AsSetter();
            properties.setDataSource(asSetter.getDataSource());
            properties.setEnvironmentSettings(asSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(asSetter.getEnvironmentVariables());
            properties.setRavana(asSetter.getRavana());
            properties.setSelenium(asSetter.getSelenium());
            properties.setProductVariables(asSetter.getProductVariables());
        }


        if (product.equals(ProductConstant.ESB_SERVER_NAME)) {
            EsbSetter esbSetter = new EsbSetter();
            properties.setDataSource(esbSetter.getDataSource());
            properties.setEnvironmentSettings(esbSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(esbSetter.getEnvironmentVariables());
            properties.setRavana(esbSetter.getRavana());
            properties.setSelenium(esbSetter.getSelenium());
            properties.setProductVariables(esbSetter.getProductVariables());
        }


        if (product.equals(ProductConstant.DSS_SERVER_NAME)) {
            DssSetter dssSetter = new DssSetter();
            properties.setDataSource(dssSetter.getDataSource());
            properties.setEnvironmentSettings(dssSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(dssSetter.getEnvironmentVariables());
            properties.setRavana(dssSetter.getRavana());
            properties.setSelenium(dssSetter.getSelenium());
            properties.setProductVariables(dssSetter.getProductVariables());
        }


        if (product.equals(ProductConstant.IS_SERVER_NAME)) {
            IsSetter isSetter = new IsSetter();
            properties.setDataSource(isSetter.getDataSource());
            properties.setEnvironmentSettings(isSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(isSetter.getEnvironmentVariables());
            properties.setRavana(isSetter.getRavana());
            properties.setSelenium(isSetter.getSelenium());
            properties.setProductVariables(isSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.BRS_SERVER_NAME)) {
            BrsSetter brsSetter = new BrsSetter();
            properties.setDataSource(brsSetter.getDataSource());
            properties.setEnvironmentSettings(brsSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(brsSetter.getEnvironmentVariables());
            properties.setRavana(brsSetter.getRavana());
            properties.setSelenium(brsSetter.getSelenium());
            properties.setProductVariables(brsSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.CEP_SERVER_NAME)) {
            CepSetter cepSetter = new CepSetter();
            properties.setDataSource(cepSetter.getDataSource());
            properties.setEnvironmentSettings(cepSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(cepSetter.getEnvironmentVariables());
            properties.setRavana(cepSetter.getRavana());
            properties.setSelenium(cepSetter.getSelenium());
            properties.setProductVariables(cepSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.GREG_SERVER_NAME)) {
            GregSetter gregSetter = new GregSetter();
            properties.setDataSource(gregSetter.getDataSource());
            properties.setEnvironmentSettings(gregSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(gregSetter.getEnvironmentVariables());
            properties.setRavana(gregSetter.getRavana());
            properties.setSelenium(gregSetter.getSelenium());
            properties.setProductVariables(gregSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.GS_SERVER_NAME)) {
            GsSetter gsSetter = new GsSetter();
            properties.setDataSource(gsSetter.getDataSource());
            properties.setEnvironmentSettings(gsSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(gsSetter.getEnvironmentVariables());
            properties.setRavana(gsSetter.getRavana());
            properties.setSelenium(gsSetter.getSelenium());
            properties.setProductVariables(gsSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.MB_SERVER_NAME)) {
            MbSetter mbSetter = new MbSetter();
            properties.setDataSource(mbSetter.getDataSource());
            properties.setEnvironmentSettings(mbSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(mbSetter.getEnvironmentVariables());
            properties.setRavana(mbSetter.getRavana());
            properties.setSelenium(mbSetter.getSelenium());
            properties.setProductVariables(mbSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.MS_SERVER_NAME)) {
            MsSetter msSetter = new MsSetter();
            properties.setDataSource(msSetter.getDataSource());
            properties.setEnvironmentSettings(msSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(msSetter.getEnvironmentVariables());
            properties.setRavana(msSetter.getRavana());
            properties.setSelenium(msSetter.getSelenium());
            properties.setProductVariables(msSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.BAM_SERVER_NAME)) {
            BamSetter bamSetter = new BamSetter();
            properties.setDataSource(bamSetter.getDataSource());
            properties.setEnvironmentSettings(bamSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(bamSetter.getEnvironmentVariables());
            properties.setRavana(bamSetter.getRavana());
            properties.setSelenium(bamSetter.getSelenium());
            properties.setProductVariables(bamSetter.getProductVariables());
        }


        if (product.equals(ProductConstant.MANAGER_SERVER_NAME)) {
            ManagerSetter manSetter = new ManagerSetter();
            properties.setDataSource(manSetter.getDataSource());
            properties.setEnvironmentSettings(manSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(manSetter.getEnvironmentVariables());
            properties.setRavana(manSetter.getRavana());
            properties.setSelenium(manSetter.getSelenium());
            properties.setProductVariables(manSetter.getProductVariables());
        }

        if (product.equals(ProductConstant.CLUSTER)) {
            ClusterSetter clusterSetter = new ClusterSetter();
            properties.setDataSource(clusterSetter.getDataSource());
            properties.setEnvironmentSettings(clusterSetter.getEnvironmentSettings());
            properties.setEnvironmentVariables(clusterSetter.getEnvironmentVariables());
            properties.setRavana(clusterSetter.getRavana());
            properties.setSelenium(clusterSetter.getSelenium());
            //properties.setProductVariables(clusterSetter.getProductVariables(cluster));
        }

        return properties;
    }

    public static FrameworkProperties getClusterProperties(String cluster) {

        ClusterSetter clusterSetter = new ClusterSetter();
        properties.setDataSource(clusterSetter.getDataSource());
        properties.setEnvironmentSettings(clusterSetter.getEnvironmentSettings());
        properties.setEnvironmentVariables(clusterSetter.getEnvironmentVariables());
        properties.setRavana(clusterSetter.getRavana());
        properties.setSelenium(clusterSetter.getSelenium());
        properties.setProductVariables(clusterSetter.getProductVariables(cluster));
        return properties;
    }

}
