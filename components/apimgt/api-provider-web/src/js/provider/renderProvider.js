var apiData;
var addAPI;

var login = function () {
    return apiProviderApp.login();
};

var logout = function () {
    apiProviderApp.logout();
};

var saveAPI = function () {

    var apiName = $("#apiName").val();
    var context = $("#context").val();
    var version = $("#version").val();
    var thumbUrl = $("#apiThumb").val();
    var description = $("#description").val();
    var endpoint = $("#endpoint").val();
    var wsdl = $("#wsdl").val();
    var tags = $("#tags").val();
    var tier = $("#tier").val();

    if (apiName == "" || version == "" || context == "") {
        alert("Please enter newly added API name and its version");
    } else {
        apiProviderApp.call("action=saveAPI&apiName=" + apiName + "&version=" + version + "&description=" + description
            + "&endpoint=" + endpoint + "&wsdl=" + wsdl + "&tags=" + tags + "&tier=" + tier + "&thumbUrl=" + thumbUrl + "&context=" + context, function (json) {
            if (json.error == "true") {
                alert(json.message);
            } else {
                $("#addAPIForm")[0].reset();
                location.href = "?place=";
            }
        });
    }
};

var getAPI = function (apiName, version) {
    apiProviderApp.call("action=getAPI&apiName=" + apiName + "&version=" + version, function (json) {
        if (json.error == "true") {
            alert(json.message);
        }
        else {
            renderAPI(json);
        }
    });

};

$(document).ready(function () {
    var options = {
        success:function () {
            location.href = "index.jag?place=api-details&name=" + apiProviderApp.currentAPIName + "&version=" + apiProviderApp.currentVersion;
        }
    };
    $('#editAPIForm').submit(function () {
        $(this).ajaxSubmit(options);
        return false;
    });

    $('#editAPIForm #apiName').val(apiProviderApp.currentAPIName);
    $('#editAPIForm #version').val(apiProviderApp.currentVersion)
});


var updateAPI = function () {
    var apiName = apiProviderApp.currentAPIName;
    var version = apiProviderApp.currentVersion;
    var description = $("#editDescription").val();
    var image = $("#imageUrl").val();

    if ($.isEmptyObject(image)) {
        image = 'images/api-default.png';
    }
    var endpoint = $("#editEndpoint").val();
    var wsdl = $("#editWsdl").val();
    var tags = $("#editTags").val();
    var tier = $("#editTier").val();
    var status = $("#editStatus").val();
    var resourceUri = $("#resourceUri").val();
    var resourceMethod = $("#resourceMethod").val();
    var resourceUriTemp = $("#uriTemplate").val();
    var context = $("#hiddenContext").val();

    apiProviderApp.call("action=updateAPI&apiName=" + apiName + "&version=" + version + "&description=" + description + "&imageUrl=" + image + "&endpoint=" + endpoint + "&wsdl=" + wsdl + "&tags=" + tags + "&tier=" + tier + "&status=" + status
        + "&resourceTemplate=" + resourceUriTemp + "&resourceUri=" + resourceUri + "&resourceMethod=" + resourceMethod + "&context=" + context, function (json) {
        if (json.error == "true") {
            alert(json.message);
        } else {
            $("#editAPIForm")[0].reset();
            location.href = "?place=";
        }
    });


};

