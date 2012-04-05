var renderServices = function(result) {
    $("#service-info").hide();
    $("#purchased-service-list").hide();
    $("#applications-list").hide();
    $("#subscriptionList").hide();
    var serviceList = $("#service-list");
    var tw = $(".template-wrapper", serviceList);
    var ew = $(".element-wrapper", serviceList).empty();
    if (result.error == "true") {
        return;
    }
    var services = result.data.apis;
    var tl = $(".tmpl-service", tw);
    var tc = $(".tmpl-clear", tw);
    var ts = $(".tmpl-star", tw);
    for (var i = 0; i < services.length; i++) {
        var service = services[i];
        var clone = tl.clone();
        ew.append(clone);
        //$(".service-name", clone).text(service.name).attr("href", "javascript:loadService(\"" + service.path + )
        $(".service-name", clone).text(service.name).data("api", service).click(function() {
            var name = $(this).data("api").name;
            var version = $(this).data("api").version;
            var provider = $(this).data("api").provider;
            activePage = "serviceInfo";
            ServiceStoreApp.currentApiProvider = provider;
            ServiceStoreApp.currentApiName = name;
            ServiceStoreApp.currentApiVersion = version;
            loadService();
        });
        $(".thumbnaillink", clone).data("api", service ).click(function() {
        	 var name = $(this).data("api").name;
             var version = $(this).data("api").version;
             var provider = $(this).data("api").provider;
             activePage = "serviceInfo";
             ServiceStoreApp.currentApiProvider = provider;
             ServiceStoreApp.currentApiName = name;
             ServiceStoreApp.currentApiVersion = version;
             loadService();
        });
        $(".company-name", clone).text(service.provider);
         if(service.thumbURL==""){
            service.thumbURL = 'images/service-default.png';
         }
        $(".thumbnail", clone).attr("src", service.thumbURL);
        var divContent=createDivContent(service.description);
        var textWithoutHtml=removeHtmlFromText(divContent);
        $(".thumbnail", clone).attr("title", textWithoutHtml);

        var rating = service.rates;
        var stars = Math.floor(rating);
        var nonstars = 5 - (stars + 1);
        nonstars = nonstars < 0 ? 0 : nonstars;
        var fraction = rating - stars;
        var ratingEl = $(".rating", clone).empty();
        for(var j = 0; j < stars; j++) {
            ratingEl.append(ts.clone().attr("src", "images/star-1.0.png"));
        }
        if(stars < 5) {
            if(fraction >= 0.75) ratingEl.append(ts.clone().attr("src", "images/star-0.75.png"));
            else if(fraction >= 0.5) ratingEl.append(ts.clone().attr("src", "images/star-0.5.png"));
            else if(fraction >= 0.25) ratingEl.append(ts.clone().attr("src", "images/star-0.25.png"));
            else ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            for(j = 0; j < nonstars; j++) {
                ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            }
        }
    }
    hideMainLoading();
    serviceList.show();
};

