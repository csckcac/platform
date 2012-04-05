package org.wso2.carbon.hosting.mgt;

import org.junit.*;

/*import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;*/

/**
 * Created by IntelliJ IDEA.
 * User: damitha
 * Date: 1/19/12
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */

public class HostingTest {
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

    //@Test(expected = Exception.class)
    @Test()
    public void testAddContainerToRegistry() {
        try {
            String retVal;
            HostingResources resources = new HostingResources();

            resources.registerContainer("ying", "public", "template-ubuntu-lucid-lamp");
            Assert.assertTrue(true);

        } catch(Exception e) {
            Assert.assertTrue(false);
            e.printStackTrace();
        }
    }

    /*//@Test(expected = Exception.class)
    @Test()
    public void testDestroyContainer() {
        try {
            int retVal = -1;
            Hosting jail = new Hosting();

            retVal = jail.destroyContainer( "deng", "/mnt/lxc");
            if(retVal == 0) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }catch(IOException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(InterruptedException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(Exception e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }*/

    /*//@Test(expected = Exception.class)
    @Test()
    public void testCreateContainer() {
        try {
            int retVal = -1;
            Hosting jail = new Hosting();

            Map<Object,String> resourceMap = new HashMap<Object, String>();
            resourceMap.put("memory", "512M");
            resourceMap.put("swap", "1G");
            resourceMap.put("cpu-shares", "1024");
            resourceMap.put("cpuset-cpus", "0-7");

            retVal = jail.createContainer( "deng", "g", "192.168.254.6", "255.255.255.0", "192.168.254.1", "/mnt/lxc",
                    "/home/damitha/.ssh/authorized_keys", "template-ubuntu-lucid-lamp", resourceMap);
            if(retVal == 0) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }catch(IOException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(InterruptedException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(Exception e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }*/

 /*   //@Test(expected = Exception.class)
    @Test
    public void testStartContainer() {
        try {
            int retVal = -1;
            Hosting jail = new Hosting();
            retVal = jail.startContainer("deng", "/mnt/lxc");
            Thread.sleep(12000);
            if(retVal == 0) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }catch(IOException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(InterruptedException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(Exception e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }*/

   /* //@Test(expected = Exception.class)
    @Test
    public void testStopContainer() {
        try {
            int retVal = -1;
            Hosting jail = new Hosting();
            retVal = jail.stopContainer("deng", "/mnt/lxc");
            if(retVal == 0) {
                assertTrue(true);
            } else {
                assertTrue(false);
            }
        }catch(IOException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(InterruptedException e) {
            assertTrue(false);
            // TODO write to the log
            e.printStackTrace();
        } catch(Exception e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }*/

}

