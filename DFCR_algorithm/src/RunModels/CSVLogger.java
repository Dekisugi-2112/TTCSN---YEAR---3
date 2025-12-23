package RunModels;

import NodeType.Gateway;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.File;

/**
 * CSVLogger
 * Dùng để ghi dữ liệu mô phỏng ra file CSV
 */
public class CSVLogger {

    private FileWriter writer;

    // ================== CONSTRUCTOR ==================
    public CSVLogger(String filePath, String header) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        writer = new FileWriter(file);
        writer.write(header + "\n");
    }


    // ================== GHI 1 DÒNG ==================
    public void logDeadCH(int round, List<Gateway> gateways) {
        int deadCH = countDeadGateways(gateways);
        try {
            writer.write(round + "," + deadCH + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ================== ĐÓNG FILE ==================
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================== HÀM PHỤ ==================
    private int countDeadGateways(List<Gateway> gateways) {
        int dead = 0;
        for (Gateway g : gateways) {
            if (!g.isAlive()) {
                dead++;
            }
        }
        return dead;
    }
    // ==========================
    public void logAliveSensors(int round, int alive) {
        try {
            writer.write(round + "," + alive + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // -------- TÍNH TỔNG NĂNG LƯỢNG TIÊU HAO ------------------
    public void logEnergy(int round, double energy) {
        try {
            writer.write(round + "," + energy + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
