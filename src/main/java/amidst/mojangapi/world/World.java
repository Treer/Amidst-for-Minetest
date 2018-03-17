package amidst.mojangapi.world;

import java.util.List;
import java.util.function.Consumer;

import amidst.documentation.ThreadSafe;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.coordinates.Region;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.mojangapi.world.icon.producer.CachedWorldIconProducer;
import amidst.mojangapi.world.icon.producer.SpawnProducer;
import amidst.mojangapi.world.icon.producer.StaticWorldIconProducer;
import amidst.mojangapi.world.icon.producer.WorldIconProducer;
import amidst.mojangapi.world.icon.type.StructureType;
import amidst.mojangapi.world.oracle.BiomeDataOracle;
import amidst.mojangapi.world.oracle.CachedBiomeDataOracle;
import amidst.mojangapi.world.oracle.EndIsland;
import amidst.mojangapi.world.oracle.EndIslandOracle;
import amidst.mojangapi.world.oracle.SlimeChunkOracle;
import amidst.mojangapi.world.oracle.WorldSpawnOracle;
import amidst.mojangapi.world.player.MovablePlayerList;
import amidst.mojangapi.world.versionfeatures.VersionFeatures;

@ThreadSafe
public class World {
	private final Consumer<World> onDisposeWorld;

	private final WorldOptions worldOptions;
	private final MovablePlayerList movablePlayerList;
	private final RecognisedVersion recognisedVersion;
	private final VersionFeatures versionFeatures;

	private final BiomeDataOracle biomeDataOracle;
	private final EndIslandOracle endIslandOracle;
	private final SlimeChunkOracle slimeChunkOracle;
	private final WorldSpawnOracle worldSpawnOracle;
	private final CachedWorldIconProducer spawnProducer;
	private final CachedWorldIconProducer strongholdProducer;
	private final CachedWorldIconProducer playerProducer;
	private final WorldIconProducer<Void> villageProducer;
	private final WorldIconProducer<Void> templeProducer;
	private final WorldIconProducer<Void> mineshaftProducer;
	private final WorldIconProducer<Void> oceanMonumentProducer;
	private final WorldIconProducer<Void> woodlandMansionProducer;
	private final WorldIconProducer<Void> netherFortressProducer;
	private final WorldIconProducer<List<EndIsland>> endCityProducer;
	private final StaticWorldIconProducer specialIconsProducer;
	
	public World(
			Consumer<World> onDisposeWorld,
			WorldOptions worldOptions,
			MovablePlayerList movablePlayerList,
			RecognisedVersion recognisedVersion,
			VersionFeatures versionFeatures,
			BiomeDataOracle biomeDataOracle,
			EndIslandOracle endIslandOracle,
			SlimeChunkOracle slimeChunkOracle,
			WorldSpawnOracle worldSpawnOracle,
			CachedWorldIconProducer strongholdProducer,
			CachedWorldIconProducer playerProducer,
			WorldIconProducer<Void> villageProducer,
			WorldIconProducer<Void> templeProducer,
			WorldIconProducer<Void> mineshaftProducer,
			WorldIconProducer<Void> oceanMonumentProducer,
			WorldIconProducer<Void> woodlandMansionProducer,
			WorldIconProducer<Void> netherFortressProducer,
			WorldIconProducer<List<EndIsland>> endCityProducer,
			List<WorldIcon> specialWorldIcons) {
		this.onDisposeWorld = onDisposeWorld;
		this.worldOptions = worldOptions;
		this.movablePlayerList = movablePlayerList;
		this.recognisedVersion = recognisedVersion;
		this.versionFeatures = versionFeatures;
		this.biomeDataOracle = biomeDataOracle;
		this.endIslandOracle = endIslandOracle;
		this.slimeChunkOracle = slimeChunkOracle;
		this.worldSpawnOracle = worldSpawnOracle;
		this.spawnProducer = new SpawnProducer(worldSpawnOracle);
		this.strongholdProducer = strongholdProducer;
		this.playerProducer = playerProducer;
		this.villageProducer = villageProducer;
		this.templeProducer = templeProducer;
		this.mineshaftProducer = mineshaftProducer;
		this.woodlandMansionProducer = woodlandMansionProducer;
		this.oceanMonumentProducer = oceanMonumentProducer;
		this.netherFortressProducer = netherFortressProducer;
		this.endCityProducer = endCityProducer;
		this.specialIconsProducer = new StaticWorldIconProducer(specialWorldIcons);
	}
	
