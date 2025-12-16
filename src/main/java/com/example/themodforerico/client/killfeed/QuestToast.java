package com.example.themodforerico.client.killfeed;

import com.example.themodforerico.TheModForErico;
import com.example.themodforerico.utils.DefaultUVs;
import com.example.themodforerico.utils.TextureUV;
import com.example.themodforerico.utils.TypeBasedUVs;
import com.example.themodforerico.utils.MathEasing;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = TheModForErico.MODID, value = Dist.CLIENT)
public class QuestToast {

    private static final ResourceLocation SPRITE_SHEET =
            new ResourceLocation(TheModForErico.MODID, "textures/gui/vanilla.png");
    private static final int TEX_SIZE = 256;
    private static final int TOAST_WIDTH = 162; // เพิ่มค่าคงที่ความกว้าง
    private static final Random random = new Random();

    // ====================================================
    // ⚙️ CONFIGURATION
    // ====================================================
    private static final TypeBasedUVs TYPE_UV = DefaultUVs.TASK;
    private static final TextureUV BG_UV = DefaultUVs.BACKGROUND;
    private static final TextureUV PLAQUE_UV = DefaultUVs.PLAQUE;

    private static final long DURATION = 5000;
    private static final long FADE_OUT_DURATION = 1000;

    // Animation Timings
    private static final long ICON_START = 0;
    private static final long ICON_DURATION = 1000;
    private static final long BANNER_START = 500;
    private static final long BANNER_DURATION = 500;
    private static final long BG_START = 600;
    private static final long BG_DURATION = 800;
    private static final long TEXT_START = 1000; // เริ่มแสดง Text ที่ 1 วินาที
    private static final long TEXT_DURATION = 500; // ใช้เวลา Fade in 0.5 วินาที

    // Sound Timings
    private static final long SOUND_POP_1_TIME = 0;
    private static final long SOUND_QUEST_TIME = 200;
    private static final long SOUND_POP_2_TIME = DURATION - 500;

    // State Variables
    private static long showTime = 0;
    private static boolean isVisible = false;
    private static int soundState = 0;

    private static List<FormattedCharSequence> titleLines;
    private static List<FormattedCharSequence> descriptionLines;

    public static void show(Component title, Component desc) {
        Minecraft mc = Minecraft.getInstance();
        titleLines = mc.font.split(desc, 142);
        descriptionLines = mc.font.split(title, 142);

        showTime = System.currentTimeMillis();
        isVisible = true;
        soundState = 0;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!isVisible) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long timeElapsed = System.currentTimeMillis() - showTime;

        if (timeElapsed > DURATION) {
            isVisible = false;
            return;
        }

        handleSounds(mc, timeElapsed);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int centerX = screenWidth / 2 - (TOAST_WIDTH / 2);

        // Calculate Progress
        float iconProgress = getProgress(timeElapsed, ICON_START, ICON_DURATION);
        float bannerProgress = getProgress(timeElapsed, BANNER_START, BANNER_DURATION);
        float bgProgress = getProgress(timeElapsed, BG_START, BG_DURATION);
        float textProgress = getProgress(timeElapsed, TEXT_START, TEXT_DURATION);

        float fadeOutProgress = 0.0f;
        if (timeElapsed > (DURATION - FADE_OUT_DURATION)) {
            fadeOutProgress = (float)(timeElapsed - (DURATION - FADE_OUT_DURATION)) / FADE_OUT_DURATION;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, 10, 0);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 1. FADE OUT
        if (fadeOutProgress > 0.0f) {
            float fadeOutY = MathEasing.easeInLerp(0.0f, -80.0f, fadeOutProgress);
            guiGraphics.pose().translate(0.0f, fadeOutY, 0.0f);
        }

        // 2. BACKGROUND & PLAQUE
        if (bgProgress > 0.0f) {
            guiGraphics.pose().pushPose();
            if (bgProgress < 1.0f) {
                float y = MathEasing.easeOutLerp(-50.0f, 0.0f, bgProgress);
                guiGraphics.pose().translate(0.0f, y, 0.0f);
            }
            drawTexture(guiGraphics, 0, 20, TOAST_WIDTH, 40, BG_UV);
            drawTexture(guiGraphics, 144, 56, 9, 14, PLAQUE_UV);
            guiGraphics.pose().popPose();
        }

        // 3. BANNER
        if (bannerProgress > 0.0f) {
            guiGraphics.pose().pushPose();
            if (bannerProgress < 1.0f) {
                float xScale = MathEasing.easeOutLerp(0.0f, 1.0f, bannerProgress);
                scaleAround(guiGraphics, xScale, 1.0f, 81.0f, 0.0f);
            }
            drawTexture(guiGraphics, 0, 5, TOAST_WIDTH, 14, TYPE_UV.banner());
            guiGraphics.pose().popPose();
        }

