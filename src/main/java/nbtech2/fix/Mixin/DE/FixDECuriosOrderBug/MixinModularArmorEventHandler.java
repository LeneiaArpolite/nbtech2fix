package nbtech2.fix.Mixin.DE.FixDECuriosOrderBug;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.handlers.ModularArmorEventHandler;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import nbtech2.fix.Config.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ModularArmorEventHandler.class)
public abstract class MixinModularArmorEventHandler {


    @Redirect(
            method = "onEntityAttacked",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/items/equipment/IModularArmor;getArmor(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack redirectArmor(LivingEntity entity) {

        if (!Config.Fix_DE_Curios_Order_Bug.getAsBoolean()) {
            return IModularArmor.getArmor(entity);
        }

        List<ItemStack> equipment = EquipmentManager.findItems(
                e -> e.getItem() instanceof IModularItem,
                entity
        );

        for (ItemStack stack : equipment) {
            ModuleHost host = DECapabilities.getHost(stack);
            if (host != null && host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).findAny().isPresent()) {
                return stack;
            }
        }


        return IModularArmor.getArmor(entity);
    }
}
