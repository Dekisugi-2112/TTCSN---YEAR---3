package gui;

import Network.NodeDeployment;
import RunModels.WSNDataSet;
import NodeType.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NetworkPanel extends JPanel {

    private NodeDeployment deployment;
    WSNDataSet data = new WSNDataSet();

    private final int padding = 30;
    private double scaleX, scaleY;

    public NetworkPanel(NodeDeployment deployment) {
        this.deployment = deployment;
        setBackground(Color.WHITE);
    }

    // =========================================================
    // SCALE
    // =========================================================
    private void updateScale() {
        if (deployment == null) return;
        double w = data.width;
        double h = data.height;

        scaleX = (getWidth() - 2.0 * padding) / w;
        scaleY = (getHeight() - 2.0 * padding) / h;
    }

    private int sx(double x) {
        return padding + (int) (x * scaleX);
    }

    private int sy(double y) {
        return padding + (int) (y * scaleY);
    }

    // =========================================================
    // PAINT
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (deployment == null) return;
        updateScale();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawNodes(g2);
        drawGatewayToBS(g2);
        drawGatewayToGateway(g2);
        drawSensorToSensor(g2);   
        drawSensorToCH(g2);
//        drawGatewayRanges(g2);    
    }

    // =========================================================
    // DRAW LINKS
    // =========================================================
    private void drawSensorToCH(Graphics2D g2) {
        g2.setColor(new Color(70, 130, 255));
        g2.setStroke(new BasicStroke(1.2f));

        for (SensorNode s : deployment.getSensors()) {
//            if (!s.isAlive()) continue;
            Integer chId = s.getClusterHeadId();
            if (chId == null) continue;

            Gateway ch = findGateway(chId);
            if (ch == null) continue;

            g2.drawLine(
                    sx(s.getX()), sy(s.getY()),
                    sx(ch.getX()), sy(ch.getY())
            );
        }
    }

    // --------------- Line CH to CH ---------------------
    private void drawGatewayToGateway(Graphics2D g2) {
        g2.setColor(Color.ORANGE);
        g2.setStroke(new BasicStroke(2.5f));

        for (Gateway g : deployment.getGateways()) {
            Integer nextHopId = g.getNextHopId();
            if (nextHopId  == null) continue;

            Gateway next = findGateway(nextHopId);
            if (next == null) continue;
            
            int x1 = sx(g.getX());
            int y1 = sy(g.getY());
            int x2 = sx(next.getX());
            int y2 = sy(next.getY());


            if (g.isAlive() && next.isAlive()) {
                    // route đang hoạt động
                    g2.setColor(Color.ORANGE);
                    g2.setStroke(new BasicStroke(2));
            } else {
                // CH chết nhưng route lịch sử vẫn tồn tại
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(
                    1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[]{6},
                    0
                ));
            }

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    // ==============================================
    
    // --------------- Line CH to BS -----------------
    private void drawGatewayToBS(Graphics2D g2) {
        BaseStation bs = deployment.getBaseStation();
        if (bs == null) return;

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2.8f));

        for (Gateway g : deployment.getGateways()) {
//            if (!g.isAlive()) continue;
            if (g.getHopCount() != 1) continue;

            g2.drawLine(
                    sx(g.getX()), sy(g.getY()),
                    sx(bs.getX()), sy(bs.getY())
            );
        }
    }
    // =======================================
    
    // ------------ Line sensor to sensor ----------
    private void drawSensorToSensor(Graphics2D g2) {

        // Màu tím, nét đứt: biểu diễn BackupSet
        g2.setColor(new Color(160, 80, 200));
        g2.setStroke(new BasicStroke(
                1.3f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{5},
                0
        ));

        for (SensorNode s : deployment.getSensors()) {

            Integer backupId = s.getBackupNodeId();
            if (backupId == null) continue;

            SensorNode backup = findSensor(backupId);
            if (backup == null) continue;

            int x1 = sx(s.getX());
            int y1 = sy(s.getY());
            int x2 = sx(backup.getX());
            int y2 = sy(backup.getY());

            g2.drawLine(x1, y1, x2, y2);
        }
    }


    // =========================================================
    // DRAW NODES
    // =========================================================
    private void drawNodes(Graphics2D g2) {
        // ---------------- Sensors -----------------------
        for (SensorNode s : deployment.getSensors()) {
//            if (!s.isAlive()) continue;
            g2.setColor(Color.GREEN);
            int r = 4;
            g2.fillOval(sx(s.getX()) - r, sy(s.getY()) - r, 2 * r, 2 * r);
        }

        // ----------------- Gateways --------------------
        for (Gateway g : deployment.getGateways()) {
//            if (!g.isAlive()) continue;

            int cx = sx(g.getX());
            int cy = sy(g.getY());

            // ===== Vẽ hình CH =====
            g2.setColor(Color.BLUE);
            int r = 7;
            g2.fillRect(
                cx - r,
                cy - r,
                2 * r,
                2 * r
            );

            // ===== Hiển thị hopCount (trên) =====
            g2.setColor(Color.BLACK);
            g2.drawString(
                "h=" + g.getHopCount(),
                cx + r + 2,
                cy - r
            );

            // ===== Hiển thị ID (bên dưới) =====
            g2.drawString(
                "G" + g.getId(),
                cx - r,
                cy + r + 12
            );
        }


        // ---------------------- Base Station ----------------------
        BaseStation bs = deployment.getBaseStation();
        if (bs != null) {
            g2.setColor(Color.RED);
            int r = 10;
            g2.fillOval(
                    sx(bs.getX()) - r,
                    sy(bs.getY()) - r,
                    2 * r,
                    2 * r
            );
            g2.setColor(Color.BLACK);
            g2.drawString("BS", sx(bs.getX()) + 6, sy(bs.getY()));
        }
    }

    // =========================================================
    // UTIL
    // =========================================================
    private Gateway findGateway(int id) {
        for (Gateway g : deployment.getGateways()) {
            if (g.getId() == id) return g;
        }
        return null;
    }
    
    private SensorNode findSensor(int id) {
        for (SensorNode s : deployment.getSensors()) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    
    
    // =========================================================
    // DRAW COMMUNICATION RANGES
    // =========================================================
    private void drawSensorRanges(Graphics2D g2) {
        g2.setColor(new Color(0, 180, 0, 60)); // xanh lá trong suốt
        g2.setStroke(new BasicStroke(1.0f));

        for (SensorNode s : deployment.getSensors()) {
            if (!s.isAlive()) continue;

            double r = s.getCommRange();
            int cx = sx(s.getX());
            int cy = sy(s.getY());

            int rrX = (int) (r * scaleX);
            int rrY = (int) (r * scaleY);

            g2.drawOval(
                    cx - rrX,
                    cy - rrY,
                    2 * rrX,
                    2 * rrY
            );
        }
    }

    private void drawGatewayRanges(Graphics2D g2) {
        g2.setColor(new Color(255, 165, 0, 70)); // cam trong suốt
        g2.setStroke(new BasicStroke(1.8f));

        for (Gateway g : deployment.getGateways()) {
            if (!g.isAlive()) continue;

            double r = g.getCommRange();
            int cx = sx(g.getX());
            int cy = sy(g.getY());

            int rrX = (int) (r * scaleX);
            int rrY = (int) (r * scaleY);

            g2.drawOval(
                    cx - rrX,
                    cy - rrY,
                    2 * rrX,
                    2 * rrY
            );
        }
    }

}
