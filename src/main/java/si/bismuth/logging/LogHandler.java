package si.bismuth.logging;

import si.bismuth.MCServer;
import si.bismuth.utils.HUDController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class LogHandler {
	private static final LogHandler CHAT = new LogHandler() {
		@Override
		public void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams) {
			Arrays.stream(message).forEach(player::sendMessage);
		}
	};
	static final LogHandler HUD = new LogHandler() {
		@Override
		public void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams) {
			for (Text m : message)
				HUDController.addMessage(player, m);
		}

		@Override
		public void onRemovePlayer(String playerName) {
			ServerPlayerEntity player = MCServer.server.getPlayerManager().get(playerName);
			if (player != null)
				HUDController.clear_player(player);
		}
	};

	private static final Map<String, LogHandlerCreator> CREATORS = new HashMap<>();

	static {
		registerCreator("chat", extraArgs -> CHAT);
		registerCreator("hud", extraArgs -> HUD);
		registerCreator("command", CommandLogHandler::new);
	}

	private static void registerCreator(String name, LogHandlerCreator creator) {
		CREATORS.put(name, creator);
	}

	public static LogHandler createHandler(String name, String... extraArgs) {
		return CREATORS.get(name).create(extraArgs);
	}

	public static List<String> getHandlerNames() {
		return CREATORS.keySet().stream().sorted().collect(Collectors.toList());
	}

	public abstract void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams);

	public void onRemovePlayer(String playerName) {
		// noop
	}

	void onAddPlayer() {
		// noop
	}

	@FunctionalInterface
	private interface LogHandlerCreator {
		LogHandler create(String... extraArgs);
	}
}
