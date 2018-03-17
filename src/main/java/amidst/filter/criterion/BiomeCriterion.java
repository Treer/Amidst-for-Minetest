package amidst.filter.criterion;

import java.util.Collections;
import java.util.List;

import amidst.documentation.Immutable;
import amidst.filter.Criterion;
import amidst.filter.CriterionResult;
import amidst.filter.WorldFilterResult;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.biome.Biome;
import amidst.mojangapi.world.biome.BiomeData;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.coordinates.Region;
import amidst.mojangapi.world.coordinates.Resolution;
import amidst.mojangapi.world.coordinates.Region.Box;

@Immutable
public class BiomeCriterion implements Criterion<BiomeCriterion.Result> {
	
	private final Region region;
	private final Biome biome;
	private final boolean shortCircuit;
	private final boolean checkDistance;
	
	public BiomeCriterion(Region region, Biome biome, boolean shortCircuit, boolean check) {
		this.region = region;
		this.biome = biome;
		this.shortCircuit = shortCircuit;
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
		double distSq = Double.POSITIVE_INFINITY;

		private Result() {
			super(region);
			found = null;
		}
		
		private Result(Result other) {
			super(other);
			found = other.found;
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
			BiomeData data;
			try {
				data = world.getBiomeDataOracle().getBiomeData(region.move(offset), true);
			} catch (MinecraftInterfaceException e) {
				AmidstLogger.error(e);
				return;
			}
			
			Coordinates center = BiomeCriterion.this.region.getCenter();
			Coordinates corner = region.getCorner();
			
			data.findFirst((x, y, b) -> {
				if(b != biome.getIndex())
					return null;
					
				Coordinates pos = corner.add(Coordinates.from(x, y, Resolution.QUARTER));
				if(checkDistance && !BiomeCriterion.this.region.contains(pos))
					return null;
				
				if(shortCircuit) {
					found = pos.add(offset);
					return found;
				}
				
				double curDistSq = pos.getDistanceSq(center);
				if(curDistSq < distSq) {
					found = pos.add(offset);
					distSq = curDistSq;
				}
				
				return null;
			});
		}
		
		@Override
		public void addItemToWorldResult(WorldFilterResult result, String goal) {
			if(found == null)
				return;
			
			result.getItemFor(found, goal).setBiome(biome);
		}
		
		@Override
		public CriterionResult copy() {
			return new Result(this);
		}
		
	}

}
