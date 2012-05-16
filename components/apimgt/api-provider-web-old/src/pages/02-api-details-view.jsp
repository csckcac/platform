<div class="row-fluid">
    <div class="span2">
        <jsp:include page="02-api-details-info.jsp" />
    </div>
    <div class="span6">
        <div class="well">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce suscipit porta leo vitae pellentesque. In
            volutpat viverra tortor id iaculis. Cras eleifend, risus ut eleifend porttitor, risus dolor tincidunt magna,
            eu elementum sapien dui eget elit. Nullam et nisl ac velit molestie pulvinar sed quis elit. Vestibulum non
            malesuada sem. Praesent rutrum sagittis iaculis. Quisque blandit, lacus ut tincidunt egestas, purus elit
            rhoncus nunc, vel venenatis purus nisl at magna. Phasellus vitae sem diam. Donec imperdiet, velit sodales
            rutrum malesuada, nibh lacus vehicula lorem, id sagittis enim turpis et enim. Duis pharetra laoreet lorem
            sit amet euismod.
        </div>
        <table class="table table-bordered">
            <tbody>
            <tr>
                <td>Endpoint URL</td>
                <td>http://appserver/services/echo</td>
            </tr>
            <tr>
                <td>WSDL/WADL</td>
                <td>http://appserver/services/echo?wsdl</td>
            </tr>
            <tr>
                <td>Date Last Updated</td>
                <td>02/02/2012</td>
            </tr>
            <tr>
                <td>Tier Availability</td>
                <td>Gold | Silver</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="span4">
        <div id="chart1"></div>
    </div>
</div>
<!-- Row -->

<div class="row-fluid">
    <form class="well" action="?place=" id="copyToVersion" style="display:none">
        <label>To Version</label>
        <input type="text" class="span3" > <span class="help-inline">Ex:v1.0.1</span>
        <div><button type="submit" class="btn btn-primary">Done</button>
            <button type="button" class="btn" onclick="javascript:$('#copyToVersion').toggle('slow');$('#copyButtonContainer').toggle()">Cancel</button></div>
      </form>
    <div class="form-actions" id="copyButtonContainer">
        <button type="submit" class="btn" onclick="javascript:$('#copyToVersion').toggle('slow');$('#copyButtonContainer').toggle()">Copy</button>
    </div>
</div>