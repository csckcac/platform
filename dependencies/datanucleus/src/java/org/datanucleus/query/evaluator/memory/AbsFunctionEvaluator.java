/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
    ...
**********************************************************************/
package org.datanucleus.query.evaluator.memory;

import java.math.BigDecimal;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.util.Localiser;

/**
 * Evaluator for the function ABS(numExpr).
 */
public class AbsFunctionEvaluator implements InvocationEvaluator
{
    /** Localisation utility for output messages */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /* (non-Javadoc)
     * @see org.datanucleus.query.evaluator.memory.InvocationEvaluator#evaluate(org.datanucleus.query.expression.InvokeExpression, org.datanucleus.query.evaluator.memory.InMemoryExpressionEvaluator)
     */
    public Object evaluate(InvokeExpression expr, Object invokedValue, InMemoryExpressionEvaluator eval)
    {
        String method = expr.getOperation();
        Object param = expr.getArguments().get(0);
        Object paramValue = null;
        if (param instanceof PrimaryExpression)
        {
            PrimaryExpression primExpr = (PrimaryExpression)param;
            paramValue = eval.getValueForPrimaryExpression(primExpr);
        }
        else if (param instanceof ParameterExpression)
        {
            ParameterExpression paramExpr = (ParameterExpression)param;
            paramValue = QueryUtils.getValueForParameterExpression(eval.getParameterValues(), paramExpr);
        }
        else if (param instanceof Literal)
        {
            paramValue = ((Literal)param).getLiteral();
        }
        else
        {
            throw new NucleusException(method + "(num) where num is instanceof " + param.getClass().getName() + " not supported");
        }

        Object result = null;
        if (paramValue instanceof Double)
        {
            result = Double.valueOf(Math.abs(((Double)paramValue).doubleValue()));
        }
        else if (paramValue instanceof Float)
        {
            result = Float.valueOf(Math.abs(((Float)paramValue).floatValue()));
        }
        else if (paramValue instanceof BigDecimal)
        {
            result = new BigDecimal(Math.abs(((BigDecimal)paramValue).doubleValue()));
        }
        else if (paramValue instanceof Integer)
        {
            result = Integer.valueOf(Math.abs(((Integer)paramValue).intValue()));
        }
        else if (paramValue instanceof Long)
        {
            result = Long.valueOf(Math.abs(((Long)paramValue).longValue()));
        }
        else
        {
            throw new NucleusException("Not possible to use Math.abs() on value of type " + paramValue.getClass().getName());
        }
        return result;
    }
}