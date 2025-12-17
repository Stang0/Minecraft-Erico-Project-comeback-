package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.client.model.SupermanPlayerModel;
import com.example.superheromod.logic.FlightHandler;
import com.example.superheromod.util.FlightAnimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3; // เพิ่ม import นี้
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientFlightEvent {

    private static boolean isRenderingManual = false;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (isRenderingManual) return;

        Player player = event.getEntity();

        if (FlightHandler.isFlying(player)) {
            if (event.getRenderer() instanceof PlayerRenderer) {
                PlayerRenderer renderer = (PlayerRenderer) event.getRenderer();
                if (renderer.getModel() instanceof SupermanPlayerModel) {

                    event.setCanceled(true);
                    isRenderingManual = true;

                    PoseStack poseStack = event.getPoseStack();
                    float partialTicks = event.getPartialTick();

                    // ❌ ลบ FlightRenderState.update(...) ทิ้งไปเลย
                    // (เพราะเราย้ายไปทำใน ClientTickHandler แล้ว)

                    FlightAnimator anim = FlightAnimator.INSTANCE;

                    // ❌ ลบ anim.updateFrame(...) ทิ้ง
                    // (เพราะเราทำใน ClientTickHandler เช่นกัน)

                    // 1. ส่งค่า Input ให้ Animator (เพื่อให้ updateVel ทำงานถูก)
                    Vec3 motion = player.getDeltaMovement();
                    anim.motX = (float) motion.x;
                    anim.motY = (float) motion.y;
                    anim.motZ = (float) motion.z;
                    anim.forward = FlightRenderState.smoothForward;
                    anim.strafe = FlightRenderState.smoothStrafe;

                    // ❌ ไม่ต้อง set anim.data1 เองแล้ว เพราะ updateAnimations จะไปดึงจาก State เอง

                    // 2. เรียก updateAnimations โดยส่ง partialTicks เข้าไป
                    anim.updateAnimations(partialTicks);

                    // 3. เริ่มวาด
                    poseStack.pushPose();

                    applyBodyRotation(poseStack, player, anim, partialTicks);

                    if (player instanceof AbstractClientPlayer clientPlayer) {
                        renderer.render(clientPlayer, player.getYRot(), partialTicks, poseStack, event.getMultiBufferSource(), event.getPackedLight());
                    }

                    poseStack.popPose();

                    isRenderingManual = false;
                }
            }
        }
    }

    private static void applyBodyRotation(PoseStack poseStack, Player player, FlightAnimator anim, float partialTicks) {
        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float smoothedPitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());

        poseStack.translate(0, 0.75, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyYaw));

        // ⭐ ใช้ค่า Boost ที่คำนวณเสร็จแล้วใน anim.data1 แทน FlightRenderState.renderBoost
        // (เพราะ data1 คือค่า boost ที่ผ่าน curve ความสมูทมาแล้วใน FlightAnimator)
        float boostFactor = anim.data1;

        // ท่านอนบิน (Pitch)
        float tilt = -boostFactor * (smoothedPitch + 80.0f);
        tilt = Mth.clamp(tilt, -180f, 180f);
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));

        // การเอียงตัว (Roll)
        // ⭐ เปลี่ยน renderYawDelta เป็น getter และใช้ boostFactor
        float yawDelta = FlightRenderState.getRenderYawDelta(partialTicks);
        float roll = yawDelta * boostFactor * 2.5f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0f - bodyYaw)));
        poseStack.translate(0, -0.75, 0);
        poseStack.translate(0, -0.85f * boostFactor, 0);
    }
}