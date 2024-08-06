package de.afrouper.api.clients.tibber.dto;

import java.util.Date;

public class LiveMeasurement {

    private Date timestamp;
    private int power;
    private double lastMeterConsumption;
    private double lastMeterProduction;
    private double accumulatedConsumption;
    private double accumulatedConsumptionLastHour;
    private double accumulatedCost;
    private double accumulatedReward;
    private double currency;
    private int minPower;
    private double averagePower;
    private int maxPower;
    private double voltagePhase1;
    private double voltagePhase2;
    private double voltagePhase3;
    private double currentL1;
    private double currentL2;
    private double currentL3;
    private double powerProduction;
    private double accumulatedProduction;
    private double accumulatedProductionLastHour;
    private double minPowerProduction;
    private double maxPowerProduction;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public double getLastMeterConsumption() {
        return lastMeterConsumption;
    }

    public void setLastMeterConsumption(double lastMeterConsumption) {
        this.lastMeterConsumption = lastMeterConsumption;
    }

    public double getLastMeterProduction() {
        return lastMeterProduction;
    }

    public void setLastMeterProduction(double lastMeterProduction) {
        this.lastMeterProduction = lastMeterProduction;
    }

    public double getAccumulatedConsumption() {
        return accumulatedConsumption;
    }

    public void setAccumulatedConsumption(double accumulatedConsumption) {
        this.accumulatedConsumption = accumulatedConsumption;
    }

    public double getAccumulatedConsumptionLastHour() {
        return accumulatedConsumptionLastHour;
    }

    public void setAccumulatedConsumptionLastHour(double accumulatedConsumptionLastHour) {
        this.accumulatedConsumptionLastHour = accumulatedConsumptionLastHour;
    }

    public double getAccumulatedCost() {
        return accumulatedCost;
    }

    public void setAccumulatedCost(double accumulatedCost) {
        this.accumulatedCost = accumulatedCost;
    }

    public double getAccumulatedReward() {
        return accumulatedReward;
    }

    public void setAccumulatedReward(double accumulatedReward) {
        this.accumulatedReward = accumulatedReward;
    }

    public double getCurrency() {
        return currency;
    }

    public void setCurrency(double currency) {
        this.currency = currency;
    }

    public int getMinPower() {
        return minPower;
    }

    public void setMinPower(int minPower) {
        this.minPower = minPower;
    }

    public double getAveragePower() {
        return averagePower;
    }

    public void setAveragePower(double averagePower) {
        this.averagePower = averagePower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(int maxPower) {
        this.maxPower = maxPower;
    }

    public double getVoltagePhase1() {
        return voltagePhase1;
    }

    public void setVoltagePhase1(double voltagePhase1) {
        this.voltagePhase1 = voltagePhase1;
    }

    public double getVoltagePhase2() {
        return voltagePhase2;
    }

    public void setVoltagePhase2(double voltagePhase2) {
        this.voltagePhase2 = voltagePhase2;
    }

    public double getVoltagePhase3() {
        return voltagePhase3;
    }

    public void setVoltagePhase3(double voltagePhase3) {
        this.voltagePhase3 = voltagePhase3;
    }

    public double getCurrentL1() {
        return currentL1;
    }

    public void setCurrentL1(double currentL1) {
        this.currentL1 = currentL1;
    }

    public double getCurrentL2() {
        return currentL2;
    }

    public void setCurrentL2(double currentL2) {
        this.currentL2 = currentL2;
    }

    public double getCurrentL3() {
        return currentL3;
    }

    public void setCurrentL3(double currentL3) {
        this.currentL3 = currentL3;
    }

    public double getPowerProduction() {
        return powerProduction;
    }

    public void setPowerProduction(double powerProduction) {
        this.powerProduction = powerProduction;
    }

    public double getAccumulatedProduction() {
        return accumulatedProduction;
    }

    public void setAccumulatedProduction(double accumulatedProduction) {
        this.accumulatedProduction = accumulatedProduction;
    }

    public double getAccumulatedProductionLastHour() {
        return accumulatedProductionLastHour;
    }

    public void setAccumulatedProductionLastHour(double accumulatedProductionLastHour) {
        this.accumulatedProductionLastHour = accumulatedProductionLastHour;
    }

    public double getMinPowerProduction() {
        return minPowerProduction;
    }

    public void setMinPowerProduction(double minPowerProduction) {
        this.minPowerProduction = minPowerProduction;
    }

    public double getMaxPowerProduction() {
        return maxPowerProduction;
    }

    public void setMaxPowerProduction(double maxPowerProduction) {
        this.maxPowerProduction = maxPowerProduction;
    }
}
