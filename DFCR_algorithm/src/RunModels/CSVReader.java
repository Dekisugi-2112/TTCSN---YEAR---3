/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RunModels;

import NodeType.*;
import java.io.*;
import java.util.*;

import NodeType.BaseStation;
import NodeType.Gateway;
import NodeType.SensorNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<SensorNode> readSensors(String path) {
        List<SensorNode> sensors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length < 6) continue;

                SensorNode s = new SensorNode(
                    Integer.parseInt(t[0].trim()),
                    Double.parseDouble(t[1].trim()),
                    Double.parseDouble(t[2].trim()),
                    Double.parseDouble(t[3].trim()),
                    Double.parseDouble(t[4].trim())
                );
                s.alive = Boolean.parseBoolean(t[5].trim());
                sensors.add(s);
            }
        } catch (Exception e) {
            System.err.println("Error reading sensors from: " + path);
            e.printStackTrace();
        }
        return sensors;
    }


    public static List<Gateway> readGateways(String path) {
        List<Gateway> gateways = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",");

                Gateway g = new Gateway(
                    Integer.parseInt(t[0].trim()),
                    Double.parseDouble(t[1].trim()),
                    Double.parseDouble(t[2].trim()),
                    Double.parseDouble(t[3].trim()),
                    Double.parseDouble(t[4].trim())
                );
                g.alive = Boolean.parseBoolean(t[5]);
                g.setHopCount(Integer.MAX_VALUE);
                gateways.add(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gateways;
    }

    public static BaseStation readBaseStation(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // header
            String[] t = br.readLine().split(",");

            return new BaseStation(
                Integer.parseInt(t[0].trim()),
                Double.parseDouble(t[1].trim()),
                Double.parseDouble(t[2].trim()),
                Double.parseDouble(t[3].trim()),
                Double.parseDouble(t[4].trim())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
