<%@page import="com.acme.shoppingcart.portal.PurchasingClient" %>
<%@ page import="com.acme.shoppingcart.portal.purchasing.types.CustomerDetails" %>
<%@ page import="com.acme.shoppingcart.portal.purchasing.types.Order" %>
<%@ page import="com.acme.shoppingcart.portal.purchasing.types.PurchaseOrder" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <jsp:include page="header_includes.jsp"/>
</head>
<body>
 <%
        PurchaseOrder purchaseOrder = new PurchaseOrder();

// product-code1:quantity1;product-code2:quantity2;product-code3:quantity3;
        String cart = request.getParameter("cart");
        if(cart != null) {
            String[] products = cart.split(";");
            for (String product : products) {
                String[] strings = product.split(":");
                String productCode = strings[0];
                String quantity = strings[1];
                Order order = new Order();
                order.setCode(productCode);
                order.setQuantity(quantity);
                purchaseOrder.addOrder(order);
            }
        }

        String email = request.getParameter("email");
        String cardHolder = request.getParameter("cardHolder");
        String cardType = request.getParameter("cardType");
        String cardNumber = request.getParameter("cardNumber");
        String shippingAddress = request.getParameter("shippingAddress");

        CustomerDetails customer = new CustomerDetails();
        customer.setName(cardHolder);
        customer.setCardType(cardType);
        customer.setShippingAddress(shippingAddress);
        customer.setCardNumber(cardNumber);
        customer.setEmail(email);

        new PurchasingClient().checkout(purchaseOrder, customer);
    %>
<div class="pageSizer">
    <div class="header">
        <img src="images/logo.png" alt="ACME"/>
    </div>
    <jsp:include page="menu.jsp"/>
    <div class="clear"></div>
    <div class="content">
        <table cellpadding="0" cellspacing="0" border="0" class="product-table-tiled" style="width:100%">

            <tr>
                <td>
                    <h1 style="font-size:20px;">Sucessfully Checked out!</h1>
                </td>
        </table>
    </div>
    <%--Add the add here--%>
</div>
 <div style="margin-top:100px;"><jsp:include page="footer.jsp"/></div>



<script type="text/javascript">
        toggleCart(document.getElementById('showCartIcon'));
        clear_cart();
    </script>
</body>
</html>