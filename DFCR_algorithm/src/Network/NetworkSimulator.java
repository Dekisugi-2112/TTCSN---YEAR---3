package Network;

import NodeType.*;
import RunModels.PacketEnergyLogger;
import java.util.*;

public class NetworkSimulator {
    
    
    private PacketEnergyLogger packetEnergyLogger;
    private int currentRound;

    public void setPacketEnergyLogger(PacketEnergyLogger logger) {
        this.packetEnergyLogger = logger;
    }

    public PacketEnergyLogger getPacketEnergyLogger() {
        return packetEnergyLogger;
    }

    public void setCurrentRound(int round) {
        this.currentRound = round;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    
    // ----------------- IN THONG TIN CAC NODE -----------------------------------
    public static void printSensorDetails(SensorNode s) {
        System.out.println(
            "SensorNode #" + s.getId() +
            " | Pos=(" + s.getX() + ", " + s.getY() + ")" +
            " | Energy=" + s.getResidualEnergy() +
            " | Alive=" + (s.isAlive() ? "YES" : "NO") +
            " | Range=" + s.getCommRange() +
            " | CH=" + (s.getClusterHeadId() == null ? "null" : s.getClusterHeadId()) +
            " | ComCH=" + s.getComCH()
        );
    }

    public static void printSensors(List<SensorNode> sensorList) {
        System.out.println("===== SENSOR NODES (" + sensorList.size() + ") =====");
        for (SensorNode s : sensorList) {
            printSensorDetails(s);
        }
        System.out.println();
    }
    
    public static void printGatewayDetails(Gateway g) {
        System.out.println(
            "Gateway #" + g.getId() +
            " | Pos=(" + g.getX() + ", " + g.getY() + ")" +
            " | Energy=" + g.getResidualEnergy() +
            " | Range=" + g.getCommRange() +
            " | Alive=" + (g.isAlive() ? "YES" : "NO") +
            " | hopCount=" + g.getHopCount() +
            " | memberCount=" + g.getMemberCount()
        );
    }
    
    public static void printGateways(List<Gateway> gatewayList) {
        System.out.println("===== GATEWAYS (" + gatewayList.size() + ") =====");
        for (Gateway g : gatewayList) {
            printGatewayDetails(g);
        }
        System.out.println();
    }

    
    
    public static void printBaseStation(BaseStation base) {
        System.out.println("===== BASE STATION =====");
        if (base == null) {
            System.out.println("BaseStation: NOT INITIALIZED");
            return;
        }

        System.out.println(
            "BaseStation #" + base.getId() +
            " | Pos=(" + base.getX() + ", " + base.getY() + ")" +
            " | Energy=" + base.getResidualEnergy() +
            " | hopCount=" + base.getHopCount()
        );
        
        System.out.println();
    }
    
    public static void printAllNodeInfo(List<SensorNode> sensors, List<Gateway> gateways, BaseStation baseStation) {
        System.out.println("\n================= NETWORK NODE INFORMATION =================");
        printSensors(sensors);
        printGateways(gateways);
        printBaseStation(baseStation);
    }
    // =======================================================================================================

    // ------------------ BOOTSTRAPPING (1 lần duy nhất) ------------------
    // BỘ PHÁT SÓNG ĐỂ TRUYỀN MESAGE TỪ NODE ĐẾN NODE
    public void broadcastToGateways(Node sender, Packet p, NodeDeployment deployment) {
        if (sender == null || p == null || deployment== null) return;
        
        for (Gateway g : deployment.getGateways()) {
            if (g == null || !g.isAlive()) {
                continue;
            }
            if (g.getId() == sender.getId()) {
                continue;
            }
            // nằm trong phạm vi của truyền của CH
            if (sender.distanceTo(g) <= sender.getCommRange()) {
                g.receive(p, this, deployment);
            }
        }
    }
    
    public void broadcastToSensors(Node sender, Packet p, NodeDeployment deployment) {
        if (sender == null || p == null || deployment== null) return;
        
        for (SensorNode s : deployment.getSensors()) {
            if (s == null || !s.isAlive()) continue;
            if (s.getId() == sender.getId()) continue;
            if (sender.distanceTo(s) <= s.getCommRange()) {
                s.receive(p, this, deployment);
            }
        }
    }
    // ==============================================================================
    
    
    
    public static Gateway findGatewayById(int id, NodeDeployment deployment) {
        if (deployment == null || deployment.getGateways() == null) return null;

        for (Gateway g : deployment.getGateways()) {
            if (g == null) {
                continue;
            }
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }
    
    public static SensorNode findSensorById(int sensorId, NodeDeployment deployment) {

        if (deployment == null) return null;

        List<SensorNode> sensors = deployment.getSensors();
        if (sensors == null) return null;

        for (SensorNode s : sensors) {
            if (s == null) continue;

            // ánh xạ ID → object
            if (s.getId() == sensorId) {
                return s;
            }
        }
        return null;
    }




   
    public void unicastToGateway(Node sender,
                             Gateway receiver,
                             Packet packet,
                             NodeDeployment deployment) {

        if (sender == null || receiver == null || packet == null) return;
        if (!receiver.isAlive()) return;

        if (sender.distanceTo(receiver) <= sender.getCommRange()) {
            receiver.receive(packet, this, deployment);
        }
    }
    
    public void unicastToSensor(Node sender,
                            int sensorId,
                            Packet packet,
                            NodeDeployment deployment) {

        if (sender == null || packet == null || deployment == null) return;

        SensorNode dst = findSensorById(sensorId, deployment);
        if (dst == null || !dst.isAlive()) return;

        // kiểm tra phạm vi truyền
        if (sender.distanceTo(dst) > sender.getCommRange()) return;

        // gửi gói tin
        dst.receive(packet, this, deployment);
    }
    
    public void unicastToBaseStation(Node sender,
                                 Packet packet,
                                 NodeDeployment deployment) {

        if (sender == null || packet == null || deployment == null) return;

        BaseStation bs = deployment.getBaseStation();
        if (bs == null) return;

        if (sender.distanceTo(bs) <= sender.getCommRange()) {
            bs.receive(packet, this, deployment);
        }
    }




    
     


    


}