var renderPurchasedServices = function(result) {
    $("#service-info").hide();
    $("#service-list").hide();
    $("#applications-list").hide();
    $("#subscriptionList").hide();
    var serviceList = $("#purchased-service-list");
    var tw = $(".template-wrapper", serviceList);
    var ew = $(".element-wrapper", serviceList).empty();
    if (result.error == "true") {
        return;
    }
    var services = result.data.services;
    var tl = $(".tmpl-service", tw);
    var tc = $(".tmpl-clear", tw);
    var ts = $(".tmpl-star", tw);
    for (var i = 0; i < services.length; i++) {
        var service = services[i];
        var clone = tl.clone();
        ew.append(clone);
        //$(".service-name", clone).text(service.name).attr("href", "javascript:loadService(\"" + service.path + )
        $(".service-name", clone).text(service.name).data("href", service.path).click(function() {
            var path = $(this).data("href");
            activePage = "serviceInfo";
            ServiceStoreApp.currentPath = path;
            loadPurchasedService(path);
        });
        $(".thumbnaillink", clone).data("href", service.path).click(function() {
            var path = $(this).data("href");
            activePage = "serviceInfo";
            ServiceStoreApp.currentPath = path;
            loadPurchasedService(path);
        });
        $(".company-name", clone).text(service.author);
         if(service.thumbURL==""){
            service.thumbURL = 'images/service-default.png';
         }
        $(".thumbnail", clone).attr("src", service.thumbURL);
        var divContent=createDivContent(service.description);
        var textWithoutHtml=removeHtmlFromText(divContent);
        $(".thumbnail", clone).attr("title", textWithoutHtml);
        $(".buy-button", clone).data({
            provider : service.provider,
            name : service.name,
            version : service.version
        }).unbind("click").click(subscribe);

        var rating = service.rating;
        var stars = Math.floor(rating);
        var nonstars = 5 - (stars + 1);
        nonstars = nonstars < 0 ? 0 : nonstars;
        var fraction = rating - stars;
        var ratingEl = $(".rating", clone).empty();
        for(var j = 0; j < stars; j++) {
            ratingEl.append(ts.clone().attr("src", "images/star-1.0.png"));
        }
        if(stars < 5) {
            if(fraction >= 0.75) ratingEl.append(ts.clone().attr("src", "images/star-0.75.png"));
            else if(fraction >= 0.5) ratingEl.append(ts.clone().attr("src", "images/star-0.5.png"));
            else if(fraction >= 0.25) ratingEl.append(ts.clone().attr("src", "images/star-0.25.png"));
            else ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            for(j = 0; j < nonstars; j++) {
                ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            }
        }
    }
    hideMainLoading();
    serviceList.show();

};

var renderApplications = function(result) {
    console.log("length "+result.data.application.length);
	$("#service-info").hide();
	$("#service-list").hide();
    $("#purchased-service-list").hide();
    $("#subscriptionList").hide();
    var applicationList = $("#application-list");
    var tw = $(".template-wrapper", applicationList);
    var ew = $(".element-wrapper", applicationList).empty();
    if (result.error == "true") {
        return;
    }
    var services = result.data.application;
    var tl = $(".tmpl-application", tw);
    var tc = $(".tmpl-clear", tw);
    var ts = $(".tmpl-star", tw);
    for (var i = 0; i < services.length; i++) {
        var service = services[i];
        var clone = tl.clone();
        ew.append(clone);
        //$(".service-name", clone).text(service.name).attr("href", "javascript:loadService(\"" + service.path + )
        $(".application-name", clone).text(service.name).data("href", service.name).click(function() {
            var path = $(this).data("href");
            activePage = "applicationsList";
            ServiceStoreApp.currentPath = path;
            loadService();
        });
       /* $(".thumbnaillink", clone).data("href", service.path).click(function() {
            var path = $(this).data("href");
            activePage = "serviceInfo";
            ServiceStoreApp.currentPath = path;
            loadService(path);
        });
        */
        $(".company-name", clone).text(service.author);
         if(service.thumbURL==""){
            service.thumbURL = 'images/service-default.png';
         }
        $(".thumbnail", clone).attr("src", service.thumbURL);

/*
        $(".buy-button", clone).text(service.purchased == "true" ? "Uninstall" : "Install").data({
            path : service.path,
            name : service.name
        }).unbind("click").click(service.purchased == "true" ? uninstall : install);
*/
        var rating = service.rating;
        var stars = Math.floor(rating);
        var nonstars = 5 - (stars + 1);
        nonstars = nonstars < 0 ? 0 : nonstars;
        var fraction = rating - stars;
        var ratingEl = $(".rating", clone).empty();
        for(var j = 0; j < stars; j++) {
            ratingEl.append(ts.clone().attr("src", "images/star-1.0.png"));
        }
        if(stars < 5) {
            if(fraction >= 0.75) ratingEl.append(ts.clone().attr("src", "images/star-0.75.png"));
            else if(fraction >= 0.5) ratingEl.append(ts.clone().attr("src", "images/star-0.5.png"));
            else if(fraction >= 0.25) ratingEl.append(ts.clone().attr("src", "images/star-0.25.png"));
            else ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            for(j = 0; j < nonstars; j++) {
                ratingEl.append(ts.clone().attr("src", "images/star-0.png"));
            }
        }
    }
    hideMainLoading();
    applicationList.show();
};

