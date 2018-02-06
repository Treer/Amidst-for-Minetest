package amidst.mojangapi.world.icon.type;

import amidst.documentation.ThreadSafe;
import amidst.mojangapi.world.coordinates.Coordinates;
import amidst.mojangapi.world.icon.WorldIconType;

@ThreadSafe
public interface WorldIconTypeProvider<T> {
	WorldIconType get(int x, int y, T additionalData);

	default WorldIconType get(Coordinates location, T additionalData) {
		return get((int) location.getX(), (int) location.getY(), additionalData);
	}
}
