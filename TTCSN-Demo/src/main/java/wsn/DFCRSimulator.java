package wsn;

import java.util.ArrayList;
import java.util.List;

public class DFCRSimulator {

    private final SimulationConfig config;
    private final Network network;
    private final EnergyModel energyModel;
    private final WeibullFailureModel failureModel;

    private int maxRounds;

    // Thống kê
    private List<Integer> deadCHsOverTime = new ArrayList<>();
    private List<Integer> inactiveSensorsOverTime = new ArrayList<>();
    private List<Integer> packetsToBSOverTime = new ArrayList<>();
    private List<Double> totalEnergyOverTime = new ArrayList<>();
    private List<Double> stdDevCHEnergyOverTime = new ArrayList<>();
    private List<Double> avgEnergyPerPacketOverTime = new ArrayList<>();

    private int totalPacketsDeliveredToBS = 0;

    public DFCRSimulator(SimulationConfig config, int maxRounds) {
        this.config = config;
        this.maxRounds = maxRounds;

        this.network = new Network(config);
        this.energyModel = new EnergyModel(config);

        // Trong bài: g = 3200, b = 3 (ví dụ)
        this.failureModel = new WeibullFailureModel(3.0, 3200.0);

        // Khởi tạo mạng
        network.initializeRandomNetwork();

        // Gán thời điểm hỏng (round) cho mỗi gateway theo Weibull
        failureModel.assignFailureRoundsToGateways(network.getGateways());

        // Tính hop-count ban đầu
        network.computeHopCounts();

        // Phân cụm ban đầu
        network.formClustersDFCR();

        // Tính nextHop ban đầu cho các gateway
        network.computeNextHopsDFCR();
    }

    public void run() {
        for (int round = 0; round < maxRounds; round++) {
            System.out.println("=== Round " + round + " ===");

            // 1) Cập nhật CH hỏng theo Weibull
            failureModel.updateGatewayFailures(network.getGateways(), round);

            // 2) Phục hồi cluster cho CH vừa hỏng
            network.recoverFromFailedGateways();

            // 3) Tính lại hop-count & next-hop (sau khi topology đổi)
            network.computeHopCounts();
            network.computeNextHopsDFCR();

            // 4) Pha truyền dữ liệu (steady-state)
            performDataTransmissionRound();

            // 5) Ghi thống kê
            recordStatistics(round);

            // 6) Điều kiện dừng sớm: nếu tất cả sensor đã chết hoặc inactive
            if (allSensorsDeadOrInactive()) {
                System.out.println("All sensors are dead or inactive at round " + round);
                break;
            }
        }

        System.out.println("Simulation finished. Total packets delivered to BS = " + totalPacketsDeliveredToBS);
    }

    /**
     * Pha truyền dữ liệu trong 1 round:
     * - Sensor -> CH (trực tiếp hoặc qua relay).
     * - CH aggregate dữ liệu.
     * - CH gửi dữ liệu lên BS qua multi-hop CH.
     */
    private void performDataTransmissionRound() {
        // 1) Sensor gửi dữ liệu lên CH
        // Đếm số packet mà mỗi CH nhận được trong round này
        var packetsReceivedAtCH = new java.util.HashMap<Gateway, Integer>();

        for (SensorNode s : network.getSensors()) {
            if (!s.isAlive() || !s.isActive()) continue;

            Gateway ch = s.getClusterHead();
            if (ch == null || ch.isFailed() || !ch.isAlive()) {
                // không có CH hợp lệ
                s.setActive(false);
                continue;
            }

            // Sensor thuộc COset (covered = true, không có relay)
            if (s.isCovered() || s.getRelay() == null) {
                // Truyền trực tiếp sensor -> CH
                double d = network.distance(s, ch);
                energyModel.consumeTxEnergy(s, config.DATA_PACKET_SIZE, d);
                energyModel.consumeRxEnergy(ch, config.DATA_PACKET_SIZE);

                if (!ch.isFailed() && ch.isAlive()) {
                    packetsReceivedAtCH.put(ch, packetsReceivedAtCH.getOrDefault(ch, 0) + 1);
                }
            } else {
                // Sensor thuộc UnCOset: sensor -> relay -> CH
                SensorNode relay = s.getRelay();

                if (relay == null || !relay.isAlive() || relay.getClusterHead() == null) {
                    s.setActive(false);
                    continue;
                }

                Gateway relayCH = relay.getClusterHead();
                if (relayCH == null || relayCH.isFailed() || !relayCH.isAlive()) {
                    s.setActive(false);
                    continue;
                }

                // s -> relay
                double d1 = network.distance(s, relay);
                energyModel.consumeTxEnergy(s, config.DATA_PACKET_SIZE, d1);
                energyModel.consumeRxEnergy(relay, config.DATA_PACKET_SIZE);

                // relay -> CH
                double d2 = network.distance(relay, relayCH);
                energyModel.consumeTxEnergy(relay, config.DATA_PACKET_SIZE, d2);
                energyModel.consumeRxEnergy(relayCH, config.DATA_PACKET_SIZE);

                if (!relayCH.isFailed() && relayCH.isAlive()) {
                    packetsReceivedAtCH.put(relayCH, packetsReceivedAtCH.getOrDefault(relayCH, 0) + 1);
                }
            }
        }

        // 2) CH aggregate dữ liệu & gửi gói tổng hợp tới BS
        for (Gateway ch : network.getGateways()) {
            if (ch.isFailed() || !ch.isAlive()) continue;

            int numPackets = packetsReceivedAtCH.getOrDefault(ch, 0);
            if (numPackets <= 0) continue;

            // Năng lượng aggregate tại CH
            energyModel.consumeAggregationEnergy(ch, config.DATA_PACKET_SIZE, numPackets);

            // Gửi 1 gói tổng hợp lên BS qua multi-hop
            sendAggregatedPacketToBS(ch);
        }
    }

