package com.example.examplemod.superman;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles additional Superman abilities
 * - No fall damage while flying
 * - Flight HUD indicator
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class SupermanAbilityHandler {

    /**
     * Cancel fall damage when in flight mode
     */
    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (SupermanFlightHandler.isFlying(player)) {
                // Cancel fall damage
                event.setCanceled(true);
            }
        }
    }

    /**
     * Reset flight state when player logs out
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SupermanFlightHandler.setFlying(event.getEntity(), false);
    }

    /**
     * Display flight status message (client-side)
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        private static boolean wasFlying = false;

        /**
         * Show action bar message when flight state changes
         */
        public static void checkFlightStatus() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return;

            boolean isFlying = SupermanFlightHandler.isFlying(mc.player);

            if (isFlying != wasFlying) {
                wasFlying = isFlying;

                if (isFlying) {
                    mc.player.displayClientMessage(
                            Component.literal("§b§l✦ Superman Flight ACTIVATED ✦"),
                            true);
                } else {
                    mc.player.displayClientMessage(
                            Component.literal("§7✦ Flight Deactivated ✦"),
                            true);
                }
            }
        }
    }
}
