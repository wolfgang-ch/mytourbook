/*******************************************************************************
 * Copyright (C) 2005, 2007  Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourCatalog;

import net.tourbook.Messages;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.ISliderMoveListener;
import net.tourbook.chart.SelectionChartInfo;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.data.TourReference;
import net.tourbook.tour.IDataModelListener;
import net.tourbook.tour.SelectionTourChart;
import net.tourbook.tour.TourChart;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.views.TourChartViewPart;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;

// author: Wolfgang Schramm
// create: 09.07.2007

public class TourCatalogViewReferenceTour extends TourChartViewPart {

	public static final String	ID				= "net.tourbook.views.tourCatalog.referenceTourView";	//$NON-NLS-1$

	private long				fActiveRefId	= -1;

	private PageBook			fPageBook;
	private Label				fPageNoChart;

	@Override
	public void createPartControl(final Composite parent) {

		super.createPartControl(parent);

		fPageBook = new PageBook(parent, SWT.NONE);

		fPageNoChart = new Label(fPageBook, SWT.NONE);
		fPageNoChart.setText(Messages.UI_Label_no_chart_is_selected);

		fTourChart = new TourChart(fPageBook, SWT.FLAT, true);
		fTourChart.setShowZoomActions(true);
		fTourChart.setShowSlider(true);
		fTourChart.setToolBarManager(getViewSite().getActionBars().getToolBarManager(), true);

//		fTourChart.addDoubleClickListener(new Listener() {
//			public void handleEvent(final Event event) {
//				TourManager.getInstance().openTourInEditor(fTourData.getTourId());
//			}
//		});

		// set chart title
		fTourChart.addDataModelListener(new IDataModelListener() {
			public void dataModelChanged(final ChartDataModel chartDataModel) {
				chartDataModel.setTitle(TourManager.getTourTitleDetailed(fTourData));
			}
		});

		// fire a slider move selection when a slider was moved in the tour chart
		fTourChart.addSliderMoveListener(new ISliderMoveListener() {
			public void sliderMoved(final SelectionChartInfo chartInfoSelection) {
				fPostSelectionProvider.setSelection(chartInfoSelection);
			}
		});

		// show current selected tour
		final ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		if (selection != null) {
			onSelectionChanged(selection);
		} else {
			fPageBook.showPage(fPageNoChart);
		}
	}

	public TourChart getTourChart() {
		return fTourChart;
	}

	private void onSelectionChanged(final ISelection selection) {

		if (selection instanceof SelectionTourCatalogView) {

			showRefTour(((SelectionTourCatalogView) selection).getRefId());

		} else if (selection instanceof StructuredSelection) {

			final Object firstElement = ((StructuredSelection) selection).getFirstElement();

			if (firstElement instanceof TVICatalogComparedTour) {

				showRefTour(((TVICatalogComparedTour) firstElement).getRefId());

			} else if (firstElement instanceof TVICompareResultComparedTour) {

				showRefTour(((TVICompareResultComparedTour) firstElement).refTour.getRefId());
			}
		}
	}

	@Override
	protected void onSelectionChanged(final IWorkbenchPart part, final ISelection selection) {

//		if (part != TourCatalogViewReferenceTour.this) {
//			return;
//		}

		onSelectionChanged(selection);
	}

	@Override
	public void setFocus() {
		fTourChart.setFocus();

		fPostSelectionProvider.setSelection(new SelectionTourChart(fTourChart));
	}

//	/**
//	 * @param refId
//	 *        Reference Id
//	 * @return
//	 */
//	private TourCompareConfig createTourCompareConfig(final long refId) {
//
//		final ReferenceTourManager refTourManager = ReferenceTourManager.getInstance();
//
//		TourCompareConfig compareConfig = refTourManager.getTourCompareConfig(refId);
//
//		if (compareConfig != null) {
//			return compareConfig;
//		}
//
//		// load the reference tour from the database
//		final EntityManager em = TourDatabase.getInstance().getEntityManager();
//		final TourReference refTour = em.find(TourReference.class, refId);
//		em.close();
//
//		if (refTour == null) {
//			return null;
//		} else {
//
//			/*
//			 * create a new reference tour configuration
//			 */
//
//			final TourData refTourData = refTour.getTourData();
//			final TourChartConfiguration refTourChartConfig = TourManager.createTourChartConfiguration();
//
//			final TourChartConfiguration compTourchartConfig = TourManager.createTourChartConfiguration();
//
//			final ChartDataModel chartDataModel = TourManager.getInstance()
//					.createChartDataModel(refTourData, refTourChartConfig);
//
//			compareConfig = new TourCompareConfig(refTour,
//					chartDataModel,
//					refTourData,
//					refTourChartConfig,
//					compTourchartConfig);
//
//			// keep ref config in the cache
//			refTourManager.setTourCompareConfig(refId, compareConfig);
//		}
//
//		return compareConfig;
//	}

	/**
	 * set the configuration for a reference tour
	 * 
	 * @param compareConfig
	 * @return Returns <code>true</code> then the ref tour changed
	 */
	private void setTourCompareConfig(final TourCompareConfig compareConfig) {

		// save the chart slider positions for the old ref tour
		final TourCompareConfig oldRefTourConfig = ReferenceTourManager.getInstance()
				.getTourCompareConfig(fActiveRefId);

		if (oldRefTourConfig != null) {

			final SelectionChartXSliderPosition oldXSliderPosition = fTourChart.getXSliderPosition();

			oldRefTourConfig.setXSliderPosition(new SelectionChartXSliderPosition(fTourChart,
					oldXSliderPosition.getSlider1ValueIndex(),
					oldXSliderPosition.getSlider2ValueIndex()));
		}

		fTourChart.addDataModelListener(new IDataModelListener() {

			public void dataModelChanged(final ChartDataModel changedChartDataModel) {

				final ChartDataXSerie xData = changedChartDataModel.getXData();
				final TourReference refTour = compareConfig.getRefTour();

				// set marker positions
				xData.setSynchMarkerValueIndex(refTour.getStartValueIndex(), refTour.getEndValueIndex());

				// set the value difference of the synch marker
				final int[] xValues = xData.getHighValues()[0];
				final int refTourXMarkerValue = xValues[refTour.getEndValueIndex()]
						- xValues[refTour.getStartValueIndex()];

				TourManager.firePropertyChange(TourManager.TOUR_PROPERTY_REFERENCE_TOUR_CHANGED,
						new TourPropertyRefTourChanged(fTourChart, refTour.getRefId(), refTourXMarkerValue));

				// set title
				changedChartDataModel.setTitle(NLS.bind(Messages.tourCatalog_view_label_chart_title_reference_tour,
						refTour.getLabel(),
						TourManager.getTourTitleDetailed(compareConfig.getRefTourData())));

			}
		});
	}

	private void showRefTour(final long refId) {

		// check if the ref tour is already displayed
		if (refId == fActiveRefId) {
			return;
		}

		final TourCompareConfig tourCompareConfig = ReferenceTourManager.getInstance().getTourCompareConfig(refId);

		if (tourCompareConfig == null) {
			return;
		}

		/*
		 * show new ref tour
		 */

		fTourData = tourCompareConfig.getRefTourData();
		fTourChartConfig = tourCompareConfig.getRefTourChartConfig();

		setTourCompareConfig(tourCompareConfig);

		// set active ref id after the configuration is set
		fActiveRefId = refId;

		// ???
		fTourChart.zoomOut(false);

		updateChart();

	}

	@Override
	public void updateChart() {

		if (fTourData == null) {
			return;
		}

		fTourChart.updateTourChart(fTourData, fTourChartConfig, false);

		fPageBook.showPage(fTourChart);

		// set application window title
		setTitleToolTip(TourManager.getTourDate(fTourData));
	}

}
