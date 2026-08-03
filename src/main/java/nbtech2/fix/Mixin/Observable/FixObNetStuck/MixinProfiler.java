package nbtech2.fix.Mixin.Observable.FixObNetStuck;


import nbtech2.fix.Config.Config;
import nbtech2.fix.NBTech2fix;
import observable.server.DiagnosticsKt;
import observable.server.Profiler;
import observable.server.ProfilingData;
import kotlinx.serialization.json.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

@Mixin(Profiler.class)
public abstract class MixinProfiler {
    @Inject(method = "uploadProfile", at = @At("HEAD"), cancellable = true)
    private void onUploadProfile(ProfilingData data, JsonObject diagnostics, CallbackInfoReturnable<String> cir) {
        if (Config.Fix_Observable_Net_Stuck.get() == Config.ObNetStuckFix.NO_UPLOAD) {
            cir.setReturnValue(null);
        }
    }

    @Redirect(method = "uploadProfile",
            at = @At(value = "INVOKE", target = "Ljava/net/URL;openConnection()Ljava/net/URLConnection;"))
    private URLConnection redirectOpenConnection(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        if (Config.Fix_Observable_Net_Stuck.get() == Config.ObNetStuckFix.TIMEOUT) {
            int timeout = Config.Observable_Upload_Timeout_Time.get();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
        }
        return conn;
    }


}
