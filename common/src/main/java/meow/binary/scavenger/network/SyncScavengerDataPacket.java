package meow.binary.scavenger.network;

import it.hurts.shatterbyte.shatterlib.module.network.Packet;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class SyncScavengerDataPacket extends Packet {
    public static Type<SyncScavengerDataPacket> TYPE = Packet.createType(Scavenger.MOD_ID, "test_screen");
    public static StreamCodec<RegistryFriendlyByteBuf, SyncScavengerDataPacket> STREAM_CODEC = Packet.createCodec(SyncScavengerDataPacket::write, SyncScavengerDataPacket::new);

    public Item getItem() {
        return item;
    }
    public Identifier getModifier() {
        return modifier;
    }
    public long getWinTimestamp() {
        return winTimestamp;
    }

    Item item;
    Identifier modifier;
    long winTimestamp;
    public boolean isWin;

    public SyncScavengerDataPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        this.item = BuiltInRegistries.ITEM.getValue(buf.readIdentifier());
        this.modifier = buf.readIdentifier();
        this.winTimestamp = buf.readLong();
        this.isWin = buf.readBoolean();
    }

    public SyncScavengerDataPacket(Item item, Identifier modifier, long winTimestamp, boolean isWin) {
        this.item = item;
        this.modifier = modifier;
        this.winTimestamp = winTimestamp;
        this.isWin = isWin;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(item.arch$registryName());
        buf.writeIdentifier(modifier);
        buf.writeLong(winTimestamp);
        buf.writeBoolean(isWin);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
