package RunModels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PacketEnergyLogger {

    private FileWriter writer;

    public PacketEnergyLogger(String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        writer = new FileWriter(file);
        writer.write("round,packetType,src,dst,distance,energy\n");
    }

    public void log(
            int round,
            String packetType,
            int src,
            String dst,
            double distance,
            double energy
    ) {
        try {
            writer.write(
                round + "," +
                packetType + "," +
                src + "," +
                dst + "," +
                String.format("%.3f", distance) + "," +
                String.format("%.8f", energy) + "\n"
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
