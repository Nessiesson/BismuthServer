package si.bismuth.logging;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Logger {
	// Reference to the minecraft server. Used to look players up by name.
	private final MinecraftServer server;

	// The set of subscribed and online players.
	private final Map<String, String> subscribedOnlinePlayers;

	// The set of subscribed and offline players.
	private final Map<String, String> subscribedOfflinePlayers;

	// The logName of this log. Gets prepended to logged messages.
	private final String logName;

	private final String default_option;

	private final String[] options;

	private final LogHandler defaultHandler;

	// The map of player names to the log handler used
	private final Map<String, LogHandler> handlers;

	Logger(MinecraftServer server, String logName, String def, String[] options) {
		this.server = server;
		subscribedOnlinePlayers = new HashMap<>();
		subscribedOfflinePlayers = new HashMap<>();
		this.logName = logName;
		this.default_option = def;
		this.options = options;
		this.defaultHandler = LogHandler.HUD;
		handlers = new HashMap<>();
	}

	String getDefault() {
		return default_option;
	}

	public String[] getOptions() {
		return options;
	}

	public String getLogName() {
		return logName;
	}

	/**
	 * Subscribes the player with the given logName to the logger.
	 */
	void addPlayer(String playerName, String option, LogHandler handler) {
		if (playerFromName(playerName) != null) {
			subscribedOnlinePlayers.put(playerName, option);
		} else {
			subscribedOfflinePlayers.put(playerName, option);
		}
		if (handler == null)
			handler = defaultHandler;
		handlers.put(playerName, handler);
		handler.onAddPlayer();
		LoggerRegistry.setAccess(this);
	}

	/**
	 * Unsubscribes the player with the given logName from the logger.
	 */
	void removePlayer(String playerName) {
		handlers.getOrDefault(playerName, defaultHandler).onRemovePlayer(playerName);
		subscribedOnlinePlayers.remove(playerName);
		subscribedOfflinePlayers.remove(playerName);
		handlers.remove(playerName);
		LoggerRegistry.setAccess(this);
	}

	/**
	 * Returns true if there are any online subscribers for this log.
	 */
	boolean hasOnlineSubscribers() {
		return subscribedOnlinePlayers.size() > 0;
	}

	public void log(lMessage messagePromise, Object... commandParams) {
		for (Map.Entry<String, String> en : subscribedOnlinePlayers.entrySet()) {
			ServerPlayerEntity player = playerFromName(en.getKey());
			if (player != null) {
				Text[] messages = messagePromise.get(en.getValue(), player);
				if (messages != null)
					sendPlayerMessage(en.getKey(), player, messages, commandParams);
			}
		}
	}

	public void log(Supplier<Text[]> messagePromise, Object... commandParams) {
		Text[] cannedMessages = null;
		for (Map.Entry<String, String> en : subscribedOnlinePlayers.entrySet()) {
			ServerPlayerEntity player = playerFromName(en.getKey());
			if (player != null) {
				if (cannedMessages == null)
					cannedMessages = messagePromise.get();
				sendPlayerMessage(en.getKey(), player, cannedMessages, commandParams);
			}
		}
	}

	private void sendPlayerMessage(String playerName, ServerPlayerEntity player, Text[] messages, Object[] commandParams) {
		handlers.getOrDefault(playerName, defaultHandler).handle(player, messages, commandParams);
	}

	/**
	 * Gets the {@code EntityPlayer} instance for a player given their UUID. Returns null if they are offline.
	 */
	private ServerPlayerEntity playerFromName(String name) {
		return server.getPlayerManager().get(name);
	}

	void onPlayerConnect(PlayerEntity player) {
		// If the player was subscribed to the log and offline, move them to the set of online subscribers.
		String playerName = player.getName();
		if (subscribedOfflinePlayers.containsKey(playerName)) {
			subscribedOnlinePlayers.put(playerName, subscribedOfflinePlayers.get(playerName));
			subscribedOfflinePlayers.remove(playerName);
		}
		LoggerRegistry.setAccess(this);
	}

	void onPlayerDisconnect(PlayerEntity player) {
		// If the player was subscribed to the log, move them to the set of offline subscribers.
		String playerName = player.getName();
		if (subscribedOnlinePlayers.containsKey(playerName)) {
			subscribedOfflinePlayers.put(playerName, subscribedOnlinePlayers.get(playerName));
			subscribedOnlinePlayers.remove(playerName);
		}
		LoggerRegistry.setAccess(this);
	}

	// ----- Event Handlers ----- //

	public String getAcceptedOption(String arg) {
		if (options != null && Arrays.asList(options).contains(arg))
			return arg;
		return null;
	}

	/**
	 * serves messages to players fetching them from the promise
	 * will repeat invocation for players that share the same option
	 */
	@FunctionalInterface
	public interface lMessage {
		Text[] get(String playerOption, PlayerEntity player);
	}
}
