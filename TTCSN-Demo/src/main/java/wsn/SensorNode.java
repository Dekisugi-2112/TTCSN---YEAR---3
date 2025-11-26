/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

/**
 *
 * @author Admin
 */
public class SensorNode extends Node {
    // CH mà sensor này thuộc về (nếu có)
    private Gateway clusterHead;
    
    // Nếu sensor không có CH trong tầm -> dùng relay để gửi tới CH
    private SensorNode relay;
    
    // Có được phủ bởi CH không? (COset / UnCOset)
    private boolean covered = false;
    
    // "Active" = còn năng lượng + có đường tới CH/BS
    private boolean active = true;

    public SensorNode(int id, double x, double y, double initialEnergy) {
        super(id, x, y, initialEnergy, NodeType.SENSOR);
    }    

    public Gateway getClusterHead() {
        return clusterHead;
    }

    public void setClusterHead(Gateway clusterHead) {
        this.clusterHead = clusterHead;
    }

    public SensorNode getRelay() {
        return relay;
    }

    public void setRelay(SensorNode relay) {
        this.relay = relay;
    }

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("SensorNode{id=%d, x=%.2f, y=%.2f, E=%.4f, covered=%b, active=%b}", id, x, y, energy, covered, active);
    }
    
    
    
    
}