var renderAPI = function (result) {

    var api = result.data.api[0];

    apiProviderApp.currentAPI = api;

    if (typeof api.name != "string") {
        location.href="login.jag";
    }

    if (typeof api.description != "string") {
        $("div#apiView").text('');
    } else {
        $("div#apiView").text(api.description);
    }
    $("td#inUrl").text(api.endpoint);
    $("td#wsdl").text(api.wsdl);
    $("td#inUpdated").text(api.lastUpdated);
    $("td#tierAvb").text(api.availableTiers);
    $("span#status").append("&nbsp;" + api.status);
    $("span#version").append("&nbsp;v" + api.version);
    if (typeof api.thumb != "string") {
        $("img#apiThumb").attr("src", "images/api-default.png");
    } else {
        $("img#apiThumb").attr("src", api.thumb);
    }
    if (api.subs == "1") {
        $('.userCount').text(api.subs + ' User');
    } else {
        $('.userCount').text(api.subs + ' Users');
    }

    $("#editDescription").text(api.description);
    $("#context").attr("value", api.context);
    $("#editEndpoint").attr("value", api.endpoint);
    $("#editWsdl").attr("value", api.wsdl);
    $("#editTags").attr("value", api.tags);
    if (typeof api.thumb) {
        $("img#apiEditThumb").attr("src", "images/api-default.png");
        //$("#imageUrl").attr("value", "images/api-default.png");
    } else {
        $("img#apiEditThumb").attr("src", api.thumb);
        //$("#imageUrl").attr("value", api.thumb);
    }
    var availTier = api.availableTiers;
    if (availTier == 'Gold') {
        document.getElementById('editTier').selectedIndex = 0;
    }
    else {
        document.getElementById('editTier').selectedIndex = 1;
    }
    var status = api.status;
    if (status == 'CREATED') {
        $("#editStatus option[value='CREATED']").attr('selected', 'selected');
    }
    else if (status == 'PUBLISHED') {
        $("#editStatus option[value='CREATED']").remove();
        $("#editStatus option[value='PUBLISHED']").attr('selected', 'selected');

    } else if (status == 'DEPRECATED') {
        $("#editStatus option[value='CREATED']").remove();
        $("#editStatus option[value='PUBLISHED']").remove();
        $("#editStatus option[value='DEPRECATED']").attr('selected', 'selected')
    }
    else if (status == 'RETIRED') {
        $("#editStatus option[value='CREATED']").remove();
        $("#editStatus option[value='PUBLISHED']").remove();
        $("#editStatus option[value='RETIRED']").attr('selected', 'selected')
    }
    else if (status == 'BLOCKED') {
        $("#editStatus option[value='CREATED']").remove();
        $("#editStatus option[value='PUBLISHED']").remove();
        $("#editStatus option[value='BLOCKED']").attr('selected', 'selected')
    }

    createResourceRows(api);

    //$("#hiddenContext").attr("value", api.context);
    $("#apiDetails h2")[0].innerHTML = api.name + "-v" + api.version;

    $('#apiLoading').hide();
    $('#myTabContent').show();

};

var deleteResource = function (id) {
    var count=$('#resourceTable tr').length;
    //Check whether only one defined resource remains before delete operation
    if(count==3){
        $('#resourceTableError').show('fast');
        return;
    }
    $('#resourceTableError').hide('fast');
    $('#item-' + id).remove();
    $('[name=resourceMethod-' + id + ']').remove();
    $('[name=uriTemplate-' + id + ']').remove();
    rowNums.splice(rowNums.indexOf(id),1);
    $('#resourceCount').val(rowNums);

};

var createResourceRows = function (api) {
    if (api.templates.length > 0) {
        for (var i = 0; i < api.templates.length; i++) {
            $('#resourceRow').clone(true).attr('id', 'item-' + resourcesCount).insertAfter($('#resourceRow'));
            var thisItem = '#item-' + resourcesCount;
            $('#item-' + resourcesCount + ' #resourceMethod').val(api.templates[i][1]);
            $('#item-' + resourcesCount + ' #uriTemplate').val(api.templates[i][0]);
            $('#item-' + resourcesCount + ' #uriTemplate').attr('disabled', 'disabled');
            $('#item-' + resourcesCount + ' #resourceMethod').attr('disabled', 'disabled');
            $('#item-' + resourcesCount + ' td#buttons a#resourceAddBtn').remove();
            $('#item-' + resourcesCount + ' td#buttons').append("<a class=\"btn btn-danger\" onclick=\"deleteResource(" + resourcesCount + ")\"><i class=\"icon-trash icon-white\"></i> Delete</a>");


            $('<input>').attr('type', 'hidden')
                .attr('name', 'resourceMethod-' + resourcesCount).attr('value', api.templates[i][1])
                .appendTo('#editAPIForm');

            $('<input>').attr('type', 'hidden')
                .attr('name', 'uriTemplate-' + resourcesCount).attr('value', api.templates[i][0])
                .appendTo('#editAPIForm');

            rowNums.push(resourcesCount);
            resourcesCount++;

            if ($('#resourceCount').length == 0) {
                $('<input>').attr('type', 'hidden')
                    .attr('name', 'resourceCount')
                    .attr('id', 'resourceCount')
                    .attr('value', rowNums)
                    .appendTo('#editAPIForm');
            } else {
                $('#resourceCount').attr('value', rowNums);
            }
        }
    }
};

