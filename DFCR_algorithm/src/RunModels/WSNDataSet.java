package RunModels;

import RunModels.CSVReader;

public class WSNDataSet {
    
    public int MAX_ROUND = 3000;
    
    // Kích thước vùng (m)
    public double width = 1000.0;
    public double height = 1000.0;
    
    // số lượng sensor và gateway
    public int sensorCount;
    public int gatewayCount;

    
    // năng lượng ban đầu 
    public double sensorEnergy = 2.0;
    public double gatewayEnergy = 5.0;
    
    // Phạm vi truyền
    public static double sensorRange = 60.0;
    public static double gatewayRange = 100.0;
    
    // kích thước gói
    public static int packetSize = 100000;
    public static int messageSize = 1000;
    
    
    
    // Tham số năng lượng radio (đã chuyển thành Joule)
    // E_elec = 50 nJ/bit = 50e-9 J/bit
    public final double E_ELEC = 50e-9;
    // eps_fs = 10 pJ/bit/m^2 = 10e-12 J/bit/m^2
    public final double EPS_FS = 10e-12;
    // eps_mp = 0.0013 pJ/bit/m^4 = 0.0013e-12 J/bit/m^4
    public final double EPS_MP = 0.0013e-12;
    // d0 threshold distance (m)
    public final double D0 = Math.sqrt(EPS_FS / EPS_MP); // hoặc dùng 30.0 tùy bạn
    // energy for data aggregation (J/bit)
    public final double E_DA = 5e-9;

}