var renderTopRated = function(result) {
    var tw = $("#top-rated .template-wrapper");
    var ew = $("#top-rated .element-wrapper").empty();
    if (result.error == "true") {
        return;
    }
    var services = result.data.topRated;
    var tl = $(".tmpl-list", tw);
    var tc = $(".tmpl-clear", tw);
    var ts = $(".tmpl-separator", tw);
    for (var i = 0; i < services.length; i++) {
        var service = services[i];
        var clone = tl.clone();
        ew.append(clone);
        $(".list-items-item-name", clone).text(service.name).data("api", service).click(function() {
             console.log("renderTopRated Click");
        	 var name = $(this).data("api").name;
        	 var version = $(this).data("api").version;
        	 var provider = $(this).data("api").author;
        	 activePage = "serviceInfo";
        	 ServiceStoreApp.currentApiProvider = provider;
        	 ServiceStoreApp.currentApiName = name;
        	 ServiceStoreApp.currentApiVersion = version;
        	 loadService();
        });
        $(".list-items-item-detail", clone).text(service.author);
        $(".list-items-number", clone).text(i + 1);
        ew.append(tc.clone());
        if (i != (services.length - 1)) ew.append(ts.clone());
    }
};

var renderRecentlyAdded = function(result) {
    var tw = $("#recently-added .template-wrapper");
    var ew = $("#recently-added .element-wrapper").empty();
    if (result.error == "true") {
        return;
    }
    var services = result.data.recentlyAdded;
    var tl = $(".tmpl-list", tw);
    var tc = $(".tmpl-clear", tw);
    var ts = $(".tmpl-separator", tw);
    for (var i = 0; i < services.length; i++) {
        var service = services[i];
        var clone = tl.clone();
        ew.append(clone);
        $(".list-items-item-name", clone).text(service.name).data("api", service).click(function() {
         var name = $(this).data("api").name;
       	 var version = $(this).data("api").version;
       	 var provider = $(this).data("api").author;
       	 activePage = "serviceInfo";
       	 ServiceStoreApp.currentApiProvider = provider;
       	 ServiceStoreApp.currentApiName = name;
       	 ServiceStoreApp.currentApiVersion = version;
       	 loadService();
        });
        $(".list-items-item-detail", clone).text(service.author);
        ew.append(tc.clone());
        if (i != (services.length - 1)) ew.append(ts.clone());
    }
};

var renderTagCloud = function(result) {
    if (result.error == "true") {
        return;
    }
    var tags = result.data.tagCloud;


    var tagCloud = $('#tag-cloud');
    var maxPercent = 150, minPercent = 100;
    var max = 1, min = 999, count = 0;
    for(var i=0;i<tags.length;i++){
        count = parseInt(tags[i].count);
        max = (count > max ? count : max);
        min = (min > count ? count : min);
    }

    var total, link, size;
    var multiplier = (maxPercent - minPercent) / (max - min);

    for(var i=0;i<tags.length;i++){
        count = parseInt(tags[i].count);
        size = minPercent + ((max - (max - (count - min))) * multiplier) + '%';
        tagCloud.append($('<a class="cloud_link" style="font-size:' + size + '">' + tags[i].name + '</a> ').click(
             function() {
                 var tag = $(this).text();
                 loadTag(tag);
             }
        ));
    }

};

