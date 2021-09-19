package me.varun.autobuilder.events.movablepoint;

import org.jetbrains.annotations.NotNull;

public interface MovablePointEventHandler {
    void onPointMove(@NotNull PointMoveEvent event);

    void onPointClick(@NotNull PointClickEvent event);
}
