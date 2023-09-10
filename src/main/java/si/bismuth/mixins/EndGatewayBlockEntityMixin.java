package si.bismuth.mixins;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.MCServer;
import si.bismuth.commands.AllowGatewayCommand;

@Mixin(EndGatewayBlockEntity.class)
public class EndGatewayBlockEntityMixin extends EndPortalBlockEntity {
	@Unique
	private static final Text DENY = new LiteralText("Stop going through ungenerated end gateways.").setStyle(new Style().setColor(Formatting.DARK_RED));

	@Shadow
	private BlockPos exitPos;

	@Shadow
	private void findExitPortal() { }

	@Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
	private void preventPeopleFromEnteringEndGatewayNearSpawn(Entity entity, CallbackInfo ci) {
		boolean shouldBlockTeleport = false;
		if (this.exitPos == null && this.getPos().squaredDistanceTo(0D, 75D, 0D) < 16384D) {
			shouldBlockTeleport = true;
			if (entity instanceof PlayerEntity) {
				final PlayerEntity player = (PlayerEntity) entity;
				if (AllowGatewayCommand.canEnterPortal) {
					shouldBlockTeleport = false;
					this.findExitPortal();
					AllowGatewayCommand.canEnterPortal = false;
					MCServer.bot.sendToDiscord("**" + player.getName() + " generated gateway " + this.pos + "**");
				} else {
					if (this.world.getTime() % 20L == 0L) {
						player.sendMessage(DENY);
					}
				}
			}
		}

		if (shouldBlockTeleport) {
			ci.cancel();
		}
	}
}
