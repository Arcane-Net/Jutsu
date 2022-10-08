package com.projectkorra.projectkorra.command;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Abstract representation of a command executor. Implements {@link SubCommand}.
 *
 * @author kingbirdy
 *
 */
public abstract class PKCommand implements SubCommand {

	protected String noPermissionMessage, mustBePlayerMessage;

	/**
	 * The full name of the command.
	 */
	private final String name;
	/**
	 * The proper use of the command, in the form '/b {@link PKCommand#name
	 * name} arg1 arg2 ... '
	 */
	private final String properUse;
	/**
	 * A description of what the command does.
	 */
	private final String description;
	/**
	 * String[] of all possible aliases of the command.
	 */
	private final String[] aliases;
	/**
	 * List of all command executors which extends PKCommand
	 */
	public static Map<String, PKCommand> instances = new HashMap<String, PKCommand>();

	public PKCommand(final String name, final String properUse, final String description, final String[] aliases) {
		this.name = name;
		this.properUse = properUse;
		this.description = description;
		this.aliases = aliases;

		this.noPermissionMessage = ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.NoPermission");
		this.mustBePlayerMessage = ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.MustBePlayer");

		instances.put(name, this);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getProperUse() {
		return this.properUse;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String[] getAliases() {
		return this.aliases;
	}

	@Override
	public void help(final CommandSender sender, final boolean description) {
		String message = ConfigManager.languageConfig.get().getString("Commands.ProperUsage").replace("{command}", ChatColor.DARK_AQUA + this.properUse);
		sender.sendMessage(ChatColor.GOLD + message);
		if (description) {
			sender.sendMessage(ChatColor.YELLOW + this.description);
		}
	}

	/**
	 * Checks if the {@link CommandSender} has permission to execute the
	 * command. The permission is in the format 'jutsu.command.
	 * {@link PKCommand#name name}'. If not, they are told so.
	 *
	 * @param sender The CommandSender to check
	 * @return True if they have permission, false otherwise
	 */
	protected boolean hasPermission(final CommandSender sender) {
		if (sender.hasPermission("jutsu.command." + this.name)) {
			return true;
		} else {
			sender.sendMessage(this.noPermissionMessage);
			return false;
		}
	}

	/**
	 * Checks if the {@link CommandSender} has permission to execute the
	 * command. The permission is in the format 'jutsu.command.
	 * {@link PKCommand#name name}.extra'. If not, they are told so.
	 *
	 * @param sender The CommandSender to check
	 * @param extra The additional node to check
	 * @return True if they have permission, false otherwise
	 */
	protected boolean hasPermission(final CommandSender sender, final String extra) {
		if (sender.hasPermission("jutsu.command." + this.name + "." + extra)) {
			return true;
		} else {
			ChatUtil.sendBrandingMessage(sender, this.noPermissionMessage);
			return false;
		}
	}

	/**
	 * Checks if the argument length is within certain parameters, and if not,
	 * informs the CommandSender of how to correctly use the command.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param size The length of the arguments list
	 * @param min The minimum acceptable number of arguments
	 * @param max The maximum acceptable number of arguments
	 * @return True if min < size < max, false otherwise
	 */
	protected boolean correctLength(final CommandSender sender, final int size, final int min, final int max) {
		if (size < min || size > max) {
			this.help(sender, false);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if the CommandSender is an instance of a Player. If not, it tells
	 * them they must be a Player to use the command.
	 *
	 * @param sender The CommandSender to check
	 * @return True if sender instanceof Player, false otherwise
	 */
	protected boolean isPlayer(final CommandSender sender) {
		if (sender instanceof Player) {
			return true;
		} else {
			ChatUtil.sendBrandingMessage(sender, this.mustBePlayerMessage);
			return false;
		}
	}

	/**
	 * Returns a string representation of one of the five base styles,
	 * converted from any possible alias of that style, its combos, or its
	 * subelements.
	 *
	 * @param style The string to try and determine an style for
	 * @return The style associated with the input string, if found, or null
	 *         otherwise
	 */
	public String getElement(final String style) {
		if (Arrays.asList(Commands.firealiases).contains(style) || Arrays.asList(Commands.firecomboaliases).contains(style)) {
			return "fire";
		} else if (Arrays.asList(Commands.combustionaliases).contains(style)) {
			return "combustion";
		} else if (Arrays.asList(Commands.lightningaliases).contains(style)) {
			return "lightning";
		} else if (Arrays.asList(Commands.earthaliases).contains(style) || Arrays.asList(Commands.earthcomboaliases).contains(style)) {
			return "earth";
		} else if (Arrays.asList(Commands.metalbendingaliases).contains(style)) {
			return "metal";
		} else if (Arrays.asList(Commands.sandbendingaliases).contains(style)) {
			return "sand";
		} else if (Arrays.asList(Commands.lavabendingaliases).contains(style)) {
			return "lava";
		} else if (Arrays.asList(Commands.airaliases).contains(style) || Arrays.asList(Commands.aircomboaliases).contains(style)) {
			return "air";
		} else if (Arrays.asList(Commands.spiritualprojectionaliases).contains(style)) {
			return "spiritual";
		} else if (Arrays.asList(Commands.flightaliases).contains(style)) {
			return "flight";
		} else if (Arrays.asList(Commands.wateraliases).contains(style) || Arrays.asList(Commands.watercomboaliases).contains(style)) {
			return "water";
		} else if (Arrays.asList(Commands.healingaliases).contains(style)) {
			return "healing";
		} else if (Arrays.asList(Commands.bloodaliases).contains(style)) {
			return "blood";
		} else if (Arrays.asList(Commands.icealiases).contains(style)) {
			return "ice";
		} else if (Arrays.asList(Commands.plantaliases).contains(style)) {
			return "plant";
		} else if (Arrays.asList(Commands.chialiases).contains(style) || Arrays.asList(Commands.chicomboaliases).contains(style)) {
			return "chi";
		}
		return null;
	}

	/**
	 * Returns a boolean if the string provided is numerical.
	 *
	 * @param id
	 * @return boolean
	 */
	protected boolean isNumeric(final String id) {
		final NumberFormat formatter = NumberFormat.getInstance();
		final ParsePosition pos = new ParsePosition(0);
		formatter.parse(id, pos);
		return id.length() == pos.getIndex();
	}

	/**
	 * Returns a list for of commands for a page.
	 *
	 * @param entries
	 * @param title
	 * @param page
	 * @return
	 */
	protected List<String> getPage(final List<String> entries, final String title, int page, final boolean sort) {
		final List<String> strings = new ArrayList<String>();
		if (sort) {
			Collections.sort(entries);
		}

		if (page < 1) {
			page = 1;
		}
		if ((page * 8) - 8 >= entries.size()) {
			page = Math.round(entries.size() / 8) + 1;
			if (page < 1) {
				page = 1;
			}
		}
		strings.add(ChatColor.DARK_GRAY + "- [" + ChatColor.GRAY + page + "/" + (int) Math.ceil((entries.size() + .0) / (8 + .0)) + ChatColor.DARK_GRAY + "]");
		strings.add(title);
		if (entries.size() > ((page * 8) - 8)) {
			for (int i = ((page * 8) - 8); i < entries.size(); i++) {
				if (entries.get(i) != null) {
					strings.add(entries.get(i).toString());
				}
				if (i >= (page * 8) - 1) {
					break;
				}
			}
		}
		return strings;
	}

	/** Gets a list of valid arguments that can be used in tabbing. */
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		return new ArrayList<String>();
	}

	/**
	 * Create all command instances again. We do this so all commands pull their stuff from
	 * configs again
	 */
	public static void reloadCommands() {
		instances.clear();
		Commands.initializeCommands();
	}

	public List<Player> getOnlinePlayers(final CommandSender sender) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> !(sender instanceof Player) || p.canSee((Player) sender)).collect(Collectors.toList());
	}

	public List<String> getOnlinePlayerNames(final CommandSender sender) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> !(sender instanceof Player) || p.canSee((Player) sender)).map(Player::getName).collect(Collectors.toList());
	}
}
