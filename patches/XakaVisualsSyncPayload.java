package dev.xaka.visuals;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Small, validated cosmetic state packet shared by the client and server. */
public record XakaVisualsSyncPayload(
        UUID playerId,
        boolean petEnabled,
        int petType,
        int petSize,
        boolean wingsEnabled,
        int wingStyle,
        float wingSpeed,
        boolean dancePlaying,
        int activeDance,
        float danceSpeed,
        float danceStartAge,
        int accentColor
) implements CustomPayload {
    public static final Id<XakaVisualsSyncPayload> ID =
            new Id<>(Identifier.of("xakavisuals", "cosmetic_sync"));
    public static final PacketCodec<RegistryByteBuf, XakaVisualsSyncPayload> CODEC =
            PacketCodec.of(XakaVisualsSyncPayload::write, XakaVisualsSyncPayload::new);

    private XakaVisualsSyncPayload(RegistryByteBuf buf) {
        this(buf.readUuid(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readVarInt(), buf.readFloat(),
                buf.readBoolean(), buf.readVarInt(), buf.readFloat(), buf.readFloat(),
                buf.readInt(), buf.readBoolean(), buf.readVarInt());
    }

    private void write(RegistryByteBuf buf) {
        buf.writeUuid(playerId);
        buf.writeBoolean(petEnabled);
        buf.writeVarInt(petType);
        buf.writeVarInt(petSize);
        buf.writeBoolean(wingsEnabled);
        buf.writeVarInt(wingStyle);
        buf.writeFloat(wingSpeed);
        buf.writeBoolean(dancePlaying);
        buf.writeVarInt(activeDance);
        buf.writeFloat(danceSpeed);
        buf.writeFloat(danceStartAge);
        buf.writeInt(accentColor);\n        buf.writeBoolean(hatEnabled);\n        buf.writeVarInt(hatStyle);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
