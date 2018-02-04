package amidst.filter.json;

import java.util.Optional;

import amidst.documentation.GsonObject;
import amidst.documentation.JsonField;
import amidst.filter.Criterion;
import amidst.filter.criterion.NegateCriterion;

/**
 * This class represent a criterion, as represented in the JSON structure.
 * The criterion is NOT validated, and may not be valid for semantic reasons.
 * 
 * It can be converted to an usable form with the method validate().
 */
@GsonObject
public abstract class CriterionJson {
	
	@JsonField(optional=true)
	public boolean negate = false;
	
	@JsonField(optional=true)
	public int score = 0;
	
	public CriterionJson() {}
	
	/**
	 * This method validate the criterion and convert it to a
	 * instance of the amidst.mojangapi.world.filter.Criterion.
	 * 
	 * The CriterionJsonContext is used to collect errors and to
	 * provide default values.
	 * 
	 * If any error occurs while validating a criterion, an empty
	 * Optional is returned.
	 */
	public Optional<Criterion<?>> validate(CriterionParseContext ctx) {		
		Optional<Criterion<?>> criterion;
		if(negate) {
			String ctxName = ctx.getName();
			criterion = doValidate(ctx.withName("!"))
						.map(c -> new NegateCriterion(ctxName, c));
		} else {
			criterion = doValidate(ctx);
		}
		
		//TODO implement score attribute
		if(score != 0)
			ctx.unsupportedAttribute("score");
		
		if(ctx.hasErrors())
			return Optional.empty();
		
		return criterion;
	}
	

	// This method takes care of subclass-specific validation.
	protected abstract Optional<Criterion<?>> doValidate(CriterionParseContext ctx);
}
