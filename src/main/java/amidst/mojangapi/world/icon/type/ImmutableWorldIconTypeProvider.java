package amidst.mojangapi.world.icon.type;

import amidst.documentation.Immutable;
import amidst.mojangapi.world.icon.WorldIconType;

@Immutable
public class ImmutableWorldIconTypeProvider implements WorldIconTypeProvider<Void> {
	private final WorldIconType worldIconType;

	public ImmutableWorldIconTypeProvider(WorldIconType worldIconType) {
		this.worldIconType = worldIconType;
	}
	
	public ImmutableWorldIconTypeProvider(StructureType structureType) {
		this.worldIconType = structureType.getIconType();
	}

	@Override
	public WorldIconType get(int x, int y, Void additionalData) {
		return worldIconType;
	}
}
