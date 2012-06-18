CoffeeShopClient = new function() {
	
this.addOrder = function(orderName) {
CoffeShopAppUtil.makeRequest("POST","/coffeeshop/orders/order.jag?", "order="+orderName, function(html) {	});
console.log("Call add Order "+orderName);
}

this.viewOrder = function() {
var orderid = $('#orderid').val();
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/orders/"+orderid+"", null , function(html) {	});
console.log("view order "+orderid);
}

this.viewOrders = function() {
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/orders/", null , function(html) {	});
}

this.addAddittion = function(orderid,addition) {
//var addition = $('#addition').val();
//var orderid = $('#orderid').val();
var content = '{"addition":'+addition+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/orders/"+orderid, content , function(html) {	});
console.log("Call add additon "+ addition + " on "+orderid);
}

this.updateStatus = function(orderid,status) {
//var status = $('#status').val();
//var orderid = $('#orderid').val();
var content = '{"status":'+status+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/orders/"+orderid, content , function(html) {	});
console.log("Call add status "+ status + " on "+orderid);
}

this.isPaidOrder = function() {
var orderid = $('#orderid').val();
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/payments/"+orderid, null , function(html) {	});
console.log("Check order is paid "+orderid);
}

this.payOrder = function(orderid) {
//var orderid = $('#orderid').val();
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/payments/"+orderid, null , function(html) {	});
console.log("Call for pay order "+orderid);
}

this.deletOrder = function() {
var orderid = $('#orderid').val();
CoffeShopAppUtil.makeRequest("DELETE","/coffeeshop/orders/"+orderid+"/", null , function(html) {	});
console.log("Call for delet order "+orderid);
}

}