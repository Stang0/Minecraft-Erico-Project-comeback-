package com.example.examplemod.superman;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles particle effects for Superman flight
 * Creates wind/speed effects when flying
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class SupermanParticleHandler {

    private static int particleTimer = 0;
    private static final int PARTICLE_INTERVAL = 2; // ticks between particles

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        LocalPlayer player = mc.player;

        if (SupermanFlightHandler.isFlying(player)) {
            particleTimer++;

            if (particleTimer >= PARTICLE_INTERVAL) {
                particleTimer = 0;
                // spawnFlightParticles(player);
            }
        } else {
            particleTimer = 0;
        }
    }

    /**
     * Spawn particles around the flying player
     */
    private static void spawnFlightParticles(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        RandomSource random = player.getRandom();
        Vec3 motion = player.getDeltaMovement();
        double speed = motion.length();

        // Only spawn particles if moving fast enough
        if (speed < 0.1)
            return;

        // Calculate particle spawn position behind the player
        double x = player.getX() - motion.x * 0.5;
        double y = player.getY() + 0.5;
        double z = player.getZ() - motion.z * 0.5;

        // Add some randomness
        double offsetX = (random.nextDouble() - 0.5) * 0.5;
        double offsetY = (random.nextDouble() - 0.5) * 0.5;
        double offsetZ = (random.nextDouble() - 0.5) * 0.5;

        // Spawn cloud/wind particles
        mc.level.addParticle(
                ParticleTypes.CLOUD,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                -motion.x * 0.1,
                -motion.y * 0.1,
                -motion.z * 0.1);

        // Spawn additional particles at higher speeds
        if (speed > 0.5) {
            // Spawn sweep attack particles for speed effect
            mc.level.addParticle(
                    ParticleTypes.SWEEP_ATTACK,
                    x + offsetX * 2,
                    y + offsetY,
                    z + offsetZ * 2,
                    0, 0, 0);
        }

        // At very high speeds, add more dramatic particles
        if (speed > 1.0) {
            mc.level.addParticle(
                    ParticleTypes.END_ROD,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    -motion.x * 0.2,
                    -motion.y * 0.2,
                    -motion.z * 0.2);
        }
    }
}
