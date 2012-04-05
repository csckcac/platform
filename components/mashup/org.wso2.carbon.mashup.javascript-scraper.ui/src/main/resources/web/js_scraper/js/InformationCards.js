/*
 * Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * This code is licensed under the Microsoft Permissive License (Ms-PL)
 * 
 * SEE: http://www.microsoft.com/resources/sharedsource/licensingbasics/permissivelicense.mspx
 * 
 * or the EULA.TXT file that comes with this software.
 */

var BrowserUtility= {
    /// <summary>
	/// This closure exposes some low-level browser functionality.
    /// </summary>
	///
    GetElementsByClass :function(cssClass, node, tag) {
		/// <summary>
		/// gets all the elements of a given class and returns them as an array.
		/// </summary>  
		///
		///          <param name="cssClass">the class to search for</param>
		///          <param name="node">the node to start with. (defaults to document)</param>
		///          <param name="tag">the tag type to scope to (defaults to all tags)</param>
		///
		/// <returns>array of requested elements</returns>

		var result = new Array();
		var els = (node?node:document).getElementsByTagName(tag?tag:'*');
		var pattern = new RegExp("(^|\\s)" + cssClass + "(\\s|$)");
		for(each in els)
		    if(pattern.test(els[each].className))
			    result[result.length] = els[each];

		return result;
    },


    ShowClass :function(classname) {
		/// <summary>
		/// Shows elements by class name
		/// </summary>
		///  
		/// <param name="classname">the css class to show</param>
	    var elements = this.GetElementsByClass(classname);
	    for(each in elements)
            try {
                elements[each].style.display = 'block';
            } catch(ex) {
            }
    },

    HideClass :function(classname) {
		/// <summary>
		/// Hides elements by class name
		/// </summary>
		///
		/// <param name="classname">the css class to hide</param>
	    var elements = this.GetElementsByClass(classname);
	    for(each in elements)
		    elements[each].style.display = 'none';
    },
    
    AddEventsToElements: function (events) {
		/// <summary>
		/// Adds event handlers to elements
		/// </summary>
		///
		/// <param name="events">a nested collection of events and handlers:</param>
		///
		///      <example><code>
		///             BrowserUtility.AddEventsToElements( {
		///                 icSignIn : { onclick: CardKit.SignIn },
		///                 icSignUp : { onclick: CardKit.SignUp },
		///                 icProtectAccount : { onclick: CardKit.ProtectAccount },
		///                 icRecover : { onclick: CardKit.RecoverId, onmouseover: CardKit.Shhhh } );
		///		</code></example>
       for( element in events )
            if( node = document.getElementById(element) )
                for( evt in events[element] )
                    node[evt]  = events[element][evt];    
    },
    
    AddField :function(form, name, value ) {
		/// <summary>
		/// Adds a field to a form
		/// </summary>
		///
		/// <param name="form">the form object</param>
		/// <param name="name">the name of the field</param>
		/// <param name="value">the value of the field</param>
		/// <returns>the created field</returns>
        var p=document.createElement('input');
        p.type = "hidden";
        p.name = name;
        p.value = value;
        form.appendChild(p);
        return p;
    },

    AddNewForm :function(url) {
		/// <summary>
		/// Adds a new form to the DOM
		///
		/// Only does HTTP POSTs 
		/// </summary>
		/// 
		/// <param name="url">the URL to post the form to.</param>
		/// <returns>the created form</returns>
        var f=document.createElement('form');
        f.name="newform";
        f.action = url;
        f.setAttribute('method','post');
        this.AppendToBody(f);
        return f;
    },
    
    AppendToBody :function(element) {
		/// <summary>
		/// Appends an element to the body of the document.
		/// </summary>
		///
		/// <param name="element">the element to add.</param>
        var body = document.getElementsByTagName("body").item(0);
        body.appendChild(element);
    },

    AddObjectParameter :function( element, name , value ) {
		/// <summary>
		///  Adds a PARAM element to a OBJECT element
		/// </summary>
		///
		/// <param name="element">the OBJECT element to add to.</param>
		/// <param name="name">the PARAM name</param>
		/// <param name="value">the PARAM value</param>
        var p = document.createElement('param');
        p.setAttribute('name' , name );
        p.setAttribute('value' , value );
        element.appendChild(p);
    },
    
    ltrim :function(str){ 
		/// <summary>
		/// javascript helper function: trim left whitespace
		/// </summary>
		///
		/// <param name="str">the string to trim</param>
		/// <returns>trimmed string</returns>
        return str.replace(/^[ ]+/, ''); 
    },
     
    rtrim :function(str){ 
		/// <summary>
		/// javascript helper function: trim right whitespace
		/// </summary>
		///
		/// <param name="str">the string to trim</param>
		/// <returns>trimmed string</returns>
        return str.replace(/[ ]+$/, ''); 
    },
 
    trim:function(str) { 
		/// <summary>
		/// javascript helper function: trim all whitespace
		/// </summary>
		///
		/// <param name="str">the string to trim</param>
		/// <returns>trimmed string</returns>
        return this.ltrim(this.rtrim(str)); 
    },
     
    Cookie: function(name) {
		/// <summary>
		/// Gets a cookie value
		/// </summary>
		///
		/// <param name="name">the name of the cookie to retrieve</param>
		/// <returns>the cookie value</returns>
        var cookies = document.cookie.split(';');
        
	    for(var i=0;i < cookies.length;i++) {
		    var p = cookies[i].split('=');
		    if( BrowserUtility.trim(p[0]) == name )
		        return BrowserUtility.trim(p[1]);
        }
	    return null;
    },
    
   SetCookie: function(name,value,days) {
		/// <summary>
		/// Sets a cookie value
		/// </summary>
		///
		/// <param name="name">the name of the cookie to retrieve</param>
		/// <param name="value">the value to set the cookie to</param>
		/// <param name="days">the length of time the cookie should last.</param>
        var expires = "";
	    if (days) {
		    var date = new Date();
		    date.setTime(date.getTime()+(days*24*60*60*1000));
		    expires = "; expires="+date.toGMTString();
	    }
	    document.cookie = name+"="+value+expires+"; path=/";
	},
	
	ClearCookie: function(name){
		/// <summary>
		/// Removes a cookie 
		/// </summary>
		///
		/// <param name="name">the name of the cookie to remove</param>
	    this.SetCookie(name,"",-1);
	},
	
	GetAndClearCookie: function( name ){
		/// <summary>
		/// Gets a cookie value, and clears it 
		/// </summary>
		///
		/// <param name="name">the name of the cookie to retrieve and clear</param>
		/// <returns>the cookie value</returns>
	    var result = this.Cookie( name );
	    this.ClearCookie( name );
	    return result;
	},
	
	IsSecure : function(addr){
		/// <summary>
		/// Determines if the URL is HTTPS
		/// </summary>
		///
		/// <param name="addr">the address to check. defaults to the current document.</param>
		/// <returns>true if the address is https:// </returns>
	    addr = addr == null ? window.location.href : addr;
	    return addr.toLowerCase().indexOf("https://" ) == 0;
	}
}


