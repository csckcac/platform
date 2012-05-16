<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="formatxml.xslt"?>
<!--
  Author:  Jonathan Marsh (jonathan@wso2.com)
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
  <xsl:output method="html"/>

  <xsl:template match="node()"/>
  
  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          body { font-size: 75%; font-family: "Lucida Grande","Lucida Sans","Microsoft Sans Serif", "Lucida Sans Unicode",verdana,sans-serif,"trebuchet ms" }
          .fx-block { margin-left:1em; text-indent:-1em; margin-right:1em; font-size:13px; color:#555; line-height:140%;  
                      font-family: "Lucida Grande","Lucida Sans","Microsoft Sans Serif", "Lucida Sans Unicode",verdana,sans-serif,"trebuchet ms"}
          .fx-elnm { color:#005; }
          .fx-atnm { color:#500; }
          .fx-att { color:black }
          .fx-att a:link { color:black; text-decoration: none}
          .fx-att a:hover { color:black; text-decoration:underline}
          .fx-att a:active { color:black; text-decoration:underline}
          .fx-att a:visited { color:black; text-decoration:none }
          .fx-text { color:black; }
          pre.fx-text { margin-top:0px; margin-bottom:0px; margin-right: 1em; margin-left:-1em; text-indent:0em; line-height:15px; }
          .fx-cmk { margin-left:1em; text-indent:-1em; margin-right:1em; color:#050;}
          .fx-com { color:#050;}
          .fx-ns { color:#505}
          .fx-nsval {color:#505}
          .fx-nsval a:link { color:#505; text-decoration: none}
          .fx-nsval a:hover { color:#505; text-decoration:underline}
          .fx-nsval a:active { color:#505; text-decoration:underline}
          .fx-nsval a:visited { color:#505; text-decoration:none}
        </style>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>  
  </xsl:template>
  
  <!-- Template for normal attributes -->
  <xsl:template match="@*">
    <span class="fx-atnm">
      <xsl:text> </xsl:text>
      <xsl:value-of select="name()"/>
    </span>
    <xsl:text>="</xsl:text>
    <span class="fx-att"><xsl:value-of select="."/></span>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <!-- Template for src and href attributes (assume the content is a link)-->
  <xsl:template match="@src | @href">
    <span class="fx-atnm">
      <xsl:text> </xsl:text>
      <xsl:value-of select="name()"/>
    </span>
    <xsl:text>="</xsl:text>
    <span class="fx-att"><a href="{.}"><xsl:value-of select="."/></a></span>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <!-- Template for text nodes -->
  <xsl:template match="text()">
    <div class="fx-block">
      <span class="fx-text">
        <xsl:value-of select="."/>
      </span>
    </div>
  </xsl:template>
    
  <!-- Template for comment nodes -->
  <xsl:template match="comment()">
    <div class="fx-cmk">
      <xsl:text>&lt;!--</xsl:text>
      <span class="fx-com"><xsl:value-of select="."/></span>
      <xsl:text>--&gt;</xsl:text>
    </div>
  </xsl:template>
    
  <!-- Template for elements not handled elsewhere (leaf nodes) -->
  <xsl:template match="*">
    <div class="fx-block">
      <div>
        <xsl:text>&lt;</xsl:text>
        <xsl:call-template name="element-name"/>
        <xsl:call-template name="attributes"/>
        <xsl:text> /&gt;</xsl:text>
      </div>
    </div>
  </xsl:template>
  
  <!-- Template for elements with comment, pi and/or text children -->
  <xsl:template match="*[node()]">
    <div class="fx-block">
      <xsl:text>&lt;</xsl:text>
      <xsl:call-template name="element-name"/>
      <xsl:call-template name="attributes"/>
      <xsl:text>&gt;</xsl:text>
      <div>
        <xsl:apply-templates/>
        <div>
          <xsl:text>&lt;/</xsl:text>
          <xsl:call-template name="element-name"/>
          <xsl:text>&gt;</xsl:text>
        </div>
      </div>
    </div>
  </xsl:template>
  
  <!-- Template for elements with only text children -->
  <xsl:template match="*[text() and not(comment() or processing-instruction() )]">
    <div class="fx-block">
      <div style="margin-left:1em;text-indent:-2em">
        <xsl:text>&lt;</xsl:text>
        <xsl:call-template name="element-name"/>
        <xsl:call-template name="attributes"/>
        <xsl:text>&gt;</xsl:text>
        <xsl:choose>
          <xsl:when test="contains(text(), '&#xA;') or contains(text(), '&#xD;')">
            <pre class="fx-text">
              <xsl:value-of select="text()"/>
            </pre>
          </xsl:when>
          <xsl:otherwise>
            <span class="fx-text">
              <xsl:value-of select="text()"/>
            </span>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&lt;/</xsl:text>
        <xsl:call-template name="element-name"/>
        <xsl:text>&gt;</xsl:text>
      </div>
    </div>
  </xsl:template>
  
  <!-- Template for elements with element children -->
  <xsl:template match="*[*]">
    <div class="fx-block">
      <xsl:text>&lt;</xsl:text>
      <xsl:call-template name="element-name"/>
      <xsl:call-template name="attributes"/>
      <xsl:text>&gt;</xsl:text>
      <div>
        <xsl:apply-templates select="node()"/>
        <div>
          <xsl:text>&lt;/</xsl:text>
          <xsl:call-template name="element-name"/>
          <xsl:text>&gt;</xsl:text>
        </div>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template name="element-name">
    <span class="fx-elnm"><xsl:value-of select="name()"/></span>
  </xsl:template>
  
  <xsl:template name="attributes">
    <xsl:apply-templates select="@*"/>
    <xsl:variable name="current" select="current()"/>
    <xsl:for-each select="namespace::*">
      <xsl:if test="not($current/parent::*[namespace::*[. = current()]])">
        <span class="fx-ns">
          <xsl:text> xmlns</xsl:text>
          <xsl:if test="name() != ''">:</xsl:if>
          <xsl:value-of select="name()"/>
          <xsl:text>="</xsl:text>
          <span class="fx-nsval"><a href="{.}"><xsl:value-of select="."/></a></span>
          <xsl:text>"</xsl:text>
        </span>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
</xsl:stylesheet>
