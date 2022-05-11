package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.config.gui.FileHandler;
import me.varun.autobuilder.events.input.InputEventThrower;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.PathGui;
import me.varun.autobuilder.gui.path.ScriptItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.serialization.path.*;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class UndoHandler {
    private static final int MAX_UNDO_HISTORY = 1000;
    private static final UndoHandler undoHandler = new UndoHandler();
    private @NotNull List<Autonomous> undoHistory = new ArrayList<>();
    int pointer = 0;
    private boolean somethingChanged = false;

    private long lastUndoSaveTime = 0;

    private static final long UNDO_SAVE_INTERVAL = 1000;

    private UndoHandler() {

    }

    public static UndoHandler getInstance() {
        return undoHandler;
    }

    public synchronized void update(PathGui pathGui, @NotNull InputEventThrower inputEventThrower,
                                    @NotNull CameraHandler cameraHandler) {
        if (somethingChanged && System.currentTimeMillis() - lastUndoSaveTime > UNDO_SAVE_INTERVAL) {
            saveCurrentState(pathGui);
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) &&
                Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (somethingChanged) {
                saveCurrentState(pathGui);
            }
            if ((Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                pointer--;
                if (pointer >= 0) {
                    restoreState(undoHistory.get(pointer), pathGui, inputEventThrower, cameraHandler);
                    //System.out.println("redoing to: " + undoHistory.get(pointer));
                } else {
                    pointer = 0;
                }
            } else {
                //Undoing
                //System.out.println(undoHistory);
                pointer++;
                if (pointer < undoHistory.size()) {
                    restoreState(undoHistory.get(pointer), pathGui, inputEventThrower, cameraHandler);
                    //System.out.println("undoing to: " + undoHistory.get(pointer));
                } else {
                    pointer = undoHistory.size() - 1;
                }
            }

            FileHandler.save();
        }
    }

    private synchronized void saveCurrentState(PathGui pathGui) {
        Autonomous newState = GuiSerializer.serializeAutonomousForUndoHistory(pathGui.guiItems);
        while (pointer > 0) {
            undoHistory.remove(0);
            pointer--;
        }
        undoHistory.add(0, newState);
        if (undoHistory.size() > MAX_UNDO_HISTORY) {
            undoHistory.remove(undoHistory.size() - 1);
        }
        //System.out.println("adding: " + newState);
        somethingChanged = false;
        lastUndoSaveTime = System.currentTimeMillis();
    }

    public synchronized void restoreState(Autonomous autonomous, PathGui pathGui, @NotNull InputEventThrower inputEventThrower,
                                          @NotNull CameraHandler cameraHandler) {
        List<AbstractGuiItem> guiItemList = new ArrayList<>();
        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep) {
                TrajectoryAutonomousStep trajectoryAutonomousStep = (TrajectoryAutonomousStep) autonomousStep;
                Color color = new Color().fromHsv(trajectoryAutonomousStep.getColor(), 1, 1);
                color.set(color.r, color.g, color.b, 1);
                TrajectoryItem trajectoryItem = new TrajectoryItem(pathGui, inputEventThrower, cameraHandler,
                        new ControlVectorList(trajectoryAutonomousStep.getControlVectors()),
                        trajectoryAutonomousStep.getRotations(),
                        trajectoryAutonomousStep.isReversed(), color, trajectoryAutonomousStep.isClosed(),
                        trajectoryAutonomousStep.getVelocityStart(), trajectoryAutonomousStep.getVelocityEnd(),
                        trajectoryAutonomousStep.getConstraints());
                guiItemList.add(trajectoryItem);
            } else if (autonomousStep instanceof ScriptAutonomousStep) {
                ScriptAutonomousStep scriptAutonomousStep = (ScriptAutonomousStep) autonomousStep;
                ScriptItem scriptItem = new ScriptItem(inputEventThrower, scriptAutonomousStep.getScript(),
                        scriptAutonomousStep.isClosed(), scriptAutonomousStep.isValid());
                guiItemList.add(scriptItem);
            }
        }
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            guiItem.dispose();
        }
        pathGui.guiItems = guiItemList;
    }

    public synchronized void somethingChanged() {
        somethingChanged = true;
        FileHandler.save();
    }

    public synchronized void clearUndoHistory() {
        undoHistory.clear();
        pointer = 0;
    }
}