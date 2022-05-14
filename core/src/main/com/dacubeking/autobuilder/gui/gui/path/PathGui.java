package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventListener;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.util.MathUtil;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import static java.awt.Color.HSBtoRGB;

public class PathGui extends InputEventListener {
    final @NotNull ExecutorService executorService;
    public @NotNull List<AbstractGuiItem> guiItems = new ArrayList<>();
    public ArrayList<AbstractGuiItem> guiItemsDeletions = new ArrayList<>();
    @NotNull AddPathButton addPathButton;
    @NotNull AddScriptButton addScriptButton;
    @NotNull PushAutoButton pushAutoButton;
    @NotNull Vector2 mouseDownPos = new Vector2();
    boolean dragging = false;
    @Nullable AbstractGuiItem draggingElement = null;
    int newDraggingElementIndex = 0;
    int oldDraggingElementIndex = 0;
    @NotNull Vector2 dragOffset = new Vector2();
    @NotNull Color color = new Color(1, 1, 1, 1);
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private float smoothScrollPos = 0;
    private float scrollPos = 0;
    private float maxScroll = 0;
    private @NotNull Rectangle clipBounds;
    private @Nullable TrajectoryItem lastPath = null;
    private boolean clickedInsidePanel;

    {
        color.fromHsv(1, 1, 1);
    }

