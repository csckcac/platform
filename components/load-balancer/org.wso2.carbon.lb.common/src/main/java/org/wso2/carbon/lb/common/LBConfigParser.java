package org.wso2.carbon.lb.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class LBConfigParser {

    private static Log log = LogFactory.getLog(LBConfigParser.class);


    /**
     * Convert given configuration file to a single String
     *
     * @param configFileName - file name to convert
     * @return String with complete lb configuration
     * @throws FileNotFoundException 
     */
    public String createLBConfigString(String configFileName) throws FileNotFoundException {
        StringBuilder lbConfigString = new StringBuilder("");

        File configFile = new File(configFileName);
        Scanner scanner;

        scanner = new Scanner(configFile);

        while (scanner.hasNextLine()) {
            lbConfigString.append(scanner.nextLine().trim() + "\n");
        }

        return lbConfigString.toString().trim();
    }
    
    public String createLBConfigString(InputStream configFileName) throws IOException {
       
        // read the stream with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(configFileName));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        
        return createLBConfigString(sb.toString());
    }


    /**
     * This method will read the whole config file, line by line and drag only the part(tag) under given element Name
     * return String - sub string from config file which is under services tag
     *
     * @param configFileName - name of config file
     * @param elementName    - element to drag out from the config file
     * @return String content under given elementName
     */
    public String dragConfigTagFromFile(String configFileName, String elementName) {
        File configFile = new File(configFileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // create a stack to check syntax
        Stack<String> stack = new Stack<String>();
        String servicesContentSubString = "";
        boolean insideServicesTag = false;

        if (scanner == null) {
            log.warn("Can not read load balance configuration file. Load balance configuration will not be available !!!");
            return null;

        }

        while (scanner.hasNextLine()) {
            String lineContent = scanner.nextLine();

            // if within service tag then add that line to servicesContentSubString
            if (insideServicesTag) {

                // If the stack is empty then this line is outside services tag.
                // So don't add it to servicesContentSubString and return, because  servicesContentSubString is already found
                if (stack.empty()) {
                    //insideServicesTag = false;
                    break;
                } else {
                    // do not add last "}" to servicesContentSubString
                    if (!(stack.size() == 1 && lineContent.contains("}"))) {
                        servicesContentSubString = servicesContentSubString + lineContent;
                    }
                }

                if (lineContent.contains("{")) {
                    stack.push("{");
                }
                if (lineContent.contains("}")) {
                    // then pop one element from stack
                    stack.pop();
                }

            } else { // not within servicesContentSubString

                if (lineContent.trim().contains(elementName)) {
                    //make insideServicesTag flag true
                    insideServicesTag = true;

                    // if this line contains { (this is the delimiter to detect configuration), then push it to stack
                    if (lineContent.contains("{")) {
                        stack.push("{");
                    }

                } else {
                    // this is something outside services tag. Just ignore them
                }
            }

        }
        return servicesContentSubString;
    }


    /**
     * This method will read the given services tag and create map of separate services
     *
     * @param servicesContentSubString string to process
     * @return Map with service list
     */
    public static HashMap<String, String> separateServices(String servicesContentSubString) {

        HashMap<String, String> serviceMap = new HashMap<String, String>();

        // create a stack to check syntax
        Stack<Character> stack = new Stack<Character>();

        ArrayList<Integer> startContentIndex = new ArrayList<Integer>();
        ArrayList<Integer> endContentString = new ArrayList<Integer>();

        ArrayList<String> serviceNamesList = new ArrayList<String>();

        // check from beginning of the services string for "{"
        for (int i = 0; i < servicesContentSubString.length(); i++) {
            char ch = servicesContentSubString.charAt(i);


            // found "{" and pop in to stack
            if (ch == '{') {

                // If stack size is empty in this point, then this is new service element
                if (stack.empty()) {
                    // find the name of new service
                    String stringWithServiceName = servicesContentSubString.substring(0, i).trim();
                    String[] arrayWithServiceName = stringWithServiceName.split(" ");
                    // last element of array will give the service name
                    String serviceName = arrayWithServiceName[arrayWithServiceName.length - 1];

                    serviceNamesList.add(serviceName);

                    // find the content related to this service
                    //String stringWithServiceContent = servicesContentSubString.substring(i, servicesContentSubString.length()).trim();

                    // set start content index (this is the starting index for service content)
                    //contentCounter = contentCounter + 1;
                    startContentIndex.add(i);

                }

                stack.push(ch);

            } else if (ch == '}') {
                // found a closing tag. so remove one element from stack
                stack.pop();

                // if stack is empty this point, then take the index to get the service content
                if (stack.empty()) {
                    endContentString.add(i);

                }
            }
        }

        Iterator iterator1 = startContentIndex.iterator();
        Iterator iterator2 = endContentString.iterator();

        Iterator iterator3 = serviceNamesList.iterator();

        while (iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext()) {
            int startIndex = (Integer) iterator1.next();
            int endIndex = (Integer) iterator2.next();

            String valueServiceContent = servicesContentSubString.substring(startIndex + 1, endIndex);
            String keyServiceName = (String) iterator3.next();

            serviceMap.put(keyServiceName, valueServiceContent);

        }

        return serviceMap;

    }


    /**
     * Search for the given configuration element from given configContent string and return the value
     *
     * @param configContent -   String to read for the properties
     * @param elementName   - Element name to search
     * @return String which contains the content of the given element
     */
    public String getConfigElementFromString(String configContent, String elementName) {

        String returnElementValue = null;
        int endElementCounter = 0;

        // create a stack to check syntax
        Stack<Character> stack = new Stack<Character>();

        // split the configContent from given elementName to get the required content
        // config content can have multiple occurrences of elementName

        String[] neededConfigContent = configContent.split(elementName, 2);

        if (neededConfigContent != null && neededConfigContent.length > 1) {

            String configSubString = neededConfigContent[1].trim();

            // check from beginning of the services string for "{"
            for (int i = 0; i < configSubString.length(); i++) {
                char ch = configSubString.charAt(i);

                // found "{" and push in to stack
                if (ch == '{') {
                    stack.push(ch);
                }
                // found "}" and pop the stack
                else if (ch == '}') {
                    stack.pop();
                    if (stack.empty()) {
                        // if stack become empty after above pop operation
                        // that means we have processed whole element.
                        endElementCounter = i;
                        break;

                    }
                }

            }

            returnElementValue = configSubString.trim().substring(1, endElementCounter).trim();

        }

        return returnElementValue;
    }


    /**
     * List outer most properties with the given propertyName defined by the given configContent string
     * There can be multiple values for a given propertyName. Multiple values are detected with character ','
     * End of the given property identified by  character ' ; '
     *
     * @param configContent - String to read for the properties
     * @param propertyName  - Property name to search
     * @return ArrayList with identified property values
     */
    public ArrayList<String> getConfigPropertyFromString(String configContent, String propertyName) {

        int startElementCounter = 0;
        ArrayList<String> propertyValueList = new ArrayList<String>();

        // split the configContent from given propertyName to get the required content

        String[] neededConfigContent = configContent.split(propertyName, 2);

        if (neededConfigContent != null && neededConfigContent.length > 1) {

            String configSubString = neededConfigContent[1].trim();

            // check from beginning of the configSubString until finding the '  ; '  delimiter
            for (int i = 0; i < configSubString.length(); i++) {
                char ch = configSubString.charAt(i);

                // use "," to separate property values
                if (ch == ',') {
                    String propertyValue = configSubString.substring(startElementCounter, i).trim();
                    propertyValueList.add(propertyValue);
                    startElementCounter = i + 1;
                }

                // use " ; " to detect the end of property
                if (ch == ';') {
                    String propertyValue = configSubString.substring(startElementCounter, i).trim();
                    propertyValueList.add(propertyValue);
                    break;
                }

            }

        }
        return propertyValueList;

    }


    /**
     * Get all top level config elements form a given configuration content string
     * This will consider outer elements identified by { } (This will not loop through inner elements)
     *
     * @param configContent - to search for configuration elements
     * @return HashMap (configuration element name as the key and content as the value)
     */
    public HashMap<String, String> getAllTopLevelConfigElements(String configContent) {
        Stack<Character> stack = new Stack<Character>();

        int startIndex = 0;

        //HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        //ArrayList<String> arrayList = new ArrayList<String>();

        String elementName;
        String elementValue;

        // Nothing on stack. So find first element
        for (int i = 0; i < configContent.length(); i++) {
            char ch = configContent.charAt(i);

            // found first outer element
            if (ch == '{') {

                // push this to stack
                stack.push('{');

                // split string from this point
                String firstHalf = configContent.substring(startIndex, i).trim();
                String secondHalf = configContent.substring(i + 1, configContent.length());

                // split first half from space to detect the element name
                String firstHalfArray[] = firstHalf.split(" ");
                elementName = firstHalfArray[firstHalfArray.length - 1];

                // go through second half of the string to detect the value of element
                for (int j = 0; j < secondHalf.length(); j++) {
                    char ch2 = secondHalf.charAt(j);

                    // if { , then push it
                    if (ch2 == '{') {
                        stack.push('{');

                    } else if (ch2 == '}') {
                        stack.pop();

                        // In this step, if the stack becomes empty then element value is already identified
                        // next time start from i+j th element
                        if (stack.empty()) {
                            elementValue = secondHalf.substring(0, j);
                            //arrayList.add(elementValue);

                            hashMap.put(elementName, elementValue);

                            startIndex = i + j;
                            i = i + j;
                            break;
                        }

                    }
                }

            }

        }
        return hashMap;

    }


}