        // 4. ICON
        if (iconProgress > 0.0f) {
            guiGraphics.pose().pushPose();

            if (iconProgress < 1.0f) {
                float scale = MathEasing.easeOutLerp(0.0f, 1.0f, iconProgress);
                scaleAround(guiGraphics, scale, scale, 81.0f, 13.0f);
                float y = MathEasing.easeOutLerp(-50.0f, 0.0f, iconProgress);
                guiGraphics.pose().translate(0.0f, y, 0.0f);
            } else {
                float loopOffset = sinusoidLoop(timeElapsed, 1.6f, 1.5f);
                guiGraphics.pose().translate(0.0f, loopOffset - 5.0f, 0.0f);
            }

            drawTexture(guiGraphics, 68, 0, 26, 26, TYPE_UV.frame());
            guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WRITABLE_BOOK), 73, 5);

            guiGraphics.pose().popPose();
        }

        // 5. TEXT (Fade In & Centered)
        // เริ่มวาดทันทีที่ textProgress > 0 และส่งค่า progress ไปเป็น alpha
        if (textProgress > 0.0f) {
            drawCenteredText(mc, guiGraphics, textProgress);
        }

        guiGraphics.pose().popPose();
    }

    // ====================================================
    // 🛠️ HELPER METHODS
    // ====================================================

    private static void drawTexture(GuiGraphics guiGraphics, int x, int y, int width, int height, TextureUV uv) {
        guiGraphics.blit(SPRITE_SHEET, x, y, uv.u(), uv.v(), width, height, TEX_SIZE, TEX_SIZE);
    }

    // เมธอดใหม่สำหรับวาด Text แบบจัดกลางและมี Fade In
// เมธอดใหม่สำหรับวาด Text แบบจัดกลางและมี Fade In
    private static void drawCenteredText(Minecraft mc, GuiGraphics guiGraphics, float alpha) {
        int titleColor = applyAlpha(0xFFFF00, alpha);
        int descColor = applyAlpha(0xFFFFFF, alpha);
        int toastCenterX = TOAST_WIDTH / 2;

        // --- Title ---
        if (titleLines != null && !titleLines.isEmpty()) {
            FormattedCharSequence titleLine = titleLines.get(0);
            if (titleLines.size() == 1) {
                drawCenteredLine(guiGraphics, mc, titleLine, toastCenterX, 25, titleColor);
            } else {
                int ellipsisWidth = mc.font.width("...");
                int lineWidth = mc.font.width(titleLine);
                guiGraphics.drawString(mc.font, titleLine, toastCenterX - (lineWidth + ellipsisWidth) / 2, 25, titleColor, true);
                guiGraphics.drawString(mc.font, "...", toastCenterX - (lineWidth + ellipsisWidth) / 2 + lineWidth, 25, titleColor, true);
            }
        }

        // --- Description ---
        if (descriptionLines != null && !descriptionLines.isEmpty()) {
            int lineY = 38;
            for (int i = 0; i < descriptionLines.size(); i++) {
                FormattedCharSequence line = descriptionLines.get(i);

                // แก้ไขจุดที่ error ตรงนี้:
                if (i == 2) {
                    // ใช้ Component.literal("...").getVisualOrderText() แทน
                    line = Component.literal("...").getVisualOrderText();
                } else if (i > 2) {
                    break;
                }

                int lineWidth = mc.font.width(line);
                int centeredX = (TOAST_WIDTH - lineWidth) / 2;

                guiGraphics.drawString(mc.font, line, centeredX, lineY, descColor, false);
                lineY += 9;
            }
        }
    }

    // Helper สำหรับผสม Alpha เข้ากับสี RGB
    private static int applyAlpha(int baseColor, float alpha) {
        // ป้องกันค่า alpha เกินช่วง 0.0 - 1.0
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        // แปลง float 0.0-1.0 เป็น byte 0-255
        int alphaByte = (int)(alpha * 255.0f);
        // นำ Alpha ไปใส่ใน bit หลักที่ 24-31 และรวมกับสีเดิม
        return (alphaByte << 24) | (baseColor & 0x00FFFFFF);
    }

    private static void drawCenteredLine(GuiGraphics guiGraphics, Minecraft mc, FormattedCharSequence text, int x, int y, int color) {
        int width = mc.font.width(text);
        // drawString แบบมีเงา (true)
        guiGraphics.drawString(mc.font, text, x - width / 2, y, color, true);
    }

    private static float sinusoidLoop(long time, float speed, float strength) {
        float scaledTime = (float)time * 0.00125F * speed;
        return (float)Math.sin((double)scaledTime) * strength;
    }

    private static void handleSounds(Minecraft mc, long timeElapsed) {
        float volume = 1.0f;
        long pop1Time = 0;
        long questSoundTime = 200;
        long pop2Time = DURATION - 500;

        switch (soundState) {
            case 0:
                if (timeElapsed >= pop1Time) {
                    playSound(mc, SoundEvents.UI_TOAST_IN, volume);
                    soundState++;
                }
                break;
            case 1:
                if (timeElapsed >= questSoundTime) {
                    playSound(mc, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, volume);
                    soundState++;
                }
                break;
            case 2:
                if (timeElapsed >= pop2Time) {
                    playSound(mc, SoundEvents.UI_TOAST_OUT, volume);
                    soundState++;
                }
                break;
        }
    }

    private static void playSound(Minecraft mc, net.minecraft.sounds.SoundEvent sound, float volume) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, random.nextFloat() * 0.1f + 0.95f, volume));
    }

    private static void scaleAround(GuiGraphics guiGraphics, float scaleX, float scaleY, float pivotX, float pivotY) {
        guiGraphics.pose().translate(pivotX, pivotY, 0.0f);
        guiGraphics.pose().scale(scaleX, scaleY, 1.0f);
        guiGraphics.pose().translate(-pivotX, -pivotY, 0.0f);
    }

    private static float getProgress(long elapsed, long start, long duration) {
        if (elapsed < start) return 0.0f;
        if (elapsed > start + duration) return 1.0f;
        return (float) (elapsed - start) / duration;
    }
}