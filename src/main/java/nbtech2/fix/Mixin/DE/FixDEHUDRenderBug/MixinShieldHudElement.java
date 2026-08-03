package nbtech2.fix.Mixin.DE.FixDEHUDRenderBug;

import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.draconicevolution.client.render.hud.ShieldHudElement;
import nbtech2.fix.Config.Config;
import nbtech2.fix.Util.DEHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Mixin(ShieldHudElement.class)
public abstract class MixinShieldHudElement extends AbstractHudElement {
    @Shadow private Minecraft mc;

    @Shadow private double shieldCharge;
    @Shadow private Component shieldText;
    @Shadow private double coolDown;

    @Shadow private double energyBar;
    @Shadow private Component energyText;

    @Shadow private double[] totemStatus;
    @Shadow private int lastTotemCount;
    @Shadow private int lastChargedTotemCount;

    @Shadow private boolean renderHud;
    @Shadow private boolean numericEnergy;
    @Shadow private boolean showUndying;
    @Shadow private int energyMode;
    @Shadow private int totemEffect;

    public MixinShieldHudElement(Vector2 defaultRawPos) {
        super(defaultRawPos);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void fixHud(boolean configuring, CallbackInfo ci) {

        if (!Config.Fix_DE_HUD_Render_Bug.getAsBoolean()) return;

        if (mc.player == null) return;

        if (!this.enabled) {this.renderHud = false;return;}
        this.renderHud = true;

        List<ItemStack> equipment = new ArrayList<>();

        equipment.add(DEHelper.getEffectiveArmor(mc.player));
        equipment.addAll(EquipmentManager.findItems(
                e -> e.getItem() instanceof IModularArmor,
                mc.player
        ));
        LinkedHashSet<ItemStack> temp = new LinkedHashSet<>(equipment);
        equipment = new ArrayList<>(temp);

        if (equipment.isEmpty() || (equipment.size()==1 && equipment.getFirst().equals(ItemStack.EMPTY))) {
            this.renderHud = false;
            return;
        }

        List<ModuleHost> hosts = new ArrayList<>();

        for (ItemStack stack : equipment) {
            ModuleHost host = DECapabilities.getHost(stack);
            if (host != null) {
                hosts.add(host);
            }
        }


        ShieldControlEntity shield = null;

        ItemStack chestStack;
        if (Config.Fix_DE_Curios_Order_Bug.getAsBoolean()) {
            chestStack = DEHelper.getEffectiveArmor(mc.player);
        } else {
            chestStack = IModularArmor.getArmor(mc.player);
        }
        ModuleHost host = DECapabilities.getHost(chestStack);
        if (host != null) {
            shield = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                    .map(e -> (ShieldControlEntity)e)
                    .findAny()
                    .orElse(null);
        }

        if (shield == null) {
            this.shieldCharge = 0;
            this.shieldText = Component.translatable("hud_armor.draconicevolution.no_shield");
        } else if (!shield.isShieldEnabled()) {
            this.shieldCharge = 0;
            this.shieldText = Component.translatable("hud_armor.draconicevolution.shield_disabled");
        } else {
            double capacity = shield.getShieldCapacity() + shield.getMaxShieldBoost();
            if (capacity == 0 && shield.getMaxShieldBoost() > 0) {
                capacity = shield.getMaxShieldBoost();
            }

            double points = shield.getShieldPoints();
            this.shieldCharge = capacity > 0 ? points / capacity : 0;
            this.shieldText = Component.literal((int)points + "/" + (int)capacity);

            double maxCooldown = shield.getMaxShieldCoolDown();
            this.coolDown = maxCooldown > 0 ? shield.getShieldCoolDown() / maxCooldown : 0;
        }

        if (this.totemEffect > 0) {
            this.totemEffect--;
        }
        if (this.showUndying) {
            List<UndyingEntity> allTotems = new ArrayList<>();

            for (ModuleHost host1 : hosts) {
                host1.getEntitiesByType(ModuleTypes.UNDYING)
                        .map(e -> (UndyingEntity)e)
                        .forEach(allTotems::add);
            }

            allTotems.sort(Comparator.comparing(e -> e.getModule().getModuleTechLevel().index));

            int charged = 0;
            this.totemStatus = new double[allTotems.size()];

            for (int i = 0; i < allTotems.size(); i++) {
                UndyingEntity e = allTotems.get(i);

                if (e.isCharged()) charged++;

                this.totemStatus[i] = e.isCharged() ? -1D : e.getCharge();
            }

            if (this.lastTotemCount == allTotems.size()) {
                if (charged > this.lastChargedTotemCount && mc.level != null) {
                    mc.level.playLocalSound(
                            mc.player.blockPosition(),
                            SoundEvents.PLAYER_LEVELUP,
                            SoundSource.PLAYERS,
                            1.0F,
                            2.0F,
                            false
                    );
                }
            }

            this.lastTotemCount = allTotems.size();
            this.lastChargedTotemCount = charged;

        } else {
            this.totemStatus = new double[0];
        }

        ItemStack chestStack1 = IModularArmor.getArmor(mc.player);
        IOPStorage opStorage = chestStack1.getCapability(CapabilityOP.ITEM);

        if (opStorage != null) {
            long energy = opStorage.getOPStored();
            long maxEnergy = opStorage.getMaxOPStored();

            if (this.energyMode > 0) {
                List<ItemStack> capacitors = new ArrayList<>(EquipmentManager.findItems(
                        e -> e.getItem() instanceof DraconiumCapacitor,
                        mc.player
                ));

                for (ItemStack stack : mc.player.getInventory().items) {
                    if (stack.getItem() instanceof DraconiumCapacitor) {
                        capacitors.add(stack);
                    }
                }

                long capMax = 0;
                long capEnergy = 0;

                for (ItemStack stack : capacitors) {
                    IOPStorage storage = stack.getCapability(CapabilityOP.ITEM);
                    if (storage != null) {
                        capMax = Utils.safeAdd(storage.getMaxOPStored(), capMax);
                        capEnergy = Utils.safeAdd(storage.getOPStored(), capEnergy);
                    }
                }

                if (this.energyMode == 1) {
                    energy = capEnergy;
                    maxEnergy = capMax;
                } else {
                    energy = Utils.safeAdd(capEnergy, energy);
                    maxEnergy = Utils.safeAdd(capMax, maxEnergy);
                }
            }

            this.energyBar = maxEnergy > 0 ? (double) energy / maxEnergy : 0;

            if (this.numericEnergy) {
                this.energyText = Component.translatable("op.brandonscore.op")
                        .append(": " + Utils.formatNumber(energy));
            }
        }

        ci.cancel();
    }
}