var renderServiceInfo = function(result) {
	  loadDocs();
	  loadReviews();
	  loadComments();
	  loadSamples();
    $("#service-list").hide();
    $("#application-list").hide();
    $("#purchased-service-list").hide();
    $("#subscriptionList").hide();
    if (result.error == "true") {
        return;
    }
    var description = result.data.description;
    var serviceInfo = $("#service-info");
    var serviceDoc = $("#documentation");
    var serviceMoreInfor = $("#moreInformation");
    var overview = $(".tmpl-overview", serviceInfo);
    var documentation = $(".tmpl-documentation", serviceDoc);
    var moreInformation = $(".tmpl-moreInformation", serviceMoreInfor);
    $("#version", overview).text(description.version);
    $("#provider", overview).text(description.author);
    $("#providerLink", overview).text(description.namespace);
    if(description.thumbURL==""){
        description.thumbURL = 'images/service-default.png';
    }
    $("#thumbnail", overview).attr("src", description.thumbURL);
    $("#serviceName", overview).text(description.name);
    $(".buy-button", overview).data({
        provider : description.author,
        name : description.name,
        version : description.version
    }).unbind("click").click(subscribe);

    //$("#detailViewTabs a[name=subscriptions]").unbind("click", getSubscriptions).click(getSubscriptions);

    if(ServiceStoreApp.loggedUser == defaultUser) {
        //$(".key-genrate-button").hide();
        //$("#api-key-wrapper").hide();
        $("#detailViewTabs a[name=subscriptions]").hide();
        $(".apps-wrapper").hide();
    } else {
        loadSubscriptions();
        $("#detailViewTabs a[name=subscriptions]").show();
        //TODO : get subscriptions..
        $(".apps-wrapper").show();
    }

    $("#description", overview).html(createDivContent(description.description));
    $("#supportForumURL", overview).text(description.supportForumURL);
    $("#supportForumURLLink", overview).attr("href",description.supportForumURL);
    $("#breadcrumb", overview).text("Services" + description.name);
    $("#titleMain", overview).text(description.name);

    //loading data to documenation tab
    $("#serviceName", documentation).text(description.name);
    $("#provider", documentation).text(description.author);
    //loading data to documenation tab
    $("#wsdl", moreInformation).text(description.wsdlURL);
    $("#wsdlversion", moreInformation).text(description.version);
    //$("#ratingValueDisplay", overview).text(service.averageRating);

    renderRatings({
        myRating:description.rating,
        averageRating:description.averageRating
    });

    hideMainLoading();
    serviceInfo.show();
};

var loadSubscriptions = function() {
    ServiceStoreApp.call(
            "action=getSubscriptions&" +
                    "providerName=" + ServiceStoreApp.currentApiProvider + "&" +
                    "apiName=" + ServiceStoreApp.currentApiName + "&" +
                    "version=" + ServiceStoreApp.currentApiVersion, function(result) {
        if (result.error == "true") {
            alert("Error loading subscriptions.");
        } else {
            var subs = result.data.subscriptions;
            ServiceStoreApp.call(
            "action=getApplications", function(result) {

                if(result.error == "true") {
                    alert("Error loading applications");
                } else {
                    var apps = result.data.applications;
                    var tbody = $("#subscription-listing").empty();
                    var el = $("#app-selection").empty();
                    var keyHtml = "", appHtml = "<option value=\"\">--Select--</option>";
                    for (var i = 0; i < subs.length; i++) {
                        var sub = subs[i];
                        keyHtml += "<tr><td><div class=\"app-name\">" + sub.application + "</div></td><td>";
                        if($.isEmptyObject(sub.key)) {
                            keyHtml += "<button class=\"generate-key\">Generate</button>";
                        } else {
                            keyHtml += "<div class=\"api-key\">" + sub.key + "</div>";
                        }
                        keyHtml += "</td></tr>";
                    }
                    var findApp = function(name, subs) {
                        for(var i = 0; i < subs.length; i++) {
                            if(subs[i].application == name) {
                                return true;
                            }
                        }
                        return false;
                    };
                    for(i = 0; i < apps.length; i++) {
                        var app = apps[i];
                        var subscribed = findApp(app.name, subs);
                        if(!subscribed) {
                            appHtml += "<option value=\"" + app.id + "\">" + app.name + "</option>";
                        }
                    }
                    el.html(appHtml);
                    tbody.html(keyHtml);
                    $("#subscription-listing .generate-key").click(function() {
                        var application = $(this).parent().parent().find(".app-name").text();
                        var that = this;
                        ServiceStoreApp.call(
                                "action=getKey&" +
                                        "providerName=" + ServiceStoreApp.currentApiProvider + "&" +
                                        "apiName=" + ServiceStoreApp.currentApiName + "&" +
                                        "version=" + ServiceStoreApp.currentApiVersion + "&" +
                                        "application=" + application, function(result) {
                            if (result.error == "true") {
                                ServiceStoreApp.showLogin();
                            } else {
                                $(that).parent().html("<div class=\"api-key\">" + result.data.key + "</div>");
                            }
                        });
                    });
                }
            });
        }
    });
};

