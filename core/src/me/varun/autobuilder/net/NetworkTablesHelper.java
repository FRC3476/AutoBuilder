package me.varun.autobuilder.net;

import com.badlogic.gdx.graphics.Color;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;
import me.varun.autobuilder.serialization.path.NotDeployableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class NetworkTablesHelper {

    private static final float INCHES_PER_METER = 39.3700787f;
    static NetworkTablesHelper networkTablesInstance = new NetworkTablesHelper();
    private final ArrayList<Float[]> robotPositions = new ArrayList<>();
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("autodata");
    NetworkTableEntry autoPath = table.getEntry("autoPath");

    NetworkTable smartDashboardTable = inst.getTable("SmartDashboard");
    NetworkTableEntry last_estimated_robot_pose_x = smartDashboardTable.getEntry("Last Estimated Robot Pose X");
    NetworkTableEntry last_estimated_robot_pose_y = smartDashboardTable.getEntry("Last Estimated Robot Pose Y");
    NetworkTableEntry last_estimated_robot_pose_angle = smartDashboardTable.getEntry("Last Estimated Robot Pose Angle");
    NetworkTableEntry last_estimated_robot_velocity_x = smartDashboardTable.getEntry("Last Estimated Robot Velocity X");
    NetworkTableEntry last_estimated_robot_velocity_y = smartDashboardTable.getEntry("Last Estimated Robot Velocity Y");
    NetworkTableEntry last_estimated_robot_velocity_theta = smartDashboardTable.getEntry("Last Estimated Robot Velocity Theta");

    NetworkTableEntry latency_comped_robot_pose_x = smartDashboardTable.getEntry("Latency Comped Robot Pose X");
    NetworkTableEntry latency_comped_robot_pose_y = smartDashboardTable.getEntry("Latency Comped Robot Pose Y");
    NetworkTableEntry latency_comped_robot_pose_angle = smartDashboardTable.getEntry("Latency Comped Robot Pose Angle");
    NetworkTableEntry latency_comped_robot_velocity_x = smartDashboardTable.getEntry("Latency Comped Robot Velocity X");
    NetworkTableEntry latency_comped_robot_velocity_y = smartDashboardTable.getEntry("Latency Comped Robot Velocity Y");
    NetworkTableEntry latency_comped_robot_velocity_theta = smartDashboardTable.getEntry("Latency Comped Robot Velocity Theta");

    NetworkTableEntry timestamp = smartDashboardTable.getEntry("Timestamp");

    NetworkTableEntry enabledTable = table.getEntry("enabled");

    NetworkTableEntry processingTable = table.getEntry("processing");
    NetworkTableEntry processingIdTable = table.getEntry("processingid");

    private boolean enabled = false;
    private double lastProcessingId = 0;

    private NetworkTablesHelper() {

    }

    public static NetworkTablesHelper getInstance() {
        return networkTablesInstance;
    }

    public void start() {
        inst.startClientTeam(AutoBuilder.getConfig().getTeamNumber());  // where TEAM=190, 294, etc, or use inst.startClient("hostname") or similar
        //inst.startDSClient();  // recommended if running on DS computer; this gets the robot IP from the DS
    }


    private static final Color LIGHT_GREEN = Color.valueOf("8FEC8F");


    public void pushAutoData(List<AbstractGuiItem> guiItemList) {
        if (inst.isConnected()) {
            try {
                String autonomousString = Serializer.serializeToString(GuiSerializer.serializeAutonomousForDeployment(guiItemList));
                autoPath.setString(autonomousString);
                Autonomous autonomous = Serializer.deserializeAuto(autoPath.getString(null));
                System.out.println("Sent Data: " + autonomous);

                NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "Auto Uploaded", 2000));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload", 2000));
            } catch (NotDeployableException e) {
                NotificationHandler.addNotification(new Notification(Color.RED, "Your autonomous contains errors: Cannot deploy!", 2000));
            }
        } else {
            System.out.println("Cannot Send Data; Not Connected");
            NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload: NOT CONNECTED", 2000 ));
        }

    }

    public void updateNT() {
        if (inst.isConnected()) {
            if (enabledTable.getBoolean(false)) {
                if (!enabled) {
                    robotPositions.clear();
                    enabled = true;
                }
                float time = (float) timestamp.getDouble(0);
                if (robotPositions.size() < 1 || time != robotPositions.get(robotPositions.size() - 1)[13]) {
                    float x = (float) last_estimated_robot_pose_x.getDouble(0);
                    float y = (float) last_estimated_robot_pose_y.getDouble(0);
                    float rotation = (float) Math.toRadians(last_estimated_robot_pose_angle.getDouble(0));
                    float xv = (float) last_estimated_robot_velocity_x.getDouble(0);
                    float yv = (float) last_estimated_robot_velocity_y.getDouble(0);
                    float thetav = (float) last_estimated_robot_velocity_theta.getDouble(0);

                    float x2 = (float) latency_comped_robot_pose_x.getDouble(0);
                    float y2 = (float) latency_comped_robot_pose_y.getDouble(0);
                    float rotation2 = (float) Math.toRadians((float) latency_comped_robot_pose_angle.getDouble(0));
                    float xv2 = (float) latency_comped_robot_velocity_x.getDouble(0);
                    float yv2 = (float) latency_comped_robot_velocity_y.getDouble(0);
                    float thetav2 = (float) latency_comped_robot_velocity_theta.getDouble(0);

                    robotPositions.add(new Float[]{x, y, rotation, xv, yv, thetav, x2, y2, rotation2, xv2, yv2, thetav2, time});
                }

            } else {
                enabled = false;
            }

            //Check for the roborio processing notification
            if(processingIdTable.getDouble(0) != lastProcessingId){
                lastProcessingId = processingIdTable.getDouble(0);
                if(processingTable.getDouble(0) == 1){
                    NotificationHandler.addNotification(new Notification(Color.CORAL, "The Roborio has started deserializing the auto", 1500));
                } else if (lastProcessingId == 2){
                    NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "The Roborio has finished deserializing the auto", 1500));
                } else {
                    NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "The Roborio has set: " + processingTable.getDouble(0), 1500));
                }
            }
        }
    }

    public ArrayList<Float[]> getRobotPositions() {
        return robotPositions;
    }

    public boolean isConnected(){
        return inst.isConnected();
    }
}