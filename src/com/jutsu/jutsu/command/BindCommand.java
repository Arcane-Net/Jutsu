package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /jutsu bind. Extends {@link PKCommand}.
 */
public class BindCommand extends PKCommand {

	private final String abilityDoesntExist;
	private final String wrongNumber;
	private final String loadingInfo;
	private final String toggledElementOff;
	private final String noElement;
	private final String noElementAE;
	private final String noSubElement;
	private final String unbindable;

	public BindCommand() {
		super("bind", "/jutsu bind <Ability> [Slot]", ConfigManager.languageConfig.get().getString("Commands.Bind.Description"), new String[] { "bind", "b" });

		this.abilityDoesntExist = ConfigManager.languageConfig.get().getString("Commands.Bind.AbilityDoesntExist");
		this.wrongNumber = ConfigManager.languageConfig.get().getString("Commands.Bind.WrongNumber");
		this.loadingInfo = ConfigManager.languageConfig.get().getString("Commands.Bind.LoadingInfo");
		this.toggledElementOff = ConfigManager.languageConfig.get().getString("Commands.Bind.ElementToggledOff");
		this.noElement = ConfigManager.languageConfig.get().getString("Commands.Bind.NoElement");
		this.noElementAE = ConfigManager.languageConfig.get().getString("Commands.Bind.NoElementAE");
		this.noSubElement = ConfigManager.languageConfig.get().getString("Commands.Bind.NoSubElement");
		this.unbindable = ConfigManager.languageConfig.get().getString("Commands.Bind.Unbindable");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 1, 2) || !this.isPlayer(sender)) {
			return;
		}

		final CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		if (coreAbil == null || !coreAbil.isEnabled()) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.abilityDoesntExist.replace("{ability}", args.get(0)));
			return;
		} else if (coreAbil instanceof PassiveAbility || coreAbil instanceof ComboAbility || coreAbil.isHiddenAbility()) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.unbindable.replace("{ability}", args.get(0)));
			return;
		}

		// jutsu bind [Ability].
		if (args.size() == 1) {
			this.bind(sender, args.get(0), ((Player) sender).getInventory().getHeldItemSlot() + 1);
		}

		// jutsu bind [ability] [#].
		if (args.size() == 2) {
			try {
				this.bind(sender, args.get(0), Integer.parseInt(args.get(1)));
			} catch (final NumberFormatException ex) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			}
		}
	}

	private void bind(final CommandSender sender, final String ability, final int slot) {
		if (!(sender instanceof Player)) {
			return;
		} else if (slot < 1 || slot > 9) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			return;
		}

		final Ninja nPlayer = Ninja.getBendingPlayer((Player) sender);
		final CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (nPlayer == null) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.loadingInfo);
			return;
		} else if (coreAbil == null || !nPlayer.canBind(coreAbil)) {
			if (coreAbil != null && coreAbil.getElement() != Style.AVATAR && !nPlayer.hasElement(coreAbil.getElement())) {
				if (coreAbil.getElement() instanceof SubElement) {
					final SubElement sub = (SubElement) coreAbil.getElement();
					if (!nPlayer.hasElement(sub.getParentElement())) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ("AEIOUaeiou".indexOf(sub.getParentElement().getName().charAt(0)) > -1 ? this.noElementAE : this.noElement).replace("{style}", sub.getParentElement().getName() + sub.getParentElement().getType().getBender()));
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.noSubElement.replace("{subelement}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBending()));
					}
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ("AEIOUaeiou".indexOf(coreAbil.getElement().getName().charAt(0)) > -1 ? this.noElementAE : this.noElement).replace("{style}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBender()));
				}
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + super.noPermissionMessage);
			}
			return;
		} else if (!nPlayer.isElementToggled(coreAbil.getElement())) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggledElementOff);
		}

		nPlayer.bindAbility(ability, slot);
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("jutsu.command.bind") || !(sender instanceof Player)) {
			return new ArrayList<String>();
		}

		List<String> abilities = new ArrayList<String>();
		final Ninja nPlayer = Ninja.getBendingPlayer(sender.getName());
		if (args.size() == 0) {
			if (nPlayer != null) {
				for (final CoreAbility coreAbil : CoreAbility.getAbilities()) {
					if (!coreAbil.isHiddenAbility() && nPlayer.canBind(coreAbil) && !(coreAbil instanceof PassiveAbility || coreAbil instanceof ComboAbility) && !abilities.contains(coreAbil.getName())) {
						abilities.add(coreAbil.getName());
					}
				}
			}
		} else {
			abilities = Arrays.asList("123456789".split(""));
		}

		Collections.sort(abilities);
		return abilities;
	}
}