var getAPIsByProvider = function (callbackFunc) {
    $('.loading').html('loading...');
    apiProviderApp.call("action=getAPIsByProvider", function (json) {
        if (json.error == "true") {
            alert(json.message);
        } else {
            $('.loading').remove();
            callbackFunc(json);
        }
    });

};
var renderAPIsPaginator = function (result, currentPage, itemsPerPage) {
    var apiList = $("#home");
    var tw = $(".thumbnails", apiList);
    var ew = $(".element-wrapper ul", apiList).empty();

    if (result.error == "true") {
        return;
    }
    var apis = result.data.apis;

    var from = currentPage * itemsPerPage - itemsPerPage;
    var to = currentPage * itemsPerPage;

    if (to > apis.length) {
        to = apis.length;
    }

    var tl = $(".span2", tw);
    // var tc=$(".thumbnail", tl);
    for (var i = from; i < to; i++) {
        var api = apis[i];
        var clone = tl.clone();
        ew.append(clone);
        $(".thumbnail a", clone).attr("href", "?place=api-details&name=" + api.name + "&version=" + api.version);

        if (typeof api.thumb == "object") {
            $(".thumb", clone).attr("src", "images/api-default.png");
        } else {
            $(".thumb", clone).attr("src", api.thumb);
        }
        $(".thumbnail h5 a", clone).append(api.name + "-v" + api.version);
        $("#noOfUsers", clone).text(api.subs + ' Users');

        $(".thumbnail h5 a", clone).attr("href", "?place=api-details&name=" + api.name + "&version=" + api.version);
        $(".status", clone).append(api.status);
    }
};
var renderAPIs = function (result) {
    /*Pagination logic
     * itemsPerPage
     * current
     *
     * */

    if (result.error == "true") {
        return;
    }
    var itemCount = result.data.apis.length;
    if (itemCount == 0) {
        if(!addAPI){
        location.href = "?place=add";
        }
        return;
    }
    var itemsPerPage = 10; //reduce this number to preview the pagination
    var currentPage = 1;
    var numberOfPages = itemCount / itemsPerPage;
    if (itemCount % itemsPerPage != 0) {
        numberOfPages++;
    }
    if ($.cookie('currentPage') != null) {
        currentPage = parseInt($.cookie('currentPage'));
        if (numberOfPages < currentPage || currentPage <= 0) {
            currentPage = 1;
        }
    }
    $('#pagination ul').html('');
    renderAPIsPaginator(result, currentPage, itemsPerPage);
    var prev = $('<li><a>Prev</a></li>');
    var next = $('<li><a>Next</a></li>');
    $(prev).click(
        function () {
            if (currentPage == 1) {
                return;
            }
            currentPage--;
            $.cookie('currentPage', "" + currentPage);
            renderAPIsPaginator(result, currentPage, itemsPerPage);
            $('#pagination ul li').removeClass('active');
            $('#pagination ul li:nth-child(' + (currentPage + 1) + ')').addClass('active');
        }
    );
    $(next).click(
        function () {
            if (currentPage == numberOfPages) {
                return;
            }
            currentPage++;
            $.cookie('currentPage', "" + currentPage);
            renderAPIsPaginator(result, currentPage, itemsPerPage);
            $('#pagination ul li').removeClass('active');
            $('#pagination ul li:nth-child(' + (currentPage + 1) + ')').addClass('active');
        }
    );

    if (itemCount > itemsPerPage) {
        $('#pagination ul').append(prev);
        for (var i = 1; i <= numberOfPages; i++) {
            var theLi;
            if (i == currentPage) {
                theLi = $('<li class="active"><a>' + i + '</a></li>');
            } else {
                theLi = $('<li><a>' + i + '</a></li>');
            }
            $('a', theLi).click(function () {
                $.cookie('currentPage', "" + parseInt($(this).text()));
                renderAPIsPaginator(result, parseInt($(this).text()), itemsPerPage);
                $('#pagination ul li').removeClass('active');
                $(this).parent().addClass('active');

            });
            $('#pagination ul').append(theLi);
        }
        //Do pagination
        $('#pagination ul').append(next);
    }
};