/*
var renderRatings = function(ratingData){
    //Reset previous event registrations
	console.log("renderRatings" );
    $('#ratingStars a').each(function(index){
        $(this).unbind();
    });
    $('#ratingStars').unbind();
    $('#ratingStars').unbind();
    $('#myRatingRemove').unbind();

    $('#ratingStars a').each(function(index){
        $(this).mouseover(
                     function() {
                    	 console.log("ratingStars mouseover" );
                         var mouseOverItemIndex = $('#ratingStars a').index(this);
                         $('#myRatingTemp').html(index+1);
                         setStartsFromRating(mouseOverItemIndex);
                     }
                );

        $(this).click(function() {
        	console.log("ratingStars click");
            setMyRating($('#ratingStars a').index(this)+1);
        });
    });
    $('#ratingStars').mouseleave(function(){
        setMyRating(ratingData);
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
	console.log("setStartsFromRating " +rating);
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
	console.log('getRatingData');
    return {myRating:0,averageRating:4.5};
};
//Here set the rating
var setMyRating = function(rating){
		console.log('setMyRate');
        loadRatings(ServiceStoreApp.currentApiName,rating);
};
//Here remove the rating
var removeMyRating = function(){
        loadRatings(ServiceStoreApp.currentApiName,0);
};*/
var loadPurchases = function() {
    APIStoreApp.call('action=getPurchases&path=' + ServiceStoreApp.currentPath, function(result) {
        if (result.error == "true") {
            ServiceStoreApp.showLogin();
        }
        renderPurchasedServices(result);

    });

};
//Here load UI with ratings
/*var loadRatings = function(path, rating) {
	//http://10.200.3.134:9763/apistore/services/registry.jag?action=rateAPI&providerName=admin&apiName=kk&version=2.8.0&rate=5
	console.log('rating ' +rating);
    ServiceStoreApp.call('action=rateAPI&providerName=' + ServiceStoreApp.currentApiProvider + '&apiName=' + ServiceStoreApp.currentApiName + '&version=' + ServiceStoreApp.currentApiVersion + '&rate=' + rating, function(result) {
        if (result.error == "true") {
            return;
        }
        var data = result.data;

        renderRatings({
                          myRating:data.rating,
                          averageRating:data.averageRating
                      });

    });


};*/
var loadServices = function() {
    showMainLoading();
    ServiceStoreApp.call("action=getAllPublishedAPIs", function(result) {
        renderServices(result);
        hideMainLoading();
    });
};

var loadService = function() {
    showMainLoading();
    ServiceStoreApp.call('action=getAPIDescription&apiName=' + ServiceStoreApp.currentApiName +
            '&version=' + ServiceStoreApp.currentApiVersion +
            '&providerName=' + ServiceStoreApp.currentApiProvider, renderServiceInfo);
    loadTabs("overview");
};

var loadPurchasedService = function(path) {
    ServiceStoreApp.currentPath = path;
    showMainLoading();
    ServiceStoreApp.call('action=getDescription&path=' + path, renderServiceInfo);
   showCommentLoading();
    loadComments();
    loadReviews();
    loadDocs();
    loadSamples();
    loadTabs("purchased");
    //load tryit
    /*   var tryitUrl = "tryit/tryit.jag?path=" + path;
    $("#tryit-frame").attr("src", tryitUrl);
    $("#tryit-external-link").attr("href", tryitUrl);*/
};


var loadTag = function(tag) {
    showMainLoading();
    activePage = "listTag";
    activeTag = tag;
    ServiceStoreApp.call("action=getAPIsWithTag&tag=" + tag, renderServices);
};

var loadReviews = function() {
	//http://10.200.3.134:9763/apistore/services/registry.jag?action=getAPIComments&providerName=admin&apiName=xx&version=1.0.0
	
    ServiceStoreApp.call("action=getAPIComments&providerName="+ServiceStoreApp.currentApiProvider +"&apiName="+ServiceStoreApp.currentApiName+"&version="+ServiceStoreApp.currentApiVersion, function(json) {
        if (json.error == "true") {

        } else {
            var reviews = json.data.comments;
            var commentsDiv = $(".comment-list").empty();
            for (var i = 0; i < reviews.length; i++) {
                var review = reviews[i];
                commentsDiv.append(getCommentHtml(review));
            }
            hideCommentLoading();
        }
    });
};

