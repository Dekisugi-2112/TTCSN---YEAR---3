/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * quan ly toan bo mang
 * @author Admin
 */
public class Network {
    private List<SensorNode> sensors = new ArrayList<>();
    private List<Gateway> gateways = new ArrayList<>();
    private BaseStation baseStation;
    private SimulationConfig config;
    
    private Random random = new Random();

    public Network(SimulationConfig config) {
        this.config = config;
    }
    
    public void initializeRandomNetwork() {
        // 1) Tạo base station, ví dụ đặt ở biên như trong bài: (200, 200) hoặc (fieldWidth/2, fieldHeight/2)
        baseStation = new BaseStation(-1, config.fieldWidh / 2.0, config.fieldHeight / 2.0);
        
        // 2) Tạo sensor nodes ngẫu nhiê
        for (int i = 0; i < config.numSensors; i++) {
            double x = random.nextDouble() * config.fieldWidh;
            double y = random.nextDouble() * config.fieldHeight;
            SensorNode s = new SensorNode(i, x, y, config.initialSensorEnergy);
            sensors.add(s);
        }
        
        // 3) Tạo gateways ngẫu nhiên
        for (int i = 0; i < config.numGateways; i++) {
            double x = random.nextDouble() * config.fieldWidh;
            double y = random.nextDouble() * config.fieldHeight;
            Gateway g = new Gateway(1000 + i, x, y, config.initialGatewayEnergy);
            gateways.add(g);
        }
        
    }
    
    // --- Accessors ---

    public List<SensorNode> getSensors() {
        return sensors;
    }

    public List<Gateway> getGateways() {
        return gateways;
    }

    public BaseStation getBaseStation() {
        return baseStation;
    }

    public SimulationConfig getConfig() {
        return config;
    }
    
    // --- Hàm tiện ích: khoảng cách / lân cận (sẽ dùng từ bước 4 trở đi) ---
    
    // khoảng cách giữa 2 node
    public double distance(Node a, Node b) {
        return a.distanceTo(b);
    }
    
    // Lấy danh sách gateway trong tầm sensorRange của 1 sensor
    // ComCH(si)
    public List<Gateway> getComCH(SensorNode s) {
        List<Gateway> result = new ArrayList<>();
        for (Gateway g : gateways) {
            if (!g.isFailed() && g.isAlive() && distance(s, g) <= config.sensorRange) {
                result.add(g);
            }
        }
        return result;
    }
    
    // Lấy danh sách sensor neighbor của 1 sensor
    // Neighbors(si)
    public List<SensorNode> getNeighbors(SensorNode s) {
        List<SensorNode> result = new ArrayList<>();
        for (SensorNode other : sensors) {
            if (other == s) continue;
            if (other.isAlive() && distance(s, other) <= config.sensorRange) {
                result.add(other);
            }
        }
        return result;
    }
    
    // Lấy danh sách gateway (và có thể BS) trong tầm gatewayRange của 1 gateway
    // Com(gi)
    public List<Gateway> getCom(Gateway g) {
        List<Gateway> result = new ArrayList<>();
        for (Gateway other : gateways) {
            if (other == g) continue;
            if (!other.isFailed() && other.isAlive() && distance(g, other) <= config.gatewayRange) {
                result.add(other);
            }
        }
        return result;
    }
    
    /**
     * Tính COset, UnCOset và chọn relay cho các sensor UnCOset
     * theo định nghĩa trong mục 4 của bài báo.
     *
     * - COset: các sensor có ít nhất một gateway trong tầm truyền.
     * - UnCOset: các sensor không có gateway nào trong tầm.
     * - BackupSet(si): các sensor hàng xóm thuộc COset.
     * - Relay cho si: node trong BackupSet(si) có energy lớn nhất (Eq. 4.9).
     */
    
