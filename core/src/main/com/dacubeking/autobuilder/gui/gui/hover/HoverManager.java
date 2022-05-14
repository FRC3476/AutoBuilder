package com.dacubeking.autobuilder.gui.gui.hover;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontHandler;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseX;
import static com.dacubeking.autobuilder.gui.util.MouseUtil.getMouseY;

public class HoverManager {
    private static TextBlock hoverText;
    private static boolean hoverShown = false;
    private static boolean hoverOnMouse = false;
    private static final Vector2 hoverPosition = new Vector2();

    public static void setHoverText(TextBlock text) {
        hoverText = text;
        hoverShown = true;
        hoverOnMouse = true;
    }

    public static void setHoverText(TextBlock text, float x, float y) {
        hoverText = text;
        hoverShown = true;
        hoverPosition.set(x, y);
        hoverOnMouse = false;
    }

    static boolean flippedLastTime = false;

    public static void showHover() {
        hoverShown = true;
    }

    public static void hideHover() {
        hoverShown = false;
    }

    public static boolean isHoverShown() {
        return hoverShown;
    }

    public static TextBlock getHoverText() {
        return hoverText;
    }

    public static void render(Batch batch, ShapeDrawer shapeDrawer) {
        if (hoverShown) {
            if (hoverOnMouse) hoverPosition.set(getMouseX(), getMouseY() + 4);
            float width = hoverText.getWidth();
            float height = hoverText.getHeight();

            if (hoverPosition.x + (width + 10) / 2 > Gdx.graphics.getWidth()) {
                hoverPosition.x = Gdx.graphics.getWidth() - (width + 10) / 2;
            } else if (hoverPosition.x - (width + 10) / 2 < 0) {
                hoverPosition.x = (width + 10) / 2;
            }
            if (hoverPosition.y + height + 5 > (flippedLastTime ? Gdx.graphics.getHeight() - 20 : Gdx.graphics.getHeight())) {
                hoverPosition.y = hoverPosition.y - height - (hoverOnMouse ? 30 : 5);
                flippedLastTime = true;
            } else {
                flippedLastTime = false;
            }

            shapeDrawer.filledRectangle(hoverPosition.x - ((width + 6) / 2), hoverPosition.y - 1,
                    width + 6, height + 6, Color.BLACK);

            shapeDrawer.filledRectangle(hoverPosition.x - ((width + 4) / 2),
                    hoverPosition.y, width + 4, height + 4, Color.WHITE);

            FontRenderer.renderText(batch, shapeDrawer,
                    hoverPosition.x - (width / 2),
                    hoverPosition.y + hoverText.getHeight() - FontHandler.getFont(
                            hoverText.getDefaultFont(), false, false, hoverText.getDefaultSize()).getCapHeight(),
                    hoverText);
        }
        hoverShown = false;
    }
}
