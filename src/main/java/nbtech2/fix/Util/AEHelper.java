package nbtech2.fix.Util;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import nbtech2.fix.Config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AEHelper {
    private static final String DRACONIC_EVOLUTION_NAMESPACE = "draconicevolution";

    private AEHelper() {
    }

    public static boolean isIgnoreNBT(AEKey expected, AEKey available, Config.AEIgnoreNBT policy) {
        if (expected.equals(available) || policy == Config.AEIgnoreNBT.DISABLED) {
            return expected.equals(available);
        }
        if (!(expected instanceof AEItemKey expectedItem) || !(available instanceof AEItemKey availableItem)) {
            return false;
        }
        if (expectedItem.getItem() != availableItem.getItem()) {
            return false;
        }
        if (policy == Config.AEIgnoreNBT.ALL) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(expectedItem.getItem());
        return policy == Config.AEIgnoreNBT.DE_ONLY
                && id != null
                && DRACONIC_EVOLUTION_NAMESPACE.equals(id.getNamespace());
    }

    public static boolean isDEItem(AEKey key) {
        if (!(key instanceof AEItemKey itemKey)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemKey.getItem());
        return id != null && DRACONIC_EVOLUTION_NAMESPACE.equals(id.getNamespace());
    }

    public static boolean matchesFinalOutput(AEKey actual, GenericStack expected) {
        return expected != null
                && (actual.matches(expected) || isIgnoreNBT(expected.what(), actual, Config.Fix_AE_Craft_NBT.get()));
    }

    public static long extractMatching(
            ListCraftingInventory inventory,
            AEKey actual,
            long amount,
            Actionable action
    ) {
        if (Config.Fix_AE_Craft_NBT.get() == Config.AEIgnoreNBT.DISABLED) {
            return inventory.extract(actual, amount, action);
        }
        List<KeyAmount> matches = matchingAmounts(inventory.list, actual, amount);
        long extracted = matches.stream().mapToLong(KeyAmount::amount).sum();
        if (action == Actionable.MODULATE) {
            for (KeyAmount match : matches) {
                inventory.extract(match.key(), match.amount(), Actionable.MODULATE);
            }
        }
        return extracted;
    }

    private static List<KeyAmount> matchingAmounts(KeyCounter candidates, AEKey reference, long maximum) {
        if (maximum <= 0) {
            return List.of();
        }
        List<KeyAmount> result = new ArrayList<>();
        long remaining = maximum;
        long exact = Math.min(candidates.get(reference), remaining);
        if (exact > 0) {
            result.add(new KeyAmount(reference, exact));
            remaining -= exact;
        }
        if (remaining > 0) {
            for (Object2LongMap.Entry<AEKey> entry : candidates) {
                AEKey candidate = entry.getKey();
                if (candidate.equals(reference)
                        || entry.getLongValue() <= 0
                        || !isIgnoreNBT(reference, candidate, Config.Fix_AE_Craft_NBT.get())) {
                    continue;
                }
                long taken = Math.min(entry.getLongValue(), remaining);
                result.add(new KeyAmount(candidate, taken));
                remaining -= taken;
                if (remaining == 0) {
                    break;
                }
            }
        }
        return List.copyOf(result);
    }

    public static List<ItemStack> getRealInputs(KeyCounter[] inputHolder, List<ItemStack> fallback) {
        if (inputHolder == null || inputHolder.length == 0 || !hasInputData(inputHolder)) {
            return fallback;
        }
        List<ItemStack> realInputs = new ArrayList<>(fallback.size());
        for (int slot = 0; slot < fallback.size(); slot++) {
            ItemStack realStack = firstItemStack(slot < inputHolder.length ? inputHolder[slot] : null);
            realInputs.add(realStack.isEmpty() ? fallback.get(slot).copy() : realStack);
        }
        return realInputs;
    }

    private static boolean hasInputData(KeyCounter[] inputHolder) {
        for (KeyCounter counter : inputHolder) {
            if (counter != null && !counter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack firstItemStack(KeyCounter counter) {
        if (counter == null || counter.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (Object2LongMap.Entry<AEKey> entry : counter) {
            if (entry.getKey() instanceof AEItemKey itemKey && entry.getLongValue() > 0) {
                return itemKey.toStack((int) Math.min(Integer.MAX_VALUE, entry.getLongValue()));
            }
        }
        return ItemStack.EMPTY;
    }

    private record KeyAmount(AEKey key, long amount) {
    }
}
