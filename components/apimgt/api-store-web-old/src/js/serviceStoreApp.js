var defaultUser = "public";
var ServiceStoreApp = new function () {

    this.url = "/apistore/services/registry.jag";
    this.loggedUser = null;
    this.loggedUserId = null;
    this.currentPath = '';
    this.currentApiProvider = '';
    this.currentApiName = '';
    this.currentApiVersion = '';
    this.currentDocName = '';
    /**
     * main operation to call the external WS
     * @param data
     * @param callback
     */
    this.call = function (data, callback) {
        var path = $("#path").val();
        var htmlResult;
        if (ServiceStoreAppUtil.isUndefined(callback) || callback == null) {
            callback = function (result) {
                console.log(JSON.stringify(result));
            };
        }
        ServiceStoreAppUtil.makeJsonRequest(ServiceStoreApp.url, data, callback);
    };

    //the login operation
    this.login = function () {
        console.log("Login");
        var name = $("#name").val();
        var pass = $("#pass").val();
        var loginCallback = function (result) {
            if (result.error == "false") {
                ServiceStoreApp.loggedUser = name;
                ServiceStoreApp.loggedUserId = result.data.id;
                $("#userName").text(name);
                $('#login-links').hide();
                $('#commentsArea').show();
//                $('#login-box').hide("slow");
                $('#logout-links').show();
                refreshContent();
                console.log("Result : " + JSON.stringify(result));
            }
        };

        ServiceStoreApp.call("action=login&username=" + name + "&password=" + pass, loginCallback);
    };
    //TODO remove public from client side
    //the login public (default login)
    this.loginDefault = function () {
        console.log("Login Default ");
        var name = defaultUser;
        var pass = "public";
        var loginCallback = function (result) {
            ServiceStoreApp.loggedUser = name;
            if (result.error == "false") {
                console.log("Result : " + JSON.stringify(result));
                ServiceStoreApp.showDataToLoggedInUser();
            }
        };

        ServiceStoreApp.call("action=login&username=" + name + "&password=" + pass, loginCallback);
    };

    //Operations for a logged in user
    this.showDataToLoggedInUser = function () {
    	console.log("xxxxxxxxxxxServiceStoreApp.loggedUser"+ServiceStoreApp.loggedUser);
    	if(ServiceStoreApp.loggedUser == defaultUser){
    		$('#logout-links').hide();
            $('#login-links').show();
    	}else{
        $('#logout-links').show();
        $('#login-links').hide();
//        $('#login-box').hide("slow");
    	 }
        ServiceStoreApp.listServices();
        ServiceStoreApp.loadCloud();
        ServiceStoreApp.loadRecentlyAdded();
        ServiceStoreApp.loadTopRated();
        ServiceStoreApp.loadDescribitionServices();
        $('#userName').text(ServiceStoreApp.loggedUser);
        //ServiceStoreApp.getSearchKeys(ServiceStoreAppSearchBar.initSearchAutoComplete);
       
    };

    //logout operation
    this.logout = function () {
        console.log("logout");
        var loginCallback = function (result) {
            if (result.error == "false") {
                ServiceStoreApp.loggedUser = defaultUser;
                $("#userName").text("");
                $('#logout-links').hide();
                $('#login-links').show();
                $('#commentsArea').hide();
                refreshContent();
            }
        };

        ServiceStoreApp.call("action=logout", loginCallback);
    };
    
    //Default User logout operation
    this.Defaultlogout = function () {
        console.log("logout");
        var loginCallback = function (result) {
            if (result.error == "false") {
                ServiceStoreApp.loggedUser = defaultUser;
                //window.location.reload();
            }
        }

        ServiceStoreApp.call("action=logout", loginCallback);
    };
    
    //calling inline content of api documents
    this.loadDocContent = function (docName,apiName,version,apiProvider) {
    	var title = "Doc1";
    	window.open("includes/home/inLineDocument.jag?docName="+docName+"&apiProvider="+this.currentApiProvider+"&apiName="+apiName+"&version="+version);
    };

    //Check if user is logged in
    this.isUserLoggedIn = function () {
        if (ServiceStoreApp.loggedUser == null || ServiceStoreApp.loggedUser == '') {
            var result = ServiceStoreAppUtil.makeSyncRequest(ServiceStoreApp.url, 'action=getUser');
            if (!jQuery.isEmptyObject(result.data.username)) {
                ServiceStoreApp.loggedUser = result.data.username;
                ServiceStoreApp.loggedUserId = result.data.id;
                return true;
            } else {
                return false;
            }

        } else {
            return true;
        }
    };

    //List services
    this.listServices = function () {
        ServiceStoreApp.call('action=getAllPublishedAPIs', ServiceStoreApp.drawServicesList);
    };
    
/*    this.showApplication = function () {
    	var activePage = "applicationsList";
    	//hideMainLoading();
    	refreshContent();
    renderApplications(applications);
    };
    
    this.showService = function () {
    	//hideMainLoading();
    	var activePage = "servicesList";
    	refreshContent();
    	renderServices(services);
    };*/
    
    this.drawServicesList = function (result) {
        if (result.error == "false") {
            var items = "";
            for (var i = 0; i < result.data.services.length; i++) {
                var installText = 'install';
                if (result.data.services[i].purchased == "true") {
                    installText = 'Uninstall';
                }
                var serviceName = result.data.services[i].name;
                var servicePath = result.data.services[i].path;
                var serviceAuthor = result.data.services[i].author;
                var serviceForumURL = result.data.services[i].supportForumURL;

                if (!ServiceStoreAppUtil.isUndefined(serviceName) && serviceName.length >= 15) {
                    serviceName = '<a id="name"  href="serviceInfo.jag?path=' + servicePath + '&name=' + serviceName + '" class="service-name" title="' + serviceName + '">' + serviceName.substring(0, 15) + '..' + '</a>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceName)) {
                    serviceName = '<a id="name" href="serviceInfo.jag?path=' + servicePath + '&name=' + serviceName + '" class="service-name">' + serviceName.substring(0, 15) + '</a>';
                }
                if (!ServiceStoreAppUtil.isUndefined(serviceAuthor) && serviceAuthor.length >= 20) {
                    serviceAuthor = '<div id="path" class="company-name" title="' + serviceAuthor + '">' + serviceAuthor.substring(0, 20) + '..' + '</div>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceAuthor)) {
                    serviceAuthor = '<div id="path" class="company-name">' + serviceAuthor.substring(0, 20) + '</div>';
                }
                if (!ServiceStoreAppUtil.isUndefined(serviceForumURL) && serviceForumURL.length >= 20) {
                    serviceForumURL = '<div id="forum" class="forum" title="' + serviceForumURL + '">' + serviceForumURL.substring(0, 20) + '..' + '</div>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceForumURL)) {
                    serviceForumURL = '<div id="forum" class="forum">' + serviceForumURL.substring(0, 20) + '&nbsp;</div>';
                }
                var serviceThumbView = result.data.services[i].thumbURL;
                if (result.data.services[i].thumbURL == "") {
                    serviceThumbView = 'images/service-default.png';
                }
                items += '<div id="list-item-' + i + '" class="list-item">' +
                    '<img src="' + serviceThumbView + '" />' +
                    serviceName +
                    serviceAuthor +
                    serviceForumURL +
                    '<div id="rating" class="rating">' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '</div>' +
                    '<div style="clear:both"></div>' +
                    '<a id="buy" class="buy-button" onclick="ServiceStoreApp.purchase(' + i + ')">' + installText + '</a>' +
                    '</div>';
            }
            $('#elementList').html(items);
        }
    };

    //Loading the tag cloud
    this.loadCloud = function () {
        var cloudCallback = function (result) {
            if (result.error == "false") {
                var maxValue, minValue, maxSize = 17, minSize = 10;
                var sizeRange = maxSize - minSize;
                //First loop to find the max and min values
                for (var i = 0; i < result.data.tagCloud.length; i++) {
                    if (maxValue == undefined || maxValue < parseInt(result.data.tagCloud[i].count)) {
                        maxValue = parseInt(result.data.tagCloud[i].count);
                    }
                    if (minValue == undefined || minValue > parseInt(result.data.tagCloud[i].count)) {
                        minValue = parseInt(result.data.tagCloud[i].count);
                    }
                }
                var valueRange = maxValue - minValue;
                var cloudContainer = $('.cloud-margin').empty();
                ;
                for (var i = 0; i < result.data.tagCloud.length; i++) {
                    var obj = result.data.tagCloud[i];
                    var value = result.data.tagCloud[i].count;
                    var html = "<a onclick=\"ServiceStoreApp.loadServicesByTag('" + obj.name + "')\">" + obj.name + "</a>";
                    var fontSize = minSize;
                    if (valueRange != 0) {
                        fontSize = parseInt((value / valueRange) * sizeRange + minSize);
                    }
                    $(html).appendTo(cloudContainer).css("font-size", fontSize);
                }
            }
        }

        ServiceStoreApp.call('action=getTagCloud', cloudCallback);

    };

    //Loading services by Tag
    this.loadServicesByTag = function (tag) {
    	//console.log("Location :: "+document.location.href); 
    	var location  = ""+document.location.href;
    		if(location.charAt(location.length-1)=="/"){
        ServiceStoreApp.call("action=searchServiceByTag&tag=" + tag, ServiceStoreApp.drawServicesList);
    		}else{
    			window.location = "index.jag";
    			window.location.reload();
    			   ServiceStoreApp.call("action=searchServiceByTag&tag=" + tag, ServiceStoreApp.drawServicesList);
    			    
    		}
    		};

    //Load recently added services
    this.loadRecentlyAdded = function () {
        console.log("xxgetRecentlyAddedServices");
        var recentCallback = function (result) {
            if (result.error == "false") {
                var items = "";
                console.log("getRecentlyAddedServices");
                for (var i = 0; i < result.data.recentlyAdded.length; i++) {
                    items += '<div class="list-items">'
                        + '<div class=\"list-items-box\"><img src=\"images/list-item.png\" alt=\"\"></div>'
                        + '<div class="list-items-content">'
                        + '<a class="list-items-item-name" href="serviceInfo.jag?path=' + result.data.recentlyAdded[i].path + '&name=' + result.data.recentlyAdded[i].name + '">' + result.data.recentlyAdded[i].name + '</a>'
                        + '<a class="list-items-item-detail">' + result.data.recentlyAdded[i].author + '</a>'
                        + '</div>'
                        + '</div><div style="clear:both"></div>'
                        + ' <hr class=\"line-seperator\"/>';
                    console.log("getRecentlyAddedServices" + result.data.recentlyAdded[i].name);

                }
                $('#recelementList').html(items);
                console.log("getRecentlyAdded" + items);
            }
        };

        ServiceStoreApp.call('action=getRecentlyAddedServices', recentCallback);
    };

    //Load Describitioin services
    this.loadDescribitionServices = function () {
        console.log("loadDescribitionServices");
        var describeCallback = function (result) {
            if (result.error == "false") {
                var items = "";
                console.log("loadDescribitionServices");
                $('#version').html(result.data.description.versionNumber);
                $('#wsdl').html(result.data.description.parentPath);
                $('#homepage').html(result.data.description.parentPath);
                $('#server').html(result.data.description.mediaType);
                $('#ratingValueDisplay').html(result.data.description.averageRating);
                $('#ratingValueDisplay-small').html(result.data.description.averageRating); 
                $('#rate').html(result.data.description.averageRating);                
                $('#myRatingTemp').html(result.data.description.rating);
                setStartsFromRating(result.data.description.rating);
                console.log("loadDescribitionServices" + result.data.description.lastModified);

            }
        };

        ServiceStoreApp.call('action=getDescription&path=' + ServiceStoreApp.currentPath, describeCallback);
    };

    //Load top rated services
    this.loadTopRated = function () {
        console.log("topelementList");
        var ratedCallback = function (result) {
            if (result.error == "false") {
                var items = "";
                for (var i = 0; i < result.data.topRated.length; i++) {
                    items += '<div class="list-items">'
                        + '<div class="list-items-number">' + (i + 1) + '</div>'
                        + '<div class="list-items-content">'
                        + '<a class="list-items-item-name" href="serviceInfo.jag?path=' + result.data.topRated[i].path + '&name=' + result.data.topRated[i].name + '">' + result.data.topRated[i].name + '</a>'
                        + '<a class="list-items-item-detail">' + result.data.topRated[i].author + '</a>'
                        + '</div>'
                        + '</div><div style="clear:both"></div>'
                        + '<hr class="line-seperator"/>';
                }
                $('#topelementList').html(items);
            }
        };
        ServiceStoreApp.call('action=getTopRatedServices', ratedCallback)
    };

    //Show-hide login box
    this.showLogin = function () {
        $.ajax({
                   url:'includes/home/login-box.jag',
                   async:false,
                   success:function(data) {
                       $('#login-box').html(arguments[2].responseText);
                       var triggers = $("#mainLoginBox").overlay({
                           onLoad: function() {
                               $('#name').focus();
                           },
                           load: true,
                           closeOnClick: false
                       });
                       $("#mainLoginForm").submit(function(e) {
                           // close the overlay
                           triggers.eq(0).overlay().close();
                           ServiceStoreApp.login('');
                           return e.preventDefault();

                       });
                   }
               });

    };

    //Operation to make a purchase
    this.purchase = function (id) {
    	console.log("purchase");
    	if(ServiceStoreApp.loggedUser == defaultUser){
    		console.log("need to login");
    		ServiceStoreApp.Defaultlogout();
    		ServiceStoreApp.showLogin();
    	}else{
        var path = $('#list-item-' + id).find('#path').text();
        var serviceName = $('#list-item-' + id).find('#name').text();
        var data = "action=purchaseService&path=" + path + "&serviceName=" + serviceName

        ServiceStoreApp.call(data, function (result) {
            if (result.error == "false") {
                $('#list-item-' + id).find('#buy').html('Uninstall');
            }
        })
    	}
    };
    this.searchServiceByName = function () {
    
        var searchTerm = $('#searchMain').val();
console.log("searchServiceByName XXXXXXxx"+searchTerm);
        ServiceStoreApp.call("action=searchAPI&searchTerm=" + searchTerm, function (result) {
        	renderServices(result);
            ServiceStoreApp.getSearchKeys(ServiceStoreAppSearchBar.initSearchAutoComplete);

        });
    };

   this.getSearchKeys = function (callback) {
       // ServiceStoreApp.call("action=getSearchKeys", callback);
    }

};
