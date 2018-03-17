package amidst.mojangapi.world.biome;

import amidst.util.BiomeFunction;
import amidst.util.BiomePredicate;

/**
 * This class encapsulate a buffer containing the biome data of a
 * rectangular region.
 * 
 * The 2D data is stored in row-major order to be directly compatible
 * with the arrays handed by the {@link MinecraftInterface}.
 * 
 * This class supports the creation of views into sub-regions of the
 * buffer, without copying any data. However, these views don't own their
 * buffer, and thus will be invalidated when the original buffer is modified.
 * If you need to store the buffer somewhere, make sure to call {@link #makeOwned()}
 * to ensure that the buffer owns its contents.
 */
public class BiomeData {
	//We use an int array so we can directly reference the
	//Minecraft arrays, without any copying.
	private int[] dataInt;
	
	//This short array is used when we copy data, to decrease memory usage.
	private short[] dataShort;
	
	private int stride; //number of elems between two rows
	private int width;  //size of a row
	private int height; //number of rows
	private int offset; //offset before first element
	
	private boolean isOwned;
	
	private BiomeData(int[] dataInt, short[] dataShort, int width, int height, int stride, int offset, boolean isOwned) {
		this.dataInt = dataInt;
		this.dataShort = dataShort;
		this.width = width;
		this.height = height;
		this.stride = stride;
		this.offset = offset;
		this.isOwned = isOwned;
		
		if((dataInt == null) == (dataShort == null))
			throw new IllegalArgumentException("exactly one of dataInt and dataShort must be null");
		
		if(isOwned && dataInt != null)
			throw new IllegalArgumentException("an owned BiomeData must use a short[]");
		
		if(stride < width)
			throw new IllegalArgumentException("invalid stride");
		
		int size = dataInt == null ? dataShort.length : dataInt.length;
		if(offset < 0 || width < 0 || height < 0 || translateIndex(width-1, height-1) >= size)
			throw new IllegalArgumentException("invalid width/height/offset");
	}
	
	//Create a non-owned BiomeData viewing into data
	public BiomeData(int[] data, int width, int height) {		
		this(data, null, width, height, width, 0, false);
	}
	
	//Create an empty owned BiomeData
	public BiomeData(int width, int height) {
		this(null, new short[width*height], width, height, width, 0, true);
	}
	
	//Create an owned BiomeData
	public BiomeData(short[] data, int width, int height) {
		this(null, data, width, height, width, 0, true);
	}
	
	public BiomeData view() {
		return new BiomeData(dataInt, dataShort, width, height, stride, offset, false);
	}
	
	public BiomeData view(int offX, int offY, int w, int h) {
		if(offX < 0 || offY < 0 || offX+w > width || offY+h > height)
			throw new IllegalArgumentException("invalid offsets");
		
		return new BiomeData(dataInt, dataShort, w, h, stride, translateIndex(offX, offY), false);
	}
	
	public void copyFrom(BiomeData other) {
		if(isOwned) {
			if(other.getSize() > dataShort.length)
				dataShort = new short[other.getSize()];
			
			if(other.dataInt == null)
				fillData(other, other.dataShort);
			else fillData(other, other.dataInt);
			
		} else {
			width = other.width;
			height = other.height;
			stride = other.stride;
			offset = other.offset;
			dataInt = other.dataInt;
			dataShort = other.dataShort;
		}
	}
	
	public boolean isOwned() {
		return isOwned;
	}
	
	public void makeOwned() {
		if(isOwned)
			return;

		if(dataInt == null) {
			short[] old = dataShort;
			dataShort = new short[getWidth()*getHeight()];
			fillData(this, old);
			
		} else {
			dataShort = new short[getWidth()*getHeight()];
			fillData(this, dataInt);
			dataInt = null;
		}
		
		isOwned = true;
	}
	
	private void fillData(BiomeData other, short[] src) {
		width = other.getWidth();
		height = other.getHeight();
		
		if(other.stride == width) {
			//no gaps: we can do a single copy
			System.arraycopy(src, translateIndex(0, 0), dataShort, 0, getSize());
		} else {
			for(int j = 0; j < height; j++)
				System.arraycopy(src, translateIndex(0, j), dataShort, j*width, width);
		}
		
		stride = width;
		offset = 0;
	}
	
	private void fillData(BiomeData other, int[] src) {
		width = other.getWidth();
		height = other.getHeight();
		
		if(other.stride == width) {
			//no gaps: we can do a basic for loop
			int off = translateIndex(0, 0);
			int size = getSize();
			for(int i = 0; i < size; i++)
				dataShort[i] = (short) src[off+i];
		} else {
			for(int j = 0; j < height; j++) {
				int off = translateIndex(0, j);
				for(int i = 0; i < width; i++)
					dataShort[j*width+i] = (short) src[off+i];
			}
		}
		
		stride = width;
		offset = 0;
	}

	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSize() {
		return getWidth()*getHeight();
	}
	
	public short get(int x, int y) {
		int idx = translateIndex(x, y);
		return dataInt != null ? (short) dataInt[idx] : dataShort[idx];
	}
	
	public<T> T findFirst(BiomeFunction<T> fn) {
		int start = offset;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int idx = start+x;
				short biome = dataInt != null ? (short) dataInt[idx] : dataShort[idx];
				T obj = fn.apply(x, y, biome);
				if(obj != null)
					return obj;
			}
			start += stride;
		}
		
		return null;
	}
	
	public boolean checkAll(BiomePredicate pred) {
		int start = offset;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int idx = start+x;
				short biome = dataInt != null ? (short) dataInt[idx] : dataShort[idx];
				if(!pred.test(x, y, biome))
					return false;
			}
			start += stride;
		}

		return true;
	}
	
	private int translateIndex(int x, int y) {
		return offset + y*stride + x;
	}
	
}
