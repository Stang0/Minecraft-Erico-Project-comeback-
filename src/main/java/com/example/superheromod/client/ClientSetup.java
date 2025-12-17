package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.client.model.SupermanPlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static String injectionError = null;

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer == null) continue;

            boolean slim = skin.equals("slim");
            SupermanPlayerModel<?> supermanModel = new SupermanPlayerModel<>(
                    event.getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER),
                    slim
            );

            try {
                // พยายามฉีดโมเดล
                ObfuscationReflectionHelper.setPrivateValue(
                        LivingEntityRenderer.class,
                        renderer,
                        supermanModel,
                        "f_115290_" // Field สำหรับ 1.20.1
                );
            } catch (Exception e) {
                // ⭐ เปลี่ยนจาก LOGGER.error เป็นการเก็บค่าไว้แจ้งเตือนในเกม
                injectionError = "Failed to inject Superman Model! (Skin: " + skin + ") - " + e.getMessage();
                SupermanMod.LOGGER.error(injectionError);
                debugInGame(injectionError,false);

            }
        }
    }

    public static void debugInGame(String message, boolean isError) {
        if (Minecraft.getInstance().player != null) {
            Component text = Component.literal(message)
                    .withStyle(isError ? ChatFormatting.RED : ChatFormatting.GREEN);

            // ส่งเข้า Chat ของผู้เล่น (แบบไม่ประกาศให้คนอื่นเห็น)
            Minecraft.getInstance().player.displayClientMessage(text, false);
        }
    }
}