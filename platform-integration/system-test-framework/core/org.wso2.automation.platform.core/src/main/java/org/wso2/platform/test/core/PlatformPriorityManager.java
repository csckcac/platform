package org.wso2.platform.test.core;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlatformPriorityManager implements IMethodInterceptor {
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<IMethodInstance>();
        result=methods;
        Comparator<IMethodInstance> comparator = new Comparator<IMethodInstance>() {
            public int compare(IMethodInstance o1, IMethodInstance o2) {
                return(o1.getMethod().getMethod().getAnnotation(Test.class).priority() -o2.getMethod().getMethod().getAnnotation(Test.class).priority());
            }
            public boolean equals(Object obj) {
                return false;
            }
        };
        Collections.sort(result,comparator);
        return result;
    }
}
