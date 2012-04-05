package org.wso2.carbon.cep.admin.internal;


public class QueryDTO {

    /**
     * Name of the QueryDTO
     * */
    private String name;

    private ExpressionDTO expressionDTO;

    /**
     * configuration ot the outputDTO topic and the outputDTO XML Element
     *
     * */
    private OutputDTO outputDTO;

    private int queryIndex;

    public ExpressionDTO getExpression() {
        return expressionDTO;
    }

    public void setExpression(ExpressionDTO expressionDTO) {
        this.expressionDTO = expressionDTO;
    }

    public OutputDTO getOutput() {
        return outputDTO;
    }

    public void setOutput(OutputDTO outputDTO) {
        this.outputDTO = outputDTO;
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