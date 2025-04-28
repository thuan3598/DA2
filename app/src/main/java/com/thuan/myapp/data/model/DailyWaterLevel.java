package com.thuan.myapp.data.model;

import java.io.Serializable;

public class DailyWaterLevel implements Serializable {

    private String id;
    private String constructionId; // Tham chiếu tới Construction
    private String date;
    private Double waterLevel7hHl;
    private Double waterLevel19hHl;
    private Double waterLevel7hTl;
    private Double waterLevel19hTl;
    private Double suctionTank7h;
    private Double suctionTank19h;
    private Double dischargeTank7h;
    private Double dischargeTank19h;
    private Double avgWaterLevel;
    private Double gateOpenHeight;
    private Integer openedGateCount;
    private Double waterFlow;
    private String notes;
    private String pumpOperationStatus;
    private String recorderId;     // Tham chiếu tới Account

    public DailyWaterLevel() {}

    public DailyWaterLevel(String id, String constructionId, String date, Double waterLevel7hHl, Double waterLevel19hHl, Double waterLevel7hTl, Double waterLevel19hTl, Double suctionTank7h, Double suctionTank19h, Double dischargeTank7h, Double dischargeTank19h, Double avgWaterLevel, Double gateOpenHeight, Integer openedGateCount, Double waterFlow, String notes, String pumpOperationStatus, String recorderId) {
        this.id = id;
        this.constructionId = constructionId;
        this.date = date;
        this.waterLevel7hHl = waterLevel7hHl;
        this.waterLevel19hHl = waterLevel19hHl;
        this.waterLevel7hTl = waterLevel7hTl;
        this.waterLevel19hTl = waterLevel19hTl;
        this.suctionTank7h = suctionTank7h;
        this.suctionTank19h = suctionTank19h;
        this.dischargeTank7h = dischargeTank7h;
        this.dischargeTank19h = dischargeTank19h;
        this.avgWaterLevel = avgWaterLevel;
        this.gateOpenHeight = gateOpenHeight;
        this.openedGateCount = openedGateCount;
        this.waterFlow = waterFlow;
        this.notes = notes;
        this.pumpOperationStatus = pumpOperationStatus;
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

    public Double getWaterLevel7hHl() {
        return waterLevel7hHl;
    }

    public void setWaterLevel7hHl(Double waterLevel7hHl) {
        this.waterLevel7hHl = waterLevel7hHl;
    }

    public Double getWaterLevel19hHl() {
        return waterLevel19hHl;
    }

    public void setWaterLevel19hHl(Double waterLevel19hHl) {
        this.waterLevel19hHl = waterLevel19hHl;
    }

    public Double getWaterLevel7hTl() {
        return waterLevel7hTl;
    }

    public void setWaterLevel7hTl(Double waterLevel7hTl) {
        this.waterLevel7hTl = waterLevel7hTl;
    }

    public Double getWaterLevel19hTl() {
        return waterLevel19hTl;
    }

    public void setWaterLevel19hTl(Double waterLevel19hTl) {
        this.waterLevel19hTl = waterLevel19hTl;
    }

    public Double getSuctionTank7h() {
        return suctionTank7h;
    }

    public void setSuctionTank7h(Double suctionTank7h) {
        this.suctionTank7h = suctionTank7h;
    }

    public Double getSuctionTank19h() {
        return suctionTank19h;
    }

    public void setSuctionTank19h(Double suctionTank19h) {
        this.suctionTank19h = suctionTank19h;
    }

    public Double getDischargeTank7h() {
        return dischargeTank7h;
    }

    public void setDischargeTank7h(Double dischargeTank7h) {
        this.dischargeTank7h = dischargeTank7h;
    }

    public Double getDischargeTank19h() {
        return dischargeTank19h;
    }

    public void setDischargeTank19h(Double dischargeTank19h) {
        this.dischargeTank19h = dischargeTank19h;
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

    public String getPumpOperationStatus() {
        return pumpOperationStatus;
    }

    public void setPumpOperationStatus(String pumpOperationStatus) {
        this.pumpOperationStatus = pumpOperationStatus;
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
                ", waterLevel7hHl=" + waterLevel7hHl +
                ", waterLevel19hHl=" + waterLevel19hHl +
                ", waterLevel7hTl=" + waterLevel7hTl +
                ", waterLevel19hTl=" + waterLevel19hTl +
                ", suctionTank7h=" + suctionTank7h +
                ", suctionTank9h=" + suctionTank19h +
                ", dischargeTank7h=" + dischargeTank7h +
                ", dischargeTank9h=" + dischargeTank19h +
                ", avgWaterLevel=" + avgWaterLevel +
                ", gateOpenHeight=" + gateOpenHeight +
                ", openedGateCount=" + openedGateCount +
                ", waterFlow=" + waterFlow +
                ", notes='" + notes + '\'' +
                ", pumpOperationStatus='" + pumpOperationStatus + '\'' +
                ", recorderId='" + recorderId + '\'' +
                '}';
    }
}
