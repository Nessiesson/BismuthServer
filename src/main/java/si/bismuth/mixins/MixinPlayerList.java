package si.bismuth.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.MCServer;
import si.bismuth.patches.EntityPlayerMPFake;
import si.bismuth.patches.NetHandlerPlayServerFake;
import si.bismuth.utils.ScoreboardHelper;

import java.util.Arrays;
import java.util.List;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerList {
	private ServerPlayerEntity mycopy;

	@Inject(method = "add", at = @At(value = "RETURN"))
	private void onPlayerLoggedIn(ServerPlayerEntity player, CallbackInfo ci) {
		MCServer.playerConnected(player);
	}

	@Inject(method = "remove", at = @At(value = "HEAD"))
	private void onPlayerLoggedOut(ServerPlayerEntity player, CallbackInfo ci) {
		MCServer.playerDisconnected(player);
	}

	@Inject(method = "onLogin", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/PlayerManager;load(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;"))
	private void onInitializeConnectionToPlayer(Connection manager, ServerPlayerEntity player, CallbackInfo ci) {
		if (player instanceof EntityPlayerMPFake) {
			((EntityPlayerMPFake) player).resetToSetPosition();
		}
	}

	@Redirect(method = "onLogin", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
	private ServerPlayNetworkHandler replaceNetHandler(MinecraftServer server, Connection manager, ServerPlayerEntity player) {
		return player instanceof EntityPlayerMPFake ? new NetHandlerPlayServerFake(server, manager, player) : new ServerPlayNetworkHandler(server, manager, player);
	}

	@Redirect(method = "createForLogin", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;connection:Lnet/minecraft/network/NetHandlerPlayServer;"))
	private ServerPlayNetworkHandler copyVariable(ServerPlayerEntity player) {
		this.mycopy = player;
		return player.networkHandler;
	}

	@Redirect(method = "createForLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
	private void handleFakePlayerJoin(ServerPlayNetworkHandler handler, Text component) {
		if (this.mycopy instanceof EntityPlayerMPFake) {
			this.mycopy.discard();
		} else {
			this.mycopy.networkHandler.sendDisconnect(new TranslatableText("multiplayer.disconnect.duplicate_login"));
		}
	}

	@Inject(method = "teleportEntityToDimension", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
	private void onTransferEntityToWorld(Entity entity, int lastDimension, ServerWorld oldWorld, ServerWorld newWorld, CallbackInfo ci) {
		if (entity.isLoaded && ((IWorldServer) oldWorld).callIsChunkLoadedAt(entity.chunkX, entity.chunkZ, true)) {
			oldWorld.getChunkAt(entity.chunkX, entity.chunkZ).removeEntity(entity, entity.chunkY);
		}
	}

	@Inject(method = "sendMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
	private void onPlayerSendMessage(Text component, boolean isSystem, CallbackInfo ci) {
		if (!isSystem) {
			final String text = component.buildString().replaceFirst("^<(\\S*?)>", "\u02F9`$1`\u02FC");
			MCServer.bot.sendToDiscord(text);
			final List<String> args = Arrays.asList(text.split(" "));
			if (args.size() > 1 && args.get(1).equals(";s")) {
				ScoreboardHelper.setSidebarScoreboard(args);
			}
		}
	}

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void checkFakePlayer(ServerPlayerEntity player, CallbackInfo ci){
		if (player instanceof EntityPlayerMPFake){
			ci.cancel();
		}
	}
}
