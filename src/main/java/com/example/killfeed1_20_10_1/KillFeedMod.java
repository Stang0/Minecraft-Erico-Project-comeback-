package com.example.killfeed1_20_10_1;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class KillFeedMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // ดักจับคนตาย
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                String skullOwnerName = victim.getName().getString();
                String message = damageSource.getDeathMessage(victim).getString();

                if (victim.getPrimeAdversary() instanceof ServerPlayerEntity killer) {
//                    skullOwnerName = killer.getName().getString();
                    message = "§e" + killer.getName().getString() + " §7eliminated §c" + victim.getName().getString();
                }

                // เตรียมข้อมูลส่ง (ใช้ PacketByteBuf สำหรับ 1.20.1)
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(message);
                buf.writeString(skullOwnerName);

                // ส่งให้ทุกคนในเซิร์ฟเวอร์
                var server = victim.getServer(); // 1.20.1 เรียกตรงนี้ได้เลย
                if (server != null) {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ServerPlayNetworking.send(player, PacketHandler.KILLFEED_PACKET_ID, buf);
                    }
                }
            }
        });
    }
}