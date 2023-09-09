package si.bismuth.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
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

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
	private EntityPlayerMP mycopy;

	@Inject(method = "playerLoggedIn", at = @At(value = "RETURN"))
	private void onPlayerLoggedIn(EntityPlayerMP player, CallbackInfo ci) {
		MCServer.playerConnected(player);
	}

	@Inject(method = "playerLoggedOut", at = @At(value = "HEAD"))
	private void onPlayerLoggedOut(EntityPlayerMP player, CallbackInfo ci) {
		MCServer.playerDisconnected(player);
	}

	@Inject(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/management/PlayerList;readPlayerDataFromFile(Lnet/minecraft/entity/player/EntityPlayerMP;)Lnet/minecraft/nbt/NBTTagCompound;"))
	private void onInitializeConnectionToPlayer(NetworkManager manager, EntityPlayerMP player, CallbackInfo ci) {
		if (player instanceof EntityPlayerMPFake) {
			((EntityPlayerMPFake) player).resetToSetPosition();
		}
	}

	@Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
	private NetHandlerPlayServer replaceNetHandler(MinecraftServer server, NetworkManager manager, EntityPlayerMP player) {
		return player instanceof EntityPlayerMPFake ? new NetHandlerPlayServerFake(server, manager, player) : new NetHandlerPlayServer(server, manager, player);
	}

	@Redirect(method = "createPlayerForUser", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;connection:Lnet/minecraft/network/NetHandlerPlayServer;"))
	private NetHandlerPlayServer copyVariable(EntityPlayerMP player) {
		this.mycopy = player;
		return player.connection;
	}

	@Redirect(method = "createPlayerForUser", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
	private void handleFakePlayerJoin(NetHandlerPlayServer handler, ITextComponent component) {
		if (this.mycopy instanceof EntityPlayerMPFake) {
			this.mycopy.onKillCommand();
		} else {
			this.mycopy.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
		}
	}

	@Inject(method = "transferEntityToWorld", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
	private void onTransferEntityToWorld(Entity entity, int lastDimension, WorldServer oldWorld, WorldServer newWorld, CallbackInfo ci) {
		if (entity.addedToChunk && ((IWorldServer) oldWorld).callIsChunkLoaded(entity.chunkCoordX, entity.chunkCoordZ, true)) {
			oldWorld.getChunk(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);
		}
	}

	@Inject(method = "sendMessage(Lnet/minecraft/util/text/ITextComponent;Z)V", at = @At("HEAD"))
	private void onPlayerSendMessage(ITextComponent component, boolean isSystem, CallbackInfo ci) {
		if (!isSystem) {
			final String text = component.getUnformattedText().replaceFirst("^<(\\S*?)>", "\u02F9`$1`\u02FC");
			MCServer.bot.sendToDiscord(text);
			final List<String> args = Arrays.asList(text.split(" "));
			if (args.size() > 1 && args.get(1).equals(";s")) {
				ScoreboardHelper.setScoreboard(args, 1);
			} else if (args.size() > 1 && args.get(1).equals(";t")) {
				ScoreboardHelper.setScoreboard(args, 0);
			}
		}
	}

	@Inject(method = "writePlayerData", at = @At("HEAD"), cancellable = true)
	private void checkFakePlayer(EntityPlayerMP player, CallbackInfo ci){
		if (player instanceof EntityPlayerMPFake){
			ci.cancel();
		}
	}
}
