package com.inetpsa.seph.messinjection;

public class InterfaceAttributes {
    private String group;
    private String dataElement;
    private String swcProducerNames;
    private String swcConsumerNames;
    private String dataDescription;
    private String dataType;
    private String unit;
    private String dataWidth;
    private String states;
    private String initValue;
    private String physMinValue;
    private String physMaxValue;
    private String runnableProducerNames;
    private String runnableConsumerNames;
    private boolean beAnalyzed = false;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataElement() {
        return dataElement;
    }

    public void setDataElement(String dataElement) {
        this.dataElement = dataElement;
    }

    public String getSwcProducerNames() {
        return swcProducerNames;
    }

    public void setSwcProducerNames(String swcProducerNames) {
        this.swcProducerNames = swcProducerNames;
    }

    public String getSwcConsumerNames() {
        return swcConsumerNames;
    }

    public void setSwcConsumerNames(String swcConsumerNames) {
        this.swcConsumerNames = swcConsumerNames;
    }

    public String getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(String dataDescription) {
        this.dataDescription = dataDescription;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDataWidth() {
        return dataWidth;
    }

    public void setDataWidth(String dataWidth) {
        this.dataWidth = dataWidth;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public String getInitValue() {
        return initValue;
    }

    public void setInitValue(String initValue) {
        this.initValue = initValue;
    }

    public String getPhysMinValue() {
        return physMinValue;
    }

    public void setPhysMinValue(String physMinValue) {
        this.physMinValue = physMinValue;
    }

    public String getPhysMaxValue() {
        return physMaxValue;
    }

    public void setPhysMaxValue(String physMaxValue) {
        this.physMaxValue = physMaxValue;
    }

    public String getRunnableProducerNames() {
        return runnableProducerNames;
    }

    public void setRunnableProducerNames(String runnableProducerNames) {
        this.runnableProducerNames = runnableProducerNames;
    }

    public String getRunnableConsumerNames() {
        return runnableConsumerNames;
    }

    public void setRunnableConsumerNames(String runnableConsumerNames) {
        this.runnableConsumerNames = runnableConsumerNames;
    }

    public boolean isBeAnalyzed() {
        return beAnalyzed;
    }

    public void setBeAnalyzed(boolean beAnalyzed) {
        this.beAnalyzed = beAnalyzed;
    }
}
