/*******************************************************************************
 * Copyright (C) 2021 Frédéric Bard
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
package tourdata.cadence;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.device.garmin.GarminDeviceDataReader;
import net.tourbook.device.garmin.GarminSAXHandler;
import net.tourbook.importdata.DeviceData;
import net.tourbook.preferences.ITourbookPreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import importdata.garmin.tcx.GarminTcxTester;
import utils.Comparison;
import utils.Initializer;

public class CadenceTester {

   private static SAXParser               parser;
   private static final String            IMPORT_PATH = "/importdata/garmin/tcx/files/"; //$NON-NLS-1$

   private static DeviceData              deviceData;
   private static HashMap<Long, TourData> newlyImportedTours;
   private static HashMap<Long, TourData> alreadyImportedTours;
   private static GarminDeviceDataReader  deviceDataReader;

	private static final IPreferenceStore _prefStore = TourbookPlugin.getPrefStore();

   @BeforeAll
   static void initAll() {
      parser = Initializer.initializeParser();
      deviceData = new DeviceData();
      newlyImportedTours = new HashMap<>();
      alreadyImportedTours = new HashMap<>();
      deviceDataReader = new GarminDeviceDataReader();
   }

	@AfterEach
   void tearDown() {
      newlyImportedTours.clear();
      alreadyImportedTours.clear();

		// Restoring the default values
		_prefStore.setValue(ITourbookPreferences.BREAK_TIME_MIN_SLICE_TIME_AS, 2);
		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, false);
   }

   	/**
	 * TCX file with pauses using the moving time
	 */
   @Test
	void testCadenceZonesTimeWithMovingTime() throws SAXException, IOException {

		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, false);
		_prefStore.setValue(ITourbookPreferences.BREAK_TIME_MIN_SLICE_TIME_AS, 1);

      final String filePathWithoutExtension = IMPORT_PATH + "2021-01-31"; //$NON-NLS-1$
      final String importFilePath = filePathWithoutExtension + ".tcx"; //$NON-NLS-1$
		final InputStream tcxFile = GarminTcxTester.class.getResourceAsStream(importFilePath);

      final GarminSAXHandler handler = new GarminSAXHandler(
            deviceDataReader,
            importFilePath,
            deviceData,
            alreadyImportedTours,
            newlyImportedTours);

      parser.parse(tcxFile, handler);

      final TourData tour = Comparison.retrieveImportedTour(newlyImportedTours);

		tour.computeCadenceZonesTimes();

		Assertions.assertEquals(70, tour.getCadenceZones_DelimiterValue());
		Assertions.assertEquals(69, tour.getCadenceZone_FastTime());
		Assertions.assertEquals(440, tour.getCadenceZone_SlowTime());
		Assertions.assertEquals((long) (tour.getCadenceZone_SlowTime()) + tour.getCadenceZone_FastTime(),
				tour.getTourComputedTime_Moving());
   }

	/**
	 * TCX file with pauses using the recorded time
	 */
   @Test
	void testCadenceZonesTimeWithRecordedTime() throws SAXException, IOException {

		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, true);

      final String filePathWithoutExtension = IMPORT_PATH + "2021-01-31"; //$NON-NLS-1$
      final String importFilePath = filePathWithoutExtension + ".tcx"; //$NON-NLS-1$
		final InputStream tcxFile = GarminTcxTester.class.getResourceAsStream(importFilePath);

      final GarminSAXHandler handler = new GarminSAXHandler(
            deviceDataReader,
            importFilePath,
            deviceData,
            alreadyImportedTours,
            newlyImportedTours);

      parser.parse(tcxFile, handler);

      final TourData tour = Comparison.retrieveImportedTour(newlyImportedTours);

		tour.computeCadenceZonesTimes();

		Assertions.assertEquals(70, tour.getCadenceZones_DelimiterValue());
		Assertions.assertEquals(294, tour.getCadenceZone_FastTime());
		Assertions.assertEquals(1601, tour.getCadenceZone_SlowTime());
		Assertions.assertEquals((long) (tour.getCadenceZone_SlowTime()) + tour.getCadenceZone_FastTime(),
				tour.getTourDeviceTime_Recorded());
   }
}
