package amidst.gui.main.menu;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import amidst.AmidstSettings;
import amidst.ResourceLoader;
import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.fragment.layer.LayerIds;
import amidst.gui.main.viewer.ViewerFacade;
import amidst.mojangapi.world.Dimension;
import amidst.settings.Setting;
import amidst.settings.Settings;

@NotThreadSafe
public class LayersMenu {
	private final JMenu menu;
	private final AmidstSettings settings;
	private final Setting<Dimension> dimensionSetting;
	private final Setting<Boolean> enableAllLayersSetting;
	private final List<JMenuItem> overworldMenuItems = new LinkedList<>();
	private final List<JMenuItem> endMenuItems = new LinkedList<>();
	private volatile ViewerFacade viewerFacade;
	private volatile boolean showEnableAllLayers;

	@CalledOnlyBy(AmidstThread.EDT)
	public LayersMenu(JMenu menu, AmidstSettings settings) {
		this.menu = menu;
		this.settings = settings;
		this.dimensionSetting = Settings.createWithListener(settings.dimension, this::createMenu);
		this.enableAllLayersSetting = Settings.createWithListener(settings.enableAllLayers, this::createMenu);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void init(ViewerFacade viewerFacade) {
		this.viewerFacade = viewerFacade;
		if (viewerFacade != null) {
			createMenu();
		} else {
			disable();
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createMenu() {
		menu.removeAll();
		overworldMenuItems.clear();
		endMenuItems.clear();
		showEnableAllLayers = false;
		createDimensionLayers(dimensionSetting.get());
		menu.setEnabled(true);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createDimensionLayers(Dimension dimension) {
		if (viewerFacade.hasLayer(LayerIds.MINETEST_OCEAN)) {
			createAllDimensions();
			menu.addSeparator();
			createMinetestLayers(dimension);
			createEnableAllLayersIfNecessary();
		
		} else if (viewerFacade.hasLayer(LayerIds.END_ISLANDS)) {
			createAllDimensions();
			menu.addSeparator();
			createOverworldAndEndLayers(dimension);
			createEnableAllLayersIfNecessary();
		} else if (!dimension.equals(Dimension.OVERWORLD)) {
			dimensionSetting.set(Dimension.OVERWORLD);
		} else {
			createAllDimensions();
			menu.addSeparator();
			createOverworldLayers(dimension);
			createEnableAllLayersIfNecessary();
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createEnableAllLayersIfNecessary() {
		if (enableAllLayersSetting.get() || showEnableAllLayers) {
			menu.addSeparator();
			createEnableAllLayers();
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createOverworldAndEndLayers(Dimension dimension) {
		// @formatter:off
		ButtonGroup group = new ButtonGroup();
		Menus.radio(   menu, dimensionSetting, group,     Dimension.OVERWORLD,                                      MenuShortcuts.DISPLAY_DIMENSION_OVERWORLD);
		createOverworldLayers(dimension);
		menu.addSeparator();
		Menus.radio(   menu, dimensionSetting, group,     Dimension.END,                                            MenuShortcuts.DISPLAY_DIMENSION_END);
		endLayer(      settings.showEndCities,            "End City Icons",         getIcon("end_city.png"),        MenuShortcuts.SHOW_END_CITIES, dimension, LayerIds.END_CITY);
		// @formatter:on
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createMinetestLayers(Dimension dimension) {
		// @formatter:off
		overworldLayer(settings.showMinetestRivers,       "Rivers",                 getIcon("rivers.png"),          MenuShortcuts.SHOW_MINETEST_RIVERS,    dimension, LayerIds.MINETEST_RIVER);
		overworldLayer(settings.showMinetestOceans,       "Oceans",                 getIcon("oceans.png"),          MenuShortcuts.SHOW_MINETEST_OCEANS,    dimension, LayerIds.MINETEST_OCEAN);
		overworldLayer(settings.showMinetestMountains,    "Mountains",              getIcon("mountains.png"),       MenuShortcuts.SHOW_MINETEST_MOUNTAINS, dimension, LayerIds.MINETEST_MOUNTAIN);
		// @formatter:on
	}
	
	@CalledOnlyBy(AmidstThread.EDT)
	private void createOverworldLayers(Dimension dimension) {
		// @formatter:off
		overworldLayer(settings.showSlimeChunks,          "Slime Chunks",           getIcon("slime.png"),           MenuShortcuts.SHOW_SLIME_CHUNKS,      dimension, LayerIds.SLIME);
		overworldLayer(settings.showSpawn,                "Spawn Location Icon",    getIcon("spawn.png"),           MenuShortcuts.SHOW_WORLD_SPAWN,       dimension, LayerIds.SPAWN);
		overworldLayer(settings.showStrongholds,          "Stronghold Icons",       getIcon("stronghold.png"),      MenuShortcuts.SHOW_STRONGHOLDS,       dimension, LayerIds.STRONGHOLD);
		overworldLayer(settings.showVillages,             "Village Icons",          getIcon("village.png"),         MenuShortcuts.SHOW_VILLAGES,          dimension, LayerIds.VILLAGE);
		overworldLayer(settings.showTemples,              "Temple/Witch Hut Icons", getIcon("desert.png"),          MenuShortcuts.SHOW_TEMPLES,           dimension, LayerIds.TEMPLE);
		overworldLayer(settings.showMineshafts,           "Mineshaft Icons",        getIcon("mineshaft.png"),       MenuShortcuts.SHOW_MINESHAFTS,        dimension, LayerIds.MINESHAFT);
		overworldLayer(settings.showOceanMonuments,       "Ocean Monument Icons",   getIcon("ocean_monument.png"),  MenuShortcuts.SHOW_OCEAN_MONUMENTS,   dimension, LayerIds.OCEAN_MONUMENT);
		overworldLayer(settings.showNetherFortresses,     "Nether Fortress Icons",  getIcon("nether_fortress.png"), MenuShortcuts.SHOW_NETHER_FORTRESSES, dimension, LayerIds.NETHER_FORTRESS);
		// @formatter:on
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createAllDimensions() {
		// @formatter:off
		Menus.checkbox(menu, settings.showGrid,           "Grid",                   getIcon("grid.png"),            MenuShortcuts.SHOW_GRID);
		Menus.checkbox(menu, settings.showPlayers,        "Player Icons",           getIcon("player.png"),          MenuShortcuts.SHOW_PLAYERS);
		// @formatter:on
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void createEnableAllLayers() {
		// @formatter:off
		Menus.checkbox(menu, enableAllLayersSetting,      "Enable All Layers",                                      MenuShortcuts.ENABLE_ALL_LAYERS);
		// @formatter:on
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void overworldLayer(
			Setting<Boolean> setting,
			String text,
			ImageIcon icon,
			MenuShortcut menuShortcut,
			Dimension dimension,
			int layerId) {
		overworldMenuItems.add(createLayer(setting, text, icon, menuShortcut, dimension, layerId));
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void endLayer(
			Setting<Boolean> setting,
			String text,
			ImageIcon icon,
			MenuShortcut menuShortcut,
			Dimension dimension,
			int layerId) {
		endMenuItems.add(createLayer(setting, text, icon, menuShortcut, dimension, layerId));
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JCheckBoxMenuItem createLayer(
			Setting<Boolean> setting,
			String text,
			ImageIcon icon,
			MenuShortcut menuShortcut,
			Dimension dimension,
			int layerId) {
		JCheckBoxMenuItem result = Menus.checkbox(menu, setting, text, icon, menuShortcut);
		if (!viewerFacade.calculateIsLayerEnabled(layerId, dimension, enableAllLayersSetting.get())) {
			result.setEnabled(false);
		}
		if (!viewerFacade.hasLayer(layerId)) {
			showEnableAllLayers = true;
		}
		return result;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void disable() {
		this.viewerFacade = null;
		menu.setEnabled(false);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private ImageIcon getIcon(String icon) {
		return new ImageIcon(ResourceLoader.getImage("/amidst/gui/main/icon/" + icon));
	}
}
