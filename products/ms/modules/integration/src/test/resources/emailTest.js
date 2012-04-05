/*
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
this.serviceName = "emailTest";
this.documentation = "Testing Email Host Object";

sendEmail.documentation = "Send a test e-mail.";
sendEmail.inputTypes = {};
sendEmail.outputType = "String";
function sendEmail() {
    try {
        var email = new Email("smtp.gmail.com", "mashupserver", "wso2wsas");
        var text = "Sample text for the attachment";
        var file = new File("attachment.txt");
        if (!file.exists) {
            file.createFile();
        }
        file.openForWriting();
        file.write(text);
        file.close();
        email.from = "mashupserver@gmail.com";
        email.to = "mashupserver@gmail.com";
        //email.cc = "cc@server.com";
        //email.bcc = "bcc@server.com";
        email.subject = "Subject of e-mail sent for testing Email Host Object";
        email.addAttachement(file);
        email.text = "This is an e-mail for testing Email Host Object";
        email.send();
        return "Successfully sent an e-mail.";
    } catch (e) {
        return "Error occurred while sending the e-mail."
    }
}

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        

                        
