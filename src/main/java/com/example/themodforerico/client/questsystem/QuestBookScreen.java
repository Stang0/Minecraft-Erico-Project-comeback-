package com.example.themodforerico.client.questsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class QuestBookScreen extends Screen {

    private static final Component TITLE = Component.literal("Quest Book");
    private QuestList questList;
    private EditBox searchBox;
    private Button trackButton;
    private Button deleteButton;

    // เควสที่ถูกเลือกใน List (เพื่อแสดงรายละเอียดด้านขวา)
    @Nullable
    private QuestEntry selectedQuest;

    public QuestBookScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        // 1. คำนวณ Layout
        int listWidth = 150; // ความกว้างของรายการด้านซ้าย
        int topMargin = 40;
        int bottomMargin = 40;

        // 2. สร้าง Search Bar ด้านบนซ้าย
        this.searchBox = new EditBox(this.font, 10, 10, listWidth - 5, 20, Component.literal("Search"));
        this.searchBox.setResponder((text) -> this.questList.refreshList(text)); // พิมพ์ปุ๊บ กรองปั๊บ
        this.addRenderableWidget(this.searchBox);

        // 3. สร้าง Scrollable List (ด้านซ้าย)
        this.questList = new QuestList(this.minecraft, listWidth, this.height, topMargin, this.height - bottomMargin, 24);
        this.questList.setLeftPos(0); // ชิดซ้าย
        this.addRenderableWidget(this.questList);

        // 4. ปุ่ม Action (Track / Delete)
        int buttonY = this.height - 30;

        this.trackButton = this.addRenderableWidget(Button.builder(Component.literal("Track Quest"), (btn) -> {
            if (selectedQuest != null) QuestManager.trackQuest(selectedQuest);
        }).bounds(160, buttonY, 100, 20).build());

        this.deleteButton = this.addRenderableWidget(Button.builder(Component.literal("Delete"), (btn) -> {
            if (selectedQuest != null) {
                QuestManager.removeQuestByName(selectedQuest.getTitle());
                this.questList.refreshList(searchBox.getValue()); // รีเฟรชรายการ
                this.selectedQuest = null; // เคลียร์การเลือก
            }
        }).bounds(270, buttonY, 80, 20).build());

        // ปุ่ม Close
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> this.onClose())
                .bounds(this.width - 60, 10, 50, 20).build());

        // อัปเดตสถานะปุ่ม
        updateButtons();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // วาด List และ ปุ่ม

        // --- วาดรายละเอียดด้านขวา (Right Panel) ---
        int leftSideEnd = 160; // จุดสิ้นสุดของ List
        int rightWidth = this.width - leftSideEnd;
        int centerX = leftSideEnd + (rightWidth / 2);
        int centerY = this.height / 2;

        if (selectedQuest != null) {
            // 1. Title (ใหญ่ๆ)
            drawPulsingTitle(guiGraphics, selectedQuest.getTitle(), centerX, 60);

            // 2. Description
            guiGraphics.drawCenteredString(this.font, "§7" + selectedQuest.getDescription(), centerX, 90, 0xFFFFFF);

            // 3. Status Coordinates
            String coords = String.format("Target: [%d, %d, %d]",
                    selectedQuest.getTargetPos().getX(),
                    selectedQuest.getTargetPos().getY(),
                    selectedQuest.getTargetPos().getZ());
            guiGraphics.drawCenteredString(this.font, "§e" + coords, centerX, 110, 0xFFFFFF);

            // 4. Status Tracking
            if (QuestManager.getTrackedQuest() == selectedQuest) {
                guiGraphics.drawCenteredString(this.font, "§a[ CURRENTLY TRACKING ]", centerX, 140, 0xFFFFFF);
            }

        } else {
            // ถ้ายังไม่เลือกอะไร
            guiGraphics.drawCenteredString(this.font, "Select a quest to view details", centerX, centerY, 0xFFAAAAAA);
        }
    }

    private void updateButtons() {
        boolean active = (selectedQuest != null);
        this.trackButton.active = active;
        this.deleteButton.active = active;
    }

    // เอฟเฟกต์ Pulsing Title (เอามาจาก Code ของคุณ)
    private void drawPulsingTitle(GuiGraphics guiGraphics, String text, int x, int y) {
        float size = (float)(Math.abs(Math.cos((double)Util.getMillis() / 250.0D) * 0.1D) + 1.2D); // ขยายใหญ่กว่าปกตินิดนึง
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(size, size, 1.0f);
        guiGraphics.pose().translate(-x, -y, 0);
        guiGraphics.drawCenteredString(this.font, text, x, y, 0xFFFFFF);
        guiGraphics.pose().popPose();
    }

    // ==========================================
    // 📜 INNER CLASS: Custom Scrollable List
    // ==========================================

    class QuestList extends ObjectSelectionList<QuestList.QuestEntryItem> {

        public QuestList(Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
            this.refreshList("");
        }

        public void refreshList(String filter) {
            this.clearEntries();
            List<QuestEntry> quests = QuestManager.getAllQuests();

            for (QuestEntry q : quests) {
                // Logic การค้นหา (Search)
                if (filter.isEmpty() || q.getTitle().toLowerCase().contains(filter.toLowerCase())) {
                    this.addEntry(new QuestEntryItem(q));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - 20; // ความกว้างแถว
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 10;
        }

        // ==========================================
        // 📜 INNER CLASS: List Entry (แถวแต่ละแถว)
        // ==========================================
        public class QuestEntryItem extends ObjectSelectionList.Entry<QuestEntryItem> {
            private final QuestEntry quest;

            public QuestEntryItem(QuestEntry quest) {
                this.quest = quest;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                // สีตัวหนังสือ
                int color = 0xFFFFFF;
                if (QuestManager.getTrackedQuest() == this.quest) {
                    color = 0x55FF55; // สีเขียวถ้า Track อยู่
                }

                // วาดชื่อเควส
                guiGraphics.drawString(Minecraft.getInstance().font, this.quest.getTitle(), left + 5, top + 5, color, true);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // เมื่อคลิกที่แถวนี้
                QuestBookScreen.this.selectedQuest = this.quest;
                QuestList.this.setSelected(this);
                QuestBookScreen.this.updateButtons(); // อัปเดตปุ่ม Track/Delete
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(quest.getTitle());
            }
        }
    }
}