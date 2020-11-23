package si.bismuth.mixins;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.MCServer;
import si.bismuth.utils.Profiler;

import javax.security.auth.login.LoginException;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCtor(CallbackInfo ci) {
		MCServer.init((MinecraftServer) (Object) this);
	}

	@Inject(method = "loadAllWorlds", at = @At("HEAD"))
	private void onLoadAllWorlds(CallbackInfo ci) throws LoginException, InterruptedException {
		MCServer.onServerLoaded((MinecraftServer) (Object) this);
	}

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 0, remap = false))
	private void silenceCantKeepUp(Logger logger, String message, Object p0, Object p1) {
		// noop
	}

	@Inject(method = "tick", at = @At(value = "FIELD", ordinal = 0, shift = At.Shift.AFTER, target = "Lnet/minecraft/server/MinecraftServer;tickCounter:I"))
	private void onTick(CallbackInfo ci) {
		MCServer.tick((MinecraftServer) (Object) this);

		if (Profiler.tick_health_requested != 0L) {
			Profiler.start_tick_profiling();
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", args = "ldc=save"))
	private void onAutoSave(CallbackInfo ci) {
		Profiler.start_section(null, "autosave");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0))
	private void postAutoSave(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void postTick(CallbackInfo ci) {
		if (Profiler.tick_health_requested != 0L) {
			Profiler.end_tick_profiling((MinecraftServer) (Object) this);
		}
	}

	@Inject(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=connection"))
	private void preNetworkTick(CallbackInfo ci) {
		Profiler.start_section(null, "network");
	}

	@Inject(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=commandFunctions"))
	private void postNetworkTick(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Inject(method = "getServerModName", at = @At("HEAD"), cancellable = true)
	private void setServerModName(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue("bismuth");
	}
}
