package org.wso2.carbon.logging.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;

public class LoggingConfigManager {

	private static final Log log = LogFactory.getLog(LoggingConfigManager.class);
	private static LoggingConfigManager cassandraConfig;
	private static BundleContext bundleContext;

	public static LoggingConfigManager getCassandraConfig() {
		return cassandraConfig;
	}

	public static void setBundleContext(BundleContext bundleContext) {
		LoggingConfigManager.bundleContext = bundleContext;
	}

	public static void setCassandraConfig(LoggingConfigManager syslogConfig) {
		LoggingConfigManager.cassandraConfig = syslogConfig;
	}

	public static Log getLog() {
		return log;
	}

	public LoggingConfig getSyslogData() {
		return null;
	}

	/**
	 * Returns the configurations from the Cassandra configuration file.
	 * 
	 * @return cassandra configurations
	 */
	public static LoggingConfig loadCassandraConfiguration() {
		// gets the configuration file name from the cassandra-config.xml.
		String cassandraConfigFileName = CarbonUtils.getCarbonConfigDirPath()
				+ RegistryConstants.PATH_SEPARATOR
				+ LoggingConstants.ETC_DIR
				+ RegistryConstants.PATH_SEPARATOR
				+ LoggingConstants.LOGGING_CONF_FILE;
		return loadCassandraConfiguration(cassandraConfigFileName);
	}

	private InputStream getInputStream(String configFilename)
			throws IOException {
		InputStream inStream = null;
		File configFile = new File(configFilename);
		if (configFile.exists()) {
			inStream = new FileInputStream(configFile);
		}
		String warningMessage = "";
		if (inStream == null) {
			URL url;
			if (bundleContext != null) {
				if ((url = bundleContext.getBundle().getResource(
						LoggingConstants.LOGGING_CONF_FILE)) != null) {
					inStream = url.openStream();
				} else {
					warningMessage = "Bundle context could not find resource "
							+ LoggingConstants.LOGGING_CONF_FILE
							+ " or user does not have sufficient permission to access the resource.";
					log.warn(warningMessage);
				}

			} else {
				if ((url = this.getClass().getClassLoader()
						.getResource(LoggingConstants.LOGGING_CONF_FILE)) != null) {
					inStream = url.openStream();
				} else {
					warningMessage = "Could not find resource "
							+ LoggingConstants.LOGGING_CONF_FILE
							+ " or user does not have sufficient permission to access the resource.";
					log.warn(warningMessage);
				}
			}
		}
		return inStream;
	}


	private static LoggingConfig loadDefaultConfiguration() {
		LoggingConfig config = new LoggingConfig();
		config.setCassandraServerAvailable(false);
		return config;
	}

	/**
	 * Loads the given Syslog Configuration file.
	 * 
	 * @param configFilename
	 *            Name of the configuration file
	 * @return the syslog configuration data.
	 */
	private static LoggingConfig loadCassandraConfiguration(
			String configFilename) {
		LoggingConfig config = new LoggingConfig();
		InputStream inputStream = null;
		try {
			inputStream = new LoggingConfigManager()
					.getInputStream(configFilename);
		} catch (IOException e1) {
			log.error("Could not close the Configuration File "
					+ configFilename);
		}
		if (inputStream != null) {
			try {
				XMLStreamReader parser = XMLInputFactory.newInstance()
						.createXMLStreamReader(inputStream);
				StAXOMBuilder builder = new StAXOMBuilder(parser);
				OMElement documentElement = builder.getDocumentElement();
				@SuppressWarnings("rawtypes")
				Iterator it = documentElement.getChildElements();
				while (it.hasNext()) {
					OMElement element = (OMElement) it.next();
					// Checks whether syslog configuration enable.
					if (LoggingConstants.CassandraConfigProperties.IS_CASSANDRA_AVAILABLE
							.equals(element.getLocalName())) {
						String isCassandraOn = element.getText();
						// by default, make the syslog off.
						boolean isCassandraAvailable = false;
						if (isCassandraOn.trim().equalsIgnoreCase("true")) {
							isCassandraAvailable = true;
						}
						config.setCassandraServerAvailable(isCassandraAvailable);
					} else if (LoggingConstants.CassandraConfigProperties.URL
							.equals(element.getLocalName())) {
						config.setUrl(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.COLUMN_FAMILY
							.equals(element.getLocalName())) {
						config.setColFamily(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.KEYSPACE
							.equals(element.getLocalName())) {
						config.setKeyspace(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.USER_NAME
							.equals(element.getLocalName())) {
						config.setUser(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.PASSWORD
							.equals(element.getLocalName())) {
						config.setPassword(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.CLUSTER
							.equals(element.getLocalName())) {
						config.setCluster(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_URL
							.equals(element.getLocalName())) {
						config.setPublisherURL(element.getText());
					}  else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_USER
							.equals(element.getLocalName())) {
						config.setPublisherUser(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_PASSWORD
							.equals(element.getLocalName())) {
						config.setPublisherPassword(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.ARCHIVED_HOST
							.equals(element.getLocalName())) {
						config.setArchivedHost(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.ARCHIVED_USER
							.equals(element.getLocalName())) {
						config.setArchivedUser(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.ARCHIVED_PASSWORD
							.equals(element.getLocalName())) {
						config.setArchivedPassword(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.ARCHIVED_PORT
							.equals(element.getLocalName())) {
						config.setArchivedPort(element.getText());
					} else if (LoggingConstants.CassandraConfigProperties.ARCHIVED_REALM
							.equals(element.getLocalName())) {
						config.setArchivedRealm(element.getText());
					}
				}
				return config;
			} catch (Exception e) {
				String msg = "Error in loading Stratos Configurations File: "
						+ configFilename + ". Default Settings will be used.";
				log.error(msg, e);
				return loadDefaultConfiguration(); // returns the default
													// configurations, if the
													// file could not be loaded.
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						log.error("Could not close the Configuration File "
								+ configFilename);
					}
				}
			}
		}
		log.error("Unable to locate the stratos configurations file. "
				+ "Default Settings will be used.");
		return loadDefaultConfiguration(); // return the default configurations,
											// if the file not found.
	}
}
