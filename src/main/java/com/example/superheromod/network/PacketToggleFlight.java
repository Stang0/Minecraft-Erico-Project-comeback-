package com.example.superheromod.network;

import com.example.superheromod.logic.FlightHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFlight {

    public final boolean enabled;

    public PacketToggleFlight(boolean enabled) {
        this.enabled = enabled;
    }

    // ---------- SERIALIZE ----------
    public static void encode(PacketToggleFlight msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
    }

    // ---------- DESERIALIZE ----------
    public static PacketToggleFlight decode(FriendlyByteBuf buf) {
        return new PacketToggleFlight(buf.readBoolean());
    }

    // ---------- HANDLE (SERVER) ----------
    public static void handle(PacketToggleFlight msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            FlightHandler.setFlying(player, msg.enabled);
        });
        ctx.get().setPacketHandled(true);
    }
}
