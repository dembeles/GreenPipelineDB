/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

/**
 *
 * @author test
 */
public class QueryStatistic {

    public Double getEstimatedTotalCost() {
        return EstimatedTotalCost;
    }

    public void setEstimatedTotalCost(Double EstimatedTotalCost) {
        this.EstimatedTotalCost = EstimatedTotalCost;
    }

    public Double getEstimatedPower() {
        return EstimatedPower;
    }

    public void setEstimatedPower(Double EstimatedPower) {
        this.EstimatedPower = EstimatedPower-(FenP.B0 * (nbreop-1));
    }

    public Double getEstimatedEnergy() {
        return EstimatedEnergy;
    }

    public void setEstimatedEnergy(Double Estima) {
        if(Estima==0.0){
            EstimatedEnergy = EstimatedPower*EstimatedTotalCost;
        }
        else {EstimatedEnergy = EstimatedEnergy;}
    }

    public Double getEstimatedIO() {
        return EstimatedIO;
    }

    public void setEstimatedIO(Double EstimatedIO) {
        this.EstimatedIO = EstimatedIO;
    }

    public Double getEstimatedCPU() {
        return EstimatedCPU;
    }

    public void setEstimatedCPU(Double EstimatedCPU) {
        this.EstimatedCPU = EstimatedCPU;
    }

    public Double getRealpower() {
        return Realpower;
    }

    public void setRealpower(Double Realpower) {
        this.Realpower = Realpower;
    }

    public Double getRealenergy() {
        return Realenergy;
    }

    public void setRealenergy(Double Realenergy) {
        this.Realenergy = Realenergy;
    }

    public Double getTime() {
        return Time;
    }

    public void setTime(Double Time) {
        this.Time = Time;
    }
    private int nbreop;
    private Double EstimatedTotalCost=0.0;
    private Double EstimatedPower=0.0;
    private Double EstimatedEnergy=0.0;
    private Double EstimatedIO=0.0;
    private Double EstimatedCPU=0.0;
    private Double Realpower=1.0;
    private Double Realenergy=0.0;
    private Double Time=-1.0;

    public int getNbreop() {
        return nbreop;
    }

    public void setNbreop(int nbreop) {
        this.nbreop = nbreop;
    }
    
}
