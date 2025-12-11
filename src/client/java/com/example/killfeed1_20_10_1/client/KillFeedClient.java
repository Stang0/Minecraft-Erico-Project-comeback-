package com.example.killfeed1_20_10_1.client;


import com.example.killfeed1_20_10_1.PacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class KillFeedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // รับข้อมูลฝั่ง Client
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.KILLFEED_PACKET_ID, (client, handler, buf, responseSender) -> {
            String message = buf.readString();
            String skullOwner = buf.readString();

            client.execute(() -> {
                // สร้าง Toast และใส่ลงคิว
                client.getToastManager().add(new KillFeedToast(message, skullOwner));
            });
        });
    }
}
