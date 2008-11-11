/*******************************************************************************
 * Copyright (C) 2005, 2008  Wolfgang Schramm and Contributors
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation version 2 of the License.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA    
 *******************************************************************************/
package net.tourbook.ui.views.tourDataEditor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;

import net.tourbook.Messages;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartMarker;
import net.tourbook.chart.SelectionChartInfo;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.data.TourPerson;
import net.tourbook.data.TourReference;
import net.tourbook.data.TourType;
import net.tourbook.database.MyTourbookException;
import net.tourbook.database.TourDatabase;
import net.tourbook.plugin.TourbookPlugin;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tag.ActionRemoveAllTags;
import net.tourbook.tag.ActionSetTourTag;
import net.tourbook.tag.TagManager;
import net.tourbook.tour.ActionOpenAdjustAltitudeDialog;
import net.tourbook.tour.ActionOpenMarkerDialog;
import net.tourbook.tour.ITourPropertyListener;
import net.tourbook.tour.ITourSaveListener;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.SelectionTourIds;
import net.tourbook.tour.TourManager;
import net.tourbook.tour.TourProperties;
import net.tourbook.tour.TourProperty;
import net.tourbook.ui.ColumnDefinition;
import net.tourbook.ui.ColumnManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.ITourViewer;
import net.tourbook.ui.MessageManager;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.UI;
import net.tourbook.ui.action.ActionModifyColumns;
import net.tourbook.ui.action.ActionOpenPrefDialog;
import net.tourbook.ui.action.ActionSetTourType;
import net.tourbook.ui.tourChart.TourChart;
import net.tourbook.ui.views.tourCatalog.SelectionTourCatalogView;
import net.tourbook.ui.views.tourCatalog.TVICatalogComparedTour;
import net.tourbook.ui.views.tourCatalog.TVICatalogRefTourItem;
import net.tourbook.ui.views.tourCatalog.TVICompareResultComparedTour;
import net.tourbook.util.PixelConverter;
import net.tourbook.util.PostSelectionProvider;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

// author: Wolfgang Schramm
// create: 24.08.2007

/**
 * This editor can edit (when all is implemented) all data for a tour
 */
public class TourDataEditorView extends ViewPart implements ISaveablePart2, ITourViewer, ITourProvider {

	public static final String				ID								= "net.tourbook.views.TourDataEditorView";	//$NON-NLS-1$

	final IDialogSettings					fViewState						= TourbookPlugin.getDefault()
																					.getDialogSettingsSection(ID);
	final IDialogSettings					fViewStateSlice					= TourbookPlugin.getDefault()
																					.getDialogSettingsSection(//
																					ID + ".slice");					//$NON-NLS-1$
	final IDialogSettings					fViewStateMarker				= TourbookPlugin.getDefault()
																					.getDialogSettingsSection(//
																					ID + ".marker");					//$NON-NLS-1$

	private static final String				WIDGET_KEY						= "widgetKey";								//$NON-NLS-1$
	private static final String				WIDGET_KEY_TOURDISTANCE			= "tourDistance";							//$NON-NLS-1$
	private static final String				WIDGET_KEY_PERSON				= "tourPerson";							//$NON-NLS-1$
	private static final String				MESSAGE_KEY_ANOTHER_SELECTION	= "anotherSelection";						//$NON-NLS-1$

	/**
	 * shows the busy indicator to load the slice viewer when there are more items than this value
	 */
	private static final int				BUSY_INDICATOR_ITEMS			= 5000;

	private static final String				MEMENTO_SELECTED_TAB			= "tourProperties.selectedTab";			//$NON-NLS-1$
	private static final String				MEMENTO_ROW_EDIT_MODE			= "tourProperties.editMode";				//$NON-NLS-1$

	/*
	 * data series which are displayed in the viewer
	 */
	private int[]							fSerieTime;
	private int[]							fSerieDistance;
	private int[]							fSerieAltitude;
	private int[]							fSerieTemperature;
	private int[]							fSerieCadence;
	private int[]							fSerieGradient;
	private int[]							fSerieSpeed;
	private int[]							fSeriePace;
	private int[]							fSeriePower;
	private int[]							fSeriePulse;
	private double[]						fSerieLatitude;
	private double[]						fSerieLongitude;

	// slice viewer
	private ColumnDefinition				fColDefAltitude;
	private ColumnDefinition				fColDefCadence;
	private ColumnDefinition				fColDefPulse;
	private ColumnDefinition				fColDefTemperature;
	private ColumnDefinition				fColDefSliceMarker;

	// marker viewer
	private ColumnDefinition				fColDefMarker;

	private ActionModifyColumns				fActionModifyColumns;

	private ActionSaveTour					fActionSaveTour;
	private ActionUndoChanges				fActionUndoChanges;
	private ActionToggleRowSelectMode		fActionToggleRowSelectMode;
	private ActionOpenMarkerDialog			fActionOpenMarkerDialog;
	private ActionOpenAdjustAltitudeDialog	fActionOpenAdjustAltitudeDialog;
	private ActionDeleteTimeSlices			fActionDeleteTimeSlices;
	private ActionSetTourTag				fActionAddTag;
	private ActionCreateTourMarker			fActionCreateTourMarker;

	private ActionSetTourTag				fActionRemoveTag;
	private ActionRemoveAllTags				fActionRemoveAllTags;
	private ActionOpenPrefDialog			fActionOpenTagPrefs;
	private ActionOpenPrefDialog			fActionOpenTourTypePrefs;
	private PageBook						fPageBook;

	private Label							fPageNoTour;
	private Form							fPageEditorForm;

	private CTabFolder						fTabFolder;
	private CTabItem						fTabTour;
	private CTabItem						fTabMarker;
	private CTabItem						fTabSlices;
	private CTabItem						fTabInfo;
	private TourChart						fTourChart;

	private TourData						fTourData;
	private Composite						fTourContainer;

	private Composite						fInfoContainer;
	private Composite						fMarkerViewerContainer;
	private Composite						fSliceContainer;

	private Composite						fSliceViewerContainer;
	private Label							fTimeSliceLabel;
	private TableViewer						fSliceViewer;

	private Object[]						fSliceViewerItems;
	private ColumnManager					fSliceColumnManager;

	private TableViewer						fMarkerViewer;
	private ColumnManager					fMarkerColumnManager;

	private Text							fTextTitle;

	private Text							fTextDescription;
	private DateTime						fDtTourDate;

	private DateTime						fDtStartTime;
	private DateTime						fDtRecordingTime;

	private DateTime						fDtDrivingTime;
	private DateTime						fDtPausedTime;
	private Label							fLblTimeSlicesCount;

	private Label							fLblDeviceName;
	private Label							fLblTourId;
	private Text							fTextStartLocation;

	private Text							fTextEndLocation;
	private Text							fTextTourDistance;

	private Label							fLblTourDistanceUnit;
	private Link							fTagLink;

	private Label							fLblTourTags;
	private Link							fTourTypeLink;
	private CLabel							fLblTourType;
	private Label							fLblRefTour;

	private Label							fLblPerson;
	private MessageManager					fMessageManager;

	private PostSelectionProvider			fPostSelectionProvider;

	private ISelectionListener				fPostSelectionListener;
	private IPartListener2					fPartListener;
	private IPropertyChangeListener			fPrefChangeListener;
	private ITourPropertyListener			fTourPropertyListener;
	private ITourSaveListener				fTourSaveListener;
	private Calendar						fCalendar						= GregorianCalendar.getInstance();

	private NumberFormat					fNumberFormatter				= NumberFormat.getNumberInstance();
	/**
	 * <code>true</code>: rows can be selected in the viewer<br>
	 * <code>false</code>: cell can be selected in the viewer
	 */
	private boolean							fIsRowEditMode					= true;

	private long							fSliceViewerTourId				= -1;
	private SelectionChartXSliderPosition	fSliceViewerXSliderPosition;

	private boolean							fIsTourDirty					= false;

	/**
	 * is <code>true</code> when the tour is currently being saved to prevent a modify event or the
	 * onSelectionChanged event
	 */
	private boolean							fIsSavingInProgress				= false;

	/**
	 * when <code>true</code>, the tour dirty flag is disabled to load data into the fields
	 */
	private boolean							fIsDirtyDisabled				= false;

	/**
	 * contains the tour id from the last selection event
	 */
	private Long							fSelectionTourId;

	private KeyAdapter						fKeyListener;
	private ModifyListener					fModifyListener;
	private ModifyListener					fVerifyFloatValue;
	private SelectionAdapter				fTourTimeListener;
	private SelectionAdapter				fDateTimeListener;
	private PixelConverter					fPixelConverter;

	/**
	 * this width is used as a hint for the width of the description field, this value also
	 * influences the width of the columns in this editor
	 */
	private int								fTextColumnWidth				= 150;

	/**
	 * is <code>true</code> when {@link #fTourChart} contains reference tours
	 */
	private boolean							fIsReferenceTourAvailable;

	/**
	 * range for the reference tours, is <code>null</code> when reference tours are not available<br>
	 * 1st index = ref tour<br>
	 * 2nd index: 0:start, 1:end
	 */
	private int[][]							fRefTourRange;

	private boolean							fIsPartVisible					= false;

	/**
	 * when <code>true</code> additional info is displayed in the title area
	 */
	private boolean							fIsInfoInTitle;

	/**
	 * every requested UI update increased this counter
	 */
	private int								fUIUpdateCounter;

	/**
	 * counter when the UI update runnable is run, this will optimize performance to not update the
	 * UI when the part is hidden
	 */
	private int								fUIRunnableCounter				= 0;

	private int								fUIUpdateTitleCounter			= 0;
	private TourData						fUIRunnableTourData;
	private boolean							fUIRunnableForceTimeSliceReload;
	private boolean							fUIRunnableIsDirtyDisabled;

// disabled because tour data get corrupted, the tour date could be from another tour	
//	private int							fUIUpdateCounterTabTour			= -1;
//	private int							fUIUpdateCounterTabInfo			= -1;
//	private int							fUIUpdateCounterTabMarker		= -1;
//	private int							fUIUpdateCounterTabSlices		= -1;

	private SliceIntegerEditingSupport		fAltitudeEditingSupport;
	private SliceIntegerEditingSupport		fPulseEditingSupport;
	private SliceIntegerEditingSupport		fTemperatureEditingSupport;
	private SliceIntegerEditingSupport		fCadenceEditingSupport;

	private HashMap<Integer, TourMarker>	fMarkerMap						= new HashMap<Integer, TourMarker>();

	private ActionDeleteTourMarker			fActionDeleteTourMarker;

	private final class MarkerEditingSupport extends EditingSupport {

		private final TextCellEditor	fCellEditor;

		private MarkerEditingSupport(final TextCellEditor cellEditor) {
			super(fMarkerViewer);
			fCellEditor = cellEditor;
		}

		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			return fCellEditor;
		}

		@Override
		protected Object getValue(final Object element) {
			return ((TourMarker) element).getLabel();
		}

