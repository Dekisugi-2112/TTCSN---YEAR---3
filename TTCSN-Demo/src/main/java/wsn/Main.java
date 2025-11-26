package wsn;

public class Main {
    public static void main(String[] args) {
        SimulationConfig config = new SimulationConfig();

        // (tuỳ ý chỉnh config: fieldWidth, numSensors, numGateways,...)

        int maxRounds = 3000; // giống cỡ trong bài mô phỏng
        DFCRSimulator simulator = new DFCRSimulator(config, maxRounds);

        simulator.run();

        // Sau đó có thể lấy thống kê để vẽ đồ thị bằng Excel, Python, ...
        // simulator.getDeadCHsOverTime();
        // simulator.getPacketsToBSOverTime();
        // ...
    }
}
