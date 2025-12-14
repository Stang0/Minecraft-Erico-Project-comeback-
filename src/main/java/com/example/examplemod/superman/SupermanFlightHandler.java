package com.example.examplemod.superman;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Superman Flight Handler
 * Double-tap jump to toggle flight mode
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class SupermanFlightHandler {

    private static final Map<UUID, Boolean> flyingPlayers = new HashMap<>();
    private static long lastJumpTime = 0;
    private static final long DOUBLE_TAP_THRESHOLD = 300; // ms

    private static final float FLIGHT_SPEED = 0.08f;
    private static final float VERTICAL_SPEED = 0.05f;
    private static final float MAX_FLIGHT_SPEED = 1.5f;

    public static boolean isFlying(Player player) {
        return flyingPlayers.getOrDefault(player.getUUID(), false);
    }

    public static void setFlying(Player player, boolean flying) {
        flyingPlayers.put(player.getUUID(), flying);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null)
            return;

        if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
            long now = System.currentTimeMillis();
            if (now - lastJumpTime < DOUBLE_TAP_THRESHOLD) {
                boolean wasFlying = isFlying(mc.player);
                setFlying(mc.player, !wasFlying);
                if (!wasFlying) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, 0.5, 0));
                }
            }
            lastJumpTime = now;
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        Player player = mc.player;
        if (isFlying(player)) {
            handleFlightMovement(player, mc);
            player.fallDistance = 0;
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < -0.1) {
                player.setDeltaMovement(motion.x, -0.1, motion.z);
            }
        }
    }

    private static void handleFlightMovement(Player player, Minecraft mc) {
        Vec3 currentMotion = player.getDeltaMovement();
        Vec3 lookVec = player.getLookAngle();

        double motionX = currentMotion.x;
        double motionY = currentMotion.y;
        double motionZ = currentMotion.z;

        boolean forward = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean up = mc.options.keyJump.isDown();
        boolean down = mc.options.keyShift.isDown();

        if (forward) {
            motionX += lookVec.x * FLIGHT_SPEED;
            motionY += lookVec.y * FLIGHT_SPEED;
            motionZ += lookVec.z * FLIGHT_SPEED;
        }
        if (backward) {
            motionX -= lookVec.x * FLIGHT_SPEED * 0.5;
            motionY -= lookVec.y * FLIGHT_SPEED * 0.5;
            motionZ -= lookVec.z * FLIGHT_SPEED * 0.5;
        }

        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        if (right) {
            motionX += rightVec.x * FLIGHT_SPEED * 0.7;
            motionZ += rightVec.z * FLIGHT_SPEED * 0.7;
        }
        if (left) {
            motionX -= rightVec.x * FLIGHT_SPEED * 0.7;
            motionZ -= rightVec.z * FLIGHT_SPEED * 0.7;
        }

        if (up)
            motionY += VERTICAL_SPEED;
        if (down)
            motionY -= VERTICAL_SPEED;

        motionX *= 0.91;
        motionY *= 0.91;
        motionZ *= 0.91;

        double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (speed > MAX_FLIGHT_SPEED) {
            double scale = MAX_FLIGHT_SPEED / speed;
            motionX *= scale;
            motionY *= scale;
            motionZ *= scale;
        }

        player.setDeltaMovement(motionX, motionY, motionZ);

        if (player.onGround() && (up || forward)) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.1, 0));
        }
        if (player.onGround() && down) {
            setFlying(player, false);
        }
    }
}
