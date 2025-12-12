package com.example.themodforerico.client.questsystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class QuestBookScreen extends Screen {
    public QuestBookScreen() { super(Component.literal("Quest Book")); }

    @Override
    protected void init() {
        int y = 40;
        for (QuestEntry quest : QuestManager.getAllQuests()) {
            String btnText = (quest.isTracking() ? "§a[Active] " : "§7[ - ] ") + quest.getTitle();

            // ปุ่ม Track
            this.addRenderableWidget(Button.builder(Component.literal(btnText), button -> {
                if (quest.isTracking()) QuestManager.trackQuest(null);
                else QuestManager.trackQuest(quest);
                this.minecraft.setScreen(new QuestBookScreen());
            }).bounds(width / 2 - 100, y, 150, 20).build());

            // ปุ่มลบ
            this.addRenderableWidget(Button.builder(Component.literal("§cX"), button -> {
                QuestManager.removeQuest(quest);
                this.minecraft.setScreen(new QuestBookScreen());
            }).bounds(width / 2 + 60, y, 20, 20).build());

            y += 24;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, this.title, width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }
}