var loadAllAPIUsageByProvider = function () {
    apiProviderApp.call("action=getAllAPIUsageByProvider", function (json) {
        if (json.error == "true") {
            alert(json.message);
            return json.error;
        } else {
            renderUsers(json);
        }
    });

};

var renderUsers = function (result) {
    var users = result.data.subscribers;
    var doc=document;
    for (var i = 0; i < users.length; i++) {
        var user = users[i];
        var tabBody = doc.getElementById("users");
        var row = doc.createElement("tr");
        var cell1 = doc.createElement("td");
        var cell2 = doc.createElement("td");
        var cell3 = doc.createElement("td");

        var icon = doc.createElement("i");
        icon.setAttribute("class", "icon-user");
        var a = doc.createElement("a");
        var userName = user.userName;
        a.setAttribute("href", "?place=user&uname=" + userName);
        a.innerHTML = userName;
        cell1.appendChild(icon);
        cell1.appendChild(a);

        cell2.innerHTML = user.application;

        var apiList = user.apis;
        var apiName;
        var version;
        var api;
        if(apiList.indexOf(",")>=0){
            var apis=apiList.split(",");
            for(var n=0;n<apis.length;n++){
            api=apis[n].split("-");
            apiName=api[0];
            version=api[1];
            var eleArray=[];
            eleArray[n] = doc.createElement("a");
            eleArray[n].setAttribute("href", "?place=api-details&name=" + apiName + "&version=" + version);
            eleArray[n].innerHTML=" "+apis[n];
            if(n!=apis.length-1){eleArray[n].innerHTML+=" ,";}
            cell3.appendChild(eleArray[n]);
            }
        }else{
        api= apiList.split("-");
        apiName = api[0];
        version = api[1];
        var a2 = doc.createElement("a");
        a2.setAttribute("href", "?place=api-details&name=" + apiName + "&version=" + version);
        a2.innerHTML = apiList;
        cell3.appendChild(a2);
        }

        row.appendChild(cell1);
        row.appendChild(cell2);
        row.appendChild(cell3);
        tabBody.appendChild(row);
    }

};

var loadSubscribersOfAPI = function (apiName, version) {
    apiProviderApp.call("action=getSubscribersOfAPI&apiName=" + apiName + "&version=" + version, renderUsersList);

};

