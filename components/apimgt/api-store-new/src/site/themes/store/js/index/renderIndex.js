var changeTab = function(that) {
    $('#detailViewTabs a').removeClass('selected');
    $('#detailViewTabs a').each(function() {
        $('#' + $(this).attr('name')).hide();
    });
    $(that).addClass('selected');
    $('#' + $(that).attr('name')).show();
};

var viewTab = function(name) {
    changeTab($("#detailViewTabs > a[name=" + name + "]"));
};

var loadTabs = function(defaultTabId){
    $('#detailViewTabs a').each(function(){
        if($(this).attr('name') == defaultTabId){
            $('#'+$(this).attr('name')).show();
            $(this).addClass('selected');
        }else{
            $('#'+$(this).attr('name')).hide();
            $(this).removeClass('selected');
        }
    });
    $('#detailViewTabs a').click(function() {
        changeTab(this);
    });
};

var renderRatings = function(ratingData){
	
    //Reset previous event registrations
    $('#ratingStars a').each(function(index){
        $(this).unbind();
    });
    $('#ratingStars').unbind();
    $('#ratingStars').unbind();
    $('#myRatingRemove').unbind();


    $('#ratingStars a').each(function(index){
        $(this).mouseover(
                     function() {
                         var mouseOverItemIndex = $('#ratingStars a').index(this);
                         $('#myRatingTemp').html(index+1);
                         setStartsFromRating(mouseOverItemIndex);
                     }
                );

        $(this).click(function() {
            setMyRating($('#ratingStars a').index(this)+1);
        });
    });
    $('#ratingStars').mouseleave(function(){
       // setMyRating(ratingData);
    });
    $('#myRatingRemove').click(function(){
        removeMyRating();
    });

    $('#ratingValueDisplay').html(ratingData.averageRating);
    $('#ratingValueDisplay-small').html(ratingData.averageRating);
    setStartsFromRating(ratingData.myRating-1);
    $('#myRatingTemp').html(ratingData.myRating);
    if(ratingData.averageRating == 0){
        $('#ratingValueDisplay').hide();
        $('#ratingDetails-secondRow').hide();
        $('#ratingDetails-thirdRow').show();
    }
    if(ratingData.myRating == 0){
        $('#myRatingRemove').hide();
    }else{
        $('#myRatingRemove').show();
    }
};
var setStartsFromRating = function(rating) {
    $('#ratingStars a').each(function(index) {
        if (index <= rating) {
            this.className = "star-1";
        } else {
            this.className = "star-0";
        }
    });
};
//Get rating information from the back end.
var getRatingData = function(){

    return {myRating:0,averageRating:4.5};
};
//Here set the rating
var setMyRating = function(rating){
	var xx= rating;
    //loadRatings(ServiceStoreApp.currentPath,rating);
	 ServiceStoreApp.call('action=rateAPI&providerName=' + ServiceStoreApp.currentApiProvider + '&apiName=' + ServiceStoreApp.currentApiName + '&version=' + ServiceStoreApp.currentApiVersion + '&rate=' + rating, function(result) {
		   
        if (result.error == "true") {
            alert(result.message);
        } var data = result.data;
         

    });
};
//Here remove the rating
var removeMyRating = function(){
        loadRatings(ServiceStoreApp.currentPath,0);
};

