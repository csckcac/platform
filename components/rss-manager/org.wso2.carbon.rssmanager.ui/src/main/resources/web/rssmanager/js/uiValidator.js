function validateRSSInstanceProperties(flag) {
    var rssInstanceName = trim(document.getElementById("rssInstanceName").value);
    var serverUrl = trim(document.getElementById("serverUrl").value);
    var serverCategories = document.getElementById("serverCategory");
    var serverCategory = trim(serverCategories[serverCategories.selectedIndex].value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Database server instance name cannot be left blank");
        return false;
    }
    if (serverCategory == '' || serverCategory == null) {
        CARBON.showWarningDialog("Select a valid server category");
        return false;
    }
    if (instanceType == '' || instanceType == null) {
        CARBON.showWarningDialog("Select a valid instance type");
        return false;
    }
    var driverClass = '';
    if (serverUrl != null && serverUrl != '') {
        driverClass = trim(getJdbcDriver(serverUrl));
        if (!(driverClass != null && driverClass != '')) {
            CARBON.showErrorDialog("JDBC URL '" + serverUrl + "' is invalid. Please enter a valid JDBC URL.");
            return false
        }
    } else {
        CARBON.showWarningDialog("JDBC URL field cannot be left blank");
        return false;
    }
    if (username == '' || username == null) {
        CARBON.showWarningDialog("Administrative username field cannot be left blank");
        return false;
    }
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Administrative password field cannot be left blank");
        return false;
    }
    dispatchRSSInstanceCreateRequest(flag);
}

function dispatchDropRSSInstanceRequest(rssInstanceName) {
    function forwardToDel() {
        var url = 'rssInstanceOps_ajaxprocessor.jsp?flag=drop&rssInstanceName=' +
                encodeURIComponent(rssInstanceName);
        jQuery('#connectionStatusDiv').load(url, displayMessages);
    }

    CARBON.showConfirmationDialog('Do you want to delete Database Server Instance?', forwardToDel);
}

function deleteInstance(obj) {
    function forwardToDel() {
        var delElement = document.getElementById(obj);
        var instanceTable = document.getElementById("instanceTable");
        instanceTable.removeChild(delElement);
        document.location.href = "rssInstances.jsp";
    }

    CARBON.showConfirmationDialog("Do you want to drop database server instance " + obj + "?",
            forwardToDel());
}

function dispatchRSSInstanceCreateRequest(flag) {
    var rssInstanceName = document.getElementById("rssInstanceName").value;
    var serverUrl = document.getElementById("serverUrl").value;
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var serverCategoryList = document.getElementById("serverCategory");
    var serverCategory = serverCategoryList[serverCategoryList.selectedIndex].value;
    var url = 'rssInstanceOps_ajaxprocessor.jsp?rssInstanceName=' + encodeURIComponent(rssInstanceName)
            + '&serverUrl=' + encodeURIComponent(serverUrl) + '&username=' + encodeURIComponent(
            username) + '&password=' + encodeURIComponent(password) + '&flag=' + flag + '&serverCategory=' + encodeURIComponent(serverCategory);
    jQuery('#connectionStatusDiv').load(url, displayMessages);
}

function setHostRssInsname() {
    var hostList = document.getElementById('hosts');
    document.getElementById('hostRssInsId').value = hostList[hostList.selectedIndex].id;
}

function setRssInsId() {
    var rssInsList = document.getElementById("hosts");
    document.getElementById("rssInsId").value = rssInsList[rssInsList.selectedIndex].value;
}

function createDatabase() {
    var rssInstances = document.getElementById("rssInstances");
    var rssInstanceName = trim(rssInstances[rssInstances.selectedIndex].value);
    var databaseName = trim(document.getElementById("databaseName").value);

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Select a valid database instance");
        return false;
    }
    if (databaseName == '' || databaseName == null) {
        CARBON.showWarningDialog("Database name cannot be left blank");
        return false;
    }
    var validChar = new RegExp("^[a-zA-Z0-9_]+$");
    if (!validChar.test(databaseName)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in database name");
        return false;
    }
    dispatchDatabaseActionRequest('create', rssInstanceName, databaseName);
}

