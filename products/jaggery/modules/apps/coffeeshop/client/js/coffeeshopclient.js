CoffeeShopClient = new function() {
this.addOrder = function() {
var orderName = $('#orderName').val();
CoffeShopAppUtil.makeRequest("POST","/coffeeshop/order.jag?", "order="+orderName, function(html) {	});
console.log("Call add Order"+orderName);
}
this.viewOrder = function() {
var addittion = $('#addittion').val();
CoffeShopAppUtil.makeRequest("POST","/coffeeshop/order.jag?", "order="+addittion, function(html) {	});
console.log("Call add Order"+orderName);
}
this.addAddittion = function() {
var addittion = $('#addittion').val();
var orderid = $('#orderid').val();
//CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/order.jag?", "orderid="+orderid+"&addition="+addittion, function(html) {	});
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/order.jag?", "orderid="+orderid+"&addition="+addittion, function(html) {	});
console.log("Call add additon "+ addittion + " on "+orderid);
}
}