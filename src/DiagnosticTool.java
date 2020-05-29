/* Copyright 2009-2019 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.analysis.diagnostics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Settings;
import org.moeaframework.util.Localization;

/** The main window of the diagnostic tool. */
public class DiagnosticTool extends JFrame implements ListSelectionListener,
	ControllerListener {

	private static final long serialVersionUID= -8770087330810075627L;

	/** The localization instance for produce locale-specific strings. */
	private static Localization localization= Localization.getLocalization(
		DiagnosticTool.class);

	/** The controller which stores the underlying data model and notifies this diagnostic tool of
	 * any changes. */
	private Controller controller;

	/** The list of all available metrics. */
	private JList metricList;

	/** The underlying data model storing all available results. */
	private SortedListModel<ResultKey> resultListModel;

	/** The underlying data model storing all available metrics. */
	private SortedListModel<String> metricListModel;

	/** The container of all plots. */
	private JPanel chartContainer;

	/** The container of parameter plots. */
	private JPanel paramContainer;

	/** The tabbed container for plots and parameter visuals. */
	private JTabbedPane charts;

	/** The table for displaying all available results. */
	private JTable resultTable;

	/** The table model that allows {@code resultListModel} to be displayed in a table. */
	private AbstractTableModel resultTableModel;

	/** The button for selecting all results. */
	private JButton selectAll;

	/** The button for displaying a statistical comparison of selected results. */
	private JButton showStatistics;

	/** The control for setting the algorithm used by evaluation jobs. */
	private JComboBox algorithm;

	/** The control for setting the problem used by evaluation jobs. */
	private JComboBox problem;

	/** The control for setting the number of seeds used by evaluation jobs. */
	private JSpinner numberOfSeeds;

	/** The control for setting the number of evaluations used by evaluation jobs. */
	private JSpinner numberOfEvaluations;

	/** The control for setting the SBX rate used by evaluation jobs. */
	private JSpinner sbxRate;

	/** The control for setting the SBX distribution index used by evaluation jobs. */
	private JSpinner sbxDistributionIndex;

	/** The control for setting the PM rate used by evaluation jobs. */
	private JSpinner pmRate;

	/** The control for setting the PM distribution index used by evaluation jobs. */
	private JSpinner pmDistributionIndex;

	/** The control for setting the DE crossover rate used by evaluation jobs. */
	private JSpinner deCrossoverRate;

	/** The control for setting the DE step size used by evaluation jobs. */
	private JSpinner deStepSize;

	/** The control for setting the UM rate used by evaluation jobs. */
	private JSpinner umRate;

	/** The control for setting the SPX epsilon used by evaluation jobs. */
	private JSpinner spxEpsilon;

	/** The control for setting the number of parents used by SPX in evaluation jobs. */
	private JSpinner spxParents;

	/** The control for setting the number of offspring used by SPX in evaluation jobs. */
	private JSpinner spxOffspring;

	/** The control for setting the PCX eta used by evaluation jobs. */
	private JSpinner pcxEta;

	/** The control for setting the PCX zeta used by evaluation jobs. */
	private JSpinner pcxZeta;

	/** The control for setting the number of parents used by PCX in evaluation jobs. */
	private JSpinner pcxParents;

	/** The control for setting the number of offspring used by PCX in evaluation jobs. */
	private JSpinner pcxOffspring;

	/** The control for setting the UNDX zeta used by evaluation jobs. */
	private JSpinner undxZeta;

	/** The control for setting the UNDX eta used by evaluation jobs. */
	private JSpinner undxEta;

	/** The control for setting the number of parents used by UNDX in evaluation jobs. */
	private JSpinner undxParents;

	/** The control for setting the number of offspring used by UNDX in evaluation jobs. */
	private JSpinner undxOffspring;

	/** The button for starting a new evaluation job. */
	private JButton run;

	/** The button for canceling the current evaluation job. */
	private JButton cancel;

	/** The button for clearing all results contained in this diagnostic tool. */
	private JButton clear;

	/** The button for updating the parmeterization. */
	private JButton paramUpdate;

	/** The progress bar displaying the individual run progress. */
	private JProgressBar runProgress;

	/** The progress bar displaying the overall progress. */
	private JProgressBar overallProgress;

	/** The factory for the actions supported in this diagnostic tool window. */
	private ActionFactory actionFactory;

	/** Maintains a mapping from series key to paints displayed in the plot. */
	private PaintHelper paintHelper;

	/** Keeps track of the number of different parameterizations tested. */
	private Integer numberOfNewParams;

	/** Maps parameterization string to parameterization number. */
	private Map<String, Integer> paramMap;

	/** Current parameterization. */
	private Parameterization param;

	/** Constructs a new diagnostic tool window.
	 *
	 * @throws IOException */
	public DiagnosticTool() throws IOException {
		super(localization.getString("title.diagnosticTool"));

		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		initialize();
		layoutMenu();
		layoutComponents();
	}

	/** Initializes this window. This method is invoked by the constructor, and should not be
	 * invoked again.
	 *
	 * @throws IOException */
	protected void initialize() throws IOException {
		numberOfNewParams= 0;
		paramMap= new HashMap<>();
		param= new Parameterization(0.5, 20, 0.5, 80, 0.0, 0.1, 0.5, 0.5, 3, 2, 0.5, 0.5, 3, 2, 0.5,
			0.5, 3, 2);
		paramMap.put(param.toString(), numberOfNewParams);
		controller= new Controller(this);
		controller.addControllerListener(this);

		actionFactory= new ActionFactory(this, controller);
		resultListModel= new SortedListModel<>();
		metricListModel= new SortedListModel<>();
		metricList= new JList(metricListModel);
		paintHelper= new PaintHelper();
		chartContainer= new JPanel();
		paramContainer= new JPanel();

		paramContainer= (JPanel) new ParameterPane("test", param).getContentPane();

		charts= new JTabbedPane();
		charts.add("Diagnostic Plots", chartContainer);
		charts.add("Parameterization", paramContainer);

		metricList.addListSelectionListener(this);

		// initialize the table containing all available results
		resultTableModel= new AbstractTableModel() {

			private static final long serialVersionUID= -4148463449906184742L;

			@Override
			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return localization.getString("text.algorithm");
				case 1:
					return localization.getString("text.problem");
				case 2:
					return localization.getString("text.numberOfSeeds");
				case 3:
					return "Paramaterization";
				default:
					throw new IllegalStateException();
				}
			}

			@Override
			public int getColumnCount() {
				return 4;
			}

			@Override
			public int getRowCount() {
				return resultListModel.getSize();
			}

			@Override
			public Object getValueAt(int row, int column) {
				ResultKey key= resultListModel.getElementAt(row);

				switch (column) {
				case 0:
					return "Borg";
				case 1:
					return key.getProblem();
				case 2:
					return controller.get(key).size();
				case 3:
					return getParamNumber(key.getParameterization());
				default:
					throw new IllegalStateException();
				}
			}

		};

		resultTable= new JTable(resultTableModel);

		resultTable.getSelectionModel().addListSelectionListener(this);

		resultTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int index= resultTable.rowAtPoint(e.getPoint());
					boolean containsSet= false;

					if (index == -1) { return; }

					ResultKey key= resultListModel.getElementAt(index);

					// verify that at least one accumulator contains data
					for (Accumulator accumulator : controller.get(key)) {
						if (accumulator.keySet().contains(
							"Approximation Set")) {
							containsSet= true;
						}
					}

					if (!containsSet) { return; }

					JPopupMenu popupMenu= new JPopupMenu();

					popupMenu.add(new JMenuItem(
						actionFactory.getShowApproximationSetAction(
							resultListModel.getElementAt(index))));

					popupMenu.show(resultTable, e.getX(), e.getY());
				}
			}

		});

		selectAll= new JButton(actionFactory.getSelectAllAction(resultTable));
		showStatistics= new JButton(actionFactory.getShowStatisticsAction());

		// initialize the sorted list of algorithms
		Set<String> algorithmNames= new HashSet<>();
		algorithmNames.add("Borg");

		algorithm= new JComboBox(algorithmNames.toArray());

		// initialize the sorted list of problems
		Set<String> problemNames= new HashSet<>();

		for (String problem : Settings.getDiagnosticToolProblems()) {
			problemNames.add(problem);
		}

		for (String problem : Settings.getProblems()) {
			problemNames.add(problem);
		}

		List<String> sortedProblemNames= new ArrayList<>(problemNames);
		Collections.sort(sortedProblemNames);

		problem= new JComboBox(sortedProblemNames.toArray());

		// initialize miscellaneous components
		numberOfSeeds= new JSpinner(new SpinnerNumberModel(10, 1,
			Integer.MAX_VALUE, 10));
		numberOfEvaluations= new JSpinner(new SpinnerNumberModel(10000, 500,
			Integer.MAX_VALUE, 1000));

		// initialize parameter components
		sbxDistributionIndex= new JSpinner(new SpinnerNumberModel(20.0, 0.0,
			100.0, 10.0));
		sbxRate= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		pmDistributionIndex= new JSpinner(new SpinnerNumberModel(80.0, 0.0,
			100.0, 10.0));
		pmRate= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		deCrossoverRate= new JSpinner(new SpinnerNumberModel(0.0, 0.0,
			1.0, 0.1));
		deStepSize= new JSpinner(new SpinnerNumberModel(0.1, 0.0,
			1.0, 0.1));
		umRate= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		spxEpsilon= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		spxParents= new JSpinner(new SpinnerNumberModel(3, 2,
			10, 1));
		spxOffspring= new JSpinner(new SpinnerNumberModel(2, 2,
			10, 1));
		pcxEta= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		pcxZeta= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		pcxParents= new JSpinner(new SpinnerNumberModel(3, 2,
			10, 1));
		pcxOffspring= new JSpinner(new SpinnerNumberModel(2, 2,
			10, 1));
		undxZeta= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		undxEta= new JSpinner(new SpinnerNumberModel(0.5, 0.0,
			1.0, 0.1));
		undxParents= new JSpinner(new SpinnerNumberModel(3, 2,
			10, 1));
		undxOffspring= new JSpinner(new SpinnerNumberModel(2, 2,
			10, 1));

		run= new JButton(actionFactory.getRunAction());
		cancel= new JButton(actionFactory.getCancelAction());
		clear= new JButton(actionFactory.getClearAction());
		paramUpdate= new JButton(actionFactory.getParamUpdateAction());

		runProgress= new JProgressBar();
		overallProgress= new JProgressBar();

		algorithm.setEditable(true);
		problem.setEditable(true);
	}

	/** Lays out the menu on this window. This method is invoked by the constructor, and should not
	 * be invoked again. */
	protected void layoutMenu() {
		JMenu file= new JMenu(localization.getString("menu.file"));
		file.add(new JMenuItem(actionFactory.getSaveAction()));
		file.add(new JMenuItem(actionFactory.getLoadAction()));
		file.addSeparator();
		file.add(new JMenuItem(actionFactory.getExitAction()));

		JMenu view= new JMenu(localization.getString("menu.view"));
		JMenuItem individualTraces= new JRadioButtonMenuItem(
			actionFactory.getShowIndividualTracesAction());
		JMenuItem quantiles= new JRadioButtonMenuItem(
			actionFactory.getShowQuantilesAction());
		ButtonGroup traceGroup= new ButtonGroup();
		traceGroup.add(individualTraces);
		traceGroup.add(quantiles);
		view.add(individualTraces);
		view.add(quantiles);
		view.addSeparator();
		view.add(new JCheckBoxMenuItem(
			actionFactory.getShowLastTraceAction()));

		JMenu metrics= new JMenu(localization.getString("menu.collect"));
		metrics.add(new JMenuItem(
			actionFactory.getEnableAllIndicatorsAction()));
		metrics.add(new JMenuItem(
			actionFactory.getDisableAllIndicatorsAction()));
		metrics.addSeparator();
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeHypervolumeAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeGenerationalDistanceAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeInvertedGenerationalDistanceAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeSpacingAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeAdditiveEpsilonIndicatorAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeContributionAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeR1Action()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeR2Action()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeR3Action()));
		metrics.addSeparator();
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeEpsilonProgressAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeAdaptiveMultimethodVariationAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeAdaptiveTimeContinuationAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeElapsedTimeAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludePopulationSizeAction()));
		metrics.add(new JCheckBoxMenuItem(
			actionFactory.getIncludeApproximationSetAction()));

		JMenu help= new JMenu(localization.getString("menu.help"));
		help.add(new JMenuItem(actionFactory.getAboutDialogAction()));

		JMenu usage= new JMenu(actionFactory.getMemoryUsageAction());

		JMenuBar menuBar= new JMenuBar();
		menuBar.add(file);
		menuBar.add(view);
		menuBar.add(metrics);
		menuBar.add(help);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(usage);

		setJMenuBar(menuBar);
	}

	/** Lays out the components on this window. This method is invoked by the constructor, and
	 * should not be invoked again. */
	protected void layoutComponents() {
		GridBagConstraints label= new GridBagConstraints();
		label.gridx= 0;
		label.gridy= GridBagConstraints.RELATIVE;
		label.anchor= GridBagConstraints.EAST;
		label.insets= new Insets(0, 5, 5, 25);

		GridBagConstraints field= new GridBagConstraints();
		field.gridx= 1;
		field.gridy= GridBagConstraints.RELATIVE;
		field.fill= GridBagConstraints.HORIZONTAL;
		field.weightx= 1.0;
		field.insets= new Insets(0, 0, 1, 1);

		GridBagConstraints pfield= new GridBagConstraints();
		pfield.gridx= 1;
		pfield.gridy= GridBagConstraints.RELATIVE;
		pfield.fill= GridBagConstraints.HORIZONTAL;
		pfield.weightx= 1.0;
		pfield.insets= new Insets(0, 0, 0, 0);

		GridBagConstraints button= new GridBagConstraints();
		button.gridx= 0;
		button.gridwidth= 2;
		button.fill= GridBagConstraints.HORIZONTAL;
		button.insets= new Insets(0, 0, 2, 0);

		JPanel analysisPane= new JPanel(new FlowLayout(FlowLayout.CENTER));
		analysisPane.add(selectAll);
		analysisPane.add(showStatistics);

		JPanel resultPane= new JPanel(new BorderLayout());
		resultPane.setBorder(BorderFactory.createTitledBorder(
			localization.getString("text.displayedResults")));
		resultPane.add(new JScrollPane(resultTable), BorderLayout.CENTER);
		resultPane.add(analysisPane, BorderLayout.SOUTH);
		resultPane.setMinimumSize(new Dimension(100, 100));

		JPanel metricPane= new JPanel(new BorderLayout());
		metricPane.setBorder(BorderFactory.createTitledBorder(
			localization.getString("text.displayedMetrics")));
		metricPane.add(new JScrollPane(metricList), BorderLayout.CENTER);
		metricPane.setMinimumSize(new Dimension(100, 100));

		JPanel selectionPane= new JPanel(new GridLayout(2, 1));
		selectionPane.add(resultPane);
		selectionPane.add(metricPane);

		JPanel buttonPane= new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPane.add(run);
		buttonPane.add(cancel);
		buttonPane.add(clear);

		// control pane
		JPanel controlPane= new JPanel(new GridBagLayout());
		controlPane.setBorder(BorderFactory.createTitledBorder(
			localization.getString("text.controls")));
		controlPane.add(new JLabel(
			localization.getString("text.algorithm") + ":"), label);
		controlPane.add(algorithm, field);
		controlPane.add(new JLabel(
			localization.getString("text.problem") + ":"), label);
		controlPane.add(problem, field);
		controlPane.add(new JLabel(
			localization.getString("text.numberOfSeeds") + ":"), label);
		controlPane.add(numberOfSeeds, field);
		controlPane.add(new JLabel(
			localization.getString("text.numberOfEvaluations") + ":"),
			label);
		controlPane.add(numberOfEvaluations, field);
		controlPane.add(buttonPane, button);
		controlPane.add(new JPanel(), button);
		controlPane.add(new JLabel(
			localization.getString("text.runProgress") + ":"), label);
		controlPane.add(runProgress, field);
		controlPane.add(new JLabel(
			localization.getString("text.overallProgress") + ":"), label);
		controlPane.add(overallProgress, field);

		// parameter pane
		JPanel parameterPane= new JPanel(new GridBagLayout());
		JPanel nonvisualizedPane= new JPanel(new GridBagLayout());
		parameterPane
			.setBorder(BorderFactory.createTitledBorder("Parameterization - Visualized"));
		nonvisualizedPane
			.setBorder(BorderFactory.createTitledBorder("Parameterization - Not Visualized"));
		parameterPane.add(new JLabel(
			"SBX Rate" + ":"), label);
		parameterPane.add(sbxRate, pfield);
		parameterPane.add(new JLabel(
			"SBX Distribution Index" + ":"), label);
		parameterPane.add(sbxDistributionIndex, pfield);
		nonvisualizedPane.add(new JLabel(
			"PM Rate" + ":"), label);
		nonvisualizedPane.add(pmRate, pfield);
		parameterPane.add(new JLabel(
			"PM Distribution Index" + ":"), label);
		parameterPane.add(pmDistributionIndex, pfield);
		parameterPane.add(new JLabel(
			"DE Crossover Rate" + ":"), label);
		parameterPane.add(deCrossoverRate, pfield);
		parameterPane.add(new JLabel(
			"DE Step Size" + ":"), label);
		parameterPane.add(deStepSize, pfield);
		parameterPane.add(new JLabel(
			"UM Rate" + ":"), label);
		parameterPane.add(umRate, pfield);
		parameterPane.add(new JLabel(
			"SPX Epsilon" + ":"), label);
		parameterPane.add(spxEpsilon, pfield);
		parameterPane.add(new JLabel(
			"SPX Parents" + ":"), label);
		nonvisualizedPane.add(spxParents, pfield);
		nonvisualizedPane.add(new JLabel(
			"SPX Offspring" + ":"), label);
		nonvisualizedPane.add(spxOffspring, pfield);
		nonvisualizedPane.add(new JLabel(
			"PCX Eta" + ":"), label);
		parameterPane.add(pcxEta, pfield);
		parameterPane.add(new JLabel(
			"PCX Zeta" + ":"), label);
		parameterPane.add(pcxZeta, pfield);
		parameterPane.add(new JLabel(
			"PCX Parents" + ":"), label);
		nonvisualizedPane.add(pcxParents, pfield);
		nonvisualizedPane.add(new JLabel(
			"PCX Offspring" + ":"), label);
		nonvisualizedPane.add(pcxOffspring, pfield);
		nonvisualizedPane.add(new JLabel(
			"UNDX Zeta" + ":"), label);
		parameterPane.add(undxZeta, pfield);
		parameterPane.add(new JLabel(
			"UNDX Eta" + ":"), label);
		parameterPane.add(undxEta, pfield);
		nonvisualizedPane.add(new JLabel(
			"UNDX Parents" + ":"), label);
		nonvisualizedPane.add(undxParents, pfield);
		nonvisualizedPane.add(new JLabel(
			"UNDX Offspring" + ":"), label);
		nonvisualizedPane.add(undxOffspring, pfield);

		JPanel parambuttonPane= new JPanel(new FlowLayout(FlowLayout.CENTER));
		parambuttonPane.add(paramUpdate);
		parameterPane.add(parambuttonPane, button);

		// create tabbed pane
		JTabbedPane controlparameter= new JTabbedPane();
		controlparameter.add("Control", controlPane);
		controlparameter.add("Visualized", parameterPane);
		controlparameter.add("Not Visualized", nonvisualizedPane);

		JPanel controls= new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.add(controlparameter);
//		controls.add(controlPane);
//		controls.add(parameterPane);
		controls.add(selectionPane);
		controls.setMinimumSize(controlPane.getPreferredSize());
		controls.setPreferredSize(controlPane.getPreferredSize());

		JSplitPane splitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			controls, charts);
		splitPane.setDividerLocation(-1);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);

	}

	/** Updates the models underlying the GUI components as a result of model changes. This method
	 * must only be invoked on the event dispatch thread.
	 *
	 * @throws IOException */
	protected void updateModel() throws IOException {
		// determine selection mode
		List<ResultKey> selectedResults= getSelectedResults();
		List<String> selectedMetrics= getSelectedMetrics();
		boolean selectAllResults= false;
		boolean selectFirstMetric= false;

		if (selectedResults.size() == resultListModel.getSize()) {
			selectAllResults= true;
		}

		if (selectedMetrics.size() == 0 && metricListModel.getSize() == 0) {
			selectFirstMetric= true;
		}

		// update metric list and result table contents
		resultListModel.addAll(controller.getKeys());

		for (ResultKey key : controller.getKeys()) {
			for (Accumulator accumulator : controller.get(key)) {
				metricListModel.addAll(accumulator.keySet());
			}
		}

		// update metric list selection
		metricList.getSelectionModel().removeListSelectionListener(this);
		metricList.clearSelection();

		if (selectFirstMetric) {
			metricList.setSelectedIndex(0);
		} else {
			for (String metric : selectedMetrics) {
				int index= metricListModel.getIndexOf(metric);
				metricList.getSelectionModel().addSelectionInterval(index,
					index);
			}
		}

		metricList.getSelectionModel().addListSelectionListener(this);

		// update result table selection
		resultTable.getSelectionModel().removeListSelectionListener(this);
		resultTableModel.fireTableDataChanged();

		if (selectAllResults && selectedResults.size() < resultListModel.getSize()) {
			resultTable.getSelectionModel().addSelectionInterval(0,
				resultListModel.getSize() - 1);
		} else {
			for (ResultKey key : selectedResults) {
				int index= resultListModel.getIndexOf(key);
				resultTable.getSelectionModel().addSelectionInterval(index,
					index);
			}
		}

		resultTable.getSelectionModel().addListSelectionListener(this);

	}

	/** Returns the controller used by this diagnostic tool instance. This controller provides
	 * access to the underlying data model displayed in this window.
	 *
	 * @return the controller used by this diagnostic tool instance */
	public Controller getController() {
		return controller;
	}

	/** Returns the paint helper used by this diagnostic tool instance. This paint helper contains
	 * the mapping from series to paints displayed in this window.
	 *
	 * @return the paint helper used by this diagnostic tool instance */
	public PaintHelper getPaintHelper() {
		return paintHelper;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) { return; }

		controller.fireViewChangedEvent();
	}

	/** Invoked when the underlying data model is cleared, resulting in the GUI removing and
	 * resetting all components. This method must only be invoked on the event dispatch thread. */
	protected void clear() {
		resultListModel.clear();
		resultTable.getSelectionModel().clearSelection();
		resultTableModel.fireTableDataChanged();
		metricListModel.clear();
		metricList.getSelectionModel().clearSelection();
		paintHelper.clear();

		chartContainer.removeAll();
		chartContainer.revalidate();
		chartContainer.repaint();
	}

	/** Updates the chart layout when the user changes which metrics to plot. This method must only
	 * be invoked on the event dispatch thread. */
	protected void updateChartLayout() {
		chartContainer.removeAll();

		List<String> selectedMetrics= getSelectedMetrics();

		if (selectedMetrics.size() > 0) {
			if (selectedMetrics.size() <= 1) {
				chartContainer.setLayout(new GridLayout(1, 1));
			} else if (selectedMetrics.size() <= 2) {
				chartContainer.setLayout(new GridLayout(2, 1));
			} else if (selectedMetrics.size() <= 4) {
				chartContainer.setLayout(new GridLayout(2, 2));
			} else if (selectedMetrics.size() <= 6) {
				chartContainer.setLayout(new GridLayout(3, 2));
			} else {
				chartContainer.setLayout(new GridLayout(
					(int) Math.ceil(selectedMetrics.size() / 3.0), 3));
			}

			GridLayout layout= (GridLayout) chartContainer.getLayout();
			int spaces= layout.getRows() * layout.getColumns();

			for (int i= 0; i < Math.max(spaces, selectedMetrics.size()); i++ ) {
				if (i < selectedMetrics.size()) {
					chartContainer.add(createChart(selectedMetrics.get(i)));
				} else {
					chartContainer.add(new EmptyPlot(this));
				}
			}
		}

		chartContainer.revalidate();
	}

	/** Updates the chart layout when the user changes which parameterization to plot. This method
	 * must only be invoked on the event dispatch thread.
	 *
	 * @throws IOException */
	protected void updateParameterizationPlot() throws IOException {
		// update parameter pane
		charts.remove(paramContainer);
		param= getParameterization();
		paramContainer= (JPanel) new ParameterPane("test", param).getContentPane();
		charts.add("Parameterization", paramContainer);
	}

	/** Returns a list of the selected metrics.
	 *
	 * @return a list of the selected metrics */
	protected List<String> getSelectedMetrics() {
		List<String> selectedMetrics= new ArrayList<>();

		for (int index : metricList.getSelectedIndices()) {
			selectedMetrics.add(metricListModel.getElementAt(index));
		}

		return selectedMetrics;
	}

	/** Returns a list of the selected results.
	 *
	 * @return a list of the selected results */
	protected List<ResultKey> getSelectedResults() {
		List<ResultKey> selectedResults= new ArrayList<>();

		for (int index : resultTable.getSelectedRows()) {
			selectedResults.add(resultListModel.getElementAt(index));
		}

		return selectedResults;
	}

	/** Returns the algorithm selected in the run control pane.
	 *
	 * @return the algorithm selected for the next evaluation job */
	protected String getAlgorithm() {
		return (String) algorithm.getSelectedItem();
	}

	/** Returns the problem selected in the run control pane.
	 *
	 * @return the problem selected in the run control pane */
	protected String getProblem() {
		return (String) problem.getSelectedItem();
	}

	/** Returns the number of evaluations set in the run control pane.
	 *
	 * @return the number of evaluations set in the run control pane */
	protected int getNumberOfEvaluations() {
		return (Integer) numberOfEvaluations.getValue();
	}

	/** Returns the number of evaluations set in the run control pane.
	 *
	 * @return the number of evaluations set in the run control pane */
	protected Parameterization getParameterization() {
		return new Parameterization(
			getsbxRate(),
			getsbxDistributionIndex(),
			getpmRate(),
			getpmDistributionIndex(),
			getdeCrossoverRate(),
			getdeStepSize(),
			getumRate(),
			getspxEpsilon(),
			getspxParents(),
			getspxOffspring(),
			getpcxEta(),
			getpcxZeta(),
			getpcxParents(),
			getpcxOffspring(),
			getundxZeta(),
			getundxEta(),
			getundxParents(),
			getundxOffspring());
	}

	/** Returns the number of seeds set in the run control pane.
	 *
	 * @return the number of seeds set in the run control pane */
	protected int getNumberOfSeeds() {
		return (Integer) numberOfSeeds.getValue();
	}

	/** Returns the number of the current parameterization.
	 *
	 * @return the number of seeds set in the run control pane */
	protected int getParamNumber(String str) {
		// parameterization has been used
		if (paramMap.containsKey(str)) { return paramMap.get(str); }
		// new parameterization
		numberOfNewParams++ ;
		paramMap.put(str, numberOfNewParams);
		return numberOfNewParams;
	}

	/** Returns the SBX distribution index in the run control pane.
	 *
	 * @return the SBX distribution index set in the run control pane */
	protected double getsbxDistributionIndex() {
		return (Double) sbxDistributionIndex.getValue();
	}

	/** Returns the SBX rate in the run control pane.
	 *
	 * @return the SBX rate set in the run control pane */
	protected double getsbxRate() {
		return (Double) sbxRate.getValue();
	}

	/** Returns the PM rate in the run control pane.
	 *
	 * @return the PM rate set in the run control pane */
	protected double getpmRate() {
		return (Double) pmRate.getValue();
	}

	/** Returns the PM distribution index in the run control pane.
	 *
	 * @return the PM distribution index set in the run control pane */
	protected double getpmDistributionIndex() {
		return (Double) pmDistributionIndex.getValue();
	}

	/** Returns the DE crossover rate in the run control pane.
	 *
	 * @return the DE crossover rate set in the run control pane */
	protected double getdeCrossoverRate() {
		return (Double) deCrossoverRate.getValue();
	}

	/** Returns the DE step size in the run control pane.
	 *
	 * @return the DE step size in the run control pane */
	protected double getdeStepSize() {
		return (Double) deStepSize.getValue();
	}

	/** Returns the UM rate in the run control pane.
	 *
	 * @return the UM rate in the run control pane */
	protected double getumRate() {
		return (Double) umRate.getValue();
	}

	/** Returns the SPX epsilon in the run control pane.
	 *
	 * @return the SPX epsilon in the run control pane */
	protected double getspxEpsilon() {
		return (Double) spxEpsilon.getValue();
	}

	/** Returns the SPX number of parents in the run control pane.
	 *
	 * @return the SPX number of parents in the run control pane */
	protected int getspxParents() {
		return (Integer) spxParents.getValue();
	}

	/** Returns the SPX number of offspring in the run control pane.
	 *
	 * @return the SPX number of offspring in the run control pane */
	protected int getspxOffspring() {
		return (Integer) spxOffspring.getValue();
	}

	/** Returns the PCX eta in the run control pane.
	 *
	 * @return the PCX eta in the run control pane */
	protected double getpcxEta() {
		return (Double) pcxEta.getValue();
	}

	/** Returns the PCX zeta in the run control pane.
	 *
	 * @return the PCX zeta in the run control pane */
	protected double getpcxZeta() {
		return (Double) pcxZeta.getValue();
	}

	/** Returns the PCX number of parents in the run control pane.
	 *
	 * @return the PCX number of parents in the run control pane */
	protected int getpcxParents() {
		return (Integer) pcxParents.getValue();
	}

	/** Returns the PCX number of offspring in the run control pane.
	 *
	 * @return the PCX number of offspring in the run control pane */
	protected int getpcxOffspring() {
		return (Integer) pcxOffspring.getValue();
	}

	/** Returns the UNDX zeta in the run control pane.
	 *
	 * @return the UNDX zeta in the run control pane */
	protected double getundxZeta() {
		return (Double) undxZeta.getValue();
	}

	/** Returns the UNDX eta in the run control pane.
	 *
	 * @return the UNDX eta in the run control pane */
	protected double getundxEta() {
		return (Double) undxEta.getValue();
	}

	/** Returns the UNDX number of parents in the run control pane.
	 *
	 * @return the UNDX number of parents in the run control pane */
	protected int getundxParents() {
		return (Integer) undxParents.getValue();
	}

	/** Returns the UNDX number of offspring in the run control pane.
	 *
	 * @return the UNDX number of offspring in the run control pane */
	protected int getundxOffspring() {
		return (Integer) undxOffspring.getValue();
	}

	/** Creates and returns the GUI component for plotting the specified metric.
	 *
	 * @param metric the metric to plot
	 * @return the GUI component for plotting the specified metric */
	protected ResultPlot createChart(String metric) {
		if (metric.equals("Approximation Set")) {
			return new ApproximationSetPlot(this, metric);
		} else {
			return new LinePlot(this, metric);
		}
	}

	@Override
	public void controllerStateChanged(ControllerEvent event) {
		if (event.getType().equals(ControllerEvent.Type.MODEL_CHANGED)) {
			if (controller.getKeys().isEmpty()) {
				clear();
			} else {
				try {
					updateModel();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (event.getType().equals(
			ControllerEvent.Type.PROGRESS_CHANGED)) {
			runProgress.setValue(controller.getRunProgress());
			overallProgress.setValue(controller.getOverallProgress());
		} else if (event.getType().equals(ControllerEvent.Type.VIEW_CHANGED)) {
			updateChartLayout();
		} else if (event.getType().equals(ControllerEvent.Type.PARAM_CHANGED)) {
			try {
				updateParameterizationPlot();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dispose() {
		controller.cancel();
		super.dispose();
	}

}