	public World cached() {
		//@formatter: off
		return new World(
			onDisposeWorld,
			worldOptions,
			movablePlayerList,
			recognisedVersion,
			versionFeatures,
			new CachedBiomeDataOracle(biomeDataOracle, Region.EMPTY),
			endIslandOracle,
			slimeChunkOracle,
			worldSpawnOracle,
			strongholdProducer,
			playerProducer,
			villageProducer,
			templeProducer,
			mineshaftProducer,
			oceanMonumentProducer,
			woodlandMansionProducer,
			netherFortressProducer,
			endCityProducer,
			specialIconsProducer.getWorldIcons()
		);
		//@formatter: on
	}

	public WorldOptions getWorldOptions() {
		return worldOptions;
	}

	public MovablePlayerList getMovablePlayerList() {
		return movablePlayerList;
	}

	public RecognisedVersion getRecognisedVersion() {
		return recognisedVersion;
	}

	public VersionFeatures getVersionFeatures() {
		return versionFeatures;
	}

	public BiomeDataOracle getBiomeDataOracle() {
		return biomeDataOracle;
	}

	public EndIslandOracle getEndIslandOracle() {
		return endIslandOracle;
	}

	public SlimeChunkOracle getSlimeChunkOracle() {
		return slimeChunkOracle;
	}
	
	public WorldSpawnOracle getSpawnOracle() {
		return worldSpawnOracle;
	}

	public WorldIconProducer<Void> getSpawnProducer() {
		return spawnProducer;
	}

	public WorldIconProducer<Void> getStrongholdProducer() {
		return strongholdProducer;
	}

	public WorldIconProducer<Void> getPlayerProducer() {
		return playerProducer;
	}

	public WorldIconProducer<Void> getVillageProducer() {
		return villageProducer;
	}

	public WorldIconProducer<Void> getTempleProducer() {
		return templeProducer;
	}

	public WorldIconProducer<Void> getMineshaftProducer() {
		return mineshaftProducer;
	}

	public WorldIconProducer<Void> getOceanMonumentProducer() {
		return oceanMonumentProducer;
	}

	public WorldIconProducer<Void> getNetherFortressProducer() {
		return netherFortressProducer;
	}

	public WorldIconProducer<List<EndIsland>> getEndCityProducer() {
		return endCityProducer;
	}
	
	public WorldIconProducer<Void> getSpecialIconsProducer() {
		return specialIconsProducer;
	}

	public WorldIconProducer<Void> getWoodlandMansionProducer() {
		return woodlandMansionProducer;
	}

	public WorldIcon getSpawnWorldIcon() {
		return spawnProducer.getFirstWorldIcon();
	}

	public List<WorldIcon> getStrongholdWorldIcons() {
		return strongholdProducer.getWorldIcons();
	}

	public List<WorldIcon> getPlayerWorldIcons() {
		return playerProducer.getWorldIcons();
	}
	
	public List<WorldIcon> getSpecialWorldIcons() {
		return specialIconsProducer.getWorldIcons();
	}

	public void reloadPlayerWorldIcons() {
		playerProducer.resetCache();
	}

	/**
	 * Unlocks the RunningLauncherProfile to allow the creation of another
	 * world. However, this does not actually prevent the usage of this world.
	 * If you keep using it, something will break.
	 */
	public void dispose() {
		onDisposeWorld.accept(this);
	}
	
	public WorldIconProducer<Void> getStructureProducer(StructureType type) {
		switch(type) {
		case MINESHAFT:
			return getMineshaftProducer();
			
		case NETHER_FORTRESS:
			return getNetherFortressProducer();
			
		case OCEAN_MONUMENT:
			return getOceanMonumentProducer();
			
		case STRONGHOLD:
			return getStrongholdProducer();
			
		case VILLAGE:
			return getVillageProducer();
			
		case WOODLAND_MANSION:
			return getWoodlandMansionProducer();

		case DESERT:
		case IGLOO:
		case JUNGLE:
		case WITCH:
			return getTempleProducer();
		}
		
		throw new IllegalArgumentException("unexpected structure type");
	}
}
