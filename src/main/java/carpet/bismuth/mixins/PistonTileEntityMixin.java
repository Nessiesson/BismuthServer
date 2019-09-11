package carpet.bismuth.mixins;

import carpet.bismuth.CarpetSettings;
import carpet.bismuth.utils.ITileEntityPiston;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityPiston.class)
public abstract class PistonTileEntityMixin extends TileEntity implements ITileEntityPiston {
    @Shadow
    private IBlockState pistonState;
    private TileEntity carriedTileEntity;

    public void setCarriedTileEntity(TileEntity tileEntity) {
        this.carriedTileEntity = tileEntity;
        if (this.carriedTileEntity != null)
            this.carriedTileEntity.setPos(this.pos);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void readFromNBTTE(NBTTagCompound compound, CallbackInfo ci) {
        if (CarpetSettings.movableTileEntities && compound.hasKey("carriedTileEntity")) {
            final Block block = this.pistonState.getBlock();
            if (block instanceof ITileEntityProvider) {
                this.carriedTileEntity = ((ITileEntityProvider) block).createNewTileEntity(this.world, block.getMetaFromState(this.pistonState));
            }

            if (this.carriedTileEntity != null) {
                this.carriedTileEntity.readFromNBT(compound.getCompoundTag("carriedTileEntity"));
            }
        }
    }

    @Inject(method = "clearPistonTileEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/tileentity/TileEntityPiston;invalidate()V"), cancellable = true)
    private void clearPistonTileEntityTE(CallbackInfo ci) {
        if (!CarpetSettings.movableTileEntities) {
            if (this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION) {
                this.placeBlock();
            } else if (!this.world.isRemote && this.carriedTileEntity != null && this.world.getBlockState(this.pos).getBlock() == Blocks.AIR) {
                this.placeBlock();
                this.world.setBlockToAir(this.pos);
            }

            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/tileentity/TileEntityPiston;invalidate()V"), cancellable = true)
    private void updateTE(CallbackInfo ci) {
        if (!CarpetSettings.movableTileEntities && this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION) {
            this.placeBlock();
            ci.cancel();
        }
    }

    private void placeBlock() {
        // workaround for the direction change caused by BlockDispenser.onBlockAdded();
        final Block block = this.pistonState.getBlock();
        if (block instanceof BlockDispenser || block instanceof BlockFurnace) {
            this.world.setBlockState(this.pos, this.pistonState, 18);
        }
        //workaround is just placing the block twice. This should not cause any problems, but is ugly code

        this.world.setBlockState(this.pos, this.pistonState, 18);  //Flag 18 => No block updates, TileEntity has to be placed first
        if (!this.world.isRemote) {
            if (this.carriedTileEntity != null) {
                this.world.removeTileEntity(this.pos);
                this.carriedTileEntity.validate();
                this.world.setTileEntity(this.pos, this.carriedTileEntity);
            }

            //Update neighbors, comparators and observers now (same order as setBlockState would have if flag was set to 3 (default))
            //This should not change piston behavior for vanilla-pushable blocks at all
            this.world.notifyNeighborsRespectDebug(pos, Blocks.PISTON_EXTENSION, true);
            if (this.pistonState.hasComparatorInputOverride()) {
                this.world.updateComparatorOutputLevel(pos, this.pistonState.getBlock());
            }

            this.world.updateObservingBlocksAt(pos, this.pistonState.getBlock());
        }

        this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
    }
}
