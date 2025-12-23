package Network;

import NodeType.*;
import java.util.*;

public class NodeDeployment {
    private BaseStation baseStation;
    private List<SensorNode> sensors;
    private List<Gateway> gateways;

    public NodeDeployment(BaseStation bs,
                          List<SensorNode> sensors,
                          List<Gateway> gateways) {
        this.baseStation = bs;
        this.sensors = sensors;
        this.gateways = gateways;
    }

    public BaseStation getBaseStation() {
        return baseStation;
    }

    public List<SensorNode> getSensors() {
        return sensors;
    }

    public List<Gateway> getGateways() {
        return gateways;
    }

    public void setBaseStation(BaseStation baseStation) {
        this.baseStation = baseStation;
    }

    public void setSensors(List<SensorNode> sensors) {
        this.sensors = sensors;
    }

    public void setGateways(List<Gateway> gateways) {
        this.gateways = gateways;
    }
    
    
}
