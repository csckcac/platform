<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%
    int requestCount=  (int) (5 + Math.random() *(46));
    int responseCount=(int) (5 + (Math.random() * (Math.random() * 46 + 1)));
    int faultCount= (int) (Math.random() * 46-( Math.random() * (Math.random() * 46 + 1)));
    int averageResponseTime= (int) (200 +( Math.random() * 501 + (Math.random() * ( Math.random() * (501-Math.random() * 501)) + 1)));
    int maximumResponseTime=(int) (200 + Math.random() * 501 + Math.random() * (501-Math.random() * 501) );
    int minimumResponseTime=(int) (200 + Math.random() * 501);
%>
<services>
    <service name="Service 01">
        <stats>
            <requestCount><%=requestCount%></requestCount>
            <responseCount><%=responseCount%></responseCount>
            <faultCount><%= faultCount %></faultCount>
            <averageResponseTime><%= averageResponseTime %></averageResponseTime>
            <maximumResponseTime><%= maximumResponseTime %></maximumResponseTime>
            <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
        </stats>
        <operations>
            <operation name="getCustomerID">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%= faultCount  %></faultCount>
                    <averageResponseTime><%=averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="canCustomerRate">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() *(46))%></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%= faultCount  %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetRating">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%= faultCount  %></faultCount>
                    <averageResponseTime><%=averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="updateRating">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="setRating">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
    <service name="Service 02">
        <stats>
            <requestCount><%=requestCount %></requestCount>
            <responseCount><%=responseCount%></responseCount>
            <faultCount><%=faultCount %></faultCount>
            <averageResponseTime><%=averageResponseTime%></averageResponseTime>
            <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
            <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
        </stats>
        <operations>
            <operation name="getCustomerID">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%=faultCount %></faultCount>
                    <averageResponseTime><%= averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="canCustomerRate">
                <stats>
                    <requestCount><%=requestCount%></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%= averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetRating">
                <stats>
                    <requestCount><%=requestCount%></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="updateRating">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%=faultCount  %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="setRating">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
    <service name="Service 03">
        <stats>
            <requestCount><%=requestCount %></requestCount>
            <responseCount><%=responseCount %></responseCount>
            <faultCount><%= faultCount %></faultCount>
            <averageResponseTime><%= averageResponseTime%></averageResponseTime>
            <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
            <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
        </stats>
        <operations>
            <operation name="findDNSServers">
                <stats>
                    <requestCount><%=requestCount %></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%= faultCount  %></faultCount>
                    <averageResponseTime><%=averageResponseTime %></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetDNSServer">
                <stats>
                    <requestCount><%=requestCount%></requestCount>
                    <responseCount><%=responseCount%></responseCount>
                    <faultCount><%=faultCount %></faultCount>
                    <averageResponseTime><%= averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="startDNSServer">
                <stats>
                    <requestCount><%=requestCount%></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%= faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
            <operation name="stopDNSServer">
                <stats>
                    <requestCount><%=requestCount%></requestCount>
                    <responseCount><%=responseCount %></responseCount>
                    <faultCount><%=faultCount %></faultCount>
                    <averageResponseTime><%=averageResponseTime%></averageResponseTime>
                    <maximumResponseTime><%=maximumResponseTime%></maximumResponseTime>
                    <minimumResponseTime><%=minimumResponseTime%></minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
</services>