var loadDocs = function() {
	console.log ("loadDocs url "+"action=getAllDocumentation&providerName="+ServiceStoreApp.currentApiProvider +"&apiName="+ServiceStoreApp.currentApiName+"&version="+ServiceStoreApp.currentApiVersion);
	//http://10.200.3.134:9763/apistore/services/registry.jag?action=getAllDocumentation&providerName=admin&apiName=xx&version=1.0.0
    ServiceStoreApp.call("action=getAllDocumentation&providerName="+ServiceStoreApp.currentApiProvider +"&apiName="+ServiceStoreApp.currentApiName+"&version="+ServiceStoreApp.currentApiVersion, function(json) {
var isURLexist=false;
    	if (json.error == "true") {
        } else {
            var docs = json.data.doc;
            var docsDiv = $(".horizontal-docs-title").empty();
            var forumURLDiv = $("#supportForumURL").empty();           
            $("#supportForumURLLabel").empty();
            for (var i = 0; i < docs.length; i++) {
            	
                var doc = docs[i];                
                docsDiv.append(getDocsHtml(doc));
                if(docs[i].doctype == 'Public Forum'){
                	
                	isURLexist = true;
                	$("#supportForumURLList").show();
                	
                	forumURLDiv.append(loadLinks(doc));
                	
                }
            }
            if(!isURLexist){
            	$("#supportForumURLList").hide();
            }

        }
    });
};


var loadSamples = function() {

	console.log ("loadSamples url "+"action=getAllSample&providerName="+ServiceStoreApp.currentApiProvider +"&apiName="+ServiceStoreApp.currentApiName+"&version="+ServiceStoreApp.currentApiVersion);
	//http://10.200.3.134:9763/apistore/services/registry.jag?action=getAllDocumentation&providerName=admin&apiName=xx&version=1.0.0
    ServiceStoreApp.call("action=getAllSample&providerName="+ServiceStoreApp.currentApiProvider +"&apiName="+ServiceStoreApp.currentApiName+"&version="+ServiceStoreApp.currentApiVersion, function(json) {

        if (json.error == "true") {
        } else {
            var samples = json.data.doc;

            var samplesDiv = $(".horizontal-sample-title").empty();
            for (var i = 0; i < samples.length; i++) {

                var sample = samples[i];
                samplesDiv.append(getSamplesHtml(sample));
            }

        }
    });
};
var subscribe = function() {
    var data = $(this).data();
    var that = this;
    if(ServiceStoreApp.loggedUser == defaultUser) {
        ServiceStoreApp.showLogin();
        return;
    }
    var applicationId = $("#app-selection").val();
    if(applicationId == "") {
        alert("Please select an application before subscribing");
        return;
    }
    if(!applicationId || applicationId == "") return;
    ServiceStoreApp.call(
            "action=addSubscription&" +
                    "providerName=" + data.provider + "&" +
                    "apiName=" + data.name + "&" +
                    "version=" + data.version + "&" +
                    "applicationId=" + applicationId, function(result) {
        if (result.error == "true") {
            ServiceStoreApp.showLogin();
        } else {
            loadSubscriptions();
            if(result.data.subscribed == "true") {
                alert("Subscription is successful. Please go to My Subscriptions tab to generate keys.");
                changeTab($('#detailViewTabs a[name=subscriptions]'));
            } else {
                alert("Error while subscribing");
            }
            //viewTab("subscriptions");
        }
    });
};

var unsubscribe = function() {
    var data = $(this).data();
    var that = this;
    var application = prompt("Please enter your application name");
    ServiceStoreApp.call(
            "action=removeSubscriber&" +
                    "providerName=" + data.provider + "&" +
                    "apiName=" + data.name + "&" +
                    "version=" + data.version + "&" +
                    "application=" + application, function(result) {
        if (result.error == "true") {
            ServiceStoreApp.showLogin();
        } else {
            $(that).text("Subscribe").unbind("click").click(subscribe);
            $(".key-genrate-button").hide();
        }
    });
};

