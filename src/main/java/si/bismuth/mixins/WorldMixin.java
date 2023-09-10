package si.bismuth.mixins;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.utils.Profiler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(World.class)
public class WorldMixin {
	// @formatter:off
	private String worldName;
	@Shadow @Final public Dimension dimension;
	@Shadow @Final public List<BlockEntity> blockEntities;
	@Shadow @Final public List<BlockEntity> tickingBlockEntities;
	@Shadow @Final private List<BlockEntity> removedBlockEntities;
	@Shadow private WorldChunk getChunk(BlockPos pos) { return null; }
	@Shadow private boolean isChunkLoaded(BlockPos pos) { return false; }
	// @formatter:on

	@Inject(method = "<init>", at = @At("RETURN"))
	private void setWorldName(WorldStorage saveHandlerIn, WorldData info, Dimension providerIn, net.minecraft.util.profiler.Profiler profilerIn, boolean client, CallbackInfo ci) {
		this.worldName = this.dimension.getType().getKey();
	}

	@Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0, remap = false))
	private boolean fasterTEremoval(List<BlockEntity> list) {
		if (!this.removedBlockEntities.isEmpty()) {
			final Set<BlockEntity> remove = Collections.newSetFromMap(new IdentityHashMap<>());
			remove.addAll(this.removedBlockEntities);
			this.tickingBlockEntities.removeAll(remove);
			this.blockEntities.removeAll(remove);
			this.removedBlockEntities.clear();
		}

		return true;
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=remove"))
	private void onRemoveEntities(CallbackInfo ci) {
		Profiler.start_section(this.worldName, "entities");
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getMount()Lnet/minecraft/entity/Entity;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onGetRidingEntity(CallbackInfo ci, int i, Entity entity) {
		Profiler.start_entity_section(this.worldName, entity);
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 1))
	private void postUpdateEntities(CallbackInfo ci) {
		Profiler.end_current_entity_section();
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=blockEntities"))
	private void onStartBlockEntities(CallbackInfo ci) {
		Profiler.end_current_section();
		Profiler.start_section(this.worldName, "tileentities");
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;isRemoved()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void startBlockEntity(CallbackInfo ci, Iterator<BlockEntity> iterator, BlockEntity entity) {
		Profiler.start_entity_section(this.worldName, entity);
	}

	@Inject(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;isRemoved()Z", ordinal = 1))
	private void endBlockEntity(CallbackInfo ci) {
		Profiler.end_current_entity_section();
	}

	@Inject(method = "tickEntities", at = @At("RETURN"))
	private void onEndUpdateEntities(CallbackInfo ci) {
		Profiler.end_current_section();
	}

	@Redirect(method = "tickEntity(Lnet/minecraft/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;m_8699128()Z"))
	private boolean alwaysLoadChunk(Entity entity) {
		return true;
	}
}
