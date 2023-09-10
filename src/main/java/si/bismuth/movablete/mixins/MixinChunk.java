package si.bismuth.movablete.mixins;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public abstract class MixinChunk {
	@Shadow
	@Final
	private World world;

	@Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/WorldChunk$BlockEntityCreationType;)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 1))
	private BlockEntity worldGetTileEntity(WorldChunk chunk, BlockPos pos, WorldChunk.BlockEntityCreationType mode) {
		return this.world.getBlockEntity(pos);
	}
}
