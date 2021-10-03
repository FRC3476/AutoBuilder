package me.varun.autobuilder.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import org.jetbrains.annotations.NotNull;

public class PointRenderer {
    protected final float radius;
    protected float x;
    protected float y;
    protected Color color;

    public PointRenderer(float x, float y, Color color, float radius) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(@NotNull Vector2 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public PointRenderer(@NotNull Vector3 pos, Color color, float radius) {
        this.x = pos.x;
        this.y = pos.y;
        this.color = color;
        this.radius = radius;
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(@NotNull ShapeRenderer shape, @NotNull OrthographicCamera camera) {
        shape.setColor(color);
        shape.circle(x * AutoBuilder.POINT_SCALE_FACTOR, y * AutoBuilder.POINT_SCALE_FACTOR, radius, 50);
    }


    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setPosition(@NotNull Vector2 position) {
        setX(position.x);
        setY(position.y);
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public @NotNull Vector2 getPos2() {
        return new Vector2(x, y);
    }

    public @NotNull Vector3 getPos3() {
        return new Vector3(x, y, 0);
    }

    public @NotNull Vector3 getRenderPos3() {
        return new Vector3(x * AutoBuilder.POINT_SCALE_FACTOR, y * AutoBuilder.POINT_SCALE_FACTOR, 0);
    }

    public @NotNull Vector2 getRenderPos2() {
        return new Vector2(x * AutoBuilder.POINT_SCALE_FACTOR, y * AutoBuilder.POINT_SCALE_FACTOR);
    }

    @Override
    public @NotNull String toString() {
        return "PointRenderer{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
