package amidst.mojangapi.world.icon;

import amidst.ResourceLoader;
import amidst.documentation.Immutable;
import amidst.mojangapi.world.Dimension;
import amidst.mojangapi.world.coordinates.Coordinates;

@Immutable
public class WorldIconType {
	
	private final String name;
	private final String label;
	private final WorldIconImage image;
	
	public WorldIconType(String name, String label, WorldIconImage image) {
		this.name = name;
		this.label = label;
		this.image = image;
	}

	public WorldIconType(String name, String label) {
		this.name = name;
		this.label = label;
		this.image = WorldIconImage.fromPixelTransparency(ResourceLoader.getImage(getFilename(name)));
	}
	
	public WorldIcon makeIcon(
			Coordinates coordinates,
			Dimension dimension,
			boolean displayDimension) {
		return new WorldIcon(coordinates, getLabel(), getImage(), dimension, displayDimension);
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public WorldIconImage getImage() {
		return image;
	}
	
	private static String getFilename(String name) {
		return "/amidst/gui/main/icon/" + name + ".png";
	}
}
