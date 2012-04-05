/*
 * Copyright 2008 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function evalStrength(passwordInput, messageOutput, minLength) {

    var proposedPassword = $(passwordInput).value;

    var strength = 0;  // initial strength score

    // score higher for lower case
    var re = new RegExp("[a-z]");
    if (re.test(proposedPassword)) strength++;

    // score higher for upper case
    re = new RegExp("[A-Z]");
    if (re.test(proposedPassword)) strength++;

    // score higher for digits
    re = new RegExp("[0-9]");
    if (re.test(proposedPassword)) strength++;

    // score higher for punctuation
    re = new RegExp("[^A-Za-z0-9]");
    if (re.test(proposedPassword)) strength++;


    var report;
    if (proposedPassword.length == 0) {
      report = '';
    } else if (proposedPassword.length < minLength) {
      report = '<strong style="color:red">too short</strong>';
    } else if (strength <= 1) {
      report = '<strong style="color:orange">weak</strong>';
    } else if (strength <= 2) {
      report = '<strong style="color:blue">medium</strong>';
    } else {
      report = '<strong style="color:green">strong</strong>';
    }

    $(messageOutput).innerHTML  = report;
}

