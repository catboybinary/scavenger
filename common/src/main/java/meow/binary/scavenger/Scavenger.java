package meow.binary.scavenger;

import it.hurts.shatterbyte.shatterlib.module.config.ConfigManager;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import it.hurts.shatterbyte.shatterlib.platform.ShatterLibServices;
import meow.binary.scavenger.client.Config;
import meow.binary.scavenger.mixin.ServerLevelAccessor;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

import java.nio.file.Path;

public final class Scavenger {
    public static final Config CONFIG = new Config();
    public static final TemporaryData TEMP_DATA = new TemporaryData();
    public static final String MOD_ID = "scavenger";

    public static final Player.BedSleepingProblem INSOMNIA_PROBLEM = new Player.BedSleepingProblem(Component.translatable("scavenger.insomnia"));

    public static final TagKey<Item> VEGETARIAN_FOOD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "vegetarian_food"));
    public static final TagKey<Item> UNROLLABLE_BY_DEFAULT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "unrollable_by_default"));

    public static void init() {
        ConfigManager.register(MOD_ID, CONFIG);
        CONFIG.load(ShatterLibServices.platform().getConfigDirectory());
        ShatterLibNetwork.registerS2CPayloadType(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC);
    }

    public static void onPlayerTick(ServerPlayer serverPlayer) {
        ServerLevel overworld = ((ServerLevelAccessor) serverPlayer.level()).scavenger$getServer().overworld();
        ScavengerSavedData data = ScavengerSavedData.get(overworld);
        if (data.isEmpty()) {
            return;
        }

        ScavengerModifier modifier = data.getModifier();

        if (!data.hasWon()) {
            checkWinCondition(serverPlayer, data);
        }

        if (modifier.hasPlayerTick()) {
            modifier.playerTick(serverPlayer);
        }
    }

    public static void onPlayerJoin(ServerPlayer serverPlayer) {
        ServerLevel overworld = ((ServerLevelAccessor) serverPlayer.level()).scavenger$getServer().overworld();
        ScavengerSavedData data = ScavengerSavedData.get(overworld);
        SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId(), data.getWinTimestamp(), false);
        ShatterLibNetwork.sendToPlayer(serverPlayer, packet);
    }

    public static void onServerLevelLoad(ServerLevel level) {
        ScavengerSavedData data = ScavengerSavedData.get(((ServerLevelAccessor) level).scavenger$getServer().overworld());
        if (data.isEmpty()) {
            return;
        }

        ScavengerModifier modifier = data.getModifier();

        if (modifier.hasWorldStart()) {
            modifier.onWorldStart(level);
        }
    }

    private static void checkWinCondition(ServerPlayer player, ScavengerSavedData data) {
        int itemCount = getItemCount(data.getModifierId());

        boolean hasItem = player.getInventory().countItem(data.getItem()) >= itemCount;
        boolean hasWon = hasItem && (!data.getModifierId().equals(Modifiers.FINALIST.getId()) || hasKilledDragon(player));
        if (hasWon) {
            data.win(player.level().getGameTime());
            SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId(), data.getWinTimestamp(), true);
            ShatterLibNetwork.sendToPlayer(player, packet);
        }
    }

    private static boolean hasKilledDragon(ServerPlayer player) {
        MinecraftServer server = ((ServerLevelAccessor) player.level()).scavenger$getServer();
        ServerLevel end = server.getLevel(Level.END);
        EnderDragonFight fight = null;

        if (end != null) {
            fight = end.getDragonFight();
        }

        if (fight == null) {
            return false;
        }

        return fight.hasPreviouslyKilledDragon();
    }

    public static int getItemCount(Identifier modifier) {
        int itemCount = 1;
        if (modifier.equals(Modifiers.TWICE.getId())) itemCount = 2;
        else if (modifier.equals(Modifiers.THRICE.getId())) itemCount = 3;

        return itemCount;
    }

    public static boolean isSlotBlocked(int index, Level level) {
        if (Modifiers.isActive(Modifiers.HOLEY_POCKETS, level) && index > 8 && index < 36) {
            return true;
        }

        return false;
    }

    public static void saveConfig() {
        CONFIG.save(ShatterLibServices.platform().getConfigDirectory());
    }
}
