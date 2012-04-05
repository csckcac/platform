/*
ServiceStoreAppSearchBar = new function () {
    this.initSearchAutoComplete = function (result) {
        var source = ["Advertising","Answers","Auctions","Blogging","Calendar","Database","Job Search","Messaging","Music"];
        $("#searchMain").autocomplete({
            minLength:0,
            source:source});

        $('#searchForm').submit(function(e){
        	console.log("searchServiceByName");
            ServiceStoreApp.searchServiceByName();
            return e.preventDefault();
        });
        
        $('#searchFormButton').click(function(){
        	console.log("searchServiceByName click");
        });
        
    };
    this.initCategories = function(){
        var categories = ["All","Services","Projects"];
        var theUl = document.createElement('ul');
        customComboText =  $('#customComboText').html();
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

*/
