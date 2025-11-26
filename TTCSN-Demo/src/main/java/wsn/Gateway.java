/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class Gateway extends Node {
    
    // HopCount(gi) – số nhảy tới BS
    private int hopCount = Integer.MAX_VALUE;
    
    // Danh sách sensor thuộc cluster này
    private List<SensorNode> members = new ArrayList<>();
    
    // Danh sách sensor thuộc cluster này
    private Gateway nextHop;
    
    // CH bị fail (Weibull hoặc hết năng lượng)
    private boolean failed = false;
    
    // Thời điểm (round) mà gateway dự kiến sẽ hỏng theo Weibull
    private int scheduledFailureRound = Integer.MAX_VALUE;
    
    public Gateway(int id, double x, double y, double initialEnergy) {
        super(id, x, y, initialEnergy, NodeType.GATEWAY);
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public List<SensorNode> getMembers() {
        return members;
    }
    
    public void addMember(SensorNode sensor) {
        if (!members.contains(sensor)) {
            members.add(sensor);
            sensor.setClusterHead(this);
        }
    }
    
    public void clearMembers() {
        members.clear();
    }

    public Gateway getNextHop() {
        return nextHop;
    }

    public void setNextHop(Gateway nextHop) {
        this.nextHop = nextHop;
    }

    public boolean isFailed() {
        return failed || !alive;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
        if (failed) {
            this.alive = false;
            this.energy = 0;
        }
    }

    public int getScheduledFailureRound() {
        return scheduledFailureRound;
    }

    public void setScheduledFailureRound(int scheduledFailureRound) {
        this.scheduledFailureRound = scheduledFailureRound;
    }
    
    

    @Override
    public String toString() {
        return String.format("Gateway{id=%d, x=%.2f, y=%.2f, E=%.4f, hop=%d, failed=%b, failRound=%d}", id, x, y, energy, hopCount, isFailed(), scheduledFailureRound);
    }
    
    
    
    
}
