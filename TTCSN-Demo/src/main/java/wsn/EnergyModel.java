/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

/**
 *
 * @author Admin
 */
public class EnergyModel {
    private SimulationConfig config;

    public EnergyModel(SimulationConfig config) {
        this.config = config;
    }
    
    /**
     * Tính năng lượng cần để TRUYỀN (Tx) gói tin size bits qua khoảng cách d.
     * Dùng công thức (3.1) trong bài báo.
     */
    
    public double calcTxEnergy(int bits, double distance) {
        double l = bits;    // l là số bit
        if (distance < config.D0) {
            // E_T = l * E_elec + l * eps_fs * d^2
            return l * config.E_ELEC + l * config.EPS_FS * distance * distance;
        } else {
            // E_T = l * E_elec + l * eps_mp * d^4
            return l * config.E_ELEC + l * config.EPS_MP * Math.pow(distance, 4);
        }
    }
    
    /**
     * Tính năng lượng cần để NHẬN (Rx) gói tin size bits.
     * Dùng công thức (3.2) trong bài báo: E_R = l * E_elec
     */
    
    public double calcRxEnergy(int bits) {
        double l = bits;
        return l + config.E_ELEC;
    }
    
    /**
     * Tính năng lượng dùng cho DATA AGGREGATION trên CH.
     * Thường dùng: E_DA * l * số gói tin.
     * Ở đây ta cho tính cho 1 gói l bit.
     */
    
    public double calcAggregationEnergy(int bits) {
        double l = bits;
        return l * config.E_DA;
    }
    
    // --- Các hàm tiện ích: trừ năng lượng trực tiếp từ node ---
    
    public void consumeTxEnergy(Node node, int bits, double distance) {
        if (!node.isAlive()) return;
        double cost = calcTxEnergy(bits, distance);
        node.setEnergy(node.getEnergy() - cost);
    }
    
    public void consumeRxEnergy(Node node, int bits) {
        if (!node.isAlive()) return;
        double cost = calcRxEnergy(bits);
        node.setEnergy(node.getEnergy() - cost);
    }
    
    public void consumeAggregationEnergy(Gateway ch, int bits, int numPackets) {
        if (!ch.isAlive() || ch.isFailed()) return;
        double cost = calcAggregationEnergy(bits) * numPackets;
        ch.setEnergy(ch.getEnergy() - cost);
    }
}
