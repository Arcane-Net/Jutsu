package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /jutsu display. Extends {@link PKCommand}.
 */
public class DisplayCommand extends PKCommand {

	private final String noCombosAvailable;
	private final String noPassivesAvailable;
	private final String invalidArgument;
	private final String playersOnly;
	private final String noAbilitiesAvailable;
	private final String noBinds;

	private Set<Style> cachedPassiveElements;
	private Set<Style> cachedComboElements;

	public DisplayCommand() {
		super("display", "/jutsu display <Style>", ConfigManager.languageConfig.get().getString("Commands.Display.Description"), new String[] { "display", "dis", "d" });

		this.noCombosAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAvailable");
		this.noPassivesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAvailable");
		this.noAbilitiesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAvailable");
		this.invalidArgument = ConfigManager.languageConfig.get().getString("Commands.Display.InvalidArgument");
		this.playersOnly = ConfigManager.languageConfig.get().getString("Commands.Display.PlayersOnly");
		this.noBinds = ConfigManager.languageConfig.get().getString("Commands.Display.NoBinds");

		//1 tick later because commands are created before abilities are
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
			cachedPassiveElements = CoreAbility.getAbilities().stream().filter(ab -> ab instanceof PassiveAbility)
					.filter(Ability::isEnabled).map(Ability::getElement).collect(Collectors.toSet());
			cachedComboElements = CoreAbility.getAbilities().stream().filter(ab -> ab instanceof ComboAbility)
					.filter(ab -> !ab.isHiddenAbility()).filter(Ability::isEnabled).map(Ability::getElement).collect(Collectors.toSet());
		}, 1L);
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}

		// jutsu display [Style].
		if (args.size() == 1) {
			String elementName = args.get(0).toLowerCase().replace("jutsu", "");
			if (elementName.equalsIgnoreCase("wc")) {
				elementName = "watercombo";
			} else if (elementName.equalsIgnoreCase("ac")) {
				elementName = "aircombo";
			} else if (elementName.equalsIgnoreCase("ec")) {
				elementName = "earthcombo";
			} else if (elementName.equalsIgnoreCase("fc")) {
				elementName = "firecombo";
			} else if (elementName.equalsIgnoreCase("cc")) {
				elementName = "chicombo";
			} else if (elementName.equalsIgnoreCase("avc")) {
				elementName = "avatarcombo";
			} else if (elementName.equalsIgnoreCase("wp")) {
				elementName = "waterpassive";
			} else if (elementName.equalsIgnoreCase("ap")) {
				elementName = "airpassive";
			} else if (elementName.equalsIgnoreCase("ep")) {
				elementName = "earthpassive";
			} else if (elementName.equalsIgnoreCase("fp")) {
				elementName = "firepassive";
			} else if (elementName.equalsIgnoreCase("cp")) {
				elementName = "chipassive";
			} else if (elementName.equalsIgnoreCase("avp")) {
				elementName = "avatarpassive";
			}
			final Style style = Style.fromString(elementName.replace("combos", "").replace("combo", "").replace("passives", "").replace("passive", ""));
			// combos.
			if (elementName.contains("combo")) {
				if (style == null) {
					sender.sendMessage(ChatColor.BOLD + "Combos");

					for (final Style e : Style.getAllElements()) {
						final ChatColor color = e != null ? e.getColor() : null;
						final ArrayList<String> combos = ComboManager.getCombosForElement(e);

						for (final String comboAbil : combos) {
							ChatColor comboColor = color;
							if (!sender.hasPermission("jutsu.ability." + comboAbil)) {
								continue;
							}

							final CoreAbility coreAbil = CoreAbility.getAbility(comboAbil);
							if (coreAbil == null || coreAbil.isHiddenAbility()) {
								continue;
							}
							comboColor = coreAbil.getElement().getColor();

							String message = (comboColor + comboAbil);

							if (coreAbil instanceof AddonAbility) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				} else {
					final ChatColor color = style != null ? style.getColor() : null;
					final ArrayList<String> combos = ComboManager.getCombosForElement(style);

					if (combos.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, color + this.noCombosAvailable.replace("{style}", style.getName()));
						return;
					}

					sender.sendMessage(style.getColor() + (ChatColor.BOLD + style.getName()) + style.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Combos"));

					for (final String comboMove : combos) {
						ChatColor comboColor = color;
						if (!sender.hasPermission("jutsu.ability." + comboMove)) {
							continue;
						}

						final CoreAbility coreAbil = CoreAbility.getAbility(comboMove);
						if (coreAbil == null || coreAbil.isHiddenAbility()) {
							continue;
						}
						comboColor = coreAbil.getElement().getColor();

						String message = (comboColor + comboMove);

						if (coreAbil instanceof AddonAbility) {
							message += ChatColor.WHITE + (ChatColor.BOLD + "*");
						}

						sender.sendMessage(message);
					}
					return;
				}
				// passives.
			} else if (elementName.contains("passive")) {
				if (style == null) {
					sender.sendMessage(ChatColor.BOLD + "Passives");

					for (final Style e : Style.getAllElements()) {
						final ChatColor color = e != null ? e.getColor() : null;
						final Set<String> passives = PassiveManager.getPassivesForElement(e);

						for (final String passiveAbil : passives) {
							ChatColor passiveColor = color;
							if (!sender.hasPermission("jutsu.ability." + passiveAbil)) {
								continue;
							}

							final CoreAbility coreAbil = CoreAbility.getAbility(passiveAbil);
							if (coreAbil == null) {
								continue;
							}
							passiveColor = coreAbil.getElement().getColor();

							String message = (passiveColor + passiveAbil);

							if (coreAbil instanceof AddonAbility) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				}
				final ChatColor color = style != null ? style.getColor() : null;
				final Set<String> passives = PassiveManager.getPassivesForElement(style);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + this.noPassivesAvailable.replace("{style}", style.getName()));
					return;
				}

				sender.sendMessage(style.getColor() + (ChatColor.BOLD + style.getName()) + style.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Passives"));

				for (final String passiveAbil : passives) {
					ChatColor passiveColor = color;
					if (!sender.hasPermission("jutsu.ability." + passiveAbil)) {
						continue;
					}

					final CoreAbility coreAbil = CoreAbility.getAbility(passiveAbil);
					if (coreAbil == null) {
						continue;
					}
					passiveColor = coreAbil.getElement().getColor();

					sender.sendMessage(passiveColor + passiveAbil);
				}
				return;
			} else if (style != null) {
				if (!(style instanceof SubElement)) {
					this.displayElement(sender, style);
				} else {
					this.displaySubElement(sender, (SubElement) style);
				}
			}

			else {
				final StringBuilder styles = new StringBuilder(ChatColor.RED + this.invalidArgument);
				styles.append(ChatColor.WHITE + "\nElements: ");
				for (final Style e : Style.getAllElements()) {
					if (!(e instanceof SubElement)) {
						styles.append(e.getColor() + e.getName() + ChatColor.WHITE + " | ");
					}
				}
				sender.sendMessage(styles.toString());
				final StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (final SubElement e : Style.getAllSubElements()) {
					subelements.append(ChatColor.WHITE + "\n- " + e.getColor() + e.getName());
				}
				sender.sendMessage(subelements.toString());
			}
		}
		if (args.size() == 0) {
			// jutsu display.
			if (!(sender instanceof Player)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playersOnly);
				return;
			}
			this.displayBinds(sender);
		}
	}

	/**
	 * Displays the enabled moves for the given style to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param style The style to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Style style) {
		final List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(style);

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{style}", style.getColor() + style.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(style.getColor() + (ChatColor.BOLD + style.getName()) + style.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>(); // Some abilities have the same name. This prevents this from showing anything.
		for (final CoreAbility ability : abilities) {
			if (ability instanceof SubAbility || ability instanceof ComboAbility || ability.isHiddenAbility() || abilitiesSent.contains(ability.getName())) {
				continue;
			}

			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				String message = ability.getElement().getColor() + ability.getName();
				if (ability instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(ability.getName());
			}
		}


		if (cachedComboElements.contains(style)) sender.sendMessage(style.getSubColor() + "Combos: " + style.getColor() + "/jutsu display " + style.toString() + "Combos");
		if (cachedPassiveElements.contains(style)) sender.sendMessage(style.getSubColor() + "Passives: " + style.getColor() + "/jutsu display " + style.toString() + "Passives");
		for (final SubElement sub : Style.getSubElements(style)) {
			if (sender.hasPermission("jutsu." + style.getName().toLowerCase() + "." + sub.getName().toLowerCase())) {
				sender.sendMessage(sub.toString() + " abilities: " + style.getColor() + "/jutsu display " + sub.toString());
			}
		}
	}

	/**
	 * Displays the enabled moves for the given subelement to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param style The subelement to show the moves for
	 */
	private void displaySubElement(final CommandSender sender, final SubElement style) {
		final List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(style);

		if (abilities.isEmpty() && style != null) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{style}", style.getColor() + style.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(style.getColor() + (ChatColor.BOLD + style.getName()) + style.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>();
		for (final CoreAbility ability : abilities) {
			if (ability.isHiddenAbility() || abilitiesSent.contains(ability.getName())) {
				continue;
			} else if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				String message = style.getColor() + ability.getName();
				if (ability instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(ability.getName());
			}
		}
		if (cachedPassiveElements.contains(style))
		sender.sendMessage(style.getParentElement().getColor() + "Passives: " + style.getColor() + "/jutsu display " + style.getName() + "Passives");
	}

	/**
	 * Displays a Player's bound abilities.
	 *
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(final CommandSender sender) {
		Ninja nPlayer = Ninja.getBendingPlayer(sender.getName());
		final HashMap<Integer, String> abilities = nPlayer.getAbilities();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + this.noBinds);
			return;
		}

		sender.sendMessage(ChatColor.WHITE + (ChatColor.BOLD + "Abilities"));

		for (int i = 1; i <= 9; i++) {
			final String ability = abilities.get(i);
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil != null && !ability.equalsIgnoreCase("null")) {
				String message = i + ". " + coreAbil.getElement().getColor() + ability;

				if (coreAbil instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
			}
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("jutsu.command.display")) {
			return new ArrayList<String>();
		}
		final List<String> list = new ArrayList<String>();
		list.add("Air");
		list.add("Earth");
		list.add("Fire");
		list.add("Water");
		list.add("Chi");

		for (final Style e : Style.getAddonElements()) {
			list.add(e.getName());
		}

		list.add("Bloodbending");
		list.add("Combustion");
		list.add("Flight");
		list.add("Healing");
		list.add("Ice");
		list.add("Lava");
		list.add("Lightning");
		list.add("Metal");
		list.add("Plantbending");
		list.add("Sand");
		list.add("Spiritual");
		list.add("BlueFire");

		for (final SubElement se : Style.getAddonSubElements()) {
			list.add(se.getName());
		}

		list.add("AirCombos");
		list.add("EarthCombos");
		list.add("FireCombos");
		list.add("WaterCombos");
		list.add("ChiCombos");
		list.add("Avatar");

		list.add("AirPassives");
		list.add("EarthPassives");
		list.add("FirePassives");
		list.add("WaterPassives");
		list.add("ChiPassives");

		return list;
	}
}
