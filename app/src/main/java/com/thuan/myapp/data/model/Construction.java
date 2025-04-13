package com.thuan.myapp.data.model;

import java.io.Serializable;

public class Construction implements Serializable {
    private String id;
    private String constructionName;
    private Integer yearBuilt;
    private String location;
    private String gateType;
    private Integer gateCount;
    private Double gateSize;
    private Double designFlow;
    private Double designWaterLevel;
    private Double bottomElevation;
    private String waterGaugeType;
    private Integer Type;

    public Construction() {}

    public Construction(String id, String constructionName, int yearBuilt, String location, String gateType, int gateCount, double gateSize, double designFlow, double designWaterLevel, double bottomElevation, String waterGaugeType, int type) {
        this.id = id;
        this.constructionName = constructionName;
        this.yearBuilt = yearBuilt;
        this.location = location;
        this.gateType = gateType;
        this.gateCount = gateCount;
        this.gateSize = gateSize;
        this.designFlow = designFlow;
        this.designWaterLevel = designWaterLevel;
        this.bottomElevation = bottomElevation;
        this.waterGaugeType = waterGaugeType;
        Type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConstructionName() {
        return constructionName;
    }

    public void setConstructionName(String constructionName) {
        this.constructionName = constructionName;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(int yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGateType() {
        return gateType;
    }

    public void setGateType(String gateType) {
        this.gateType = gateType;
    }

    public Integer getGateCount() {
        return gateCount;
    }

    public void setGateCount(int gateCount) {
        this.gateCount = gateCount;
    }

    public Double getGateSize() {
        return gateSize;
    }

    public void setGateSize(double gateSize) {
        this.gateSize = gateSize;
    }

    public Double getDesignFlow() {
        return designFlow;
    }

    public void setDesignFlow(double designFlow) {
        this.designFlow = designFlow;
    }

    public Double getDesignWaterLevel() {
        return designWaterLevel;
    }

    public void setDesignWaterLevel(double designWaterLevel) {
        this.designWaterLevel = designWaterLevel;
    }

    public Double getBottomElevation() {
        return bottomElevation;
    }

    public void setBottomElevation(double bottomElevation) {
        this.bottomElevation = bottomElevation;
    }

    public String getWaterGaugeType() {
        return waterGaugeType;
    }

    public void setWaterGaugeType(String waterGaugeType) {
        this.waterGaugeType = waterGaugeType;
    }


    public Integer getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    @Override
    public String toString() {
        return constructionName;
    }
}
