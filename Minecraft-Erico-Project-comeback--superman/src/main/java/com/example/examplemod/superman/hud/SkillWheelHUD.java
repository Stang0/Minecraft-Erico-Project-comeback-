package com.example.examplemod.superman.hud;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.superman.skill.SkillManager;
import com.example.examplemod.superman.skill.SkillManager.Skill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Circular Skill Wheel HUD
 * Shows 3 skill circles in the bottom-right corner
 * Mouse scroll to change selected skill
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class SkillWheelHUD {

    // Circle sizes
    private static final int CIRCLE_RADIUS_NORMAL = 20;
    private static final int CIRCLE_RADIUS_SELECTED = 26;
    private static final int CIRCLE_SPACING = 8;

    // Position (closer to bottom corner)
    private static final int MARGIN_RIGHT = 50;
    private static final int MARGIN_BOTTOM = 30; // Lower position

    // Colors (ARGB) - Brighter colors for visibility
    private static final int COLOR_SELECTED_BORDER = 0xFFFFD700; // Bright Gold
    private static final int COLOR_NORMAL_BORDER = 0xFFCCCCCC; // Light gray (more visible)
    private static final int COLOR_BACKGROUND = 0xC0222222; // Darker, more opaque
    private static final int COLOR_COOLDOWN = 0xE0FF4400; // Bright orange
    private static final int COLOR_READY = 0xFF00FF00; // Green (ready)

    // Circle segments for drawing
    private static final int CIRCLE_SEGMENTS = 32;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        // Tick cooldowns
        SkillManager.tickCooldowns(mc.player);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;
        if (mc.screen != null)
            return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        renderSkillWheel(graphics, mc.player, screenWidth, screenHeight);
    }

    private static void renderSkillWheel(GuiGraphics graphics, Player player, int screenWidth, int screenHeight) {
        Skill selectedSkill = SkillManager.getSelectedSkill(player);
        Skill[] skills = Skill.values();

        // Base position (bottom-right corner)
        int baseX = screenWidth - MARGIN_RIGHT;
        int baseY = screenHeight - MARGIN_BOTTOM;

        // Curved layout offsets for each skill (creates diagonal arc like the drawing)
        // Skill 0 (SPEED): top-right
        // Skill 1 (FLIGHT): middle
        // Skill 2 (LASER): bottom-left
        int[][] positions = {
                { 0, -80 }, // SPEED - top right
                { -30, -40 }, // FLIGHT - middle (shifted left)
                { -50, 0 } // LASER - bottom left
        };

        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            boolean isSelected = (skill == selectedSkill);
            int radius = isSelected ? CIRCLE_RADIUS_SELECTED : CIRCLE_RADIUS_NORMAL;

            int centerX = baseX + positions[i][0];
            int centerY = baseY + positions[i][1];

            // Get cooldown info
            float cooldownPercent = SkillManager.getCooldownPercent(player, skill);
            boolean isReady = SkillManager.isSkillReady(player, skill);

            // Draw circle
            drawSkillCircle(graphics, centerX, centerY, radius, skill, isSelected, cooldownPercent, isReady);
        }
    }

    private static void drawSkillCircle(GuiGraphics graphics, int centerX, int centerY, int radius,
            Skill skill, boolean isSelected, float cooldownPercent, boolean isReady) {

        PoseStack poseStack = graphics.pose();

        // Draw background circle
        drawFilledCircle(poseStack, centerX, centerY, radius, COLOR_BACKGROUND);

        // Draw cooldown overlay if on cooldown
        if (cooldownPercent > 0) {
            drawCooldownArc(poseStack, centerX, centerY, radius, cooldownPercent);
        }

        // Draw border
        int borderColor = isSelected ? COLOR_SELECTED_BORDER : COLOR_NORMAL_BORDER;
        drawCircleOutline(poseStack, centerX, centerY, radius, borderColor, isSelected ? 3 : 2);

        // Draw skill icon/text
        String displayText = skill.getIcon();
        int textWidth = Minecraft.getInstance().font.width(displayText);
        int textX = centerX - textWidth / 2;
        int textY = centerY - 4;
        int textColor = isReady ? 0xFFFFFFFF : 0xFF888888;
        graphics.drawString(Minecraft.getInstance().font, displayText, textX, textY, textColor, false);

        // Draw skill name below if selected
        if (isSelected) {
            String name = skill.getDisplayName();
            int nameWidth = Minecraft.getInstance().font.width(name);
            graphics.drawString(Minecraft.getInstance().font, name,
                    centerX - nameWidth / 2, centerY + radius + 2, 0xFFFFFFFF, true);
        }
    }

    private static void drawFilledCircle(PoseStack poseStack, int centerX, int centerY, int radius, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // Center vertex
        buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a).endVertex();

        // Circle vertices
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            float x = (float) (centerX + Math.cos(angle) * radius);
            float y = (float) (centerY + Math.sin(angle) * radius);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    private static void drawCircleOutline(PoseStack poseStack, int centerX, int centerY, int radius, int color,
            int thickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(thickness);

        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            float x = (float) (centerX + Math.cos(angle) * radius);
            float y = (float) (centerY + Math.sin(angle) * radius);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    private static void drawCooldownArc(PoseStack poseStack, int centerX, int centerY, int radius, float percent) {
        if (percent <= 0)
            return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float a = (COLOR_COOLDOWN >> 24 & 255) / 255.0f;
        float r = (COLOR_COOLDOWN >> 16 & 255) / 255.0f;
        float g = (COLOR_COOLDOWN >> 8 & 255) / 255.0f;
        float b = (COLOR_COOLDOWN & 255) / 255.0f;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // Center vertex
        buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a).endVertex();

        // Arc from top going clockwise
        int arcSegments = (int) (CIRCLE_SEGMENTS * percent);
        for (int i = 0; i <= arcSegments; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / CIRCLE_SEGMENTS;
            float x = (float) (centerX + Math.cos(angle) * radius);
            float y = (float) (centerY + Math.sin(angle) * radius);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
}
