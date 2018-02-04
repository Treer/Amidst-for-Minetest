package amidst.filter.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import amidst.documentation.GsonObject;
import amidst.documentation.JsonField;
import amidst.filter.Criterion;
import amidst.mojangapi.world.coordinates.Coordinates;

@GsonObject
public abstract class CriterionJsonContainer extends CriterionJson {
	
	@JsonField(optional=true)
	public Coordinates center;

	@JsonField(optional=true)
	public Integer radius;
	
	@Override
	public Optional<Criterion<?>> validate(CriterionParseContext ctx) {
		if(center != null)
			ctx = ctx.withCenter(ctx.getCenter().add(center));
		
		if(radius != null) {
			if(radius <= 0)
				ctx.error("the radius must be strictly positive (is " + radius + ")");
			else ctx = ctx.withRadius(radius);
		}
		
		return super.validate(ctx);
	}
	
	protected static Optional<List<Criterion<?>>> validateList(List<CriterionJson> list, CriterionParseContext ctx, String listName) {	
		List<Criterion<?>> criteria = new ArrayList<>();
		boolean isOk = true;
		for(int i = 0; i < list.size(); i++) {
			Optional<Criterion<?>> res = list.get(i).validate(ctx.withName(listName + "[" + i + "]"));
			
			if(isOk && res.isPresent())
				criteria.add(res.get());
			else isOk = false;
		}
		return isOk ? Optional.of(criteria) : Optional.empty();
	}
}