    /**
     * Gửi 1 gói tin tổng hợp (DATA_PACKET_SIZE) từ ch đến BS
     * sử dụng nextHop của mỗi gateway.
     */
    private void sendAggregatedPacketToBS(Gateway startCH) {
        Gateway current = startCH;
        int hopLimit = 1000; // tránh vòng lặp vô hạn, phòng bug

        while (hopLimit-- > 0 && current != null && current.isAlive() && !current.isFailed()) {
            // Nếu current có hopCount = 1 và trong tầm BS -> gửi thẳng BS
            if (current.getHopCount() == 1 && network.canGatewayReachBSDirectly(current)) {
                double d = network.distance(current, network.getBaseStation());
                energyModel.consumeTxEnergy(current, config.DATA_PACKET_SIZE, d);
                // Không tính năng lượng BS

                totalPacketsDeliveredToBS++;
                return;
            }

            // Ngược lại: gửi tới nextHop CH
            Gateway next = current.getNextHop();
            if (next == null || !next.isAlive() || next.isFailed()) {
                // không có đường nào về BS nữa
                return;
            }

            double d = network.distance(current, next);
            energyModel.consumeTxEnergy(current, config.DATA_PACKET_SIZE, d);
            energyModel.consumeRxEnergy(next, config.DATA_PACKET_SIZE);

            current = next;
        }
        // Nếu thoát vì hopLimit hết, coi như gói bị mất
    }

    /**
     * Ghi lại các thống kê sau mỗi round.
     */
    private void recordStatistics(int round) {
        int deadCHs = 0;
        double totalEnergy = 0.0;
        int inactiveSensors = 0;

        // Gateway stats
        List<Gateway> gateways = network.getGateways();
        for (Gateway g : gateways) {
            if (g.isFailed() || !g.isAlive()) {
                deadCHs++;
            }
            totalEnergy += g.getEnergy();
        }

        // Sensor stats
        for (SensorNode s : network.getSensors()) {
            totalEnergy += s.getEnergy();
            if (s.isAlive() && !s.isActive()) {
                inactiveSensors++;
            }
        }

        deadCHsOverTime.add(deadCHs);
        inactiveSensorsOverTime.add(inactiveSensors);
        packetsToBSOverTime.add(totalPacketsDeliveredToBS);
        totalEnergyOverTime.add(totalEnergy);

        // Độ lệch chuẩn năng lượng CH
        stdDevCHEnergyOverTime.add(computeStdDevCHEnergy());

        // Năng lượng tiêu thụ trung bình trên mỗi packet tới BS
        double avgEnergyPerPacket = 0.0;
        if (totalPacketsDeliveredToBS > 0) {
            double initialTotalEnergy = config.initialSensorEnergy * config.numSensors
                    + config.initialGatewayEnergy * config.numGateways;
            double energyConsumed = initialTotalEnergy - totalEnergy;
            avgEnergyPerPacket = energyConsumed / totalPacketsDeliveredToBS;
        }
        avgEnergyPerPacketOverTime.add(avgEnergyPerPacket);
    }

    /**
     * Tính độ lệch chuẩn năng lượng còn lại của các gateway (CH).
     */
    private double computeStdDevCHEnergy() {
        List<Gateway> gateways = network.getGateways();
        int n = gateways.size();
        if (n == 0) return 0.0;

        double sum = 0.0;
        for (Gateway g : gateways) {
            sum += g.getEnergy();
        }
        double mean = sum / n;

        double sumSq = 0.0;
        for (Gateway g : gateways) {
            double diff = g.getEnergy() - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / n);
    }

    /**
     * Kiểm tra xem tất cả sensor đã chết hoặc inactive chưa.
     */
    private boolean allSensorsDeadOrInactive() {
        for (SensorNode s : network.getSensors()) {
            if (s.isAlive() && s.isActive()) {
                return false;
            }
        }
        return true;
    }

    // --- Getter cho thống kê nếu muốn vẽ đồ thị bên ngoài ---

    public List<Integer> getDeadCHsOverTime() {
        return deadCHsOverTime;
    }

    public List<Integer> getInactiveSensorsOverTime() {
        return inactiveSensorsOverTime;
    }

    public List<Integer> getPacketsToBSOverTime() {
        return packetsToBSOverTime;
    }

    public List<Double> getTotalEnergyOverTime() {
        return totalEnergyOverTime;
    }

    public List<Double> getStdDevCHEnergyOverTime() {
        return stdDevCHEnergyOverTime;
    }

    public List<Double> getAvgEnergyPerPacketOverTime() {
        return avgEnergyPerPacketOverTime;
    }
}