    public CoverageInfo computeCoverageAndRelays() {
        List<SensorNode> coSet = new ArrayList<>();
        List<SensorNode> unCoSet = new ArrayList<>();
        
        // 1) Reset trạng thái covered & 
        for (SensorNode s : sensors) {
            s.setCovered(false);
            s.setRelay(null);
        }
        
        // 2) Xác định COset & UnCOset dựa trên ComCH(si)
        for (SensorNode s : sensors) {
            List<Gateway> comCH = getComCH(s);
            if (!comCH.isEmpty()) {
                s.setCovered(true);     // thuộc COset
                coSet.add(s);
            } else {
                s.setCovered(false);    // thuộc UnCOset
                unCoSet.add(s);
            }
        }
        
        // 3) Với mỗi sensor UnCOset, xây BackupSet và chọn relay theo Eq. (4.9)
        for (SensorNode si : unCoSet) {
            List<SensorNode> neighbors = getNeighbors(si);
            
            // BackupSet(si) = neighbor vừa alive vừa covered
            List<SensorNode> backupSet = new ArrayList<>();
            for (SensorNode sj : neighbors) {
                if (sj.isAlive() && sj.isCovered()) {
                    backupSet.add(sj);
                }
            }
            
            if (!backupSet.isEmpty()) {
                // Eq. (4.9): chọn sj trong BackupSet có EResidual(sj) lớn nhất
                SensorNode bestRelay = null;
                double maxEnergy = -1.0;
                
                for (SensorNode candidate : backupSet) {
                    double e = candidate.getEnergy();
                    if (e > maxEnergy) {
                        maxEnergy = e;
                        bestRelay = candidate;
                    }
                }
                
                si.setRelay(bestRelay);
                // Lưu ý: clusterHead của si sẽ được quyết định ở bước phân cụm (CHCost) sau,
                // thông qua relay này.
                
            } else {
                // Không có backup nào -> si thật sự bị "cô lập"
                si.setRelay(null);
            }
        }
        
        return new CoverageInfo(coSet, unCoSet);
    }
    
    /**
     * Kiểm tra gateway có thể giao tiếp trực tiếp với BS hay không,
     * giả sử dùng cùng một tầm truyền cho gateway-BS như gateway-gateway.
     */
    
    public boolean canGatewayReachBSDirectly(Gateway g) {
        if (baseStation == null) return false;
        return distance(g, baseStation) <= config.gatewayRange;
    }
    
    /**
     * Tính hop-count HCount(gi) cho từng gateway, mô phỏng quá trình
     * BS flood HopPacket như mô tả trong mục 4.1 của bài báo.
     *
     * - Gateways có thể nói chuyện trực tiếp với BS sẽ có hopCount = 1.
     * - Các gateway khác sẽ có hopCount > 1, tính bằng BFS trên đồ thị gateway-gateway.
     * - Gateways không kết nối được tới BS (theo graph) sẽ giữ hopCount = Integer.MAX_VALUE.
     */
    
    public void computeHopCounts() {
        // 1) Khởi tạo hopCount = vô cùng (Integer.MAX_VALUE)
        for (Gateway g : gateways) {
            g.setHopCount(Integer.MAX_VALUE);
        }
        
        // 2) Hàng đợi BFS
        java.util.Queue<Gateway> queue = new java.util.LinkedList<>();
        
        // 3) Gateways trực tiếp trong tầm BS: hopCount = 1, cho vào queue
        for (Gateway g : gateways) {
            if (!g.isFailed() && g.isAlive() && canGatewayReachBSDirectly(g)) {
                g.setHopCount(1);
                queue.add(g);
            }
        }
        
        // 4) BFS: lan truyền hop-count ra các gateway khác
        while (!queue.isEmpty()) {
            Gateway current = queue.poll();
            int currentHop = current.getHopCount();
            
            // Lấy các gateway lân cận (Com(gi))
            java.util.List<Gateway> neighbors = getCom(current);
            
            for (Gateway neighbor : neighbors) {
                if (neighbor.isFailed() || !neighbor.isAlive()) continue;
                
                int newHop = currentHop + 1;
                
                // Nếu tìm được đường đi với hop-count nhỏ hơn, cập nhật và lan tiếp
                if (newHop < neighbor.getHopCount()) {
                    neighbor.setHopCount(newHop);
                    queue.add(neighbor);
                }
            }
        }
        
        // (Tuỳ chọn) In debug kết quả hopCount
        /*
        for (Gateway g : gateways) {
            System.out.println("Gateway " + g.getId() + " hopCount = " + g.getHopCount());
        }
        */
    }
    
    
    /**
     * Thực hiện phân cụm theo DFCR:
     * - Tính lại COset / UnCOset và relay (Bước 4).
     * - Với sensor trong COset: chọn CH theo hàm CHCost (Eq. 4.13, 4.14).
     * - Với sensor trong UnCOset: gắn clusterHead = clusterHead của relay (nếu có).
     */
    
