package nbtech2.fix.Mixin.PS.FixPlayerShellsDiePriority;

import com.mojang.authlib.GameProfile;

import nbtech2.fix.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

@Mixin(value = ServerPlayer.class, priority = 500)
public abstract class MixinServerPlayer extends Player{


    private static Class<?> class_PlayerShells_config = null;
    private static Field field_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = null;
    private static ModConfigSpec.BooleanValue value_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = null;
    private static boolean boolean_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = false;

    private static Class<?> class_PlayerShells_ShellSavedData = null;
    private static Method method_PlayerShells_ShellSavedData_getShellData = null;
    private static Method method_PlayerShells_ShellSavedData_getNearestActive = null;

    private static boolean inited = false;
    private void initStatics(){
        try{
            class_PlayerShells_config = Class.forName("com.ultramega.playershells.Config");
            field_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = class_PlayerShells_config.getDeclaredField("TRANSFER_INTO_SHELL_AFTER_DEATH");
            field_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH.setAccessible(true);
            value_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = (ModConfigSpec.BooleanValue)field_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH.get(null);
            boolean_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH = value_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH.getAsBoolean();

            class_PlayerShells_ShellSavedData = Class.forName("com.ultramega.playershells.storage.ShellSavedData");
            method_PlayerShells_ShellSavedData_getShellData = class_PlayerShells_ShellSavedData.getDeclaredMethod("getShellData",ServerLevel.class);
            method_PlayerShells_ShellSavedData_getShellData.setAccessible(true);

            method_PlayerShells_ShellSavedData_getNearestActive = class_PlayerShells_ShellSavedData.getDeclaredMethod("getNearestActive", UUID.class, ResourceLocation.class, BlockPos.class);
            method_PlayerShells_ShellSavedData_getNearestActive.setAccessible(true);
            inited = true;
        }catch (Exception ignore){}

    }

    public MixinServerPlayer(Level level, BlockPos pos, float rotY, GameProfile gameProfile) {
        super(level, pos, rotY, gameProfile);
    }

    @Shadow
    public abstract ServerLevel serverLevel();

    @Inject(
            method = {"die"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void die(DamageSource cause, CallbackInfo ci) {

        if(!inited) initStatics();

        if(!ModList.get().isLoaded("playershells")) return;

        if(!Config.Fix_Player_Shells_Die_Priority.getAsBoolean()) return;

        if(!boolean_PlayerShells_config_TRANSFER_INTO_SHELL_AFTER_DEATH) return;

        try {
            if(method_PlayerShells_ShellSavedData_getNearestActive.invoke(
                    method_PlayerShells_ShellSavedData_getShellData.invoke(null,this.serverLevel()),
                    this.getUUID(),
                    this.level().dimension().location(),
                    this.blockPosition()
            ) == null) return;
        } catch (Exception ignore) {
            return;
        }

        if (CommonHooks.onLivingDeath(this, cause)) ci.cancel();
    }
}
