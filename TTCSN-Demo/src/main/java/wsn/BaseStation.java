/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

/**
 *
 * @author Admin
 */
public class BaseStation extends Node {
    
    public BaseStation(int id, double x, double y) {
        super(id, x, y, Double.POSITIVE_INFINITY, NodeType.BASE_STATION);
    }

    public void setEnergy(double energy) {
        
    }

    public boolean setAlive() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("BaseStation{id=%d, x=%.2f, y=%.2f}", id, x, y);
    }
    
    
    
    
    
}
