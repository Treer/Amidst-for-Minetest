package amidst.gui.voronoi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import amidst.ResourceLoader;
import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.fragment.IBiomeDataOracle;
import amidst.gui.text.TextWindow;
import amidst.minetest.world.mapgen.IHistogram2D;
import amidst.minetest.world.mapgen.IHistogram2DTransformationProvider;
import amidst.minetest.world.mapgen.MapgenRelay;
import amidst.minetest.world.mapgen.MapgenUpdatedListener;
import amidst.minetest.world.mapgen.MinetestBiomeProfileImpl;
import amidst.minetest.world.oracle.MinetestBiomeDataOracle;
import amidst.settings.biomeprofile.BiomeProfile;
import amidst.settings.biomeprofile.BiomeProfileSelection;
import amidst.settings.biomeprofile.BiomeProfileUpdateListener;

public class VoronoiWindow implements BiomeProfileUpdateListener, MapgenUpdatedListener, ChangeListener, KeyEventDispatcher {

	private static final int ALTITUDESLIDER_DEFAULT_LOW    = -40;
	private static final int ALTITUDESLIDER_DEFAULT_HIGH   = 200;
	private static final int ALTITUDESLIDER_STARTING_VALUE = 10; // higher than beaches and oceans, so we start showing "normal" biomes.

	private static VoronoiWindow voronoiWindow = null;
	private BiomeProfileSelection biomeProfileSelection;
	private MapgenRelay mapgenRelay;

	private final JFrame windowFrame;
	private VoronoiPanel voronoiPanel;
	private JSlider   altitudeSlider;
	private JSlider   freqGraphSlider;
	private JLabel    graphHeading;
	private JSpinner  altitudeOffset;
	private JCheckBox option_showAxis;
	private JCheckBox option_showLabels;
	private JCheckBox option_showNodes;
	private JCheckBox option_showCoverage;

	private MinetestBiomeProfileImpl selectedProfile = null;
	private MinetestBiomeDataOracle  selectedMapgen  = null;