    public void formClustersDFCR() {
        // 0) Xoá cluster cũ
        clearClusters();
        
        // 1) Tính COset, UnCOset và relay
        CoverageInfo coverage = computeCoverageAndRelays();
        java.util.List<SensorNode> coSet = coverage.getCoveredNodes();
        java.util.List<SensorNode> unCoSet = coverage.getUncoveredNodes();
        
        // 2) Với mỗi sensor trong COset: chọn CH theo CHCost
        for (SensorNode sj : coSet) {
            java.util.List<Gateway> comCH = getComCH(sj);
            if (comCH.isEmpty()) {
                // Trường hợp hiếm: do gateway vừa chết giữa chừng
                sj.setCovered(false);
                unCoSet.add(sj);
                continue;
            }
            
            Gateway bestCH = null;
            double bestCost = -1.0;
        
            for (Gateway gi : comCH) {
                if (gi.isFailed() || !gi.isAlive()) continue;;  // bỏ CH chết
                
                double eResidual = gi.getEnergy();
                double d1 = distance(sj, gi);
                double d2 = distance(gi, baseStation);
                
                // Tránh chia cho 0
                if (d1 == 0 || d2 == 0) {
                    continue;
                }
                
                // CHCost(gi, sj) = EResidual(gi) / (dis(sj,gi) * dis(gi,BS))
                double cost = eResidual / (d1 * d2);
                
                if (cost > bestCost) {
                    bestCost = cost;
                    bestCH = gi;
                }
            }
            
            // Gán clusterHead nếu tìm được CH phù hợp
            if (bestCH != null) {
                bestCH.addMember(sj);   // addMember sẽ đồng thời setClusterHead(sj)
                
            } else {
                // Không tìm được CH sống -> đẩy sang UnCOset để xử lý như sensor không được phủ
                sj.setCovered(false);
                unCoSet.add(sj);
            }
        }
        
        // 3) Với mỗi sensor trong UnCOset: dùng relay để gán clusterHead (nếu có)
        for (SensorNode si : unCoSet) {
            SensorNode relay = si.getRelay();
            if (relay != null && relay.getClusterHead() != null) {
                Gateway ch = relay.getClusterHead();
                // Si thuộc cùng cluster với relay, nhưng sẽ gửi dữ liệu qua relay
                ch.addMember(si);
                // (ta vẫn để si.isCovered() = false để phân biệt là không có CH trực tiếp)
            } else {
                // Sensor này hoàn toàn cô lập, không có CH và cũng không có relay tới COset
                si.setClusterHead(null);
                si.setActive(false);    // về logic DFCR: đây là inactive node (vẫn còn năng lượng nhưng không gửi được dữ liệu)
            }
        }
        
    }
    
    /**
     * Xoá mọi thông tin phân cụm hiện tại:
     * - Xoá danh sách members của mỗi gateway
     * - Xoá clusterHead của mỗi sensor
     * - Đặt lại active = true (sẽ cập nhật lại sau)
     */
    
