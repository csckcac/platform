package org.wso2.carbon.hadoop.security;

import org.apache.axis2.context.ConfigurationContext;

public class HadoopCarbonMessageContext {
	private ConfigurationContext cfgCtx;
	private String cookie;
	
	public HadoopCarbonMessageContext(ConfigurationContext cfgCtx, String cookie) {
		this.cfgCtx = cfgCtx;
		this.cookie = cookie;
	}
	private static ThreadLocal<HadoopCarbonMessageContext> currentMessageContext = new InheritableThreadLocal<HadoopCarbonMessageContext>();
	
	public static HadoopCarbonMessageContext get() {
		HadoopCarbonMessageContext ctx = currentMessageContext.get();
		return ctx;
	}
	
	public static void set(HadoopCarbonMessageContext ctx) {
		currentMessageContext.set(ctx);
	}
	
	public String getCookie() {
		return this.cookie;
	}
	
	public ConfigurationContext getConfigurationContext() {
		return this.cfgCtx;
	}
}
