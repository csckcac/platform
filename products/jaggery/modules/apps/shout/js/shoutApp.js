ShoutApp = new function() {
	this.authPopUp = function() {

		ShoutAppUtil.makeRequest("/shout/twitter.jag", "", function(html) {
			var windowName = 'Twitter Auth'; 
			var popUp = window.open(html, windowName, 'width=1000, height=700, left=24, top=24, scrollbars, resizable');
			if (popUp == null || typeof(popUp)=='undefined') { 	
				alert('Please disable your pop-up blocker and click again.'); 
			} 
			else { 	
				popUp.focus();
			}
			$('#key').show();
		});
		$.fancybox.close();
		console.log("Called for Auth PopUp");
	}

	this.sendVerificationCode = function() {
		ShoutAppUtil.makeRequest("/shout/twitter.jag?atoken="
				+ $('#authKey').val(), "", function(html) {
			$('#key').hide();
			ShoutApp.readXML(html);
		});
		console.log("Called for send VerificationCode");
	}

	this.readXML = function(xml) {
		$("#feed_scroll").html("");
		$(xml)
				.find("status")
				.each(
						function() {
							var imgUrl = "http"
									+ $(this).find("profile_image_url").text()
											.split('http')[1];
							$("#feed_scroll")
									.append(
											"<div class=\"feed_entry html\"><img src=\""
													+ imgUrl
													+ "\" class=\"user_thumb\"/>"
													+ "<span class=\"status\"><a href=\"#\" class=\"author\">"
													+ $(this).find("name")
															.text() +" ||"
													+ "</a> said <span class=\"status_content\">"
													+ "\""
													+ $(this).find("text")
															.text()
													+ "\" </span></span>"
													+ "</span>"
													+ "<span class=\"status_details\"><a href=\"#\"><img src=\"images/twitter_tiny.png\" alt=\"\" />"
													+ "</span>" + "</div>");
						});

	}

	this.postIt = function() {
		$("#feed_scroll").html("");
		console.log("Post It");
		ShoutAppUtil.makeRequest("/shout/twitter.jag?share="
				+ $('#shout_txt').val(), "", function(html) {

		});
		console.log("Called for send Share");
		ShoutApp.loadBack();
	}
	


	this.loadBack = function() {
		console.log("Loading back Time line");

		ShoutAppUtil.makeRequest("/shout/twitter.jag?loadback=load", "",
				function(html) {
					ShoutApp.readXML(html);
				});
	}
}