	@CalledOnlyBy(AmidstThread.EDT)
	private JFrame createWindowFrame(Component parent, int width, int height) {
		JFrame result = new JFrame();

		result.getContentPane().setLayout(new MigLayout(/*"debug"/**/));
		result.setSize(width, height);
		result.setLocationRelativeTo(parent);
		result.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				result.setVisible(false);
			}
		});

		this.option_showAxis     = createControl_VoronoiOption(VoronoiPanel.FLAG_SHOWAXIS);
		this.option_showNodes    = createControl_VoronoiOption(VoronoiPanel.FLAG_SHOWNODES);
		this.option_showLabels   = createControl_VoronoiOption(VoronoiPanel.FLAG_SHOWLABELS);
		this.option_showCoverage = createControl_VoronoiOption(VoronoiPanel.FLAG_SHOWCOVERAGE);

		// Place the controls in the window
		JLabel altOffsetLablel;
		result.add(this.voronoiPanel = new VoronoiPanel(),                 "grow, pushx, spany 2"); // the next row (which we span) will be "push", so don't do it here - the rest of this row needs to be thin
		result.add(new JLabel("Altitude"),                                 "center, wrap");
		result.add(this.altitudeSlider = createControl_AltitudeSlider(),   "grow, pushy, wrap");
		result.add(this.graphHeading = new JLabel(),                       "center");
		result.add(altOffsetLablel = new JLabel("Altitude offset:"),       "left, wrap");
		result.add(createControl_InfoButton(),                             "split 6");
		result.add(this.freqGraphSlider = createControl_FreqGraphSlider(), "gapx push push");
		result.add(this.option_showAxis,                                   "gapx push push");
		result.add(this.option_showLabels,                                 "gapx push push");
		result.add(this.option_showNodes,                                  "gapx push push");
		result.add(this.option_showCoverage,                               "gapx push push");
		result.add(altitudeOffset = createControl_AltitudeOffset(altOffsetLablel));

		return result;
	}

	/** Creates a slider which controls the altitude displayed in the Voronoi diagram */
	private JSlider createControl_AltitudeSlider() {
		JSlider result = new JSlider(
				JSlider.VERTICAL,
				ALTITUDESLIDER_DEFAULT_LOW,
				ALTITUDESLIDER_DEFAULT_HIGH,
				ALTITUDESLIDER_STARTING_VALUE
		);
		result.addChangeListener(this);
		result.setMajorTickSpacing(10);
		result.setMinorTickSpacing(5);
		result.setPaintTicks(true);
		result.setPaintLabels(true);
		return result;
	}

	/** Creates a button which shows the data in text form */
	private JButton createControl_InfoButton() {
		JButton result = new JButton();
		result.setToolTipText("Show data, legend, and notes");
		Image icon = ResourceLoader.getImage("/amidst/gui/main/dataicon.png");
		result.setIcon(new ImageIcon(icon));
		//infoButton.setBackground(Color.WHITE);
		//infoButton.setOpaque(false);
		Border line = new LineBorder(Color.BLACK);
		Border margin = new EmptyBorder(2, 2, 2, 2);
		Border compound = new CompoundBorder(line, margin);
		result.setBorder(compound);
		result.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent actionEvent) {
		    	InfoButtonClicked();
		    }
		});
		return result;
	}

	/** creates a slider for controlling the visibility of the frequency distribution graph */
	private JSlider createControl_FreqGraphSlider() {
		JSlider result = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		result.setToolTipText("<html>Hides or shows a graph of how temperature and humidity are distributed in the world.<br/>Click the \"Show data\" button on the left for more details.</html>");
		result.addChangeListener(this);
		result.setMajorTickSpacing(100);
		result.setMinorTickSpacing(25);
		result.setPaintTicks(true);
		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		JLabel sliderLabel = new JLabel("Show distribution");
        labels.put(0, new JLabel("")); // otherwise it allocates space at the end for "Show distribution"
        labels.put(50, sliderLabel);
        labels.put(100, new JLabel("")); // otherwise it allocates space at the end for "Show distribution"
        result.setLabelTable(labels);
        result.setPaintLabels(true);
        result.setPreferredSize(
    		new Dimension(
				sliderLabel.getPreferredSize().width,
				result.getPreferredSize().height
    		)
        );
        return result;
	}

	/** Creates a CheckBox for specifying one of the VoronoiPanel option flags */
	private JCheckBox createControl_VoronoiOption(int voronoi_panel_flag) {

		String caption, tooltip;
		switch (voronoi_panel_flag) {
		case VoronoiPanel.FLAG_SHOWAXIS:
			caption = "Show axes";
			tooltip = "Set whether temperature and humidity axis are displayed";
			break;
		case VoronoiPanel.FLAG_SHOWNODES:
			caption = "Show nodes";
			tooltip = "Set whether temperature and humidity positions of each biome are displayed";
			break;
		case VoronoiPanel.FLAG_SHOWLABELS:
			caption = "Show labels";
			tooltip = "Set whether the biomes and axis are labelled";
			break;
		default:
			caption = "Show %world covered";
			tooltip = "Set whether to show percentage of the world covered by each biome at this altitude";
			break;
		}
		JCheckBox result = new JCheckBox(caption);
		result.setToolTipText(tooltip);
		result.addChangeListener(this);
		return result;
	}

	/**
	 * Creates a numeric spinner so the range provided by the AltitudeSlider can be adjusted.
	 * @param label - the label to try to match the width of the JSpinner to
	 */
	private JSpinner createControl_AltitudeOffset(JLabel label) {
		JSpinner result = new JSpinner(new SpinnerNumberModel(0, Short.MIN_VALUE - ALTITUDESLIDER_DEFAULT_LOW, Short.MAX_VALUE - ALTITUDESLIDER_DEFAULT_HIGH, 100));
		result.setToolTipText("<html>Allows the range of the altitude slider to be adjusted.<br/>Allowing for example, to view the biomes at Floatlands altitudes</html>");
		result.addChangeListener(this);

		// Drop the spinner width a little
		JComponent field = ((JSpinner.DefaultEditor) result.getEditor());
		Dimension prefSize = new Dimension(
			(int)(label.getPreferredSize().width * 0.8f), // the 0.8 is because the scroll buttons will widen it.
			field.getPreferredSize().height
		);
	    field.setPreferredSize(prefSize);
	    return result;
	}


	@Override
	public void onBiomeProfileUpdate(BiomeProfile newBiomeProfile) {
		UpdateSelectedBiomeProfile(newBiomeProfile);
	}

	@Override
	public void onMapgenUpdated(IBiomeDataOracle biomeDataOracle) {
		UpdateSelectedMapgen(biomeDataOracle);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.VK_F5) {
			// TODO: Reload the biome from disk and update the Voronoi diagram
			// (Use case: biome-editing using a text editor, since Amidst doesn't provide
			// a Voronoi editor. Auto update when file updates would be even better)			
	    }
		// return false to pass the KeyEvent to the next KeyEventDispatcher in the chain
		return false;
	}	

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == altitudeOffset) {
			updateHeightSlider((int)altitudeOffset.getValue());
		}
		updateVoronoiDiagram();
	}

	private void InfoButtonClicked() {
		StringBuilder data = new StringBuilder();
		data.append("Biome profile: ");
		data.append(selectedProfile.getName());		
		if (selectedMapgen instanceof IHistogram2DTransformationProvider) {
			data.append(" with ");
			data.append(selectedMapgen.getName());
			data.append(" mapgen");
		}		
		data.append("\r\n\r\n");
		data.append("At altitude " + getAltitudeFromDialog() + ", the world is composed of the following biomes:\r\n\r\n");
		data.append(voronoiPanel.getDistributionData());
		data.append("\r\n");
		data.append("These values are not the area the biomes cover in the Voronoi diagram,\r\n"
				+ "they are the area of the Minetest world that will be covered by the biome\r\n"
				+ "at the given altitude.\r\n\r\n"
				+ "The surface alitudes most commonly occuring in the world will be determined\r\n"
				+ "by the choice of mapgen, so these value (above) should only be considered\r\n"
				+ "\"world-wide\" absolute values when biomes don't change with altitude, or the\r\n"
				+ "mapgen is flat.\r\n");

		data.append("\r\n\r\nDistribution legend:\r\n\r\n");

		data.append("Dragging the \"Show distribution\" slider causes colored rings to appear on the\r\n"
				+ "diagram. These indicate the frequently at which different temperature and\r\n"
				+ "humidity combinations occur in the world:\r\n"
				+ "\r\n"
				+ "    * Square grey outline — the technical limit of temperature and humidity.\r\n"
				+ "        The world does not contain temperature or humidity values in this\r\n"
				+ "        range.\r\n"
				+ "    * Red dotted ring — the practical limit of temperature and humidity. The\r\n"
				+ "        temperature and humidity in the world falls within this ring 99.99% of\r\n"
				+ "        the time.\r\n"
				+ "    * Red or purple solid rings - these indicate the four quartiles. 25% of\r\n"
				+ "        the world has a temperature and humidity falling outside the outermost\r\n"
				+ "        quartile ring. The next (purple) ring has 50% of the world inside it,\r\n"
				+ "        and 50% outside. The innermost red ring contains 25% of the world\r\n"
				+ "        inside it, with 25% between it and the middle ring, etc.\r\n"
				+ "    * Blue or purple solid rings - these are spaced at 10 percentile\r\n"
				+ "        intervals.\r\n"				
				+ "        Note the 50th percentile ring is drawn purple, but should also be\r\n"
				+ "        considered one of the blue rings.\r\n"
				+ "    * Red dot - the center of the distribution. This is the most common\r\n"
				+ "        temperature and humidity value.\r\n"
				+ "    * Purple solid ring - the median, or 50th percentile. 50% of the world is\r\n"
				+ "        inside this ring and 50% is outside.\n\n");

		TextWindow.showMonospace(windowFrame, "Voronoi diagram data", data.toString());
	}

	private void UpdateSelectedBiomeProfile(BiomeProfile newProfile) {

		MinetestBiomeProfileImpl minetestProfile = (newProfile instanceof MinetestBiomeProfileImpl) ? (MinetestBiomeProfileImpl)newProfile : null;

		boolean changed = (minetestProfile == null) ? (selectedProfile != null) : !minetestProfile.equals(selectedProfile);
		selectedProfile = minetestProfile;

		if (changed) updateVoronoiDiagram();
		this.windowFrame.setTitle(getTitle(selectedProfile, selectedMapgen));
	}
	
	private void UpdateSelectedMapgen(IBiomeDataOracle newMapgen) {

		MinetestBiomeDataOracle minetestMapgen = (newMapgen instanceof MinetestBiomeDataOracle) ? (MinetestBiomeDataOracle)newMapgen : null;

		boolean changed = (minetestMapgen == null) ? (selectedMapgen != null) : (selectedMapgen == null || (minetestMapgen.getClass() != selectedMapgen.getClass()));
		selectedMapgen = minetestMapgen;

		if (changed) updateVoronoiDiagram();
		this.windowFrame.setTitle(getTitle(selectedProfile, selectedMapgen));
	}
	
	private String getTitle(MinetestBiomeProfileImpl biome_profile, MinetestBiomeDataOracle mapgen) {
		String result = biome_profile == null ? "Biome profile Voronoi diagram" : "Voronoi diagram for " + biome_profile.getName();
		
		if (mapgen instanceof IHistogram2DTransformationProvider) {
			result += " with " + mapgen.getName() + " mapgen";
		}
		return result;
	}

	private void updateVoronoiDiagram() {
		EventQueue.invokeLater(
			() -> {
				int altitude = getAltitudeFromDialog();
				float freqGraphOpacity = freqGraphSlider.getValue() / 100f;
				voronoiPanel.Update(selectedProfile, selectedMapgen, altitude, freqGraphOpacity, getOptionFlagsFromDialog());
				graphHeading.setText("Biomes at altitude " + altitude);
			}
		);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void updateHeightSlider(int offset) {
		int oldHeightPosition = altitudeSlider.getValue() - altitudeSlider.getMinimum();
		altitudeSlider.setMinimum(ALTITUDESLIDER_DEFAULT_LOW + offset);
		altitudeSlider.setMaximum(ALTITUDESLIDER_DEFAULT_HIGH + offset);
		altitudeSlider.setValue(ALTITUDESLIDER_DEFAULT_LOW + offset + oldHeightPosition);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private int getAltitudeFromDialog() {
		return altitudeSlider.getValue();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void setOptionFlagsInDialog(int optionFlags) {
		option_showAxis.setSelected((optionFlags     & VoronoiPanel.FLAG_SHOWAXIS)     > 0);
		option_showNodes.setSelected((optionFlags    & VoronoiPanel.FLAG_SHOWNODES)    > 0);
		option_showLabels.setSelected((optionFlags   & VoronoiPanel.FLAG_SHOWLABELS)   > 0);
		option_showCoverage.setSelected((optionFlags & VoronoiPanel.FLAG_SHOWCOVERAGE) > 0);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private int getOptionFlagsFromDialog() {
		int result = 0;
		if (option_showAxis.isSelected())     result  |= VoronoiPanel.FLAG_SHOWAXIS;
		if (option_showNodes.isSelected())    result  |= VoronoiPanel.FLAG_SHOWNODES;
		if (option_showLabels.isSelected())   result  |= VoronoiPanel.FLAG_SHOWLABELS;
		if (option_showCoverage.isSelected()) result  |= VoronoiPanel.FLAG_SHOWCOVERAGE;
		return result;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void show(BiomeProfileSelection biomeProfileSelection, MapgenRelay mapgen_relay) {

		if (this.biomeProfileSelection != null) {
			this.biomeProfileSelection.removeUpdateListener(this);
		}
		if (this.mapgenRelay != null) {
			this.mapgenRelay.removeMapgenUpdatedListener(this);
		}

		this.biomeProfileSelection = biomeProfileSelection;
		this.mapgenRelay = mapgen_relay;

		BiomeProfile newProfile = null;
		if (biomeProfileSelection != null) {
			this.biomeProfileSelection.addUpdateListener(this);
			newProfile = this.biomeProfileSelection.getCurrentBiomeProfile();
		}
		IBiomeDataOracle newMapgen = null;
		if (mapgen_relay != null) {
			this.mapgenRelay.addMapgenUpdatedListener(this);
			newMapgen = this.mapgenRelay.getBiomeDataOracle();
		}
		

		UpdateSelectedBiomeProfile(newProfile);
		UpdateSelectedMapgen(newMapgen);
		this.windowFrame.setVisible(true);
	}

	/** Creates and displays the Voronoi diagram window */
	public static void showDiagram(Component parent, BiomeProfileSelection biome_profile_selection, MapgenRelay mapgen, IHistogram2D climate_histogram) {

		if (voronoiWindow == null) {
			voronoiWindow = new VoronoiWindow(parent);
		}
		SwingUtilities.invokeLater(() -> {
			voronoiWindow.voronoiPanel.setClimateHistogram(climate_histogram);
			voronoiWindow.show(biome_profile_selection, mapgen);
		});
	}

	/**
	 * Private constructor as this is a singleton. Invoke showDiagram() instead.
	 * @see showDiagram
	 */
	@CalledOnlyBy(AmidstThread.EDT)
	private VoronoiWindow(Component parent) {
		this.windowFrame = createWindowFrame(parent, 770, 760);
		setOptionFlagsInDialog(
				VoronoiPanel.FLAG_SHOWLABELS |
				VoronoiPanel.FLAG_SHOWAXIS   |
				VoronoiPanel.FLAG_SHOWNODES  |
				VoronoiPanel.FLAG_SHOWCOVERAGE
		);
		
		//KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
	}
}
