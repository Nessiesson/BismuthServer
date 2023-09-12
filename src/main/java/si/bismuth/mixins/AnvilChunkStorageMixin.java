package si.bismuth.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;
import net.minecraft.world.chunk.storage.io.FileIoCallback;
import net.minecraft.world.chunk.storage.io.FileIoThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.MCServer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mixin(AnvilChunkStorage.class)
public class AnvilChunkStorageMixin implements FileIoCallback {
	private final Map<ChunkPos, NbtCompound> chunksToSave = new HashMap<>();
	private final Map<ChunkPos, NbtCompound> chunksInWrite = new HashMap<>();
	private ChunkPos copyOfChunkPos1;
	private ChunkPos copyOfChunkPos2;

	@Shadow
	private void saveChunk(ChunkPos pos, NbtCompound compound) { }

	synchronized private void queueChunkToRemove(ChunkPos pos, NbtCompound compound) {
		chunksToSave.put(pos, compound);
	}

	synchronized private Map.Entry<ChunkPos, NbtCompound> fetchChunkToWrite() {
		if (this.chunksToSave.isEmpty()) {
			return null;
		}

		final Iterator<Map.Entry<ChunkPos, NbtCompound>> iter = this.chunksToSave.entrySet().iterator();
		final Map.Entry<ChunkPos, NbtCompound> entry = iter.next();
		iter.remove();
		this.chunksInWrite.put(entry.getKey(), entry.getValue());
		return entry;
	}

	synchronized private void retireChunkToWrite(ChunkPos pos) {
		this.chunksInWrite.remove(pos);
	}

	synchronized private NbtCompound reloadChunkFromRemoveQueues(ChunkPos pos) {
		final NbtCompound data = this.chunksToSave.get(pos);
		if (data != null) {
			return data;
		}

		return this.chunksInWrite.get(pos);
	}

	@Inject(method = "loadChunk(Lnet/minecraft/world/World;II)Lnet/minecraft/world/chunk/WorldChunk;", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
	private void copyChunkPos(World world, int x, int z, CallbackInfoReturnable<WorldChunk> cir, ChunkPos pos) {
		this.copyOfChunkPos1 = pos;
	}

	@Redirect(method = "loadChunk(Lnet/minecraft/world/World;II)Lnet/minecraft/world/chunk/WorldChunk;", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
	private Object onLoadChunk(Map<ChunkPos, NbtCompound> map, Object key) {
		return reloadChunkFromRemoveQueues(this.copyOfChunkPos1);
	}

	@Inject(method = "doesChunkExist", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
	private void copyPos(int x, int z, CallbackInfoReturnable<Boolean> cir, ChunkPos pos) {
		this.copyOfChunkPos2 = pos;
	}

	@Redirect(method = "doesChunkExist", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
	private Object onDoesChunkExist(Map<ChunkPos, NbtCompound> map, Object key) {
		return reloadChunkFromRemoveQueues(this.copyOfChunkPos2);
	}

	@Inject(method = "queueChunkSave", at = @At("HEAD"), cancellable = true)
	private void onQueueChunkSave(ChunkPos pos, NbtCompound compound, CallbackInfo ci) {
		if (!this.chunksInWrite.containsKey(pos)) {
			queueChunkToRemove(pos, compound);
		}

		FileIoThread.getInstance().registerCallback(this);
		ci.cancel();
	}

	/**
	 * @author DeadlyMC
	 * @reason The changes to this method are very invasive and this is the simplest way to achieve what's needed
	 */
	@Overwrite
	@Override
	public boolean run() {
		final Map.Entry<ChunkPos, NbtCompound> entry = this.fetchChunkToWrite();
		if (entry == null) {
			return false;
		}

		final ChunkPos chunkpos = entry.getKey();
		final NbtCompound compound = entry.getValue();
		try {
			this.saveChunk(chunkpos, compound);
		} catch (Exception exception) {
			MCServer.log.error("Failed to save chunk", exception);
		}

		this.retireChunkToWrite(chunkpos);
		return true;
	}
}
