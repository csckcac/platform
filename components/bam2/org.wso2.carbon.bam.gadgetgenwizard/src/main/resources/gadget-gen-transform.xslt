<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
  ~  Copyright (c) 2005-2010, WSO2 Inc. (http://wso2.com) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  ~
  -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xmlns:m0="http://services.samples"
                xmlns:gg="http://wso2.com/bam/gadgetgen"
                exclude-result-prefixes="gg m0 fn">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="/">

<html>
<head>
    <link rel="stylesheet" type="text/css" href="jquery.jqplot.min.css" />
         <xsl:text disable-output-escaping="yes">&#60;</xsl:text>script src="jquery.min.js" type="text/javascript"<xsl:text disable-output-escaping="yes">&#62;</xsl:text><xsl:text disable-output-escaping="yes">&#60;</xsl:text>/script<xsl:text disable-output-escaping="yes">&#62;</xsl:text>


    <xsl:text disable-output-escaping="yes">&#60;</xsl:text>script src="jquery.jqplot.min.js" type="text/javascript"<xsl:text disable-output-escaping="yes">&#62;</xsl:text><xsl:text disable-output-escaping="yes">&#60;</xsl:text>/script<xsl:text disable-output-escaping="yes">&#62;</xsl:text>


    <xsl:text disable-output-escaping="yes">&#60;</xsl:text>script type="text/javascript" src="plugins/jqplot.categoryAxisRenderer.js"<xsl:text disable-output-escaping="yes">&#62;</xsl:text><xsl:text disable-output-escaping="yes">&#60;</xsl:text>/script<xsl:text disable-output-escaping="yes">&#62;</xsl:text>


	<xsl:text disable-output-escaping="yes">&#60;</xsl:text>script type="text/javascript" src="plugins/jqplot.barRenderer.js"<xsl:text disable-output-escaping="yes">&#62;</xsl:text><xsl:text disable-output-escaping="yes">&#60;</xsl:text>/script<xsl:text disable-output-escaping="yes">&#62;</xsl:text>

    <script lang="javascript" type="text/javascript">

        <xsl:text disable-output-escaping="yes">&#60;</xsl:text><xsl:text>&#37;</xsl:text>
        var db = new Database("<xsl:value-of select="//gg:jdbcurl" />", "<xsl:value-of select="//gg:username"/>", "<xsl:value-of select="//gg:password"/>");
    	var result = db.query("<xsl:value-of select="//gg:sql"/>");

        var colx = "<xsl:value-of select="//gg:bar-xcolumn" />".toUpperCase();
    	var coly = "<xsl:value-of select="//gg:bar-ycolumn" />".toUpperCase();

    	function convertDBResult(result, colx, coly) {
			var array = [];
			for (var i = 0; i <xsl:text disable-output-escaping="yes">&#60;</xsl:text> result.length; i++) {
				// print("The " + i + "th result is : " + result[i]);
				array.push([result[i][colx],parseFloat(result[i][coly])]);
			}

			return array;
        };
        var plotarray = convertDBResult(result, colx, coly);


   		 <xsl:text>&#37;</xsl:text><xsl:text disable-output-escaping="yes">&#62;</xsl:text>
        $(document).ready(function () {

            var plot1 = $.jqplot('chart1', [<xsl:text disable-output-escaping="yes">&#60;</xsl:text><xsl:text>&#37;</xsl:text>=plotarray<xsl:text>&#37;</xsl:text><xsl:text disable-output-escaping="yes">&#62;</xsl:text>], {
    title: '<xsl:value-of select="//gg:bar-title" />',
    series:[{renderer:$.jqplot.BarRenderer}],
    axes: {
      xaxis: {
        renderer: $.jqplot.CategoryAxisRenderer,
          label: '<xsl:value-of select="//gg:bar-xlabel" />',
        // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
        tickOptions: {
          enableFontSupport: true,
            angle: -30
        }

      },
      yaxis: {
        autoscale:true,
          label: '<xsl:value-of select="//gg:bar-ylabel" />',
        // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
        tickOptions: {
          enableFontSupport: true,
            angle: -30
        }
      }
    }
  });
        });
    </script>
</head>
<body>
        <div id="chart1" style="width: 500px; height: 300px;"/>
        <div id="text1" />
</body>
</html>
        </xsl:template>
</xsl:stylesheet>