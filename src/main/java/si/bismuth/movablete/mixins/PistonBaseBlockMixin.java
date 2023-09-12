package si.bismuth.movablete.mixins;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.piston.PistonMoveStructureResolver;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.movablete.IMovingBlockEntity;

import java.util.List;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
	@Unique
	private List<BlockEntity> blockEntitiesToMove;

	@Redirect(method = "canMoveBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasBlockEntity()Z"))
	private static boolean canMoveBlockEntity(Block block) {
		return block.hasBlockEntity() && !isMovableBlockEntityBlock(block);
	}

	private static boolean isMovableBlockEntityBlock(Block block) {
		return block != Blocks.ENDER_CHEST && block != Blocks.ENCHANTING_TABLE && block != Blocks.END_GATEWAY
				&& block != Blocks.END_PORTAL && block != Blocks.MOB_SPAWNER && block != Blocks.MOVING_BLOCK;
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
	private void moveBlockEntity(World world, BlockPos p, Direction d, boolean e, CallbackInfoReturnable<Boolean> cir, PistonMoveStructureResolver bpsh, List<BlockPos> list) {
		this.blockEntitiesToMove = Lists.newArrayList();
		for (BlockPos pos : list) {
			final BlockEntity be = world.getBlockEntity(pos);
			this.blockEntitiesToMove.add(be);
			if (be != null) {
				world.removeBlockEntity(pos);
				be.markDirty();
			}
		}
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V", shift = At.Shift.AFTER, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void setMovedBlockEntity(World world, BlockPos p, Direction d, boolean ex, CallbackInfoReturnable<Boolean> cir, PistonMoveStructureResolver bpsh, List<BlockPos> list, List<BlockState> lbs, List<BlockPos> lbp, int i, BlockState[] abs, Direction enumfacing, int l, BlockPos pos) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof MovingBlockEntity) {
			((IMovingBlockEntity) be).setCarriedBlockEntity(this.blockEntitiesToMove.get(l));
		}
	}
}
