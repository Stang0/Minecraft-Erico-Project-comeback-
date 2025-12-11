package com.example.killfeed1_20_10_1.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText; // Import สำคัญสำหรับตัดคำ
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List; // Import List

public class KillFeedToast implements Toast {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/toasts.png");
    private static final Identifier STEVE_SKIN = new Identifier("minecraft", "textures/entity/steve.png");

    private final Text text;
    private final String skullOwnerName;
    private long startTime;

    public KillFeedToast(String message, String skullOwnerName) {
        this.text = Text.of(message);
        this.skullOwnerName = skullOwnerName;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.startTime == 0L) {
            this.startTime = startTime;
        }

        long elapsed = startTime - this.startTime;
        TextRenderer textRenderer = manager.getClient().textRenderer;

        // 1. วาดพื้นหลัง
        context.drawTexture(TEXTURE, 0, 0, 0, 0, 160, 32);

        // 2. วาดไอคอน
        Identifier skinTexture = STEVE_SKIN;
        if (manager.getClient().getNetworkHandler() != null) {
            PlayerListEntry entry = manager.getClient().getNetworkHandler().getPlayerListEntry(skullOwnerName);
            if (entry != null) {
                skinTexture = entry.getSkinTexture();
            }
        }
        RenderSystem.setShaderTexture(0, skinTexture);
        context.drawTexture(skinTexture, 8, 8, 16, 16, 8, 8, 8, 8, 64, 64);
        context.drawTexture(skinTexture, 8, 8, 16, 16, 40, 8, 8, 8, 64, 64);

        // --- Timing ---
        long fadeStart = 1000L;
        long fadeDuration = 500L;

        float titleOpacity = 1.0f;
        float descOpacity = 0.0f;

        if (elapsed >= fadeStart) {
            float progress = MathHelper.clamp((elapsed - fadeStart) / (float)fadeDuration, 0.0f, 1.0f);
            titleOpacity = 1.0f - progress;
            descOpacity = progress;
        }

        RenderSystem.enableBlend();

        // 3. วาด Title ("Goal Reached!") - แสดงตรงกลางเสมอ
        if (titleOpacity > 0.05f) {
            int alpha = (int)(titleOpacity * 255.0F) << 24;
            int color = alpha | 0xFFA000;
            context.drawText(textRenderer, Text.of("รายงานข่าวผู้เสียชีวิต!"), 30, 12, color, false);
        }

        // 4. วาด Description (รายละเอียด) - แบบตัดบรรทัด
        if (descOpacity > 0.05f) {
            int alpha = (int)(descOpacity * 255.0F) << 24;
            int color = alpha | 0xFFFFFF;

            // ความกว้างสูงสุดที่จะยอมให้เขียนได้ (160 - 30 - 5 = 125 px)
            int maxWidth = 125;

            // สั่งตัดคำอัตโนมัติ (จะได้ List ของบรรทัดที่ตัดแล้ว)
            List<OrderedText> lines = textRenderer.wrapLines(this.text, maxWidth);

            if (lines.size() == 1) {
                // ถ้ามีแค่ 1 บรรทัด -> วาดตรงกลาง (Y=12) เหมือนเดิม
                context.drawText(textRenderer, lines.get(0), 30, 12, color, false);
            } else {
                // ถ้ามี 2 บรรทัดขึ้นไป
                // วาดบรรทัดที่ 1 ที่ Y=7 (ด้านบน)
                context.drawText(textRenderer, lines.get(0), 30, 7, color, false);

                // วาดบรรทัดที่ 2 ที่ Y=18 (ด้านล่าง)
                if (lines.size() >= 2) {
                    context.drawText(textRenderer, lines.get(1), 30, 18, color, false);
                }
            }
        }

        RenderSystem.disableBlend();

        return elapsed < 4000L ? Visibility.SHOW : Visibility.HIDE;
    }
}