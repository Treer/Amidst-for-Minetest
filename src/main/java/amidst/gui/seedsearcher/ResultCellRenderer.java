package amidst.gui.seedsearcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ResultCellRenderer<T> implements ListCellRenderer<T> {
	
	private final Function<T, String> stringMapper;
	private final Function<T, Color> colorMapper;
	
	public ResultCellRenderer(Function<T, String> stringMapper) {
		this(stringMapper, null);
	}
	
	public ResultCellRenderer(Function<T, String> stringMapper, Function<T, Color> colorMapper) {
		this.stringMapper = stringMapper;
		this.colorMapper = colorMapper;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends T> list, T result,
			int index, boolean selected, boolean hasFocus) {
		
		JLabel label = new JLabel(stringMapper.apply(result));
		
		label.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray), 
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
			));
		
		if(colorMapper != null) {
			Color color = colorMapper.apply(result);
			label.setIcon(new ColorIcon(color, 13));
		}
		
		if(selected) {
			label.setOpaque(true);
			label.setBackground(new Color(160, 190, 255));
		}
		return label;
	}
	
	private static class ColorIcon implements Icon {
		private final int size;
		private final Color color;
		
		public ColorIcon(Color color, int size) {
			this.size = size;
			this.color = color;
		}

		public int getIconHeight() {
			return size;
		}

		@Override
		public int getIconWidth() {
			return size + 3;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if(color == null)
				return;
			
			Graphics2D g2d = (Graphics2D) g.create();

			g2d.setColor(color);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.fillOval(x, y, size, size);
			
			g2d.dispose();
		}
		
	}
}
