package com.gildedgames.aether.core.network.packet.client;

import com.gildedgames.aether.core.network.AetherPacket.AbstractAetherPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MoaInteractPacket extends AbstractAetherPacket {
    private final int playerID;
    private final boolean mainHand;

    public MoaInteractPacket(int playerID, boolean mainHand) {
        this.playerID = playerID;
        this.mainHand = mainHand;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playerID);
        buf.writeBoolean(this.mainHand);
    }

    public static MoaInteractPacket decode(FriendlyByteBuf buf) {
        int playerID = buf.readInt();
        boolean rightHand = buf.readBoolean();
        return new MoaInteractPacket(playerID, rightHand);
    }

    @Override
    public void execute(Player player) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().player.level.getEntity(this.playerID);
            if (entity instanceof Player playerEntity) {
                playerEntity.swing(this.mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
    }
}
