package com.example.superheromod.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlightHandler {
    private static final String TAG_FLYING = "superman_is_flying";

    public static boolean isFlying(Player player) {
        return player.getPersistentData().getBoolean(TAG_FLYING);
    }

    public static void setFlying(Player player, boolean enabled) {
        player.getPersistentData().putBoolean(TAG_FLYING, enabled);
        if (enabled) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
            player.sendSystemMessage(Component.literal("§bFlight ON"));
        } else {
            player.setNoGravity(false);
            player.sendSystemMessage(Component.literal("§6Flight OFF"));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        // Anti-Drop: ป้องกันการร่วงตอนเริ่มบิน
        if (player.onGround() && isFlying(player) && player.getDeltaMovement().y <= 0.01) {
            setFlying(player, false);
            return;
        }

        if (isFlying(player)) {
            player.setNoGravity(true);
            player.fallDistance = 0;

            Vec3 motion = player.getDeltaMovement();

            boolean boost = player.isSprinting();
            boolean brake = player.isShiftKeyDown();

            float forward = player.zza; // W / S
            float strafe  = player.xxa; // A / D
            boolean hasInput = forward != 0 || strafe != 0;

// ===== DRAG =====
            double drag = brake ? 0.75 : boost ? 0.985 : 0.9;
            motion = motion.scale(drag);

// ===== INPUT DIRECTION (Superman Look-controlled) =====
            if (hasInput) {
                // ใช้ Look Vector ของผู้เล่นเพื่อให้บินไปตามทิศที่เป้าเล็งชี้ (รวมขึ้น/ลง)
                Vec3 look = player.getLookAngle();

                // คำนวณความเร็ว
                double baseSpeed = boost ? 0.25 : 0.08; // เพิ่มความเร็ว Boost ให้พุ่งแรงขึ้น

                // ถ้ากด W อย่างเดียว ให้พุ่งไปตาม Look Vector
                // ถ้ากด A/S/D ให้ผสมทิศทาง WASD ปกติเข้าไป
                Vec3 inputDir;
                if (forward > 0 && strafe == 0) {
                    inputDir = look; // พุ่งตามเป้าเล็งเป๊ะๆ
                } else {
                    inputDir = new Vec3(strafe, 0, forward).yRot(-player.getYRot() * Mth.DEG_TO_RAD).normalize();
                }

                motion = motion.add(inputDir.scale(baseSpeed));
            }

// ===== VERTICAL (Superman style) =====
            if (brake) {
                // ดิ่งเบรก
                motion = motion.add(0, -0.08, 0);
            } else if (!boost) {
                // hover นิ่ง
                motion = new Vec3(
                        motion.x,
                        Mth.lerp(0.25, motion.y, 0.0),
                        motion.z
                );
            }

// ===== APPLY =====
            player.setDeltaMovement(motion);

        } else {
            if (!player.isCreative() && !player.isSpectator()) player.setNoGravity(false);
        }
    }
}