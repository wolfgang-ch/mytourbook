/*******************************************************************************
 * Copyright (C) 2005, 2009  Wolfgang Schramm and Contributors
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
/**
 * @author Alfred Barten
 */
package net.tourbook.ext.srtm.download;

import java.io.File;
import java.io.FileNotFoundException;

import net.tourbook.ext.srtm.Activator;
import net.tourbook.ext.srtm.GeoLat;
import net.tourbook.ext.srtm.GeoLon;
import net.tourbook.ext.srtm.IPreferences;
import net.tourbook.ext.srtm.PrefPageSRTM;

import org.eclipse.jface.preference.IPreferenceStore;

public class DownloadSRTM3 {

	private static final String		URL_SEPARATOR		= "/";

	private final static int		DirEurasia			= 0;
	private final static int		DirNorth_America	= 1;
	private final static int		DirSouth_America	= 2;
	private final static int		DirAfrica			= 3;
	private final static int		DirAustralia		= 4;
	private final static int		DirIslands			= 5;

	private static String[]			dirs				= { "/srtm/version2/SRTM3/Eurasia", // 0 //$NON-NLS-1$
			"/srtm/version2/SRTM3/North_America", //	1 //$NON-NLS-1$
			"/srtm/version2/SRTM3/South_America", //	2 //$NON-NLS-1$
			"/srtm/version2/SRTM3/Africa", //			3 //$NON-NLS-1$
			"/srtm/version2/SRTM3/Australia", //		4 //$NON-NLS-1$
			"/srtm/version2/SRTM3/Islands" //			5 //$NON-NLS-1$
														};

	private static FTPDownloader	fFtpDownloader		= null;

	public static void get(final String remoteFileName, final String localZipName) throws Exception {

		final String remoteFilePath = getDir(remoteFileName);

		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();

		final boolean isFtp = prefStore.getBoolean(IPreferences.STATE_IS_SRTM3_FTP);
		if (isFtp) {

			// download from FTP server

			if (fFtpDownloader == null) {
				fFtpDownloader = new FTPDownloader("anonymous", "");//$NON-NLS-1$ //$NON-NLS-2$
			}

			// set ftp host from url which contains the protocol ftp://
			String ftpUrl = prefStore.getString(IPreferences.STATE_SRTM3_FTP_URL);
			ftpUrl = ftpUrl.substring(PrefPageSRTM.PROTOCOL_FTP.length());
			fFtpDownloader.setHost(ftpUrl);

			fFtpDownloader.get(remoteFilePath, remoteFileName, localZipName);

		} else {

			// download from HTTP server

			String baseUrl = prefStore.getString(IPreferences.STATE_SRTM3_HTTP_URL);
			if (baseUrl.endsWith(URL_SEPARATOR)) {
				// remove separator at the end
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			}

			HTTPDownloader.get(new StringBuilder().append(baseUrl)
					.append(remoteFilePath)
					.append(URL_SEPARATOR)
					.toString(),//
					remoteFileName,
					localZipName);
		}
	}

	private static String getDir(final String pathName) throws Exception {

		final String fileName = pathName.substring(pathName.lastIndexOf(File.separator) + 1);
		final String latString = fileName.substring(0, 3) + ":00"; // e.g. N50 //$NON-NLS-1$
		final String lonString = fileName.substring(3, 7) + ":00"; // e.g. E006 //$NON-NLS-1$
		final GeoLat lat = new GeoLat(latString);
		final GeoLon lon = new GeoLon(lonString);

		if (lat.greaterThen(new GeoLat("N60:00"))) //$NON-NLS-1$
			throw (new FileNotFoundException());
		if (lat.lessThen(new GeoLat("S56:00"))) //$NON-NLS-1$
			throw (new FileNotFoundException());

		// order important!
		// compare map ftp://e0srp01u.ecs.nasa.gov/srtm/version2/Documentation/Continent_def.gif
		if (isIn(lat, lon, "S56", "S28", "E165", "E179")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirIslands];
		if (isIn(lat, lon, "S56", "S55", "E158", "E159")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirIslands];
		if (isIn(lat, lon, "N15", "N30", "W180", "W155")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirIslands];
		if (isIn(lat, lon, "S44", "S05", "W030", "W006")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirIslands];
		if (isIn(lat, lon, "S56", "S45", "W039", "E060")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirIslands];
		if (isIn(lat, lon, "N35", "N39", "W040", "W020")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirAfrica];
		if (isIn(lat, lon, "S20", "S20", "E063", "E063")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirAfrica];
		if (isIn(lat, lon, "N10", "N10", "W110", "W110")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirNorth_America];
		if (isIn(lat, lon, "S10", "N14", "W180", "W139")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirEurasia];
		if (isIn(lat, lon, "S13", "S11", "E096", "E105")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirEurasia];
		if (isIn(lat, lon, "S44", "S11", "E112", "E179")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirAustralia];
		if (isIn(lat, lon, "S28", "S11", "W180", "W106")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirAustralia];
		if (isIn(lat, lon, "S35", "N34", "W030", "E059")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirAfrica];
		if (isIn(lat, lon, "N35", "N60", "W011", "E059")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirEurasia];
		if (isIn(lat, lon, "S10", "N60", "E060", "E179")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirEurasia];
		if (isIn(lat, lon, "N15", "N60", "W180", "W043")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirNorth_America];
		if (isIn(lat, lon, "S56", "N14", "W093", "W033")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return dirs[DirSouth_America];

		return dirs[DirIslands];
	}

	private static boolean isIn(final GeoLat lat,
								final GeoLon lon,
								final String latMin,
								final String latMax,
								final String lonMin,
								final String lonMax) {

		if (lat.lessThen(new GeoLat(latMin + ":00"))) //$NON-NLS-1$
			return false;
		if (lat.greaterThen(new GeoLat(latMax + ":00"))) //$NON-NLS-1$
			return false;
		if (lon.lessThen(new GeoLon(lonMin + ":00"))) //$NON-NLS-1$
			return false;
		if (lon.greaterThen(new GeoLon(lonMax + ":00"))) //$NON-NLS-1$
			return false;
		return true;
	}
}
