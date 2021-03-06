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
import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.device.garmin.GarminDeviceDataReader;
import net.tourbook.device.garmin.GarminSAXHandler;
import net.tourbook.device.suunto.Suunto9DeviceDataReader;
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
	private static final String GARMIN_IMPORT_PATH = "/importdata/garmin/tcx/files/"; //$NON-NLS-1$
	private static final String SUUNTO_IMPORT_FILE_PATH = "test/importdata/suunto9/files/"; //$NON-NLS-1$
	private static final String JSON_GZ = ".json.gz"; //$NON-NLS-1$

   private static DeviceData              deviceData;
   private static HashMap<Long, TourData> newlyImportedTours;
   private static HashMap<Long, TourData> alreadyImportedTours;
	private static GarminDeviceDataReader garminDeviceDataReader;
	private static Suunto9DeviceDataReader suunto9DeviceDataReader;

	private static final IPreferenceStore _prefStore = TourbookPlugin.getPrefStore();

   @BeforeAll
   static void initAll() {

      parser = Initializer.initializeParser();
      deviceData = new DeviceData();
      newlyImportedTours = new HashMap<>();
      alreadyImportedTours = new HashMap<>();
		garminDeviceDataReader = new GarminDeviceDataReader();
		suunto9DeviceDataReader = new Suunto9DeviceDataReader();
   }

	@AfterEach
   void tearDown() {
      newlyImportedTours.clear();
      alreadyImportedTours.clear();

		// Restoring the default values
		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, false);
   }

   	/**
	 * Suunto JSON.GZ with pauses using the moving time.
	 */
	@Test
	void testCadenceZonesSuunto9TimeWithMovingTime() {

		final String filePath = SUUNTO_IMPORT_FILE_PATH + "1590349595199_183010004848_post_timeline-1"; //$NON-NLS-1$

		final String testFilePath = Paths.get(filePath + JSON_GZ).toAbsolutePath().toString();
		suunto9DeviceDataReader.processDeviceData(testFilePath, deviceData, alreadyImportedTours, newlyImportedTours,
				false);

		final TourData tour = Comparison.retrieveImportedTour(newlyImportedTours);

		tour.computeCadenceZonesTimes();

		Assertions.assertEquals(70, tour.getCadenceZones_DelimiterValue());
		Assertions.assertEquals(852, tour.getCadenceZone_FastTime());
		Assertions.assertEquals(9795, tour.getCadenceZone_SlowTime());
		Assertions.assertEquals((long) (tour.getCadenceZone_SlowTime()) + tour.getCadenceZone_FastTime(),
				tour.getTourComputedTime_Moving());
	}

	/**
	 * Suunto JSON.GZ with pauses using the recorded time. It's a special case
	 * because the total time for both cadence zones could be at most 1 second
	 * different than the total tour recorded time as the milliseconds omitted
	 * when calling {@link TourData#getPausedTime()} can create this
	 * discrepancy. It does not exist for the FIT files for example since the
	 * pause times don't contain milliseconds
	 */
	@Test
	void testCadenceZonesSuunto9TimeWithRecordedTime() {

		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, true);

		final String filePath = SUUNTO_IMPORT_FILE_PATH + "1590349595199_183010004848_post_timeline-1"; //$NON-NLS-1$

		final String testFilePath = Paths.get(filePath + JSON_GZ).toAbsolutePath().toString();
		suunto9DeviceDataReader.processDeviceData(testFilePath, deviceData, alreadyImportedTours, newlyImportedTours,
				false);

		final TourData tour = Comparison.retrieveImportedTour(newlyImportedTours);

		tour.computeCadenceZonesTimes();

		Assertions.assertEquals(70, tour.getCadenceZones_DelimiterValue());
		Assertions.assertEquals(2125, tour.getCadenceZone_FastTime());
		Assertions.assertEquals(14663, tour.getCadenceZone_SlowTime());
		Assertions.assertEquals((long) (tour.getCadenceZone_SlowTime()) + tour.getCadenceZone_FastTime(),
				tour.getTourDeviceTime_Recorded() - 1);
	}

	/**
	 * TCX file with pauses using the moving time
	 */
   @Test
	void testCadenceZonesTimeWithMovingTime() throws SAXException, IOException {

		final String filePathWithoutExtension = GARMIN_IMPORT_PATH + "2021-01-31"; //$NON-NLS-1$
      final String importFilePath = filePathWithoutExtension + ".tcx"; //$NON-NLS-1$
		final InputStream tcxFile = GarminTcxTester.class.getResourceAsStream(importFilePath);

      final GarminSAXHandler handler = new GarminSAXHandler(
				garminDeviceDataReader,
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
				tour.getTourComputedTime_Moving());
   }

	/**
	 * TCX file with pauses using the recorded time
	 */
   @Test
	void testCadenceZonesTimeWithRecordedTime() throws SAXException, IOException {

		_prefStore.setValue(ITourbookPreferences.APPEARANCE_IS_PACEANDSPEED_FROM_RECORDED_TIME, true);

		final String filePathWithoutExtension = GARMIN_IMPORT_PATH + "2021-01-31"; //$NON-NLS-1$
      final String importFilePath = filePathWithoutExtension + ".tcx"; //$NON-NLS-1$
		final InputStream tcxFile = GarminTcxTester.class.getResourceAsStream(importFilePath);

      final GarminSAXHandler handler = new GarminSAXHandler(
				garminDeviceDataReader,
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
