function validateInstanceProperties() {
    var instanceName = trim(document.getElementById("instanceName").value);
    var instanceUrl = trim(document.getElementById("instanceUrl").value);
    var serverCategories = document.getElementById("serverCategory");
    var serverCategory = trim(serverCategories[serverCategories.selectedIndex].value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (instanceName == '' || instanceName == null) {
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
    if (instanceUrl != null && instanceUrl != '') {
        driverClass = trim(getJdbcDriver(instanceUrl));
        if (!(driverClass != null && driverClass != '')) {
            CARBON.showErrorDialog("Invalid JDBC Url");
            return false
        }
    } else {
        CARBON.showWarningDialog("JDBC url field cannot be left blank");
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
    validateConnectionStatus();
}

function validateEditInstanceProperties() {
    var instanceName = trim(document.getElementById("instanceName").value);
    var instanceUrl = trim(document.getElementById("instanceUrl").value);
    var serverCategories = document.getElementById("serverCategory");
    var serverCategory = trim(serverCategories[serverCategories.selectedIndex].value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (instanceName == '' || instanceName == null) {
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
    if (instanceUrl != null && instanceUrl != '') {
        driverClass = trim(getJdbcDriver(instanceUrl));
        if (!(driverClass != null && driverClass != '')) {
            CARBON.showErrorDialog("Invalid JDBC Url");
            return false
        }
    } else {
        CARBON.showWarningDialog("JDBC url field cannot be left blank");
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
    redirectToInstanceProcessor();
}

function redirectToInstanceProcessor() {
    var instanceName = document.getElementById("instanceName").value;
    var instanceUrl = document.getElementById("instanceUrl").value;
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var flag = document.getElementById("flag").value;
    var rssInsId = document.getElementById("rssInsId").value;
    var serverCategoryList = document.getElementById("serverCategory");
    var serverCategory = serverCategoryList[serverCategoryList.selectedIndex].value;
    var url = 'connection_status_ajaxprocessor.jsp?instanceName=' + encodeURIComponent(instanceName)
            + '&instanceUrl=' + encodeURIComponent(instanceUrl) + '&username=' + encodeURIComponent(
            username) + '&password=' + encodeURIComponent(password) + '&flag=' + encodeURIComponent(
            flag) + '&serverCategory=' + encodeURIComponent(serverCategory) + '&rssInsId=' +
            encodeURIComponent(rssInsId);
    jQuery('#connectionStatusDiv').load(url, displayMessages);
}

function removeInstance(obj, rssInsId) {
    function forwardToDel() {
        var rowId = $(obj).parents('tr:eq(0)').attr('id');
        var instanceName = rowId.substring("tr_".length, rowId.length);
        var url = 'connection_status_ajaxprocessor.jsp?instanceName=' + encodeURIComponent(
                instanceName) + '&flag=remove&rssInsId=' + encodeURIComponent(rssInsId);
        jQuery('#connectionStatusDiv').load(url, displayMessages);
    }

    CARBON.showConfirmationDialog('Do you want to delete Database Server Instance?', forwardToDel);
}

function deleteInstance(obj) {
    function forwardToDel() {
        var delElement = document.getElementById(obj);
        var instanceTable = document.getElementById("instanceTable");
        instanceTable.removeChild(delElement);
        document.location.href = "instances.jsp";
    }

    CARBON.showConfirmationDialog("Do you want to drop database server instance " + obj + "?",
            forwardToDel());
}

function validateConnectionStatus() {
    var instanceName = document.getElementById("instanceName").value;
    var instanceUrl = document.getElementById("instanceUrl").value;
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var flag = document.getElementById("flag").value;
    var serverCategoryList = document.getElementById("serverCategory");
    var serverCategory = serverCategoryList[serverCategoryList.selectedIndex].value;
    var url = 'connection_status_ajaxprocessor.jsp?instanceName=' + encodeURIComponent(instanceName)
            + '&instanceUrl=' + encodeURIComponent(instanceUrl) + '&username=' + encodeURIComponent(
            username) + '&password=' + encodeURIComponent(password) + '&flag=' + encodeURIComponent(
            flag) + '&serverCategory=' + encodeURIComponent(serverCategory);
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

function validateDatabaseProperties() {
    var instanceNameBox = document.getElementById("instances");
    var instanceName = trim(instanceNameBox[instanceNameBox.selectedIndex].value);
    var dbName = trim(document.getElementById("dbName").value);

    if (instanceName == '' || instanceName == null) {
        CARBON.showWarningDialog("Select a valid database instance");
        return false;
    }
    if (dbName == '' || dbName == null) {
        CARBON.showWarningDialog("Database name cannot be left blank");
        return false;
    }
    var validChar = new RegExp("^[a-zA-Z0-9_]+$");
    if (!validChar.test(dbName)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in database name");
        return false;
    }
    redirectToDatabaseProcessor();

}

function addDatabases() {
    var instances = document.getElementById("instances").value;
    var dbName = document.getElementById("dbName").value;
    var url = 'database_ajaxprocessor.jsp?dbName=' + encodeURIComponent(dbName) + '&instances=' +
            encodeURIComponent(instances);
    jQuery('#connectionStatusDiv').load(url, displayMessages);
}

function validatePrivileges() {
    var username = trim(document.getElementById('username').value);
    var password = document.getElementById('password').value;
    var repeatPass = document.getElementById('repeatPassword').value;
    var privGroupList = document.getElementById('privGroupList');
    var privGroup = trim(privGroupList[privGroupList.selectedIndex].value);
    var privGroupId = privGroupList[privGroupList.selectedIndex].id;
    
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
    if (privGroup == '' || privGroup == null) {
        CARBON.showWarningDialog("Select a valid privilege group");
        return false;
    }
    redirectToUsers();
}

function validateEditUser() {
    var password = document.getElementById('password').value;
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Password field cannot be left blank");
        return false;
    }
    redirectToUsers();
    return true;
}

function setJDBCValues(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    document.getElementById("instanceUrl").value = selectedValue.substring(
            0, selectedValue.indexOf("#"));
}

function testConnection() {
    var instanceName = trim(document.getElementById("instanceName").value);
    var serverCategories = document.getElementById("serverCategory");
    var serverCategory = trim(serverCategories[serverCategories.selectedIndex].value);
    var instanceUrl = trim(document.getElementById("instanceUrl").value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (instanceName == '' || instanceName == null) {
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
    if (instanceUrl == '' || instanceUrl == null) {
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
    var jdbcUrl = trim(document.getElementById('instanceUrl').value);
    var driverClass = '';
    if (jdbcUrl != null && jdbcUrl != '') {
        driverClass = trim(getJdbcDriver(jdbcUrl));
        if (driverClass != null && driverClass != '') {
            var url = 'connection_test_ajaxprocessor.jsp?driverClass=' + encodeURIComponent(
                    driverClass) + '&jdbcUrl=' + encodeURIComponent(retrieveValidatedUrl(jdbcUrl)) +
                    '&username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(
                    password);
            jQuery('#connectionStatusDiv').load(url, displayMsg);
        } else {
            CARBON.showErrorDialog("Invalid JDBC Url");
        }
    }

    return false;
}

function retrieveValidatedUrl(url) {
    var prefix = url.split(':')[1];
    var hostname = url.split('//')[1].split('/')[0];
    return 'jdbc:' + prefix + "://" + hostname;
}

function deleteDatabaseUser(userId, dbInsId, rssInsId) {
    function forwardToDel() {
        document.location.href = 'userProcessor.jsp?flag=delete&userId=' +
                encodeURIComponent(userId) + '&dbInsId=' + encodeURIComponent(dbInsId) +
                '&rssInsId=' + encodeURIComponent(rssInsId);
    }

    CARBON.showConfirmationDialog("Do you want to drop the user?", forwardToDel);
}

function dropDatabase(obj) {
    function forwardToDel() {
        var rowId = $(obj).parents('tr:eq(0)').attr('id');
        var temp = rowId.split('_');
        var rssInsId = trim(temp[1]);
        var dbInsId = trim(temp[2]);
        document.location.href = "databaseProcessor.jsp?rssInsId=" + encodeURIComponent(rssInsId) +
                "&dbInsId=" + encodeURIComponent(dbInsId) + "&flag=drop";
    }

    CARBON.showConfirmationDialog("Do you want to drop the database?", forwardToDel);
}

function manageDatabase(rssInsId, dbInsId) {
    //document.location.href = 'users.jsp?rssInsId=' + encodeURIComponent(rssInsId) + '&dbInsId=' +
    // encodeURIComponent(dbInsId);
    document.location.href = 'users.jsp?rssInsId=' + encodeURIComponent(rssInsId) +
            '&dbInsId=' + encodeURIComponent(dbInsId);
}

function redirectToEditPage(obj, rssInsId) {
    var rowId = $(obj).parents('tr:eq(0)').attr('id');
    var instanceName = rowId.substring("tr_".length, rowId.length);
    document.location.href = "editInstance.jsp?instanceName=" + encodeURIComponent(instanceName) +
            "&flag=edit&rssInsId=" + encodeURIComponent(rssInsId);
}

function redirectToUsers() {
    var flag = document.getElementById('flag').value;
    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;
    var dbInsId = document.getElementById('dbInsId').value;
    //var rssInsId = document.getElementById('rssInsId').value;
    var privGroupList = document.getElementById('privGroupList');
    var privGroupId = privGroupList[privGroupList.selectedIndex].id;
    var url = 'user_ajaxprocessor.jsp?flag=' + encodeURIComponent(flag) + '&username=' +
            encodeURIComponent(username) + '&password=' + encodeURIComponent(password) + '&dbInsId='
            + encodeURIComponent(dbInsId) + '&privGroupId=' + encodeURIComponent(privGroupId);
    jQuery('#connectionStatusDiv').load(url, displayMessagesForUser);
}

function redirectToDatabaseExplorer() {
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

function redirectToDatabaseProcessor() {
    var instanceList = document.getElementById('instances');
    var rssInsId = instanceList[instanceList.selectedIndex].id;
    var dbName = document.getElementById('dbName').value;
    var url = 'database_ajaxprocessor.jsp?flag=create&rssInsId=' + encodeURIComponent(rssInsId) +
            '&dbName=' + encodeURIComponent(dbName);
    jQuery('#connectionStatusDiv').load(url, displayMessagesForDB);
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
    document.location.href = 'privilegeGroups.jsp?dbInsId=' + encodeURIComponent(dbInsId) +
            '&rssInsId=' + encodeURIComponent(rssInsId);
}

function redirectToUsersPage(rssInsId, dbInsId) {
    document.location.href = 'users.jsp?rssInsId=' + encodeURIComponent(rssInsId) + '&dbInsId=' +
            encodeURIComponent(dbInsId);
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

function savePrivilegeGroup() {
    var privGroupName = trim(document.getElementById('privGroupName').value);
    var flag = trim(document.getElementById('flag').value);

    if (privGroupName == '' || privGroupName == null) {
        CARBON.showWarningDialog("Privilege group name field cannot be left blank");
        return false;
    }
    var url = createPrivGroupAddUrl(flag, privGroupName);
    jQuery('#connectionStatusDiv').load(url, displayMessagesForPrivGroups);
}

function createPrivGroupAddUrl(flag, privGroupName) {
    var select_priv = document.getElementById("select_priv").value;
    var insert_priv = document.getElementById("insert_priv").value;
    var update_priv = document.getElementById("update_priv").value;
    var delete_priv = document.getElementById("delete_priv").value;
    var create_priv = document.getElementById("create_priv").value;
    var drop_priv = document.getElementById("drop_priv").value;
    var grant_priv = document.getElementById("grant_priv").value;
    var references_priv = document.getElementById("references_priv").value;
    var index_priv = document.getElementById("index_priv").value;
    var alter_priv = document.getElementById("alter_priv").value;
    var create_tmp_table_priv = document.getElementById("create_tmp_table_priv").value;
    var lock_tables_priv = document.getElementById("lock_tables_priv").value;
    var create_view_priv = document.getElementById("create_view_priv").value;
    var show_view_priv = document.getElementById("show_view_priv").value;
    var create_routine_priv = document.getElementById("create_routine_priv").value;
    var alter_routine_priv = document.getElementById("alter_routine_priv").value;
    var execute_priv = document.getElementById("execute_priv").value;
    var event_priv = document.getElementById("event_priv").value;
    var trigger_priv = document.getElementById("trigger_priv").value;

    return 'privgroup_ajaxprocessor.jsp?flag=' + flag + '&privGroupName=' + privGroupName +
            '&Select_priv=' + select_priv + '&Insert_priv=' + insert_priv + '&Update_priv=' +
            update_priv + '&Delete_priv=' + delete_priv + '&Create_priv=' + create_priv +
            '&Drop_priv=' + drop_priv + '&Grant_priv=' + grant_priv + '&References_priv=' +
            references_priv + '&Index_priv=' + index_priv + '&Alter_priv=' + alter_priv +
            '&Create_tmp_table_priv=' + create_tmp_table_priv + '&Lock_tables_priv=' +
            lock_tables_priv + '&Create_view_priv=' + create_view_priv + '&Show_view_priv=' +
            show_view_priv + '&Create_routine_priv=' + create_routine_priv + '&Alter_routine_priv='
            + alter_routine_priv + '&Execute_priv=' + execute_priv + '&Event_priv=' + event_priv +
            '&Trigger_priv=' + trigger_priv;
}

function createPrivGroupEditUrl(flag, privGroupId) {
    var select_priv = document.getElementById("select_priv").value;
    var insert_priv = document.getElementById("insert_priv").value;
    var update_priv = document.getElementById("update_priv").value;
    var delete_priv = document.getElementById("delete_priv").value;
    var create_priv = document.getElementById("create_priv").value;
    var drop_priv = document.getElementById("drop_priv").value;
    var grant_priv = document.getElementById("grant_priv").value;
    var references_priv = document.getElementById("references_priv").value;
    var index_priv = document.getElementById("index_priv").value;
    var alter_priv = document.getElementById("alter_priv").value;
    var create_tmp_table_priv = document.getElementById("create_tmp_table_priv").value;
    var lock_tables_priv = document.getElementById("lock_tables_priv").value;
    var execute_priv = document.getElementById("execute_priv").value;
    var create_view_priv = document.getElementById("create_view_priv").value;
    var show_view_priv = document.getElementById("show_view_priv").value;
    var create_routine_priv = document.getElementById("create_routine_priv").value;
    var alter_routine_priv = document.getElementById("alter_routine_priv").value;
    var event_priv = document.getElementById("event_priv").value;
    var trigger_priv = document.getElementById("trigger_priv").value;

    return 'privgroup_ajaxprocessor.jsp?flag=' + flag + '&privGroupId=' + privGroupId +
            '&Select_priv=' + select_priv + '&Insert_priv=' + insert_priv + '&Update_priv=' +
            update_priv + '&Delete_priv=' + delete_priv + '&Create_priv=' + create_priv +
            '&Drop_priv=' + drop_priv + '&Grant_priv=' + grant_priv + '&References_priv=' +
            references_priv + '&Index_priv=' + index_priv + '&Alter_priv=' + alter_priv +
            '&Create_tmp_table_priv=' + create_tmp_table_priv + '&Lock_tables_priv=' +
            lock_tables_priv + '&Create_view_priv=' + create_view_priv + '&Show_view_priv=' +
            show_view_priv + '&Create_routine_priv=' + create_routine_priv + '&Alter_routine_priv='
            + alter_routine_priv + '&Event_priv=' + event_priv + '&Trigger_priv=' + trigger_priv +
            '&Execute_priv=' + execute_priv;
}

function editPrivGroup(obj, privGroupId) {
    //    jQuery('#connectionStatusDiv').load(createPrivGroupEditUrl('edit', privGroupId),
    //            displayMessagesForPrivGroups);
    document.location.href = 'editPrivilegeGroup.jsp?privGroupId=' + privGroupId;

}

function removePrivilegeGroup(obj, privGroupId) {
    function forwardToDel() {
        var rowId = $(obj).parents('tr:eq(0)').attr('id');
        var url = 'privgroup_ajaxprocessor.jsp?privGroupId=' + encodeURIComponent(privGroupId) +
                '&flag=remove';
        jQuery('#connectionStatusDiv').load(url, displayMessagesForPrivGroups);
    }

    CARBON.showConfirmationDialog('Do you want to remove privilege group?', forwardToDel);
}

function displayMessages(msg) {
    if (msg.search(/Database server instance has been successfully added/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/A database instance with the same name already exists/) != -1) {
        CARBON.showErrorDialog(msg);
    } else if (msg.search(/Database server instance has been successfully edited/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Database Server Instance has been successfully removed/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Unable to remove database server instance/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to add database server instance/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'instances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function displayMessagesForDB(msg) {
    if (msg.search(/Database has been successfully created/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/A database with the same name already exists/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'databases.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function displayMessagesForUser(msg) {
    if (msg.search(/User has been successfully created/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/User has been successfully edited/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create user/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to edit user/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMsg(msg) {
    var successMsg = new RegExp("^Database connection is successful with driver class");
    if (msg.search(successMsg) == -1) //if match failed
    {
        CARBON.showErrorDialog(msg);
    } else {
        CARBON.showInfoDialog(msg);
    }

}

function displayMessagesForPrivGroups(msg) {
    if (msg.search(/Privilege group has been successfully created/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'privilegeGroups.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/Privilege group has been successfully edited/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'privilegeGroups.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Privilege group has been successfully removed/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'privilegeGroups.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create privilege group/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'privilegeGroups.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to remove privilege group/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'privilegeGroups.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        CARBON.showErrorDialog('Failed to create privilege group');
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

function createDataSource(dbInsId, userId) {
    var url = 'user_ajaxprocessor.jsp?dbInsId=' + dbInsId + '&userId=' + userId +
            '&flag=createDS';
    jQuery('#connectionStatusDiv').load(url, displayMessagesForCarbonDS);
}

function displayMessagesForCarbonDS(msg) {
    if (msg.search(/Carbon datasource has been successfully created/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Unable to create carbon datasource/) != -1) {
        jQuery(document).init(function() {
            function handleOK() {
                window.location = 'users.jsp';
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

function editUser(dbInsId, rssInsId, username, userId, password) {
    //jQuery('#connectionStatusDiv').load(url, displayMessagesForUser);

    $(document.forms['dataForm']).submit(function(msg) {

    });
}

function selectAllOptions() {
    var selectAll = document.getElementById('selectAll');
    var c = new Array();
    c = document.getElementsByTagName('input');
    if (selectAll.checked) {
        for (var i = 0; i < c.length; i++){
            if (c[i].type == 'checkbox'){
                c[i].checked = true;
            }
        }
    } else {
        for (var j = 0; j < c.length; j++){
            if (c[j].type == 'checkbox'){
                c[j].checked = false;
            }
        }
    }
}

