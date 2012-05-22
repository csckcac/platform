/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.core;

import org.wso2.carbon.cep.core.mapping.output.Output;

public class Query {

    /**
     * Query String
     * */
    private Expression expression;

    /**
     * Name of  the query
     * */
    private String name;

    /**
     * configuration ot the output topic and the output XML Element
     *
     * */
    private Output output;

    /***
     *Order of the query, important when having more than one query to maintain the order of
     *  queries
     */

    private int queryIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        if (!name.equals(query.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQueryIndex() {
        return queryIndex;
    }

    public void setQueryIndex(int queryIndex) {
        this.queryIndex = queryIndex;
    }
}
