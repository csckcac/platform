<div class="customForms">
    <span>get<input type="button" class="button" id="addIndexBtn" value="Add Index Element" style="width: auto; margin-left: 100px;" /></span>

    <div class="shiftingLeft">
        <table id="mainAttributeTable">
            <tr><td>name</td><td><input type="text" class="required" name="name" id="getName" /></td></tr>
            <tr><td>batchSize</td><td><input type="text" name="batchSize" id="batchSize"></td></tr>
        </table>
        <div id="indexForm" style="display: none;"><span>Index</span>
            <table id="indexFormTable"></table>    
        </div>
        <div class="optionalElements">
            <input type="checkbox" value="granularity" id="granularity_checkbox">
            <label for="granularity_checkbox">Granularity</label>
            
            <input type="checkbox" value="groupBy" id="groupBy_checkbox">
            <label for="groupBy_checkbox">Group By</label>
        </div>
        <div id="granularityForm" style="display:none"><span>granularity</span>
            <table>
                <tr>
                    <td>index</td>
                    <td><input type="text" class="required" name="index" id="granularityColumn"></td>
                </tr>
                <tr>
                    <td>type</td>
                    <td><input type="text" class="required" name="type" id="granularityType"></td></tr>
            </table>
        </div>
        <div id="groupByForm" style="display: none;"><span>groupBy</span>
            <table>
                <tr>
                    <td>index</td>
                    <td><input type="text" class="required" name="index" id="groupByColumn"></td></tr>
            </table>
        </div>

    </div>
</div>