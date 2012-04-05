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
this.serviceName = "fileTest";
this.documentation = "Testing File Host Object";

testFile.documentation = "Create, open, write, move, read and delete a file.";
testFile.inputTypes = {};
testFile.outputType = "String";
function testFile() {
    try {
        var text = "Hello World!";
        var file = new File("hello.txt");
        if (!file.exists) {
            file.createFile();
        }
        file.openForWriting();
        file.write(text);
        file.close();
        var moved = file.move("backup/goodbye.txt");
        if (moved) {
            var file2 = new File("backup/goodbye.txt");
            file2.openForReading();
            print(file2.readAll());
            file2.close();

            if (!file2.deleteFile()) {
                return "Unable to delete file.";
            }
        }
        return "Successfully created, opened, written, moved, read and deleted a file.";
    } catch (e) {
        return "Error occurred while tried to create, open, write, move, read or delete a file.";
    }
}

                        

                        

                        

                        