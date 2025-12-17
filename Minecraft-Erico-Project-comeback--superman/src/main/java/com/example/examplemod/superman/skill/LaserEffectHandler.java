package com.example.examplemod.superman.skill;

import com.example.examplemod.ExampleMod;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Handles spawning Effekseer laser effects using AAA Particles mod
 */
@OnlyIn(Dist.CLIENT)
public class LaserEffectHandler {

    // Path to the Effekseer effect file: assets/examplemod/effeks/laser.efkefc
    private static final ParticleEmitterInfo LASER_EFFECT = new ParticleEmitterInfo(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "laser"));

    /**
     * Spawn the laser effect from start position to end position
     * 
     * @param start Starting position (player's eye position)
     * @param end   Ending position (hit location)
     */
    public static void spawnLaserEffect(Vec3 start, Vec3 end) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        try {
            // Debug: Print the effect path being used
            System.out.println("[LaserEffect] Spawning effect: " + ExampleMod.MODID + ":laser");
            System.out.println("[LaserEffect] Effect position: " + end);

            // Calculate direction and rotation for the effect
            Vec3 direction = end.subtract(start);
            float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
            float pitch = (float) Math.toDegrees(-Math.atan2(direction.y,
                    Math.sqrt(direction.x * direction.x + direction.z * direction.z)));

            // Spawn effect at the impact point
            AAALevel.addParticle(
                    mc.level,
                    false, // not forced
                    LASER_EFFECT.clone()
                            .position(end.x, end.y, end.z)
                            .rotation(pitch, yaw, 0) // rotation in degrees
                            .scale(1.0f));

            System.out.println("[LaserEffect] Effect spawned successfully!");

        } catch (Exception e) {
            // AAA Particles might not be loaded or effect file missing
            System.err.println("[LaserEffect] Failed to spawn laser effect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Spawn a simple impact effect at a position
     */
    public static void spawnImpactEffect(Vec3 position) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        try {
            AAALevel.addParticle(
                    mc.level,
                    false,
                    LASER_EFFECT.clone()
                            .position(position.x, position.y, position.z)
                            .scale(1.5f));
        } catch (Exception e) {
            System.err.println("[LaserEffect] Failed to spawn impact effect: " + e.getMessage());
        }
    }
}
