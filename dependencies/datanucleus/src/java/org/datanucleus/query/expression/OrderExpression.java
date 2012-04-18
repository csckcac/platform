/**********************************************************************
Copyright (c) 2008 Erik Bengtson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
2008 Andy Jefferson - javadocs, toString()
    ...
**********************************************************************/
package org.datanucleus.query.expression;

import org.datanucleus.query.symbol.Symbol;
import org.datanucleus.query.symbol.SymbolTable;

/**
 * Expression as part of an ordering clause. Composed of an expression and an order direction.
 */
public class OrderExpression extends Expression
{
    private String sortOrder;

    /**
     * Constructor.
     * @param expr The expression to order by
     * @param sortOrder The order (either "ascending" or "descending")
     */
    public OrderExpression(Expression expr, String sortOrder)
    {
        super();
        this.left = expr;
        this.sortOrder = sortOrder;
    }

    public OrderExpression(Expression expr)
    {
        super();
        this.left = expr;
    }

    public String getSortOrder()
    {
        return sortOrder;
    }

    public Symbol bind(SymbolTable symtbl)
    {
        if (this.left instanceof VariableExpression)
        {
            VariableExpression ve = (VariableExpression)this.left;
            ve.bind(symtbl);
        }
        return null;
    }

    public Object evaluate(ExpressionEvaluator eval)
    {
        return eval.evaluate(left);
    }

    public String toString()
    {
        return "OrderExpression{" + left + " " + sortOrder + "}";
    }
}