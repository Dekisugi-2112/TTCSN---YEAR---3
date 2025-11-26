/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

/**
 *
 * @author Admin
 */
public class Node {
    public int id;
    public double x;
    public double y;
    public NodeType type;            // toạn độ           // toạn độ
    public double energy;          // năng lượng (J)
    public boolean alive = true;    // trạng thái sống

    public Node(int id, double x, double y, double energy, NodeType type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.energy = energy;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public NodeType getType() {
        return type;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
        if (this.energy <= 0) {
            this.energy = 0;
            this.alive = false;
        }
    }
    
    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            this.energy = 0;
        }
    }

    // Tính khoảng cách Euclid
    public double distanceTo(Node other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx*dx + dy*dy);
        // return Math.hypot(dx, dy);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, x=%.2f, y=%.2f, energy=%.4f, alive=%b}", type, id, x, y, energy, alive);
    }
    
    
    
    
    
    
}
