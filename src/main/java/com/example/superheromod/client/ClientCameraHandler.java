package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.logic.FlightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, value = Dist.CLIENT)
public class ClientCameraHandler {

    private static float currentRoll = 0;

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.isPaused()) return;

        boolean isFlying = FlightHandler.isFlying(player);
        float targetRoll = 0;

        // ⭐ แก้ไขบรรทัดนี้: ใช้ getRenderBoost(partialTick) แทนตัวแปรเดิม
        float partialTick = (float) event.getPartialTick();
        float boostFactor = FlightRenderState.getRenderBoost(partialTick);

        if (isFlying && !player.onGround() && boostFactor > 0.05f) {
            // ใช้ค่า YawDelta ที่สมูทแล้วจาก RenderState จะนิ่งกว่าเดิมมาก
            float yawDelta = FlightRenderState.getRenderYawDelta(partialTick);

            float speedFactor = (float) player.getDeltaMovement().horizontalDistance() * 20f;
            speedFactor = Mth.clamp(speedFactor, 0, 1.5f);

            // คำนวณ Roll
            targetRoll = yawDelta * 3.0f * speedFactor * boostFactor;
            targetRoll = Mth.clamp(targetRoll, -40, 40);
        }

        currentRoll = Mth.lerp(0.1f, currentRoll, targetRoll);
        event.setRoll(currentRoll);
    }
}