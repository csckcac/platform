<%@ page import="java.util.Map" %>
<%

    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }

%>
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.barRenderer.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.categoryAxisRenderer.js"></script>


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

            // clear div
            $("#preview-area").html("");

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
<table class="normal">
    <tbody>
    <tr>
        <td>
            <table>
                <tbody>
                <tr>
                    <td>Pick UI Element
                    </td>
                    <td><select name="uielement" id="uielement" style="width:200px">
                        <option value="bar">Bar Chart</option>
                        <option value="table">Table</option>
                    </select>
                    </td>
                    <td><input id="preview" type="button" value="Preview"></td>

                </tr>
                <tr>
                    <td colspan="3">
                        <div id="bar-chart-options">
                            <table>
                                <tbody>
                                <tr>
                                    <td>Chart Title<span style="color: red; ">*</span>
                                    </td>
                                    <td><input type="text" class="bar" name="bar-title" value="Product vs Total Amount" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td>Y-Axis Label<span style="color: red; ">*</span></td>
                                    <td><input class="bar" type="text" name="bar-ylabel" value="Total Amount (Rs.)" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td>Y-Axis Column<span style="color: red; ">*</span></td>
                                    <td><select class="bar" name="bar-ycolumn" style="width:150px"></select></td>
                                </tr>
                                <tr>
                                    <td>X-Axis Label<span style="color: red; ">*</span></td>
                                    <td><input type="text" class="bar" name="bar-xlabel" value="Product Name" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td>X-Axis Column<span style="color: red; ">*</span></td>
                                    <td><select class="bar" name="bar-xcolumn" style="width:150px"></select></td>
                                </tr>


                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
        <td align="center" style="border:1px solid #000 !important">
            <div id="preview-area" style="text-align:center;width:325px;height:250px">Preview Area</div>
        </td>
        <input type="hidden" name="page" id="page" value="3"/>
    </tr>
    </tbody>
</table>