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
	private final boolean checkDistance;
	
	public BiomeCriterion(Region region, Biome biome, boolean check) {
		this.region = region;
		this.biome = biome;
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

		private Result() {
			super(region);
			found = null;
		}
		
		private Result(Result other) {
			super(other);
			found = other.found;
		}

		@Override
		protected boolean hasFound() {
			return found != null;
		}

		@Override
		protected void doCheckRegion(World world, Box region) {
			try {
				BiomeData data = world.getBiomeDataOracle().getBiomeData(region, true);
				
				Coordinates result = data.findFirst((x, y, b) -> {
					if(b != biome.getIndex())
						return null;
					
					Coordinates pos = region.getCorner().add(Coordinates.from(x, y, Resolution.QUARTER));
					if(checkDistance && !BiomeCriterion.this.region.contains(pos))
						return null;
					return pos;
				});
				
				if(result != null)
					found = result;
				
			} catch (MinecraftInterfaceException e) {
				AmidstLogger.error(e);
			}		
		}
		
		public void addItemToWorldResult(WorldFilterResult result) {
			if(found == null)
				return;
			
			result.getItemFor(found).setBiome(biome);
		}
		
		@Override
		public CriterionResult copy() {
			return new Result(this);
		}
		
	}

}
