/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Admin
 */
public class WeibullFailureModel {
    public double beta;   // shape parameter (b)
    public double eta;    // scale parameter (g) - đặt tên eta cho dễ phân biệt
    private Random random = new Random();

    public WeibullFailureModel(double beta, double eta) {
        this.beta = beta;
        this.eta = eta;
    }
    
    /**
     * Hàm mật độ xác suất f(t) của Weibull (không nhất thiết phải dùng trong mô phỏng,
     * nhưng mình viết cho đầy đủ lý thuyết theo bài báo).
     */
    public double pdf(double t) {
        if (t < 0) return 0.0;
        double x = t / eta;
        return (beta / eta) * Math.pow(x, beta - 1) * Math.exp(-Math.pow(x, beta));
    }
    
    /**
     * Hàm độ tin cậy R(t) = P(node vẫn sống đến thời điểm t)
     * R(t) = exp(-(t/eta)^beta)
     */
    
    public double reliability(double t) {
        if (t < 0) return 1.0;
        double x = t / eta;
        return Math.exp(-Math.pow(x, beta));
    }
    
    /**
     * Lấy mẫu thời điểm hỏng (failure time) T từ phân bố Weibull
     * dùng inverse CDF:
     * T = eta * [-ln(U)]^(1/beta), với U ~ Uniform(0,1)
     */
    
    public double sampleFailureTime() {
        double u = random.nextDouble();
        // tránh log(0)
        double value = -Math.log(1.0 - u);
        return eta * Math.pow(value, 1.0 / beta);
    }
    
    /**
     * Gán cho mỗi gateway một "round hỏng" dự kiến dựa trên Weibull.
     * (Đo thời gian theo đơn vị round).
     */
    
    public void assignFailureRoundsToGateways(List<Gateway> gateways) {
        for (Gateway g : gateways) {
            // Nếu bạn muốn một số gateway "rất bền", có thể random thêm điều kiện,
            // nhưng ở đây cứ gán 1 time cho tất cả.
            double t = sampleFailureTime();
            int failRound = (int) Math.round(t);    // làm tròn về round gần nhất
            if (failRound < 0) failRound = 0;
            g.setScheduledFailureRound(failRound);
        }
    }
    
    /**
     * Hàm tiện ích: Với round hiện tại, đánh dấu các gateway đã đến lúc hỏng (theo Weibull).
     * Chú ý: nếu gateway đã chết do hết năng lượng trước đó thì coi như bỏ qua.
     */
    
    public void updateGatewayFailures(List<Gateway> gateways, int currentRound) {
        for (Gateway g : gateways) {
            if (!g.isFailed() && g.isAlive()) {
                if (currentRound >= g.getScheduledFailureRound()) {
                    g.setFailed(true);
                }
            }
        }
    }
}
