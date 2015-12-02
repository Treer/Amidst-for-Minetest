package amidst.fragment.layer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import amidst.fragment.loader.FragmentLoader;
import amidst.map.Fragment;

public class LayerManager {
	private final List<AtomicBoolean> invalidatedLayers;
	private final List<LayerDeclaration> declarations;
	private final Iterable<FragmentLoader> loaders;

	public LayerManager(List<AtomicBoolean> invalidatedLayers,
			List<LayerDeclaration> declarations,
			Iterable<FragmentLoader> loaders) {
		this.invalidatedLayers = invalidatedLayers;
		this.declarations = declarations;
		this.loaders = loaders;
	}

	public List<LayerDeclaration> getLayerDeclarations() {
		return declarations;
	}

	public void clearInvalidatedLayers() {
		for (AtomicBoolean invalidatedLayer : invalidatedLayers) {
			invalidatedLayer.set(false);
		}
	}

	public void invalidateLayer(int layerId) {
		invalidatedLayers.get(layerId).set(true);
	}

	public void loadAll(Fragment fragment) {
		for (FragmentLoader loader : loaders) {
			loader.load(fragment);
		}
	}

	public void reloadInvalidated(Fragment fragment) {
		for (FragmentLoader loader : loaders) {
			if (isInvalidated(loader.getLayerDeclaration())) {
				loader.reload(fragment);
			}
		}
	}

	private boolean isInvalidated(LayerDeclaration layerDeclaration) {
		return invalidatedLayers.get(layerDeclaration.getLayerId()).get();
	}
}