var refreshContent = function() {
    if(activePage == "servicesList") {
        loadServices();
    } else if(activePage == "serviceInfo") {
        loadService(ServiceStoreApp.currentPath);
    } else if(activePage == "listTag") {
        loadTag(activeTag);
    }else if(activePage == "applicationsList") {
     //   loadTag(activeTag);
    }
};

var showMainLoading = function() {
    $("#middle-container").hide();
    $("#main-ajax-loader").show();
};

var hideMainLoading = function() {
    $("#middle-container").fadeIn(800);
    $("#main-ajax-loader").hide();
};

var showCommentLoading = function() {
    $(".comment-list").hide();
    $("#comment-ajax-loader").show();
};

var hideCommentLoading = function() {
    $(".comment-list").fadeIn(800);
    $("#comment-ajax-loader").hide();
};

var createDivContent = function(text) {
    var divContent;
    //Create a temporary <Div> to strip out HTML from text.
    var tmp = document.createElement("DIV");
    tmp.innerHTML = text;
    //Retrieve either textContent or innerText of <Div> based on browser
    return divContent = tmp.textContent || tmp.innerText;

};
var removeHtmlFromText = function(textWithHtml) {
    var textWithoutHtml;
    if (textWithHtml != undefined) {
        textWithHtml = textWithHtml.replace(/&(lt|gt);/g, function (strMatch, p1) {
            return (p1 == "lt") ? "<" : ">";
        });
        textWithoutHtml = textWithHtml.replace(/<\/?[^>]+(>|$)/g, "");
        return textWithoutHtml;
    } else {
        return textWithHtml;
    }
};
ServiceStoreAppSearchBar = new function () {
    this.initSearchAutoComplete = function (result) {
        var source = [];//["Advertising","Answers","Auctions","Blogging","Calendar","Database","Job Search","Messaging","Music"];
        if(result.data.apis != undefined){
            for(var i=0;i<result.data.apis.length;i++){
                source.push(result.data.apis[i].name.toString());
            }
        }
        //source = ["Advertising","Answers","Auctions","Blogging","Calendar","Database","Job Search","Messaging","Music"];
        $("#searchMain").autocomplete({
            minLength:0,
            source:source});

        $('#searchForm').submit(function(e){
            ServiceStoreApp.searchServiceByName();
            return e.preventDefault();
        });
    };
    this.initCategories = function(){
        var categories = ["All","Services","Projects"];
        var theUl = document.createElement('ul');
        var customComboText =  $('#customComboText').html();
        for(var i=0;i<categories.length;i++){
            var theLi = document.createElement('li');
            $(theLi).click(
                          function() {
                              $('#customComboText').html($(this).html());
                              customComboText = $(this).html();
                              $('#customComboOptions').toggle("drop");
                          }
                    );
            $(theLi).mouseover(
                          function() {
                              $('#customComboText').html($(this).html());
                          }
                    );
            $(theLi).mouseout(
                          function() {
                              $('#customComboText').html(customComboText);
                          }
                    );
            theLi.innerHTML = categories[i];
            theUl.appendChild(theLi);
        }
        $('#customComboOptions').append(theUl);
        $('#customComboOptions').mouseleave(function(){
            $('#customComboOptions').hide("drop");
        });
        $('#customComboText').click(function(){
            $('#customComboOptions').toggle("drop");
        });
        $('#customComboBtn').click(function(){
            $('#customComboOptions').toggle("drop");
        });
    };


};

$("#commentBoxBtn").click(function() {
	console.log("commentBoxBtn Click");});

var generateKey = function(provider, api, version, application, callback) {
    ServiceStoreApp.call(
            "action=getKey&" +
                    "providerName=" + provider + "&" +
                    "apiName=" + api + "&" +
                    "version=" + version + "&" +
                    "application=" + application, function(result) {
        if (result.error == "true") {
            ServiceStoreApp.showLogin();
        } else {
            callback(result.data.key);
        }
    });
};

