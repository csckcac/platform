/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.util;

public class BAMMath {
    public static double max(double[] doubles) {
        if (doubles.length == 0) return 0;

        double max = doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if (max < doubles[i]) {
                max = doubles[i];
            }
        }
        return max;
    }

    public static double min(double[] doubles) {
        if (doubles.length == 0) return 0;

        double min = doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if (min > doubles[i]) {
                min = doubles[i];
            }
        }
        return min;
    }

    public static double avg(double[] doubles) {
        if (doubles.length == 0) return 0;

        double sum = 0;
        for (int i = 0; i < doubles.length;) {
            sum += doubles[i++];
        }
        return sum / doubles.length;
    }



    public static int max(int[] ints) {
        if (ints.length == 0) return 0;

        int max = ints[0];
        for (int i = 1; i < ints.length; i++) {
            if (max < ints[i]) {
                max = ints[i];
            }
        }
        return max;
    }

    public static int min(int[] ints) {
        if (ints.length == 0) return 0;

        int min = ints[0];
        for (int i = 1; i < ints.length; i++) {
            if (min > ints[i]) {
                min = ints[i];
            }
        }
        return min;
    }

}
