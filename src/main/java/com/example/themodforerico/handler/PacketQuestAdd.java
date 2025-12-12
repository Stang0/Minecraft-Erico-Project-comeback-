package com.example.themodforerico.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PacketQuestAdd {
    public final String name;
    public final String desc;
    public final BlockPos pos;
    public final boolean shouldTrack;

    public PacketQuestAdd(String name, String desc, BlockPos pos, boolean shouldTrack) {
        this.name = name;
        this.desc = desc;
        this.pos = pos;
        this.shouldTrack = shouldTrack;
    }

    public PacketQuestAdd(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
        this.desc = buf.readUtf();
        this.pos = buf.readBlockPos();
        this.shouldTrack = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeUtf(desc);
        buf.writeBlockPos(pos);
        buf.writeBoolean(shouldTrack);
    }
}