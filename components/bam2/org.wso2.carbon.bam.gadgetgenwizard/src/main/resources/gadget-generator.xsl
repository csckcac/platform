<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xmlns:m0="http://services.samples"
                xmlns:gg="http://wso2.com/bam/gadgetgen"
                exclude-result-prefixes="gg m0 fn">
    <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>


    <xsl:template match="/">

   <?xml version="1.0" encoding="UTF-8" ?>
<Module>
  <ModulePrefs title="Gadget Gen Sample" height="300" scaling="false">
    <Require feature="dynamic-height"/>
  </ModulePrefs>
  <Content type="html">
  <![CDATA[
    <link href="css/jquery.jqplot.min.css" type="text/css" rel="stylesheet"/>
    <script src="js/jquery.min.js" type="text/javascript"></script>
    <script src="js/jquery.jqplot.min.js" type="text/javascript"></script>
    <script type="text/javascript" src="js/plugins/jqplot.categoryAxisRenderer.js"></script>
    <script type="text/javascript" src="js/plugins/jqplot.barRenderer.js"></script>
    <script type="text/javascript" lang="javascript">
    $(document).ready(function () {



            var widthToHeightRatio = 325/250;
            var width = gadgets.window.getViewportDimensions()["width"];
            var height = (width/widthToHeightRatio);

            $("#chart1").width(width);
            $("#chart1").height(height);

            var plot = null;

            update();
            setInterval(update, 10000);

            function update() {
                var plotarray = null;
                $.ajax({
                    url: "../../gadgetgen/gadgetgen.jag",

                    dataType: 'json',
                    //GET method is used
                    type: "POST",

                    async: false,

                    //pass the data
                    data: "",

                    //Do not cache the page
                    cache: false,

                    //success
                    success: function (html) {

                        plotarray = html;
                    }
                });

                if (plot != null) {
                //    plot.destroy();
                }

                plot = $.jqplot('chart1', [plotarray], {
                    title: 'Product vs Total Amount',
                    series:[{renderer:$.jqplot.BarRenderer}],
                    axes: {
                        xaxis: {
                            renderer: $.jqplot.CategoryAxisRenderer,
                            label: '<xsl:value-of select="//gg:bar-title" />',
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }

                        },
                        yaxis: {
                            autoscale:true,
                            label: 'Total Amount (Rs.)',
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }
                        }
                    }
                });
                plot.replot();
                gadgets.window.adjustHeight();
            };
        });
    </script>

<div style="width: 325px; height: 250px;" id="chart1"/>
<div id="text1"/>

  ]]>
  </Content>
</Module>
    </xsl:template>

</xsl:stylesheet>