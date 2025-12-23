package RunModels;

import Network.*;
import NodeType.*;
import gui.*;
import java.io.IOException;

import javax.swing.SwingUtilities;
import java.util.List;

public class Main {
    
    

    // ================== METRIC ==================
    private static int countAliveSensors(List<SensorNode> sensors) {
        int cnt = 0;
        for (SensorNode s : sensors) {
            if (s.isAlive()) cnt++;
        }
        return cnt;
    }
    
    private static int countDeadGateways(List<Gateway> gateways) {
        int dead = 0;
        for (Gateway g : gateways) {
            if (!g.isAlive()) {
                dead++;
            }
        }
        return dead;
    }

    
    private static double totalNetworkEnergy(
        List<SensorNode> sensors,
        List<Gateway> gateways
    ) {
        double sum = 0.0;

        for (SensorNode s : sensors) {
            sum += s.getResidualEnergy();
        }

        for (Gateway g : gateways) {
            sum += g.getResidualEnergy();
        }

        return sum;
    }


    // ================== MAIN ==================
    public static void main(String[] args) throws IOException {
        WSNDataSet data = new WSNDataSet();
        NetworkSimulator network = new NetworkSimulator();
        CostFunction cf = new CostFunction();
        
        
        
        // ---------------- LÁY DATA TỪ CSV ---------------
        System.out.println("Lấy dataset thứ: ");
        int choice = 2;
        String datasetPath = "datasets/dataset_" + choice + "/";
        
        List<SensorNode> sensors = CSVReader.readSensors(datasetPath + "sensors.csv");

        List<Gateway> gateways = CSVReader.readGateways(datasetPath + "gateways.csv");

        BaseStation bs = CSVReader.readBaseStation(datasetPath + "base_station.csv");
        
        NodeDeployment deployment = new NodeDeployment(bs, sensors, gateways);
        
        // --------------- VẼ -------------------
        NetworkFrame frame = new NetworkFrame(deployment);



        

        // -----------  BOOTSTRAPPING ----------------
        
        // BS → HOPPACKET (tính hopCount + pathDistance)
        Packet hopPacket = new Packet(
            Packet.MessageType.HOP,
            bs.getId(),
            -1,
            0,
            1
        );
        bs.send(hopPacket, null, network, deployment);
        
//         BS → HELLO (đo khoảng cách trực tiếp)
        Packet bsHelloCH = new Packet(
            Packet.MessageType.HELLO,
            bs.getId(),
            -1,
            0,
            null
        );
        
        bs.send(bsHelloCH, null, network, deployment);

        try {
            CSVLogger csvLogger = new CSVLogger(
                "results/dfcr_dead_chs_wsn" + choice + ".csv",
                "round,deadCH"
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // ==============================
        CSVLogger aliveLogger;
        try {
            aliveLogger = new CSVLogger(
                "results/alive_sensors_wsn" + choice + ".csv",
                "round,aliveSensors"
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        CSVLogger energyLogger = new CSVLogger(
            "results/energy_per_round_wsn" + choice + ".csv",
            "round,energyConsumed"
        );
        
        CSVLogger csvLogger = new CSVLogger(
            "results/dfcr_dead_chs_wsn" + choice + ".csv",
            "round,deadCH"
        );
        
        PacketEnergyLogger packetLogger;
        try {
            packetLogger = new PacketEnergyLogger(
                "results/energy_per_packet_wsn" + choice + ".csv"
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        

        
        
        int roundFND = -1;
        int roundHND = -1;
        int roundLND = -1;


            // -------------- ROUND-BASED SIMULATION -----------------
        for (int round = 1; round <= data.MAX_ROUND; round++) {
            int alive = countAliveSensors(sensors);
            network.setCurrentRound(round);

            

//            /* ---------------------------------------------------------
//               RESET ROUND STATE
//            --------------------------------------------------------- */
            for (Gateway g : gateways) {
                g.resetMemberCount();
                g.clearTDMASlots();
                g.setNextHopId(null);
            }

            for (SensorNode s : sensors) {
                s.clearComCH();
                s.clearBackupCandidates();
                s.setClusterHeadId(null);
                s.setBackupNodeId(null);
            }
            
            
            double energyBeforeRound = totalNetworkEnergy(sensors, gateways);


            // ------------ HELLO PHASE (Gateway → Sensor) --------------------
// ------------ CH ADVERTISEMENT PHASE (Gateway → Sensor) ----------
            for (Gateway g : gateways) {
                if (!g.isAlive()) continue;

                // Chỉ CH có đường về BS mới được quảng bá
                if (g.getHopCount() == Integer.MAX_VALUE) continue;

                Packet advertise = new Packet(
                    Packet.MessageType.HELLO,   // dùng HELLO cho đơn giản
                    g.getId(),
                    Packet.BROADCAST_ID,
                    0,
                    new Object[]{
                        g.getId(),                // CH id
                        g.getResidualEnergy(),    // năng lượng CH
                        g.getHopCount()           // hopCount về BS
                    }
                );

                // Gateway broadcast tới sensor
                network.broadcastToSensors(g, advertise, deployment);
            }

            // =======================================================================
            
            // ---------------- CLUSTERING PHASE (Sensor chọn CH) -------------------
            for (SensorNode s : sensors) {
                if (!s.isAlive()) continue;
                if (s.getComCH().isEmpty()) continue;

                Gateway ch = s.selectClusterHead(network, cf, deployment, bs);
                if (ch == null) continue;

                s.setClusterHeadId(ch.getId());

                Packet join = new Packet(
                    Packet.MessageType.JOIN_REQ,
                    s.getId(),
                    ch.getId(),
                    0,
                    s.getId()
                );
                s.send(join, ch, network, deployment);
            }
            // ====================================================================================

            // ------------------------ ROUTING PHASE (Gateway chọn next-hop) -------------------
            // ---------------- ROUTING PHASE (Gateway chọn next-hop) ----------------
            for (Gateway g : gateways) {
                if (!g.isAlive()) continue;

                Gateway next = cf.selectNextHopForGateway(
                        g,
                        gateways,
                        bs
                );

                if (next != null) {
                    g.setNextHopId(next.getId());
                } else {
                    g.setNextHopId(null);
                }
            }

            // ===============================================================

            // ------------------- HELP + BACKUP PHASE (UnCoSet) --------------------
            for (SensorNode s : sensors) {
                if (!s.isAlive()) continue;
                if (!s.getComCH().isEmpty()) continue;

                Packet help = new Packet(
                    Packet.MessageType.HELP,
                    s.getId(),
                    -1,
                    0,
                    s.getId()
                );
                s.send(help, null, network, deployment);
            }
            // =====================================

            // 
            for (SensorNode s : sensors) {
                if (!s.isAlive()) continue;
                if (!s.getComCH().isEmpty()) continue;

                SensorNode backup = cf.chooseBackupSensor(
                    s.getBackupCandidates(),
                    s,
                    bs,
                    gateways
                );

                if (backup != null) {
                    s.setBackupNodeId(backup.getId());
                }
            }

            // ---------------- DATA PHASE (TDMA) --------------------
            
            for (Gateway g : gateways) {
                if (!g.isAlive()) continue;

                List<Integer> slots = g.getTDMASlots();
                if (slots.isEmpty()) continue;

                for (Integer sid : slots) {
                    SensorNode s = network.findSensorById(sid, deployment);
                    if (s == null || !s.isAlive()) continue;
                    s.sendData(network, deployment);
                }
            }
            
            // ----------- vẽ biểu đồ ----------------
            double energyAfterRound = totalNetworkEnergy(sensors, gateways);
            double energyConsumedRound = energyBeforeRound - energyAfterRound;

            energyLogger.logEnergy(round, energyConsumedRound);
            
            // -----------------------
            int deadCH = countDeadGateways(gateways);
            csvLogger.logDeadCH(round, gateways);
            
            // =============================================


            // ----------------- METRIC + CSV LOGGING ----------------------

            if (alive < sensors.size() && roundFND == -1) {
                roundFND = round;
            }
            if (alive <= sensors.size() / 2 && roundHND == -1) {
                roundHND = round;
            }
            if (alive == 0) {
                roundLND = round;
            }
            // ===========================================
            // --------------- SỐ SENSOR SỐNG TRONG MỖI ROUND ------------

            
            // =======================================
            

            // ===== GHI CSV: Dead CHs =====
            aliveLogger.logAliveSensors(round, alive);


            if (alive == 0) break;
            
            network.setPacketEnergyLogger(packetLogger);

        }


        // Đóng CSV
        csvLogger.close();
        aliveLogger.close();
        System.out.println("CSV exported: dfcr_dead_chs_wsn1.csv");

        // ------------------- GUI ---------------
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });

    }
}
