$(document).ready(function () {
    $('.api-documentation a.accordion-toggle').click(
            function () {
                $(this).parent().next().toggle('blind');
            }
    );
});

var docView = function (provider, apiName, version, docName) {
    var current = window.location.pathname;
    if (current.indexOf(".jag") >= 0) {
        window.open("doc-viewer.jag?docName=" + docName + "&name=" + apiName + "&version=" + version + "&provider=" + provider);
    } else {
        window.open("../site/pages/doc-viewer.jag?docName=" + docName + "&name=" + apiName + "&version=" + version + "&provider=" + provider);
    }

};
