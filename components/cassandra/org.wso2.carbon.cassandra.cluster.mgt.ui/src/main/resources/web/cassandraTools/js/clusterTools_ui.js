function displayNodeOperations(hostName,token) {
   location.href = 'node_operations.jsp?hostName=' + hostName+'&token='+token;
}

function displayKeyspaceOperations() {
    location.href = 'keyspace_operations.jsp';
}

function displayColumnFamlilyOperations(keyspace) {
    location.href = 'column_family_operations.jsp?keyspace=' + keyspace;
}

function decommissionNode(nodeCount,hostName)
{
    if(nodeCount>1)
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.decommission.node.warning"], function() {
            var url = 'decommision_node-ajaxprocessor.jsp';
            jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.decommission.node.fail"]);
                        return false;
                    }
                    else
                    {
                        if(data.success=="no")
                        {
                            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.decommission.node.fail"]);
                            return false;
                        }
                    }
                }, "json");
        });
    }
    else
    {
            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tools.decommission.node.exists"], function () {
                CARBON.closeWindow();
            }, function () {
                CARBON.closeWindow();
            });
        return false;
    }
    return true;
}

function drainNode(hostName)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.drain.node.warning"], function() {
        var url = 'drain_node-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.drain.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.drain.node.fail"]);
                        return false;
                    }
                }
            }, "json");
    });

    return true;
}

function performGC()
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.warning"], function() {
        var url = 'performGC-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.fai"]);
                        return false;
                    }
                }
            }, "json");
    });

    return false;
}
function moveNode()
{
    var newToken=jQuery('#newToken').val();
    if(newToken!=null)
    {
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.move.node.warning"], function() {
        var url = 'move_node-ajaxprocessor.jsp?newToken='+newToken;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.error"]);
                    jQuery('#newToken').val("");
                    jQuery('#myDiv').hide('slow');
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.error"]);
                        jQuery('#newToken').val("");
                        jQuery('#myDiv').hide('slow');
                        return false;
                    }
                jQuery('#newToken').val("");
                jQuery('#myDiv').hide('slow');
                }
            }, "json");
    });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.token.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
return true;
}

function showTokenForm()
{
   jQuery('#myDiv').slideToggle();

}
function showTakeSnapShotForm()
{
    jQuery('#clearSnapshotTag').val("");
    jQuery('#divClearSnapShot').hide('slow');
    jQuery('#divSnapShot').slideToggle();

}
function showClearSnapShotForm()
{
    jQuery('#snapshotTag').val("");
    jQuery('#divSnapShot').hide('slow');
    jQuery('#divClearSnapShot').slideToggle();

}
function showKSTakeSnapShotForm(keyspace)
{
    jQuery('#'+keyspace+'ClearTag').val("");
    jQuery('#'+keyspace+'DivClear').hide('slow');
    jQuery('#'+keyspace+'DivTake').slideToggle();

}
function showKSClearSnapShotForm(keyspace)
{
    jQuery('#'+keyspace+'TakeTag').val("");
    jQuery('#'+keyspace+'DivTake').hide('slow');
    jQuery('#'+keyspace+'DivClear').slideToggle();

}
function joinRing()
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.warning"], function() {
        var url = 'joinRing-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="no")
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.fail"]);
                               return false;
                           }
                           else
                           {
                               window.location.reload();
                           }
                       }
                   }, "json");
    });

    return false;
}
function disableRPC(){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.warning"], function() {
        var url = 'diableRPCServerStatus-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                        return true;
                    }
                }
            }, "json");
    });

    return false;
}
function enableRPC(){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.warning"], function() {
        var url = 'enableRPCServerStatus-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                        return true;
                    }
                }
            }, "json");
    });

    return false;
}
function disableGossip(){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.warning"], function() {
        var url = 'disableGossipServerStatus-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                        return true;
                    }
                }
            }, "json");
    });

    return false;
}
function enableGossip(){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.warning"], function() {
        var url = 'enableGossipServerStatus-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startGossip.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startGossip.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                        return true;
                    }
                }
            }, "json");
    });

    return false;
}
function enableIncrementalBackUp()
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.warning"], function() {
        var url = 'enableIncrementalBackup-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                    }
                }
            }, "json");
    });

    return false;
}
function disableIncrementalBackUp()
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.warning"], function() {
        var url = 'disableIncrementalBackup-ajaxprocessor.jsp';
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.fail"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.fail"]);
                        return false;
                    }
                    else
                    {
                        window.location.reload();
                    }
                }
            }, "json");
    });

    return false
}



