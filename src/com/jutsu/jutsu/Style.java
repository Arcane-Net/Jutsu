package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.configuration.ConfigManager;

public class Style {

	public enum ElementType {
		BENDING("jutsu", "bender", "bend"), BLOCKING("blocking", "blocker", "block"), NO_SUFFIX("", "", "");

		private String jutsu;
		private String bender;
		private String bend;

		ElementType(final String jutsu, final String bender, final String bend) {
			this.jutsu = jutsu;
			this.bender = bender;
			this.bend = bend;
		}

		public String getBending() {
			return this.jutsu;
		}

		public String getBender() {
			return this.bender;
		}

		public String getBend() {
			return this.bend;
		}
	}

	private static final HashMap<String, Style> ALL_ELEMENTS = new HashMap<>(); // Must be initialized first.

	public static final Style AIR = new Style("Air");
	public static final Style WATER = new Style("Water");
	public static final Style EARTH = new Style("Earth");
	public static final Style FIRE = new Style("Fire");
	public static final Style CHI = new Style("Chi", ElementType.BLOCKING);
	public static final Style AVATAR = new Style("Avatar", null);
	public static final SubElement FLIGHT = new SubElement("Flight", AIR, ElementType.NO_SUFFIX);
	public static final SubElement SPIRITUAL = new SubElement("Spiritual", AIR, ElementType.NO_SUFFIX);
	public static final SubElement BLOOD = new SubElement("Blood", WATER);
	public static final SubElement HEALING = new SubElement("Healing", WATER, ElementType.NO_SUFFIX);
	public static final SubElement ICE = new SubElement("Ice", WATER);
	public static final SubElement PLANT = new SubElement("Plant", WATER);
	public static final SubElement LAVA = new SubElement("Lava", EARTH);
	public static final SubElement METAL = new SubElement("Metal", EARTH);
	public static final SubElement SAND = new SubElement("Sand", EARTH);
	public static final SubElement LIGHTNING = new SubElement("Lightning", FIRE);
	public static final SubElement COMBUSTION = new SubElement("Combustion", FIRE);
	public static final SubElement BLUE_FIRE = new SubElement("BlueFire", FIRE);