		@Override
		protected void setValue(final Object element, final Object value) {

			if (value instanceof String) {

				final TourMarker tourMarker = (TourMarker) element;
				final String newValue = (String) value;

				if (newValue.equals(tourMarker.getLabel()) == false) {

					// value has changed

					tourMarker.setLabel(newValue);

					setTourDirty();

					// update viewer
					getViewer().update(element, null);

					// display modified time slices in this editor and in other views/editors
					fireModifyNotification();
				}
			}
		}
	}

	private class MarkerViewerContentProvicer implements IStructuredContentProvider {

		public void dispose() {}

		public Object[] getElements(final Object inputElement) {
			if (fTourData == null) {
				return new Object[0];
			} else {
				return fTourData.getTourMarkers().toArray();
			}
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
	}

	/**
	 * Sort the markers by time
	 */
	private class MarkerViewerSorter extends ViewerSorter {
		@Override
		public int compare(final Viewer viewer, final Object obj1, final Object obj2) {
			return ((TourMarker) (obj1)).getTime() - ((TourMarker) (obj2)).getTime();
		}
	}

	private final class SliceIntegerEditingSupport extends EditingSupport {

		private final TextCellEditor	fCellEditor;
		private int[]					fDataSerie;

		private SliceIntegerEditingSupport(final TextCellEditor cellEditor, final int[] dataSerie) {
			super(fSliceViewer);
			fCellEditor = cellEditor;
			fDataSerie = dataSerie;
		}

		@Override
		protected boolean canEdit(final Object element) {
			if (fDataSerie == null || isTourInDb() == false) {
				return false;
			}
			return true;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			return fCellEditor;
		}

		@Override
		protected Object getValue(final Object element) {
			final TimeSlice timeSlice = (TimeSlice) element;
			return new Integer(fDataSerie[timeSlice.serieIndex]).toString();
		}

		public void setDataSerie(final int[] dataSerie) {
			fDataSerie = dataSerie;
		}

		@Override
		protected void setValue(final Object element, final Object value) {

			if (value instanceof String) {

				try {
					final TimeSlice timeSlice = (TimeSlice) element;
					final int newValue = Integer.parseInt((String) value);

					if (newValue != fDataSerie[timeSlice.serieIndex]) {

						// value has changed

						// update dataserie
						fDataSerie[timeSlice.serieIndex] = newValue;

						setTourDirty();

						fTourData.clearComputedSeries();
						updateDataSeriesFromTourData();

						// refresh the whole viewer because the computed data series could have been changed
						getViewer().refresh();

						// display modified time slices other views
						fireModifyNotification();
					}

				} catch (final Exception e) {
					// ignore invalid characters
				} finally {}
			}
		}
	}

	private final class SliceMarkerEditingSupport extends EditingSupport {

		private final TextCellEditor	fCellEditor;

		private SliceMarkerEditingSupport(final TextCellEditor cellEditor) {
			super(fSliceViewer);
			fCellEditor = cellEditor;
		}

		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			return fCellEditor;
		}

		@Override
		protected Object getValue(final Object element) {

			final TourMarker tourMarker = fMarkerMap.get(((TimeSlice) element).serieIndex);
			if (tourMarker == null) {

				// marker is not yet available

				return UI.EMPTY_STRING;

			} else {

				// marker is available

				return tourMarker.getLabel();
			}
		}

		@Override
		protected void setValue(final Object element, final Object value) {

			if (value instanceof String) {

				final String newMarkerLabel = (String) value;
				final int serieIndex = ((TimeSlice) element).serieIndex;

				boolean isMarkerModified = false;

				final TourMarker tourMarker = fMarkerMap.get(serieIndex);
				if (tourMarker == null) {

					// marker is not yet available

					if (newMarkerLabel.trim().length() == 0) {
						return;
					}

					// create a new marker
					final TourMarker newMarker = new TourMarker(fTourData, ChartMarker.MARKER_TYPE_CUSTOM);
					newMarker.setSerieIndex(serieIndex);
					newMarker.setDistance(fTourData.getMetricDistanceSerie()[serieIndex]);
					newMarker.setTime(fTourData.timeSerie[serieIndex]);
					newMarker.setLabel(newMarkerLabel);
					newMarker.setVisualPosition(ChartMarker.VISUAL_HORIZONTAL_ABOVE_GRAPH_CENTERED);

					fTourData.getTourMarkers().add(newMarker);
					updateMarkerMap();

					// update marker viewer
					fMarkerViewer.setInput(new Object[0]);

					isMarkerModified = true;

				} else {

					// marker is available

					if (newMarkerLabel.equals(tourMarker.getLabel()) == false) {

						// value has changed

						tourMarker.setLabel(newMarkerLabel);

						isMarkerModified = true;
					}
				}

				if (isMarkerModified) {

					setTourDirty();

					// update slide viewer
					getViewer().update(element, null);

					// display modified time slices in other views
					fireModifyNotification();
				}
			}
		}
	}

	private class SliceViewerContentProvider implements IStructuredContentProvider {

		public SliceViewerContentProvider() {}

		public void dispose() {}

		public Object[] getElements(final Object parent) {
			return fSliceViewerItems;
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {}
	}

	/**
	 * delete selected time slices
	 */
	void actionDeleteTimeSlices() {

		// a tour with reference tours is currently not supported 
		if (fIsReferenceTourAvailable) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					Messages.tour_editor_dlg_delete_rows_title,
					Messages.tour_editor_dlg_delete_rows_message);
			return;
		}

		/*
		 * check row/cell mode, row mode must be set, it works with the cell mode but can be
		 * confusing because multiple rows can be selected but they are not visible
		 */
		if (fIsRowEditMode == false) {
			final MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(Display.getCurrent()
					.getActiveShell(),
					Messages.tour_editor_dlg_delete_rows_title,
					Messages.tour_editor_dlg_delete_rows_mode_message,
					Messages.tour_editor_dlg_delete_rows_mode_toggle_message,
					true,
					null,
					null);

			if (dialog.getToggleState()) {
				fActionToggleRowSelectMode.setChecked(true);
				actionToggleEditMode();
			}

			return;
		}

		// get selected time slices
		final StructuredSelection selection = (StructuredSelection) fSliceViewer.getSelection();
		if (selection.size() == 0) {
			return;
		}

		final Object[] selectedTimeSlices = selection.toArray();

		/*
		 * check if time slices have a successive selection
		 */
		int lastIndex = -1;
		int firstIndex = -1;

		for (final Object selectedItem : selectedTimeSlices) {

			final TimeSlice timeSlice = (TimeSlice) selectedItem;

			if (lastIndex == -1) {

				// first slice

				firstIndex = lastIndex = timeSlice.serieIndex;

			} else {

				// 2...n slices

				if (lastIndex - timeSlice.serieIndex == -1) {

					// successive selection

					lastIndex = timeSlice.serieIndex;

				} else {

					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							Messages.tour_editor_dlg_delete_rows_title,
							Messages.tour_editor_dlg_delete_rows_not_successive);
					return;
				}
			}
		}

		// check if markers are within the selection
		if (canDeleteMarkers(firstIndex, lastIndex) == false) {
			return;
		}

		/*
		 * get first selection index to select a time slice after removal
		 */
		final Table table = (Table) fSliceViewer.getControl();
		final int[] indices = table.getSelectionIndices();
		Arrays.sort(indices);
		int lastSelectionIndex = indices[0];

		/*
		 * update data series
		 */
		int[] intSerie = fTourData.altitudeSerie;
		if (intSerie != null) {
			fTourData.altitudeSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}
		intSerie = fTourData.cadenceSerie;
		if (intSerie != null) {
			fTourData.cadenceSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}
		intSerie = fTourData.distanceSerie;
		if (intSerie != null) {
			fTourData.distanceSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}
		intSerie = fTourData.pulseSerie;
		if (intSerie != null) {
			fTourData.pulseSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}
		intSerie = fTourData.temperatureSerie;
		if (intSerie != null) {
			fTourData.temperatureSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}
		intSerie = fTourData.timeSerie;
		if (intSerie != null) {
			fTourData.timeSerie = getRemainingIntegerSerieData(intSerie, firstIndex, lastIndex);
		}

		double[] doubleSerie = fTourData.latitudeSerie;
		if (doubleSerie != null) {
			fTourData.latitudeSerie = getRemainingDoubleSerieData(doubleSerie, firstIndex, lastIndex);
		}
		doubleSerie = fTourData.longitudeSerie;
		if (doubleSerie != null) {
			fTourData.longitudeSerie = getRemainingDoubleSerieData(doubleSerie, firstIndex, lastIndex);
		}

		// reset computed data series and clear cached world positions
		fTourData.clearComputedSeries();
		fTourData.clearWorldPositions();

		// segments must be recomputed
		fTourData.segmentSerieIndex = null;

		removeTourMarkers(firstIndex, lastIndex);

		updateDataSeriesFromTourData();
		updateUINumberOfTimeSlice();
		updateUITabMarker();

		// update viewer items and adjust serie index
		fSliceViewerItems = getRemainingViewerItems(fSliceViewerItems, firstIndex, lastIndex);

		fSliceViewer.getControl().setRedraw(false);
		{
			// update viewer
			fSliceViewer.remove(selectedTimeSlices);

			// update serie index label
			fSliceViewer.refresh(true);
		}
		fSliceViewer.getControl().setRedraw(true);

		setTourDirty();

		// notify other viewers
		fireModifyNotification();

		/*
		 * select next available time slice
		 */
		final int itemCount = table.getItemCount();
		if (itemCount > 0) {

			// adjust to array bounds 
			lastSelectionIndex = Math.max(0, Math.min(lastSelectionIndex, itemCount - 1));

			table.setSelection(lastSelectionIndex);
			table.showSelection();

			// fire slider position
			fSliceViewer.setSelection(fSliceViewer.getSelection());
		}
	}

	void actionDeleteTourMarker() {

		final StructuredSelection selection = (StructuredSelection) fMarkerViewer.getSelection();
		if (selection.size() == 0) {
			return;
		}

		// get last selected index
		final Table table = fMarkerViewer.getTable();
		final int[] indices = table.getSelectionIndices();
		Arrays.sort(indices);
		int lastSelectionIndex = indices[0];

		fTourData.getTourMarkers().removeAll(selection.toList());
		fMarkerViewer.remove(selection.toArray());

		updateMarkerMap();

		setTourDirty();

		// notify other viewers
		fireModifyNotification();

		// select next available marker

		final int itemCount = table.getItemCount();
		if (itemCount > 0) {

			// adjust to array bounds 
			lastSelectionIndex = Math.max(0, Math.min(lastSelectionIndex, itemCount - 1));

			table.setSelection(lastSelectionIndex);
			table.showSelection();

			// fire slider position
			onMarkerViewerSelectionChanged();
		}
	}

	void actionSaveTour() {

		// action is enabled when the tour is modified

		saveTourIntoDB();
	}

	void actionToggleEditMode() {

		fIsRowEditMode = fActionToggleRowSelectMode.isChecked();

		recreateViewer();
	}

	void actionUndoChanges() {

		if (confirmUndoChanges()) {
			discardModifications();
		}
	}

	private void addPartListener() {

		// set the part listener
		fPartListener = new IPartListener2() {
			public void partActivated(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourDataEditorView.this) {
					fPostSelectionProvider.setSelection(new SelectionTourData(null, fTourData));
				}
			}

			public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

			public void partClosed(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourDataEditorView.this) {

					saveState();
					TourManager.setTourDataEditor(null);
				}
			}

			public void partDeactivated(final IWorkbenchPartReference partRef) {}

			public void partHidden(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourDataEditorView.this) {
					fIsPartVisible = false;
				}
			}

			public void partInputChanged(final IWorkbenchPartReference partRef) {}

			public void partOpened(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourDataEditorView.this) {
					TourManager.setTourDataEditor(TourDataEditorView.this);
				}
			}

			public void partVisible(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == TourDataEditorView.this) {

					fIsPartVisible = true;

					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							updateUIRunnable();
						}
					});
				}
			}
		};

		// register the listener in the page
		getSite().getPage().addPartListener(fPartListener);
	}

	private void addPrefListener() {

		fPrefChangeListener = new Preferences.IPropertyChangeListener() {
			public void propertyChange(final Preferences.PropertyChangeEvent event) {

				if (fTourData == null) {
					return;
				}

				final String property = event.getProperty();
				if (property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)
						|| property.equals(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED)) {

					/*
					 * tour data could have been changed but the changes are not reflected in the
					 * data model, the model needs to be updated from the UI
					 */
					if (isTourValid()) {
						updateTourDataFromUI();
					} else {
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
								Messages.tour_editor_dlg_discard_tour_title,
								Messages.tour_editor_dlg_discard_tour_message);
						discardModifications();
					}

					if (property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)) {

						// measurement system has changed

						UI.updateUnits();
						recreateViewer();

						updateUIFromTourData(fTourData, false, true);

					} else if (property.equals(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED)) {

						// reload tour data

						updateUIFromTourData(fTourData, false, true);
					}
				}
			}
		};
		TourbookPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fPrefChangeListener);
	}

	/**
	 * listen for events when a tour is selected
	 */
	private void addSelectionListener() {

		fPostSelectionListener = new ISelectionListener() {
			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {

				if (part == TourDataEditorView.this) {
					return;
				}

				onSelectionChanged(selection);
			}
		};
		getSite().getPage().addPostSelectionListener(fPostSelectionListener);
	}

	private void addTourPropertyListener() {

		fTourPropertyListener = new ITourPropertyListener() {
			public void propertyChanged(final IWorkbenchPart part,
										final TourProperty propertyId,
										final Object propertyData) {

				if (fTourData == null || part == TourDataEditorView.this) {
					return;
				}

				final long tourDataEditorTourId = fTourData.getTourId();

				if (propertyId == TourProperty.TOUR_PROPERTIES_CHANGED && propertyData instanceof TourProperties) {

					final TourProperties tourProperties = (TourProperties) propertyData;
					final ArrayList<TourData> modifiedTours = tourProperties.getModifiedTours();

					if (modifiedTours == null) {
						return;
					}

					for (final TourData tourData : modifiedTours) {
						if (tourData.getTourId() == tourDataEditorTourId) {

							// update modified tour

							if (tourProperties.tourDataEditorSavedTour == fTourData) {

								/*
								 * nothing to do because the tour is already saved (when it was not
								 * modified before) and the UI is already updated
								 */
								return;
							}

							if (tourProperties.isReverted) {
								setTourClean();
							} else {
								setTourDirty();
							}

							updateUIFromTourData(tourData, true, tourProperties.isReverted);

							// nothing more to do, the editor contains only one tour
							return;
						}
					}

				} else if (propertyId == TourProperty.TAG_STRUCTURE_CHANGED) {

					updateUIFromTourData(fTourData, false, true);

				} else if (propertyId == TourProperty.UPDATE_UI) {

					// check if this tour data editor contains a tour which must be updated

					Long updatedTourId = null;

					if (propertyData instanceof SelectionTourId) {

						final Long tourId = ((SelectionTourId) propertyData).getTourId();
						if (tourDataEditorTourId == tourId) {
							updatedTourId = tourId;
						}

					} else if (propertyData instanceof SelectionTourIds) {

						for (final Long tourId : ((SelectionTourIds) propertyData).getTourIds()) {
							if (tourDataEditorTourId == tourId) {
								updatedTourId = tourId;
								break;
							}
						}
					}

					// update editor
					if (updatedTourId != null) {

						fTourData = TourManager.getInstance().getTourData(fTourData.getTourId());
						updateMarkerMap();

						updateUIFromTourData(fTourData, false, true);
					}
				}
			}
		};

		TourManager.getInstance().addPropertyListener(fTourPropertyListener);
	}

	private void addTourSaveListener() {

		fTourSaveListener = new ITourSaveListener() {
			public boolean saveTour() {

				boolean isTourSaved;

				fIsSavingInProgress = true;
				{
					isTourSaved = TourDataEditorView.this.saveTourValidation();
				}
				fIsSavingInProgress = false;

				return isTourSaved;
			}
		};

		TourManager.getInstance().addTourSaveListener(fTourSaveListener);
	}

	/**
	 * Checks if a marker is within the selected time slices
	 * 
	 * @param firstSliceIndex
	 * @param lastSliceIndex
	 * @return Returns <code>true</code> when the marker can be deleted or there is no marker <br>
	 *         Returns <code>false</code> when the marker can not be deleted.
	 */
	private boolean canDeleteMarkers(final int firstSliceIndex, final int lastSliceIndex) {

		final Integer[] markerSerieIndex = fMarkerMap.keySet().toArray(new Integer[fMarkerMap.size()]);

		for (final Integer markerIndex : markerSerieIndex) {

			if (markerIndex >= firstSliceIndex && markerIndex <= lastSliceIndex) {

				// there is a marker within the deleted time slices

				if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
						Messages.tour_editor_dlg_delete_marker_title,
						Messages.tour_editor_dlg_delete_marker_message)) {
					return true;
				} else {
					return false;
				}
			}
		}

		// marker is not in the selection
		return true;
	}

	private boolean confirmUndoChanges() {

		final IPreferenceStore store = TourbookPlugin.getDefault().getPreferenceStore();

		// check if confirmation is disabled
		if (store.getBoolean(ITourbookPreferences.TOURDATA_EDITOR_CONFIRMATION_REVERT_TOUR)) {

			return true;

		} else {

			final MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(Display.getCurrent()
					.getActiveShell(),//
					Messages.tour_editor_dlg_revert_tour_title, // title
					Messages.tour_editor_dlg_revert_tour_message, // message
					Messages.tour_editor_dlg_revert_tour_toggle_message, // toggle message
					false, // toggle default state
					null,
					null);

			store.setValue(ITourbookPreferences.TOURDATA_EDITOR_CONFIRMATION_REVERT_TOUR, dialog.getToggleState());

			return dialog.getReturnCode() == Window.OK;
		}
	}

	private void createActions() {

		fActionSaveTour = new ActionSaveTour(this);
		fActionUndoChanges = new ActionUndoChanges(this);
		fActionToggleRowSelectMode = new ActionToggleRowSelectMode(this);
		fActionOpenAdjustAltitudeDialog = new ActionOpenAdjustAltitudeDialog(this, false);
		fActionOpenMarkerDialog = new ActionOpenMarkerDialog(this, false);

		fActionDeleteTimeSlices = new ActionDeleteTimeSlices(this);

		fActionCreateTourMarker = new ActionCreateTourMarker(this);
		fActionDeleteTourMarker = new ActionDeleteTourMarker(this);

		fActionAddTag = new ActionSetTourTag(this, true, false);
		fActionRemoveTag = new ActionSetTourTag(this, false, false);
		fActionRemoveAllTags = new ActionRemoveAllTags(this, false);
		fActionOpenTagPrefs = new ActionOpenPrefDialog(Messages.action_tag_open_tagging_structure,
				ITourbookPreferences.PREF_PAGE_TAGS);

		fActionOpenTourTypePrefs = new ActionOpenPrefDialog(Messages.action_tourType_modify_tourTypes,
				ITourbookPreferences.PREF_PAGE_TOUR_TYPE);

		fActionModifyColumns = new ActionModifyColumns(this);
	}

	private void createFieldListener() {

		fModifyListener = new ModifyListener() {
			public void modifyText(final ModifyEvent e) {

				if (fIsDirtyDisabled || fIsSavingInProgress) {
					return;
				}

				setTourDirty();
			}
		};

		fKeyListener = new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				updateContentOnKeyUp();
			}
		};

		/*
		 * listener for tour date/time
		 */
		fDateTimeListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {

				if (fIsDirtyDisabled || fIsSavingInProgress) {
					return;
				}

				setTourDirty();

				updateUITitle();
			}
		};

		/*
		 * listener for recording/driving/paused time
		 */
		fTourTimeListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {

				if (fIsDirtyDisabled || fIsSavingInProgress) {
					return;
				}

				setTourDirty();

				/*
				 * ensure validation for all 3 times
				 */

				final DateTime dt = (DateTime) event.widget;

				int recTime = (fDtRecordingTime.getHours() * 3600)
						+ (fDtRecordingTime.getMinutes() * 60)
						+ fDtRecordingTime.getSeconds();

				int pausedTime = (fDtPausedTime.getHours() * 3600)
						+ (fDtPausedTime.getMinutes() * 60)
						+ fDtPausedTime.getSeconds();

				int driveTime = (fDtDrivingTime.getHours() * 3600)
						+ (fDtDrivingTime.getMinutes() * 60)
						+ fDtDrivingTime.getSeconds();

				if (dt == fDtRecordingTime) {

					// recording time is modified

					if (pausedTime > recTime) {
						pausedTime = recTime;
					}

					driveTime = recTime - pausedTime;

				} else if (dt == fDtPausedTime) {

					// paused time is modified

					if (pausedTime > recTime) {
						recTime = pausedTime;
					}

					driveTime = recTime - pausedTime;

				} else if (dt == fDtDrivingTime) {

					// driving time is modified

					if (driveTime > recTime) {
						recTime = driveTime;
					}

					pausedTime = recTime - driveTime;
				}

				fDtRecordingTime.setTime(recTime / 3600, ((recTime % 3600) / 60), ((recTime % 3600) % 60));
				fDtDrivingTime.setTime(driveTime / 3600, ((driveTime % 3600) / 60), ((driveTime % 3600) % 60));
				fDtPausedTime.setTime(pausedTime / 3600, ((pausedTime % 3600) / 60), ((pausedTime % 3600) % 60));
			}
		};

		fVerifyFloatValue = new ModifyListener() {

			public void modifyText(final ModifyEvent event) {

				if (fIsDirtyDisabled || fIsSavingInProgress) {
					return;
				}

				setTourDirty();

				final Text widget = (Text) event.widget;
				final String valueText = widget.getText().trim();

				if (valueText.length() > 0) {
					try {

						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
						//
						// Float.parseFloat() ignores localized strings therefore the databinding converter is used
						// which provides also a good error message
						//
						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

						StringToNumberConverter.toFloat(true).convert(valueText);

						fMessageManager.removeMessage(widget.getData(WIDGET_KEY), widget);

					} catch (final IllegalArgumentException e) {

						// wrong characters are entered, display an error message

						fMessageManager.addMessage(widget.getData(WIDGET_KEY),
								e.getLocalizedMessage(),
								null,
								IMessageProvider.ERROR,
								widget);
					}
				}
			}
		};
	}

	/**
	 * @param parent
	 */
	private void createMarkerViewer(final Composite parent) {

		final Composite layoutContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(layoutContainer);

		final TableColumnLayout tableLayout = new TableColumnLayout();
		layoutContainer.setLayout(tableLayout);

		/*
		 * create table
		 */
		final Table table = new Table(layoutContainer, SWT.FULL_SELECTION | SWT.MULTI);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
//		table.setLinesVisible(TourbookPlugin.getDefault()
//				.getPreferenceStore()
//				.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

		/*
		 * create viewer
		 */
		fMarkerViewer = new TableViewer(table);
		if (fIsRowEditMode == false) {
			setCellEditSupport(fMarkerViewer);
		}

		// create editing support after the viewer is created but before the columns are created
		final TextCellEditor cellEditor = new TextCellEditor(fMarkerViewer.getTable());
		fColDefMarker.setEditingSupport(new MarkerEditingSupport(cellEditor));

		fMarkerColumnManager.setColumnLayout(tableLayout);
		fMarkerColumnManager.createColumns(fMarkerViewer);

		fMarkerViewer.setUseHashlookup(true);
		fMarkerViewer.setContentProvider(new MarkerViewerContentProvicer());
		fMarkerViewer.setSorter(new MarkerViewerSorter());
		createMarkerViewerContextMenu(table);

		fMarkerViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				onMarkerViewerSelectionChanged();
			}
		});

		fMarkerViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {

				// edit selected marker
				final IStructuredSelection selection = (IStructuredSelection) fMarkerViewer.getSelection();
				if (selection.size() > 0) {
					fActionOpenMarkerDialog.setSelectedMarker((TourMarker) selection.getFirstElement());
					fActionOpenMarkerDialog.run();
				}
			}
		});
	}

	/**
	 * create the views context menu
	 * 
	 * @param table
	 */
	private void createMarkerViewerContextMenu(final Table table) {

		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				fillMarkerContextMenu(manager);
			}
		});

		table.setMenu(menuMgr.createContextMenu(table));

		getSite().registerContextMenu(menuMgr, fMarkerViewer);
	}

	/**
	 * create the drop down menus, this must be created after the parent control is created
	 */
	private void createMenus() {

		/*
		 * tag menu
		 */
		MenuManager menuMgr = new MenuManager();

		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager menuMgr) {

				final boolean isTagSet = fTourData.getTourTags().size() > 0;

				// enable actions
				fActionRemoveTag.setEnabled(isTagSet);
				fActionRemoveAllTags.setEnabled(isTagSet);

				// set menu items
				menuMgr.add(fActionAddTag);
				menuMgr.add(fActionRemoveTag);
				menuMgr.add(fActionRemoveAllTags);

				TagManager.fillRecentTagsIntoMenu(menuMgr, TourDataEditorView.this, true, false);

				menuMgr.add(new Separator());
				menuMgr.add(fActionOpenTagPrefs);
			}
		});

		// set menu for the tag item
		fTagLink.setMenu(menuMgr.createContextMenu(fTagLink));

		/*
		 * tour type menu
		 */
		menuMgr = new MenuManager();

		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager menuMgr) {

				// set menu items

				ActionSetTourType.fillMenu(menuMgr, TourDataEditorView.this, false);

				menuMgr.add(new Separator());
				menuMgr.add(fActionOpenTourTypePrefs);
			}
		});

		// set menu for the tag item
		fTourTypeLink.setMenu(menuMgr.createContextMenu(fTourTypeLink));
	}

	@Override
	public void createPartControl(final Composite parent) {

		// define columns for the viewers
		fSliceColumnManager = new ColumnManager(this, fViewStateSlice);
		defineSliceViewerColumns(parent);

		fMarkerColumnManager = new ColumnManager(this, fViewStateMarker);
		defineMarkerViewerColumns(parent);

		restoreStateBeforeUI();

		createFieldListener(); // must be set before the UI is created
		createUI(parent);
		createMenus();

		addSelectionListener();
		addPartListener();
		addPrefListener();
		addTourPropertyListener();
		addTourSaveListener();

		createActions();
		fillToolbar();

		// this part is a selection provider
		getSite().setSelectionProvider(fPostSelectionProvider = new PostSelectionProvider());

		restoreStateWithUI();

		fPageBook.showPage(fPageNoTour);

		displaySelectedTour();
	}

	private Composite createSection(final Composite parent, final FormToolkit tk, final String title) {

		final Section section = tk.createSection(parent,//
				//Section.TWISTIE | 
//				Section.SHORT_TITLE_BAR
				Section.TITLE_BAR
		// | Section.DESCRIPTION 
		// | Section.EXPANDED
		);

		section.setText(title);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);

		final Composite sectionContainer = tk.createComposite(section);
		section.setClient(sectionContainer);

