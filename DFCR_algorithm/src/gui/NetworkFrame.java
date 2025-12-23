package gui;

import javax.swing.*;
import Network.NodeDeployment;
import RunModels.WSNDataSet;

public class NetworkFrame extends JFrame {

    private final NetworkPanel panel;

    public NetworkFrame(NodeDeployment deployment) {

        setTitle("DFCR Wireless Sensor Network");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full màn hình
        setLocationRelativeTo(null);             // căn giữa
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        panel = new NetworkPanel(deployment);
        setContentPane(panel);

        setVisible(true);
    }

    public void refresh() {
        panel.repaint();
    }
}