    public PathGui(@NotNull Viewport viewport, @NotNull InputEventThrower eventThrower,
                   @NotNull ExecutorService executorService, @NotNull CameraHandler cameraHandler) {


        addPathButton = new AddPathButton(0, 0, 40, 40, eventThrower, cameraHandler);
        addScriptButton = new AddScriptButton(0, 0, 40, 40, eventThrower);
        pushAutoButton = new PushAutoButton(0, 0, 40, 40);

        updateScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        eventThrower.register(this);

        this.executorService = executorService;
    }

    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, @NotNull Camera camera) {
        shapeRenderer.setColor(Color.WHITE);
        RoundedShapeRenderer.roundedRect(shapeRenderer, panelX, panelY, panelWidth, panelHeight, 5);

        addScriptButton.render(shapeRenderer, spriteBatch);
        addPathButton.render(shapeRenderer, spriteBatch);
        pushAutoButton.render(shapeRenderer, spriteBatch);

        Rectangle scissors = new Rectangle();

        ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), clipBounds, scissors);
        boolean pop = ScissorStack.pushScissors(scissors);

        int yPos = Gdx.graphics.getHeight() - 20 + (int) smoothScrollPos;

        lastPath = null;
        boolean draggingElementSpotFound = false;
        TrajectoryItem lastDraggingElementTrajectory = null;
        for (int i = 0; i < guiItems.size(); i++) {
            AbstractGuiItem guiItem = guiItems.get(i);

            if (dragging && draggingElement == null && guiItem.isMouseOver(panelX + 10, yPos - 40, panelWidth - 20 - 45, 40)) {
                draggingElement = guiItem;
                draggingElement.setClosed(true);
                oldDraggingElementIndex = i;
                dragOffset.set(Gdx.input.getX() - panelX + 10, Gdx.graphics.getHeight() - Gdx.input.getY() - yPos);
            }

            if (draggingElement != null && !draggingElementSpotFound &&
                    (yPos - (guiItem.getHeight() / 2f) - (newDraggingElementIndex == i ? draggingElement.getHeight() : 0))
                            < Gdx.graphics.getHeight() - Gdx.input.getY() - dragOffset.y) {
                yPos = yPos - 10 - draggingElement.getHeight();
                newDraggingElementIndex = i;
                draggingElementSpotFound = true;
                lastDraggingElementTrajectory = lastPath;
                if (draggingElement instanceof TrajectoryItem) {
                    lastPath = (TrajectoryItem) draggingElement;
                }
            }

            if (guiItem != draggingElement) {
                yPos = yPos - 10 - guiItem.render(shapeRenderer, spriteBatch, panelX + 10, yPos, panelWidth - 20, this,
                        isLeftMouseJustUnpressed);
            }

            if (guiItem instanceof TrajectoryItem) {
                lastPath = (TrajectoryItem) guiItem;
            }
        }

        if (draggingElement != null) {
            if (!draggingElementSpotFound) {
                newDraggingElementIndex = guiItems.size();
            }
            lastPath = lastDraggingElementTrajectory;
            draggingElement.render(shapeRenderer, spriteBatch,
                    (int) (Gdx.input.getX() - dragOffset.x),
                    (int) ((Gdx.graphics.getHeight() - Gdx.input.getY()) - dragOffset.y),
                    panelWidth - 20, this, isLeftMouseJustUnpressed);
        }


        if (pop) {
            spriteBatch.flush();
            ScissorStack.popScissors();
        }

        maxScroll = Math.max(0, -(yPos - (int) smoothScrollPos - 10));
        //System.out.println(maxScroll);
    }

    boolean isLeftMousePressed = false;
    boolean isLeftMouseJustUnpressed = false;

    public boolean update() {
        isLeftMouseJustUnpressed = !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && isLeftMousePressed;
        isLeftMousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        for (AbstractGuiItem guiItemsDeletion : guiItemsDeletions) {
            guiItemsDeletion.dispose();
            guiItems.remove(guiItemsDeletion);
        }
        guiItemsDeletions.clear();

        scrollPos = MathUtil.clamp(scrollPos, 0, maxScroll);
        smoothScrollPos = (float) (smoothScrollPos + (scrollPos - smoothScrollPos) / Math.max(1,
                0.05 / AutoBuilder.getDeltaTime()));

        if (Math.abs(scrollPos - smoothScrollPos) < 1e-2) {
            AutoBuilder.disableContinuousRendering(this);
        } else {
            AutoBuilder.enableContinuousRendering(this);
        }

        boolean onGui = false;

        //TODO: Store in a list and iterate over the list
        onGui = addScriptButton.checkHover();
        onGui = onGui | addPathButton.checkHover();
        onGui = onGui | pushAutoButton.checkHover();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            addScriptButton.checkClick(this);
            addPathButton.checkClick(this);
            pushAutoButton.checkClick(this);
        }

        if (Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.input.getY() > panelY && Gdx.input.getY() < panelY + panelHeight) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                mouseDownPos.x = Gdx.input.getX();
                mouseDownPos.y = Gdx.input.getY();
                draggingElement = null;
                clickedInsidePanel = true;
            } else if (clickedInsidePanel && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                if (MathUtil.len2(mouseDownPos, Gdx.input.getX(), Gdx.input.getY()) > 100) {
                    dragging = true;
                }
            } else {
                clickedInsidePanel = false;
                dragging = false;
                if (draggingElement != null) {
                    System.out.println(newDraggingElementIndex);
                    guiItems.remove(oldDraggingElementIndex);
                    if (newDraggingElementIndex > oldDraggingElementIndex) newDraggingElementIndex--;
                    guiItems.add(newDraggingElementIndex, draggingElement);
                    draggingElement = null;
                    UndoHandler.getInstance().somethingChanged();
                }
            }

            return true;
        }

        return onGui;
    }

    public void updateScreen(int width, int height) {
        panelX = (width - 410);
        panelY = 10;
        panelWidth = 400;
        panelHeight = height - 20;

        addPathButton.setX(panelX - 10 - addPathButton.getWidth());
        addPathButton.setY(10);
        addScriptButton.setX(addPathButton.getX() - 10 - addScriptButton.getWidth());
        addScriptButton.setY(10);
        pushAutoButton.setX(addScriptButton.getX() - 10 - pushAutoButton.getWidth());
        pushAutoButton.setY(10);

        clipBounds = new Rectangle(pushAutoButton.getX() - 500, panelY, panelWidth + panelX - pushAutoButton.getX() + 500,
                panelHeight);
    }

    public void dispose() {
        for (AbstractGuiItem guiItem : guiItems) {
            guiItem.dispose();
        }
        addPathButton.dispose();
        addScriptButton.dispose();
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (Gdx.input.getX() > panelX && Gdx.input.getX() < panelX + panelWidth &&
                Gdx.graphics.getHeight() - Gdx.input.getY() > panelY && Gdx.graphics.getHeight() - Gdx.input.getY() < panelY + panelHeight) {
            scrollPos = scrollPos + amountY * 80;
        }
    }


    Random random = new Random();

    public @NotNull Color getNextColor() {
        java.awt.Color colorJava = new java.awt.Color(HSBtoRGB(random.nextFloat(), 1, 1));
        return new Color(colorJava.getRed() / 255f, colorJava.getGreen() / 255f, colorJava.getBlue() / 255f, 1);
    }

    public @Nullable TrajectoryItem getLastPath() {
        return lastPath;
    }
}