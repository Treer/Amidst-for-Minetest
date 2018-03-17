package amidst.mojangapi.world.icon.producer;

import java.util.ArrayList;
import java.util.List;

import amidst.mojangapi.world.icon.WorldIcon;
import java.util.Collections;

public class StaticWorldIconProducer extends CachedWorldIconProducer {
	private final List<WorldIcon> icons;
	
	public StaticWorldIconProducer(List<WorldIcon> icons) {
		if(icons == null)
			this.icons = Collections.emptyList();
		else this.icons = new ArrayList<>(icons);
	}

	@Override
	protected List<WorldIcon> doCreateCache() {
		return icons;
	}
}
