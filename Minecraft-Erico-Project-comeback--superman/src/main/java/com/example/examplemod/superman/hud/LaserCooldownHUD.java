package com.example.examplemod.superman.hud;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.superman.SupermanFlightHandler;
import com.example.examplemod.superman.skill.LaserSkillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Custom HUD for displaying laser skill cooldown
 * DISABLED - Replaced by SkillWheelHUD
 */
// @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class LaserCooldownHUD {

    // HUD dimensions
    private static final int ICON_SIZE = 32;
    private static final int BAR_WIDTH = 40;
    private static final int BAR_HEIGHT = 6;
    private static final int PADDING = 10;
    private static final int MARGIN_RIGHT = 10;
    private static final int MARGIN_BOTTOM = 40;

    // Colors (ARGB format)
    private static final int COLOR_READY = 0xFF00FF00; // Green
    private static final int COLOR_COOLDOWN = 0xFFFF6600; // Orange
    private static final int COLOR_BAR_BG = 0x80000000; // Semi-transparent black
    private static final int COLOR_BAR_FILL = 0xFFFFAA00; // Gold
    private static final int COLOR_BAR_READY = 0xFF00FF00; // Green
    private static final int COLOR_ICON_BG = 0x80000000; // Semi-transparent black
    private static final int COLOR_ICON_BORDER = 0xFFFFFFFF; // White

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Only render after hotbar
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        Player player = mc.player;

        // Only show HUD while flying
        if (!SupermanFlightHandler.isFlying(player)) {
            return;
        }

        // Don't render if inventory or other screens are open
        if (mc.screen != null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        renderCooldownHUD(graphics, player, screenWidth, screenHeight);
    }

    /**
     * Render the cooldown HUD in the bottom-right corner
     */
    private static void renderCooldownHUD(GuiGraphics graphics, Player player, int screenWidth, int screenHeight) {
        boolean isReady = LaserSkillHandler.isReady(player);
        float cooldownPercent = LaserSkillHandler.getCooldownPercent(player);
        int remainingTicks = LaserSkillHandler.getCooldownTicks(player);

        // Calculate position (bottom-right corner)
        int hudWidth = ICON_SIZE + PADDING + BAR_WIDTH;
        int hudHeight = ICON_SIZE;

        int x = screenWidth - hudWidth - MARGIN_RIGHT;
        int y = screenHeight - hudHeight - MARGIN_BOTTOM;

        // Draw icon background
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, COLOR_ICON_BG);

        // Draw icon border
        int borderColor = isReady ? COLOR_READY : COLOR_COOLDOWN;
        drawBorder(graphics, x, y, ICON_SIZE, ICON_SIZE, borderColor);

        // Draw skill icon (laser/eye symbol using text)
        String icon = isReady ? "⚡" : "○";
        int iconColor = isReady ? COLOR_READY : 0xFFAAAAAA;
        int iconX = x + (ICON_SIZE - 8) / 2;
        int iconY = y + (ICON_SIZE - 9) / 2;
        graphics.drawString(Minecraft.getInstance().font, icon, iconX, iconY, iconColor, true);

        // Draw cooldown bar background
        int barX = x + ICON_SIZE + PADDING;
        int barY = y + (ICON_SIZE - BAR_HEIGHT) / 2;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, COLOR_BAR_BG);

        // Draw cooldown bar fill
        if (isReady) {
            // Full bar when ready
            graphics.fill(barX + 1, barY + 1, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT - 1, COLOR_BAR_READY);
        } else {
            // Progress bar during cooldown
            int fillWidth = (int) ((1.0f - cooldownPercent) * (BAR_WIDTH - 2));
            if (fillWidth > 0) {
                graphics.fill(barX + 1, barY + 1, barX + 1 + fillWidth, barY + BAR_HEIGHT - 1, COLOR_BAR_FILL);
            }
        }

        // Draw cooldown text
        String text;
        if (isReady) {
            text = "READY";
        } else {
            // Show seconds remaining
            float secondsRemaining = remainingTicks / 20.0f;
            text = String.format("%.1fs", secondsRemaining);
        }

        int textColor = isReady ? COLOR_READY : 0xFFFFFFFF;
        int textX = barX + (BAR_WIDTH - Minecraft.getInstance().font.width(text)) / 2;
        int textY = y + ICON_SIZE + 2;
        graphics.drawString(Minecraft.getInstance().font, text, textX, textY, textColor, true);

        // Draw key hint
        String keyHint = "[G]";
        int hintX = x + (ICON_SIZE - Minecraft.getInstance().font.width(keyHint)) / 2;
        int hintY = y + ICON_SIZE + 2;
        graphics.drawString(Minecraft.getInstance().font, keyHint, hintX, hintY, 0xFFAAAAAA, true);
    }

    /**
     * Draw a 1-pixel border around a rectangle
     */
    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Top
        graphics.fill(x, y, x + width, y + 1, color);
        // Bottom
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        graphics.fill(x, y, x + 1, y + height, color);
        // Right
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