function takeNodeSnapShot()
{
    var snapshotTag=jQuery('#snapshotTag').val();
    if(snapshotTag!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.warning"], function() {
            var url = 'takeSnapShot_node-ajaxprocessor.jsp?snapshotTag='+snapshotTag;
            jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                        jQuery('#snapshotTag').val("");
                        jQuery('#divSnapShot').hide('slow');
                        return false;
                    }
                    else
                    {
                        if(data.success=="no")
                        {
                            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                            jQuery('#snapshotTag').val("");
                            jQuery('#divSnapShot').hide('slow');
                            return false;
                        }
                        jQuery('#snapshotTag').val("");
                        jQuery('#divSnapShot').hide('slow');
                    }
                }, "json");
        });
    }
    else
    {
        jQuery('#snapshotTag').val("");
        jQuery('#divSnapShot').hide('slow');
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        jQuery('#snapshotTag').val("");
        jQuery('#divSnapShot').hide('slow');
        return false;
    }
    return true;
}
function clearNodeSnapShot()
{
    var clearSnapshotTag=jQuery('#clearSnapshotTag').val();
    if(clearSnapshotTag!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.warning"], function() {
            var url = 'clearSnapShot_node-ajaxprocessor.jsp?clearSnapshotTag='+clearSnapshotTag;
            jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                        jQuery('#clearSnapshotTag').val("");
                        jQuery('#divClearSnapShot').hide('slow');
                        return false;
                    }
                    else
                    {
                        if(data.success=="no")
                        {
                            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                            jQuery('#clearSnapshotTag').val("");
                            jQuery('#divClearSnapShot').hide('slow');
                            return false;
                        }
                        jQuery('#clearSnapshotTag').val("");
                        jQuery('#divClearSnapShot').hide('slow');
                    }
                }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}
function repairKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.repair.keyspace.warning"], function() {
        var url = 'repair_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog("cassandra.cluster.repair.keyspace.error");
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}

function cleanUpKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.warning"], function() {
        var url = 'cleanUp_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function flushKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.warning"], function() {
        var url = 'flush_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function scrubKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.warning"], function() {
        var url = 'scrub_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function upgradeSSTablesKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.warning"], function() {
        var url = 'upgradeSSTables_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}

function compactKeyspace(keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.warning"], function() {
        var url = 'compact_ks-ajaxprocessor.jsp?keyspace='+keyspace;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function compactColumnFamily(keyspace)
{
    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.warning"], function() {
        var url = 'compact_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function flushColumnFamily(keyspace)
{
    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.warning"], function() {
        var url = 'flush_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function repairColumnFamily(keyspace)
{

    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.warning"], function() {
        var url = 'repair_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function cleanUpColumnFamily(keyspace)
{
    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.warning"], function() {
        var url = 'cleanUp_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="no")
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}
function scrubColumnFamily(keyspace)
{
    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.warning"], function() {
        var url = 'scrub_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function upgradeSSTablesColumnFamily(keyspace)
{
    var column_family = jQuery("#column_family option:selected").text();
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.warning"], function() {
        var url = 'upgradeSSTables_cf-ajaxprocessor.jsp?keyspace='+keyspace+"&columnFamily="+column_family;
        jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.column.family.error"]);
                    return false;
                }
                else
                {
                    if(data.success=="no")
                    {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.column.family.error"]);
                        return false;
                    }
                }
            }, "json");
    });
    return true;
}
function takeKSNodeSnapShot(keyspace)
{
    var snapshotTag=jQuery('#'+keyspace+'TakeTag').val();
    if(snapshotTag!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.warning"], function() {
            var url = 'takeSnapShot_node-ajaxprocessor.jsp?snapshotTag='+snapshotTag+"&keyspace="+keyspace;
            jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                        jQuery('#'+keyspace+'TakeTag').val("");
                        jQuery('#'+keyspace+'DivTake').hide('slow');
                        return false;
                    }
                    else
                    {
                        if(data.success=="no")
                        {
                            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                            jQuery('#'+keyspace+'TakeTag').val("");
                            jQuery('#'+keyspace+'DivTake').hide('slow');
                            return false;
                        }
                        jQuery('#'+keyspace+'TakeTag').val("");
                        jQuery('#'+keyspace+'DivTake').hide('slow');
                    }
                }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}
function clearKSNodeSnapShot(keyspace)
{
    var clearSnapshotTag=jQuery('#'+keyspace+'ClearTag').val();
    if(clearSnapshotTag!=null)
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.warning"], function() {
            var url = 'clearSnapShot_ks-ajaxprocessor.jsp?clearSnapshotTag='+clearSnapshotTag+"&keyspace="+keyspace;
            jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                        jQuery('#'+keyspace+'ClearTag').val("");
                        jQuery('#'+keyspace+'DivClear').hide('slow');
                        return false;
                    }
                    else
                    {
                        if(data.success=="no")
                        {
                            CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                            jQuery('#'+keyspace+'ClearTag').val("");
                            jQuery('#'+keyspace+'DivClear').hide('slow');
                            return false;
                        }
                        else
                        {
                        jQuery('#'+keyspace+'ClearTag').val("");
                        jQuery('#'+keyspace+'DivClear').hide('slow');
                        return true;
                        }
                    }
                }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return false;
}