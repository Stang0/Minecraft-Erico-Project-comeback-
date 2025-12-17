package com.example.examplemod.superman.geckolib;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Initializes GeckoLib integration for Superman flight
 */
public class SupermanGeckoLibInit {

    /**
     * Mod bus events (layer registration)
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            // Add layer to default player renderer
            PlayerRenderer defaultRenderer = event.getSkin("default");
            if (defaultRenderer != null) {
                defaultRenderer.addLayer(new SupermanAnimationLayer(defaultRenderer));
            }

            // Add layer to slim player renderer
            PlayerRenderer slimRenderer = event.getSkin("slim");
            if (slimRenderer != null) {
                slimRenderer.addLayer(new SupermanAnimationLayer(slimRenderer));
            }
        }
    }

    /**
     * Forge bus events (vanilla model hiding)
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onRenderPlayerPre(RenderLivingEvent.Pre<?, ?> event) {
            if (event.getEntity() instanceof AbstractClientPlayer player) {
                if (SupermanAnimationLayer.isVisualFlying(player)) {
                    if (event.getRenderer().getModel() instanceof PlayerModel<?> model) {
                        model.setAllVisible(false);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onRenderPlayerPost(RenderLivingEvent.Post<?, ?> event) {
            if (event.getEntity() instanceof AbstractClientPlayer player) {
                if (event.getRenderer().getModel() instanceof PlayerModel<?> model) {
                    model.setAllVisible(true);
                }
            }
        }
    }
}