var loadApplicationUI = function() {

    if (ServiceStoreApp.loggedUser == defaultUser) {
        ServiceStoreApp.showLogin();
        return;
    }

    $("#service-info").hide();
    $("#purchased-service-list").hide();
    $("#application-list").hide();
    $("#service-list").hide();
    $("#subscriptionList").hide();

    var appDiv = $("#applications-list");
    showMainLoading();
    if (appDiv.length > 0) {
        loadApplications();
        appDiv.show();
        return;
    }
    ServiceStoreAppUtil.makeRequest("applications.jag", null, function(data) {
        $("#middle-container").append(data);
        $("#applications-list").show();
        $('#application-name').focus();
        $('#titleMain').html('My Applications');
        $("#save-application").click(function() {
            var app = $("#application-name");
            ServiceStoreApp.call("action=addApplication&name=" + app.val(), function(result) {
                if (result.error == "true") {
                    alert(result.error);
                } else {
                    loadApplications();
                }
            });
        });
        loadApplications();
    });
};

var loadApplications = function() {
    ServiceStoreApp.call("action=getApplications", function(result) {
        hideMainLoading();
        if (result.error == "true") {
            alert(result.error);
            return;
        }
        var apps = result.data.applications;
        var html = "";
        for (var i = 0; i < apps.length; i++) {
            html += "<tr><td>" + apps[i].name + "</td></tr>";
        }
        $("#application-list").empty().html(html).show();
    });
};

var loadAllSubscriptionsUI = function() {

    if (ServiceStoreApp.loggedUser == defaultUser) {
        ServiceStoreApp.showLogin();
        return;
    }

    $("#service-info").hide();
    $("#purchased-service-list").hide();
    $("#applications-list").hide();
    $("#service-list").hide();

    var subDiv = $("#subscriptionList");
    showMainLoading();
    if (subDiv.length > 0) {
        loadAllSubscriptions();
        subDiv.show();
        return;
    }
    ServiceStoreAppUtil.makeRequest("subscriptions.jag", null, function(data) {
        $("#middle-container").append(data);
        $("#subscriptionList").show();
        $('#application-name').focus();
        $('#titleMain').html('My Subscriptions');
        /*$("#save-application").click(function() {
            var app = $("#application-name");
            ServiceStoreApp.call("action=addApplication&name=" + app.val(), function(result) {
                if (result.error == "true") {
                    alert(result.error);
                } else {
                    loadApplications();
                }
            });
        });*/
        loadAllSubscriptions();
    });
};

var loadAllSubscriptions = function() {
    ServiceStoreApp.call("action=getAllSubscriptions", function(result) {
        hideMainLoading();
        if (result.error == "true") {
            alert(result.error);
            return;
        }
        console.log(result);
        var apps = result.data.applications;
        var tmplWrapper = $("#subscriptionList .template-wrapper");
        var accordion = $(".tmpl-subscription", tmplWrapper);
        var apiEl = $(".tmpl-subscription-list", accordion);
        var subsList = $("#subscriptionList .element-wrapper").empty();
        for (var i = 0; i < apps.length; i++) {
            var app = apps[i];
            accordion.clone();
            var clone0 = accordion.clone();
            $(".accordion-toggle", clone0).text(app.name);

            var innerDiv = $(".accordion-inner", clone0).empty();
            for(var j = 0; j < app.subscriptions.length; j++) {
                var sub = app.subscriptions[j];
                var clone1 = apiEl.clone();
                $("img", clone1).attr("src", sub.thumburl);
                $(".service-name", clone1).text(sub.name);
                $(".company-name", clone1).text(sub.provider);
                clone1.click(function() {
                    var api = $(this).data("api");
                    activePage = "serviceInfo";
                    ServiceStoreApp.currentApiProvider = api.provider;
                    ServiceStoreApp.currentApiName = api.name;
                    ServiceStoreApp.currentApiVersion = api.version;
                    loadService();
                    viewTab("subscriptions");
                }).data("api", sub);
                innerDiv.append(clone1);
            }
            subsList.append(clone0.show());
        }
        setSubscriptionUIEvents();
        subsList.show();
        $("#subscriptionList").show();
    });
};

var setSubscriptionUIEvents = function () {
    $('#subscriptionList .element-wrapper div.accordion-body').each(
            function (index) {
                if (index == 0) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            }
    );
    $('#subscriptionList .element-wrapper a.accordion-toggle').click(
            function () {
                $(this).parent().next().toggle('blind');
            }
    );
};

