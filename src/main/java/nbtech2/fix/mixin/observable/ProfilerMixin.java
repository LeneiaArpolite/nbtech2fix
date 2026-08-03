package nbtech2.fix.mixin.observable;

import kotlinx.serialization.json.JsonObject;
import nbtech2.fix.NBTech2Fix;
import nbtech2.fix.config.Config;
import observable.server.DiagnosticsKt;
import observable.server.Profiler;
import observable.server.ProfilingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Pseudo
@Mixin(targets = "observable.server.Profiler", remap = false)
public abstract class ProfilerMixin {
    @Inject(method = "uploadProfile", at = @At("HEAD"), cancellable = true, remap = false)
    private void nbtech2fix$skipUpload(
            ProfilingData data,
            JsonObject diagnostics,
            CallbackInfoReturnable<String> callback
    ) {
        if (Config.FIX_OBSERVABLE_NETWORK.get() == Config.ObservableNetworkFix.NO_UPLOAD) {
            callback.setReturnValue(null);
        }
    }

    @Redirect(
            method = "uploadProfile",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/net/URL;openConnection()Ljava/net/URLConnection;",
                    remap = false
            ),
            remap = false
    )
    private URLConnection nbtech2fix$openConnectionWithTimeout(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (Config.FIX_OBSERVABLE_NETWORK.get() == Config.ObservableNetworkFix.TIMEOUT) {
            int timeout = Config.OBSERVABLE_UPLOAD_TIMEOUT_MS.get();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        }
        return connection;
    }

    @Redirect(
            method = "stopRunning",
            at = @At(
                    value = "INVOKE",
                    target = "Lobservable/server/DiagnosticsKt;getDiagnostics(Lobservable/server/Profiler;)Lkotlinx/serialization/json/JsonObject;",
                    remap = false
            ),
            remap = false
    )
    private JsonObject nbtech2fix$getDiagnosticsWithTimeout(Profiler profiler) {
        if (!Config.FIX_OBSERVABLE_DIAGNOSTICS.get()) {
            return DiagnosticsKt.getDiagnostics(profiler);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "NBTech2Fix-Observable-Diagnostics");
            thread.setDaemon(true);
            return thread;
        });
        Future<JsonObject> future = executor.submit(() -> DiagnosticsKt.getDiagnostics(profiler));
        try {
            return future.get(Config.OBSERVABLE_DIAGNOSTICS_TIMEOUT_MS.get(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            NBTech2Fix.LOGGER.warn("Observable diagnostics timed out; continuing with an empty report");
            return emptyDiagnostics();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            NBTech2Fix.LOGGER.warn("Observable diagnostics was interrupted; continuing with an empty report");
            return emptyDiagnostics();
        } catch (ExecutionException exception) {
            NBTech2Fix.LOGGER.warn("Observable diagnostics failed; continuing with an empty report", exception.getCause());
            return emptyDiagnostics();
        } finally {
            future.cancel(true);
            executor.shutdownNow();
        }
    }

    private static JsonObject emptyDiagnostics() {
        return new JsonObject(new LinkedHashMap<>());
    }
}
