package Network;

import RunModels.WSNDataSet;

/**
 *
 * @author Admin
 */
public class EnergyModel {
    WSNDataSet data = new WSNDataSet();
    public final double E_ELEC = data.E_ELEC;
    public final double EPS_FS = data.EPS_FS;
    public final double EPS_MP = data.EPS_MP;
    public final double D0 = data.D0;
    public final double E_DA = data.E_DA;


    // (3.1) Công thức tính năng lượng truyền
    public double energyToTransmit(int bits, double dist) {
        double e = E_ELEC * bits;       
        if (dist < D0) {
            e += EPS_FS * bits * dist * dist;
        } else {
            e += EPS_MP * bits * Math.pow(dist, 4);
        }
        return e;
    }

    // (3.2) công thức tính năng lượng nhận
    public double energyToReceive(int bits) {
        return E_ELEC * bits;   
    }
    
    // Energy for data aggregation of k bits (optional)
    public double energyToAggregate(int bits) {
        return (double) bits * E_DA;
    }
    
    public double energyTxPacket(double distance) {
        return energyToTransmit(
                WSNDataSet.messageSize,
                distance
        );
    }
    
    public double energyRxPacket() {
        return energyToReceive(
                WSNDataSet.messageSize
        );
    }
    
    public double energyPerPacket(double distance) {
        return energyTxPacket(distance) + energyRxPacket();
    }
    
    public double energyPacketToBS(double distance) {
        return energyToTransmit(
                WSNDataSet.messageSize,
                distance
        );
    }




}
