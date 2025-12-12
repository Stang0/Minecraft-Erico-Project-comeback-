package com.example.themodforerico.handler;

import com.example.themodforerico.client.ClientPacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("themodforerico:main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        // 1. PacketKillFeed
        INSTANCE.messageBuilder(PacketKillFeed.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketKillFeed::encode)
                .decoder(PacketKillFeed::new)
                .consumerMainThread((msg, ctx) -> {
                    // ⚠️ ใช้ DistExecutor ป้องกัน Server Crash
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleKillFeed(msg, ctx));
                })
                .add();

        // 2. PacketQuestAdd
        INSTANCE.messageBuilder(PacketQuestAdd.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketQuestAdd::encode)
                .decoder(PacketQuestAdd::new)
                .consumerMainThread((msg, ctx) -> {
                    // ⚠️ ใช้ DistExecutor ป้องกัน Server Crash
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleQuestAdd(msg, ctx));
                })
                .add();

        // 3. PacketQuestRemove
        INSTANCE.messageBuilder(PacketQuestRemove.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketQuestRemove::encode)
                .decoder(PacketQuestRemove::new)
                .consumerMainThread((msg, ctx) -> {
                    // ⚠️ ใช้ DistExecutor ป้องกัน Server Crash
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleQuestRemove(msg, ctx));
                })
                .add();
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAll(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}