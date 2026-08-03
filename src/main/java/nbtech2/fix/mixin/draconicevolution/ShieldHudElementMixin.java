package nbtech2.fix.mixin.draconicevolution;

import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.entities.ShieldControlEntity;
import com.brandon3055.draconicevolution.api.modules.entities.UndyingEntity;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.brandon3055.draconicevolution.items.tools.DraconiumCapacitor;
import nbtech2.fix.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.client.render.hud.ShieldHudElement", remap = false)
public abstract class ShieldHudElementMixin extends AbstractHudElement {
    @Shadow private Minecraft mc;
    @Shadow private double shieldCharge;
    @Shadow private String shieldText;
    @Shadow private double coolDown;
    @Shadow private double energyBar;
    @Shadow private String energyText;
    @Shadow private double[] totemStatus;
    @Shadow private int totemEffect;
    @Shadow private int lastTotemCount;
    @Shadow private int lastChargedTotemCount;
    @Shadow private boolean renderHud;
    @Shadow private boolean numericEnergy;
    @Shadow private boolean showUndying;
    @Shadow private int energyMode;

    protected ShieldHudElementMixin(Vector2 defaultRawPos) {
        super(defaultRawPos);
    }

    @Shadow
    private void setupExample() {
        throw new AssertionError();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void nbtech2fix$aggregateEquippedModuleHosts(boolean configuring, CallbackInfo callback) {
        if (!Config.FIX_DE_HUD.get()) {
            return;
        }

        if (mc.player == null || !enabled) {
            renderHud = false;
            if (configuring) {
                setupExample();
            }
            callback.cancel();
            return;
        }

        if (totemEffect > 0) {
            --totemEffect;
        }

        List<ItemStack> equipment = collectModularArmor();
        List<ModuleHost> hosts = collectHosts(equipment);
        ItemStack originalArmor = IModularArmor.getArmor(mc.player);
        IOPStorage opStorage = getStorage(originalArmor);
        if (opStorage == null) {
            for (ItemStack stack : equipment) {
                opStorage = getStorage(stack);
                if (opStorage != null) {
                    break;
                }
            }
        }

        if (hosts.isEmpty() || opStorage == null) {
            renderHud = false;
            if (configuring) {
                setupExample();
            }
            callback.cancel();
            return;
        }

        updateShield(originalArmor, hosts);
        updateEnergy(opStorage);
        updateUndying(hosts);
        renderHud = true;
        callback.cancel();
    }

    private List<ItemStack> collectModularArmor() {
        List<ItemStack> result = new ArrayList<>();
        Set<ItemStack> seen = java.util.Collections.newSetFromMap(new IdentityHashMap<>());

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof IModularArmor && seen.add(chest)) {
            result.add(chest);
        }
        for (ItemStack stack : EquipmentManager.findItems(
                candidate -> candidate.getItem() instanceof IModularArmor,
                mc.player
        )) {
            if (!stack.isEmpty() && seen.add(stack)) {
                result.add(stack);
            }
        }
        return result;
    }

    private static List<ModuleHost> collectHosts(List<ItemStack> equipment) {
        List<ModuleHost> result = new ArrayList<>();
        for (ItemStack stack : equipment) {
            stack.getCapability(DECapabilities.MODULE_HOST_CAPABILITY).ifPresent(result::add);
        }
        return result;
    }

