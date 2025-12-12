package com.example.themodforerico.client.questsystem;

import net.minecraft.core.BlockPos;

public class QuestEntry {
    private String title;
    private String description;
    private BlockPos targetPos;
    private boolean isTracking;
    private boolean shouldtrack;
    private boolean isCompleted;

    public QuestEntry(String title, String description, BlockPos targetPos , boolean shouldtrack) {
        this.title = title;
        this.description = description;
        this.targetPos = targetPos;
        this.shouldtrack = shouldtrack;
        this.isTracking = false;
        this.isCompleted = false;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BlockPos getTargetPos() { return targetPos; }
    public boolean isTracking() { return isTracking; }
    public void setTracking(boolean tracking) { isTracking = tracking; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}