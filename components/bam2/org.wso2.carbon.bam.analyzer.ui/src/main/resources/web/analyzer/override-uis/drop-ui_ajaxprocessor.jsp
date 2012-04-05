<div class="customForms">
    <span>Drop</span>

    <div class="shiftingLeft">
        <table id="mainAttributeTable">
            <tr>
                <td style="width:110px">Type</td>
                <td>
                <select onchange="updateDropUI(this)" id="dropTypeSelect">
                    <option value="group" selected="selected">Group</option>
                    <option value="row">Row</option>
                    <option value="column">Column</option>
                </select>
                </td>
            </tr>
        </table>

        <div id="fieldSetFormRow"><span>FieldSet <input type="button" class="button" value="Add Field Element" onclick="addFieldElementRow()" style="width: auto; margin-left: 100px;"></span>
            <table>
                <tr>
                    <td style="width:110px">Match using</td>
                    <td>
                    <select name="matchUsing" id="matchUsingSelector">
                        <option value="and">And</option>
                        <option value="or">OR</option>
                    </select>
                    </td>
                </tr>
            </table>
            <div class="shiftingLeft">
                <span>Fields</span>
                <table id="dropFieldTable">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>RegEx</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><input type="text" class="required" name="name"></td>
                            <td><input type="text" name="regex"></td>
                        </tr>
                    </tbody>
            </table>
            </div>
        </div>

         <div id="fieldSetFormColumn"><span>FieldSet <input type="button" class="button" value="Add Field Element" onclick="addFieldElementColumn()" style="width: auto; margin-left: 100px;"></span>
               <br/>
               <br/>
             <div class="shiftingLeft">
                <span>Fields</span>

                <table id="dropFieldTableColumn">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>RegEx</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><input type="text" class="required" name="name"></td>
                            <td><input type="text" name="regex"></td>
                        </tr>
                    </tbody>
            </table>
            </div>
        </div>

        <div id="groupSetForm">
            <span>GroupSet <input type="button" class="button" value="Add Groups" onclick="addGroupElement()" style="width: auto; margin-left: 100px;"></span>

            <div class="shiftingLeft">
                <table id="dropGroupTable">
                <tr>
                    <td>RegEx</td>
                    <td><input type="text" name="regex"></td>
                </tr>
            </table>
            </div>
        </div>

    </div>
</div>