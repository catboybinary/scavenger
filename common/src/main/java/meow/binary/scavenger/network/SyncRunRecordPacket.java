package meow.binary.scavenger.network;

import it.hurts.shatterbyte.shatterlib.module.network.Packet;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.RunRecord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class SyncRunRecordPacket extends Packet {
    public static Type<SyncRunRecordPacket> TYPE = Packet.createType(Scavenger.MOD_ID, "sync_run_record");

    public static StreamCodec<RegistryFriendlyByteBuf, SyncRunRecordPacket> STREAM_CODEC =
            Packet.createCodec(SyncRunRecordPacket::write, SyncRunRecordPacket::new);

    public final String levelId;
    public final Identifier itemId;
    public final Identifier modifierId;
    public final long winTimestamp;
    public final Optional<Long> seed;
    public final boolean multiplayer;

    public SyncRunRecordPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        this.levelId = buf.readUtf();
        this.itemId = buf.readIdentifier();
        this.modifierId = buf.readIdentifier();
        this.winTimestamp = buf.readLong();
        this.seed = buf.readBoolean() ? Optional.of(buf.readLong()) : Optional.empty();
        this.multiplayer = buf.readBoolean();
    }

    public SyncRunRecordPacket(String levelId, Identifier itemId, Identifier modifierId, long winTimestamp, Optional<Long> seed, boolean multiplayer) {
        this.levelId = levelId;
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.winTimestamp = winTimestamp;
        this.seed = seed;
        this.multiplayer = multiplayer;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(levelId);
        buf.writeIdentifier(itemId);
        buf.writeIdentifier(modifierId);
        buf.writeLong(winTimestamp);
        buf.writeBoolean(seed.isPresent());
        seed.ifPresent(buf::writeLong);
        buf.writeBoolean(multiplayer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
