package org.wso2.carbon.hosting.wnagent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.hosting.wnagent.dto.ContainerInformation;
import org.wso2.carbon.hosting.wnagent.service.WorkerNodeAgentService;

/**
 * Created by IntelliJ IDEA.
 * User: damitha
 * Date: 1/19/12
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */

public class WNAgentTest {
	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@BeforeClass
	public static void runBeforeClass() throws Exception {

	}

	@AfterClass
	public static void runAfterClass() {

	}

	// @Test(expected = Exception.class)
	@Test()
	public void testAddContainerToRegistry() {
		try {

			WorkerNodeAgentService agentService = new WorkerNodeAgentService();

			ContainerInformation container = new ContainerInformation();

			container.setIp("10.100.80.2");
			container.setContainerRoot("/mnt/lxc");
			container.setJailKeysFile("/home/wso2/container_steup/jailKeysFilePath");
			container.setTemplate("template-ubuntu-lucid-lamp");
			container.setZone("public");

			container.setBridge("br-lxc");
			container.setNetMask("255.255.255.0");
			container.setNetGateway("192.168.254.1");

			container.setMemory("512M");
			container.setSwap("1G");
			container.setCpuShares("1024");
			container.setCpuSetShares("0-7");
			container.setStorage("5G");

			int ret = agentService.createContainer("ying", "g", container);
			Assert.assertEquals(1, ret);

		} catch (Exception e) {

			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
}
