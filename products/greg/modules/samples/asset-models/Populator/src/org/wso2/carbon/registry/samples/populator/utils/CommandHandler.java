package org.wso2.carbon.registry.samples.populator.utils;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private static final Map<String, String> inputs = new HashMap<String, String>();

    public static boolean setInputs(String[] arguments) {

        if (arguments.length == 0) {
            printMessage();
            return false;
        }
        if (arguments.length == 1 && arguments[0].equals("--help")) {
            printMessage();
            return false;
        }

        // now loop through the arguments list to capture the options
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-h")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Hostname of the registry is missing");
                }
                inputs.put("-h", arguments[++i]);

            } else if (arguments[i].equals("-p")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Port of the registry is missing");
                }
                inputs.put("-p", arguments[++i]);

            } else if (arguments[i].equals("-u")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Username of the admin is missing");
                }
                inputs.put("-u", arguments[++i]);

            } else if (arguments[i].equals("-pw")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Password of the admin is missing");
                }
                inputs.put("-pw", arguments[++i]);
            } else if (arguments[i].equals("-l")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Location of Model is missing");
                }
                inputs.put("-l", arguments[++i]);
            }  else if (arguments[i].equals("-cr")) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException("Context root of the service is missing");
                }
                inputs.put("-cr", arguments[++i]);
            }
        }

        return true;
    }


    private static void printMessage() {
        System.out.println("Usage: migration-client <options>");
        System.out.println("Valid options are:");
        System.out.println("\t-h :\t(Required) The hostname/ip of the registry to login.");
        System.out.println("\t-p :\t(Required) The port of the registry to login.");
        System.out.println("\t-u :\t(Required) The user name of the registry login.");
        System.out.println("\t-pw:\t(Required) The password of the registry login.");
        System.out.println();
        System.out.println("Example to migrate a registry running on localhost on default values");
        System.out.println("\te.g: migration-client -h localhost -p 9443 -u admin -pw admin");
    }

    public static String getRegistryURL() {
        String contextRoot = inputs.get("-cr");

        if (contextRoot == null) {
            return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/registry/";
        } else {
            return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/" + contextRoot + "/registry/";
        }
    }

    public static String getHost() {
        return inputs.get("-h");
    }

    public static String getServiceURL() {
        String contextRoot = inputs.get("-cr");

        if (contextRoot == null) {
            return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/services/";
        } else {
            return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/" + contextRoot + "/services/";
        }
    }

    public static String getHandlerJarLocation() {
        return inputs.get("-l")+"/target";
    }

    public static String getHandlerDef() {
        return inputs.get("-l")+"/handler-def/handlers.xml";
    }

    public static String getRxtFileLocation() {
        return inputs.get("-l")+"/registry-extensions";
    }

    public static String getUsername() {
        return inputs.get("-u");
    }

    public static String getPassword() {
        return inputs.get("-pw");
    }

    public static String getJRTemplateLocation(){
        return inputs.get("-l")+"/reporting-templates";
    }

    public static String getModelName(){
        return inputs.get("-l").split("/")[1];
    }
}

