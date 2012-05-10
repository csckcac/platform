package org.wso2.carbon.hive.storage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.Constants;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class JDBCDataSerDe implements SerDe {


    static final String HIVE_TYPE_DOUBLE = "double";
    static final String HIVE_TYPE_FLOAT = "float";
    static final String HIVE_TYPE_BOOLEAN = "boolean";
    static final String HIVE_TYPE_BIGINT = "bigint";
    static final String HIVE_TYPE_TINYINT = "tinyint";
    static final String HIVE_TYPE_SMALLINT = "smallint";
    static final String HIVE_TYPE_INT = "int";

    private final MapWritable cachedWritable = new MapWritable();

    private StructObjectInspector objectInspector;
    private int fieldCount;
    private List<String> columnNames;
    String[] columnTypesArray;
    
    public void initialize(Configuration entries, Properties properties) throws SerDeException {

        String tableColumnNamesString = properties.getProperty(Constants.LIST_COLUMNS);

        if (tableColumnNamesString != null) {
            
            String[] columnNamesArray = tableColumnNamesString.split(",");

/*            if (log.isDebugEnabled()) {
                log.debug("table column string: " + tblColumnNamesStr);
            }*/
            fieldCount = columnNamesArray.length;
            columnNames = new ArrayList<String>(columnNamesArray.length);
            columnNames.addAll(Arrays.asList(columnNamesArray));
            
            String columnTypesString= properties.getProperty(Constants.LIST_COLUMN_TYPES);
            
            columnTypesArray = columnTypesString.split(":");

            final List<ObjectInspector> fieldObjectInspectors = new ArrayList<ObjectInspector>(columnNamesArray.length);
            
            for (int i=0; i < columnNamesArray.length; i++){

                if(HIVE_TYPE_INT.equalsIgnoreCase(columnTypesArray[i])){
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
                } else if (HIVE_TYPE_SMALLINT.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaShortObjectInspector);
                } else if (HIVE_TYPE_TINYINT.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaByteObjectInspector);
                } else if (HIVE_TYPE_BIGINT.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
                } else if (HIVE_TYPE_BOOLEAN.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaBooleanObjectInspector);
                } else if (HIVE_TYPE_FLOAT.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaFloatObjectInspector);
                } else if (HIVE_TYPE_DOUBLE.equalsIgnoreCase(columnTypesArray[i])) {
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector);
                } else {
                    // treat as string
                    fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
                }
            }

            objectInspector = ObjectInspectorFactory
                    .getStandardStructObjectInspector(columnNames, fieldObjectInspectors);

        } else {
            throw new SerDeException("Can't find table column definitions");
        }
    }

    public Class<? extends Writable> getSerializedClass() {
        return null;
    }

    public Writable serialize(Object o, ObjectInspector objectInspector) throws SerDeException {
        if (objectInspector.getCategory() != ObjectInspector.Category.STRUCT) {
            throw new SerDeException(getClass().toString()
                    + " can only serialize struct types, but we got: "
                    + objectInspector.getTypeName());
        }

        // Prepare the field ObjectInspectors
        StructObjectInspector structObjectInspector = (StructObjectInspector) objectInspector;

        final List<? extends StructField> fields = structObjectInspector.getAllStructFieldRefs();
        if (fields.size() != columnNames.size()) {
            throw new SerDeException(String.format("Required %d columns, received %d.", columnNames.size(), fields.size()));
        }
        //Work in progress

        return null;
    }

    public Object deserialize(Writable writable) throws SerDeException {
        return null;
    }

    public ObjectInspector getObjectInspector() throws SerDeException {
        return objectInspector;
    }

    public SerDeStats getSerDeStats() {
        return null;
    }
}
