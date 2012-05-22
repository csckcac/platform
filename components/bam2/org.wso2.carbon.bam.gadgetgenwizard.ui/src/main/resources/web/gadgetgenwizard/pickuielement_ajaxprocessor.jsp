<%@ page import="java.util.Map" %>
<%

    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }

%>
<!--[if lt IE 9]><script language="javascript" type="text/javascript" src="../gadgetgenwizard/js/excanvas.min.js"></script><![endif]-->
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.barRenderer.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.categoryAxisRenderer.js"></script>

<link rel="stylesheet" type="text/css" href="../gadgetgenwizard/css/jquery.jqplot.min.css" />

<script type="text/javascript">

    var sqlResultJSON = null;

    $("#preview").click(function () {
        genPreview();
    });

    function uiElementChange() {
        if ($("#uielement").val() == "bar") {
            $("#bar-chart-options").show('fast', function() {
                if (!sqlResultJSON) {
                $.post("execute_sql_ajaxprocessor.jsp", null, function(html) {
                    var success = !(html.toLowerCase().match(/error/));
                    if (success) {
                        sqlResultJSON = JSON.parse(html);
                        var optionHTML = "";
                        $.each(sqlResultJSON.ColumnNames, function(i, val) {
                            optionHTML += "<option value=\"" + val + "\">" + val + "</option>"
                        });
                        $("#bar-chart-options [name$=\"column\"]").html(optionHTML);
                        $("#bar-chart-options .bar").change(genPreview);
                    } else {
                        CARBON.showErrorDialog(html);
                    }
                });


            }
            });

        } else {
            $("#bar-chart-options").hide();
        }
    }



    function genPreview() {

        var allValsComplete = $("#bar-chart-options .bar").map(function(i, e) {
            return $(e).val();
        }).get().join(",");
        var matchVals = allValsComplete.match(/,/g);

        if (sqlResultJSON && matchVals.length >= 3) {

            var xColIndex;
            var yColIndex;

            $.each(sqlResultJSON.ColumnNames, function(i, val) {
                if ($("[name=\"bar-xcolumn\"]").val() == val) {
                    xColIndex = i;
                }
                if ($("[name=\"bar-ycolumn\"]").val() == val) {
                    yColIndex = i;
                }
            });

            var plotArray = [];
            $.each(sqlResultJSON.Rows, function (i, val) {
                plotArray.push([val[yColIndex], parseInt(val[xColIndex])]);
            });

            $.jqplot('preview-area', [plotArray], {
                title: $("[name=bar-title]").val(),
                series:[{renderer:$.jqplot.BarRenderer}],
                axes: {
                    xaxis: {
                        renderer: $.jqplot.CategoryAxisRenderer,
                        label: $("[name=bar-xlabel]").val(),
                        // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                        tickOptions: {
                            enableFontSupport: true,
                            angle: -30
                        }

                    },
                    yaxis: {
                        autoscale:true,
                        label: $("[name=bar-ylabel]").val(),
                        // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                        tickOptions: {
                            enableFontSupport: true,
                            angle: -30
                        }
                    }
                }
            });


        }
    }

    $(document).ready(function () {
        uiElementChange();
        $("#uielement").change(function() {
            uiElementChange();
        })
    })

</script>
<tr>
    <td>Pick UI Element<font color="red">*</font>
    </td>
    <td><select name="uielement" id="uielement" style="width:200px">
        <option value="bar">Bar Chart</option>
        <option value="table">Table</option>
    </select>
    </td>
    <td><input id="preview" type="button" value="Preview"></td>

</tr>
<tr>
    <div id="bar-chart-options">
        <table class="normal">
            <tbody>
            <tr>
                <td>Chart Title<font color="red">*</font>
                </td>
                <td><input type="text" class="bar" name="bar-title" value="Product vs Total Amount" style="width:150px"/></td>
            </tr>
            <tr>
                <td>Y-Axis Label<font color="red">*</font></td>
                <td><input class="bar" type="text" name="bar-ylabel" value="Total Amount (Rs.)" style="width:150px"/></td>
            </tr>
            <tr>
                <td>Y-Axis Column<font color="red">*</font></td>
                <td><select class="bar" name="bar-ycolumn" style="width:150px"/></td>
            </tr>
            <tr>
                <td>X-Axis Label<font color="red">*</font></td>
                <td><input type="text" class="bar" name="bar-xlabel" value="Product Name" style="width:150px"/></td>
            </tr>
            <tr>
                <td>X-Axis Column<font color="red">*</font></td>
                <td><select class="bar" name="bar-xcolumn" style="width:150px"/></td>
            </tr>


            </tbody>
        </table>
    </div>

</tr>

<tr rowspan="2">
    <div id="preview-area" style="width:600px;height:400px"></div>
</tr>

<input type="hidden" name="page" id="page" value="3"/>
