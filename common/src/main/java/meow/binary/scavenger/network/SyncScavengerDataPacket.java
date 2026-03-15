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

    public Identifier getModifier() {
        return modifier;
    }

    public Item getItem() {
        return item;
    }

    Item item;
    Identifier modifier;

    public SyncScavengerDataPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        this.item = BuiltInRegistries.ITEM.getValue(buf.readIdentifier());
        this.modifier = buf.readIdentifier();
    }

    public SyncScavengerDataPacket(Item item, Identifier modifier) {
        this.item = item;
        this.modifier = modifier;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(item.arch$registryName());
        buf.writeIdentifier(modifier);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
