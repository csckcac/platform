var jagg = jagg || {};

(function () {
    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + args[0];
        $.post.apply(this, args);
    };

   jagg.messageDisplay = function (params) {
        $('#messageModal').html($('#confirmation-data').html());
        if(params.title == undefined){
            $('#messageModal h3.modal-title').html('API Publisher');
        }else{
            $('#messageModal h3.modal-title').html(params.title);
        }
        $('#messageModal div.modal-body').html(params.content);
        if(params.buttons != undefined){
            $('#messageModal a.btn-primary').hide();
            for(var i=0;i<params.buttons.length;i++){
                $('#messageModal div.modal-footer').append($('<a class="btn '+params.buttons[i].cssClass+'">'+params.buttons[i].name+'</a>').click(params.buttons[i].cbk));
            }
        }else{
            $('#messageModal a.btn-primary').html('OK').click(function() {
                $('#messageModal').modal('hide');
            });
        }
        $('#messageModal a.btn-other').hide();
        $('#messageModal').modal();
    };
    jagg.message = function(params){
        if(params.type == "custom"){
            jagg.messageDisplay(params);
            return;
        }
        params.content = '<img src="'+siteRoot+'/images/'+params.type+'.png" align="center" hspace="10" /><span class="messageText">'+params.content+'</span>';
        jagg.messageDisplay({content:params.content,title:"API Publisher - "+params.type,buttons:[
            {name:"OK",cssClass:"btn btn-primary",cbk:function() {
                $('#messageModal').modal('hide');
                if(params.cbk && typeof params.cbk == "function")
	                    params.cbk();
            }}
        ]
        });
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