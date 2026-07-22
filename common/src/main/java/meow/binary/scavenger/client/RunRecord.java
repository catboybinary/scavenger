package meow.binary.scavenger.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

public record RunRecord(String levelId, Identifier itemId, Identifier modifierId, long winTimestamp,
                         Optional<Long> seed, String sig, boolean multiplayer) {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HMAC_KEY = "meow.binary.scavenger.run_history.v1";

    public static final Codec<RunRecord> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("levelId").forGetter(RunRecord::levelId),
                    Identifier.CODEC.fieldOf("itemId").forGetter(RunRecord::itemId),
                    Identifier.CODEC.fieldOf("modifierId").forGetter(RunRecord::modifierId),
                    Codec.LONG.fieldOf("winTimestamp").forGetter(RunRecord::winTimestamp),
                    Codec.LONG.optionalFieldOf("seed").forGetter(RunRecord::seed),
                    Codec.STRING.fieldOf("sig").forGetter(RunRecord::sig),
                    Codec.BOOL.optionalFieldOf("multiplayer", false).forGetter(RunRecord::multiplayer)
            ).apply(instance, RunRecord::new));

    public static RunRecord create(String levelId, Identifier itemId, Identifier modifierId, long winTimestamp, Optional<Long> seed) {
        return create(levelId, itemId, modifierId, winTimestamp, seed, false);
    }

    public static RunRecord create(String levelId, Identifier itemId, Identifier modifierId, long winTimestamp, Optional<Long> seed, boolean multiplayer) {
        return new RunRecord(levelId, itemId, modifierId, winTimestamp, seed, sign(levelId, itemId, modifierId, winTimestamp, seed, multiplayer), multiplayer);
    }

    public boolean isAuthentic() {
        String expected = sign(levelId, itemId, modifierId, winTimestamp, seed, multiplayer);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8));
    }

    public double totalSeconds() {
        return ScavengerTimeFormat.totalSeconds(winTimestamp, modifierId);
    }

    private static String sign(String levelId, Identifier itemId, Identifier modifierId, long winTimestamp, Optional<Long> seed, boolean multiplayer) {
        String payload = levelId + "\0" + itemId + "\0" + modifierId + "\0" + winTimestamp + "\0" + seed.map(String::valueOf).orElse("") + "\0" + multiplayer;

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(HMAC_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
