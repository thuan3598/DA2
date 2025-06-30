package com.thuan.myapp.data.model;

import java.io.Serializable;

public class DailyWaterLevel implements Serializable {

    private String id;
    private String constructionId; // Tham chiếu tới Construction
    private String date;
    private Double waterLevel7h;
    private Double waterLevel19h;

    private Double avgWaterLevel;
    private Double gateOpenHeight;
    private Integer openedGateCount;
    private Double waterFlow;
    private String notes;

    private String recorderId;     // Tham chiếu tới Account

    public DailyWaterLevel() {}

    public DailyWaterLevel(String id, String constructionId, String date, Double waterLevel7h, Double waterLevel19h, Double avgWaterLevel, Double gateOpenHeight, Integer openedGateCount, Double waterFlow, String notes, String recorderId) {
        this.id = id;
        this.constructionId = constructionId;
        this.date = date;
        this.waterLevel7h = waterLevel7h;
        this.waterLevel19h = waterLevel19h;
        this.avgWaterLevel = avgWaterLevel;
        this.gateOpenHeight = gateOpenHeight;
        this.openedGateCount = openedGateCount;
        this.waterFlow = waterFlow;
        this.notes = notes;

        this.recorderId = recorderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConstructionId() {
        return constructionId;
    }

    public void setConstructionId(String constructionId) {
        this.constructionId = constructionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getWaterLevel7h() {
        return waterLevel7h;
    }

    public void setWaterLevel7h(Double waterLevel7h) {
        this.waterLevel7h = waterLevel7h;
    }

    public Double getWaterLevel19h() {
        return waterLevel19h;
    }

    public void setWaterLevel19h(Double waterLevel19h) {
        this.waterLevel19h = waterLevel19h;
    }

    public Double getAvgWaterLevel() {
        return avgWaterLevel;
    }

    public void setAvgWaterLevel(Double avgWaterLevel) {
        this.avgWaterLevel = avgWaterLevel;
    }

    public Double getGateOpenHeight() {
        return gateOpenHeight;
    }

    public void setGateOpenHeight(Double gateOpenHeight) {
        this.gateOpenHeight = gateOpenHeight;
    }

    public Integer getOpenedGateCount() {
        return openedGateCount;
    }

    public void setOpenedGateCount(Integer openedGateCount) {
        this.openedGateCount = openedGateCount;
    }

    public Double getWaterFlow() {
        return waterFlow;
    }

    public void setWaterFlow(Double waterFlow) {
        this.waterFlow = waterFlow;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(String recorderId) {
        this.recorderId = recorderId;
    }

    @Override
    public String toString() {
        return "DailyWaterLevel{" +
                "id='" + id + '\'' +
                ", constructionId='" + constructionId + '\'' +
                ", date='" + date + '\'' +
                ", waterLevel7hHl=" + waterLevel7h +
                ", waterLevel19hHl=" + waterLevel19h+
                ", avgWaterLevel=" + avgWaterLevel +
                ", gateOpenHeight=" + gateOpenHeight +
                ", openedGateCount=" + openedGateCount +
                ", waterFlow=" + waterFlow +
                ", notes='" + notes + '\'' +
                ", recorderId='" + recorderId + '\'' +
                '}';
    }
}
