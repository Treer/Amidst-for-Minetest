package amidst.filter;

import amidst.mojangapi.world.coordinates.Region;

public class RegionInfo {
	private final Region.Box region;
	private final double cost;
	
	public RegionInfo(Region.Box region, double cost) {
		this.region = region;
		this.cost = cost;
	}
	
	public Region.Box getRegion() {
		return region;
	}
	
	public double getCost() {
		return cost;
	}
}
