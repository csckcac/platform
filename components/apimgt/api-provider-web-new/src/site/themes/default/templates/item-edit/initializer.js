var getTemplateFile = function() {
    return "tmpl/item-edit/template.jag";
};

var initialize = function (jagg) {
    addHeaderJS(global, "edit", "edit", "tmpl/item-edit/js/edit.js");
};

var getData = function (params) {
    var api = params.api;
    if (typeof api.name != "string") {
        location.href = "login.jag";
    }
    $("#editDescription").text(api.description);

    $("#context").attr("value", api.context);
    $("#editEndpoint").attr("value", api.endpoint);
    $("#editWsdl").attr("value", api.wsdl);
    $("#editTags").attr("value", api.tags);
    if (typeof api.thumb) {
        $("img#apiEditThumb").attr("src", "images/api-default.png");

    } else {
        $("img#apiEditThumb").attr("src", api.thumb);

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

};

var getParams = function () {

};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
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