function attachUserToDatabase() {
    var rssInstanceName = document.getElementById('rssInstanceName').value;
    var databaseName = document.getElementById('databaseName').value;
    var templates = document.getElementById('privilegeTemplates');
    var templateName = templates[templates.selectedIndex].value;
    var databaseUsers = document.getElementById('databaseUsers');
    var username = databaseUsers[databaseUsers.selectedIndex].value;

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Select a valid database instance");
        return false;
    }
    if (databaseName == '' || databaseName == null) {
        CARBON.showWarningDialog("Database name cannot be left blank");
        return false;
    }
    if (templateName == '' || templateName == null || templateName == 'SELECT') {
        CARBON.showWarningDialog("Select a valid database privilege template");
        return false;
    }
    if (username == '' || username == null || username == 'SELECT') {
        CARBON.showWarningDialog("Select a valid database user");
        return false;
    }
    dispatchDatabaseManageAction('attach', rssInstanceName, username, databaseName);
}

function dispatchDatabaseManageAction(flag, rssInstanceName, username, databaseName) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var privilegeTemplates = document.getElementById('privilegeTemplates');
    var privilegeTemplate = '';
    if (privilegeTemplates != null) {
        privilegeTemplate = privilegeTemplates[privilegeTemplates.selectedIndex].value;
    } else {
        var tmpTemplate = document.getElementById('privilegeTemplateName');
        if (tmpTemplate != null) {
            privilegeTemplate = tmpTemplate.value;
        }
    }
    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
            '&username=' + encodeURIComponent(username) + '&password=' +
            encodeURIComponent(password) + '&privilegeTemplateName=' +
            encodeURIComponent(privilegeTemplate) + '&databaseName=' + databaseName;
    jQuery('#connectionStatusDiv').load(url, displayDatabaseManageActionStatus);
}

function displayDatabaseManageActionStatus(msg) {
    if (msg.search(/has been successfully attached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully detached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to attach user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to detach user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}


function createDatabaseUser() {
    var username = trim(document.getElementById('username').value);
    var password = document.getElementById('password').value;
    var repeatPass = document.getElementById('repeatPassword').value;
    var rssInstances = document.getElementById('rssInstances');
    var rssInstanceName = rssInstances[rssInstances.selectedIndex].value;

    if (username == '' || username == null) {
        CARBON.showWarningDialog("Username field cannot be left blank");
        return false;
    } else if (username.length > 7) {
        CARBON.showWarningDialog("Value in the username field entered exceeds the maximum permitted length of 7");
        return false;
    }
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Password field cannot be left blank");
        return false;
    }
    if (repeatPass == '' || repeatPass == null) {
        CARBON.showWarningDialog("Repeat password field cannot be left blank");
        return false;
    }
    if (password != repeatPass) {
        CARBON.showErrorDialog("Vlaues in Password and Repeat password fields do not match");
        return false;
    }
    dispatchDatabaseUserActionRequest('create', rssInstanceName, username, '');
}

function editDatabaseUser(rssInstanceName, username) {
    var password = document.getElementById('password').value;
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Password field cannot be left blank");
        return false;
    }
    dispatchDatabaseUserActionRequest('edit', rssInstanceName, username, '');
    return true;
}

function setJDBCValues(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    document.getElementById("serverUrl").value = selectedValue.substring(
            0, selectedValue.indexOf("#"));
}

function testConnection() {
    var rssInstanceName = trim(document.getElementById("rssInstanceName").value);
    var serverCategories = document.getElementById("serverCategory");
    var serverCategory = trim(serverCategories[serverCategories.selectedIndex].value);
    var serverUrl = trim(document.getElementById("serverUrl").value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Database server instance name cannot be left blank");
        return false;
    }
    if (serverCategory == '' || serverCategory == null) {
        CARBON.showWarningDialog("Select a valid server category");
        return false;
    }
    if (instanceType == '' || instanceType == null) {
        CARBON.showWarningDialog("Select a valid instance type");
        return false;
    }
    if (serverUrl == '' || serverUrl == null) {
        CARBON.showWarningDialog("JDBC url field cannot be left blank");
        return false;
    }
    if (username == '' || username == null) {
        CARBON.showWarningDialog("Administrative username field cannot Be left blank");
        return false;
    }
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Administrative password field cannot be left blank");
        return false;
    }
    var jdbcUrl = trim(document.getElementById('serverUrl').value);
    var driverClass = '';
    if (jdbcUrl != null && jdbcUrl != '') {
        driverClass = trim(getJdbcDriver(jdbcUrl));
        if (driverClass != null && driverClass != '') {
            var url = 'rssInstanceOps_ajaxprocessor.jsp?flag=testCon&driverClass=' + encodeURIComponent(
                    driverClass) + '&serverUrl=' + encodeURIComponent(retrieveValidatedUrl(jdbcUrl)) +
                    '&username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(
                    password);
            jQuery('#connectionStatusDiv').load(url, displayMsg);
        } else {
            CARBON.showErrorDialog("Invalid JDBC URL '" + jdbcUrl + "'. Please enter an appropriate JDBC URL.");
        }
    }
    return false;
}

