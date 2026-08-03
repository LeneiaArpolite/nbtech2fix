package nbtech2.fix.Mixin.MMSM;

import nbtech2.fix.Config.Config;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.util.StorageUtils;
import moremekasuitmodules.common.ShieldProviderHandler;
import moremekasuitmodules.common.item.interfaces.IShieldProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShieldProviderHandler.class)
public class MixinShieldProviderHandler {

    @Inject(
            method = "onPlayerDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            ),
            cancellable = true
    )
    private void fixLastStandWithoutShield(LivingDeathEvent event, CallbackInfo ci) {

        if (!Config.Fix_MMSM_Energy_Shield_AntiDie_BUG.getAsBoolean()) {
            return;
        }

        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        ShieldProviderHandler.ArmorSummery summery = new ShieldProviderHandler.ArmorSummery().getSummery(player);
        if (summery == null) {
            return;
        }

        boolean shieldEnabled = ((ShieldProviderHandler)(Object)this).getShieldState(player);

        if (!shieldEnabled) {
            ci.cancel();
        }
    }


    @Redirect(
            method = "onPlayerDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoremekasuitmodules/common/item/interfaces/IShieldProvider;modifyEnergy(Lnet/minecraft/world/item/ItemStack;JLnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void fixEnergyCost(IShieldProvider provider, ItemStack stack, long amount, LivingEntity entity) {

        if (!(entity instanceof Player player)) {
            provider.modifyEnergy(stack, amount, entity);
            return;
        }

        if (!Config.Fix_MMSM_Energy_Shield_Cost_BUG.getAsBoolean()) {
            provider.modifyEnergy(stack, amount, player);
            return;
        }

        ShieldProviderHandler.ArmorSummery summery =
                new ShieldProviderHandler.ArmorSummery().getSummery(player);
        if (summery == null) return;

        int size = summery.armorStacks.size();
        long[] energyStored = new long[size];
        long[] maxEnergy = new long[size];

        double sumMax = 0;
        double sumStored = 0;

        for (int i = 0; i < size; i++) {
            ItemStack s = summery.armorStacks.get(i);
            if (!s.isEmpty() && s.getItem() instanceof IShieldProvider p) {
                energyStored[i] = p.getEnergyStored(s);
                maxEnergy[i] = p.getMaxEnergyStored(s);

                if (maxEnergy[i] > 0) {
                    sumMax += maxEnergy[i];
                    sumStored += energyStored[i];
                }
            }
        }

        if (sumMax <= 0) return;

        double totalCost = 10_000_000D;

        double k = (sumStored - totalCost) / sumMax;
        if (k < 0) k = 0;
        if (k > 1) k = 1;

        for (int i = 0; i < size; i++) {
            if (summery.armorStacks.get(i) == stack) {

                long current = energyStored[i];
                long target = (long)(k * maxEnergy[i]);
                long cost = current - target;

                if (cost < 0) cost = 0;
                if (cost > current) cost = current;

                if (cost > 0) {

                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    if (energyContainer != null) {
                        energyContainer.extract(cost, Action.EXECUTE, AutomationType.MANUAL);
                    } else {
                        provider.modifyEnergy(stack, cost, player);
                    }

                }
                return;
            }
        }
    }

}
