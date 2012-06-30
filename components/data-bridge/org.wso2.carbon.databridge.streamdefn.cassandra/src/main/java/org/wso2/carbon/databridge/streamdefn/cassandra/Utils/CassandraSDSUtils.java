package org.wso2.carbon.databridge.streamdefn.cassandra.Utils;

import me.prettyprint.cassandra.serializers.*;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.streamdefn.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.databridge.streamdefn.cassandra.datastore.DataType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CassandraSDSUtils {
    public static String convertStreamNameToCFName(String streamName) {
        if (streamName == null) {
            return null;
        }
        int keySpaceLength = CassandraConnector.BAM_EVENT_DATA_KEYSPACE.length();
        if ((streamName.length() + keySpaceLength) > 48) {
            throw new RuntimeException("The stream name you provided is too long. This has caused the" +
                    " generated key (\""+ streamName+"\") to go " +
                    "beyond the allowed characters. of "+ (48 - keySpaceLength) );
        }
        return streamName.replace(".", "_");
    }

    public static long getLong(ByteBuffer byteBuffer) throws IOException {
        return longSerializer.fromByteBuffer(byteBuffer);
    }

    public static String getString(ByteBuffer byteBuffer) throws IOException {
        return stringSerializer.fromByteBuffer(byteBuffer);
    }

    private final static StringSerializer stringSerializer = StringSerializer.get();
    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final static FloatSerializer floatSerializer = FloatSerializer.get();
    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();

    public static Object getOriginalValueFromColumnValue(ByteBuffer byteBuffer, AttributeType attributeType)
            throws IOException {
        switch (attributeType) {
            case BOOL: {
                return booleanSerializer.fromByteBuffer(byteBuffer);
            }
            case INT: {
                return integerSerializer.fromByteBuffer(byteBuffer);
            }
            case DOUBLE: {
                return doubleSerializer.fromByteBuffer(byteBuffer);
            }
            case FLOAT: {
                return floatSerializer.fromByteBuffer(byteBuffer);
            }
            case LONG: {
                return longSerializer.fromByteBuffer(byteBuffer);
            }
            case STRING: {
                return stringSerializer.fromByteBuffer(byteBuffer);
            }
        }
        return null;
    }




    public static String getColumnName(DataType dataType, Attribute attribute) {
        return dataType.name() + "_" + attribute.getName();
    }

    public static String createRowKey(long timestamp, UUID uuid) {
        return timestamp + "::" + uuid.toString();
    }
}
