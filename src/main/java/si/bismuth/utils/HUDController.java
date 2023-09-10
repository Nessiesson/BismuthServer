package si.bismuth.utils;

import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TabListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import si.bismuth.logging.LoggerRegistry;
import si.bismuth.mixins.ITabListS2CPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HUDController {
	private static final Map<PlayerEntity, List<Text>> player_huds = new HashMap<>();

	public static void addMessage(PlayerEntity player, Text hudMessage) {
		if (!player_huds.containsKey(player)) {
			player_huds.put(player, new ArrayList<>());
		} else {
			player_huds.get(player).add(new LiteralText("\n"));
		}
		player_huds.get(player).add(hudMessage);
	}

	public static void clear_player(PlayerEntity player) {
		TabListS2CPacket packet = new TabListS2CPacket();
		((ITabListS2CPacket) packet).setHeader(new LiteralText(""));
		((ITabListS2CPacket) packet).setFooter(new LiteralText(""));
		((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
	}


	public static void update_hud(MinecraftServer server) {
		if (server.getTicks() % 20 != 0)
			return;

		player_huds.clear();

		if (LoggerRegistry.__tps)
			log_tps(server);

		if (LoggerRegistry.__mobcaps)
			log_mobcaps();

		for (PlayerEntity player : player_huds.keySet()) {
			TabListS2CPacket packet = new TabListS2CPacket();
			((ITabListS2CPacket) packet).setHeader(new LiteralText(""));
			((ITabListS2CPacket) packet).setFooter(Messenger.m(null, player_huds.get(player).toArray(new Object[0])));
			((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
		}
	}

	private static void log_tps(MinecraftServer server) {
		double MSPT = MathHelper.average(server.averageTickTimes) * 1.0E-6D;
		double TPS = 1000.0D / Math.max(50, MSPT);
		String color = Messenger.heatmap_color(MSPT, 50);
		Text[] message = new Text[]{Messenger.m(null, "g TPS: ", String.format(Locale.US, "%s %.1f", color, TPS), "g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT))};
		LoggerRegistry.getLogger("tps").log(() -> message, "MSPT", MSPT, "TPS", TPS);
	}

	private static void log_mobcaps() {
		List<Object> commandParams = new ArrayList<>();
		for (int dim = -1; dim <= 1; dim++) {
			for (MobCategory type : MobCategory.values()) {
				Pair<Integer, Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Pair<>(0, 0));
				int actual = counts.getLeft(), limit = counts.getRight();
				Collections.addAll(commandParams, type.name() + "_ACTUAL_DIM_" + dim, actual, type.name() + "_ACTUAL_LIMIT_" + dim, limit);
			}
		}
		LoggerRegistry.getLogger("mobcaps").log((option, player) -> {
			int dim = player.dimensionId;
			switch (option) {
				case "overworld":
					dim = 0;
					break;
				case "nether":
					dim = -1;
					break;
				case "end":
					dim = 1;
					break;
			}
			return send_mobcap_display(dim);
		}, commandParams.toArray());
	}

	private static Text[] send_mobcap_display(int dim) {
		List<Text> components = new ArrayList<>();
		for (MobCategory type : MobCategory.values()) {
			Pair<Integer, Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Pair<>(0, 0));
			int actual = counts.getLeft();
			int limit = counts.getRight();
			components.add(Messenger.m(null, (actual + limit == 0) ? "g -" : Messenger.heatmap_color(actual, limit) + " " + actual, Messenger.creatureTypeColor(type) + " /" + ((actual + limit == 0) ? "-" : limit)));
			components.add(Messenger.m(null, "w  "));
		}
		components.remove(components.size() - 1);
		return new Text[]{Messenger.m(null, components.toArray(new Object[0]))};
	}
}
