package com.example.themodforerico.client.killfeed;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

public class KillFeedToast implements Toast {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/gui/toasts.png");
    private static final ResourceLocation STEVE_SKIN = new ResourceLocation("minecraft:textures/entity/steve.png");
    private final Component text;
    private final String skullOwnerName;
    private long startTime;

    public KillFeedToast(String message, String skullOwnerName) {
        this.text = Component.literal(message);
        this.skullOwnerName = skullOwnerName;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent manager, long startTime) {
        if (this.startTime == 0L) {
            this.startTime = startTime;
        }

        long elapsed = startTime - this.startTime;
        Font font = manager.getMinecraft().font;

        // 1. วาดพื้นหลัง
        guiGraphics.blit(TEXTURE, 0, 0, 0, 0, 160, 32);

        // 2. วาดไอคอน
        ResourceLocation skinTexture = STEVE_SKIN;
        if (manager.getMinecraft().getConnection() != null) {
            PlayerInfo entry = manager.getMinecraft().getConnection().getPlayerInfo(skullOwnerName);
            if (entry != null) {
                skinTexture = entry.getSkinLocation();
            }
        }

        RenderSystem.setShaderTexture(0, skinTexture);
        guiGraphics.blit(skinTexture, 8, 8, 16, 16, 8, 8, 8, 8, 64, 64);
        guiGraphics.blit(skinTexture, 8, 8, 16, 16, 40, 8, 8, 8, 64, 64);

        // --- Timing ---
        long fadeStart = 1000L;
        long fadeDuration = 500L;

        float titleOpacity = 1.0f;
        float descOpacity = 0.0f;

        if (elapsed >= fadeStart) {
            float progress = Mth.clamp((elapsed - fadeStart) / (float)fadeDuration, 0.0f, 1.0f);
            titleOpacity = 1.0f - progress;
            descOpacity = progress;
        }

        RenderSystem.enableBlend();

        // 3. วาด Title
        if (titleOpacity > 0.05f) {
            int alpha = (int)(titleOpacity * 255.0F) << 24;
            int color = alpha | 0xFFA000;
            guiGraphics.drawString(font, Component.literal("รายงานข่าวผู้เสียชีวิต!"), 30, 12, color, false);
        }

        // 4. วาด Description
        if (descOpacity > 0.05f) {
            int alpha = (int)(descOpacity * 255.0F) << 24;
            int color = alpha | 0xFFFFFF;
            int maxWidth = 125;

            List<FormattedCharSequence> lines = font.split(this.text, maxWidth);

            if (lines.size() == 1) {
                guiGraphics.drawString(font, lines.get(0), 30, 12, color, false);
            } else {
                guiGraphics.drawString(font, lines.get(0), 30, 7, color, false);
                if (lines.size() >= 2) {
                    guiGraphics.drawString(font, lines.get(1), 30, 18, color, false);
                }
            }
        }

        RenderSystem.disableBlend();

        return elapsed < 4000L ? Visibility.SHOW : Visibility.HIDE;
    }
}