var renderUsersList = function (result) {
    var users = result.data.subscribers;
    var doc=document;
    for (var i = 0; i < users.length; i++) {
        var user = users[i];
        var tabBody = doc.getElementById("userList");
        var row = doc.createElement("tr");
        var cell1 = doc.createElement("td");
        var cell2 = doc.createElement("td");
        var cell3 = doc.createElement("td");
        var icon = doc.createElement("i");
        icon.setAttribute("class", "icon-user");
        var a = doc.createElement("a");
        a.setAttribute("href", "?place=user&uname=" + user.userName);
        a.innerHTML = user.userName;
        cell1.appendChild(icon);
        cell1.appendChild(a);

        cell2.innerHTML = user.subscribedDate;

        var icon2 = doc.createElement("i");
        icon2.setAttribute("class", "icon-edit");
        var a2 = doc.createElement("a");
        a2.setAttribute("href", "#");
        a2.innerHTML = "Stats";
        cell3.appendChild(icon2);
        cell3.appendChild(a2);

        var icon3 = doc.createElement("i");
        icon3.setAttribute("class", "icon-trash");
        var a3 = doc.createElement("a");
        a3.setAttribute("href", "#");
        a3.innerHTML = "Revoke Access";
        cell3.appendChild(icon3);
        cell3.appendChild(a3);

        var icon4 = doc.createElement("i");
        icon4.setAttribute("class", "icon-ban-circle");
        var a4 = doc.createElement("a");
        a4.setAttribute("href", "#");
        a4.innerHTML = "Block";
        cell3.appendChild(icon4);
        cell3.appendChild(a4);

        row.appendChild(cell1);
        row.appendChild(cell2);
        row.appendChild(cell3);
        tabBody.appendChild(row);
    }

};

var getSubscribedAPIs = function (userName) {
    apiProviderApp.call("action=getSubscribedAPIs&username="+userName, function (json) {
        if (json.error == "true") {
            alert(json.message);
        }
        else {
            renderSubscribedAPIs(json);

        }
    });
};

var renderSubscribedAPIs = function (result) {
    var apis = result.data.apis;
    var doc=document;
    for (var i = 0; i < apis.length; i++) {
        var api = apis[i];
        var tabBody = doc.getElementById("userSubscribedAPIs");
        var row = doc.createElement("tr");
        var cell1 = doc.createElement("td");
        var cell2 = doc.createElement("td");
        var cell3 = doc.createElement("td");
        var icon = doc.createElement("i");
        icon.setAttribute("class", "icon-user");
        var a = doc.createElement("a");
        a.setAttribute("href", "?place=api-details&name=" + api.name + "&version=" + api.version);
        a.innerHTML = api.name;
        cell1.appendChild(icon);
        cell1.appendChild(a);

        cell2.innerHTML = api.lastUpdatedDate;

        var icon2 = doc.createElement("i");
        icon2.setAttribute("class", "icon-edit");
        var a2 = doc.createElement("a");
        a2.setAttribute("href", "#");
        a2.innerHTML = "Stats";
        cell3.appendChild(icon2);
        cell3.appendChild(a2);

        var icon3 = doc.createElement("i");
        icon3.setAttribute("class", "icon-trash");
        var a3 = doc.createElement("a");
        a3.setAttribute("href", "#");
        a3.innerHTML = "Revoke Access";
        cell3.appendChild(icon3);
        cell3.appendChild(a3);

        var icon4 = doc.createElement("i");
        icon4.setAttribute("class", "icon-ban-circle");
        var a4 = doc.createElement("a");
        a4.setAttribute("href", "#");
        a4.innerHTML = "Block";
        cell3.appendChild(icon4);
        cell3.appendChild(a4);

        row.appendChild(cell1);
        row.appendChild(cell2);
        row.appendChild(cell3);
        tabBody.appendChild(row);
    }

};

var loadSubscribersOfProvider = function () {
    apiProviderApp.call("action=getSubscribersOfProvider", function (json) {
        if (json.error == "true") {
            alert(json.message);
            return json.error;
        } else {
            renderUsers(json);
        }
    });

};

var loadDocs = function (apiName, version) {
    apiProviderApp.call("action=getAllDocumentation&apiName=" + apiName + "&version=" + version, renderDocs);
};


