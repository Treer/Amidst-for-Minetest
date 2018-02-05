package amidst.filter.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import amidst.documentation.GsonObject;
import amidst.documentation.JsonField;
import amidst.filter.Criterion;
import amidst.filter.criterion.MatchSomeCriterion;
import amidst.filter.criterion.BiomeCriterion;
import amidst.filter.criterion.StructureCriterion;
import amidst.mojangapi.world.biome.Biome;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.coordinates.Region;
import amidst.mojangapi.world.icon.type.DefaultWorldIconTypes;

@GsonObject
public class CriterionJsonBase extends CriterionJson {
	
	@JsonField(optional=true)
	public Coordinates center = Coordinates.origin();
	
	@JsonField(optional=true)
	public Integer radius;
	
	@JsonField(optional=true)
	public String shape = null;
	
	@JsonField(optional=true)
	public List<String> biomes = Collections.emptyList();
	
	@JsonField(optional=true)
	public boolean variants = false;

	@JsonField(optional=true)
	public List<String> structures = null;
	
	@JsonField(optional=true, require={"structures"})
	public ClusterInfo cluster;

	@GsonObject
	public static class ClusterInfo {
		@JsonField(optional=true)
		public int radius = 0;
		
		@JsonField()
		public int size;
	}
	
	
	@Override
	protected Optional<Criterion<?>> doValidate(CriterionParseContext ctx) {
		
		Coordinates center = ctx.getCenter().add(this.center);
		int radius = ctx.getRadius();
		if(this.radius != null) {
			radius = this.radius;
			if(radius <= 0)
				ctx.error("the radius must be strictly positive (is " + radius + ")");
		} else if(radius <= 0) {
			ctx.error("a radius must be specified");
		}

		if(shape == null)
			shape = ctx.getShape();

		//TODO implement clusters
		if(cluster != null)
			ctx.unsupportedAttribute("cluster");
		
		if(radius <= 0)
			ctx.error("the radius must be strictly positive (is " + radius + ")");
		
		Collection<Biome> biomeSet = getBiomeSet(ctx);
		Collection<DefaultWorldIconTypes> structSet = getStructureSet(ctx);
		if(biomeSet.isEmpty() && structSet.isEmpty())
			ctx.error("the biome list can't be empty if no structure is specified");
		
		
		boolean isChecked = false;
		boolean isSquare = false;
		switch(shape) {
		case "square":
			isChecked = true;
			isSquare = true;
			break;
			
		case "circle":
			isChecked = true;
			isSquare = false;
			break;
			
		case "square_nocheck":
			isChecked = false;
			isSquare = true;
			break;
			
		case "circle_nocheck":
			isChecked = false;
			isSquare = false;
			break;
			
		default:
			ctx.error("unknown shape " + shape);
		}			
					
		if(ctx.hasErrors())
			return Optional.empty();
		
		Region region = isSquare ? Region.box(center, radius) : Region.circle(center, radius);
		
		
		List<Criterion<?>> list = new ArrayList<>();
		
		if(structSet.isEmpty()) {
			for(Biome b: biomeSet)
				list.add(new BiomeCriterion(region, b, isChecked));
		} else {
			for(DefaultWorldIconTypes struct: structSet) {
				list.add(new StructureCriterion(region, struct, biomeSet, isChecked));
			}
		}
		
		if(list.size() == 1)
			return Optional.of(list.get(0));

		return Optional.of(new MatchSomeCriterion(list, 1));
	}
	

	private Collection<Biome> getBiomeSet(CriterionParseContext ctx) {
		Set<Biome> biomeSet = new HashSet<>();
		for(String biomeName: biomes) {
			if(Biome.exists(biomeName)) {
				Biome b = Biome.getByName(biomeName); 
				if(!biomeSet.add(b))
					ctx.error("duplicate biome " + b.getName());
				
				if(variants) {
					Biome spec = b.getSpecialVariant();
					if(b != spec && !biomeSet.add(spec))
						ctx.error("duplicate biome " + spec.getName());
				}
							
			} else ctx.error("the biome " + biomeName + " doesn't exist");
		}
		return biomeSet;
	}

	private Collection<DefaultWorldIconTypes> getStructureSet(CriterionParseContext ctx) {	
		Set<DefaultWorldIconTypes> structSet = new HashSet<>();
		if(structures == null)
			return structSet;
		
		for(String structName: this.structures) {
			DefaultWorldIconTypes struct = DefaultWorldIconTypes.getByName(structName.toLowerCase());
			if(struct == null)
				ctx.error("the structure " + structName + " doesn't exist");
			else if(StructureCriterion.UNSUPPORTED_STRUCTURES.contains(struct))
				ctx.error("the structure " + structName + " isn't supported");
			else structSet.add(struct);
		}
		return structSet;
	}
	
	public static class ClusterInfoDeserializer implements JsonDeserializer<ClusterInfo> {
		@Override
		public ClusterInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if(json.isJsonPrimitive()) {
				JsonObject obj = new JsonObject();
				obj.add("size", json);
				json = obj;
			}
			
			return context.deserialize(json, ClusterInfo.class);
		}
		
	}
}