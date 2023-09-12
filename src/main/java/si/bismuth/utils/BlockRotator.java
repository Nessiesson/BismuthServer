package si.bismuth.utils;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotation;
import net.minecraft.block.Blocks;
import net.minecraft.block.DiodeBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class BlockRotator {
	private static final List<String> UUIDS = Arrays.asList("c36e8cbd-b090-47b7-8166-bab6985e4382", "73a0e9c7-d30f-43dd-b820-55c72c62a6f7", "78bbb591-b677-41cc-9d60-194fcb2e422c", "8a45fff7-0335-4928-a98c-dcc8b1ad5193");

	public static boolean flipBlockWithCactus(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Direction facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.abilities.canModifyWorld || !player_holds_cactus_mainhand(playerIn)) {
			return false;
		}

		return flip_block(worldIn, pos, state, facing, hitX, hitY, hitZ);
	}

	private static boolean flip_block(World worldIn, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ) {
		Block block = state.getBlock();
		if ((block instanceof GlazedTerracottaBlock) || (block instanceof DiodeBlock) || (block instanceof AbstractRailBlock) || (block instanceof TrapdoorBlock) || (block instanceof LeverBlock) || (block instanceof FenceGateBlock)) {
			worldIn.setBlockState(pos, state.rotate(BlockRotation.CLOCKWISE_90), 130);
		} else if ((block instanceof ObserverBlock) || (block instanceof EndRodBlock)) {
			worldIn.setBlockState(pos, state.set(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 130);
		} else if (block instanceof DispenserBlock) {
			worldIn.setBlockState(pos, state.set(DispenserBlock.FACING, state.get(DispenserBlock.FACING).getOpposite()), 130);
		} else if (block instanceof PistonBaseBlock) {
			if (!(state.get(PistonBaseBlock.EXTENDED)))
				worldIn.setBlockState(pos, state.set(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 130);
		} else if (block instanceof SlabBlock) {
			if (!((SlabBlock) block).isDouble()) {
				if (state.get(SlabBlock.HALF) == SlabBlock.Half.TOP) {
					worldIn.setBlockState(pos, state.set(SlabBlock.HALF, SlabBlock.Half.BOTTOM), 130);
				} else {
					worldIn.setBlockState(pos, state.set(SlabBlock.HALF, SlabBlock.Half.TOP), 130);
				}
			}
		} else if (block instanceof HopperBlock) {
			if (state.get(HopperBlock.FACING) != Direction.DOWN) {
				worldIn.setBlockState(pos, state.set(HopperBlock.FACING, state.get(HopperBlock.FACING).clockwiseY()), 130);
			}
		} else if (block instanceof StairsBlock) {
			if ((facing == Direction.UP && hitY == 1.0f) || (facing == Direction.DOWN && hitY == 0.0f)) {
				if (state.get(StairsBlock.HALF) == StairsBlock.Half.TOP) {
					worldIn.setBlockState(pos, state.set(StairsBlock.HALF, StairsBlock.Half.BOTTOM), 130);
				} else {
					worldIn.setBlockState(pos, state.set(StairsBlock.HALF, StairsBlock.Half.TOP), 130);
				}
			} else {
				boolean turn_right;
				if (facing == Direction.NORTH) {
					turn_right = (hitX <= 0.5);
				} else if (facing == Direction.SOUTH) {
					turn_right = !(hitX <= 0.5);
				} else if (facing == Direction.EAST) {
					turn_right = (hitZ <= 0.5);
				} else if (facing == Direction.WEST) {
					turn_right = !(hitZ <= 0.5);
				} else {
					return false;
				}
				if (turn_right) {
					worldIn.setBlockState(pos, state.rotate(BlockRotation.COUNTERCLOCKWISE_90), 130);
				} else {
					worldIn.setBlockState(pos, state.rotate(BlockRotation.CLOCKWISE_90), 130);
				}
			}
		} else {
			return false;
		}
		worldIn.onRegionChanged(pos, pos);
		return true;
	}

	private static boolean player_holds_cactus_mainhand(PlayerEntity playerIn) {
		return (!playerIn.getMainHandStack().isEmpty() && playerIn.getMainHandStack().getItem() instanceof BlockItem && ((BlockItem) (playerIn.getMainHandStack().getItem())).getBlock() == Blocks.CACTUS);
	}

	public static boolean flippinEligibility(Entity entity) {
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			return (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() instanceof BlockItem && ((BlockItem) (player.getOffHandStack().getItem())).getBlock() == Blocks.CACTUS);
		}
		return false;
	}
}