var renderDocs = function (result) {
    var docs = result.data.docs;
    $('#listDocs').html('');
    var docVar=document;
    for (var i = 0; i < docs.length; i++) {
        var doc = docs[i];
        var tabBody = docVar.getElementById("listDocs");
        var row = docVar.createElement("tr");
        row.setAttribute("id", apiProviderApp.currentAPIName + '-' + doc.docName);
        var cell1 = docVar.createElement("td");
        var cell2 = docVar.createElement("td");
        var cell3 = docVar.createElement("td");
        var cell4 = docVar.createElement("td");
        var cell5 = docVar.createElement("td");
        var cell6 = docVar.createElement("td");
        var icon = docVar.createElement("i");
        icon.setAttribute("class", "icon-file");
        var a = docVar.createElement("a");
        a.setAttribute("href", "#");
        a.appendChild(icon);
        a.innerHTML = doc.docName;
        cell1.appendChild(a);

        cell2.innerHTML = doc.docType;
        var icon1 = docVar.createElement("i");
        icon1.setAttribute("class", "icon-file");
        var a1 =docVar.createElement("a");
        if (docs.sourceType == "url") {
            a1.setAttribute("href", "#");
        } else {
            a1.setAttribute("href", "#");
        }
        a1.appendChild(icon1);
        a1.innerHTML = "View";

        cell3.appendChild(a1);

        var icon2 = docVar.createElement("i");
        icon2.setAttribute("class", "icon-user");
        var a2 = docVar.createElement("a");
        a2.setAttribute("href", "#");
        a2.appendChild(icon2);
        a2.innerHTML = $('#userNameShow').text();
        cell4.appendChild(a2);

        cell5.innerHTML = doc.lastUpdated;

        var icon3 = docVar.createElement("i");
        icon3.setAttribute("class", "icon-edit");
        var a3 = docVar.createElement("a");
        a3.setAttribute("href", 'javascript:updateDocumentation("' + apiProviderApp.currentAPIName + '","' + apiProviderApp.currentVersion + '","' + doc.docName + '","' + doc.docType + '","' + doc.summary + '","' + doc.docUrl + '")');
        a3.appendChild(icon3);
        a3.innerHTML = "Update | ";

        if (doc.sourceType == "INLINE") {

            var icon6 = docVar.createElement("i");
            icon6.setAttribute("class", "icon-edit");
            var a6 = docVar.createElement("a");
            a6.setAttribute("href", 'javascript:editInlineContent("' + apiProviderApp.currentAPIName + '","' + apiProviderApp.currentVersion + '","' + doc.docName + '","' + doc.docType + '","' + doc.summary + '","' + doc.docUrl + '")');
            a6.appendChild(icon6);
            a6.innerHTML = "Edit Content | ";
            cell6.appendChild(a6);
        }

        var icon4 = docVar.createElement("i");
        icon4.setAttribute("class", "icon-trash");
        var a4 = docVar.createElement("a");

        a4.setAttribute("href", 'javascript:removeDocumentation("' + apiProviderApp.currentAPIName + '","' + apiProviderApp.currentVersion + '","' + doc.docName + '","' + doc.docType + '")');
        a4.appendChild(icon4);
        a4.innerHTML = "Delete | ";

        var icon5 = docVar.createElement("i");
        icon5.setAttribute("class", "icon-share");
        var a5 = docVar.createElement("a");
        a5.setAttribute("href", 'javascript:copyDocumentation("' + apiProviderApp.currentAPIName + '","' + apiProviderApp.currentVersion + '","' + doc.docName + '","' + doc.docType + '","' + doc.summary + '")');
        a5.appendChild(icon5);
        a5.innerHTML = "Copy";


        cell6.appendChild(a3);

        cell6.appendChild(a4);
        cell6.appendChild(a5);


        row.appendChild(cell1);
        row.appendChild(cell2);
        row.appendChild(cell3);
        row.appendChild(cell4);
        row.appendChild(cell5);
        row.appendChild(cell6);

        tabBody.appendChild(row);
    }
};

var removeDocumentation = function (apiName, version, docName, docType) {
    apiProviderApp.call("action=removeDocumentation&apiName="
        + apiName + "&version=" + version
        + "&docName=" + docName + "&docType=" + docType, function () {
        $('#' + apiProviderApp.currentAPIName + '-' + docName).hide('slow');
    });
};

