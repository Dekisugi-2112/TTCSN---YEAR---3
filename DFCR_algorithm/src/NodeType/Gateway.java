package NodeType;

import RunModels.WSNDataSet;
import Network.*;
import java.util.*;

public class Gateway extends Node {

    private EnergyModel em = new EnergyModel();

    // ================= DFCR STATE =================
    private int hopCount = Integer.MAX_VALUE;
    private int memberCount = 0;

    private double directDistanceToBS;
    private double pathDistanceToBS;

    private Integer nextHopId = null;

    // ================= TDMA =================
    private List<Integer> tdmaSlots = new ArrayList<>();
    private List<Integer> memberSensorIds = new ArrayList<>();

    // ================= HOP FLOOD CONTROL =================
    private Set<Long> seenHops = new HashSet<>();

    // =====================================================
    public Gateway(int id, double x, double y, double energy, double commRange) {
        super(id, x, y, energy, commRange);
    }

    // ================= GET / SET =================
    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public Integer getNextHopId() { return nextHopId; }
    public void setNextHopId(Integer id) { this.nextHopId = id; }

    public double getDirectDistanceToBS() { return directDistanceToBS; }
    public double getPathDistanceToBS() { return pathDistanceToBS; }

    public int getMemberCount() { return memberCount; }

    // ================= TDMA =================
    public void addMember(int sensorId) {
        memberSensorIds.add(sensorId);
        memberCount++;
        tdmaSlots.add(sensorId);
    }

    public List<Integer> getTDMASlots() {
        return tdmaSlots;
    }

    public void clearTDMASlots() {
        tdmaSlots.clear();
    }

    public void resetMemberCount() {
        memberCount = 0;
        memberSensorIds.clear();
    }

    // =====================================================
    // ================= RECEIVE ===========================
    // =====================================================
    public void receive(Packet packet, NetworkSimulator net, NodeDeployment deployment) {
        if (packet == null || !this.isAlive()) return;

        switch (packet.getType()) {

            // ---------------- HELLO từ BS ----------------
            case HELLO: {
                BaseStation bs = deployment.getBaseStation();
                if (packet.getSrcId() != bs.getId()) return;

                double d = this.distanceTo(bs);
                directDistanceToBS = d;
                pathDistanceToBS   = d;
                break;
            }

            // ---------------- HOP flood ----------------
            case HOP: {
                if (!(packet.getPayload() instanceof Integer)) return;
                int counter = (Integer) packet.getPayload();

                long key = (((long) packet.getSrcId()) << 32) | counter;
                if (seenHops.contains(key)) return;
                seenHops.add(key);

                if (counter < hopCount) {
                    hopCount = counter;

                    Gateway prev = net.findGatewayById(packet.getSrcId(), deployment);
                    if (prev != null) {
                        pathDistanceToBS =
                                this.distanceTo(prev) + prev.getPathDistanceToBS();
                    }

                    Packet forward = new Packet(
                            Packet.MessageType.HOP,
                            this.getId(),
                            Packet.BROADCAST_ID,
                            0,
                            counter + 1
                    );

                    net.broadcastToGateways(this, forward, deployment);
                }
                break;
            }

            // ---------------- JOIN REQUEST ----------------
            case JOIN_REQ: {
                if (hopCount == Integer.MAX_VALUE) return;

                int sensorId = (Integer) packet.getPayload();
                addMember(sensorId);

                Packet ack = new Packet(
                        Packet.MessageType.JOIN_ACK,
                        this.getId(),
                        sensorId,
                        WSNDataSet.messageSize,
                        this.getId()
                );

                net.unicastToSensor(this, sensorId, ack, deployment);
                break;
            }

            // ---------------- DATA ----------------
            case DATA: {

                // ===== RX =====
                double eRx = em.energyToReceive(packet.getSizeBits());
                consumeEnergy(eRx);
                if (!isAlive()) return;

                packet.incrementHop();

                // ===== DATA AGGREGATION (nếu từ sensor) =====
                if (packet.getSrcId() < 10000) {
                    double eDA = em.energyToAggregate(packet.getSizeBits());
                    consumeEnergy(eDA);
                    if (!isAlive()) return;
                }

                // ===== FORWARD =====
                forwardData(packet, net, deployment);
                break;
            }

            default:
                break;
        }
    }

    // =====================================================
    // ================= FORWARD DATA ======================
    // =====================================================
    private void forwardData(Packet packet,
                             NetworkSimulator net,
                             NodeDeployment deployment) {

        // ===== CH → BS =====
        if (hopCount == 1) {
            BaseStation bs = deployment.getBaseStation();
            double d = this.distanceTo(bs);

            // LOG năng lượng / gói
            if (net.getPacketEnergyLogger() != null) {
                net.getPacketEnergyLogger().log(
                        net.getCurrentRound(),
                        "DATA",
                        this.getId(),
                        "BS",
                        d,
                        em.energyPacketToBS(d)
                );
            }

            double eTx = em.energyToTransmit(packet.getSizeBits(), d);
            consumeEnergy(eTx);
            if (!isAlive()) return;

            net.unicastToBaseStation(this, packet, deployment);
            return;
        }

        // ===== CH → CH =====
        if (nextHopId == null) return;

        Gateway next = net.findGatewayById(nextHopId, deployment);
        if (next == null || !next.isAlive()) return;

        double d = this.distanceTo(next);

        if (net.getPacketEnergyLogger() != null) {
            net.getPacketEnergyLogger().log(
                    net.getCurrentRound(),
                    "DATA",
                    this.getId(),
                    "G" + next.getId(),
                    d,
                    em.energyPerPacket(d)
            );
        }

        double eTx = em.energyToTransmit(packet.getSizeBits(), d);
        consumeEnergy(eTx);
        if (!isAlive()) return;

        net.unicastToGateway(this, next, packet, deployment);
    }
}
