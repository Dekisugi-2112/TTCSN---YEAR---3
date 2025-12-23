package NodeType;

import Network.*;

public abstract class Node {
    // ---------------- COMMON FEATURES -------------------
    public int id;  // mã của node
    public double x;    // tọa độ x
    public double y;    // tọa độ y
    public double residualEnergy;   // năng lượng còn lại
    public double commRange;    // phạm vi truyền
    public boolean alive = true;  // tình trạng sống/chết
    // --------------------------------------
    
    // --------------------- constructor --------------------
    public Node(int id, double x, double y, double residualEnergy, double commRange) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.residualEnergy = residualEnergy;
        this.commRange = commRange;
    }
    // ----------------------------------------------
    
    // ----------------------- getter vs setter ---------------------------
    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getCommRange() {
        return commRange;
    }

    public double getResidualEnergy() {
        return residualEnergy;
    }

    public boolean isAlive() {
        return alive;
    } 
    // -------------------------------------------- 
    
    // ------------------ METHOD --------------------------

    // Công thức Euclid tính khoảng cách giữa 2 node
    public double distanceTo(Node other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;        
        return Math.hypot(dx, dy);
    }
    
    // kiểm tra trong phạm vi (dùng min của hai commRange)
    public boolean isInRange(Node other) {
        return this.distanceTo(other) <= Math.min(this.getCommRange(), other.getCommRange());
    }
    // -------------------------------------------------
    
    // ------------TIÊU HAO NĂNG LUỘNG ----------------------
    public boolean consumeEnergy(double amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non-negative");
        if (!alive) return false;
        residualEnergy -= amount;
        if (residualEnergy <= 0) {
            residualEnergy = 0;
            alive = false;
            onDeath();
            return false;
        }
        return true;
    }
    
    protected void onDeath() {
        System.out.println("Node#" + id + " died.");
    }
    // ----------------------------------




}
