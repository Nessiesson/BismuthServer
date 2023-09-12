package si.bismuth.utils;

import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Formatting;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class Messenger {
    /*
     messsage: "desc me ssa ge"
     desc contains:
     i = italic
     s = strikethrough
     u = underline
     b = bold
     o = obfuscated

     w = white
     y = yellow
     m = magenta (light purple)
     r = red
     c = cyan (aqua)
     l = lime (green)
     t = light blue (blue)
     f = dark gray
     g = gray
     d = gold
     p = dark purple (purple)
     n = dark red (brown)
     q = dark aqua
     e = dark green
     v = dark blue (navy)
     k = black

     / = action added to the previous component
     */

	private static Text _applyStyleToTextComponent(Text comp, String style) {
		//could be rewritten to be more efficient
		comp.getStyle().setItalic(style.indexOf('i') >= 0);
		comp.getStyle().setStrikethrough(style.indexOf('s') >= 0);
		comp.getStyle().setUnderlined(style.indexOf('u') >= 0);
		comp.getStyle().setBold(style.indexOf('b') >= 0);
		comp.getStyle().setObfuscated(style.indexOf('o') >= 0);
		comp.getStyle().setColor(Formatting.WHITE);
		if (style.indexOf('w') >= 0)
			comp.getStyle().setColor(Formatting.WHITE); // not needed
		if (style.indexOf('y') >= 0)
			comp.getStyle().setColor(Formatting.YELLOW);
		if (style.indexOf('m') >= 0)
			comp.getStyle().setColor(Formatting.LIGHT_PURPLE);
		if (style.indexOf('r') >= 0)
			comp.getStyle().setColor(Formatting.RED);
		if (style.indexOf('c') >= 0)
			comp.getStyle().setColor(Formatting.AQUA);
		if (style.indexOf('l') >= 0)
			comp.getStyle().setColor(Formatting.GREEN);
		if (style.indexOf('t') >= 0)
			comp.getStyle().setColor(Formatting.BLUE);
		if (style.indexOf('f') >= 0)
			comp.getStyle().setColor(Formatting.DARK_GRAY);
		if (style.indexOf('g') >= 0)
			comp.getStyle().setColor(Formatting.GRAY);
		if (style.indexOf('d') >= 0)
			comp.getStyle().setColor(Formatting.GOLD);
		if (style.indexOf('p') >= 0)
			comp.getStyle().setColor(Formatting.DARK_PURPLE);
		if (style.indexOf('n') >= 0)
			comp.getStyle().setColor(Formatting.DARK_RED);
		if (style.indexOf('q') >= 0)
			comp.getStyle().setColor(Formatting.DARK_AQUA);
		if (style.indexOf('e') >= 0)
			comp.getStyle().setColor(Formatting.DARK_GREEN);
		if (style.indexOf('v') >= 0)
			comp.getStyle().setColor(Formatting.DARK_BLUE);
		if (style.indexOf('k') >= 0)
			comp.getStyle().setColor(Formatting.BLACK);
		return comp;
	}

	static String heatmap_color(double actual, double reference) {
		String color = "e";
		if (actual > 0.5D * reference)
			color = "y";
		if (actual > 0.8D * reference)
			color = "r";
		if (actual > reference)
			color = "m";
		return color;
	}

	static String creatureTypeColor(MobCategory type) {
		switch (type) {
			case MONSTER:
				return "n";
			case CREATURE:
				return "e";
			case AMBIENT:
				return "f";
			case WATER_CREATURE:
				return "v";
		}
		return "w";
	}

	private static Text _getChatComponentFromDesc(String message, Text previous_message) {
		String[] parts = message.split("\\s", 2);
		String desc = parts[0];
		String str = "";
		if (parts.length > 1)
			str = parts[1];
		if (desc.charAt(0) == '/') // deprecated
		{
			if (previous_message != null)
				previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
			return previous_message;
		}
		if (desc.charAt(0) == '?') {
			if (previous_message != null)
				previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
			return previous_message;
		}
		if (desc.charAt(0) == '!') {
			if (previous_message != null)
				previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
			return previous_message;
		}
		if (desc.charAt(0) == '^') {
			if (previous_message != null)
				previous_message.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Messenger.m(null, message.substring(1))));
			return previous_message;
		}
		Text txt = new LiteralText(str);
		return _applyStyleToTextComponent(txt, desc);
	}

	/*
	builds single line, multicomponent message, optionally returns it to sender, and returns as one chat messagge
	 */
	public static Text m(CommandSource receiver, Object... fields) {
		Text message = new LiteralText("");
		Text previous_component = null;
		for (Object o : fields) {
			if (o instanceof Text) {
				message.append((Text) o);
				previous_component = (Text) o;
				continue;
			}
			String txt = o.toString();
			Text comp = _getChatComponentFromDesc(txt, previous_component);
			if (comp != previous_component)
				message.append(comp);
			previous_component = comp;
		}
		if (receiver != null)
			receiver.sendMessage(message);
		return message;
	}

	static void print_server_message(MinecraftServer server, String message) {
		server.sendMessage(new LiteralText(message));
		Text txt = m(null, "gi " + message);
		for (PlayerEntity entityplayer : server.getPlayerManager().getAll()) {
			entityplayer.sendMessage(txt);
		}
	}
}
