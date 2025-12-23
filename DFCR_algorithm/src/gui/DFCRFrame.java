package gui;

import Network.NodeDeployment;

import javax.swing.*;

public class DFCRFrame extends JFrame {

    private NetworkPanel networkPanel;

    public DFCRFrame(NodeDeployment deployment) {

        setTitle("DFCR Simulation");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        NetworkPanel panel = new NetworkPanel(deployment); // ✅ TRUYỀN deployment
        add(panel);
    }

    public void refresh() {
        networkPanel.repaint();
    }
}
