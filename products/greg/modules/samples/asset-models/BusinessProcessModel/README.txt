Process Governance Model
========================

Follow the steps to extend the Registry

1. Compile the code using "mvn clean install"
2. Add all registry extension files as resources to the location "/_system/governance/repository/components/org.wso2.carbon.governance/types" in the registry.
3. Sign-out and Sign-in. Make sure new data types added to the Metadata.
4. Add the compiled jar found in the target folder using Extensions --> Add. Or add it to CARBON_HOME/repository/components/dropins
5. Add the following handlers. Extension --> Configure --> Handlers

	<handler class="org.wso2.carbon.registry.samples.handler.process.BusinessProcessMediaTypeHandler">
		<filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher">
			<property name="mediaType">application/vnd.wso2.registry-ext-process+xml</property>
		</filter>
	</handler>

6. Use the sample processes, process archives to make sure that the process succeed. If succeed, registry should populated using those processes and dependancies (wsdl,xsd etc...) should be added. Use "application/vnd.wso2.registry-ext-process+xml" mediatype as the mediatype of process definition file.