    private void updateShield(ItemStack originalArmor, List<ModuleHost> hosts) {
        ShieldControlEntity shield = null;
        if (Config.FIX_DE_CURIOS_ORDER.get()) {
            for (ModuleHost host : hosts) {
                ShieldControlEntity candidate = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                        .map(entity -> (ShieldControlEntity) entity)
                        .findFirst()
                        .orElse(null);
                if (candidate != null && shield == null) {
                    shield = candidate;
                }
                if (candidate != null && candidate.isShieldEnabled()) {
                    shield = candidate;
                    break;
                }
            }
        } else if (!originalArmor.isEmpty()) {
            ModuleHost host = originalArmor.getCapability(DECapabilities.MODULE_HOST_CAPABILITY)
                    .resolve()
                    .orElse(null);
            if (host != null) {
                shield = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                        .map(entity -> (ShieldControlEntity) entity)
                        .findFirst()
                        .orElse(null);
            }
        }

        if (shield == null) {
            shieldCharge = 0.0D;
            coolDown = 0.0D;
            shieldText = I18n.get("hud_armor.draconicevolution.no_shield");
            return;
        }
        if (!shield.isShieldEnabled()) {
            shieldCharge = 0.0D;
            coolDown = 0.0D;
            shieldText = I18n.get("hud_armor.draconicevolution.shield_disabled");
            return;
        }

        double capacity = shield.getShieldCapacity() + shield.getMaxShieldBoost();
        if (capacity == 0.0D && shield.getMaxShieldBoost() > 0.0D) {
            capacity = shield.getMaxShieldBoost();
        }
        double points = shield.getShieldPoints();
        shieldCharge = capacity > 0.0D ? points / capacity : 0.0D;
        shieldText = (int) points + "/" + (int) capacity;
        double maxCooldown = shield.getMaxShieldCoolDown();
        coolDown = maxCooldown > 0.0D ? shield.getShieldCoolDown() / maxCooldown : 0.0D;
    }

    private void updateEnergy(IOPStorage armorStorage) {
        long energy = armorStorage.getOPStored();
        long maxEnergy = armorStorage.getMaxOPStored();

        if (energyMode > 0) {
            List<ItemStack> capacitors = new ArrayList<>(EquipmentManager.findItems(
                    candidate -> candidate.getItem() instanceof DraconiumCapacitor,
                    mc.player
            ));
            for (ItemStack stack : mc.player.getInventory().items) {
                if (stack.getItem() instanceof DraconiumCapacitor) {
                    capacitors.add(stack);
                }
            }

            long capacitorEnergy = 0L;
            long capacitorMax = 0L;
            for (ItemStack stack : capacitors) {
                LazyOptional<IOPStorage> optional = stack.getCapability(CapabilityOP.OP);
                if (optional.isPresent()) {
                    IOPStorage storage = optional.orElseThrow(IllegalStateException::new);
                    capacitorEnergy = Utils.safeAdd(capacitorEnergy, storage.getOPStored());
                    capacitorMax = Utils.safeAdd(capacitorMax, storage.getMaxOPStored());
                }
            }

            if (energyMode == 1) {
                energy = capacitorEnergy;
                maxEnergy = capacitorMax;
            } else {
                energy = Utils.safeAdd(energy, capacitorEnergy);
                maxEnergy = Utils.safeAdd(maxEnergy, capacitorMax);
            }
        }

        energyBar = maxEnergy > 0L ? (double) energy / (double) maxEnergy : 0.0D;
        energyText = numericEnergy
                ? I18n.get("op.brandonscore.op") + ": " + Utils.formatNumber(energy)
                : "";
    }

    private void updateUndying(List<ModuleHost> hosts) {
        if (!showUndying) {
            totemStatus = new double[0];
            return;
        }

        List<UndyingEntity> totems = new ArrayList<>();
        for (ModuleHost host : hosts) {
            host.getEntitiesByType(ModuleTypes.UNDYING)
                    .map(entity -> (UndyingEntity) entity)
                    .forEach(totems::add);
        }
        totems.sort(Comparator.comparing(entity -> entity.getModule().getModuleTechLevel().index));

        int charged = 0;
        totemStatus = new double[totems.size()];
        for (int i = 0; i < totems.size(); ++i) {
            UndyingEntity totem = totems.get(i);
            if (totem.isCharged()) {
                ++charged;
            }
            totemStatus[i] = totem.isCharged() ? -1.0D : totem.getCharge();
        }

        if (lastTotemCount == totems.size()
                && charged > lastChargedTotemCount
                && mc.level != null) {
            mc.level.playLocalSound(
                    mc.player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS,
                    1.0F,
                    2.0F,
                    false
            );
        }
        lastTotemCount = totems.size();
        lastChargedTotemCount = charged;
    }

    private static IOPStorage getStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getCapability(DECapabilities.OP_STORAGE).resolve().orElse(null);
    }
}
