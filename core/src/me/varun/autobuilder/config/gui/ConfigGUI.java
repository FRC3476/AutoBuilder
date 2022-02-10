package me.varun.autobuilder.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ConfigGUI {
    Color transparentWhite = new Color(1, 1, 1, 0.8f);

    UploadFileButton uploadFileButton = new UploadFileButton(60, 10, 40, 40, this);

    TextBlock dropToImport = new TextBlock(Fonts.ROBOTO, 30,
            new TextComponent("Drop here to switch to the auto").setColor(transparentWhite));

    Polygon boarderPolygon = new Polygon(new float[]{
            10, 10,
            Gdx.graphics.getWidth() - 10, 10,
            Gdx.graphics.getWidth() - 10, Gdx.graphics.getHeight() - 10,
            10, Gdx.graphics.getHeight() - 10
    });

    public boolean update() {
        uploadFileButton.checkHover();
        uploadFileButton.checkClick();
        return false;
    }

    @Nullable CompletableFuture<File> completedFile;

    public void setOpenedFile(CompletableFuture<File> completedFile) {
        this.completedFile = completedFile;
    }

    public void draw(ShapeDrawer shapeDrawer, Batch batch, Camera cam) {

        if (completedFile != null) {
            if (completedFile.isDone()) {
                FileHandler.handleFile(completedFile.join());
                completedFile = null;
            }
        }

        uploadFileButton.render(shapeDrawer, batch, true);

//        shapeDrawer.setColor(transparentWhite);
//        FontRenderer.renderText(batch, shapeDrawer,
//                (Gdx.graphics.getWidth() - dropToImport.getWidth() - 410) / 2,
//                (Gdx.graphics.getHeight() - dropToImport.getHeight()) / 2,
//                dropToImport);
//
//        shapeDrawer.polygon(boarderPolygon, 5, JoinType.POINTY);
    }

    public void updateScreen(int width, int height) {
        uploadFileButton.setPosition(width - uploadFileButton.getWidth() - 410 - 160, 10);
    }
}