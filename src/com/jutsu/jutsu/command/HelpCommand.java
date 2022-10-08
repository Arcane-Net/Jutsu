package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /jutsu help. Extends {@link PKCommand}.
 */
public class HelpCommand extends PKCommand {

	private final String required;
	private final String optional;
	private final String properUsage;
	private final String learnMore;
	private final String air;
	private final String water;
	private final String earth;
	private final String fire;
	private final String chi;
	private final String avatar;
	private final String invalidTopic;
	private final String usage;

	public HelpCommand() {
		super("help", "/jutsu help <Page/Topic>", ConfigManager.languageConfig.get().getString("Commands.Help.Description"), new String[] { "help", "h" });

		this.required = ConfigManager.languageConfig.get().getString("Commands.Help.Required");
		this.optional = ConfigManager.languageConfig.get().getString("Commands.Help.Optional");
		this.properUsage = ConfigManager.languageConfig.get().getString("Commands.Help.ProperUsage");
		this.learnMore = ConfigManager.languageConfig.get().getString("Commands.Help.Style.LearnMore");
		this.air = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Air");
		this.water = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Water");
		this.earth = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Earth");
		this.fire = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Fire");
		this.chi = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Chi");
		this.avatar = ConfigManager.languageConfig.get().getString("Commands.Help.Style.Avatar");
		this.invalidTopic = ConfigManager.languageConfig.get().getString("Commands.Help.InvalidTopic");
		this.usage = ConfigManager.languageConfig.get().getString("Commands.Help.Usage");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		boolean firstMessage = true;

		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 0) {
			final List<String> strings = new ArrayList<String>();
			for (final PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && sender.hasPermission("jutsu.command." + command.getName())) {
					strings.add(command.getProperUse());
				}
			}
			
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);

			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", 1, false)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
			return;
		}

		final String arg = args.get(0).toLowerCase();

		if (this.isNumeric(arg)) {
			final List<String> strings = new ArrayList<String>();
			for (final PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			
			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", Integer.valueOf(arg), true)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
		} else if (instances.keySet().contains(arg)) {// jutsu help command.
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { // jutsu help elementcombo.
			sender.sendMessage(ChatColor.GOLD + this.properUsage.replace("{command1}", ChatColor.RED + "/jutsu display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/jutsu help <Combo Name>" + ChatColor.GOLD));
		} else if (Arrays.asList(Commands.passivealiases).contains(arg)) { // jutsu help elementpassive.
			sender.sendMessage(ChatColor.GOLD + this.properUsage.replace("{command1}", ChatColor.RED + "/jutsu display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/jutsu help <Passive Name>" + ChatColor.RED));
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility) && CoreAbility.getAbility(arg).isEnabled() && !CoreAbility.getAbility(arg).isHiddenAbility() || CoreAbility.getAbility(arg) instanceof PassiveAbility) { // jutsu help ability.
			final CoreAbility ability = CoreAbility.getAbility(arg);
			final ChatColor color = ability.getElement().getColor();

			if (ability instanceof AddonAbility) {
				if (ability instanceof PassiveAbility) {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon)");
				}

				sender.sendMessage(color + ability.getDescription());

				if (!ability.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
				}

				final AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
				sender.sendMessage(color + "- By: " + ChatColor.WHITE + abil.getAuthor());
				sender.sendMessage(color + "- Version: " + ChatColor.WHITE + abil.getVersion());
			} else {
				if (ability instanceof PassiveAbility) {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()));
				}

				sender.sendMessage(color + ability.getDescription());

				if (!ability.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
				}
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			sender.sendMessage(Style.AIR.getColor() + this.air.replace("/b display Air", Style.AIR.getSubColor() + "/b display Air" + Style.AIR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			sender.sendMessage(Style.WATER.getColor() + this.water.replace("/b display Water", Style.WATER.getSubColor() + "/b display Water" + Style.WATER.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			sender.sendMessage(Style.EARTH.getColor() + this.earth.replace("/b display Earth", Style.EARTH.getSubColor() + "/b display Earth" + Style.EARTH.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			sender.sendMessage(Style.FIRE.getColor() + this.fire.replace("/b display Fire", Style.FIRE.getSubColor() + "/b display Fire" + Style.FIRE.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			sender.sendMessage(Style.CHI.getColor() + this.chi.replace("/b display Chi", Style.CHI.getSubColor() + "/b display Chi" + Style.CHI.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.avataraliases).contains(arg)) {
			sender.sendMessage(Style.AVATAR.getColor() + this.avatar.replace("/b display Avatar", Style.AVATAR.getSubColor() + "/b display Avatar" + Style.AVATAR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else {
			// combos - handled differently because they're stored in CamelCase in ComboManager.
			for (final String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					final CoreAbility ability = CoreAbility.getAbility(combo);
					final ChatColor color = ability != null ? ability.getElement().getColor() : null;

					if (ability instanceof AddonAbility) {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Combo)");
						sender.sendMessage(color + ability.getDescription());

						if (!ability.getInstructions().isEmpty()) {
							sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
						}

						final AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
						sender.sendMessage(color + "- By: " + ChatColor.WHITE + abil.getAuthor());
						sender.sendMessage(color + "- Version: " + ChatColor.WHITE + abil.getVersion());
					} else {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Combo)");
						sender.sendMessage(color + ComboManager.getDescriptions().get(combo));
						sender.sendMessage(ChatColor.WHITE + this.usage + ComboManager.getInstructions().get(combo));
					}

					return;
				}
			}

			sender.sendMessage(ChatColor.RED + this.invalidTopic);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("jutsu.command.help")) {
			return new ArrayList<String>();
		}

		final List<String> list = new ArrayList<String>();
		for (final Style e : Style.getAllElements()) {
			list.add(e.getName());
		}

		final List<String> abils = new ArrayList<String>();
		for (final CoreAbility coreAbil : CoreAbility.getAbilities()) {
			if (!(sender instanceof Player) && (!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
				abils.add(coreAbil.getName());
			} else if (sender instanceof Player) {
				if ((!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
					abils.add(coreAbil.getName());
				}
			}
		}

		Collections.sort(abils);
		list.addAll(abils);
		return list;
	}
}
