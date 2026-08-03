package nbtech2.fix.Util;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import nbtech2.fix.Config.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DEHelper {
    public static ItemStack getEffectiveArmor(LivingEntity entity) {

        List<ItemStack> candidates = new ArrayList<>();

        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof IModularArmor) {
            candidates.add(chest);
        }

        candidates.addAll(EquipmentManager.findItems(
                e -> e.getItem() instanceof IModularArmor,
                entity
        ));

        for (ItemStack stack : candidates) {
            ModuleHost host = DECapabilities.getHost(stack);
            if (host != null && host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).findAny().isPresent()) {
                return stack;
            }
        }

        return candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(0);
    }
	
}

