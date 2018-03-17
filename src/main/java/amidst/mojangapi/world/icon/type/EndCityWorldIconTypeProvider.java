package amidst.mojangapi.world.icon.type;

import java.util.List;

import amidst.mojangapi.world.icon.WorldIconType;
import amidst.mojangapi.world.oracle.EndIsland;

public class EndCityWorldIconTypeProvider implements WorldIconTypeProvider<List<EndIsland>> {

	public static final WorldIconType END_CITY_ICON = new WorldIconType("end_city", "Likely End City");
	public static final WorldIconType POSSIBLE_END_CITY_ICON = new WorldIconType("possible_end_city", "Possible End City");
	
	/**
	 * REQUIRED_INFLUENCE is a value between 0 and 80 that I'm finding by trial
	 * and error. If the island influence is 0 or higher then an End City can
	 * spawn, but they don't spawn unless all of the ground under then is at a
	 * higher y value than 60. Since we don't want to generate the land to
	 * discover the high areas, I'm using the island influence as proxy for how
	 * high the land might be.
	 */
	private static final int REQUIRED_INFLUENCE = 60;

	@Override
	public WorldIconType get(int chunkX, int chunkY, List<EndIsland> endIslands) {
		if ((chunkX * chunkX + chunkY * chunkY) > 4096) {
			return hasSuitableIslandFoundation(chunkX, chunkY, endIslands);
		} else {
			return null;
		}
	}

	/**
	 * If the influence is greater or equal to zero, Minecraft WILL attempt to
	 * build an End City, however if the ground at any of the corners of the end
	 * city is below height 60 then the End City will be aborted.
	 * 
	 * TODO: Use Amidst's ability to hook into the minecraft .jar file to get
	 * Minecraft to build just this single chunk so we can tell for certain
	 * whether an End City builds here. (If that's feasible)
	 * 
	 * In the meantime, fall back on the requiredInfluence heuristic
	 */
	private WorldIconType hasSuitableIslandFoundation(int chunkX, int chunkY, List<EndIsland> endIslands) {
		WorldIconType result = null;
		for (EndIsland island : endIslands) {
			float influence = island.influenceAtChunk(chunkX, chunkY);
			if (influence >= REQUIRED_INFLUENCE) {
				return END_CITY_ICON;
			} else if (influence >= 0) {
				result = POSSIBLE_END_CITY_ICON;
			}
		}
		return result;
	}
}