    private void clearClusters() {
        for (Gateway g : gateways) {
            g.clearMembers();
        }
        
        for (SensorNode s : sensors) {
            s.setClusterHead(null);
            s.setActive(true);  // tạm thời coi tất cả active, sẽ đánh dấu lại những node thật sự "cô lập"
        }
    }
    
    
        /**
     * Phục hồi các sensor của một Cluster Head (gateway) bị hỏng
     * theo mục 4.2.1 của bài báo:
     * - Mỗi member sensor của failedCH sẽ:
     *   + Thử tìm CH mới trong ComCH(si) (trừ CH đã chết).
     *   + Nếu không có CH trong tầm, tìm BackupSet si từ hàng xóm đã có CH
     *     và chọn relay có năng lượng lớn nhất.
     *   + Nếu không có luôn cả backup, sensor trở thành inactive.
     */
    public void recoverFromCHFailure(Gateway failedCH) {
        // Sao chép danh sách member hiện tại vì lát nữa sẽ clear
        java.util.List<SensorNode> affectedSensors =
                new java.util.ArrayList<>(failedCH.getMembers());

        // Xóa danh sách members của CH chết (cluster này coi như giải tán)
        failedCH.clearMembers();

        for (SensorNode si : affectedSensors) {
            // Reset trạng thái cluster của sensor
            si.setClusterHead(null);
            si.setActive(true);  // tạm cho là active, sẽ quyết định lại bên dưới
            si.setCovered(false);
            si.setRelay(null);

            // 1) Thử tìm CH mới: ComCH(si) (gateway trong tầm, trừ CH chết)
            java.util.List<Gateway> comCH = getComCH(si); // getComCH đã bỏ qua gateway isFailed()

            Gateway bestCH = null;
            double bestCost = -1.0;

            for (Gateway gi : comCH) {
                if (gi == failedCH) continue;          // bỏ CH vừa chết
                if (gi.isFailed() || !gi.isAlive()) continue;

                double eResidual = gi.getEnergy();
                double d1 = distance(si, gi);
                double d2 = distance(gi, baseStation);

                if (d1 == 0 || d2 == 0) continue;

                double cost = eResidual / (d1 * d2);   // CHCost(gi, si)

                if (cost > bestCost) {
                    bestCost = cost;
                    bestCH = gi;
                }
            }

            if (bestCH != null) {
                // Tìm được CH mới trực tiếp
                bestCH.addMember(si);
                si.setCovered(true);
                si.setRelay(null);
                si.setActive(true);
                continue;
            }

            // 2) Không có CH nào trong tầm -> xử lý như UnCOset:
            //   Tìm BackupSet(si) = hàng xóm đã có CH

            java.util.List<SensorNode> neighbors = getNeighbors(si);
            java.util.List<SensorNode> backupSet = new java.util.ArrayList<>();

            for (SensorNode sj : neighbors) {
                // Hàng xóm còn sống và đã có CH => coi như "covered"
                if (sj.isAlive() && sj.getClusterHead() != null) {
                    backupSet.add(sj);
                }
            }

            if (!backupSet.isEmpty()) {
                // Chọn relay với EResidual lớn nhất (Eq. 4.9)
                SensorNode bestRelay = null;
                double maxEnergy = -1.0;
                for (SensorNode candidate : backupSet) {
                    double e = candidate.getEnergy();
                    if (e > maxEnergy) {
                        maxEnergy = e;
                        bestRelay = candidate;
                    }
                }

                // Gán relay và đưa si vào cluster của relay
                si.setRelay(bestRelay);
                Gateway relayCH = bestRelay.getClusterHead();
                if (relayCH != null && !relayCH.isFailed()) {
                    relayCH.addMember(si);
                    // si không có CH trực tiếp nên covered = false, nhưng vẫn active
                    si.setCovered(false);
                    si.setActive(true);
                } else {
                    // CH của relay lại vừa chết, coi như không dùng được
                    si.setRelay(null);
                    si.setActive(false);
                }
            } else {
                // 3) Không có backup luôn -> si trở thành inactive sensor node
                si.setRelay(null);
                si.setCovered(false);
                si.setActive(false);    // còn pin nhưng không gửi dữ liệu được
            }
        }
    }
    
    
        /**
     * Tìm tất cả gateway (CH) đã bị hỏng (isFailed == true)
     * nhưng vẫn còn giữ danh sách members, rồi phục hồi các sensor đó.
     * Nên gọi hàm này sau khi cập nhật trạng thái hỏng của gateway
     * (WeibullFailureModel + hết năng lượng) trong mỗi round.
     */
    public void recoverFromFailedGateways() {
        for (Gateway g : gateways) {
            // Chỉ phục hồi nếu gateway đã fail và vẫn còn members chưa xử lý
            if (g.isFailed() && !g.getMembers().isEmpty()) {
                recoverFromCHFailure(g);
            }
        }
    }
    
    
        /**
     * BackCH(g): tập các gateway lân cận có hopCount LỚN HƠN g,
     * theo Eq. (4.5) trong bài.
     * Đây là "backward CHs" - những CH phía sau có thể chọn g làm next-hop.
     */
    public java.util.List<Gateway> getBackCH(Gateway g) {
        java.util.List<Gateway> result = new java.util.ArrayList<>();
        int h = g.getHopCount();

        // Com(g) = các gateway neighbor trong tầm
        java.util.List<Gateway> neighbors = getCom(g);
        for (Gateway other : neighbors) {
            if (other.isFailed() || !other.isAlive()) continue;
            if (other.getHopCount() > h) { // HCount(other) > HCount(g)
                result.add(other);
            }
        }
        return result;
    }

