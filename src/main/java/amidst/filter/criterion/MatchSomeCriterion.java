package amidst.filter.criterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import amidst.documentation.Immutable;
import amidst.filter.Criterion;
import amidst.filter.CriterionResult;
import amidst.filter.RegionInfo;
import amidst.filter.ResultsMap;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.coordinates.Region;
import amidst.util.TriState;

@Immutable
public class MatchSomeCriterion implements Criterion<MatchSomeCriterion.Result> {
	
	private final List<Criterion<?>> criteria;
	private final int minCriteria;
	
	public MatchSomeCriterion(List<Criterion<?>> list, int min) {
		if(min <= 0 && min > list.size())
			throw new IllegalArgumentException("min must be between 0 and list.size()");
		
		criteria = Collections.unmodifiableList(new ArrayList<>(list));
		this.minCriteria = min;
	}
	
	@Override
	public List<Criterion<?>> getChildren() {
		return criteria;
	}
	
	@Override
	public Result createResult() {
		return new Result();
	}
	
	public class Result implements CriterionResult {
		private List<Criterion<?>> undecided;
		private int matched;
		
		private Result() {
			this.undecided = new ArrayList<>(criteria);
			this.matched = 0;
		}
		
		private Result(Result r) {
			this.undecided = new ArrayList<>(r.undecided);
			this.matched = r.matched;
		}
		
		@Override
		public TriState hasMatched() {
			if(matched >= minCriteria)
				return TriState.TRUE;
			if(undecided.isEmpty())
				return TriState.FALSE;
			return TriState.UNKNOWN;
		}
		
		@Override
		public RegionInfo getNextRegionToCheck(ResultsMap map) {
			return undecided.stream()
				.map(c -> c.getNextRegionToCheck(map))
				.filter(i -> i != null)
				.min(Comparator.comparingDouble(RegionInfo::getCost))
				.orElse(null);
		}
		
		@Override
		public void checkRegionAndUpdate(ResultsMap map, World world, Coordinates offset, Region.Box region) {
			Iterator<Criterion<?>> iter = undecided.iterator();
			while(iter.hasNext()) {			
				TriState match = iter.next().checkRegion(map, world, offset, region);
				
				if(match != TriState.UNKNOWN)
					iter.remove();
				
				if(match == TriState.TRUE) {
					matched++;
					//We found enough criteria, we can stop
					if(matched >= minCriteria) {
						undecided.clear();
						break;
					}
					
				} else if(match == TriState.FALSE) {
					//We will never find enough criteria, we can stop
					int leftToMatch = minCriteria - matched;
					if(undecided.size() < leftToMatch) {
						undecided.clear();
						break;
					}
				}
			}
		}

		@Override
		public CriterionResult copy() {
			return new Result(this);
		}
		
	}
}
