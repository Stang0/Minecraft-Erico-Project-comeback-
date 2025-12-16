package com.example.themodforerico.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import com.example.themodforerico.client.killfeed.QuestToast;

import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
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
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        // สั่งให้ทำงานใน Main Thread (เพื่อความปลอดภัยของการวาดหน้าจอ)
        context.enqueueWork(() -> {
            // เรียกใช้ QuestToast ที่เราสร้างไว้
            // ต้องแปลง String (name) เป็น Component ก่อน


            //
            Minecraft.getInstance().player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });

        context.setPacketHandled(true);
    }
}