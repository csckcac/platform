<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html"/>

	<!--TEMPLATE: init access point
	-->
	<xsl:template match="/">
		<!--<xsl:call-template name="main"/>-->
		<xsl:call-template name="gadget-wrap"/>
	</xsl:template>

	<!--TEMPLATE: call the main and wrap it with gadget xml
	TODO: call main only ones
	-->
	<xsl:template name="gadget-wrap">
		<xsl:text disable-output-escaping="yes">

		  <![CDATA[ <Module> <ModulePrefs title="Sample" scrolling="true" height="350"> <Require feature="dynamic-height"/> </ModulePrefs> <Content type="html" view="default"> <![CDATA[ ]]>

		</xsl:text>
		<xsl:call-template name="main"/>
		<xsl:text disable-output-escaping="yes">

		  ]]&gt; <![CDATA[ </Content> <Content type="html" view="canvas"> <![CDATA[ ]]>

		</xsl:text>
		<xsl:call-template name="main"/>
		<xsl:text disable-output-escaping="yes">

		  ]]&gt; <![CDATA[ </Content> </Module> ]]>

		</xsl:text>
	</xsl:template>


    <!--TEMPLATE: main template genrated the HTML for the gadget-->
	<xsl:template name="main">
		<html>
			<head>
                <link href="css/gadgetide-client.css" rel="stylesheet"/>
				<xsl:call-template name="include-stubs"/>
				<script src="js/gadgetide-client.js"></script>
			</head>
			<body>
				<xsl:call-template name="gen-divs"/>
				<script>
					<xsl:call-template name="gen-js"/>
				</script>
			</body>
		</html>
	</xsl:template>

    <!--TEMPLATE: add script tags for inclusing js stubs -->
    <xsl:template name="include-stubs">
        <xsl:if test="//units/unit/state/config/operation">
            <xsl:for-each select="//units/unit/state/config/operation">
                <xsl:text disable-output-escaping="yes">&lt;script src="js/</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text disable-output-escaping="yes">.js"&gt;&lt;/script&gt;</xsl:text>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>


    <!--TEMPLATE: generate contents of the script tag -->
	<xsl:template name="gen-js">
		<xsl:text>&#10;</xsl:text>
		<xsl:call-template name="init-js-var"/>
		<xsl:call-template name="call-render"/>
		<xsl:call-template name="init-js-graph"/>
	</xsl:template>

    <!--TEMPLATE: call render() on units-->
	<xsl:template name="call-render">
		<xsl:text>&#10;</xsl:text>
		<xsl:for-each select="//units/unit">
			<xsl:if test="state/ui">
            <xsl:choose>
                <xsl:when test="state/spec/Text">
                    <xsl:text></xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="name"/>
				    <xsl:text>.render(document.getElementById("</xsl:text>
				    <xsl:value-of select="name"/>
				    <xsl:text>Div"));&#10;</xsl:text>
                </xsl:otherwise>
            </xsl:choose>

			</xsl:if>
		</xsl:for-each>
	</xsl:template>

    <!--TEMPLATE: define and initialize data flow graph-->
	<xsl:template name="init-js-graph">
		<xsl:text>&#10;</xsl:text>
		<xsl:for-each select="//graph/dependents/dependent">
			<xsl:variable name="dependentName" select="name" />
			<xsl:for-each select="dependencies/dependency">
				<xsl:text>addDependency(</xsl:text>
				<xsl:value-of select="name"/>
				<xsl:text>,</xsl:text>
				<xsl:call-template name="to-field-path">
					<xsl:with-param name="path" select="from/qname"/>
				</xsl:call-template>
				<xsl:text>,</xsl:text>
				<xsl:value-of select="$dependentName"/>
				<xsl:text>,</xsl:text>
				<xsl:call-template name="to-field-path">
					<xsl:with-param name="path" select="to/qname"/>
				</xsl:call-template>
				<xsl:text>);&#10;</xsl:text>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

    <!--TEMPLATE: gets a nod-set with namespace and localpart pairs and converts
	it to an array -->
	<xsl:template name="to-field-path">
        <xsl:param name="path"/>
		<xsl:text>[</xsl:text>
		<xsl:for-each select="$path">
			<xsl:choose>
				<xsl:when test="namespace">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="namespace"/>
					<xsl:text>"</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>null</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>, "</xsl:text>
			<xsl:value-of select="localpart"/>
			<xsl:text>"</xsl:text>
			<xsl:if test="position() != last()">
				<xsl:text>, </xsl:text>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>]</xsl:text>
	</xsl:template>

    <!--TEMPLATE: define and initialize js variables for each unit-->
	<xsl:template name="init-js-var">
		<xsl:for-each select="//units/unit">
            <xsl:choose>
                <xsl:when test="state/spec/Text">
                    <xsl:text></xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>var </xsl:text>
                    <xsl:value-of select="name"/>
                    <xsl:text> = new_unitByType("</xsl:text>
                    <xsl:value-of select="type"/>
                    <xsl:text>",{</xsl:text>
                    <xsl:apply-templates select="state/config/*"/>}
                    <xsl:text>);&#10;</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
		</xsl:for-each>
	</xsl:template>

    <!--TEMPLATE: generate empty DIV tags for each unit with position and size-->
	<xsl:template name="gen-divs">
		<xsl:for-each select="//units/unit">
			<xsl:if test="state/ui">
				<xsl:text disable-output-escaping="yes">&lt;div id="</xsl:text>
				<xsl:value-of select="name"/>
				<xsl:text>Div" style="position:absolute; top:</xsl:text>
				<xsl:value-of select="state/ui/top"/>
				<xsl:text>px; left:</xsl:text>
				<xsl:value-of select="state/ui/left"/>
				<xsl:text>px; height:</xsl:text>
				<xsl:value-of select="state/ui/height"/>
				<xsl:text>px; width:</xsl:text>
				<xsl:value-of select="state/ui/width"/>
				<xsl:text>px; overflow:auto;</xsl:text>
                <xsl:text disable-output-escaping="yes">" &gt;</xsl:text>
                <xsl:choose>
                    <xsl:when test="state/spec/Text">
                        <xsl:text disable-output-escaping="yes">&lt;span id="</xsl:text>
                        <xsl:value-of select="name"/>
                        <xsl:text disable-output-escaping="yes">span" &gt;</xsl:text>
                        <xsl:value-of select="state/spec/Text" disable-output-escaping="yes"/>
                        <xsl:text disable-output-escaping="yes">&lt;/span&gt;</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <!--<xsl:if test="state/spec/Text">
                    <xsl:value-of select="state/spec/Text"/>
                </xsl:if>-->
				<xsl:text disable-output-escaping="yes">&lt;/div&gt;</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>




<!--http://www.bizcoder.com/index.php/2010/02/12/convert-xml-to-json-using-xslt/-->

    <!-- Object or Element Property-->
    <xsl:template match="*">
        "<xsl:value-of select="name()"/>" : <xsl:call-template name="Properties"/>
    </xsl:template>

    <!-- Array Element -->
    <xsl:template match="*" mode="ArrayElement">
        <xsl:call-template name="Properties"/>
    </xsl:template>

    <!-- Object Properties -->
    <xsl:template name="Properties">
        <xsl:variable name="childName" select="name(*[1])"/>
        <xsl:choose>
            <xsl:when test="not(*|@*)">"<xsl:value-of select="."/>"</xsl:when>
            <xsl:when test="count(*[name()=$childName]) > 1">{ "<xsl:value-of select="$childName"/>" :[<xsl:apply-templates select="*" mode="ArrayElement"/>] }</xsl:when>
            <xsl:otherwise>{
                <xsl:apply-templates select="@*"/>
                <xsl:apply-templates select="*"/>
			}</xsl:otherwise>
        </xsl:choose>
        <xsl:if test="following-sibling::*">,</xsl:if>
    </xsl:template>

    <!-- Attribute Property -->
    <xsl:template match="@*">"<xsl:value-of select="name()"/>" : "<xsl:value-of select="."/>",
    </xsl:template>



</xsl:stylesheet>
