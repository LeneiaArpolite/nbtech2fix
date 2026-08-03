package nbtech2.fix.Mixin.Observable.FixObNoOshiFileStuck;

import kotlinx.serialization.json.JsonObject;
import nbtech2.fix.Config.Config;
import nbtech2.fix.NBTech2fix;
import observable.server.DiagnosticsKt;
import observable.server.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashMap;
import java.util.concurrent.*;

@Mixin(Profiler.class)
public abstract class MixinProfiler {
    @Redirect(
            method = "stopRunning",
            at = @At(
                    value = "INVOKE",
                    target = "Lobservable/server/DiagnosticsKt;getDiagnostics(Lobservable/server/Profiler;)Lkotlinx/serialization/json/JsonObject;"
            ),
            remap = false
    )
    private JsonObject observable$safeGetDiagnostics(Profiler profiler) {
        if (Config.Fix_Observable_No_oshi_File_Stuck.getAsBoolean()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<JsonObject> future = executor.submit(() ->
                    DiagnosticsKt.getDiagnostics(profiler)
            );
            try {
                return future.get(Config.Observable_No_File_Timeout_Time.get(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                NBTech2fix.LOGGER.warn("[MixinProfiler] Diagnostics timed out, using empty report");
                future.cancel(true);
                return new JsonObject(new LinkedHashMap<>());
            } catch (Exception e) {
                NBTech2fix.LOGGER.warn("[MixinProfiler] Diagnostics failed, using empty report", e);
                return new JsonObject(new LinkedHashMap<>());
            } finally {
                executor.shutdownNow();
            }
        }
        return DiagnosticsKt.getDiagnostics(profiler);
    }
}
