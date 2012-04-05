Distributed Lifecycle Management sample
---------------------------------------

Introduction
------------
Software Project Lifecycle is a sample on how a user is able to make use of the "Lifecycle status"
in a project development cycle. Initially an Architect designs 'Use case diagrams and other designs
which is then implemented by the Developers. After finishing the implementation task,
Developers promote the resource to the Testing state for further validation of the resource/product.
After passing all the tests, QA allows customers to deploy the product/project. Therefore,
altogether this has five states.
  - Design
  - Development
  - Testing
  - Production
  - Deprecated


Running the sample
------------------
 1. Copy the org.wso2.carbon.governance.samples.lcm-@carbon.version@.jar and
    org.wso2.carbon.governance.samples.lcm.notifications-@carbon.version@.jar bundles to
    GREG_HOME/repository/components/dropins folder

    You also can upload these jars through the administration console by visiting the Extensions --> Add menu.

 2. Start the server.

 3. Log on to the Management Console and select Extensions --> Configure --> Lifecycles menu.

 4. Add the following lifecycle configuration using 'Add New Lifecycle' option.
        <aspect name="Software Project Lifecycle" class="org.wso2.carbon.governance.samples.lcm.DistributedLCM">
            <!-- Checklist can either be provided as a resource(type=resource) or as xml content(type=literal, default) as provided below. -->
            <!-- <configuration type="resource">/workspace/configuration</configuration> -->
            <!-- OR -->
            <configuration type="literal">
                <lifecycle>
                    <state name="Design" location="/environment/design" roles="arch">
                        <checkitem>Requirements Gathered</checkitem>
                        <checkitem>Architecture Finalized</checkitem>
                        <checkitem>High Level Design Completed</checkitem>
                        <js>
                            <console promoteFunction="doPromote">
                                <script type="text/javascript">
                                    doPromote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/development");
                                    }
                                </script>
                            </console>
                            <server promoteFunction="doPromote">
                                <script type="text/javascript">
                                    function doPromote() {
                                        return "Promoted Resource/Collection to Development State";
                                    }
                                </script>
                            </server>
                        </js>
                    </state>
                    <state name="Development" location="/environment/development" roles="dev">
                        <permissions>
                            <permission action="promote" roles="tmc"/>
                        </permissions>
                        <checkitem>Code Completed</checkitem>
                        <checkitem>WSDL, Schema Created</checkitem>
                        <checkitem>QoS Created</checkitem>
                        <js>
                            <console promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    doDemote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/design");
                                    }
                                    doPromote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/qa");
                                    }
                                </script>
                            </console>
                            <server promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    function doDemote() {
                                        return "Demoted Resource/Collection to Design State";
                                    }
                                    function doPromote() {
                                        return "Promoted Resource/Collection to Testing State";
                                    }
                                </script>
                            </server>
                        </js>
                    </state>
                    <state name="Testing" location="/environment/qa" roles="qa">
                        <checkitem>Effective Inspection Completed</checkitem>
                        <checkitem>Test Cases Passed</checkitem>
                        <checkitem>Smoke Test Passed</checkitem>
                        <js>
                            <console promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    doDemote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/development");
                                    }
                                    doPromote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/production");
                                    }
                                </script>
                            </console>
                            <server promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    function doDemote() {
                                        return "Demoted Resource/Collection to Development State";
                                    }
                                    function doPromote() {
                                        return "Promoted Resource/Collection to Production State";
                                    }
                                </script>
                            </server>
                        </js>
                    </state>
                    <state name="Production" location="/environment/production" roles="prod">
                        <checkitem>Project Item Deprecated</checkitem>
                        <js>
                            <console promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    doDemote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26amp;item=resource_browser_menu%26path=/environment/qa");
                                    }
                                    doPromote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/deprecated");
                                    }
                                </script>
                            </console>
                            <server promoteFunction="doPromote" demoteFunction="doDemote">
                                <script type="text/javascript">
                                    function doDemote() {
                                        return "Demoted Resource/Collection to Testing State";
                                    }
                                    function doPromote() {
                                        return "Promoted Resource/Collection to Deprecated State";
                                    }
                                </script>
                            </server>
                        </js>
                    </state>
                    <state name="Deprecated" location="/environment/deprecated" roles="arch, prod">
                        <js>
                            <console demoteFunction="doDemote">
                                <script type="text/javascript">
                                    doDemote = function() {
                                        window.location = unescape("../resources/resource.jsp?region=region3%26item=resource_browser_menu%26path=/environment/production");
                                    }
                                </script>
                            </console>
                            <server demoteFunction="doDemote">
                                <script type="text/javascript">
                                    function doDemote() {
                                        return "Demoted Resource/Collection to Production State";
                                    }
                                </script>
                            </server>
                        </js>
                    </state>
                </lifecycle>
            </configuration>
        </aspect>

 5. Create a collection "/environment/design".

 6. Add a resource into that collection.

 7. Navigate to the newly created resource.

 8. At the "Lifecycle" panel, Add "Software Project Lifecycle" to the resource.

 9. Create new role "tmc" from the "User Management" menu.

10. Add the current user to the role 'tmc' (required to promote from the 'Development' state).

11. Go to "environment/design" collection, where resource is stored with its new life cycle state. 
    Select that resource to promote by checking all the options and click 'Promote'. 
    It can then be found in "environment/development" collection with the next state.

12. Your resource will be promoted in the following manner:
	-/environment/design
	-/environment/development
	-/environment/qa
	-/environment/production
	-/environment/deprecated

13. Similarly your resource can be demoted in the reverse order.

Note:- You also can add lifecycle configurations on the "registry.xml" file. Such configurations
will be globally available to all users of this server instance.
