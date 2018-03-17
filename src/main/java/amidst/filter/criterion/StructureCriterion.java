package amidst.filter.criterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import amidst.documentation.Immutable;
import amidst.filter.Criterion;
import amidst.filter.CriterionResult;
import amidst.filter.WorldFilterResult;
import amidst.filter.WorldFilterResult.ResultItem;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.biome.Biome;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.coordinates.Region;
import amidst.mojangapi.world.coordinates.Region.Box;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.mojangapi.world.icon.producer.WorldIconProducer;
import amidst.mojangapi.world.icon.type.StructureType;

@Immutable
public class StructureCriterion implements Criterion<StructureCriterion.Result> {
			
	private final Region region;
	private final StructureType structure;
	private final List<Biome> biomes;
	private final boolean checkDistance;
	private final boolean shortCircuit;
	
	public StructureCriterion(Region region, StructureType structure, Collection<Biome> biomes, boolean shortCircuit, boolean check) {
		this.region = region;
		this.structure = structure;
		this.shortCircuit = shortCircuit;
		this.biomes = Collections.unmodifiableList(new ArrayList<>(biomes));
		checkDistance = check;
	}
	
	@Override
	public List<Criterion<?>> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public Result createResult() {
		return new Result();
	}
	
	public class Result extends SubRegionResult {
		Coordinates found;
		Biome foundBiome;
		double distSq = Double.POSITIVE_INFINITY;

		private Result() {
			super(region);
		}
		
		private Result(Result other) {
			super(other);
			found = other.found;
			foundBiome = other.foundBiome;
			distSq = other.distSq;
		}

		@Override
		protected Coordinates getFound() {
			return found;
		}
		
		@Override
		protected Coordinates getCenter() {
			return region.getCenter();
		}
		
		@Override
		protected boolean doShortCircuit() {
			return shortCircuit;
		}

		@Override
		protected void doCheckRegion(World world, Coordinates offset, Box region) {
			WorldIconProducer<Void> producer = world.getStructureProducer(structure);
			String label = structure.getIconType().getLabel();
			
			for(WorldIcon icon: producer.getAt(region.move(offset), null)) {
				if(!icon.getName().equals(label))
					continue;

				Coordinates realPos = icon.getCoordinates();
				Coordinates pos = realPos.substract(offset);
				if(checkDistance && !StructureCriterion.this.region.contains(pos))
					continue;
				
				if(shortCircuit) {
					if(checkForValidBiome(world, realPos))
						return;
					
				} else {
					double curDistSq = pos.getDistanceSq(region.getCenter());
					if(curDistSq < distSq && checkForValidBiome(world, realPos))
						distSq = curDistSq;
				}
			}
		}
		
		private boolean checkForValidBiome(World world, Coordinates pos) {
			if(biomes.isEmpty()) {
				found = pos;
				foundBiome = null;
				return true;
			}
				
			try {
				short bid = world.getBiomeDataOracle().getBiomeAt(pos.getX(), pos.getY());
				for(Biome b: biomes) {
					if(bid == b.getIndex()) {
						found = pos;
						foundBiome = b;
						return false;
					}
				}
			} catch (MinecraftInterfaceException e) {
				AmidstLogger.error(e);
			}
			return false;
		}
		
		@Override
		public void addItemToWorldResult(WorldFilterResult result, String goal) {
			if(found == null)
				return;
			
			ResultItem item = result.getItemFor(found, goal);
			item.structures.add(structure);
			
			if(foundBiome != null)
				item.setBiome(foundBiome);
		}
		
		@Override
		public CriterionResult copy() {
			return new Result(this);
		}
		
	}

}
