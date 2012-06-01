var jagg = jagg || {};

(function () {
    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + args[0];
        $.post.apply(this, args);
    };

    jagg.message = function (content) {
        $('#messageModal').html($('#confirmation-data').html());
        $('#messageModal h3.modal-title').html('API Store');
        $('#messageModal div.modal-body').html(content);
        $('#messageModal a.btn-primary').html('OK');
        $('#messageModal a.btn-other').hide();

        $('#messageModal a.btn-primary').click(function() {
            $('#messageModal').modal('hide');
        });
        $('#messageModal').modal();
    };

    jagg.initStars = function (elem, saveCallback, removeCallback, data) {
        $('.dynamic-rating-stars a', elem).each(function () {
            $(this).mouseover(function () {
                        var rating = $('a', $(this).parent()).index(this) + 1;
                        $('.selected-rating', $(this).parent().parent()).html(rating);
                        $('a', $(this).parent()).each(function (index) {
                            if (index < rating) {
                                $(this).removeClass("star-0").addClass("star-1");
                            } else {
                                $(this).removeClass("star-1").addClass("star-0");
                            }
                        });
                    }).click(function () {
                        var rating = $('a', $(this).parent()).index(this) + 1;
                        $(this).parent().parent().data("rating", rating);
                        saveCallback(rating, $(".dynamic-rating", elem).data("rating-meta"));
                    }).mouseleave(function () {
                        var rating = $(this).parent().parent().data("rating");
                        rating = rating || 0;
                        $('.selected-rating', $(this).parent().parent()).html(rating);
                        $('a', $(this).parent()).each(function (index) {
                            if (index < rating) {
                                $(this).removeClass("star-0").addClass("star-1");
                            } else {
                                $(this).removeClass("star-1").addClass("star-0");
                            }
                        });
                    });
        });

        $(".dynamic-rating", elem).data("rating-meta", data).data("rating", $(".selected-rating", elem).text());

        $(".remove-rating", elem).click(function () {
            removeCallback($(".dynamic-rating", elem).data("rating-meta", data));
        });
    };
}());