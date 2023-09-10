package si.bismuth.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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

@Mixin(ServerPlayerEntity.class)
public abstract class MixinEntityPlayerMP extends PlayerEntity implements IRecipeBookItemDuper {
	@Shadow
	public abstract boolean isSpectator();

	@Shadow
	public abstract BlockPos getSourceBlockPos();

	@Shadow
	public abstract Entity getCamera();

	public MixinEntityPlayerMP(World world, GameProfile profile) {
		super(world, profile);
	}

	private int dupe;
	private boolean scanForDuping;

	@Shadow
	@Nullable
	public abstract Entity teleportToDimension(int dim);

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

	@Inject(method = "tick", at = @At("RETURN"))
	private void postOnUpdate(CallbackInfo ci) {
		this.clearDupeItem();
		if (this.isSpectator() && this.getCamera() == this) {
			final BlockPos pos = this.getSourceBlockPos();
			final Block block = this.world.getBlockState(pos).getBlock();
			if (block == Blocks.NETHER_PORTAL) {
				this.onPortalCollision(pos);
			} else if (block == Blocks.END_PORTAL) {
				this.teleportToDimension(1);
			} else if (block == Blocks.END_GATEWAY) {
				final BlockEntity te = this.world.getBlockEntity(pos);
				if (te instanceof EndGatewayBlockEntity) {
					((EndGatewayBlockEntity) te).teleport(this);
				}
			}
		}
	}

	@Redirect(method = "onKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendSystemMessage(Lnet/minecraft/text/Text;)V"))
	private void sendMessage(PlayerManager list, Text component) {
		list.sendSystemMessage(component);
		MCServer.bot.sendDeathMessage(component);
		MCServer.log.info("Player {} died at {} {} {} in {}", this.getName(), this.x, this.y, this.z, this.world.dimension.getType().getKey());
	}
}