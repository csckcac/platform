package org.wso2.carbon.hostobjects.jaggeryparser;

import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.io.*;

public class JaggeryParser {

    /**
     * Main Parser to process the .jss script
     *
     * @param is script as the input stream
     * @throws ScriptException If an error occurred during the script parsing
     */
    public static InputStream parse(InputStream is) throws ScriptException {
        try {
            boolean opened = false;
            boolean isExpression = false;
            String str;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream source = new PrintStream(output);
            StringBuilder html = new StringBuilder();
            StringBuilder jsExp = new StringBuilder();
            int ch = is.read();
            while (ch != -1) {
                if (ch == '<') {
                    ch = is.read();
                    if (ch == '%') {
                        opened = true;
                        str = html.toString();
                        //as it is html, we can avoid adding empty print("") calls
                        if (!str.equals("")) {
                            source.append("print(\"").append(str).append("\");");
                            html = new StringBuilder();
                        }
                        ch = is.read();
                        if (ch == '=') {
                            isExpression = true;
                        } else {
                            continue;
                        }
                    } else {
                        if (opened) {
                            if (isExpression) {
                                jsExp.append("<");
                            } else {
                                source.append("<");
                            }
                        } else {
                            html.append('<');
                        }
                        continue;
                    }
                } else if (ch == '%') {
                    ch = is.read();
                    if (ch == '>') {
                        opened = false;
                        if (isExpression) {
                            isExpression = false;
                            //if it need, we can validate "jsExp" here or let the compiler to do it.
                            source.append("print(").append(jsExp).append(");");
                            jsExp = new StringBuilder();
                        }
                    } else {
                        if (opened) {
                            source.append('%');
                        } else {
                            html.append('%');
                        }
                        continue;
                    }
                } else {
                    if (opened) {
                        if (isExpression) {
                            jsExp.append((char) ch);
                        } else {
                            source.append((char) ch);
                        }
                    } else {
                        if (ch == '"') {
                            html.append('\\').append('\"');
                        } else if (ch == '\\') {
                            html.append('\\').append('\\');
                        } else if (ch == '\r') {
                            html.append('\\').append('r');
                        } else if (ch == '\n') {
                            source.append("print(\"").append(html.toString()).
                                    append('\\').append('n').append("\");").append('\n');
                            html = new StringBuilder();
                        } else if (ch == '\t') { // Not sure we need this
                            html.append('\\').append('t');
                        } else {
                            html.append((char) ch);
                        }
                    }
                }
                ch = is.read();
            }
            str = html.toString();
            if (!str.equals("")) {
                source.append("print(\"").append(str).append("\");");
            }
            str = jsExp.toString();
            if (!str.equals("")) {
                source.append("print(").append(str).append(");");
            }
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }
}