function retrieveValidatedUrl(url) {
    var prefix = url.split(':')[1];
    var hostname = url.split('//')[1].split('/')[0];
    return 'jdbc:' + prefix + "://" + hostname;
}

function dropDatabaseUser(rssInstanceName, username) {
    function forwardToDel() {
        dispatchDatabaseUserActionRequest('drop', rssInstanceName, username, '')
    }

    CARBON.showConfirmationDialog("Do you want to drop the user?", forwardToDel);
}

function dropDatabase(rssInstanceName, databaseName) {
    function forwardToDel() {
        dispatchDatabaseActionRequest('drop', rssInstanceName, databaseName);
    }

    CARBON.showConfirmationDialog("Do you want to drop the database?", forwardToDel);
}

function manageDatabase(rssInstanceName, databaseName) {
    //document.location.href = 'databaseUsers.jsp?rssInsId=' + encodeURIComponent(rssInsId) + '&dbInsId=' +
    // encodeURIComponent(dbInsId);
    document.location.href = 'databaseUsers.jsp?rssInstanceName=' + encodeURIComponent(rssInstanceName) +
            '&databaseName=' + encodeURIComponent(databaseName);
}

function redirectToEditPage(obj, rssInsId) {
    var rowId = $(obj).parents('tr:eq(0)').attr('id');
    var instanceName = rowId.substring("tr_".length, rowId.length);
    document.location.href = "editRSSInstance.jsp?instanceName=" +
            encodeURIComponent(instanceName) + "&flag=edit&rssInsId=" +
            encodeURIComponent(rssInsId);
}

function dispatchDatabaseUserActionRequest(flag, rssInstanceName, username, databaseName) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var privilegeTemplates = document.getElementById('privilegeTemplates');
    var privilegeTemplate = '';
    if (privilegeTemplates != null) {
        privilegeTemplate = privilegeTemplates[privilegeTemplates.selectedIndex].value;
    } else {
        var tmpTemplate = document.getElementById('privilegeTemplateName');
        if (tmpTemplate != null) {
            privilegeTemplate = tmpTemplate.value;
        }
    }
    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
            '&username=' + encodeURIComponent(username) + '&password=' +
            encodeURIComponent(password) + '&privilegeTemplateName=' +
            encodeURIComponent(privilegeTemplate) + '&databaseName=' + databaseName;
    jQuery('#connectionStatusDiv').load(url, displayMessagesForUser);
}

function populateSelectedUsername() {
    var userList = document.getElementById('users');
    document.getElementById('selectedUsername').value = userList[userList.selectedIndex].value;
}

function forwardToRedirector(rssInstId, dbInstId) {
    var selectedUsername = document.getElementById('selectedUsername');
    document.location.href = 'redirector.jsp?rssInstId=' + encodeURIComponent(rssInstId) +
            '&dbInstId=' + encodeURIComponent(dbInstId) + '&username=' + encodeURIComponent(
            selectedUsername);
}

function dispatchDatabaseActionRequest(flag, rssInstanceName, databaseName) {
    var url = 'databaseOps_ajaxprocessor.jsp?flag=' + flag + '&rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&databaseName=' +
            encodeURIComponent(databaseName);
    jQuery('#connectionStatusDiv').load(url, displayDatabaseActionStatus);
}

function deleteUser(userId) {
    document.location.href = "userProcessor.jsp?flag=delete&userId=" + encodeURIComponent(userId);
}

function setFlag(flag) {
    document.getElementById('flag').value = flag;
}

