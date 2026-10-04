package nbtech2.fix.util;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import nbtech2.fix.config.Config;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class AEHelper {
    private static final String DRACONIC_EVOLUTION_NAMESPACE = "draconicevolution";
    private static final String[] EMPTY_NAMESPACES = new String[0];

    private AEHelper() {
    }

    public static boolean matchesWithConfiguredNbtPolicy(AEKey expected, AEKey available) {
        return matchesWithNbtPolicy(expected, available, Config.FIX_AE_CRAFT_NBT.get());
    }

    /**
     * NBT 策略判定。语义与 1.6.0 完全一致，仅把 {@code DE_ONLY} 的命名空间判断抽成
     * 「配置的命名空间集合」，并新增 {@code LIST} 模式。
     * <p>
     * 无论走哪个分支，第一步都是 {@code expected.equals(available)}：
     * 完全相同的 key 一律先判 true，且所有放宽分支都要求 {@code getItem()} 同一实例，
     * 因此不存在跨物品放行的路径。
     */
    public static boolean matchesWithNbtPolicy(AEKey expected, AEKey available, Config.AEIgnoreNbt policy) {
        if (expected.equals(available) || policy == Config.AEIgnoreNbt.DISABLED) {
            return expected.equals(available);
        }

        if (!(expected instanceof AEItemKey expectedItem) || !(available instanceof AEItemKey availableItem)) {
            return false;
        }
        if (expectedItem.getItem() != availableItem.getItem()) {
            return false;
        }
        if (policy == Config.AEIgnoreNbt.ALL) {
            return true;
        }

        String[] namespaces = configuredNamespaces(policy);
        if (namespaces.length == 0) {
            return false;
        }
        return matchesNamespace(BuiltInRegistries.ITEM.getKey(expectedItem.getItem()), namespaces);
    }

    /**
     * 当前策略是否启用（非 {@code DISABLED}）。
     */
    public static boolean isNbtPolicyActive() {
        return Config.FIX_AE_CRAFT_NBT.get() != Config.AEIgnoreNbt.DISABLED;
    }

    private static boolean matchesNamespace(ResourceLocation id, String[] namespaces) {
        if (id == null) {
            return false;
        }

        String namespace = id.getNamespace();
        for (String candidate : namespaces) {
            if (candidate.equals(namespace)) {
                return true;
            }
        }
        return false;
    }

    private static String[] configuredNamespaces(Config.AEIgnoreNbt policy) {
        if (policy == Config.AEIgnoreNbt.DE_ONLY) {
            // 1.6.0 的 DE_ONLY 语义原样保留
            return new String[] { DRACONIC_EVOLUTION_NAMESPACE };
        }
        if (policy == Config.AEIgnoreNbt.LIST) {
            return Config.FIX_AE_CRAFT_NBT_LIST.get().toArray(EMPTY_NAMESPACES);
        }
        return EMPTY_NAMESPACES;
    }

    /**
     * 候选是否与 {@code slot} 槽位声明的输入构成「同物品、仅 NBT 不同」的变体。
     * <p>
     * 该方法<b>不</b>调用 {@link IPatternDetails.IInput#isValid}，因此可以安全地
     * 从被注入的 {@code isValid} / {@code isItemValid} 内部调用而不会自递归。
     */
    public static boolean isNbtEquivalentInput(GenericStack declared, AEKey candidate) {
        if (!isNbtPolicyActive() || !(candidate instanceof AEItemKey candidateItem) || !candidateItem.hasTag()) {
            return false;
        }
        if (declared == null || !(declared.what() instanceof AEItemKey declaredItem)) {
            return false;
        }
        return !declaredItem.equals(candidateItem)
                && matchesWithConfiguredNbtPolicy(declaredItem, candidateItem);
    }

    /**
     * 遍历输入声明的全部候选，判断 {@code candidate} 是否为其中任一条的 NBT 变体。
     * 同样不调用 {@code isValid}。
     */
    public static boolean isNbtEquivalentInput(IPatternDetails.IInput input, AEKey candidate) {
        if (!isNbtPolicyActive() || !(candidate instanceof AEItemKey candidateItem) || !candidateItem.hasTag()) {
            return false;
        }

        for (GenericStack possible : input.getPossibleInputs()) {
            if (isNbtEquivalentInput(possible, candidateItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统一的「合法输入」判定：先跑原生逻辑，仅当原生为 false 时按配置策略做同物品 NBT 回退。
     * <p>
     * <b>绝不</b>把原生 true 改成 false，也绝不放宽到不同 {@code Item} 上。
     */
    public static boolean isValidInput(IPatternDetails.IInput input, AEKey candidate, Level level) {
        if (input.isValid(candidate, level)) {
            return true;
        }
        return isNbtEquivalentInput(input, candidate);
    }

    /**
     * 配方重跑的最小抽象：把「样板声明的精确 stack」放回格子后重新匹配并产出。
     * 只用于锻造台 / 切石机这类 {@code assemble} 完全依赖原版配方判定的样板。
     */
    @FunctionalInterface
    public interface RecipeGate {
        ItemStack rematch(Container container, RegistryAccess registryAccess);
    }

    /**
     * 用样板自己编码的 stack 重跑原版配方。
     * <p>
     * 产物 NBT 完全由原版配方决定，本方法不构造任何产物；失败时返回 {@link ItemStack#EMPTY}
     * 让调用方保持 AE2 的原始行为。
     *
     * @param gridSlots 声明的 stack 依次放入容器的哪些槽位（索引与 {@code declared} 一一对应）
     */
    public static ItemStack rematchWithDeclaredStacks(
            RecipeGate gate,
            int containerSize,
            int[] gridSlots,
            GenericStack[] declared,
            Level level
    ) {
        if (gate == null || gridSlots == null || declared == null || declared.length < gridSlots.length) {
            return ItemStack.EMPTY;
        }

        Container container = new SimpleContainer(containerSize);
        for (int i = 0; i < gridSlots.length; i++) {
            GenericStack stack = declared[i];
            if (stack == null || !(stack.what() instanceof AEItemKey itemKey)) {
                return ItemStack.EMPTY;
            }
            container.setItem(gridSlots[i], itemKey.toStack(1));
        }

        return gate.rematch(container, level.registryAccess());
    }

    /**
     * 判断装配室网格里是否存在「与样板声明输入仅 NBT 不同」的实际物品。
     * 用于把配方重跑限定在真正的变体场景，避免影响其它失败原因。
     */
    public static boolean hasNbtVariantInput(GenericStack[] declared, int[] gridSlots, Container container) {
        if (declared == null || gridSlots == null || container == null || !isNbtPolicyActive()) {
            return false;
        }

        for (int i = 0; i < gridSlots.length; i++) {
            if (i >= declared.length) {
                break;
            }
            AEItemKey actual = AEItemKey.of(container.getItem(gridSlots[i]));
            if (actual != null && isNbtEquivalentInput(declared[i], actual)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDraconicEvolutionItem(AEKey key) {
        if (!(key instanceof AEItemKey itemKey)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemKey.getItem());
        return id != null && DRACONIC_EVOLUTION_NAMESPACE.equals(id.getNamespace());
    }

    public static boolean matchesFinalOutput(AEKey actual, GenericStack expected) {
        return expected != null
                && (actual.matches(expected) || matchesWithConfiguredNbtPolicy(expected.what(), actual));
    }

    public static long extractMatching(
            ListCraftingInventory inventory,
            AEKey actual,
            long amount,
            Actionable action
    ) {
        if (Config.FIX_AE_CRAFT_NBT.get() == Config.AEIgnoreNbt.DISABLED) {
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

    public static List<KeyAmount> matchingAmounts(KeyCounter candidates, AEKey reference, long maximum) {
        return matchingAmounts(candidates, reference, maximum, Config.FIX_AE_CRAFT_NBT.get());
    }

    public static void removeMatching(KeyCounter candidates, AEKey reference, long maximum) {
        for (KeyAmount match : matchingAmounts(candidates, reference, maximum)) {
            candidates.remove(match.key(), match.amount());
        }
        candidates.removeZeros();
    }

    private static List<KeyAmount> matchingAmounts(
            KeyCounter candidates,
            AEKey reference,
            long maximum,
            Config.AEIgnoreNbt policy
    ) {
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

        if (remaining > 0 && policy != Config.AEIgnoreNbt.DISABLED) {
            for (Object2LongMap.Entry<AEKey> entry : candidates) {
                AEKey candidate = entry.getKey();
                if (candidate.equals(reference)
                        || entry.getLongValue() <= 0
                        || !matchesWithNbtPolicy(reference, candidate, policy)) {
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

    public record KeyAmount(AEKey key, long amount) {
    }
}
