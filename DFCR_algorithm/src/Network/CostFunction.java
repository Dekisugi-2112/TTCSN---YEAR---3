/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Network;

import NodeType.Gateway;
import NodeType.*;
import java.util.*;

/**
 *
 * @author Admin
 */
public class CostFunction {
    // ------------- FEATURE --------------
    private static final double EPS = 1e-9;


    // CT: 4.13 và 4.14 Chọn CH tối ưu 
    public double CHCost(SensorNode s, Gateway g, BaseStation bs) {

        if (s == null || g == null || bs == null) {
            return Double.NEGATIVE_INFINITY;
        }

        if (!s.isAlive() || !g.isAlive()) {
            return Double.NEGATIVE_INFINITY;
        }

        if (g.getHopCount() == Integer.MAX_VALUE) {
            return Double.NEGATIVE_INFINITY; // không có route về BS
        }

        double d1 = Math.max(EPS, s.distanceTo(g));
//        double d2 = Math.max(EPS, g.getPathDistanceToBS());
        double d2 = Math.max(EPS, g.distanceTo(bs));
        double Eg = g.getResidualEnergy();

        return Eg / (d1 * d2);
    }
    
    


    // ---------------------------------------------------------------------------------------
    
    // CT: chọn sensor tối ưu nhất từ BackupSet(si)
    public SensorNode chooseBackupSensor(
            List<SensorNode> backupSet,
            SensorNode si,
            BaseStation bs,
            List<Gateway> gateways) {

        final double EPS = 1e-9;

        // Trọng số (có thể ghi rõ trong báo cáo)
        double wDist  = 0.4;  // gần sensor bị cô lập
        double wEnergy = 0.3; // năng lượng của backup
        double wRoute = 0.2;  // chất lượng route về BS
        double wLoad  = 0.1;  // tải CH

        if (backupSet == null || backupSet.isEmpty()
                || si == null || !si.isAlive())
            return null;

        SensorNode best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (SensorNode sj : backupSet) {
            if (sj == null || !sj.isAlive()) continue;

            /* ---------------- 1. Distance component ---------------- */
            double d_sisj = Math.max(EPS, si.distanceTo(sj));
            double compDist = 1.0 / d_sisj;

            /* ---------------- 2. Energy of backup sensor ------------ */
            double compEnergy = sj.getResidualEnergy();

            /* ---------------- 3. Route quality via CH --------------- */
            double compRoute = 0.0;
            double compLoad  = 0.0;

            Integer chId = sj.getClusterHeadId();
            if (chId != null) {
                Gateway ch = null;
                for (Gateway g : gateways) {
                    if (g.getId() == chId) {
                        ch = g;
                        break;
                    }
                }

                if (ch != null && ch.isAlive()
                        && ch.getHopCount() != Integer.MAX_VALUE) {

                    // dùng MULTI-HOP distance (đúng DFCR)
                    double d_ch_bs = Math.max(EPS, ch.getPathDistanceToBS());

                    // route quality: CH nhiều năng lượng + đường về BS ngắn
                    compRoute = ch.getResidualEnergy() / d_ch_bs;

                    // penalty cho CH quá tải
                    compLoad = 1.0 / (1.0 + ch.getMemberCount());
                }
            }

            /* ---------------- 4. Tổng điểm ---------------- */
            double score =
                    wDist  * compDist +
                    wEnergy * compEnergy +
                    wRoute * compRoute +
                    wLoad  * compLoad;

            if (score > bestScore) {
                bestScore = score;
                best = sj;
            }
        }

        return best;
    }
    
    // ====================================================
    public Gateway selectNextHopForGateway(
        Gateway gi,
        List<Gateway> gateways,
        BaseStation bs
    ) {

        if (gi == null || !gi.isAlive())
            return null;

        int myHop = gi.getHopCount();
        if (myHop == Integer.MAX_VALUE)
            return null;

        Gateway best = null;
        double bestCost = Double.NEGATIVE_INFINITY;

        for (Gateway gj : gateways) {
            if (gj == gi) continue;
            if (!gj.isAlive()) continue;

            // 🔒 tránh loop (DFCR)
            if (gj.getHopCount() >= myHop) continue;

            double dij = gi.distanceTo(gj);
            if (dij > gi.getCommRange()) continue;

            double dPath = gj.getPathDistanceToBS();
            if (dPath == Double.POSITIVE_INFINITY) continue;

            double Eres = gj.getResidualEnergy();
            int backCH = Math.max(1, gj.getMemberCount());

            // ===== CT (4.15) =====
            double cost = Eres / (dij * dPath * backCH);

            if (cost > bestCost) {
                bestCost = cost;
                best = gj;
            }
        }

        return best;
    }



    
    
}