function setSSLType() {
    var sslTypes = document.getElementById(sslTypes);
    document.getElementById("ssl_type").value = sslTypes[sslTypes.selectedIndex].value;
}

function populateCheckBox(obj, val) {
    if (val == 'Y') {
        obj.checked = true;
    } else if (val == 'N') {
        obj.checked = false;
    }
}

function redirectToPrivilegeGroupsPage(rssInsId, dbInsId) {
    document.location.href = 'databasePrivilegeTemplates.jsp?dbInsId=' + encodeURIComponent(dbInsId) +
            '&rssInsId=' + encodeURIComponent(rssInsId);
}

function redirectToUsersPage() {
    document.location.href = 'databaseUsers.jsp';
}

function setPrivilegeGroup() {
    var privilegeGroups = document.getElementById('privilegeGroups');
    document.getElementById('privilegeGroup').value = privilegeGroups[privilegeGroups.selectedIndex].value;
}

function checkSelectedPrivileges() {
    var isSelected = false;
    var privileges = new Array();
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Insert_priv"));
    privileges.push(document.getElementById("Update_priv"));
    privileges.push(document.getElementById("Delete_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Lock_tables_priv"));
    privileges.push(document.getElementById("Create_view_priv"));
    privileges.push(document.getElementById("Show_view_priv"));
    privileges.push(document.getElementById("Create_routine_priv"));
    privileges.push(document.getElementById("Alter_routine_priv"));
    privileges.push(document.getElementById("Event_priv"));
    privileges.push(document.getElementById("Trigger_priv"));
    for (var i = 0; i < privileges.length; i++) {
        var val = privileges.pop();
        if (val != '' && val != null) {
            isSelected = true;
        }
    }
    if (!isSelected) {
        CARBON.showWarningDialog("No privilege has been selected. User might not be able to " +
                "login to the database");
    }
}

function createDatabasePrivilegeTemplate(flag) {
    var templateName = trim(document.getElementById('privilegeTemplateName').value);
    if (templateName == '' || templateName == null) {
        CARBON.showWarningDialog("'Database privilege template name' field cannot be left blank");
        return false;
    }
    var url = composeDatabasePrivilegeTemplateActionUrl(flag, templateName);
    jQuery('#connectionStatusDiv').load(url, displayPrivilegeTemplateActionStatus);
}

function validateDatabasePrivilegeTemplateName() {
    var templateName = trim(document.getElementById('privilegeTemplateName').value);
    if (templateName == '' || templateName == null) {
        CARBON.showWarningDialog("'Database privilege template name' field cannot be left blank");
        return false;
    }
    return true;
}

