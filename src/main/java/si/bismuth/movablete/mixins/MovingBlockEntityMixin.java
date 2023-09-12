package si.bismuth.movablete.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityProvider;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.movablete.IMovingBlockEntity;

@Mixin(MovingBlockEntity.class)
public class MovingBlockEntityMixin extends BlockEntity implements IMovingBlockEntity {
	@Shadow
	private BlockState movedState;
	@Unique
	private BlockEntity carriedBlockEntity;

	@Inject(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityPiston;invalidate()V", shift = At.Shift.AFTER), cancellable = true)
	private void finish(CallbackInfo ci) {
		ci.cancel();
		final Block block = this.world.getBlockState(this.pos).getBlock();
		if (block == Blocks.MOVING_BLOCK) {
			this.placeBlock();
		} else if (this.carriedBlockEntity != null && block == Blocks.AIR) {
			this.placeBlock();
			this.world.removeBlock(this.pos);
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"), cancellable = true)
	private void tick(CallbackInfo ci) {
		ci.cancel();
		this.placeBlock();
	}

	@Inject(method = "readNbt", at = @At("RETURN"))
	private void readNbt(NbtCompound compound, CallbackInfo ci) {
		if (compound.isType("carriedTileEntity", 10)) {
			final Block block = this.movedState.getBlock();
			if (block instanceof BlockEntityProvider) {
				this.carriedBlockEntity = ((BlockEntityProvider) block).createBlockEntity(this.world, block.getMetadataFromState(this.movedState));
			}

			if (this.carriedBlockEntity != null) {
				this.carriedBlockEntity.readNbt(compound.getCompound("carriedTileEntity"));
			}
		}
	}

	@Inject(method = "writeNbt", at = @At("RETURN"))
	private void writeNbt(NbtCompound compound, CallbackInfoReturnable<NbtCompound> cir) {
		if (this.carriedBlockEntity != null) {
			compound.put("carriedTileEntity", this.carriedBlockEntity.writeNbt(new NbtCompound()));
		}
	}

	@Unique
	@Override
	public void setCarriedBlockEntity(BlockEntity blockEntity) {
		this.carriedBlockEntity = blockEntity;
	}

	@Unique
	private void placeBlock() {
		final Block block = this.movedState.getBlock();
		this.world.setBlockState(this.pos, this.movedState, 18);
		if (this.carriedBlockEntity != null) {
			this.world.removeBlockEntity(this.pos);
			this.carriedBlockEntity.cancelRemoval();
			this.world.setBlockEntity(this.pos, this.carriedBlockEntity);
		}

		this.world.onBlockChanged(this.pos, Blocks.MOVING_BLOCK, true);
		if (this.movedState.isAnalogSignalSource()) {
			this.world.updateNeighborComparators(this.pos, block);
		}

		this.world.updateObservers(this.pos, block);
		this.world.neighborChanged(this.pos, block, this.pos);
	}
}
