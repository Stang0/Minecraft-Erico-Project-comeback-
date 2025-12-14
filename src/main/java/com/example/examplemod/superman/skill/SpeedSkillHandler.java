package com.example.examplemod.superman.skill;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.superman.SupermanFlightHandler;
import com.example.examplemod.superman.skill.SkillManager.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Speed Skill Handler
 * When Speed skill is selected, player runs faster using direct movement
 * modification
 * (No potion effects - custom speed boost)
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class SpeedSkillHandler {

    // Speed multiplier (how much faster the player moves)
    // 1.0 = normal speed, 2.0 = double speed, 3.0 = triple speed
    private static final double SPEED_MULTIPLIER = 2.5;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        Player player = mc.player;
        Skill selectedSkill = SkillManager.getSelectedSkill(player);

        // Apply speed boost when SPEED skill is selected
        if (selectedSkill == Skill.SPEED) {
            applySpeedBoost(player);
        }
    }

    /**
     * Apply custom speed boost by modifying player movement directly
     */
    private static void applySpeedBoost(Player player) {
        // Don't apply speed boost while flying
        if (SupermanFlightHandler.isFlying(player))
            return;

        // Only apply when player is moving on ground
        if (!player.onGround())
            return;

        Minecraft mc = Minecraft.getInstance();

        // Check if player is actively moving (pressing movement keys)
        boolean isMoving = mc.options.keyUp.isDown() ||
                mc.options.keyDown.isDown() ||
                mc.options.keyLeft.isDown() ||
                mc.options.keyRight.isDown();

        if (!isMoving)
            return;

        // Get current movement
        Vec3 motion = player.getDeltaMovement();

        // Only boost horizontal movement (not vertical)
        double currentHorizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        // Apply boost if moving significantly
        if (currentHorizontalSpeed > 0.01) {
            // Calculate boosted movement
            double boostFactor = SPEED_MULTIPLIER;

            // Apply the speed boost to horizontal movement
            player.setDeltaMovement(
                    motion.x * boostFactor,
                    motion.y, // Keep vertical movement unchanged
                    motion.z * boostFactor);
        }
    }

    /**
     * Get the current speed multiplier
     */
    public static double getSpeedMultiplier() {
        return SPEED_MULTIPLIER;
    }
}
