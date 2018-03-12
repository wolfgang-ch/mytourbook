/*******************************************************************************
 * Copyright (C) 2005, 2018 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourCatalog.geo;

import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.data.NormalizedGeoData;
import net.tourbook.data.TourData;
import net.tourbook.tour.TourManager;

public class GeoPartTourComparer {

// SET_FORMATTING_OFF
	
	private static final int COMPARATOR_THREADS = Runtime.getRuntime().availableProcessors();
//	private static final int COMPARATOR_THREADS = 1;

	private static final LinkedBlockingDeque<GeoPartComparerItem>	_compareWaitingQueue	= new LinkedBlockingDeque<>();

	private static ThreadPoolExecutor								_comparerExecutor;
	
// SET_FORMATTING_ON

	static {}

	private static final boolean	LOG_TOUR_COMPARING	= false;

	static GeoPartView				geoPartView;

	static {

		/*
		 * Setup comparer executer
		 */

		final ThreadFactory threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(final Runnable r) {

				final Thread thread = new Thread(r, "Comparing geo tours");//$NON-NLS-1$

				thread.setPriority(Thread.MIN_PRIORITY);
				thread.setDaemon(true);

				return thread;
			}
		};

		System.out.println(
				(String.format(
						"[%s] Comparing tours with %d threads",
						GeoPartTourComparer.class.getSimpleName(),
						COMPARATOR_THREADS)));
