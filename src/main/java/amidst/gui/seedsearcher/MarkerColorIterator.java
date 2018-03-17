package amidst.gui.seedsearcher;

import java.awt.Color;
import java.util.Iterator;

public class MarkerColorIterator implements Iterator<Color> {
	
	private final float saturation;
	private final float brightness;
	
	private float offset = 0;
	private int curInTriplet = 0;
	
	private float multiplier = 2;
	private int curRoundSize = 0;
	private int curInRound = 0;
	
	public MarkerColorIterator(float saturation, float brightness) {
		this.saturation = saturation;
		this.brightness = brightness;
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Color next() {
		float hue = (offset + curInTriplet++)/3;
		if(curInTriplet == 3) {
			advanceOffset();
			curInTriplet = 0;
		}
		return new Color(Color.HSBtoRGB(hue, saturation, brightness));
	}
	
	private void advanceOffset() {
		offset = (offset + multiplier) % 1f;
		curInRound++;
		if(curInRound >= curRoundSize) {
			curRoundSize = Math.max(1, curRoundSize*2);
			curInRound = 0;
			multiplier /= 2;
			offset = multiplier/2;
		}
	}
	
}
