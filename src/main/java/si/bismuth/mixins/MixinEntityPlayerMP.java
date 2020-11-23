package si.bismuth.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.MCServer;
import si.bismuth.utils.IRecipeBookItemDuper;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements IRecipeBookItemDuper {
	@Shadow
	public abstract boolean isSpectator();

	@Shadow
	public abstract BlockPos getPosition();

	@Shadow
	public abstract Entity getSpectatingEntity();

	public MixinEntityPlayerMP(World world, GameProfile profile) {
		super(world, profile);
	}

	private int dupe;
	private boolean scanForDuping;

	@Shadow
	@Nullable
	public abstract Entity changeDimension(int dim);

	@Override
	public void clearDupeItem() {
		this.dupe = Integer.MIN_VALUE;
	}

	@Override
	public void dupeItem(int slot) {
		if (this.scanForDuping) {
			this.dupe = slot;
		}
	}

	@Override
	public int getDupeItem() {
		return this.dupe;
	}

	@Override
	public void dupeItemScan(boolean s) {
		this.scanForDuping = s;
	}

	@Inject(method = "onUpdate", at = @At("RETURN"))
	private void postOnUpdate(CallbackInfo ci) {
		this.clearDupeItem();
		if (this.isSpectator() && this.getSpectatingEntity() == this) {
			final BlockPos pos = this.getPosition();
			final Block block = this.world.getBlockState(pos).getBlock();
			if (block == Blocks.PORTAL) {
				this.setPortal(pos);
			} else if (block == Blocks.END_PORTAL) {
				this.changeDimension(1);
			} else if (block == Blocks.END_GATEWAY) {
				final TileEntity te = this.world.getTileEntity(pos);
				if (te instanceof TileEntityEndGateway) {
					((TileEntityEndGateway) te).teleportEntity(this);
				}
			}
		}
	}

	@Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
	private void sendMessage(PlayerList list, ITextComponent component) {
		list.sendMessage(component);
		MCServer.bot.sendDeathMessage(component);
		MCServer.log.info("Player {} died at {} {} {} in {}", this.getName(), this.posX, this.posY, this.posZ, this.world.provider.getDimensionType().getName());
	}
}