package si.bismuth.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.utils.Profiler;

@Mixin(ServerWorld.class)
public abstract class MixinWorldServer extends World {
	protected MixinWorldServer(WorldStorage ish, WorldData wi, Dimension wp, net.minecraft.util.profiler.Profiler p, boolean b) {
		super(ish, wi, wp, p, b);
	}

	@Shadow
	protected abstract boolean isChunkLoadedAt(int x, int y, boolean allowEmpty);

	@Shadow
	@Final
	private static Logger LOGGER;
	private String worldName;

	@Redirect(method = "canAddEntity", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private void silenceTriedToAddEntityButWasMarkedRemovedAlready(Logger logger, String message, Object p0) {
		// noop
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(MinecraftServer server, WorldStorage saveHandlerIn, WorldData info, int dimensionId, net.minecraft.util.profiler.Profiler profilerIn, CallbackInfo ci) {
		this.worldName = this.dimension.getType().getKey();
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=mobSpawner"))
	private void onMobSpawning(CallbackInfo ci) {
		Profiler.start_section(this.worldName, "spawning");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=chunkSource"))
	private void onChunkSource(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=tickPending"))
	private void onTickPending(CallbackInfo ci) {
		Profiler.start_section(this.worldName, "tickupdates");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=tickBlocks"))
	private void onTickBlocks(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=chunkMap"))
	private void onChunkMap(CallbackInfo ci) {
		Profiler.start_section(this.worldName, "chunkmap");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=village"))
	private void onVillage(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "villages");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=portalForcer"))
	private void onTeleporter(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "portals");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V"))
	private void preSendQueuedBlockEvents(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "blockevents");
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V", shift = At.Shift.AFTER))
	private void postSendQueuedBlockEvents(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Inject(method = "tickChunks", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=checkNextLight"))
	private void onCheckNextLight(CallbackInfo ci) {
		Profiler.start_section(this.worldName, "checknextlight");
	}

	@Inject(method = "tickChunks", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=tickChunk"))
	private void onTickChunk(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "tickchunk");
	}

	@Inject(method = "tickChunks", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=thunder"))
	private void onThunder(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "thunder");
	}

	@Inject(method = "tickChunks", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=iceandsnow"))
	private void onIceAndSnow(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "iceandsnow");
	}

	@Inject(method = "tickChunks", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=thunder"))
	private void onRandomTick(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "randomticks");
	}

	@Inject(method = "tickChunks", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Ljava/util/Random;)V")), at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;endSection()V", ordinal = 1))
	private void endMethod(CallbackInfo ci) {
		Profiler.end_current_section();
	}
}