        /**
     * Chọn next-hop cho gateway gj theo Cost function (Eq. 4.15, 4.16).
     * - Nếu hopCount == 1 và có thể tới BS trực tiếp: nextHop = null (gửi thẳng BS).
     * - Ngược lại:
     *   + Xét các neighbor gr có hopCount < hopCount(gj).
     *   + Tính Cost(gj, gr) = E_res(gr) / (d_jr * d_rBS * |BackCH(gr)|).
     *   + Chọn gr có Cost lớn nhất làm nextHop.
     * - Nếu không tìm được candidate nào -> nextHop = null (không có đường về BS).
     */
    private void chooseNextHopForGateway(Gateway gj) {
        // Nếu gateway đã chết hoặc bị hỏng -> không có next hop
        if (gj.isFailed() || !gj.isAlive()) {
            gj.setNextHop(null);
            return;
        }

        int hop = gj.getHopCount();

        // CH với hopCount = 1 và trong tầm BS -> gửi trực tiếp, không đặt nextHop CH
        if (hop == 1 && canGatewayReachBSDirectly(gj)) {
            gj.setNextHop(null);
            return;
        }

        java.util.List<Gateway> neighbors = getCom(gj);
        Gateway bestNext = null;
        double bestCost = -1.0;

        for (Gateway gr : neighbors) {
            if (gr.isFailed() || !gr.isAlive()) continue;

            // Chỉ xét CH có hopCount nhỏ hơn (gần BS hơn)
            if (gr.getHopCount() >= hop) continue;

            double eResidual = gr.getEnergy();
            double d1 = distance(gj, gr);          // dis(gj, gr)
            double d2 = distance(gr, baseStation); // dis(gr, BS)

            if (d1 == 0 || d2 == 0) continue;

            java.util.List<Gateway> backCH = getBackCH(gr);
            int backSize = backCH.size();

            // Tránh chia cho 0 nếu vì lý do nào đó BackCH rỗng
            if (backSize == 0) {
                backSize = 1;
            }

            // Cost(gj, gr) = E_res(gr) / (d1 * d2 * |BackCH(gr)|)
            double cost = eResidual / (d1 * d2 * backSize);

            if (cost > bestCost) {
                bestCost = cost;
                bestNext = gr;
            }
        }

        gj.setNextHop(bestNext);
    }

        /**
     * Tính nextHop cho TẤT CẢ gateway theo thuật toán DFCR.
     * Nên gọi hàm này sau khi đã có hopCount (computeHopCounts)
     * và sau khi cập nhật trạng thái gateway (sống/chết).
     */
    public void computeNextHopsDFCR() {
        for (Gateway g : gateways) {
            chooseNextHopForGateway(g);
        }
    }



}
