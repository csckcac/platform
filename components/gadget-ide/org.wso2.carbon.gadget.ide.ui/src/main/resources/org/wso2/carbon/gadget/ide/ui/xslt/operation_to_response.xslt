<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:exslt="http://exslt.org/common">

    <xsl:import href="xml-to-string.xslt"/>
    <xsl:output method="text"/>

    <xsl:param name="e4x" select="false()"/>
    <xsl:param name="localhost-endpoints" select="false()"/>

    <xsl:template match="/">
        <xsl:apply-templates select="operation"/>
    </xsl:template>

    <xsl:template match="operation">
    <xsl:variable name="payload-xml">
        <xsl:call-template name="payload-xml">
            <xsl:with-param name="params" select="signature/returns"/>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="payload">
        <xsl:choose>
            <xsl:when test="$payload-xml != ''">
					<xsl:call-template name="xml-to-string">
						<xsl:with-param name="node-set" select="exslt:node-set($payload-xml)"/>
					</xsl:call-template>
				</xsl:when>
            <xsl:otherwise><error>empty payload</error></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
        <xsl:value-of select="$payload"/>

</xsl:template>

<xsl:template name="localnameify-url">
    <xsl:param name="url"/>
    <xsl:variable name="scheme" select="substring-before($url, '://')"/>
    <xsl:choose>
        <xsl:when test="$localhost-endpoints and ($scheme='http' or $scheme='https')">
            <xsl:value-of select="$scheme"/>
            <xsl:text>://localhost</xsl:text>
            <xsl:variable name="remainder" select="substring-after($url, concat($scheme,'://'))"/>
            <xsl:variable name="domain" select="substring-before($remainder,'/')"/>
            <xsl:if test="contains($domain,':')">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="substring-after($domain, ':')"/>
            </xsl:if>
            <xsl:text>/</xsl:text>
            <xsl:value-of select="substring-after($remainder, '/')"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$url"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="payload-xml">
    <xsl:param name="params"/>
    <xsl:if test="$params/*">
        <xsl:element name="p:{$params/@wrapper-element}" namespace="{$params/@wrapper-element-ns}">
            <xsl:call-template name="infer-types">
                <xsl:with-param name="params" select="$params"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:if>
</xsl:template>

<xsl:template name="infer-types">
        <xsl:param name="params"/>
        <xsl:variable name="attributes" select="$params/child::node()[@attribute = 'yes']"/>
        <xsl:variable name="elements" select="$params/child::node()[not(@attribute) or @attribute != 'yes']"/>
        <xsl:for-each select="$attributes">
            <xsl:choose>
                <xsl:when test="@targetNamespace">
                    <xsl:attribute name="{@name}" namespace="{@targetNamespace}">?</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="{@name}">?</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="$elements">
            <xsl:variable name="lname">
                <xsl:choose>
                    <xsl:when test="@targetNamespace and @type-prefix and @type-prefix != ''">
                        <xsl:value-of select="concat(@type-prefix, ':', @name)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@name"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="local-name() = 'param'">
                    <xsl:choose>
                        <xsl:when test="@recursive = 'yes'">
                            <xsl:call-template name="occurrence-comment">
                                <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                <xsl:with-param name="minOccurs" select="@minOccurs"/>
                            </xsl:call-template>
                            <xsl:element name="{$lname}" namespace="{@targetNamespace}">?</xsl:element>
                        </xsl:when>
                        <xsl:when test="@token = '#return'">
                            <xsl:choose>
                                <xsl:when test="@simple = 'yes'">
                                    <xsl:call-template name="occurrence-comment">
                                        <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                        <xsl:with-param name="minOccurs" select="@minOccurs"/>
                                    </xsl:call-template>
                                    <xsl:element name="{$lname}" namespace="{@targetNamespace}"><xsl:attribute name="multiple">
											<xsl:choose>
												<xsl:when test="@maxOccurs != '1'">true</xsl:when>
												<xsl:otherwise>false</xsl:otherwise>
											</xsl:choose>
									</xsl:attribute>?</xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="occurrence-comment">
                                        <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                        <xsl:with-param name="minOccurs" select="@minOccurs"/>
                                    </xsl:call-template>
                                    <xsl:element name="{$lname}" namespace="{@targetNamespace}">
										<xsl:attribute name="multiple">
											<xsl:choose>
												<xsl:when test="@maxOccurs != '1'">true</xsl:when>
												<xsl:otherwise>false</xsl:otherwise>
											</xsl:choose>
										</xsl:attribute>
                                        <xsl:choose>
                                            <xsl:when test="count(.//child::node()) > 0">
                                                <xsl:call-template name="infer-types">
                                                    <xsl:with-param name="params" select="."/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>?</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="@token = '#any'">
                            <xsl:comment>You may enter ANY elements at this point</xsl:comment>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:message>TODO:
                                <xsl:value-of select="@token"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="local-name() = 'inherited-content'">
                    <xsl:choose>
                        <xsl:when test="@recursive = 'yes'">
                            <xsl:comment>Content of type "<xsl:value-of select="@extension"/>" which has a recursive type definition goes here</xsl:comment>

                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="infer-types">
                                <xsl:with-param name="params" select="."/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="occurrence-comment">
        <xsl:param name="maxOccurs"/>
        <xsl:param name="minOccurs"/>
        <xsl:choose>
            <xsl:when test="$minOccurs = 'unbounded' and $maxOccurs = 'unbounded'">
                <xsl:comment>0 or more occurrences</xsl:comment>
            </xsl:when>
            <xsl:when test="$maxOccurs = 'unbounded'">
                <xsl:comment><xsl:value-of select="$minOccurs"/> or more occurrences</xsl:comment>
            </xsl:when>
            <xsl:when test="$minOccurs = $maxOccurs">
                <xsl:comment>Exactly <xsl:value-of select="$minOccurs"/> occurrence<xsl:if test="$minOccurs != '1'">s</xsl:if></xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment><xsl:value-of select="$minOccurs"/> to <xsl:value-of select="$maxOccurs"/> occurrence<xsl:if test="$maxOccurs != '1'">s</xsl:if></xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