	private static final Style[] ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI, FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION, BLUE_FIRE };
	private static final Style[] MAIN_ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI };
	private static final SubElement[] SUB_ELEMENTS = { FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION, BLUE_FIRE };

	protected final String name;
	protected final ElementType type;
	protected final Plugin plugin;
	protected ChatColor color;
	protected ChatColor subColor;

	/**
	 * To be used when creating a new Style. Do not use for comparing
	 * Style.
	 *
	 * @param name Name of the new Style.
	 */
	public Style(final String name) {
		this(name, ElementType.BENDING, ProjectKorra.plugin);
	}

	/**
	 * To be used when creating a new Style. Do not use for comparing
	 * Style.
	 *
	 * @param name Name of the new Style.
	 * @param type ElementType specifies if its a regular style or chi style
	 *            style.
	 */
	public Style(final String name, final ElementType type) {
		this(name, type, ProjectKorra.plugin);
	}

	/**
	 * To be used when creating a new Style. Do not use for comparing
	 * Style.
	 *
	 * @param name Name of the new Style.
	 * @param type ElementType specifies if its a regular style or chi style
	 *            style.
	 * @param plugin The plugin that is adding the style.
	 */
	public Style(final String name, final ElementType type, final Plugin plugin) {
		this.name = name;
		this.type = type;
		this.plugin = plugin;
		ALL_ELEMENTS.put(name.toLowerCase(), this);
	}

	public String getPrefix() {
		String name_ = this.name;
		if (this instanceof SubElement) {
			name_ = ((SubElement) this).parentElement.name;
		}
		return this.getColor() + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes." + name_)) + " ";
	}

	public ChatColor getColor() {
		if (this.color == null) {
			FileConfiguration config = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get() : this.plugin.getConfig();
			String key = "Chat.Colors." + this.name;
			String value = config.getString(key);

			if (value == null && this instanceof SubElement && !(this instanceof MultiSubElement)) {
				this.color = ((SubElement) this).parentElement.getSubColor();
				return this.color;
			}

			try {
				this.color = ChatColor.of(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		return this.color != null ? this.color : ChatColor.WHITE;
	}

	public ChatColor getSubColor() {
		if (this.subColor == null) {
			FileConfiguration config = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get() : this.plugin.getConfig();
			String key = "Chat.Colors." + this.name + "Sub";
			String value = config.getString(key);

			if (value == null && this instanceof SubElement && !(this instanceof MultiSubElement)) {
				this.color = ((SubElement) this).parentElement.getSubColor();
				return this.color;
			}
			try {
				this.subColor = ChatColor.of(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return this.subColor != null ? this.subColor : ChatColor.WHITE;
	}

	void setColor(ChatColor color) {
		this.color = color;
	}

	void setSubColor(ChatColor color) {
		this.subColor = color;
	}

	public String getName() {
		return this.name;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public ElementType getType() {
		if (this.type == null) {
			return ElementType.NO_SUFFIX;
		}
		return this.type;
	}

	@Override
	public String toString() {
		return (this == Style.BLUE_FIRE) ? this.getColor() + "Blue Fire": this.getColor() + this.getName();
	}

	public static Style getElement(final String name) {
		if (name == null) {
			return null;
		}
		return ALL_ELEMENTS.get(name.toLowerCase());
	}

	/**
	 * Returns an array of all official and addon styles excluding
	 * subelements.
	 *
	 * @return Array of all official and addon styles.
	 */
	public static Style[] getAllElements() {
		final List<Style> ae = new ArrayList<Style>();
		ae.addAll(Arrays.asList(getMainElements()));
		for (final Style e : ALL_ELEMENTS.values()) {
			if (!ae.contains(e) && !(e instanceof SubElement)) {
				ae.add(e);
			}
		}
		return ae.toArray(new Style[ae.size()]);
	}

	/**
	 * Returns an array of all the official styles and subelements.
	 *
	 * @return Array of all official styles and subelements.
	 */
	public static Style[] getElements() {
		return ELEMENTS;
	}

	/**
	 * Returns an array of all the official styles.
	 *
	 * @return Array of all official styles.
	 */
	public static Style[] getMainElements() {
		return MAIN_ELEMENTS;
	}

	/**
	 * Returns an array of all the addon styles.
	 *
	 * @return Array of all addon styles.
	 */
	public static Style[] getAddonElements() {
		final List<Style> ae = new ArrayList<Style>();
		for (final Style e : getAllElements()) {
			if (!Arrays.asList(getMainElements()).contains(e)) {
				ae.add(e);
			}
		}
		ae.remove(Style.AVATAR);
		return ae.toArray(new Style[ae.size()]);
	}

	/**
	 * Returns all subelements, official and addon.
	 *
	 * @return Array of all the subelements.
	 */
	public static SubElement[] getAllSubElements() {
		final List<SubElement> se = new ArrayList<SubElement>();
		se.addAll(Arrays.asList(getSubElements()));
		for (final Style e : ALL_ELEMENTS.values()) {
			if (!se.contains(e) && e instanceof SubElement) {
				se.add((SubElement) e);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	/**
	 * Return official subelements.
	 *
	 * @return Array of official subelements.
	 */
	public static SubElement[] getSubElements() {
		return SUB_ELEMENTS;
	}

	/**
	 * Return all subelements belonging to a parent style.
	 *
	 * @param style
	 * @return Array of all subelements belonging to a parent style.
	 */
	public static SubElement[] getSubElements(final Style style) {
		final List<SubElement> se = new ArrayList<SubElement>();
		for (final SubElement sub : getAllSubElements()) {
			if (sub.getParentElement().equals(style) || (sub instanceof MultiSubElement && ((MultiSubElement) sub).isParentElement(style))) {
				se.add(sub);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	/**
	 * Returns an array of all the addon subelements.
	 *
	 * @return Array of all addon subelements.
	 */
	public static SubElement[] getAddonSubElements() {
		final List<SubElement> ae = new ArrayList<SubElement>();
		for (final SubElement e : getAllSubElements()) {
			if (!Arrays.asList(getSubElements()).contains(e)) {
				ae.add(e);
			}
		}
		return ae.toArray(new SubElement[ae.size()]);
	}

	/**
	 * Returns array of addon subelements belonging to a parent style.
	 *
	 * @param style
	 * @return Array of addon subelements belonging to a parent style.
	 */
	public static SubElement[] getAddonSubElements(final Style style) {
		final List<SubElement> se = new ArrayList<SubElement>();
		for (final SubElement sub : getAllSubElements()) {
			if ((sub.getParentElement().equals(style) || (sub instanceof MultiSubElement && ((MultiSubElement) sub).isParentElement(style))) && !Arrays.asList(getSubElements()).contains(sub)) {
				se.add(sub);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	public static Style fromString(final String style) {
		if (style == null || style.equals("")) {
			return null;
		}
		if (getElement(style) != null) {
			return getElement(style);
		}
		for (final String s : ALL_ELEMENTS.keySet()) {
			if (style.length() <= 1 && getElement(s) instanceof SubElement) {
				continue;
			}
			if (s.length() >= style.length()) {
				if (s.substring(0, style.length()).equalsIgnoreCase(style)) {
					return getElement(s);
				}
			}
		}
		return null;
	}

	public static class SubElement extends Style {

		protected Style parentElement;

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 *
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 */
		public SubElement(final String name, final Style parentElement) {
			this(name, parentElement, ElementType.BENDING, ProjectKorra.plugin);
		}

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 *
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 * @param type ElementType specifies if its a regular style or chi
		 *            style style.
		 */
		public SubElement(final String name, final Style parentElement, final ElementType type) {
			this(name, parentElement, type, ProjectKorra.plugin);
		}

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 *
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 * @param type ElementType specifies if its a regular style or chi
		 *            style style.
		 * @param plugin The plugin that is adding the style.
		 */
		public SubElement(final String name, final Style parentElement, final ElementType type, final Plugin plugin) {
			super(name, type, plugin);
			this.parentElement = parentElement;
		}

		public Style getParentElement() {
			return this.parentElement;
		}
	}

	public static class MultiSubElement extends SubElement {

		private Style[] parentElements;
		private final Set<Style> parentElementsSet;

		/**
		 * To be used when creating a new Style. Do not use for comparing
		 * Style.
		 *
		 * @param name Name of the new Style.
		 */
		public MultiSubElement(String name, Style... parentElements) {
			this(name, ElementType.NO_SUFFIX, parentElements);
		}

		/**
		 * To be used when creating a new Style. Do not use for comparing
		 * Style.
		 *
		 * @param name Name of the new Style.
		 * @param type ElementType specifies if its a regular style or chi style
		 */
		public MultiSubElement(String name, ElementType type, Style... parentElements) {
			this(name, type, ProjectKorra.plugin, parentElements);
		}

		/**
		 * To be used when creating a new Style. Do not use for comparing
		 * Style.
		 *
		 * @param name   Name of the new Style.
		 * @param type   ElementType specifies if its a regular style or chi style
		 *               style.
		 * @param plugin The plugin that is adding the style.
		 */
		public MultiSubElement(String name, ElementType type, Plugin plugin, Style... parentElements) {
			super(name, null, type, plugin);

			if (parentElements.length == 0) throw new IllegalArgumentException("MultiSubElement must have at least one parent style.");

			this.parentElements = parentElements;
			this.parentElementsSet = new HashSet<>(Lists.newArrayList(parentElements));

			this.parentElement = parentElements[0];
		}

		public Style[] getParentElements() {
			return this.parentElements;
		}

		public boolean isParentElement(Style style) {
			return this.parentElementsSet.contains(style);
		}
	}
}
