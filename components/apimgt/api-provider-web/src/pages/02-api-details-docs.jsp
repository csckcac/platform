<div class="row-fluid">
    <div class="span2">
        <jsp:include page="02-api-details-info.jsp" />
    </div>
    <div class="span10">
        <div class="row row-fluid">
            <div class="control-group">
                <a onclick="javascript:$('#newDoc').toggle('slow')" href="#"><i class=" icon-plus-sign"></i> Add New Document</a>
            </div>
        </div><!-- Row -->
        <div class="row row-fluid" id="newDoc" style="display:none;">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label" for="input01">Name</label>

                    <div class="controls">
                        <input type="text" class="input-xlarge" id="input01">
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="textarea">Summary</label>

                    <div class="controls">
                        <textarea class="input-xlarge" id="textarea" rows="3"></textarea>
                    </div>
                </div>
            </div>
            <div class="span4">
                <div class="well">
                    <div class="control-group">
                        <label class="control-label">Type</label>
                        <div class="controls">
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios1" value="option1" checked="">
                            How To
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios2" value="option2">
                            Samples & SDK
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios3" value="option3">
                            Public Forum
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios4" value="option4">
                            Support Forum
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios5" value="option5">
                            API Message Formats
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios" id="optionsRadios6" value="option5" onclick="javascript:$('#specifyBox').toggle()">
                            Other (specify)
                          </label>
                          <input type="text" id="specifyBox" style="display:none;" />
                        </div>
                      </div>
                  </div>
            </div>
            <div class="span4">
                <div class="well">
                    <div class="control-group">
                        <label class="control-label">Source</label>
                        <div class="controls">
                          <label class="radio">
                            <input type="radio" name="optionsRadios1" id="optionsRadios7" value="option1" checked="">
                            In-line
                          </label>
                          <label class="radio">
                            <input type="radio" name="optionsRadios1" id="optionsRadios8" value="option2">
                            URL
                          </label>
                        </div>
                      </div>
                  </div>
            </div>
            <div class="span12">
                <div class="control-group">
                <button type="submit" class="btn btn-primary">Add Document</button>
                <button class="btn">Cancel</button>
                </div>
            </div>
        </div><!-- Row -->
        <div class="row row-fluid">
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Content</th>
                    <th>Modified By</th>
                    <th>Modified On</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <tr>
                <td><a href="#"><i class="icon-file"></i>Doc1</a></td>
                <td>HOWTO</td>
                <td><a href="#"><i class="icon-file"></i>View</a></td>
                <td><a href="?place=user"><i class="icon-user"></i>Sumedha</a></td>
                <td>11/11/10</td>
                <td>
                    <a href="#"><i class="icon-edit"></i>Update</a>
                    <a href="#"><i class="icon-trash"></i>Delete</a>
                    <a href="#"><i class="icon-share"></i>Copy</a>
                </td>
            </tr>
            <tr>
                <td><a href="#"><i class="icon-file"></i>Doc2</a></td>
                <td>HOWTO</td>
                <td><a href="#"><i class="icon-file"></i>View</a></td>
                <td><a href="?place=user"><i class="icon-user"></i>Chanaka</a></td>
                <td>12/03/12</td>
                <td>
                    <a href="#"><i class="icon-edit"></i>Update</a>
                    <a href="#"><i class="icon-trash"></i>Delete</a>
                    <a href="#"><i class="icon-share"></i>Copy</a>
                </td>
            </tr>
            </tbody>
        </table>

        </div><!-- Row -->
    </div>
</div>
<!-- Row -->