var InformationCard = {
	/// <summary>
	/// InformationCard 
	///      This closure exposes functions to use Information Cards easier.
	///
	/// </summary>

    /* Constants */
    SAML10 : "urn:oasis:names:tc:SAML:1.0:assertion" , 
    SAML11 : "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1" , 

    /* claims */
    GivenName : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
    Surname : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
    StreetAddress : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/streetaddress",
    Locality : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality",
    StateOrProvince : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/stateorprovince",
    PostalCode : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/postalcode",
    Country : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/country",
    HomePhone : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/homephone",
    OtherPhone : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/otherphone",
    MobilePhone : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/mobilephone",
    DateOfBirth : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/dateofbirth",
    Gender : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/gender",
    PPID : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier",
    Webpage : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/webpage",
    Email : "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",

    init : function() {
		/// <summary>
		/// Initialization:
		///  
		/// Does not get called until the browser is finished loading the page.
		/// </summary>
        this.TokenType = this.SAML11;
        this.RequiredClaims = { 0:this.PPID };
        this.OptionalClaims = [];
        this.PrivacyUrl = "";
        this.PrivacyVersion = null;
        this.Issuer = "";
        
        // shows the elements in the page that enable Information Cards.
        if( InformationCard.AreCardsSupported() ) {
            BrowserUtility.ShowClass("InformationCardsSupported");
        } else {
            // Otherwise, disable the buttons
            for( each in { icProtectAccount:0,icSignIn:0,icSignUp:0,icConfirmation:0} )
                try{ document.getElementById(each).disabled = true; } catch(e) { }
            BrowserUtility.ShowClass("InformationCardsNotSupported");
        }
    },
  
    //
    // default value for Information Cards being supported.
    // allows us to short-circuit the function later.
    //
    _areCardsSupported : 'undefined',

    AreCardsSupported : function()  { 
		/// <summary>
		/// Determines if Information Cards are supported by the browser.
		/// </summary>
		/// <returns>true if the browser supports Information Cards.</returns>
    
		// short circuit after the first call by caching the value.
		if( this._areCardsSupported != 'undefined')
			return this._areCardsSupported;
			
		  var IEVer = -1; 
		  if (navigator.appName == 'Microsoft Internet Explorer') 
			if (new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})")
			   .exec(navigator.userAgent) != null) 
			  IEVer = parseFloat( RegExp.$1 ); 

		  // Look for IE 7+. 
		  if( IEVer >= 7 ) { 
			var embed = document.createElement("object"); 
			embed.setAttribute("type", "application/x-informationcard"); 

			return (this._areCardsSupported = (""+embed.issuerPolicy != "undefined" && embed.isInstalled));
		  }     
		  // not IE (any version)
		  if( IEVer < 0 && navigator.mimeTypes && navigator.mimeTypes.length)  { 
			// check to see if there is a mimeType handler. 
			x = navigator.mimeTypes['application/x-informationcard']; 
			if (x && x.enabledPlugin) 
			  return (this._areCardsSupported = true);

			// check for the IdentitySelector event handler is there. 
			var event = document.createEvent("Events"); 
			event.initEvent("IdentitySelectorAvailable", true, true); 
			top.dispatchEvent(event); 

			if( top.IdentitySelectorAvailable == true) 
			  return (this._areCardsSupported = true); 
		  } 
		   return (this._areCardsSupported = false); 
    },

    GetInformationCardObject :function() {
		/// <summary>
		/// Creates an Information Card object, with the values from this closure.
		/// </summary> 
		/// <returns>the Identity Selector object</returns>
		
        // create object
        var obj = document.createElement('object');
        
        // add all parameters first
        var claims = "";
        for(each in this.RequiredClaims)
            claims+=" "+this.RequiredClaims[each];
            
        BrowserUtility.AddObjectParameter( obj, "requiredClaims", claims );

        if( this.TokenType )
            BrowserUtility.AddObjectParameter( obj, "tokenType" , this.TokenType );
            
        if( this.OptionalClaims )
        {
            claims = "";
            for(each in this.OptionalClaims)
                claims+=" "+this.OptionalClaims[each];

            if( claims != ""  )
                BrowserUtility.AddObjectParameter( obj, "optionalClaims", claims);
        }
        
        
        if( this.PrivacyVersion && this.PrivacyUrl && this.PrivacyUrl != "" )
        {
            BrowserUtility.AddObjectParameter( obj, "privacyUrl" , this.PrivacyUrl);
            BrowserUtility.AddObjectParameter( obj, "privacyVersion" , this.PrivacyVersion);
        }
        
        if( this.Issuer && this.Issuer != "")
            BrowserUtility.AddObjectParameter( obj, "Issuer" , this.Issuer);

        // set the type, and the it suddenly 'understands'
        obj.setAttribute( "type", "application/x-informationcard");
        
        // adding the object to the body activates it.
        BrowserUtility.AppendToBody(obj);
        return obj;
    },

    GetToken :function() {
		/// <summary>
		/// Calls the identity selector to get a token
		/// returns either the token, or an error number.
		/// </summary> 
		/// <returns>the token requested from the Identity Selector, or the error code if no token is aquired.</returns>
		
        var xmltkn=this.GetInformationCardObject();
        
        var result = null;
	    try {
            result = xmltkn.value;
        }
        catch( e ) {
            return e.number;
        }
	    return result;
    },


    SignInWithCard :function(target,action,trouble,rememberUser, extraFieldsToSubmit ) {
		/// <summary>
		/// Gets the token, and forwards the user to the actionable page
		/// </summary>
		///
		/// <param name="target">the URL to post the token to.</param>
		/// <param name="action">an action message sent to the destination page.</param>
		/// <param name="trouble">either a URL or a javascript function to call if the get token fails.</param>
		/// <param name="rememberUser">a helper value to tell the action page to 'remember me'</param>
        with( InformationCard ) {
            var card = GetToken();
            if( (""+card).charAt(0) == '<' )
            {
                //got a card. let's send it in.
                var f=BrowserUtility.AddNewForm( target ? target : window.location.href );
                BrowserUtility.AddField( f , "action" , action ? action : "SignIn");
                BrowserUtility.AddField( f , "xmltoken" , card );
                BrowserUtility.AddField( f , "sourcepage" , window.location.href);
                BrowserUtility.AddField( f , "rememberUser" , rememberUser ? rememberUser : false);
                if( extraFieldsToSubmit )
                    for(each in extraFieldsToSubmit)
                        BrowserUtility.AddField( f , each, extraFieldsToSubmit[each] );
                f.submit();
            } else {
                //error, let's go troubleshooting.
                //want to call a function?
                if( typeof(trouble) == "function" );
                    return trouble( card );
                    
                // post to the error page
                var f=BrowserUtility.AddNewForm(  trouble ? trouble : window.location.href );
                BrowserUtility.AddField( f , "action" , "troubleshoot");
                BrowserUtility.AddField( f , "errorvalue" , card );
                BrowserUtility.AddField( f , "sourcepage" , window.location.href);
                f.submit();
            }
        }
    }
}

/* initial setup for Information Cards */
var _ic_init =  document.addEventListener?document.addEventListener("DOMContentLoaded", function(){InformationCard.init()}, false): setInterval(function(){if (/loaded|complete/.test(document.readyState)){clearInterval(_ic_init);InformationCard.init(); } }, 10);