function displayPrivilegeTemplateActionStatus(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database privilege template/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database privilege template/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function composeDatabasePrivilegeTemplateActionUrl(flag, templateName) {
    var select_priv = document.getElementById("select_priv").checked;
    var insert_priv = document.getElementById("insert_priv").checked;
    var update_priv = document.getElementById("update_priv").checked;
    var delete_priv = document.getElementById("delete_priv").checked;
    var create_priv = document.getElementById("create_priv").checked;
    var drop_priv = document.getElementById("drop_priv").checked;
    var grant_priv = document.getElementById("grant_priv").checked;
    var references_priv = document.getElementById("references_priv").checked;
    var index_priv = document.getElementById("index_priv").checked;
    var alter_priv = document.getElementById("alter_priv").checked;
    var create_tmp_table_priv = document.getElementById("create_tmp_table_priv").checked;
    var lock_tables_priv = document.getElementById("lock_tables_priv").checked;
    var create_view_priv = document.getElementById("create_view_priv").checked;
    var show_view_priv = document.getElementById("show_view_priv").checked;
    var create_routine_priv = document.getElementById("create_routine_priv").checked;
    var alter_routine_priv = document.getElementById("alter_routine_priv").checked;
    var execute_priv = document.getElementById("execute_priv").checked;
    var event_priv = document.getElementById("event_priv").checked;
    var trigger_priv = document.getElementById("trigger_priv").checked;

    return 'databasePrivilegeTemplateOps_ajaxprocessor.jsp?flag=' + flag + '&privilegeTemplateName=' + templateName +
            '&select_priv=' + select_priv + '&insert_priv=' + insert_priv + '&update_priv=' +
            update_priv + '&delete_priv=' + delete_priv + '&create_priv=' + create_priv +
            '&drop_priv=' + drop_priv + '&grant_priv=' + grant_priv + '&references_priv=' +
            references_priv + '&index_priv=' + index_priv + '&alter_priv=' + alter_priv +
            '&create_tmp_table_priv=' + create_tmp_table_priv + '&lock_tables_priv=' +
            lock_tables_priv + '&create_view_priv=' + create_view_priv + '&show_view_priv=' +
            show_view_priv + '&create_routine_priv=' + create_routine_priv + '&alter_routine_priv='
            + alter_routine_priv + '&execute_priv=' + execute_priv + '&event_priv=' + event_priv +
            '&trigger_priv=' + trigger_priv;
}

function dispatchDropDatabasePrivilegeTemplateRequest(privilegeTemplateName) {
    function forwardToDel() {
        var url = 'databasePrivilegeTemplateOps_ajaxprocessor.jsp?privilegeTemplateName=' +
                encodeURIComponent(privilegeTemplateName) + '&flag=drop';
        jQuery('#connectionStatusDiv').load(url, displayPrivilegeTemplateActionStatus);
    }

    CARBON.showConfirmationDialog('Do you want to drop database privilege template?', forwardToDel);
}

function displayMessages(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database server instance/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database server instance/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function displayDatabaseActionStatus(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}


function displayMessagesForUser(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to edit user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMsg(msg) {
    var successMsg = new RegExp("^Database connection is successful");
    if (msg.search(successMsg) == -1) //if match failed
    {
        CARBON.showErrorDialog(msg);
    } else {
        CARBON.showInfoDialog(msg);
    }

}

function validateJdbcUrl(url) {
    if (url.split(":")[0] != 'jdbc') {
        CARBON.showErrorDialog("Invalid Jdbc url");
        return false;
    }
    return true;
}

function rightTrim(str) {
    for (var i = str.length - 1; i >= 0 && (str.charAt(i) == ' '); i--) {
        str = str.substring(0, i);
    }
    return str;
}

function leftTrim(str) {
    for (var i = 0; i >= 0 && (str.charAt(i) == ' '); i++) {
        str = str.substring(i + 1, str.length);
    }
    return str;
}

function trim(str) {
    return leftTrim(rightTrim(str));
}

function getJdbcDriver(instanceUrl) {
    var prefix = instanceUrl.split(':')[1];
    if (prefix == 'mysql') {
        return 'com.mysql.jdbc.Driver';
    } else if (prefix == 'oracle') {
        return 'oracle.jdbc.driver.OracleDriver';
    }
    return '';
}

function createDataSource(databaseName, username) {
    var url = 'databaseUserOps_ajaxprocessor.jsp?databaseName=' + databaseName + '&username=' +
            username + '&flag=createDS';
    jQuery('#connectionStatusDiv').load(url, displayMessagesForCarbonDS);
}

function detachDatabaseUser(rssInstanceName, databaseName, username) {
    var url = 'databaseUserOps_ajaxprocessor.jsp?databaseName=' + databaseName + '&username=' +
            username + '&rssInstanceName=' + rssInstanceName + '&flag=detach';
    jQuery('#connectionStatusDiv').load(url, displayMessagesForDatabaseUserActions);
}

function displayMessagesForDatabaseUserActions(msg) {
    if (msg.search(/has been successfully attached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully detached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to attach database user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to detach database user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMessagesForCarbonDS(msg) {
    if (msg.search(/Carbon datasource has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Unable to create carbon datasource/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg);
        });
    } else {
        CARBON.showErrorDialog('Unable to create carbon datasource');
    }
}

function exploreDatabase(userId, url, driver) {
    document.location.href = '../dbconsole/login.jsp?userId=' + userId +
            '&url=' + url + '&driver=' + driver;
}

function selectAllOptions() {
    var selectAll = document.getElementById('selectAll');
    var c = new Array();
    c = document.getElementsByTagName('input');
    if (selectAll.checked) {
        for (var i = 0; i < c.length; i++) {
            if (c[i].type == 'checkbox') {
                c[i].checked = true;
            }
        }
    } else {
        for (var j = 0; j < c.length; j++) {
            if (c[j].type == 'checkbox') {
                c[j].checked = false;
            }
        }
    }
}