//		section.addExpansionListener(new ExpansionAdapter() {
//			@Override
//			public void expansionStateChanged(final ExpansionEvent e) {
//				form.reflow(false);
//			}
//		});

		return sectionContainer;
	}

	private void createSectionCharacteristics(final Composite parent, final FormToolkit tk) {

		final Composite section = createSection(parent, tk, Messages.tour_editor_section_characteristics);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(section);

		/*
		 * tags
		 */
		fTagLink = new Link(section, SWT.NONE);
		fTagLink.setText(Messages.tour_editor_label_tour_tag);
		GridDataFactory.fillDefaults()//
				.align(SWT.BEGINNING, SWT.FILL)
				.applyTo(fTagLink);
		fTagLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				UI.openControlMenu(fTagLink);
			}
		});
		tk.adapt(fTagLink, true, true);

		fLblTourTags = tk.createLabel(section, UI.EMPTY_STRING, SWT.WRAP);
		GridDataFactory.fillDefaults()//
				.grab(true, false)
				// hint is necessary that the width is not expanded when the text is long
				.hint(fTextColumnWidth, SWT.DEFAULT)
				.span(3, 1)
				.applyTo(fLblTourTags);

		/*
		 * tour type
		 */
		fTourTypeLink = new Link(section, SWT.NONE);
		fTourTypeLink.setText(Messages.tour_editor_label_tour_type);
		fTourTypeLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				UI.openControlMenu(fTourTypeLink);
			}
		});
		tk.adapt(fTourTypeLink, true, true);

		fLblTourType = new CLabel(section, SWT.NONE);
		GridDataFactory.swtDefaults()//
				.grab(true, false)
				.span(3, 1)
				.applyTo(fLblTourType);
	}

	private void createSectionDateTime(final Composite parent, final FormToolkit tk) {

		final Composite section = createSection(parent, tk, Messages.tour_editor_section_date_time);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(20, 5).applyTo(section);

		/*
		 * container: 1. column
		 */
		final Composite tourDtContainer = tk.createComposite(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(tourDtContainer);
		GridDataFactory.fillDefaults().applyTo(tourDtContainer);

		/*
		 * date
		 */
		tk.createLabel(tourDtContainer, Messages.tour_editor_label_tour_date);

		fDtTourDate = new DateTime(tourDtContainer, SWT.DATE | SWT.MEDIUM | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).applyTo(fDtTourDate);
		tk.adapt(fDtTourDate, true, false);
		fDtTourDate.addSelectionListener(fDateTimeListener);

		//////////////////////////////////////
		createUISeparator(tk, tourDtContainer);

		/*
		 * start time
		 */
		tk.createLabel(tourDtContainer, Messages.tour_editor_label_start_time);

		fDtStartTime = new DateTime(tourDtContainer, SWT.TIME | SWT.SHORT | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).applyTo(fDtStartTime);
		tk.adapt(fDtStartTime, true, false);
		fDtStartTime.addSelectionListener(fDateTimeListener);

		//////////////////////////////////////
		createUISeparator(tk, tourDtContainer);

		/*
		 * tour distance
		 */
		tk.createLabel(tourDtContainer, Messages.tour_editor_label_tour_distance);

		fTextTourDistance = tk.createText(tourDtContainer, UI.EMPTY_STRING, SWT.TRAIL);
		GridDataFactory.fillDefaults().applyTo(fTextTourDistance);
		fTextTourDistance.addModifyListener(fVerifyFloatValue);
		fTextTourDistance.setData(WIDGET_KEY, WIDGET_KEY_TOURDISTANCE);

		fLblTourDistanceUnit = tk.createLabel(tourDtContainer, UI.UNIT_LABEL_DISTANCE);

		/*
		 * container: 2. column
		 */
		final Composite timeContainer = tk.createComposite(section);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(timeContainer);
		GridDataFactory.fillDefaults().applyTo(timeContainer);

		/*
		 * recording time
		 */
		tk.createLabel(timeContainer, Messages.tour_editor_label_recording_time);

		fDtRecordingTime = new DateTime(timeContainer, SWT.TIME | SWT.MEDIUM | SWT.BORDER);
		fDtRecordingTime.addSelectionListener(fTourTimeListener);
		tk.adapt(fDtRecordingTime, true, true);

		/*
		 * paused time
		 */
		tk.createLabel(timeContainer, Messages.tour_editor_label_paused_time);

		fDtPausedTime = new DateTime(timeContainer, SWT.TIME | SWT.MEDIUM | SWT.BORDER);
		tk.adapt(fDtPausedTime, true, true);
		fDtPausedTime.addSelectionListener(fTourTimeListener);

		/*
		 * driving time
		 */
		tk.createLabel(timeContainer, Messages.tour_editor_label_driving_time);

		fDtDrivingTime = new DateTime(timeContainer, SWT.TIME | SWT.MEDIUM | SWT.BORDER);
		tk.adapt(fDtDrivingTime, true, true);
		fDtDrivingTime.addSelectionListener(fTourTimeListener);
	}

	private void createSectionInfo(final Composite parent, final FormToolkit tk) {

		final Composite section = createSection(parent, tk, Messages.tour_editor_section_info);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(section);

		/*
		 * reference tours
		 */
		final Label label = tk.createLabel(section, Messages.tour_editor_label_ref_tour);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);

		fLblRefTour = tk.createLabel(section, UI.EMPTY_STRING);

		/*
		 * number of time slices
		 */
		tk.createLabel(section, Messages.tour_editor_label_datapoints);

		fLblTimeSlicesCount = tk.createLabel(section, UI.EMPTY_STRING, SWT.TRAIL);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(fLblTimeSlicesCount);

		/*
		 * device name
		 */
		tk.createLabel(section, Messages.tour_editor_label_device_name);

		fLblDeviceName = tk.createLabel(section, UI.EMPTY_STRING);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(fLblDeviceName);

		/*
		 * person
		 */
		tk.createLabel(section, Messages.tour_editor_label_person);

		fLblPerson = tk.createLabel(section, UI.EMPTY_STRING);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblPerson);

		/*
		 * tour id
		 */
		tk.createLabel(section, Messages.tour_editor_label_tour_id);

		fLblTourId = tk.createLabel(section, UI.EMPTY_STRING);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblTourId);
	}

	private void createSectionTitle(final Composite parent, final FormToolkit tk) {

		Label label;

		final Composite section = createSection(parent, tk, Messages.tour_editor_section_tour);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(section);

		/*
		 * title
		 */
		tk.createLabel(section, Messages.tour_editor_label_tour_title);

		fTextTitle = tk.createText(section, UI.EMPTY_STRING);
		GridDataFactory.fillDefaults()//
				.grab(true, false)
				.applyTo(fTextTitle);
		fTextTitle.addKeyListener(fKeyListener);
		fTextTitle.addModifyListener(fModifyListener);

		/*
		 * description
		 */
		label = tk.createLabel(section, Messages.tour_editor_label_description);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);

		fTextDescription = tk.createText(section, UI.EMPTY_STRING, SWT.BORDER //
				| SWT.WRAP
				| SWT.V_SCROLL
				| SWT.H_SCROLL//
		);

		final IPreferenceStore store = TourbookPlugin.getDefault().getPreferenceStore();

		int descLines = store.getInt(ITourbookPreferences.TOUR_EDITOR_DESCRIPTION_HEIGHT);
		descLines = descLines == 0 ? 5 : descLines;

		GridDataFactory.fillDefaults()//
				.grab(true, false)
				//
				// SWT.DEFAULT causes lot's of problems with the layout therefore the hint is set
				//
				.hint(fTextColumnWidth, fPixelConverter.convertHeightInCharsToPixels(descLines))
				.applyTo(fTextDescription);

		fTextDescription.addModifyListener(fModifyListener);

		/*
		 * start location
		 */
		tk.createLabel(section, Messages.tour_editor_label_start_location);

		fTextStartLocation = tk.createText(section, UI.EMPTY_STRING);
		fTextStartLocation.addModifyListener(fModifyListener);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTextStartLocation);

		/*
		 * end location
		 */
		tk.createLabel(section, Messages.tour_editor_label_end_location);

		fTextEndLocation = tk.createText(section, UI.EMPTY_STRING);
		fTextEndLocation.addModifyListener(fModifyListener);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTextEndLocation);
	}

	/**
	 * @param parent
	 */
	private void createSliceViewer(final Composite parent) {

		// table
		final Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		createSliceViewerContextMenu(table);

//		table.addTraverseListener(new TraverseListener() {
//			public void keyTraversed(final TraverseEvent e) {
//				e.doit = e.keyCode != SWT.CR; // vetoes all CR traversals
//			}
//		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					actionDeleteTimeSlices();
				}
			}
		});

		fSliceViewer = new TableViewer(table);

		if (fIsRowEditMode == false) {
			setCellEditSupport(fSliceViewer);
		}

		/*
		 * create editing support after the viewer is created but before the columns are created
		 */
		final TextCellEditor cellEditor = new TextCellEditor(fSliceViewer.getTable());

		fAltitudeEditingSupport = new SliceIntegerEditingSupport(cellEditor, fSerieAltitude);
		fPulseEditingSupport = new SliceIntegerEditingSupport(cellEditor, fSeriePulse);
		fTemperatureEditingSupport = new SliceIntegerEditingSupport(cellEditor, fSerieTemperature);
		fCadenceEditingSupport = new SliceIntegerEditingSupport(cellEditor, fSerieCadence);

		fColDefAltitude.setEditingSupport(fAltitudeEditingSupport);
		fColDefPulse.setEditingSupport(fPulseEditingSupport);
		fColDefTemperature.setEditingSupport(fTemperatureEditingSupport);
		fColDefCadence.setEditingSupport(fCadenceEditingSupport);

		fColDefSliceMarker.setEditingSupport(new SliceMarkerEditingSupport(cellEditor));

		fSliceColumnManager.createColumns(fSliceViewer);

		fSliceViewer.setContentProvider(new SliceViewerContentProvider());
		fSliceViewer.setUseHashlookup(true);

		fSliceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null) {
					fireSliderPosition(selection);
				}
			}
		});

		// hide first column, this is a hack to align the "first" visible column to right
		table.getColumn(0).setWidth(0);
	}

	private void createSliceViewerContextMenu(final Table table) {

		final MenuManager menuMgr = new MenuManager();

		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				fillSliceContextMenu(manager);
			}
		});

		table.setMenu(menuMgr.createContextMenu(table));
	}

	private void createUI(final Composite parent) {

		fPageBook = new PageBook(parent, SWT.NONE);
		fPageBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fPageNoTour = new Label(fPageBook, SWT.NONE);
		fPageNoTour.setText(Messages.UI_Label_no_chart_is_selected);

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		fPageEditorForm = toolkit.createForm(fPageBook);
		toolkit.decorateFormHeading(fPageEditorForm);

		fMessageManager = new MessageManager(fPageEditorForm);
		fPixelConverter = new PixelConverter(parent);

		final Composite formBody = fPageEditorForm.getBody();
		GridLayoutFactory.fillDefaults().applyTo(formBody);
		formBody.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		fTabFolder = new CTabFolder(formBody, SWT.FLAT | SWT.BOTTOM);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fTabFolder);

		fTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				onSelectTab();
			}
		});

