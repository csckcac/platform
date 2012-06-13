CoffeeShopClient = new function() {
	
this.addOrder = function() {
var orderName = $('#orderName').val();
CoffeShopAppUtil.makeRequest("POST","/coffeeshop/order.jag?", "order="+orderName, function(html) {	});
console.log("Call add Order"+orderName);
}

this.viewOrder = function() {
var orderid = $('#orderid').val();
//var content = '{"id":'+orderid+'}';
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/order.jag?", "orderid="+orderid , function(html) {	});
console.log("view order "+orderid);
}

this.addAddittion = function() {
var addition = $('#addition').val();
var orderid = $('#orderid').val();
var content = '{"id":'+orderid+',"addition":'+addition+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/order.jag?", content , function(html) {	});
console.log("Call add additon "+ addition + " on "+orderid);
}

this.updateStatus = function() {
var status = $('#status').val();
var orderid = $('#orderid').val();
var content = '{"id":'+orderid+',"status":'+status+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/order.jag?", content , function(html) {	});
console.log("Call add status "+ status + " on "+orderid);
}

this.isPaidOrder = function() {
var orderid = $('#orderid').val();
//var content = '{"id":'+orderid+'}';
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/payment.jag?", "orderid="+orderid , function(html) {	});
console.log("Check order is paid "+orderid);
}

this.payOrder = function() {
var orderid = $('#orderid').val();
var content = '{"id":'+orderid+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/payment.jag?", content , function(html) {	});
console.log("Call for pay order "+orderid);
}

this.deletOrder = function() {
var orderid = $('#orderid').val();
var content = '{"id":'+orderid+'}';
CoffeShopAppUtil.makeRequest("DELETE","/coffeeshop/order.jag?", content , function(html) {	});
console.log("Call for delet order "+orderid);
}

}