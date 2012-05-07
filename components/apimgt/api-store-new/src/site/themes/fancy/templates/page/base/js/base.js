$(document).ready(function() {
        $('.cat_and_charts ul.tabs li a').corner("bevel  tr");
        $('.api-info ul.tabs li a').corner("bevel  tr");
        $('.clouds ul.tabs li a').corner("bevel  tl");
        $('#left div.bevel-from-left').corner("bevel  bl");
        $('.bevel-from-right').corner("bevel  br");
		
		//Hiding the tabs to fix a bug in kikstart
		$('div.tab-content').hide();
        $('#tab0').show();
});