var getCommentHtml = function(review) {
   /* var time = new Date().getTime() - review.time;
    var milMin = 1000 * 60;
    var milHour = milMin * 60;
    var milDay = 24 * milHour;
    var days = Math.floor(time / milDay);
    var dayT = time - (days * milDay);
    var hours = Math.floor(dayT / milHour);
    var minutes = Math.floor((dayT - (hours * milHour)) / milMin);
    var timeText = ((days == 0) ? "" : days + " days, ") + hours + " hours and " + minutes + "min ago";
   */
	var timeText = '';
	var html =
            '<div class="comment-box">' +
                    '<img src="images/user-pic-default.png">' +
                    '<div class="comment-box-content">' +
                    '<a class="comment-box-user">' + review.userName + '</a>&nbsp;' +
                    '<span>' + review.comment + '</span>' +
                    '</div>' +
                    '<div class="comment-box-date">' + timeText + '</div>' +
                    '<div style="clear:both"></div>' +
                    '</div>';
    return html;
};
var loadComments = function(){
    $("#commentsPagination").paginate({
        count         : 50,
        start         : 1,
        display     : 10,
        border                    : false,
        text_color              : '#888',
        background_color        : '#EEE',
        text_hover_color          : 'black',
        background_hover_color    : '#CFCFCF',
        onChange                 : function(page) {
            loadReviews();
        }
    });

    $("#commentBoxBtn").click(function() {
    	
    	console.log("commentBoxBtn Click");
        var text = $(".comment-textarea").val();
        if (text != "") {
        	//http://10.200.3.134:9763/apistore/services/registry.jag?action=addAPIComment&providerName=admin&apiName=xx&version=1.0.0&comment=dasdsada
            ServiceStoreApp.call('action=addAPIComment' +
            		'&providerName=' + ServiceStoreApp.currentApiProvider + '&apiName=' + ServiceStoreApp.currentApiName + '&version=' + ServiceStoreApp.currentApiVersion + '&comment=' +  text, function(json) {
                if (json.error == "true") {
                    alert(json.message);
                } else {
                    $(".comment-textarea").val("");
                    loadReviews();
                }
            });
        }
    });

};
var getDocsHtml = function(doc) {
	console.log('sfsf'+doc.sourcetype);
    var title = doc.title;
    var describtion = doc.describtion;
    var sourceUrl = doc.sourceUrl;
    var sourceType = doc.sourcetype;
    if(sourceType == "INLINE"){
    	var html ='<tr><td>'+
        title+'</td><td>'+
        describtion+ '<br>'+    
        '<a href="javascript:ServiceStoreApp.loadDocContent(\''+title+'\',\''+ServiceStoreApp.currentApiName+'\',\''+ServiceStoreApp.currentApiVersion+'\',\''+ServiceStoreApp.currentApiProvider+'\');">View</a>|' +
       
        
    '</td><tr>';
    }else{
    var html ='<tr><td>'+
    title+'</td><td>'+
    describtion+ '<br>'+    
    '<a href="'+sourceUrl+'">'+sourceUrl+'</a>'+
'</td><tr>';
    }
    return html;
};

var getSamplesHtml = function(sample) {
	
    var title = sample.title;
    var describtion = sample.describtion;
    var sourceUrl = sample.sourceUrl;
    if(sourceUrl == "null"){
    var html ='<tr><td>'+
    title+'</td><td>'+
    describtion+ '<br>'+    
'</td><tr>';
	}else{
		var html ='<tr><td>'+
	    title+'</td><td>'+
	    describtion+ '<br>'+    
	    '<a href="'+sourceUrl+'">'+sourceUrl+'</a>'+
	'</td><tr>';	
	}
    return html;
};

var loadLinks = function(doc) {	
    var title = doc.title;
    var describtion = doc.describtion;
    var sourceUrl = doc.sourceUrl;
    if(sourceUrl != "null"){
var html="";
    var htmlmain = "<div id='supportForumURLLabel' class='content-label'>Forum URL:</div>";
        html += "<div id='supportForumURL' class='content-value'><p><b>"+title+": </b><a id='supportForumURLLink'  href='"+sourceUrl+"'>"+sourceUrl+" </a></p></div>";
        $("#supportForumURLLabel").html(htmlmain).show();
        $("#supportForumURL").append(html).show();
    }
};

var getPurchasedServices = function() {
    ServiceStoreApp.call("action=getPurchases&path=" +
                         ServiceStoreApp.currentPath, function(json) {
        if (json.error == "true") {
            alert(json.message);
        } var data = result.data;
         alert(result.data.purchases);

    });

};

$("#purchased").click(function() {
  alert("Handler for .click() called.");
});