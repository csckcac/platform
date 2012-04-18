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
2008 Andy Jefferson - javadocs
2008 Andy Jefferson - rewritten to allow chaining. Use "left" for what we invoke on, and store "method" explicitly.
    ...
**********************************************************************/
package org.datanucleus.query.expression;

import java.util.Iterator;
import java.util.List;

import org.datanucleus.query.symbol.Symbol;
import org.datanucleus.query.symbol.SymbolTable;
import org.datanucleus.util.StringUtils;

/**
 * Expression representing invocation of a method.
 * This may be an aggregate in a result clause (like "count(this)"), or a method on a class, or a function.
 * The "left" expression is what we are invoking on. This is typically a PrimaryExpression, or an InvokeExpression.
 * This then allows chaining of invocations.
 */
public class InvokeExpression extends Expression
{
    /** Name of the method to invoke. */
    String methodName;

    /** Arguments for the method invocation. */
    List<Expression> arguments;

    /**
     * Constructor for an expression for the invocation of a method/function.
     * @param invoked Expression on which we are invoking
     * @param methodName Name of the method
     * @param args Arguments passed in to the method/function call
     */
    public InvokeExpression(Expression invoked, String methodName, List args)
    {
        this.left = invoked;
        this.methodName = methodName;
        this.arguments = args;
        if (invoked != null)
        {
            invoked.parent = this;
        }
        if (args != null && !args.isEmpty())
        {
            Iterator<Expression> argIter = args.iterator();
            while (argIter.hasNext())
            {
                argIter.next().parent = this;
            }
        }
    }

    /**
     * The method/function invoked.
     * @return The method/function invoked.
     */
    public String getOperation()
    {
        return methodName;
    }

    /**
     * Accessor for any arguments to be passed in the invocation.
     * @return The arguments.
     */
    public List<Expression> getArguments()
    {
        return arguments;
    }

    /**
     * Method to bind the expression to the symbol table as appropriate.
     * @param symtbl Symbol table
     * @return The symbol for this expression
     */
    public Symbol bind(SymbolTable symtbl)
    {
        if (left != null)
        {
            try
            {
                left.bind(symtbl);
            }
            catch (PrimaryExpressionIsVariableException pive)
            {
                left = pive.getVariableExpression();
                left.bind(symtbl);
            }
            catch (PrimaryExpressionIsInvokeException piie)
            {
                left = piie.getInvokeExpression();
                left.bind(symtbl);
            }
        }
        // TODO Set symbol using the invoked method so we represent what the type really is

        if (arguments != null && arguments.size() > 0)
        {
            for (int i=0;i<arguments.size();i++)
            {
                Expression expr = arguments.get(i);
                try
                {
                    expr.bind(symtbl);
                }
                catch (PrimaryExpressionIsVariableException pive)
                {
                    VariableExpression ve = pive.getVariableExpression();
                    ve.bind(symtbl);
                    arguments.remove(i);
                    arguments.add(i, ve);
                }
                catch (PrimaryExpressionIsInvokeException piie)
                {
                    InvokeExpression ve = piie.getInvokeExpression();
                    ve.bind(symtbl);
                    arguments.remove(i);
                    arguments.add(i, ve);
                }
            }
        }
        return symbol;
    }

    public String toString()
    {
        if (left == null)
        {
            return "InvokeExpression{STATIC." + methodName +
                "(" + StringUtils.collectionToString(arguments) + ")}" + (alias != null ? " AS " + alias : "");
        }
        else
        {
            return "InvokeExpression{[" + left + "]." + methodName +
                "(" + StringUtils.collectionToString(arguments) + ")}" + (alias != null ? " AS " + alias : "");
        }
    }
}