//		fTabFolder.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
////				fIsDirtyDisabled = true;
////				updateUITab();
////				fIsDirtyDisabled = false;
//			}
//		});

		fTabTour = new CTabItem(fTabFolder, SWT.FLAT);
		fTabTour.setText(Messages.tour_editor_tabLabel_tour);
		fTabTour.setControl(createUITabData(fTabFolder, toolkit));

		fTabMarker = new CTabItem(fTabFolder, SWT.FLAT);
		fTabMarker.setText(Messages.tour_editor_tabLabel_tour_marker);
		fTabMarker.setControl(createUITabMarker(fTabFolder));

		fTabSlices = new CTabItem(fTabFolder, SWT.FLAT);
		fTabSlices.setText(Messages.tour_editor_tabLabel_tour_data);
		fTabSlices.setControl(createUITabSlices(fTabFolder));

		fTabInfo = new CTabItem(fTabFolder, SWT.FLAT);
		fTabInfo.setText(Messages.tour_editor_tabLabel_info);
		fTabInfo.setControl(createUITabInfo(fTabFolder, toolkit));

	}

	private void createUISectionSeparator(final FormToolkit tk, final Composite parent) {
		final Composite sep = tk.createComposite(parent);
//		sep.setLayoutData(new ColumnLayoutData(SWT.DEFAULT, 10));
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 5).applyTo(sep);
	}

	private void createUISeparator(final FormToolkit tk, final Composite parent) {
		tk.createLabel(parent, UI.EMPTY_STRING);
	}

	private Composite createUITabData(final Composite parent, final FormToolkit tk) {

		/*
		 * scrolled container
		 */
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				sc.setMinSize(fTourContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		fTourContainer = new Composite(sc, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fTourContainer);
		tk.adapt(fTourContainer);
		GridLayoutFactory.swtDefaults().applyTo(fTourContainer);

		// set content for scrolled composite
		sc.setContent(fTourContainer);

		createSectionTitle(fTourContainer, tk);
		createUISectionSeparator(tk, fTourContainer);

		createSectionDateTime(fTourContainer, tk);
		createUISectionSeparator(tk, fTourContainer);

		createSectionCharacteristics(fTourContainer, tk);

		return sc;
	}

	private Composite createUITabInfo(final Composite parent, final FormToolkit tk) {

		/*
		 * scrolled container
		 */
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				sc.setMinSize(fInfoContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		fInfoContainer = new Composite(sc, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fInfoContainer);
		tk.adapt(fInfoContainer);
		GridLayoutFactory.swtDefaults().applyTo(fInfoContainer);

		// set content for scrolled composite
		sc.setContent(fInfoContainer);

		createSectionInfo(fInfoContainer, tk);

		return sc;
	}

	/**
	 * @param parent
	 * @return returns the controls for the tab
	 */
	private Control createUITabMarker(final Composite parent) {

		final Composite markerContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(markerContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(markerContainer);

		fMarkerViewerContainer = new Composite(markerContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fMarkerViewerContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(fMarkerViewerContainer);

		createMarkerViewer(fMarkerViewerContainer);

		return markerContainer;
	}

	/**
	 * @param parent
	 * @return returns the controls for the tab
	 */
	private Control createUITabSlices(final Composite parent) {

		fSliceContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fSliceContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(fSliceContainer);

		fSliceViewerContainer = new Composite(fSliceContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fSliceViewerContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(fSliceViewerContainer);

		createSliceViewer(fSliceViewerContainer);

		fTimeSliceLabel = new Label(fSliceContainer, SWT.WRAP);
		fTimeSliceLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fTimeSliceLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		fTimeSliceLabel.setVisible(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTimeSliceLabel);

		return fSliceContainer;
	}

//	private Control createUITabMarkerOLDOLDOLDOLDOLDOLDOLD(final Composite parent) {
//
//		final TableLayoutComposite tableLayouter = new TableLayoutComposite(parent, SWT.NONE);
//		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
//		tableLayouter.setLayoutData(gd);
//
//		/*
//		 * create table
//		 */
//		final Table table = new Table(tableLayouter, SWT.FULL_SELECTION | /* SWT.BORDER | */SWT.MULTI);
//
//		table.setLayout(new TableLayout());
//		table.setHeaderVisible(true);
//		table.setLinesVisible(true);
//
//		fMarkerViewer = new TableViewer(table);
//
//		/*
//		 * create columns
//		 */
//		TableViewerColumn tvc;
//		final PixelConverter pixelConverter = new PixelConverter(table);
//
//		final MarkerViewerLabelProvider labelProvider = new MarkerViewerLabelProvider();
//
//		// column: time
//		tvc = new TableViewerColumn(fMarkerViewer, SWT.TRAIL);
//		tvc.getColumn().setText(Messages.Tour_Marker_Column_time);
//		tvc.setLabelProvider(labelProvider);
//		tableLayouter.addColumnData(new ColumnPixelData(pixelConverter.convertWidthInCharsToPixels(8), false));
//
//		// column: distance km/mi
//		tvc = new TableViewerColumn(fMarkerViewer, SWT.TRAIL);
//		tvc.getColumn().setText(UI.UNIT_LABEL_DISTANCE);
//		tvc.getColumn().setToolTipText(Messages.Tour_Marker_Column_km_tooltip);
//		tvc.setLabelProvider(labelProvider);
//		tableLayouter.addColumnData(new ColumnPixelData(pixelConverter.convertWidthInCharsToPixels(8), false));
//
//		// column: marker
//		tvc = new TableViewerColumn(fMarkerViewer, SWT.LEAD);
//		tvc.getColumn().setText(Messages.Tour_Marker_Column_remark);
//		tvc.setLabelProvider(labelProvider);
//		tableLayouter.addColumnData(new ColumnWeightData(1, true));
//
//		/*
//		 * create table viewer
//		 */
//
//		fMarkerViewer.setContentProvider(new MarkerViewerContentProvicer());
//		fMarkerViewer.setLabelProvider(labelProvider);
//		fMarkerViewer.setSorter(new MarkerViewerSorter());
//
//		fMarkerViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(final SelectionChangedEvent event) {
//				final StructuredSelection selection = (StructuredSelection) event.getSelection();
//				if (selection != null) {
//					fireSliderPosition(selection);
//				}
//			}
//		});
//
//		fMarkerViewer.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(final DoubleClickEvent event) {
//
//				// edit selected marker
//				final IStructuredSelection selection = (IStructuredSelection) fMarkerViewer.getSelection();
//				if (selection.size() > 0) {
//					fActionEditTourMarker.setSelectedMarker((TourMarker) selection.getFirstElement());
//					fActionEditTourMarker.run();
//				}
//			}
//		});
//
//		return tableLayouter;
//	}

	private void defineMarkerViewerColumns(final Composite parent) {

		final PixelConverter pixelConverter = new PixelConverter(parent);

		ColumnDefinition colDef;

		/*
		 * column: time
		 */
		colDef = TableColumnFactory.TOUR_TIME_HH_MM_SS.createColumn(fMarkerColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				cell.setText(UI.formatSeconds(((TourMarker) cell.getElement()).getTime()));
			}
		});

		/*
		 * column: distance
		 */
		colDef = TableColumnFactory.DISTANCE.createColumn(fMarkerColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TourMarker marker = (TourMarker) cell.getElement();
				fNumberFormatter.setMinimumFractionDigits(3);
				fNumberFormatter.setMaximumFractionDigits(3);

				cell.setText(fNumberFormatter.format((marker.getDistance()) / (1000 * UI.UNIT_VALUE_DISTANCE)));
			}
		});

		/*
		 * column: marker
		 */
		fColDefMarker = colDef = TableColumnFactory.MARKER.createColumn(fMarkerColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TourMarker tourMarker = (TourMarker) cell.getElement();

				cell.setText(tourMarker.getLabel());

				if (tourMarker.getType() == ChartMarker.MARKER_TYPE_DEVICE) {
					cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
			}
		});
	}

	private void defineSliceViewerColumns(final Composite parent) {

		final PixelConverter pixelConverter = new PixelConverter(parent);

		ColumnDefinition colDef;

		/*
		 * 1. column will be hidden because the alignment for the first column is always to the left
		 */
		colDef = TableColumnFactory.FIRST_COLUMN.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setCanModifyVisibility(false);
		colDef.setIsColumnMoveable(false);
		colDef.setHideColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {}
		});

		/*
		 * column: #
		 */
		colDef = TableColumnFactory.SEQUENCE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setCanModifyVisibility(false);
		colDef.setIsColumnMoveable(false);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final int serieIndex = ((TimeSlice) cell.getElement()).serieIndex;
				final int logIndex = ((TimeSlice) cell.getElement()).uniqueCreateIndex;

				// the UI shows the time slice number starting with 1 and not with 0
				cell.setText(Integer.toString(logIndex + 1));

				// mark reference tour with a different background color
				boolean isBgSet = false;

				if (fRefTourRange != null) {
					for (final int[] oneRange : fRefTourRange) {
						if (serieIndex >= oneRange[0] && serieIndex <= oneRange[1]) {
							cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
							isBgSet = true;
							break;
						}
					}
				}

				if (isBgSet == false) {
					cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				}
			}
		});

		/*
		 * column: time hh:mm:ss
		 */
		colDef = TableColumnFactory.TOUR_TIME_HH_MM_SS.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final int serieIndex = ((TimeSlice) cell.getElement()).serieIndex;
				cell.setText(UI.formatSeconds(fSerieTime[serieIndex]));
			}
		});

		/*
		 * column: time in seconds
		 */
		colDef = TableColumnFactory.TOUR_TIME.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				if (fSerieTime != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					final int serieIndex = timeSlice.serieIndex;
					cell.setText(Integer.toString(fSerieTime[serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: distance
		 */
		colDef = TableColumnFactory.DISTANCE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				if (fSerieDistance != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					final int serieIndex = timeSlice.serieIndex;
					fNumberFormatter.setMinimumFractionDigits(3);
					fNumberFormatter.setMaximumFractionDigits(3);
					cell.setText(fNumberFormatter.format((float) fSerieDistance[serieIndex] / 1000));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: altitude
		 */
		fColDefAltitude = colDef = TableColumnFactory.ALTITUDE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieAltitude != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieAltitude[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: pulse
		 */
		fColDefPulse = colDef = TableColumnFactory.PULSE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSeriePulse != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSeriePulse[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: temperature
		 */
		fColDefTemperature = colDef = TableColumnFactory.TEMPERATURE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieTemperature != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieTemperature[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: cadence
		 */
		fColDefCadence = colDef = TableColumnFactory.CADENCE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieCadence != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieCadence[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: gradient
		 */
		colDef = TableColumnFactory.GRADIENT.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieGradient != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSerieGradient[timeSlice.serieIndex] / 10));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: speed
		 */
		colDef = TableColumnFactory.SPEED.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieSpeed != null) {

					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSerieSpeed[timeSlice.serieIndex] / 10));

				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: pace
		 */
		colDef = TableColumnFactory.PACE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSeriePace != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSeriePace[timeSlice.serieIndex] / 10));

				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: power
		 */
		colDef = TableColumnFactory.POWER.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSeriePower != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSeriePower[timeSlice.serieIndex]));

				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: longitude
		 */
		colDef = TableColumnFactory.LONGITUDE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieLongitude != null) {

					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Double.toString(fSerieLongitude[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: latitude
		 */
		colDef = TableColumnFactory.LATITUDE.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieLatitude != null) {

					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Double.toString(fSerieLatitude[timeSlice.serieIndex]));
				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

		/*
		 * column: marker
		 */
		fColDefSliceMarker = colDef = TableColumnFactory.MARKER.createColumn(fSliceColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				final TimeSlice timeSlice = (TimeSlice) cell.getElement();

				final TourMarker tourMarker = fMarkerMap.get(timeSlice.serieIndex);
				if (tourMarker != null) {
					cell.setText(tourMarker.getLabel());

					if (tourMarker.getType() == ChartMarker.MARKER_TYPE_DEVICE) {
						cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
					}

				} else {
					cell.setText(UI.EMPTY_STRING);
				}
			}
		});

	}

	/**
	 * Discard modifications and fire revert event
	 */
	private void discardModifications() {

		setTourClean();

		fMessageManager.removeAllMessages();

		fTourData = reloadTourData();
		updateMarkerMap();
		updateUIFromTourData(fTourData, true, true);

		fireRevertNotification();
	}

	/**
	 * tries to get tour data from the last selection or from a tour provider
	 */
	private void displaySelectedTour() {

		// show tour from last selection
		onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

		if (fTourData == null) {
			getAndDisplayTour();
		}
	}

	private void displayTour(final Long tourId) {

		if (tourId == null) {
			return;
		}

		// don't reload the same tour
		if (fTourData != null) {
			if (fTourData.getTourId().equals(tourId)) {
				return;
			}
		}

		final TourData tourData = TourManager.getInstance().getTourData(tourId);
		if (tourData != null) {
			fTourChart = null;
			updateUIFromTourData(tourData, false, true);
		}
	}

	private void displayTour(final TourData tourData) {

		if (tourData == null) {
			return;
		}

		// don't reload the same tour
		if (fTourData == tourData) {
			return;
		}

		fTourChart = null;
		updateUIFromTourData(tourData, true, true);
	}

	@Override
	public void dispose() {

		final IWorkbenchPage page = getSite().getPage();

		page.removePostSelectionListener(fPostSelectionListener);
		page.removePartListener(fPartListener);

		TourbookPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fPrefChangeListener);

		TourManager.getInstance().removePropertyListener(fTourPropertyListener);
		TourManager.getInstance().removeTourSaveListener(fTourSaveListener);

		super.dispose();
	}

	/**
	 * saving is done in the {@link #promptToSaveOnClose()} method
	 */
	public void doSave(final IProgressMonitor monitor) {}

	public void doSaveAs() {}

	private void enableActions() {

		final boolean isTourValid = isTourValid() && isTourInDb();

		/*
		 * tour can only be saved when the tour is saved in the database
		 */
		fActionSaveTour.setEnabled(fIsTourDirty && isTourValid);
		fActionUndoChanges.setEnabled(fIsTourDirty);

		fActionOpenAdjustAltitudeDialog.setEnabled(isTourValid);
		fActionOpenMarkerDialog.setEnabled(isTourValid);

		final CTabItem selectedTab = fTabFolder.getSelection();
		final boolean isTableViewerTab = selectedTab == fTabSlices || selectedTab == fTabMarker;

		fActionModifyColumns.setEnabled(isTableViewerTab && isTourValid);
		fActionToggleRowSelectMode.setEnabled(isTableViewerTab && isTourValid);
	}

	private void enableControls() {

		final boolean isTourInDb = isTourInDb();
		final boolean isManualTour = false;

		fTextTitle.setEnabled(isTourInDb);
		fTextDescription.setEnabled(isTourInDb);

		fTextStartLocation.setEnabled(isTourInDb);
		fTextEndLocation.setEnabled(isTourInDb);

		fDtTourDate.setEnabled(isManualTour);
		fDtStartTime.setEnabled(isManualTour);

		fDtRecordingTime.setEnabled(isManualTour);
		fDtDrivingTime.setEnabled(isManualTour);
		fDtPausedTime.setEnabled(isManualTour);

		fTextTourDistance.setEnabled(isManualTour);

		fTagLink.setEnabled(isTourInDb);
		fTourTypeLink.setEnabled(isTourInDb);

		fMarkerViewer.getTable().setEnabled(isTourInDb && !isManualTour);
		fSliceViewer.getTable().setEnabled(isTourInDb && !isManualTour);
	}

	private void fillMarkerContextMenu(final IMenuManager menuMgr) {

		menuMgr.add(fActionOpenMarkerDialog);

		menuMgr.add(new Separator());
		menuMgr.add(fActionDeleteTourMarker);

		// add standard group which allows other plug-ins to contribute here
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// set the marker which should be selected in the marker dialog
		final IStructuredSelection selection = (IStructuredSelection) fMarkerViewer.getSelection();
		fActionOpenMarkerDialog.setSelectedMarker((TourMarker) selection.getFirstElement());

		/*
		 * enable actions
		 */
		final boolean tourInDb = isTourInDb();

		fActionOpenMarkerDialog.setEnabled(tourInDb);
		fActionDeleteTourMarker.setEnabled(tourInDb);
	}

	private void fillSliceContextMenu(final IMenuManager menuMgr) {

		menuMgr.add(fActionCreateTourMarker);

		menuMgr.add(new Separator());
		menuMgr.add(fActionDeleteTimeSlices);

		/*
		 * enable actions
		 */
		final StructuredSelection sliceSelection = (StructuredSelection) fSliceViewer.getSelection();
		final boolean isOneTimeSlice = sliceSelection.size() == 1;

		final boolean isTourInDb = isTourInDb();

		fActionCreateTourMarker.setEnabled(isTourInDb && isOneTimeSlice);
		fActionDeleteTimeSlices.setEnabled(isTourInDb && sliceSelection.size() > 0);
	}

	private void fillToolbar() {

		/*
		 * fill view toolbar
		 */
		final IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();

		tbm.add(fActionSaveTour);

		tbm.add(new Separator());
		tbm.add(fActionOpenMarkerDialog);
		tbm.add(fActionOpenAdjustAltitudeDialog);
		tbm.add(fActionToggleRowSelectMode);

		tbm.update(true);

		/*
		 * fill toolbar view menu
		 */
		final IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();

		menuMgr.add(fActionUndoChanges);
		menuMgr.add(new Separator());

		menuMgr.add(fActionModifyColumns);
	}

	/**
	 * fire notification for changed tour data
	 */
	private void fireModifyNotification() {

		final ArrayList<TourData> modifiedTour = new ArrayList<TourData>();
		modifiedTour.add(fTourData);

		final TourProperties propertyData = new TourProperties(modifiedTour);
		propertyData.isTourEdited = true;

		TourManager.firePropertyChange(TourProperty.TOUR_PROPERTIES_CHANGED, propertyData, TourDataEditorView.this);
	}

	/**
	 * fire notification for the reverted tour data
	 */
	private void fireRevertNotification() {

		final ArrayList<TourData> modifiedTour = new ArrayList<TourData>();
		modifiedTour.add(fTourData);

		final TourProperties tourProperties = new TourProperties(modifiedTour);
		tourProperties.isReverted = true;

		TourManager.firePropertyChange(TourProperty.TOUR_PROPERTIES_CHANGED, tourProperties, TourDataEditorView.this);
	}

	/**
	 * select the chart slider(s) according to the selected marker(s)
	 * 
	 * @return
	 */
	private SelectionChartXSliderPosition fireSliderPosition(final StructuredSelection selection) {

		if (fTourChart == null) {

			final TourChart tourChart = TourManager.getInstance().getActiveTourChart();

			if (tourChart == null || tourChart.isDisposed()) {
				return null;
			} else {
				fTourChart = tourChart;
			}
		}

		final Object[] selectedData = selection.toArray();
		if (selectedData == null || selectedData.length == 0) {
			return null;
		}

		SelectionChartXSliderPosition xSliderPosition = null;
		final Object firstItem = selectedData[0];

		if (selectedData.length > 1) {

			// two or more data are selected, set the 2 sliders to the first and last selected data

			if (firstItem instanceof TimeSlice) {

				xSliderPosition = new SelectionChartXSliderPosition(fTourChart,
						((TimeSlice) firstItem).serieIndex,
						((TimeSlice) selectedData[selectedData.length - 1]).serieIndex);

			} else if (firstItem instanceof TourMarker) {

				xSliderPosition = new SelectionChartXSliderPosition(fTourChart,
						((TourMarker) firstItem).getSerieIndex(),
						((TourMarker) selectedData[selectedData.length - 1]).getSerieIndex(),
						true);
			}

		} else if (selectedData.length > 0) {

			// one data is selected

			if (firstItem instanceof TimeSlice) {

				xSliderPosition = new SelectionChartXSliderPosition(fTourChart,
						((TimeSlice) firstItem).serieIndex,
						SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION);

			} else if (firstItem instanceof TourMarker) {

				xSliderPosition = new SelectionChartXSliderPosition(fTourChart,
						((TourMarker) firstItem).getSerieIndex(),
						SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION,
						true);
			}
		}

		if (xSliderPosition != null) {
			fPostSelectionProvider.setSelection(xSliderPosition);
		}

		return xSliderPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(final Class adapter) {

		if (adapter == ColumnViewer.class) {
			return fSliceViewer;
		}

		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * when a tour is not displayed, find a tour provider which provides a tour
	 */
	private void getAndDisplayTour() {

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {

				/*
				 * check if tour is set from a selection provider
				 */
				if (fTourData != null) {
					return;
				}

				final ArrayList<TourData> selectedTours = TourManager.getSelectedTours();
				if (selectedTours != null && selectedTours.size() > 0) {

					// get first tour, this view shows only one tour
					displayTour(selectedTours.get(0));

					setTourClean();
				}
			}
		});
	}

	public ColumnManager getColumnManager() {

		final CTabItem selectedTab = fTabFolder.getSelection();

		if (selectedTab == fTabSlices) {
			return fSliceColumnManager;
		} else if (selectedTab == fTabMarker) {
			return fMarkerColumnManager;
		}

		return null;
	}

	/**
	 * Converts a string into a float value
	 * 
	 * @param valueText
	 * @return Returns the float value for the parameter valueText, return <code>0</code>
	 * @throws IllegalArgumentException
	 */
	private float getFloatValue(String valueText) throws IllegalArgumentException {

		valueText = valueText.trim();
		if (valueText.length() == 0) {

			return 0;

		} else {

			final Object convertedValue = StringToNumberConverter.toFloat(true).convert(valueText);
			if (convertedValue instanceof Float) {
				return ((Float) convertedValue).floatValue();
			}
		}

		return 0;
	}

	private double[] getRemainingDoubleSerieData(final double[] dataSerie, final int firstIndex, final int lastIndex) {

		final int oldSerieLength = dataSerie.length;
		final int newSerieLength = oldSerieLength - (lastIndex - firstIndex + 1);

		final double[] newDataSerie = new double[newSerieLength];

		if (firstIndex == 0) {

			// delete from start, copy data by skipping removed slices
			System.arraycopy(dataSerie, lastIndex + 1, newDataSerie, 0, newSerieLength);

		} else if (lastIndex == oldSerieLength - 1) {

			// delete until the end 
			System.arraycopy(dataSerie, 0, newDataSerie, 0, newSerieLength);

		} else {

			// delete somewhere in the middle

			// copy start segment
			System.arraycopy(dataSerie, 0, newDataSerie, 0, firstIndex);

			// copy end segment
			final int copyLength = oldSerieLength - (lastIndex + 1);
			System.arraycopy(dataSerie, lastIndex + 1, newDataSerie, firstIndex, copyLength);
		}

		return newDataSerie;
	}

	private int[] getRemainingIntegerSerieData(final int[] oldDataSerie, final int firstIndex, final int lastIndex) {

		final int oldSerieLength = oldDataSerie.length;
		final int newSerieLength = oldSerieLength - (lastIndex - firstIndex + 1);

		final int[] newDataSerie = new int[newSerieLength];

		if (firstIndex == 0) {

			// delete from start, copy data by skipping removed slices
			System.arraycopy(oldDataSerie, lastIndex + 1, newDataSerie, 0, newSerieLength);

		} else if (lastIndex == oldSerieLength - 1) {

			// delete until the end 
			System.arraycopy(oldDataSerie, 0, newDataSerie, 0, newSerieLength);

		} else {

			// delete somewhere in the middle

			// copy start segment
			System.arraycopy(oldDataSerie, 0, newDataSerie, 0, firstIndex);

			// copy end segment
			final int copyLength = oldSerieLength - (lastIndex + 1);
			System.arraycopy(oldDataSerie, lastIndex + 1, newDataSerie, firstIndex, copyLength);
		}

		return newDataSerie;
	}

	private TimeSlice[] getRemainingViewerItems(final Object[] dataViewerItems,
												final int firstIndex,
												final int lastIndex) {

		final int oldSerieLength = dataViewerItems.length;
		final int newSerieLength = oldSerieLength - (lastIndex - firstIndex + 1);

		final TimeSlice[] newViewerItems = new TimeSlice[newSerieLength];

		if (firstIndex == 0) {

			// delete from start, copy data by skipping removed slices
			System.arraycopy(dataViewerItems, lastIndex + 1, newViewerItems, 0, newSerieLength);

		} else if (lastIndex == oldSerieLength - 1) {

			// get items from start, delete until the end 
			System.arraycopy(dataViewerItems, 0, newViewerItems, 0, newSerieLength);

		} else {

			// delete somewhere in the middle

			// copy start segment
			System.arraycopy(dataViewerItems, 0, newViewerItems, 0, firstIndex);

			// copy end segment
			final int copyLength = oldSerieLength - (lastIndex + 1);
			System.arraycopy(dataViewerItems, lastIndex + 1, newViewerItems, firstIndex, copyLength);
		}

		// update serie index
		int serieIndex = 0;
		for (final TimeSlice timeSlice : newViewerItems) {
			timeSlice.serieIndex = serieIndex++;
		}

		return newViewerItems;
	}

	public ArrayList<TourData> getSelectedTours() {

		if (fTourData == null) {
			return null;
		}

		final ArrayList<TourData> tourDataList = new ArrayList<TourData>();
		tourDataList.add(fTourData);

		return tourDataList;
	}

	TableViewer getSliceViewer() {
		return fSliceViewer;
	}

	private Object[] getSliceViewerItems() {

		if (fTourData == null) {
			return new Object[0];
		}

		updateDataSeriesFromTourData();

		/*
		 * create viewer elements (time slices), each viewer item contains the index into the data
		 * series
		 */
		final TimeSlice[] viewerItems = new TimeSlice[fTourData.timeSerie.length];
		for (int serieIndex = 0; serieIndex < viewerItems.length; serieIndex++) {
			viewerItems[serieIndex] = new TimeSlice(serieIndex);
		}

		if (viewerItems.length == 0) {
			return viewerItems;
		}
		return viewerItems;
	}

	/**
	 * @return Returns {@link TourData} for the tour in the tour data editor or <code>null</code>
	 *         when a tour is not in the tour data editor
	 */
	public TourData getTourData() {
		return fTourData;
	}

	/**
	 * @return Returns the title of the active tour
	 */
	public String getTourTitle() {
		return TourManager.getTourTitle(fTourData);
	}

	public ColumnViewer getViewer() {

		final CTabItem selectedTab = fTabFolder.getSelection();

		if (selectedTab == fTabSlices) {
			return fSliceViewer;
		} else if (selectedTab == fTabMarker) {
			return fMarkerViewer;
		}

		return null;
	}

	/**
	 * @return Returns <code>true</code> when the data have been modified and not saved
	 */
	public boolean isDirty() {
		return fIsTourDirty;
	}

	/**
	 * @return Returns <code>true</code> when the tour should be discarded<br>
	 *         returns <code>false</code> when the tour is invalid but should be saved<br>
	 */
	private boolean isDiscardTour() {

		final MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
				Messages.tour_editor_dlg_save_tour_title,
				null,
				NLS.bind(Messages.tour_editor_dlg_save_invalid_tour, TourManager.getTourDateFull(fTourData)),
				MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
				1);

		final int result = dialog.open();
		if (result == 0) {

			// discard modifications

			return true;

		} else {

			// save modifications

			return false;
		}
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		return isDirty();
	}

	/**
	 * @return Returns <code>true</code> when the tour is saved in the database
	 */
	private boolean isTourInDb() {

		if (fTourData != null && fTourData.getTourPerson() != null) {
			return true;
		}

		return false;
	}

	/**
	 * Checks the selection if it contains the current tour, {@link #fSelectionTourId} contains the
	 * tour id which is within the selection
	 * 
	 * @param selection
	 * @return Returns <code>true</code> when the current tour is within the selection
	 */
	private boolean isTourInSelection(final ISelection selection) {

		boolean isCurrentTourSelected = false;

		if (fTourData == null) {
			return false;
		}

		TourData selectedTourData = null;
		final long currentTourId = fTourData.getTourId();

		if (selection instanceof SelectionTourData) {

			final TourData tourData = ((SelectionTourData) selection).getTourData();
			if (tourData == null) {
				return false;
			}

			fSelectionTourId = tourData.getTourId();

			if (tourData != null && currentTourId == fSelectionTourId) {
				isCurrentTourSelected = true;
				selectedTourData = tourData;
			}

		} else if (selection instanceof SelectionTourId) {

			fSelectionTourId = ((SelectionTourId) selection).getTourId();

			if (currentTourId == fSelectionTourId) {
				isCurrentTourSelected = true;
			}

		} else if (selection instanceof SelectionChartInfo) {

			final SelectionChartInfo chartInfo = (SelectionChartInfo) selection;
			final ChartDataModel chartDataModel = chartInfo.chartDataModel;
			if (chartDataModel != null) {

				final Object tourId = chartDataModel.getCustomData(TourManager.CUSTOM_DATA_TOUR_ID);
				if (tourId instanceof Long) {

					final TourData tourData = TourManager.getInstance().getTourData((Long) tourId);
					if (tourData != null) {

						fSelectionTourId = tourData.getTourId();
						if (currentTourId == fSelectionTourId) {

							isCurrentTourSelected = true;
							selectedTourData = tourData;

							// select time slices
							selectTimeSlice(chartInfo);
						}
					}
				}
			}

		} else if (selection instanceof SelectionChartXSliderPosition) {

			final SelectionChartXSliderPosition xSliderPosition = (SelectionChartXSliderPosition) selection;

			final Chart chart = xSliderPosition.getChart();
			if (chart != null) {

				final ChartDataModel chartDataModel = chart.getChartDataModel();
				if (chartDataModel != null) {

					final Object tourId = chartDataModel.getCustomData(TourManager.CUSTOM_DATA_TOUR_ID);
					if (tourId instanceof Long) {

						final TourData tourData = TourManager.getInstance().getTourData((Long) tourId);
						if (tourData != null) {

							fSelectionTourId = tourData.getTourId();
							if (currentTourId == fSelectionTourId) {

								isCurrentTourSelected = true;
								selectedTourData = tourData;

								// select time slices
								selectTimeSlice(xSliderPosition);
							}
						}
					}
				}
			}

		} else if (selection instanceof StructuredSelection) {

			final Object firstElement = ((StructuredSelection) selection).getFirstElement();

			if (firstElement instanceof TVICatalogComparedTour) {
				fSelectionTourId = ((TVICatalogComparedTour) firstElement).getTourId();
				if (currentTourId == fSelectionTourId) {
					isCurrentTourSelected = true;
				}

			} else if (firstElement instanceof TVICompareResultComparedTour) {

				final long comparedTourTourId = ((TVICompareResultComparedTour) firstElement).getComparedTourData()
						.getTourId();

				fSelectionTourId = comparedTourTourId;
				if (currentTourId == fSelectionTourId) {
					isCurrentTourSelected = true;
				}
			}
		}

		if (selectedTourData != null) {
			try {
				UI.checkTourData(selectedTourData, fTourData);
			} catch (final MyTourbookException e) {
				System.out.println("Selection:" + selection);//$NON-NLS-1$
				e.printStackTrace();
			}
		}

		return isCurrentTourSelected;
	}

	/**
	 * Checks if tour has no errors
	 * 
	 * @return Returns <code>true</code> when all data for the tour are valid, <code>false</code>
	 *         otherwise
	 */
	private boolean isTourValid() {

		if (fTourData == null) {
			return false;
		}

		if (fIsTourDirty) {

			if (fTourData.getTourPerson() == null) {

				// tour is modified but not yet saved in the database

				fMessageManager.addMessage(WIDGET_KEY_PERSON,
						Messages.tour_editor_message_person_is_required,
						null,
						IMessageProvider.ERROR,
						fLblPerson);

			} else {
				fMessageManager.removeMessage(WIDGET_KEY_PERSON, fLblPerson);
			}

			// tour is valid when there are no error messages

			return fMessageManager.getErrorMessageCount() == 0;

		} else {

			// tour is not dirty

			return true;
		}
	}

	/**
	 * fires a slider position for the marker viewer and selects the corresponding time slice
	 */
	private void onMarkerViewerSelectionChanged() {

		final StructuredSelection selection = (StructuredSelection) fMarkerViewer.getSelection();
		if (selection != null) {

			final SelectionChartXSliderPosition xSliderPosition = fireSliderPosition(selection);
			if (xSliderPosition != null) {

				// position slice viewer to the marker position

				// keep position for the slice viewer when it was not yet displayed for the current tour
				fSliceViewerXSliderPosition = xSliderPosition;

				if (fSliceViewerTourId == fTourData.getTourId()) {
					selectTimeSlice(xSliderPosition);
				}
			}
		}
	}

	private void onSelectionChanged(final ISelection selection) {

		if (fIsSavingInProgress) {
			return;
		}

		if (isTourInSelection(selection)) {

			/*
			 * tour in the selection is already displayed or a tour is not in the selection
			 */

			if (fIsInfoInTitle) {
				showDefaultTitle();
			}

			return;

		} else {

			// another tour is selected, show info

			if (fIsTourDirty) {

				if (fIsInfoInTitle == false) {

					/*
					 * show info only when it is not yet displayed, this is an optimization because
					 * setting the message causes an layout and this is EXTREMLY SLOW because of the
					 * bad date time controls
					 */

					// hide title
					fPageEditorForm.setText(UI.EMPTY_STRING);

					// show info
					fMessageManager.addMessage(MESSAGE_KEY_ANOTHER_SELECTION,
							NLS.bind(Messages.tour_editor_message_show_another_tour, getTourTitle()),
							null,
							IMessageProvider.WARNING);

					fIsInfoInTitle = true;
				}

				return;
			}
		}

		if (fIsInfoInTitle) {
			showDefaultTitle();
		}

		if (selection instanceof SelectionTourData) {

			final SelectionTourData selectionTourData = (SelectionTourData) selection;
			final TourData tourData = selectionTourData.getTourData();
			if (tourData == null) {
				fTourChart = null;
			} else {

				final TourChart tourChart = selectionTourData.getTourChart();

				fTourChart = tourChart;
				updateUIFromTourData(tourData, false, true);
			}

		} else if (selection instanceof SelectionTourId) {

			displayTour(((SelectionTourId) selection).getTourId());

		} else if (selection instanceof SelectionTourCatalogView) {

			final SelectionTourCatalogView tourCatalogSelection = (SelectionTourCatalogView) selection;

			final TVICatalogRefTourItem refItem = tourCatalogSelection.getRefItem();
			if (refItem != null) {
				displayTour(refItem.getTourId());
			}

		} else if (selection instanceof SelectionChartInfo) {

			final ChartDataModel chartDataModel = ((SelectionChartInfo) selection).chartDataModel;
			if (chartDataModel != null) {

				final Object tourId = chartDataModel.getCustomData(TourManager.CUSTOM_DATA_TOUR_ID);
				if (tourId instanceof Long) {

					final TourData tourData = TourManager.getInstance().getTourData((Long) tourId);
					if (tourData != null) {

						if (fTourData == null) {

							fTourData = tourData;
							fTourChart = null;
							updateMarkerMap();
							updateUIFromTourData(tourData, false, true);

						} else {

							if (fTourData.getTourId() != tourData.getTourId()) {

								// a new tour id is in the selection
								fTourData = tourData;
								fTourChart = null;
								updateMarkerMap();
								updateUIFromTourData(tourData, false, true);
							}
						}
					}
				}
			}

		} else if (selection instanceof StructuredSelection) {

			final Object firstElement = ((StructuredSelection) selection).getFirstElement();
			if (firstElement instanceof TVICatalogComparedTour) {

				displayTour(((TVICatalogComparedTour) firstElement).getTourId());

			} else if (firstElement instanceof TVICompareResultComparedTour) {

				displayTour(((TVICompareResultComparedTour) firstElement).getComparedTourData().getTourId());
			}
		}
	}

	private void onSelectTab() {

		if (fTabFolder.getSelection() == fTabSlices) {

			if (fSliceViewerTourId == -1L) {
				// load viewer when this is not done
				fSliceViewerTourId = fTourData.getTourId();

				reloadViewer();
				updateStatusLine();

				// run asynch because relaodViewer is also running asynch
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						selectTimeSlice(fSliceViewerXSliderPosition);
						fSliceViewer.getTable().setFocus();
					}
				});
			}

		}

		enableActions();

	}

	/*
	 * this method is called when the application is shut down to save dirty tours or to cancel the
	 * shutdown
	 * @see org.eclipse.ui.ISaveablePart2#promptToSaveOnClose()
	 */
	public int promptToSaveOnClose() {

		int returnCode;

		if (fIsTourDirty == false) {
			returnCode = ISaveablePart2.NO;
		}

		fIsSavingInProgress = true;
		{
			if (saveTourValidation()) {
				returnCode = ISaveablePart2.NO;
			} else {
				returnCode = ISaveablePart2.CANCEL;
			}
		}
		fIsSavingInProgress = false;

		return returnCode;
	}

	private void recreateViewer() {

		// recreate slice viewer 
		fSliceColumnManager.saveState(fViewStateSlice);
		fSliceColumnManager.clearColumns();

		defineSliceViewerColumns(fSliceViewerContainer);
		fSliceViewer = (TableViewer) recreateViewer(fSliceViewer);

		// recreate marker viewer 
		fMarkerColumnManager.saveState(fViewStateMarker);
		fMarkerColumnManager.clearColumns();

		defineMarkerViewerColumns(fMarkerViewerContainer);
		fMarkerViewer = (TableViewer) recreateViewer(fMarkerViewer);
	}

	public ColumnViewer recreateViewer(final ColumnViewer columnViewer) {

		final ColumnViewer[] newColumnViewer = new ColumnViewer[1];

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

			private void recreateMarkerViewer() {

				// preserve column width, selection and focus
				final ISelection selection = fMarkerViewer.getSelection();

				final Table table = fMarkerViewer.getTable();
				final boolean isFocus = table.isFocusControl();

				fMarkerViewerContainer.setRedraw(false);
				{
					fMarkerViewerContainer.getChildren()[0].dispose();
//					table.dispose();

					createMarkerViewer(fMarkerViewerContainer);
					fMarkerViewerContainer.layout();

					// update the viewer
					fMarkerViewer.setInput(new Object[0]);
				}
				fMarkerViewerContainer.setRedraw(true);

				fMarkerViewer.setSelection(selection, true);
				if (isFocus) {
					fMarkerViewer.getTable().setFocus();
				}

				newColumnViewer[0] = fMarkerViewer;
			}

			private void recreateSliceViewer() {

				// preserve column width, selection and focus
				final ISelection selection = fSliceViewer.getSelection();

				final Table table = fSliceViewer.getTable();
				final boolean isFocus = table.isFocusControl();

				fSliceViewerContainer.setRedraw(false);
				{
					table.dispose();

					createSliceViewer(fSliceViewerContainer);
					fSliceViewerContainer.layout();

					// update the viewer
					fSliceViewerItems = getSliceViewerItems();
					fSliceViewer.setInput(fSliceViewerItems);
				}
				fSliceViewerContainer.setRedraw(true);

				fSliceViewer.setSelection(selection, true);
				if (isFocus) {
					fSliceViewer.getTable().setFocus();
				}

				newColumnViewer[0] = fSliceViewer;
			}

			public void run() {

				if (columnViewer == fSliceViewer) {

					recreateSliceViewer();

				} else if (columnViewer == fMarkerViewer) {

					recreateMarkerViewer();
				}
			}
		});

		return newColumnViewer[0];
	}

	private TourData reloadTourData() {

		if (fTourData.getTourPerson() == null) {

			// tour is not saved, reloading tour data is not possible

			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					Messages.tour_editor_dlg_reload_data_title,
					Messages.tour_editor_dlg_reload_data_message);

			return fTourData;
		}

		final Long tourId = fTourData.getTourId();
		final TourManager tourManager = TourManager.getInstance();

		tourManager.removeTourFromCache(tourId);

		return tourManager.getTourData(tourId);
	}

	/**
	 * reload the content of the viewer
	 */
	public void reloadViewer() {

		Display.getCurrent().asyncExec(new Runnable() {

			private void reloadMarkerViewer() {

				final ISelection previousSelection = fMarkerViewer.getSelection();

				final Table table = fMarkerViewer.getTable();
				if (table.isDisposed()) {
					return;
				}

				table.setRedraw(false);
				{
					fMarkerViewer.setInput(new Object[0]);
					fMarkerViewer.setSelection(previousSelection, true);
				}
				table.setRedraw(true);
			}

			private void reloadSliceViewer() {

				final ISelection previousSelection = fSliceViewer.getSelection();

				final Table table = fSliceViewer.getTable();
				if (table.isDisposed()) {
					return;
				}

				table.setRedraw(false);
				{
					/*
					 * update the viewer, show busy indicator when it's a large tour or the previous
					 * tour was large because it takes time to remove the old items
					 */
					if (fTourData != null
							&& fTourData.timeSerie != null
							&& fTourData.timeSerie.length > BUSY_INDICATOR_ITEMS
							|| table.getItemCount() > BUSY_INDICATOR_ITEMS) {

						BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
							public void run() {
								fSliceViewerItems = getSliceViewerItems();
								fSliceViewer.setInput(fSliceViewerItems);
							}
						});
					} else {
						fSliceViewerItems = getSliceViewerItems();
						fSliceViewer.setInput(fSliceViewerItems);
					}

					fSliceViewer.setSelection(previousSelection, true);
				}
				table.setRedraw(true);
			}

			public void run() {

				final CTabItem selectedTab = fTabFolder.getSelection();

				if (selectedTab == fTabSlices) {
					reloadSliceViewer();
				} else if (selectedTab == fTabMarker) {
					reloadMarkerViewer();
				}

			}
		});
	}

	/**
	 * Removes markers which are deleted and updates marker serie index which are positioned after
	 * the deleted time slices
	 * 
	 * @param firstSerieIndex
	 * @param lastSerieIndex
	 */
	private void removeTourMarkers(final int firstSerieIndex, final int lastSerieIndex) {

		// check if markers are available
		final Set<TourMarker> tourMarkers = fTourData.getTourMarkers();
		if (tourMarkers.size() == 0) {
			return;
		}

		// remove deleted markers from tourData
		final TourMarker[] markerCopy = tourMarkers.toArray(new TourMarker[tourMarkers.size()]);

		for (final TourMarker tourMarker : markerCopy) {

			final int markerSerieIndex = tourMarker.getSerieIndex();
			if (markerSerieIndex >= firstSerieIndex && markerSerieIndex <= lastSerieIndex) {
				tourMarkers.remove(tourMarker);
//				System.out.println(tourMarker + " isRemoved: " + isRemoved);
			}
		}
		updateMarkerMap();

		// update marker index
		final int diffSerieIndex = lastSerieIndex - firstSerieIndex + 1;

		for (final TourMarker tourMarker : fMarkerMap.values()) {

			final int markerSerieIndex = tourMarker.getSerieIndex();

			if (markerSerieIndex > lastSerieIndex) {
				tourMarker.setSerieIndex(markerSerieIndex - diffSerieIndex);
			}
		}
		updateMarkerMap();
	}

	private void restoreStateBeforeUI() {

		// row or column edit mode
		fIsRowEditMode = fViewState.getBoolean(MEMENTO_ROW_EDIT_MODE);
	}

	private void restoreStateWithUI() {

		// select tab
		try {
			fTabFolder.setSelection(fViewState.getInt(MEMENTO_SELECTED_TAB));
		} catch (final NumberFormatException e) {
			fTabFolder.setSelection(fTabTour);
		}

		fActionToggleRowSelectMode.setChecked(fIsRowEditMode);
	}

	private void saveState() {

		// selected tab
		fViewState.put(MEMENTO_SELECTED_TAB, fTabFolder.getSelectionIndex());

		// row/column edit mode
		fViewState.put(MEMENTO_ROW_EDIT_MODE, fActionToggleRowSelectMode.isChecked());

		fSliceColumnManager.saveState(fViewStateSlice);
		fMarkerColumnManager.saveState(fViewStateMarker);
	}

	/**
	 * @param isConfirmSave
	 * @return Returns <code>true</code> when the tour was saved, <code>false</code> when the tour
	 *         is not saved but canceled
	 */
	private boolean saveTourConfirmation() {

		if (fIsTourDirty == false) {
			return true;
		}

		// show the tour data editor
		try {
			getSite().getPage().showView(ID, null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (final PartInitException e) {
			e.printStackTrace();
		}

		// confirm save/discard/cancel
		final int returnCode = new MessageDialog(Display.getCurrent().getActiveShell(),
				Messages.tour_editor_dlg_save_tour_title,
				null,
				NLS.bind(Messages.tour_editor_dlg_save_tour_message, TourManager.getTourDateFull(fTourData)),
				MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL },
				0)//
		.open();

		if (returnCode == 0) {

			// button YES: save tour

			saveTourIntoDB();

			return true;

		} else if (returnCode == 1) {

			// button NO: discard modifications

			discardModifications();

			return true;

		} else {

			// button CANCEL / dialog is canceled: tour is not saved and not discarded

			return false;
		}
	}

	/**
	 * saves the tour when it is dirty, valid and confirmation is done
	 */
	private boolean saveTourIntoDB() {

		fIsSavingInProgress = true;

		updateTourDataFromUI();

		fTourData = TourDatabase.saveTour(fTourData);
		updateMarkerMap();

		setTourClean();

		// notify all views which display the tour type
		final ArrayList<TourData> modifiedTour = new ArrayList<TourData>();
		modifiedTour.add(fTourData);

		TourManager.firePropertyChange(TourProperty.TOUR_PROPERTIES_CHANGED,
				new TourProperties(modifiedTour),
				TourDataEditorView.this);

		fIsSavingInProgress = false;

		return true;
	}

	/**
	 * saves the tour in the {@link TourDataEditorView}
	 * 
	 * @return Returns <code>true</code> when the tour is saved or <code>false</code> when the tour
	 *         could not saved because the user canceled saving
	 */
	private boolean saveTourValidation() {

		if (fTourData == null) {
			return true;
		}

		if (isTourValid()) {

			return saveTourConfirmation();

		} else {

			// tour is invalid

			if (isDiscardTour()) {

				// discard modifications
				discardModifications();

				return true;

			} else {

				/*
				 * tour is not saved because the tour is invalid and should not be discarded
				 */

				return false;
			}
		}
	}

	private void selectTimeSlice(final SelectionChartInfo chartInfo) {

		final Table table = (Table) fSliceViewer.getControl();
		final int itemCount = table.getItemCount();

		// adjust to array bounds 
		int valueIndex = chartInfo.selectedSliderValuesIndex;
		valueIndex = Math.max(0, Math.min(valueIndex, itemCount - 1));

		table.setSelection(valueIndex);
		table.showSelection();

		// fire slider position
//		fDataViewer.setSelection(fDataViewer.getSelection());
	}

	private void selectTimeSlice(final SelectionChartXSliderPosition sliderPosition) {

		if (sliderPosition == null) {
			return;
		}

		final Table table = (Table) fSliceViewer.getControl();
		final int itemCount = table.getItemCount();

		final int valueIndex1 = sliderPosition.getSlider1ValueIndex();
		final int valueIndex2 = sliderPosition.getSlider2ValueIndex();

		// adjust to array bounds 
		final int checkedValueIndex1 = Math.max(0, Math.min(valueIndex1, itemCount - 1));
		final int checkedValueIndex2 = Math.max(0, Math.min(valueIndex2, itemCount - 1));

		if (valueIndex1 == SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION
				&& valueIndex1 == SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION) {
			return;
		}

		if (valueIndex1 == SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION) {
			table.setSelection(checkedValueIndex2);
		} else if (valueIndex2 == SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION) {
			table.setSelection(checkedValueIndex1);
		} else {
			table.setSelection(checkedValueIndex1, checkedValueIndex2);
		}

		table.showSelection();
	}

	/**
	 * initialize cell editing
	 * 
	 * @param viewer
	 */
	private void setCellEditSupport(final TableViewer viewer) {

		final TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer,
				new FocusCellOwnerDrawHighlighter(viewer));

		final ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(viewer, //
				focusCellManager,
				actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL //
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR //
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}

	@Override
	public void setFocus() {

// !!! disabled because the first field gets the focus !!!
//		fTabFolder.setFocus();

		fPageEditorForm.setFocus();
	}

	/**
	 * removes the dirty state from the tour editor, updates the save/undo actions and updates the
	 * part name
	 */
	private void setTourClean() {

		fIsTourDirty = false;

		enableActions();
		enableControls();

		showDefaultTitle();

		/*
		 * this is not an eclipse editor part but the property change must be fired to hide the "*"
		 * marker in the part name
		 */
		firePropertyChange(PROP_DIRTY);
	}

	/**
	 * sets the tour editor dirty, updates the save/undo actions and updates the part name
	 */
	private void setTourDirty() {

		if (fIsTourDirty) {
			return;
		}

		fIsTourDirty = true;

		enableActions();

		/*
		 * this is not an eclipse editor part but the property change must be fired to show the "*"
		 * marker in the part name
		 */
		firePropertyChange(PROP_DIRTY);
	}

	/**
	 * show the default title in the editor
	 */
	private void showDefaultTitle() {

		fMessageManager.removeMessage(MESSAGE_KEY_ANOTHER_SELECTION);
		updateUITitle();

		fIsInfoInTitle = false;
	}

	private void updateContentOnKeyUp() {

		if (fTourData == null) {
			return;
		}
		// set changed data
		fTourData.setTourTitle(fTextTitle.getText());

		enableActions();

		fireModifyNotification();
	}

	private void updateDataSeriesFromTourData() {

		fSerieTime = fTourData.timeSerie;

		fSerieDistance = fTourData.getDistanceSerie();
		fSerieAltitude = fTourData.getAltitudeSerie();
		fSerieTemperature = fTourData.getTemperatureSerie();
		fSerieGradient = fTourData.getGradientSerie();
		fSerieSpeed = fTourData.getSpeedSerie();
		fSeriePace = fTourData.getPaceSerie();
		fSeriePower = fTourData.getPowerSerie();

		fSerieCadence = fTourData.cadenceSerie;
		fSeriePulse = fTourData.pulseSerie;

		fSerieLatitude = fTourData.latitudeSerie;
		fSerieLongitude = fTourData.longitudeSerie;

		fAltitudeEditingSupport.setDataSerie(fSerieAltitude);
		fTemperatureEditingSupport.setDataSerie(fSerieTemperature);
		fPulseEditingSupport.setDataSerie(fSeriePulse);
		fCadenceEditingSupport.setDataSerie(fSerieCadence);
	}

	/**
	 * converts the {@link TourMarker} from {@link TourData} into the map {@link #fMarkerMap}
	 */
	void updateMarkerMap() {

		fMarkerMap.clear();

		final Set<TourMarker> tourMarkers = fTourData.getTourMarkers();

		for (final TourMarker tourMarker : tourMarkers) {
			fMarkerMap.put(tourMarker.getSerieIndex(), tourMarker);
		}
	}

	private void updateRefTourInfo(final Collection<TourReference> refTours) {

		final ArrayList<TourReference> refTourList = new ArrayList<TourReference>(refTours);

		// sort reference tours by start index
		Collections.sort(refTourList, new Comparator<TourReference>() {
			public int compare(final TourReference refTour1, final TourReference refTour2) {
				return refTour1.getStartValueIndex() - refTour2.getStartValueIndex();
			}
		});

		final StringBuilder sb = new StringBuilder();
		int refCounter = 0;

		fRefTourRange = new int[refTourList.size()][2];

		for (final TourReference refTour : refTourList) {

			if (refCounter > 0) {
				sb.append(UI.NEW_LINE);
			}

			sb.append(refTour.getLabel());

			sb.append(" ("); //$NON-NLS-1$
			sb.append(refTour.getStartValueIndex());
			sb.append(UI.DASH_WITH_SPACE);
			sb.append(refTour.getEndValueIndex());
			sb.append(")"); //$NON-NLS-1$

			final int[] oneRange = fRefTourRange[refCounter];
			oneRange[0] = refTour.getStartValueIndex();
			oneRange[1] = refTour.getEndValueIndex();

			refCounter++;
		}

		fLblRefTour.setText(sb.toString());
		fLblRefTour.pack(true);
	}

	private void updateStatusLine() {

		final boolean isVisible = fTimeSliceLabel.isVisible();
		boolean setVisible = false;

		if (fIsReferenceTourAvailable) {

			// tour contains reference tours

			fTimeSliceLabel.setText(Messages.TourDataEditorView_tour_editor_status_tour_contains_ref_tour);
			setVisible = true;

		} else {

			fTimeSliceLabel.setText(UI.EMPTY_STRING);
		}

		if (isVisible != setVisible) {

			// changes visibility

			fTimeSliceLabel.setVisible(setVisible);

			fSliceContainer.layout(true, true);
		}
	}

	/**
	 * update {@link TourData} from the UI fields
	 */
	private void updateTourDataFromUI() {

		try {

			fTourData.setTourTitle(fTextTitle.getText());
			fTourData.setTourDescription(fTextDescription.getText());

			fTourData.setTourStartPlace(fTextStartLocation.getText());
			fTourData.setTourEndPlace(fTextEndLocation.getText());

			fTourData.setStartYear((short) fDtTourDate.getYear());
			fTourData.setStartMonth((short) (fDtTourDate.getMonth() + 1));
			fTourData.setStartDay((short) fDtTourDate.getDay());

			fTourData.setStartHour((short) fDtStartTime.getHours());
			fTourData.setStartMinute((short) fDtStartTime.getMinutes());

			// set week of year
			fCalendar.set(fTourData.getStartYear(), fTourData.getStartMonth() - 1, fTourData.getStartDay());
			fTourData.setStartWeek((short) fCalendar.get(Calendar.WEEK_OF_YEAR));

			final float distanceValue = getFloatValue(fTextTourDistance.getText()) * UI.UNIT_VALUE_DISTANCE * 1000;
			fTourData.setTourDistance((int) distanceValue);

		} catch (final IllegalArgumentException e) {

			// this should not happen (but it happend when developing the tour data editor :-)
			//
			// wrong characters are entered, display an error message

			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", e.getLocalizedMessage());//$NON-NLS-1$

			e.printStackTrace();
		}
	}

//	/**
//	 * update each tab separately
//	 */
//	private void updateUITab() {
//
////		final CTabItem selectedTab = fTabFolder.getSelection();
////
////		if (selectedTab == fTabTour) {
////			updateUITabTour();
////		} else if (selectedTab == fTabMarker) {
////			updateUITabMarker();
////		} else if (selectedTab == fTabSlices) {
////			updateUITabSlices();
////		} else if (selectedTab == fTabInfo) {
////			updateUITabInfo();
////		}
//
//	}

	/**
	 * Updates the UI from {@link TourData}, dirty flag is not set
	 * 
	 * @param tourData
	 */
	public void updateUI(final TourData tourData) {

		updateUIFromTourData(tourData, true, true);
	}

	public void updateUI(final TourData tourData, final boolean isDirty) {

		updateUI(tourData);

		if (isDirty) {
			setTourDirty();
		}
	}

	/**
	 * updates the fields in the tour data editor and enables actions and controls
	 * 
	 * @param tourData
	 * @param forceTimeSliceReload
	 *            <code>true</code> will reload time slices
	 * @param isModifyEventDisabled
	 */
	private void updateUIFromTourData(	final TourData tourData,
										final boolean forceTimeSliceReload,
										final boolean isModifyEventDisabled) {

		if (tourData == null) {
			fPageBook.showPage(fPageNoTour);
			return;
		}

		fUIUpdateCounter++;

		/*
		 * set tour data because the TOUR_PROPERTIES_CHANGED event can occure before the runnable is
		 * executed, this ensures that tour data is already set even if the ui is not yet updated
		 */
		fTourData = tourData;
		updateMarkerMap();

		final Runnable runnable = new Runnable() {

			final int	fRunnableCounter	= fUIUpdateCounter;

			public void run() {

				/*
				 * update the UI
				 */

				// check if this is the last runnable
				if (fRunnableCounter != fUIUpdateCounter) {
					// a new runnable was created
					return;
				}

				fUIRunnableTourData = tourData;
				fUIRunnableForceTimeSliceReload = forceTimeSliceReload;
				fUIRunnableIsDirtyDisabled = isModifyEventDisabled;

				// force reload
				fUIRunnableCounter = fUIUpdateCounter - 1;

				if (fIsPartVisible) {
					updateUIRunnable();
				}
			}
		};

		Display.getCurrent().asyncExec(runnable);
	}

	/**
	 * update UI data point counter
	 */
	private void updateUINumberOfTimeSlice() {

		final int[] timeSerie = fTourData.timeSerie;
		if (timeSerie == null) {
			fLblTimeSlicesCount.setText(UI.EMPTY_STRING);
		} else {
			final int dataPoints = timeSerie.length;
			fLblTimeSlicesCount.setText(Integer.toString(dataPoints));
		}
		fLblTimeSlicesCount.pack(true);
	}

	private void updateUIRunnable() {

		if (fUIRunnableCounter == fUIUpdateCounter) {
			// UI is updated
			return;
		}

		fUIRunnableCounter = fUIUpdateCounter;

		if (fPageEditorForm.isDisposed() || fUIRunnableTourData == null) {
			// widget is disposed or data is not set
			return;
		}

		fIsDirtyDisabled = fUIRunnableIsDirtyDisabled;

		// keep tour data
		fTourData = fUIRunnableTourData;
		updateMarkerMap();

		// a tour which is not saved has no tour references
		final Collection<TourReference> tourReferences = fUIRunnableTourData.getTourReferences();
		if (tourReferences == null) {
			fIsReferenceTourAvailable = false;
		} else {
			fIsReferenceTourAvailable = tourReferences.size() > 0;
		}

		// show tour type image when tour type is set
		final TourType tourType = fUIRunnableTourData.getTourType();
		if (tourType == null) {
			fPageEditorForm.setImage(null);
		} else {
			fPageEditorForm.setImage(UI.getInstance().getTourTypeImage(tourType.getTypeId()));
		}

		updateUITitleAsynch(getTourTitle());

		updateUITabTour();
		updateUITabMarker();
		updateUITabSlices();
		updateUITabInfo();

		enableActions();
		enableControls();

		fPageBook.showPage(fPageEditorForm);

		fIsDirtyDisabled = false;
	}

	private void updateUITabInfo() {

//		if (fUIUpdateCounterTabInfo == fUIRunnableCounter) {
//			return;
//		}
//
//		fUIUpdateCounterTabInfo = fUIRunnableCounter;

		// data points
		updateUINumberOfTimeSlice();

		// device name
		fLblDeviceName.setText(fTourData.getDeviceName());
		fLblDeviceName.pack(true);

		/*
		 * reference tours
		 */
		final Collection<TourReference> refTours = fTourData.getTourReferences();
		if (refTours.size() > 0) {
			updateRefTourInfo(refTours);
		} else {
			fLblRefTour.setText(Messages.tour_editor_label_ref_tour_none);
			fRefTourRange = null;
		}

		/*
		 * person
		 */
		final TourPerson tourPerson = fTourData.getTourPerson();
		if (tourPerson == null) {
			fLblPerson.setText(UI.EMPTY_STRING);
		} else {
			fLblPerson.setText(tourPerson.getName());
		}

		/*
		 * tour ID
		 */
		final Long tourId = fTourData.getTourId();
		if (tourId == null) {
			fLblTourId.setText(UI.EMPTY_STRING);
		} else {
			fLblTourId.setText(Long.toString(tourId));
		}

		/*
		 * layout container to resize labels
		 */
		fInfoContainer.layout(true);
	}

	private void updateUITabMarker() {

//		if (fUIUpdateCounterTabMarker == fUIRunnableCounter) {
//			return;
//		}
//
//		fUIUpdateCounterTabMarker = fUIRunnableCounter;

		fMarkerViewer.setInput(new Object[0]);
	}

	private void updateUITabSlices() {

//		if (fUIUpdateCounterTabSlices == fUIRunnableCounter) {
//			return;
//		}
//
//		fUIUpdateCounterTabSlices = fUIRunnableCounter;

		if (fUIRunnableForceTimeSliceReload) {
			fSliceViewerTourId = -1L;
		}

		if (fTabFolder.getSelection() == fTabSlices && fSliceViewerTourId != fTourData.getTourId()) {

			/*
			 * time slice tab is selected and the viewer is not yeat loaded
			 */

			reloadViewer();
			fSliceViewerTourId = fTourData.getTourId();

			updateStatusLine();

		} else {

			if (fSliceViewerTourId != fTourData.getTourId()) {
				// force reload when it's not yet loaded
				fSliceViewerTourId = -1L;
			}
		}
	}

	private void updateUITabTour() {

//		if (fUIUpdateCounterTabTour == fUIRunnableCounter) {
//			return;
//		}
//
//		fUIUpdateCounterTabTour = fUIRunnableCounter;

		final short tourYear = fTourData.getStartYear();
		final int tourMonth = fTourData.getStartMonth() - 1;
		final short tourDay = fTourData.getStartDay();

		// title/description
		fTextTitle.setText(fTourData.getTourTitle());
		fTextDescription.setText(fTourData.getTourDescription());

		// tour date
		fDtTourDate.setDate(tourYear, tourMonth, tourDay);

		// start time
		fDtStartTime.setTime(fTourData.getStartHour(), fTourData.getStartMinute(), 0);

		// recording time
		final int recordingTime = fTourData.getTourRecordingTime();
		fDtRecordingTime.setTime(recordingTime / 3600, ((recordingTime % 3600) / 60), ((recordingTime % 3600) % 60));

		// driving time
		final int drivingTime = fTourData.getTourDrivingTime();
		fDtDrivingTime.setTime(drivingTime / 3600, ((drivingTime % 3600) / 60), ((drivingTime % 3600) % 60));

		// paused time
		final int pausedTime = recordingTime - drivingTime;
		fDtPausedTime.setTime(pausedTime / 3600, ((pausedTime % 3600) / 60), ((pausedTime % 3600) % 60));

		// start/end location
		fTextStartLocation.setText(fTourData.getTourStartPlace());
		fTextEndLocation.setText(fTourData.getTourEndPlace());

		UI.updateUITourType(fTourData, fLblTourType);
		UI.updateUITags(fTourData, fLblTourTags);

		// tour distance
		final int tourDistance = fTourData.getTourDistance();
		if (tourDistance == 0) {
			fTextTourDistance.setText(Integer.toString(tourDistance));
		} else {

			fNumberFormatter.setMinimumFractionDigits(3);
			fNumberFormatter.setMaximumFractionDigits(3);
			fNumberFormatter.setGroupingUsed(false);

			final float distance = ((float) tourDistance) / 1000 / UI.UNIT_VALUE_DISTANCE;
			fTextTourDistance.setText(fNumberFormatter.format(distance));

		}
		fLblTourDistanceUnit.setText(UI.UNIT_LABEL_DISTANCE);

		/*
		 * person
		 */
		final TourPerson tourPerson = fTourData.getTourPerson();
		if (tourPerson == null) {
			fLblPerson.setText(UI.EMPTY_STRING);
		} else {
			fLblPerson.setText(tourPerson.getName());
		}

		/*
		 * tour ID
		 */
		final Long tourId = fTourData.getTourId();
		if (tourId == null) {
			fLblTourId.setText(UI.EMPTY_STRING);
		} else {
			fLblTourId.setText(Long.toString(tourId));
		}

		/*
		 * layout container to resize labels
		 */
		fTourContainer.layout(true);

	}

	private void updateUITitle() {

		updateUITitle(fDtTourDate.getYear(),
				fDtTourDate.getMonth(),
				fDtTourDate.getDay(),
				fDtStartTime.getHours(),
				fDtStartTime.getMinutes());
	}

	/**
	 * update title of the view
	 * 
	 * @param tourYear
	 * @param tourMonth
	 * @param tourDay
	 * @param hour
	 * @param minute
	 */
	private void updateUITitle(	final int tourYear,
								final int tourMonth,
								final int tourDay,
								final int hour,
								final int minute) {

		fCalendar.set(tourYear, tourMonth, tourDay, hour, minute);

		updateUITitleAsynch(TourManager.getTourTitle(fCalendar.getTime()));
	}

	/**
	 * update the title is a really performance hog because of the date/time controls when they are
	 * layouted
	 */
	private void updateUITitleAsynch(final String title) {

		fUIUpdateTitleCounter++;

		Display.getCurrent().asyncExec(new Runnable() {

			final int	runnableCounter	= fUIUpdateTitleCounter;

			public void run() {

				if (fPageEditorForm.isDisposed()) {
					return;
				}

				// check if this is the last runnable
				if (runnableCounter != fUIUpdateTitleCounter) {
					// a new runnable was created
					return;
				}

				fPageEditorForm.setText(title);
			}
		});
	}

}
