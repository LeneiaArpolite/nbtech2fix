package nbtech2.fix.mixin.draconicevolution;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import nbtech2.fix.config.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.handlers.ModularArmorEventHandler", remap = false)
public abstract class ModularArmorEventHandlerMixin {
    @Redirect(
            method = "onEntityAttacked",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/items/equipment/IModularArmor;getArmor(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
                    remap = false
            ),
            remap = false
    )
    private static ItemStack nbtech2fix$findArmorWithShieldController(LivingEntity entity) {
        if (!Config.FIX_DE_CURIOS_ORDER.get()) {
            return IModularArmor.getArmor(entity);
        }

        for (ItemStack stack : EquipmentManager.findItems(
                candidate -> candidate.getItem() instanceof IModularItem,
                entity
        )) {
            ModuleHost host = stack.getCapability(DECapabilities.MODULE_HOST_CAPABILITY)
                    .resolve()
                    .orElse(null);
            if (host != null && host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).findAny().isPresent()) {
                return stack;
            }
        }

        return IModularArmor.getArmor(entity);
    }
}
