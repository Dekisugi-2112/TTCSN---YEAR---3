/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wsn;

/**
 *
 * @author Admin
 */

// tham so mo phong
public class SimulationConfig {
    // Kích thước vùng
    public double fieldWidh = 400;
    public double fieldHeight = 400;
    
     // Số node
    public int numSensors = 400;
    public int numGateways = 40;
    
     // Năng lượng ban đầu
    public double initialSensorEnergy = 2.0;
    public double initialGatewayEnergy = 10.0;
    
     // Tầm truyền
    public double sensorRange = 60.0;   // m
    public double gatewayRange = 100.0; // m
    
    // --- Tham số mô hình năng lượng (theo Table 1) ---
    
    // Năng lượng dùng cho mạch điện tử (J/bit)
    public double E_ELEC = 50e-9;   // 50 nJ/bit
    
    // Hệ số khuếch đại free-space (J/bit/m^2)
    public double EPS_FS = 10e-12;  // 10 pJ/bit/m^2
    
    // Hệ số khuếch đại multipath (J/bit/m^4)
    public double EPS_MP = 0.0013e-12;  // 0.0013 pJ/bit/m^4
    
    // Ngưỡng khoảng cách
    public double D0 = 30.0;    // m
    
    // Năng lượng xử lý/aggregate dữ liệu (J/bit)
    public double E_DA = 5e-9;  // 5 nJ/bit
    
    // Kích thước gói tin
    public int DATA_PACKET_SIZE = 4000; // bits
    public int CONTROL_PACKET_SIZE = 200;   // bits (HELLO, HELP, JOIN_REQ,...)
    
    
}
