/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

import java.util.List;

/**
 *
 * @author Admin
 */
public class CoverageInfo {
    private List<SensorNode> coveredNodes;  // COset
    private List<SensorNode> uncoveredNodes;    // UnCOset

    public CoverageInfo(List<SensorNode> coveredNodes, List<SensorNode> uncoveredNodes) {
        this.coveredNodes = coveredNodes;
        this.uncoveredNodes = uncoveredNodes;
    }

    public List<SensorNode> getCoveredNodes() {
        return coveredNodes;
    }

    public List<SensorNode> getUncoveredNodes() {
        return uncoveredNodes;
    }
    
    
}