var copyDocumentation = function (apiName, version, docName, docType, summary) {
    $('#newDoc .btn-primary').text('update');
    $('#newDoc').show('slow');
    $('#newDoc #docName').val(docName + '-copy');
    $('#newDoc #summary').val(summary);

    for (var i = 1; i <= 6; i++)
        if ($('#optionsRadios' + i).val().toUpperCase().indexOf(docType.toUpperCase()) >= 0) {
            $('#optionsRadios' + i).attr('checked', true)
        }
};

var updateDocumentation = function (apiName, version, docName, docType, summary, docUrl) {
    $('#newDoc .btn-primary').text('update');
    $('#newDoc').show('slow');
    $('#newDoc #docName').val(docName);
    $('#newDoc #summary').val(summary);
    if (docUrl != "undefined") {
        $('#newDoc #docUrl').val(docUrl);
        $('#optionsRadios8').attr('checked', true);
        $('#newDoc #docUrl').show();
    }

    for (var i = 1; i <= 6; i++)
        if ($('#optionsRadios' + i).val().toUpperCase().indexOf(docType.toUpperCase()) >= 0) {
            $('#optionsRadios' + i).attr('checked', true);
        }
};

var editInlineContent = function (apiName, version, docName, docType, summary, docUrl) {

    window.open("includes/provider/inLineEditor.jag?docName=" + docName + "&apiName=" + apiName + "&version=" + version);

};

var addNewDoc = function () {
    var apiName = apiProviderApp.currentAPIName;
    var version = apiProviderApp.currentVersion;
    var docName = $("#docName").val();
    var summary = $("#summary").val();
    var docType = getRadioValue($('input[name=optionsRadios]:radio:checked'));
    var sourceType = getRadioValue($('input[name=optionsRadios1]:radio:checked'));
    var docUrl = $("#docUrl").val();
    apiProviderApp.call("action=addDocumentation&apiName=" + apiName + "&version=" + version + "&docName=" + docName + "&docType=" + docType +
        "&summary=" + summary + "&sourceType=" + sourceType + "&sourceUrl=" + docUrl, function (json) {
        if (json.error == "true") {
            alert(json.message);
        } else {
            clearDocs();
            loadDocs(apiName, version);
        }
    });


};

var clearDocs = function () {
    var doc=document;
    doc.getElementById('docName').value = '';
    doc.getElementById('summary').value = '';
    doc.getElementById('docUrl').value = '';
    $('#newDoc').hide('slow');
};

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    }
    else {
        return 0;
    }
};

var copyAPIToNewVersion = function () {
    var apiName = apiProviderApp.currentAPIName;
    var version = apiProviderApp.currentVersion;
    var newVersion = $("#copyToVersion #newVersion").val();

    apiProviderApp.call("action=createNewAPIVersion&apiName=" + apiName + "&version=" + version + "&newVersion=" + newVersion, function (json) {
        if (json.error == "true") {
            alert(json.message);
        }
        else {
            location.href = "?place=";
        }
    });
};
var isContextExist = function () {
    var context = $("#context").val();
    if (context == apiProviderApp.currentAPI.context) {
        $("#context").val(context);
        return;
    }
    if (context.charAt(0) != "/") {
        context = "/" + context;
    }
    apiProviderApp.call("action=isContextExist&context=" + context, function (json) {
        if (json.error == "true") {
            alert(json.message);
        }
        else {
            var contextExist = json.data.contextExist;
            if (contextExist == 'true') {
                alert("Duplicated context value.Please add another one");
                document.getElementById('context').value = apiProviderApp.currentAPI.context;
            }

        }
    });
};

var popupText = function () {
    var selectValue = $("#editStatus").val();
    if (selectValue == "PUBLISHED") {
        $('#resourceTableDiv').show('fast');
    } else {
        $('#resourceTableDiv').hide('fast');
    }
};








