CoffeeShop = new function() {
$('.message-trace').hide();
$('.orderstatus').tooltip();

this.addNewOrder = function () {
console.log("Click  on addNewOrder");
var bevType = $("#beverage-type").val();
CoffeeShopClient.addOrder(bevType);
$('#addOrder').modal('toggle');

}

this.markPaid = function (id) {
console.log("Call markPaid" +id);
CoffeeShopClient.payOrder(id);
$("#" + id).children(".paid").show();
$("#" + id).children(".payment-action").hide();

}

this.markComplete =  function (id) {
console.log("Call markComplete" +id);
CoffeeShopClient.updateStatus(id,'Complete');
$("#" + id).children(".completed").show();
$("#" + id).children(".order-status").hide();
}

this.markDeletOrder = function(id) {
console.log("Call delet" +id);
CoffeeShopClient.deletOrder(id);
$("#" + id).children(".completed").show();
$("#" + id).children(".order-status").hide();
}

$('.minimize-button').click(function() {
$('.message-trace').toggle('slow');

});
this.showAddition = function(orderID) {
document.getElementById("modal-order-no").innerHTML = orderID;
$("input#orderAdditionId").val(orderID);
$('#addition').modal('toggle');
}

this.addAddition = function() {
//console.log(orderID+"Click  on addAddition ");
//document.getElementById("modal-order-no").innerHTML = orderID;
var orderID = $("input#orderAdditionId").val();
var additional = $("#additional-toppings").val();

//alert("order "+orderID+" has been saved");
CoffeeShopClient.addAddittion(orderID,additional);
console.log(orderID+"Clickx  on addAddition "+additional);
$('#addition').modal('toggle');
}

this.loadOrders = function(data) {
var template = '{{#orders}}<div class="order-entry"> '
+'<div class="order-number">{{ORDER_ID}}</div>'
+'<div class="order-content">'
+'{{DRINK}} <span class="help-block">{{ADDITION}}<a href="#" onclick="CoffeeShop.showAddition({{ORDER_ID}});" > Add addition</a></span>'
+'</div> <div class="order-actions" id="{{ORDER_ID}}"> '
+'<a class="btn btn-success completed" style="display:none;">Completed</a>'
+'<ul class="order-status">'
+'<li class="dropdown">'
+'<a class="dropdown-toggle btn btn-warning" data-toggle="dropdown" rel="tooltip" title="Order Status" href="#menu1"> {{STATUS}} <b class="caret"></b> </a>'
+'<ul class="dropdown-menu">'
+'<li>'
+'<a href="#" class="mark-paid" onclick="CoffeeShop.markComplete({{ORDER_ID}})">Mark as Complete</a>'
+'</li>'
+'<li>'
+'<a href="#" onclick="CoffeeShop.markDeletOrder({{ORDER_ID}})">Delete Order</a>'
+'</li>'
+'</ul>'
+'</li>'
+'</ul>'
+'<a class="btn btn-success paid" style="display:none;">Paid</a>'
+'<ul class="payment-action">'
+'{{^PAY}}<li class="dropdown">'
+'<a class="dropdown-toggle btn btn-danger" data-toggle="dropdown" href="#menu1"> Not Paid <b class="caret"></b> </a>'
+'<ul class="dropdown-menu">'
+'<li>'
+'<a href="#" class="mark-paid" onclick="CoffeeShop.markPaid({{ORDER_ID}})">Mark as Paid</a>'
+'</li>{{/PAY}}'
+'</ul> '
+'{{#PAY}} <button class="btn btn-success"  href="#"> paid</button>{{/PAY}}'
+'</li>'
+'</ul>'
+'</div>'
+'<div class="amount">'
+'{{COST}}'
+'</div>'
+'</div>{{/orders}}</div></div>';

var html = Mustache.to_html(template, data);
console.log("Call template onload ");
$("#order-window").html(html);

}

}