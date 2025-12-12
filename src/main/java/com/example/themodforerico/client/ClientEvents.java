package com.example.themodforerico.client;

import com.example.themodforerico.TheModForErico;
import com.example.themodforerico.client.questsystem.QuestBookScreen;
import com.example.themodforerico.client.questsystem.QuestEntry;
import com.example.themodforerico.client.questsystem.QuestManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TheModForErico.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping openBookKey = new KeyMapping(
            "key.themodforerico.open_book",
            GLFW.GLFW_KEY_K,
            "category.themodforerico.title"
    );

    @Mod.EventBusSubscriber(modid = TheModForErico.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(openBookKey);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (openBookKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new QuestBookScreen());
        }
    }

    // --- HUD (จอซ้าย) ---
    private static long hudStartTime = 0;
    private static final int HUD_WIDTH = 150;

    // กำหนดระยะที่จะให้ซ่อน Waypoint (เช่น 5 บล็อก)
    private static final double HIDE_DISTANCE = 5.0;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            QuestEntry active = QuestManager.getTrackedQuest();

            // ถ้าไม่มีเควส หรือเควสนั้นถูกซ่อนอยู่ (เพราะใกล้เกินไป) ไม่ต้องวาด HUD
            if (active == null) {
                if (hudStartTime != 0) hudStartTime = 0;
                return;
            }

            // คำนวณระยะทาง
            double distance = client.player.position().distanceTo(active.getTargetPos().getCenter());

            // *** Logic ใหม่: ถ้าใกล้กว่ากำหนด ให้ซ่อน HUD (และไม่วาด Waypoint) ***
            // แต่ไม่ต้อง setCompleted(true) เพื่อให้เวลาเดินถอยออกมา มันแสดงใหม่ได้
//            if (distance < HIDE_DISTANCE) {
//                // ถ้าอยากให้มีข้อความแจ้งเตือนเมื่อถึงจุด ใส่ตรงนี้ได้ (แต่ต้องระวัง Spam)
//                return;
//            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = client.font;

            if (hudStartTime == 0) hudStartTime = System.currentTimeMillis();
            long elapsed = System.currentTimeMillis() - hudStartTime;
            long duration = 500;
            float progress = Mth.clamp(elapsed / (float) duration, 0.0f, 1.0f);
            int finalX = 10;
            int currentX = (int) (finalX - HUD_WIDTH + (HUD_WIDTH * progress));

            String distText = String.format(" §b%.1fm", distance);
            guiGraphics.drawString(font, "| " + active.getTitle() + " " + distText, currentX, 100, 0xFFFFFF);
        }
    }

    // --- World Rendering (3D Waypoint) ---
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            QuestEntry active = QuestManager.getTrackedQuest();
            if (active == null) return;

            Minecraft client = Minecraft.getInstance();
            Vec3 camPos = event.getCamera().getPosition();
            Vec3 targetPos = active.getTargetPos().getCenter().add(0, 1.0, 0);
            double dist = camPos.distanceTo(targetPos);

            // *** Logic ใหม่: ถ้าใกล้กว่ากำหนด ให้ "return" ออกไปเลย (Waypoint หายไป) ***
            // แต่พอเดินถอยออกมา dist > HIDE_DISTANCE มันจะข้ามบรรทัดนี้ไปทำงานต่อ (Waypoint โผล่มา)
            if (dist < HIDE_DISTANCE) return;

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(targetPos.x - camPos.x, targetPos.y - camPos.y, targetPos.z - camPos.z);
            poseStack.mulPose(event.getCamera().rotation());

            float fixedScale = 0.005f;
            float scale = Math.max((float) dist * fixedScale, fixedScale);
            poseStack.scale(-scale, -scale, scale);

            Matrix4f matrix = poseStack.last().pose();
            Font font = client.font;

            String text1 = " ▼ ";
            String text2 = String.format("%.1f m", dist);
            float x1 = -font.width(text1) / 2.0f;
            float x2 = -font.width(text2) / 2.0f;

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            font.drawInBatch(text1, x1, -10, 0xFFFF00, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            font.drawInBatch(text2, x2, 0, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);

            buffer.endBatch();

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }
}