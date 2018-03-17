package amidst.mojangapi.world.icon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import amidst.ResourceLoader;
import amidst.documentation.ThreadSafe;

@ThreadSafe
public enum WorldIconMarkers {
	;
	
	private static WorldIconImage BASE_MARKER = WorldIconImage.fromPixelTransparency(
			ResourceLoader.getImage("/amidst/gui/main/icon/marker.png"));
		
	private static Map<Color, WorldIconType> markersCache = Collections.synchronizedMap(new HashMap<>());

	public static WorldIconType makeMarkerType(Color color) {
		WorldIconType marker = markersCache.get(color);
		if(marker != null)
			return marker;
		
		BufferedImage image = makeColoredImage(BASE_MARKER.getImage(), color);
		
		WorldIconImage icon = new WorldIconImage(
				image,
				BASE_MARKER.getFrameOffsetX(),
				BASE_MARKER.getFrameOffsetY(),
				BASE_MARKER.getFrameWidth(),
				BASE_MARKER.getFrameHeight());
		
		marker = new WorldIconType("marker", "Marker", icon);
		markersCache.put(color, marker);
		return marker;
	}
	
	private static BufferedImage makeColoredImage(BufferedImage source, Color color) {
		int[] fromColor = getColorComponents(Color.WHITE);
		int[] toColor = getColorComponents(color);
		
		LookupOp op = new LookupOp(new LookupTable(0, toColor.length) {
			@Override
			public int[] lookupPixel(int[] src, int[] dest) {
				if (dest == null)
		            dest = new int[src.length];

		        int[] newColor = (Arrays.equals(src, fromColor) ? toColor : src);
		        System.arraycopy(newColor, 0, dest, 0, newColor.length);
		        return dest;
			}
		}, null);
		
		return op.filter(source, null);
	}
	
	private static int[] getColorComponents(Color color) {
		return new int[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
	}
}
