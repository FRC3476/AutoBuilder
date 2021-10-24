package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.CameraHandler;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;
import org.jetbrains.annotations.NotNull;

public class AddPathButton extends AbstractGuiButton {

    private final @NotNull ShaderProgram fontShader;
    private final @NotNull BitmapFont font;
    private final @NotNull InputEventThrower eventThrower;
    private final CameraHandler cameraHandler;

    public AddPathButton(int x, int y, int width, int height, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                         @NotNull InputEventThrower eventThrower, CameraHandler cameraHandler) {
        super(x, y, width, height, new Texture(Gdx.files.internal("path_icon.png"), true));
        this.fontShader = fontShader;
        this.font = font;
        this.eventThrower = eventThrower;
        this.cameraHandler = cameraHandler;
    }

    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick()) {
            pathGui.guiItems.add(new TrajectoryItem(pathGui, fontShader, font, eventThrower, cameraHandler));
            UndoHandler.getInstance().somethingChanged();
            return true;
        }

        return false;

    }
}