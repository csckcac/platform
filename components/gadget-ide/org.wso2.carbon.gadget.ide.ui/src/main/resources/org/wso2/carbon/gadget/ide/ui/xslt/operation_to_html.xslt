<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:strip-space elements="question"/>

    <xsl:template match="/operation">
        <xsl:variable name="name">
            <xsl:call-template name="xml-name-to-javascript-name">
                <xsl:with-param name="name" select="@name"/>
            </xsl:call-template>
        </xsl:variable>

        <style>
            table.ops textarea.nonemptyfield {
            height: 1.7em;
            overflow-x:hidden;
            overflow-y:auto;
            margin:0px;
            border: 1px solid #CCCCCC;
            width: 15em;
            }
            table.ops textarea.emptyfield {
            height: 1.7em;
            color:#CCC;
            overflow-x:hidden;
            overflow-y:auto;
            margin:0px;
            border: 1px solid #CCCCCC;
            width: 15em;
            }
        </style>

        <script type="text/javascript">
            function restoreInput(e, hint) {
            var thisInput = e.target;
				if (thisInput.value == "") {
					thisInput.value = hint;
					thisInput.className = "emptyfield";
				}
            }

			function prepareInput(e) {
			    var thisInput = e.target;
				if (thisInput.className == "emptyfield") {
					thisInput.value = "";
					thisInput.className = "nonemptyfield";
				}
			}
        </script>
		<br/>
        <div class="params" id="params_{$name}">
            <table class="ops">
                <tr>
                    <td colspan="2">
                        <xsl:if test="documentation/node()">
                            <div class="operationDocumentation">
                                <xsl:copy-of select="documentation/node()"/>
                            </div>
                        </xsl:if>
                        <div id="resturl_{$name}" class="operationDocumentation"></div>
                    </td>
                </tr>
                <xsl:for-each select="signature/params/param">
                    <tr>
                        <xsl:choose>
                            <!-- this parameter represents expandable parameters -->
                            <xsl:when test="@token = '#any'">
                                <td class="label">
                                    <div>(additional parameters)</div>
                                </td>
                                <td class="param">
                                    <input type="text" id="input_{$name}_additionalParameters" class="emptyfield"
                                           value="xs:anyType" onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                           onblur="restoreInput(event,'(xs:anyType)')"/>
                                    <!-- TODO expandable fields of additional parameters -->
                                </td>
                            </xsl:when>

                            <!-- this parameter represents a boolean (checkbox) -->
                            <xsl:when test="@type = 'boolean'">
                                <td class="label">
                                    <xsl:value-of select="@name"/>
                                    <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                        <sub>(
                                            <xsl:value-of select="@minOccurs"/>..
                                            <xsl:choose>
                                                <xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="@maxOccurs"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            )
                                        </sub>
                                    </xsl:if>
                                </td>
                                <td class="param">
                                    <div id="arrayparams_{$name}_{@name}">
                                        <!-- first child is a hidden template for cloning additional array items -->
                                        <div style="display:none">
                                            <input type="checkbox" id="input_{$name}_{@name}_"
                                                   title="An [xs:boolean] value representing {@name}"
                                                   onchange="showRestTemplate()"/>
                                            <span class="typeannotation">(xs:boolean)</span>
                                        </div>
                                        <div>
                                            <input type="checkbox" id="input_{$name}_{@name}_0"
                                                   title="An [xs:boolean] value representing {@name}"
                                                   onchange="showRestTemplate()"/>
                                            <span class="typeannotation">(xs:boolean)</span>
                                        </div>
                                    </div>
                                    <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                        <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                        <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)"
                                               disabled="disabled"></input>
                                    </xsl:if>
                                </td>
                            </xsl:when>

                            <!-- this parameter represents a QName (separate namespace and QName fields) -->
                            <xsl:when test="@type = 'QName'">
                                <td class="label">
                                    <xsl:value-of select="@name"/>
                                    <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                        <sub>(
                                            <xsl:value-of select="@minOccurs"/>..
                                            <xsl:choose>
                                                <xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="@maxOccurs"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            )
                                        </sub>
                                    </xsl:if>
                                </td>
                                <td class="param">
                                    <div id="arrayparams_{$name}_{@name}">
                                        <!-- first child is a hidden template for cloning additional array items -->
                                        <div style="display:none">
                                            <textarea id="input_{$name}_{@name}_ns_" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'(namespace URI)')"
                                                      title="The [namespace URI] value corresponding to the xs:QName">
                                                (namespace URI)
                                            </textarea>
                                            <xsl:text> </xsl:text>
                                            <textarea id="input_{$name}_{@name}_" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'(xs:QName)')"
                                                      title="An [xs:QName] value representing prefix and localName of {@name}">
                                                (xs:QName)
                                            </textarea>
                                        </div>
                                        <div>
                                            <textarea id="input_{$name}_{@name}_ns_0" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'(namespace URI)')"
                                                      title="The [namespace URI] value corresponding to the xs:QName">
                                                (namespace URI)
                                            </textarea>
                                            <xsl:text> </xsl:text>
                                            <textarea id="input_{$name}_{@name}_0" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'(xs:QName)')"
                                                      title="An [xs:QName] value representing prefix and localName of {@name}">
                                                (xs:QName)
                                            </textarea>
                                        </div>
                                    </div>
                                    <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                        <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                        <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)"
                                               disabled="disabled"></input>
                                    </xsl:if>
                                </td>
                            </xsl:when>

                            <!-- this parameter represents an enumeration (<select>) -->
                            <xsl:when test="enumeration">
                                <td class="label">
                                    <xsl:value-of select="@name"/>
                                </td>
                                <td class="param">
                                    <select id="input_{$name}_{@name}_0" onchange="showRestTemplate()">
                                        <xsl:for-each select="enumeration">
                                            <option value="{@value}">
                                                <xsl:value-of select="@value"/>
                                            </option>
                                        </xsl:for-each>
                                    </select>
                                </td>
                            </xsl:when>

                            <!-- this parameter represents a type exposed as a <textarea> -->
                            <xsl:otherwise>
                                <xsl:variable name="prefix">
                                    <xsl:if test="@type-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:</xsl:if>
                                </xsl:variable>
                                <xsl:variable name="restriction">
                                    <xsl:if test="@restriction-of">
                                        <xsl:if test="@restriction-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:
                                        </xsl:if>
                                        <xsl:value-of select="@restriction-of"/>
                                        <xsl:text> restriction</xsl:text>
                                    </xsl:if>
                                </xsl:variable>
                                <xsl:variable name="type">
                                    <xsl:choose>
                                        <xsl:when test="@type">
                                            <xsl:value-of select="@type"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="../@wrapper-element"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <td class="label">
                                    <div>
                                        <xsl:value-of select="@name"/>
                                        <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                            <sub>(
                                                <xsl:value-of select="@minOccurs"/>..
                                                <xsl:choose>
                                                    <xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="@maxOccurs"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                )
                                            </sub>
                                        </xsl:if>
                                    </div>
                                </td>
                                <td class="param">
                                    <div id="arrayparams_{$name}_{@name}">
                                        <!-- first child is a hidden template for cloning additional array items -->
                                        <div style="display:none">
                                            <textarea id="input_{$name}_{@name}_" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'({$prefix}{$type}{$restriction})')"
                                                      title="A [{$prefix}{$type}{$restriction}] value representing the {@name}">(<xsl:value-of select="$prefix"/><xsl:value-of select="$type"/><xsl:value-of select="$restriction"/>)</textarea>
                                        </div>
                                        <div>
                                            <textarea id="input_{$name}_{@name}_0" class="emptyfield"
                                                      onkeyup="showRestTemplate()" onfocus="prepareInput(event)" 
                                                      onblur="restoreInput(event,'({$prefix}{$type}{$restriction})')"
                                                      title="A [{$prefix}{$type}{$restriction}] value representing the {@name}">(<xsl:value-of select="$prefix"/><xsl:value-of select="$type"/><xsl:value-of select="$restriction"/>)</textarea>
                                        </div>
                                    </div>
                                    <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                        <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                        <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)"
                                               disabled="disabled"></input>
                                    </xsl:if>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>
                </xsl:for-each>
            </table>
        </div>


    </xsl:template>
    <xsl:template name="xml-name-to-javascript-name">
        <xsl:param name="name"/>
        <xsl:value-of select="translate($name,'.-/','___')"/>
    </xsl:template>

</xsl:stylesheet>

