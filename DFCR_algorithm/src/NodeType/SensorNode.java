package NodeType;

import RunModels.WSNDataSet;
import Network.*;
import static NodeType.Packet.MessageType.HELP;
import java.util.*;

public class SensorNode extends Node {
    
    WSNDataSet data = new WSNDataSet();
    EnergyModel em = new EnergyModel();

    // ----------- COMMON FEATURES ----------------
    private Integer clusterHeadId = null; // lấy id của CH
    // Danh sách gateway mà sensor nghe được từ HELLO
    private Map<Integer, GatewayInfo> comCH = new HashMap<>();
    
    private List<SensorNode> backupCandidates = new ArrayList<>();
    


    
    
    private double totalTxEnergy = 0.0;

    public double getTotalTxEnergy() {
        return totalTxEnergy;
    }

    
    
    private Integer backupNodeId;   // sensor trung chuyển
    public Integer getBackupNodeId() {
        return backupNodeId;
    }


    public List<SensorNode> getBackupCandidates() {
        return backupCandidates;
    }

    public void clearBackupCandidates() {
        backupCandidates.clear();
    }


    public void setBackupNodeId(Integer id) { this.backupNodeId = id; }

    // -------------- CONSTRUCTER --------------------
    public SensorNode(int id, double x, double y, double energy, double sensorRange) {
        super(id, x, y, energy, sensorRange);
    }
    // --------------------------------------

    
    // ------------- GETTER & SETTER -----------------
    public boolean isInCoSet() {
        return !comCH.isEmpty();
    }

    public Map<Integer, GatewayInfo> getComCH() {
        return comCH;
    }

    public void clearComCH() {
        comCH.clear();
    }


    public Integer getClusterHeadId() {
        return clusterHeadId;
    }

    public void setClusterHeadId(Integer clusterHeadId) {
        this.clusterHeadId = clusterHeadId;
    }
    // --------------------------------------------


    
    // ---------------------- METHOD ----------------------------
    public static class GatewayInfo {
        public final int id;
        public final double energy;
        public final int hop;

        public GatewayInfo(int id, double energy, int hop) {
            this.id = id;
            this.energy = energy;
            this.hop = hop;
        }
    }
    
    
    

    
    // -------------------------------------------------
    // SENSOR NHẬN: HELLO
    public void receive(Packet packet, NetworkSimulator net, NodeDeployment deployment) {
        if (packet == null || !this.isAlive()) return;

        switch (packet.getType()) {
            case HELLO: 
                if (!(packet.getPayload() instanceof Object[])) {
                    // HELLO từ BS → Sensor bỏ qua
                    return;
                }
                Object payload = packet.getPayload();
                if (!(payload instanceof Object[])) {
                    return;
                }

                Object[] arr = (Object[]) payload;

                // payload phải đủ 3 phần tử
                if (arr.length < 3) {
                    return;
                }

                int gwId;
                double gwEnergy;
                int gwHop;

                try {
                    gwId     = (Integer) arr[0];
                    gwEnergy = (Double)  arr[1];
                    gwHop    = (Integer) arr[2];
                } catch (Exception e) {
                    return;
                }

                // gateway không có route về BS → bỏ qua
                if (gwHop == Integer.MAX_VALUE) {
                    return;
                }
                Gateway gw = null;
                for (Gateway g : deployment.getGateways()) {
                    if (g.getId() == gwId && g.isAlive()) {
                        gw = g;
                        break;
                    }
                }
                if (gw == null) return;
                // lưu vào ComCH
                comCH.put(
                    gwId,
                    new GatewayInfo(gwId, gwEnergy, gwHop)
                );
                break;
            
            case JOIN_ACK:
                Integer chId = (Integer) packet.getPayload();
                this.setClusterHeadId(chId);
                break;

            case HELP_REPLY:
                Object[] pl = (Object[]) packet.getPayload();
                int helperId = (Integer) pl[0];

                SensorNode helper =
                    NetworkSimulator.findSensorById(helperId, deployment);

                if (helper != null && helper.isAlive()) {
                    backupCandidates.add(helper);
                }
                break;

            case DATA:
                if (this.getClusterHeadId() == null) break;
                if (packet.getSrcId() == this.getId()) break;

                // TRỪ RX energy
                double eRx = em.energyToReceive(packet.getSizeBits());
                this.consumeEnergy(eRx);
                if (!this.isAlive()) break;

                Gateway ch = net.findGatewayById(this.getClusterHeadId(), deployment);
                if (ch == null || !ch.isAlive()) break;

                // TRỪ TX energy
                double d = this.distanceTo(ch);
                double eTx = em.energyToTransmit(packet.getSizeBits(), d);
                this.consumeEnergy(eTx);
                if (!this.isAlive()) break;

                Packet forward = new Packet(
                    Packet.MessageType.DATA,
                    packet.getSrcId(),
                    ch.getId(),
                    packet.getSizeBits(),
                    packet.getPayload()
                );

                net.unicastToGateway(this, ch, forward, deployment);
                break;

                
            case HELP:
                int requesterId = (Integer) packet.getPayload();

                // Chỉ sensor thuộc CoSet mới trả lời
                if (!this.isAlive()) break;
                if (this.getClusterHeadId() == null) break;

                Packet reply = new Packet(
                    Packet.MessageType.HELP_REPLY,
                    this.getId(),
                    requesterId,
                    WSNDataSet.messageSize,
                    new Object[]{
                        this.getId(),          // sensor helper
                        this.getClusterHeadId() // CH của helper
                    }
                );

                net.unicastToSensor(this, requesterId, reply, deployment);
                break;


            default:
                // ignore
                break;
        }
    }




