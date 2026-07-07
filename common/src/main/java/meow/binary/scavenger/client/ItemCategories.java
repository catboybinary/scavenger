package meow.binary.scavenger.client;

import meow.binary.scavenger.Scavenger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemCategories {
    private static final String CATEGORY_PATH_PREFIX = "category/";
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, CATEGORY_PATH_PREFIX + "food"));
    public static final TagKey<Item> MUSIC_DISCS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, CATEGORY_PATH_PREFIX + "music_discs"));

    public static Set<TagKey<Item>> getCategories(Item item) {
        if (item == null || item == Items.AIR) return Set.of();
        Set<TagKey<Item>> categories = item.getDefaultInstance().getTags()
                .filter(ItemCategories::isCategoryTag)
                .collect(Collectors.toCollection(HashSet::new));
        if (item.components().has(DataComponents.FOOD)) categories.add(FOOD);
        if (item.components().has(DataComponents.JUKEBOX_PLAYABLE)) categories.add(MUSIC_DISCS);
        return categories;
    }

    private static boolean isCategoryTag(TagKey<Item> tag) {
        return tag.location().getNamespace().equals(Scavenger.MOD_ID)
                && tag.location().getPath().startsWith(CATEGORY_PATH_PREFIX);
    }
}
