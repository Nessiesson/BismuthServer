package carpet.bismuth.mixins;

import carpet.bismuth.CarpetSettings;
import carpet.bismuth.utils.ITileEntityPiston;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BlockPistonBase.class)
public abstract class BlockPistonBaseMixin {
    private List<TileEntity> tileEntitiesList;

    @Redirect(method = "canPush", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasTileEntity()Z"))
    private static boolean canPushTE(Block block) {
        if (CarpetSettings.movableTileEntities) {
            return block.hasTileEntity();
        }

        if (block.hasTileEntity()) {
            return !(isPushableTileEntityBlock(block));
        } else {
            return false;
        }
    }

    private static boolean isPushableTileEntityBlock(Block block) {
        //Making PISTON_EXTENSION (BlockPistonMoving) pushable would not work as its createNewTileEntity()-method returns null
        return block != Blocks.ENDER_CHEST && block != Blocks.ENCHANTING_TABLE && block != Blocks.END_GATEWAY
                && block != Blocks.END_PORTAL && block != Blocks.MOB_SPAWNER && block != Blocks.PISTON_EXTENSION;
    }

    @Inject(method = "doMove", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void doMoveTE(World world, BlockPos p, EnumFacing d, boolean e, CallbackInfoReturnable<Boolean> cir, BlockPistonStructureHelper bpsh, List<BlockPos> list, List<IBlockState> lbs, List<BlockPos> lbp, int k, IBlockState[] abs) {
        if (CarpetSettings.movableTileEntities) {
            this.tileEntitiesList = Lists.newArrayList();
            for (BlockPos blockPos : list) {
                final TileEntity tileEntity = world.getTileEntity(blockPos);
                tileEntitiesList.add(tileEntity);
                if (tileEntity != null) {
                    world.removeTileEntity(blockPos);
                    tileEntity.markDirty();
                }
            }
        }
    }

    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", shift = At.Shift.AFTER, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void setTileEntityTE(World world, BlockPos p, EnumFacing d, boolean ex, CallbackInfoReturnable<Boolean> cir, BlockPistonStructureHelper bpsh, List<BlockPos> list, List<IBlockState> lbs, List<BlockPos> lbp, int i, IBlockState[] abs, EnumFacing enumfacing, int l, BlockPos pos, IBlockState bs) {
        if (CarpetSettings.movableTileEntities) {
            // For movableTE
            final TileEntity e = world.getTileEntity(pos);
            if (e instanceof TileEntityPiston) {
                ((ITileEntityPiston) e).setCarriedTileEntity(tileEntitiesList.get(l));
            }
        }
    }
}