    // ------------ GIỬI: JOIN_REQ, 
    public void send(Packet packet, Node to, NetworkSimulator net, NodeDeployment deployment) {
        if (packet == null) return;
        
        switch (packet.getType()) {
            case JOIN_REQ:
                // JOIN_REQ chỉ gửi tới Gateway
                if (!(to instanceof Gateway)) return;

                // kiểm tra phạm vi truyền
                if (this.distanceTo(to) > this.getCommRange()) return;

                // gửi unicast qua NetworkSimulator
                net.unicastToGateway(
                    this,
                    (Gateway) to,
                    packet,
                    deployment
                );
                break;
            
                
            case HELP:
                if (!this.getComCH().isEmpty()) break;

                double d = this.getCommRange(); // hoặc distance trung bình
                double eTx = em.energyToTransmit(WSNDataSet.messageSize, d);
                this.consumeEnergy(eTx);

                net.broadcastToSensors(this, packet, deployment);
                break;

            default:
                break;
        }
    }
    
    

    public void sendData(NetworkSimulator net, NodeDeployment deployment) {
        if (!this.isAlive()) return;

        // CASE 1: CoSet → gửi thẳng CH
        if (clusterHeadId != null) {

            Gateway ch = net.findGatewayById(clusterHeadId, deployment);
            if (ch == null || !ch.isAlive()) return;

            Packet data = new Packet(
                Packet.MessageType.DATA,
                this.getId(),
                ch.getId(),
                WSNDataSet.messageSize,
                this.getId() // payload = sensorId
            );
            
            // --------- TRỪ NĂNG LƯỢNG ------------
            double d = this.distanceTo(ch);
            double eTx = em.energyToTransmit(
                    WSNDataSet.messageSize,
                    d
            );
            
            double ePacket = em.energyPerPacket(d);

            this.consumeEnergy(eTx);
            this.totalTxEnergy += eTx;
            if (!this.isAlive()) return;
            
            net.unicastToGateway(this, ch, data, deployment);
            return;
        }

        // CASE 2: UnCoSet → gửi qua backup
        if (backupNodeId != null) {
            SensorNode backup = net.findSensorById(backupNodeId, deployment);
            if (backup == null || !backup.isAlive()) return;

            Packet data = new Packet(
                Packet.MessageType.DATA,
                this.getId(),
                backup.getId(),
                WSNDataSet.messageSize,
                this.getId()
            );
            
            double d = this.distanceTo(backup);
            double eTx = em.energyToTransmit(
                    WSNDataSet.messageSize,
                    d
            );

            this.consumeEnergy(eTx);
            this.totalTxEnergy += eTx;
            if (!this.isAlive()) return;

            net.unicastToSensor(this, backup.getId(), data, deployment);
        }
    }

    // -------------- CHỌN GATEWAY DỰA TRÊN CHCOST ----------------------
    public Gateway selectClusterHead(
        NetworkSimulator net,
        CostFunction cf,
        NodeDeployment deployment,
        BaseStation bs) {

        Gateway bestCH = null;
        double bestCost = Double.NEGATIVE_INFINITY;

        for (GatewayInfo info : comCH.values()) {

            Gateway g = net.findGatewayById(info.id, deployment);
            if (g == null || !g.isAlive()) continue;
            if (g.getHopCount() == Integer.MAX_VALUE) continue;

            double cost = cf.CHCost(this, g, bs);

//            if (bestCH == null ||
//                cost > bestCost ||
//               (Math.abs(cost - bestCost) < 1e-12 &&
//                g.getMemberCount() < bestCH.getMemberCount())) {
//
//                bestCost = cost;
//                bestCH = g;
//            }

            if (bestCH == null ||
                cost > bestCost) {

                bestCost = cost;
                bestCH = g;
            }
        }
        return bestCH;
    }
    // =======================================================================
    





}

