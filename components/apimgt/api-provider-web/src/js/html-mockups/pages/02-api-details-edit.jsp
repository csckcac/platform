<div class="row-fluid">
    <div class="span12">
        <form class="form-horizontal well">
            <div class="control-group">
                <label class="control-label" for="textarea">Description:</label>

                <div class="controls">
                    <textarea class="input-xlarge" id="textarea" rows="3"
                              style="margin-left: 0px; margin-right: 0px; width: 501px; "></textarea>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="input01">Icon</label>

                <div class="controls">
                    <div><img src="images/tmpAPIIcons/api5.png" alt=""></div>
                    <a href="#"><i class="icon-picture"></i> Change Icon</a>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="input01">Endpoint URL:</label>

                <div class="controls">
                    <input type="text" class="input-xlarge" id="input01">

                    <p class="help-block">Ex:http://appserver/services/echo</p>
                </div>
            </div>


            <div class="control-group">
                <label class="control-label" for="input02">WSDL/WADL:</label>

                <div class="controls">
                    <input type="text" class="input-xlarge" id="input02">
                    <button class="btn" id="registryPicker" rel="tooltip" title="Pick From Registry" type="button"
                            data-toggle="modal" href="#myModal">..
                    </button>
                    <p class="help-block">Ex:http://appserver/services/echo?wsdl</p>
                </div>
            </div>


            <div class="control-group">
                <label class="control-label" for="input03">Tags:</label>

                <div class="controls">
                    <input type="text" class="input-xlarge" id="input03">
                </div>
            </div>


            <div class="control-group">
                <label class="control-label" for="select01">Tier Availability:</label>

                <div class="controls">
                    <select id="select01">
                        <option>Gold</option>
                        <option>Silver</option>
                    </select>

                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="select01">State:</label>

                <div class="controls">
                    <div class="btn-group">
                            <a class="btn btn-inverse dropdown-toggle" data-toggle="dropdown" href="#">
                                <i class="icon-resize-vertical icon-white"></i>Publish<span class="caret"></span></a>
                                <ul class="dropdown-menu">
                                    <li><a href="#">Un-publish</a></li>
                                    <li><a href="#">Deprecate</a></li>
                                    <li><a href="#">Retire</a></li>
                                </ul>
                        </div>

                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="select01"></label>

                <div class="controls">
                    <button class="btn btn-info" href="#"><i class="icon-edit"></i>Edit Documentation</button>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-primary" onclick="javascript:showTab('viewLink')">Save</button>
                <button class="btn">Cancel</button>
            </div>

        </form>
    </div>
    <!--span 11 -->
</div>