// TODO remove SYSTEM.OUT.PRINTLN

		_comparerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(COMPARATOR_THREADS, threadFactory);
	}

	/**
	 * @param loaderItem
	 */
	static void compareGeoTours(final GeoPartItem loaderItem) {

		for (final long tourId : loaderItem.tourIds) {

			final GeoPartComparerItem comparerItem = new GeoPartComparerItem(tourId, loaderItem);

			// keep compared tour
			loaderItem.comparedTours.add(comparerItem);

			_compareWaitingQueue.add(comparerItem);

			_comparerExecutor.submit(new Runnable() {
				@Override
				public void run() {

					// get last added loader item
					final GeoPartComparerItem comparatorItem = _compareWaitingQueue.pollFirst();

					if (comparatorItem == null) {
						return;
					}

					try {

						compareTour(comparatorItem);

					} catch (final Exception e) {

						StatusUtil.log(e);
					}

					geoPartView.compare_50_TourIsCompared(comparatorItem);
				}
			});
		}
	}

	private static void compareTour(final GeoPartComparerItem comparerItem) {

		final GeoPartItem geoPartItem = comparerItem.geoPartItem;

		if (geoPartItem.isCanceled) {
			return;
		}

		/*
		 * Load tour data
		 */
		final long startLoading = System.nanoTime();

		final TourData tourData = TourManager.getInstance().getTourData(comparerItem.tourId);

		/*
		 * Normalize data
		 */
		final long startConvert = System.nanoTime();

		final NormalizedGeoData normalizedPart = geoPartItem.normalizedTourPart;
		final int[] partLatSerie = normalizedPart.normalizedLat;
		final int[] partLonSerie = normalizedPart.normalizedLon;

		final NormalizedGeoData normalizedTour = tourData.getNormalizedLatLon();
		final int[] tourLatSerie = normalizedTour.normalizedLat;
		final int[] tourLonSerie = normalizedTour.normalizedLon;

		final int numPartSlices = partLatSerie.length;
		final int numTourSlices = tourLatSerie.length;

		final long[] tourLatLonDiff = new long[numTourSlices];

		/*
		 * Compare
		 */
		final long startComparing = System.nanoTime();

		long minDiffValue = Long.MAX_VALUE;
		int minDiffIndex = -1;
		int numCompares = 0;

		// loop: all tour slices
		for (int tourIndex = 0; tourIndex < numTourSlices; tourIndex++) {

			long latLonDiff = -1;

			// loop: all part slices
			for (int partIndex = 0; partIndex < numPartSlices; partIndex++) {

				if (geoPartItem.isCanceled) {

//					System.out.println(
//							(" [" + GeoPartTourComparer.class.getSimpleName() + "] isCanceled")
//									+ ("\texecId: " + loaderItem.executorId)
////							+ ("\t: " + )
//					);
//// TODO remove SYSTEM.OUT.PRINTLN

					return;
				}

				numCompares++;

				final int compareIndex = tourIndex + partIndex;

				/*
				 * Make sure the compare index is not larger than the tour index, this happens when
				 * the part slices has exeeded the tour slices
				 */
				if (compareIndex == numTourSlices) {
					latLonDiff = -1;
					break;
				}

				final int latDiff = partLatSerie[partIndex] - tourLatSerie[compareIndex];
				final int lonDiff = partLonSerie[partIndex] - tourLonSerie[compareIndex];

				// optimize Math.abs() !!!
				final int latDiffAbs = latDiff < 0 ? -latDiff : latDiff;
				final int lonDiffAbs = lonDiff >= 0 ? lonDiff : -lonDiff;

				// summarize all diffs for one tour slice
				latLonDiff += (latDiffAbs + lonDiffAbs);
			}

			// keep diff value
			tourLatLonDiff[tourIndex] = latLonDiff;

			// keep min diff value/index
			if (latLonDiff < minDiffValue && latLonDiff != -1) {

				minDiffValue = latLonDiff;

				// keep tour index where the min diff occured
				minDiffIndex = tourIndex;
			}

		}

		// a tour is available
		if (minDiffIndex > -1) {

			final int[] normalizedIndices = normalizedTour.normalized2OriginalIndices;

			final int startIndex = normalizedIndices[minDiffIndex];
			final int endIndex = normalizedIndices[minDiffIndex + numPartSlices - 1];

			comparerItem.avgPulse = tourData.computeAvg_PulseSegment(startIndex, endIndex);
			comparerItem.speed = TourManager.computeTourSpeed(tourData, startIndex, endIndex);
		}

		final ZonedDateTime tourStartTime = tourData.getTourStartTime();

		comparerItem.tourStartTime = tourStartTime;
		comparerItem.tourStartTimeMS = TimeTools.toEpochMilli(tourStartTime);

		comparerItem.tourLatLonDiff = tourLatLonDiff;
		comparerItem.tourMinDiffIndex = minDiffIndex;

		comparerItem.minDiffValue = minDiffIndex < 0 ? -1 : tourLatLonDiff[minDiffIndex];

		if (LOG_TOUR_COMPARING) {

			final float time_Compare = (float) (System.nanoTime() - startComparing) / 1000000;
			final float time_All = (float) (System.nanoTime() - startLoading) / 1000000;
			final float time_Load = (float) (startConvert - startLoading) / 1000000;
			final float time_Convert = (float) (startComparing - startConvert) / 1000000;

			final float cmpAvgTime = numCompares / time_Compare;

			System.out.println(
					String.format(
							""
									+ "[%3d]" // thread
									+ " tour %-20s"
									// + "   exec %5d"

									+ "   diff %12d"
									+ "   # %5d / %5d"

									+ "   cmp %7.0f"
									+ "   #cmp %10d"
									+ "   #cmpAvg %8.0f"

									+ "   all %7.0f ms"
									+ "   ld %10.4f"
									+ "   cnvrt %10.4f",

							Thread.currentThread().getId(),
							comparerItem.tourId,
							//							loaderItem.executorId,

							minDiffIndex < 0 ? minDiffIndex : tourLatLonDiff[minDiffIndex],
							numTourSlices,
							numPartSlices,

							time_Compare,
							numCompares,
							cmpAvgTime,

							time_All,
							time_Load,
							time_Convert

					));
			// TODO remove SYSTEM.OUT.PRINTLN
		}
	}

}
