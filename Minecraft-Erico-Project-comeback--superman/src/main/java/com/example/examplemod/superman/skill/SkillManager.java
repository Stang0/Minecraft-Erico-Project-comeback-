package com.example.examplemod.superman.skill;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the skill wheel selection and skill states
 * Skills: SPEED, FLIGHT, LASER
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class SkillManager {

    /**
     * Available skills in the wheel
     */
    public enum Skill {
        SPEED("", "⚡"),
        FLIGHT("", "🦅"),
        LASER("", "💥");

        private final String displayName;
        private final String icon;

        Skill(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }
    }

    // Currently selected skill per player
    private static final Map<UUID, Skill> selectedSkills = new HashMap<>();

    // Skill cooldowns per player (in ticks)
    private static final Map<UUID, Map<Skill, Integer>> skillCooldowns = new HashMap<>();

    // Cooldown durations in ticks
    public static final int SPEED_COOLDOWN = 100; // 5 seconds
    public static final int FLIGHT_COOLDOWN = 0; // No cooldown for flight
    public static final int LASER_COOLDOWN = 100; // 5 seconds

    /**
     * Get currently selected skill for a player
     */
    public static Skill getSelectedSkill(Player player) {
        return selectedSkills.getOrDefault(player.getUUID(), Skill.SPEED);
    }

    /**
     * Set selected skill for a player
     */
    public static void setSelectedSkill(Player player, Skill skill) {
        selectedSkills.put(player.getUUID(), skill);
    }

    /**
     * Cycle to next skill (scroll down)
     */
    public static void nextSkill(Player player) {
        Skill current = getSelectedSkill(player);
        Skill[] skills = Skill.values();
        int nextIndex = (current.ordinal() + 1) % skills.length;
        setSelectedSkill(player, skills[nextIndex]);
    }

    /**
     * Cycle to previous skill (scroll up)
     */
    public static void previousSkill(Player player) {
        Skill current = getSelectedSkill(player);
        Skill[] skills = Skill.values();
        int prevIndex = (current.ordinal() - 1 + skills.length) % skills.length;
        setSelectedSkill(player, skills[prevIndex]);
    }

    /**
     * Get cooldown for a specific skill (in ticks)
     */
    public static int getCooldown(Player player, Skill skill) {
        Map<Skill, Integer> playerCooldowns = skillCooldowns.get(player.getUUID());
        if (playerCooldowns == null)
            return 0;
        return playerCooldowns.getOrDefault(skill, 0);
    }

    /**
     * Check if skill is ready (no cooldown)
     */
    public static boolean isSkillReady(Player player, Skill skill) {
        return getCooldown(player, skill) <= 0;
    }

    /**
     * Get cooldown percent for a skill (0 = ready, 1 = full cooldown)
     */
    public static float getCooldownPercent(Player player, Skill skill) {
        int cooldown = getCooldown(player, skill);
        if (cooldown <= 0)
            return 0f;

        int maxCooldown = getMaxCooldown(skill);
        return (float) cooldown / maxCooldown;
    }

    /**
     * Get max cooldown for a skill type
     */
    public static int getMaxCooldown(Skill skill) {
        return switch (skill) {
            case SPEED -> SPEED_COOLDOWN;
            case FLIGHT -> FLIGHT_COOLDOWN;
            case LASER -> LASER_COOLDOWN;
        };
    }

    /**
     * Start cooldown for a skill
     */
    public static void startCooldown(Player player, Skill skill) {
        skillCooldowns.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
                .put(skill, getMaxCooldown(skill));
    }

    /**
     * Tick cooldowns for a player
     */
    public static void tickCooldowns(Player player) {
        Map<Skill, Integer> playerCooldowns = skillCooldowns.get(player.getUUID());
        if (playerCooldowns == null)
            return;

        for (Skill skill : Skill.values()) {
            int current = playerCooldowns.getOrDefault(skill, 0);
            if (current > 0) {
                playerCooldowns.put(skill, current - 1);
            }
        }
    }

    /**
     * Handle mouse scroll to change selected skill
     */
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        // Only process scroll when in game (not in menu)
        if (mc.screen != null)
            return;

        double delta = event.getScrollDelta();

        if (delta > 0) {
            // Scroll up - previous skill
            previousSkill(mc.player);
            event.setCanceled(true);
        } else if (delta < 0) {
            // Scroll down - next skill
            nextSkill(mc.player);
            event.setCanceled(true);
        }
    }
}
