package amidst.filter.criterion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import amidst.filter.CriterionResult;
import amidst.filter.RegionInfo;
import amidst.filter.ResultsMap;
import amidst.filter.WorldFilter;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.coordinates.Region;
import amidst.util.TriState;

public abstract class SubRegionResult implements CriterionResult {
	//Regions to test, stored in order of increasing distance
	private LinkedHashMap<Region.Box, RegionInfo> regionsToTest;
	
	protected SubRegionResult(Region region) {
		regionsToTest = new LinkedHashMap<>();
		for(RegionInfo info: getSubRegions(region)) {
			regionsToTest.put(info.getRegion(), info);
		}
	}
	
	protected SubRegionResult(SubRegionResult other) {
		this.regionsToTest = new LinkedHashMap<>(other.regionsToTest);
	}

	@Override
	public TriState hasMatched() {
		boolean found = getFound() != null;
		
		if(doShortCircuit() && found)
			return TriState.TRUE;
		
		if(regionsToTest.isEmpty())
			return TriState.from(found);
		return TriState.UNKNOWN;
	}
	
	@Override
	public RegionInfo getNextRegionToCheck(ResultsMap map) {
		if(hasMatched() != TriState.UNKNOWN)
			return null;
		
		return regionsToTest.entrySet().iterator().next().getValue();
	}
	
	@Override
	public void checkRegionAndUpdate(ResultsMap map, World world, Coordinates offset, Region.Box region) {
		if(regionsToTest.remove(region) == null)
			return;
		
		doCheckRegion(world, offset, region);
		Coordinates found = getFound();
		
		if(found != null) {
			if(doShortCircuit()) {
				regionsToTest.clear();
				return;
			}
			
			//If the only regions left are further away than the best find we are done
			RegionInfo next = getNextRegionToCheck(map);
			if(next == null || next.getCost() >= found.getDistance(getCenter()))
				regionsToTest.clear();
		}
	}
	
	protected abstract Coordinates getFound();
	protected abstract void doCheckRegion(World world, Coordinates offset, Region.Box region);
	protected abstract Coordinates getCenter();
	protected abstract boolean doShortCircuit();


	private List<RegionInfo> getSubRegions(Region region) {
		Coordinates c1 = region.getCornerNW().snapTo(WorldFilter.REGION_SIZE);
		Coordinates c2 = region.getCornerSE().snapUpwardsTo(WorldFilter.REGION_SIZE);
		
		Coordinates center = getCenter();
		List<RegionInfo> list = new ArrayList<>();
		
		int size = WorldFilter.REGION_SIZE.getStep();
		for(int x = c1.getX(); x < c2.getX(); x += size) {
			for(int y = c1.getY(); y < c2.getY(); y += size) {
				Region.Box r = Region.box(x, y, size, size);
				if(region.intersectsWith(r))
					list.add(new RegionInfo(r, r.smallestDistanceTo(center)));
			}
		}
		
		list.sort(Comparator.comparing(RegionInfo::getCost));
		return list;
	}
	
}