package com.example.examplemod.superman.skill;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.superman.SupermanFlightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Laser Skill Handler
 * Press G while flying to fire a laser beam towards where you're looking
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class LaserSkillHandler {

    // Skill configuration
    public static final int COOLDOWN_TICKS = 100; // 5 seconds
    public static final float DAMAGE = 10.0f;
    public static final double RANGE = 50.0;
    public static final int LASER_DURATION_TICKS = 20; // 1 second effect duration

    // Track cooldown per player
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();

    // Track active laser beams for rendering
    private static final Map<UUID, LaserBeam> activeBeams = new HashMap<>();

    /**
     * Represents an active laser beam
     */
    public static class LaserBeam {
        public final Vec3 start;
        public final Vec3 end;
        public int ticksRemaining;

        public LaserBeam(Vec3 start, Vec3 end, int duration) {
            this.start = start;
            this.end = end;
            this.ticksRemaining = duration;
        }
    }

    /**
     * Get remaining cooldown in ticks for a player
     */
    public static int getCooldownTicks(Player player) {
        return cooldowns.getOrDefault(player.getUUID(), 0);
    }

    /**
     * Get remaining cooldown as a percentage (0.0 = ready, 1.0 = full cooldown)
     */
    public static float getCooldownPercent(Player player) {
        int remaining = getCooldownTicks(player);
        if (remaining <= 0)
            return 0.0f;
        return (float) remaining / COOLDOWN_TICKS;
    }

    /**
     * Check if laser skill is ready to use
     */
    public static boolean isReady(Player player) {
        return getCooldownTicks(player) <= 0;
    }

    /**
     * Get active laser beam for a player (for rendering)
     */
    public static LaserBeam getActiveLaser(Player player) {
        return activeBeams.get(player.getUUID());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null)
            return;

        // Check for G key press
        if (event.getKey() == GLFW.GLFW_KEY_G && event.getAction() == GLFW.GLFW_PRESS) {
            Player player = mc.player;

            // Only fire while flying
            if (!SupermanFlightHandler.isFlying(player)) {
                return;
            }

            // Only fire if LASER skill is selected
            if (SkillManager.getSelectedSkill(player) != SkillManager.Skill.LASER) {
                return;
            }

            // Check cooldown
            if (!SkillManager.isSkillReady(player, SkillManager.Skill.LASER)) {
                return;
            }

            // Fire the laser!
            fireLaser(player);

            // Start cooldown via SkillManager
            SkillManager.startCooldown(player, SkillManager.Skill.LASER);
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

        // Update cooldowns
        UUID playerId = mc.player.getUUID();
        int cooldown = cooldowns.getOrDefault(playerId, 0);
        if (cooldown > 0) {
            cooldowns.put(playerId, cooldown - 1);
        }

        // Update active beams
        LaserBeam beam = activeBeams.get(playerId);
        if (beam != null) {
            beam.ticksRemaining--;
            if (beam.ticksRemaining <= 0) {
                activeBeams.remove(playerId);
            }
        }
    }

    /**
     * Fire the laser beam
     */
    private static void fireLaser(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        // Calculate ray from player's eyes in look direction
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(RANGE));

        // Perform block raycast
        BlockHitResult blockHit = mc.level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));

        Vec3 hitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : endPos;

        // Check for entity hits along the path
        AABB searchBox = new AABB(eyePos, hitPos).inflate(1.0);
        List<Entity> entities = mc.level.getEntities(player, searchBox, e -> e instanceof LivingEntity && e != player);

        Entity closestEntity = null;
        double closestDist = hitPos.distanceToSqr(eyePos);

        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Vec3 intersection = entityBox.clip(eyePos, hitPos).orElse(null);

            if (intersection != null) {
                double dist = intersection.distanceToSqr(eyePos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestEntity = entity;
                    hitPos = intersection;
                }
            }
        }

        // Deal damage to hit entity
        if (closestEntity instanceof LivingEntity livingTarget) {
            livingTarget.hurt(mc.level.damageSources().magic(), DAMAGE);
        }

        // Create laser beam visual
        LaserBeam beam = new LaserBeam(eyePos, hitPos, LASER_DURATION_TICKS);
        activeBeams.put(player.getUUID(), beam);

        // Start cooldown
        cooldowns.put(player.getUUID(), COOLDOWN_TICKS);

        // Spawn Effekseer particle effect
        spawnLaserEffect(eyePos, hitPos);
    }

    /**
     * Spawn the Effekseer laser effect using AAA Particles
     */
    private static void spawnLaserEffect(Vec3 start, Vec3 end) {
        try {
            // Use LaserEffectHandler to spawn the Effekseer effect
            LaserEffectHandler.spawnLaserEffect(start, end);
        } catch (NoClassDefFoundError e) {
            // AAA Particles mod not installed - just log
            System.out.println("Laser fired from " + start + " to " + end + " (AAA Particles not installed)");
        }
    }
}
