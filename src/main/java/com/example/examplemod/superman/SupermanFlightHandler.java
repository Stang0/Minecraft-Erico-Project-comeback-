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
 * Allows players to fly in survival mode by double-tapping jump
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class SupermanFlightHandler {

    // Track flying state for each player
    private static final Map<UUID, Boolean> flyingPlayers = new HashMap<>();

    // Track last jump time for double-tap detection
    private static long lastJumpTime = 0;
    private static final long DOUBLE_TAP_THRESHOLD = 300; // milliseconds

    // Flight speed settings
    private static final float FLIGHT_SPEED = 0.08f;
    private static final float VERTICAL_SPEED = 0.05f;
    private static final float MAX_FLIGHT_SPEED = 1.5f;

    // Track if jump key was pressed last tick
    private static boolean wasJumpPressed = false;

    /**
     * Check if a player is currently in superman flight mode
     */
    public static boolean isFlying(Player player) {
        return flyingPlayers.getOrDefault(player.getUUID(), false);
    }

    /**
     * Set the flying state for a player
     */
    public static void setFlying(Player player, boolean flying) {
        flyingPlayers.put(player.getUUID(), flying);
    }

    /**
     * Toggle flight mode for a player
     */
    public static void toggleFlight(Player player) {
        boolean currentlyFlying = isFlying(player);
        setFlying(player, !currentlyFlying);

        if (!currentlyFlying) {
            // Starting to fly - give a small upward boost
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
        }
    }

    /**
     * Handle keyboard input to detect double-tap jump
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null)
            return;

        // Check if jump key (SPACE) was pressed
        if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
            long currentTime = System.currentTimeMillis();

            // Check for double-tap
            if (currentTime - lastJumpTime < DOUBLE_TAP_THRESHOLD) {
                // Double-tap detected! Toggle flight
                toggleFlight(mc.player);
            }

            lastJumpTime = currentTime;
        }
    }

    /**
     * Handle flight movement each tick
     */
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
            // Handle flight physics
            handleFlightMovement(player, mc);

            // Prevent fall damage and falling
            player.fallDistance = 0;

            // Cancel default gravity
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < -0.1) {
                player.setDeltaMovement(motion.x, -0.1, motion.z);
            }
        }
    }

    /**
     * Handle the actual flight movement based on player input
     */
    private static void handleFlightMovement(Player player, Minecraft mc) {
        Vec3 currentMotion = player.getDeltaMovement();
        Vec3 lookVec = player.getLookAngle();

        double motionX = currentMotion.x;
        double motionY = currentMotion.y;
        double motionZ = currentMotion.z;

        // Check movement keys
        boolean forward = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean up = mc.options.keyJump.isDown();
        boolean down = mc.options.keyShift.isDown();

        // Apply forward/backward movement in look direction
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

        // Apply strafing (left/right movement)
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        if (right) {
            motionX += rightVec.x * FLIGHT_SPEED * 0.7;
            motionZ += rightVec.z * FLIGHT_SPEED * 0.7;
        }
        if (left) {
            motionX -= rightVec.x * FLIGHT_SPEED * 0.7;
            motionZ -= rightVec.z * FLIGHT_SPEED * 0.7;
        }

        // Apply vertical movement
        if (up) {
            motionY += VERTICAL_SPEED;
        }
        if (down) {
            motionY -= VERTICAL_SPEED;
        }

        // Apply air resistance / drag
        motionX *= 0.91;
        motionY *= 0.91;
        motionZ *= 0.91;

        // Clamp maximum speed
        double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (speed > MAX_FLIGHT_SPEED) {
            double scale = MAX_FLIGHT_SPEED / speed;
            motionX *= scale;
            motionY *= scale;
            motionZ *= scale;
        }

        // Apply the motion
        player.setDeltaMovement(motionX, motionY, motionZ);

        // Disable ground check while flying (prevents ground friction)
        if (player.onGround() && (up || forward)) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.1, 0));
        }

        // Stop flying if player touches ground and is sneaking
        if (player.onGround() && down) {
            setFlying(player, false);
        }
    }
}
