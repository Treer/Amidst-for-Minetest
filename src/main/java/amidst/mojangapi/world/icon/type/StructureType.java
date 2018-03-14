package amidst.mojangapi.world.icon.type;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import amidst.documentation.Immutable;
import amidst.mojangapi.world.icon.WorldIconType;

@Immutable
public enum StructureType {
	// @formatter:off
	NETHER_FORTRESS     ("nether_fortress",   "Nether Fortress"),
	STRONGHOLD          ("stronghold",        "Stronghold"),
	JUNGLE              ("jungle",            "Jungle Temple"),
	DESERT              ("desert",            "Desert Temple"),
	VILLAGE             ("village",           "Village"),
	WITCH               ("witch",             "Witch Hut"),
	OCEAN_MONUMENT      ("ocean_monument",    "Ocean Monument"),
	WOODLAND_MANSION    ("woodland_mansion",  "Woodland Mansion"),
	IGLOO               ("igloo",             "Igloo"),
	MINESHAFT           ("mineshaft",         "Mineshaft");
	// @formatter:on

	private static final Map<String, StructureType> typeMap = createTypeMap();

	private static Map<String, StructureType> createTypeMap() {
		Map<String, StructureType> result = new HashMap<>();
		for (StructureType type : EnumSet.allOf(StructureType.class)) {
			result.put(type.getLabel().toLowerCase(), type);
		}
		return Collections.unmodifiableMap(result);
	}

	public static StructureType getByLabel(String label) {
		return typeMap.get(label.toLowerCase());
	}

	public static boolean exists(String name) {
		return typeMap.containsKey(name);
	}

	private final WorldIconType icon;

	private StructureType(String name, String label) {
		this.icon = new WorldIconType(name, label);
	}
	
	public String getName() {
		return icon.getName();
	}
	
	public String getLabel() {
		return icon.getLabel();
	}
	
	public WorldIconType getIconType() {
		return icon;
	}
}
