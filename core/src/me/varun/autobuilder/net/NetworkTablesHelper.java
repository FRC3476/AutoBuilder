package me.varun.autobuilder.net;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.serialization.Autonomous;
import me.varun.autobuilder.serialization.GuiSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkTablesHelper {

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("autodata");
    NetworkTableEntry autoPath = table.getEntry("autoPath");
    NetworkTable position = table.getSubTable("position");
    NetworkTableEntry xPos = position.getEntry("x");
    NetworkTableEntry yPos = position.getEntry("y");
    NetworkTableEntry enabledTable = table.getEntry("enabled");

    static NetworkTablesHelper networkTablesInstance = new NetworkTablesHelper();

    private ArrayList<Float[]> robotPositions = new ArrayList<>();
    private boolean enabled = false;

    public static NetworkTablesHelper getInstance(){
        return networkTablesInstance;
    }

    private NetworkTablesHelper(){

    }

    public void start() {
        inst.startClientTeam(3476);  // where TEAM=190, 294, etc, or use inst.startClient("hostname") or similar
        //inst.startDSClient();  // recommended if running on DS computer; this gets the robot IP from the DS
    }

    public void pushData(List<AbstractGuiItem> guiItemList){

        if(inst.isConnected()){
            try {
                String autonomousString = Serializer.serializeToString(GuiSerializer.serializeAutonomousForDeployment(guiItemList));
                autoPath.setString(autonomousString);
                Autonomous autonomous = Serializer.deserialize(autoPath.getString(null));
                System.out.println("Sent Data: " + autonomous);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            System.out.println("Cannot Send Data; Not Connected");
        }

    }

    public void updateRobotPath(){
        if(inst.isConnected()){
            if(enabledTable.getBoolean(false)){
                if(!enabled){
                    robotPositions.clear();
                    enabled = true;
                }

                float x = (float) xPos.getDouble(0);
                float y = (float) yPos.getDouble(0);
                if(robotPositions.size()>1 || (robotPositions.get(robotPositions.size()-1)[0] != x || robotPositions.get(robotPositions.size()-1)[1] != y)){
                    robotPositions.add(new Float[] {x, y});
                }
            } else {
                enabled = false;
            }

        }

    }

    public ArrayList<Float[]> getRobotPositions() {
        return robotPositions;
    }
}