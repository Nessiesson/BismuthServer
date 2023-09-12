package si.bismuth.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.utils.BlockRotator;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Shadow
	public World world;
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "startMiningBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateBlockMiningProgress(ILnet/minecraft/util/math/BlockPos;I)V"))
	private void notifyUpdate(BlockPos pos, Direction face, CallbackInfo ci) {
		this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));
	}

	@Redirect(method = "useItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;use(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/util/math/Direction;FFF)Z"))
	private boolean onUseItemOnBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, InteractionHand hand, Direction face, float hitX, float hitY, float hitZ) {
		final boolean flipped = BlockRotator.flipBlockWithCactus(world, pos, state, player, face, hitX, hitY, hitZ);
		if (flipped) {
			return true;
		}

		return state.getBlock().use(world, pos, state, player, hand, face, hitX, hitY, hitZ);
	}
}
