package org.wso2.carbon.mapred.mgt;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HadoopJobRunnerActivator implements BundleActivator {

	private Log log = LogFactory.getLog(HadoopJobRunnerActivator.class);
	@Override
	public void start(BundleContext bc) throws Exception {
		log.info("Starting HadoopJobRunner bundle.");
		ServiceFactory serviceFactory = new HadoopJobRunnerFactory();
		bc.registerService(HadoopJobRunnerFactory.class.getName(), serviceFactory, new Hashtable());
		log.info("Registered HadoopJobRunner service.");
	}

	@Override
	public void stop(BundleContext bc) throws Exception {
		bc.ungetService(bc.getServiceReference(HadoopJobRunnerFactory.class.getName()));
		log.info("Stopping HadoopJobRunner bundle");
	}

}
