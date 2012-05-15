$(document).ready(function () {

    $('.subscription-list a.accordion-toggle').click(
            function () {
                $(this).parent().next().toggle('blind');
            }
    );

    $(".key-generate-button").click(function () {
        var elem = $(this);
        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"generateAPIKey",
            name:elem.attr("data-name"),
            version:elem.attr("data-version"),
            provider:elem.attr("data-provider"),
            context:elem.attr("data-context"),
            application:elem.attr("data-application"),
            keyType:elem.attr("data-keyType")
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                $("#subscribe-button").html('Subscribe').addClass('green').removeClass('disabled').removeAttr('disabled');
                jagg.message(result.message);
            }
        }, "json");

        $(this).html('Please wait...').removeClass('green').addClass('disabled').attr('disabled', 'disabled');
    });
});