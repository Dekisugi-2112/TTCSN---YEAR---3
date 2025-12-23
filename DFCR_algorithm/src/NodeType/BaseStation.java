package NodeType;

import RunModels.WSNDataSet;
import Network.*;

public class BaseStation extends Node {
    WSNDataSet data = new WSNDataSet();
    EnergyModel em = new EnergyModel();
    
    private Integer hopCount = Integer.MAX_VALUE;
    
    // ---------------- CONSTRUCTURE --------------
    public BaseStation(int id, double x, double y, double energy, double commRange) {
        // BS không cần giới hạn năng lượng hay phạm vi -> dùng VERY LARGE values
        super(id, x, y, energy, commRange);
        this.alive = true;
    }
    
    // ---------------------------
    // ----------- GETTER & SETTER --------------
    public Integer getHopCount() {
        return hopCount;
    }

    public void setHopCount(Integer hopCount) {
        this.hopCount = hopCount;
    }
    // ----------------------------------------

   
    // ------------ METHOD --------------------

    // bs nhận: DATA_AGG, STATUS_
    public void receive(Packet packet, NetworkSimulator net, NodeDeployment deployment) {
        if (packet.getType() == Packet.MessageType.DATA) {
            System.out.println(
                "BaseStation RECEIVED DATA from Sensor " +
                packet.getPayload()
            );
        }
        
    }
    // ==================================================================================

    // -----------------------------------------------------------------
    // BS GIỬI: HOP_PACKET
    public void send(Packet packet, Node to, NetworkSimulator net, NodeDeployment deployment) {

        if (packet == null || net == null || deployment == null) {
            return;
        }

        // BS chỉ gửi broadcast (HELLO, HOP)
        if (to == null || packet.isBroadcast() || packet.getDstId() == -1) {

            switch (packet.getType()) {

                case HELLO:
                    // BS → Gateway (trong phạm vi)
                    net.broadcastToGateways(this, packet, deployment);
                    break;

                case HOP:
                    // BS flood HopPacket
                    net.broadcastToGateways(this, packet, deployment);
                    break;

                default:
                    // BS không gửi các loại packet khác
                    break;
            }
        }
    }
    

    
   

  

    

}
