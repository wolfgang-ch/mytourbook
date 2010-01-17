/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
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
package de.byteholder.geoclipse.mapprovider;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.byteholder.geoclipse.Messages;
import de.byteholder.geoclipse.logging.StatusUtil;
import de.byteholder.geoclipse.map.ITileLoader;
import de.byteholder.geoclipse.map.ITilePainter;
import de.byteholder.geoclipse.map.Map;
import de.byteholder.geoclipse.map.MapViewPortData;
import de.byteholder.geoclipse.map.Mercator;
import de.byteholder.geoclipse.map.Projection;
import de.byteholder.geoclipse.map.Tile;
import de.byteholder.geoclipse.map.TileCache;
import de.byteholder.geoclipse.map.TileImageCache;
import de.byteholder.geoclipse.map.TileImageLoader;
import de.byteholder.geoclipse.map.UI;
import de.byteholder.geoclipse.map.event.ITileListener;
import de.byteholder.geoclipse.map.event.TileEventId;
import de.byteholder.geoclipse.util.Util;
import de.byteholder.gpx.GeoPosition;

/**
 * This is the base class for map providers (MP) which provides all data which are necessary to draw
 * a map.<br>
 * <br>
 * This is a new implementation of the previous TileFactory class
 */
public abstract class MP implements Cloneable, Comparable<Object> {

	private static final int						TILE_CACHE_SIZE					= 2000;													//2000;
	private static final int						ERROR_CACHE_SIZE				= 10000;													//10000;
	private static final int						IMAGE_CACHE_SIZE				= 200;

	public static final int							OFFLINE_INFO_NOT_READ			= -1;

	/**
	 * these zoom levels are displayed in the UI therefore they start with 1 instead of 0
	 */
	public static final int							UI_MIN_ZOOM_LEVEL				= 1;
	public static final int							UI_MAX_ZOOM_LEVEL				= 18;

	// loading tiles pool
	private static final int						THREAD_POOL_SIZE				= 20;
	private static ExecutorService					fExecutorService;

	private static final ReentrantLock				EXECUTOR_LOCK					= new ReentrantLock();
	private static final ReentrantLock				RESET_LOCK						= new ReentrantLock();

	/**
	 * Cache for tiles which do not have loading errors
	 */
	private static final TileCache					fTileCache						= new TileCache(TILE_CACHE_SIZE);

	/**
	 * Contains tiles which has loading errors, they are kept in this map that they are not loaded
	 * again
	 */
	private static final TileCache					fErrorTiles						= new TileCache(ERROR_CACHE_SIZE);

	/**
	 * Cache for tile images
	 */
	private static final TileImageCache				fTileImageCache					= new TileImageCache(
																							IMAGE_CACHE_SIZE);

	/**
	 * This queue contains tiles which needs to be loaded, only the number of
	 * {@link #THREAD_POOL_SIZE} can be loaded at the same time, the other tiles are waiting in this
	 * queue. <br>
	 * <br>
	 * TODO !!!!! THIS IS JDK 1.6 !!!!!!!
	 */
	private static final LinkedBlockingDeque<Tile>	fTileWaitingQueue				= new LinkedBlockingDeque<Tile>();

	/**
	 * Listener which throws {@link ITileListener} events
	 */
	private final static ListenerList				fTileListeners					= new ListenerList(
																							ListenerList.IDENTITY);

	private int										fDimmingAlphaValue				= 0xFF;
	private RGB										fDimmingColor;

	private Projection								fProjection;

	/**
	 * image size in pixel for a square image
	 */
	private int										fTileSize						= Integer
																							.parseInt(MapProviderManager.DEFAULT_IMAGE_SIZE);
	// map min/max zoom level
	private int										fMinZoomLevel					= 0;
	private int										fMaxZoomLevel					= UI_MAX_ZOOM_LEVEL
																							- UI_MIN_ZOOM_LEVEL;

	private int										fDefaultZoomLevel				= 0;

	/**
	 * The number of tiles wide at each zoom level
	 */
	private int[]									fMapWidthInTilesAtZoom;

	/**
	 * An array of coordinates in <em>pixels</em> that indicates the center in the world map for the
	 * given zoom level.
	 */
	private Point2D[]								fMapCenterInPixelsAtZoom;

	/**
	 * An array of doubles that contain the number of pixels per degree of longitude at a give zoom
	 * level.
	 */
	private double[]								fLongitudeDegreeWidthInPixels;

	/**
	 * An array of doubles that contain the number of radians per degree of longitude at a given
	 * zoom level (where longitudeRadianWidthInPixels[0] is the most zoomed out)
	 */
	private double[]								fLongitudeRadianWidthInPixels;

	private boolean									fUseOfflineImage				= true;

	/**
	 * This is the image shown as long as the real tile image is not yet fully loaded.
	 */
	private Image									fLoadingImage;

	/**
	 * This is the image displayed when the real tile image could not be loaded.
	 */
	private Image									fErrorImage;

	/**
	 * unique id to identify a map provider
	 */
	private String									fMapProviderId;

	/**
	 * mime image format which is currently used
	 */
	private String									fImageFormat					= MapProviderManager.DEFAULT_IMAGE_FORMAT;

	private int										fFavoriteZoom					= 0;
	private GeoPosition								fFavoritePosition				= new GeoPosition(0.0, 0.0);

	private int										fLastUsedZoom					= 0;
	private GeoPosition								fLastUsedPosition				= new GeoPosition(0.0, 0.0);

	/**
	 * name of the map provider which is displayed in the UI
	 */
	private String									fMapProviderName;

	/**
	 * map provider description
	 */
	private String									fDescription					= UI.EMPTY_STRING;

	/**
	 * OS folder to save offline images
	 */
	private String									fOfflineFolder;

	/**
	 * number of files in the offline cache
	 */
	private int										fOfflineFileCounter				= -1;

	/**
	 * size in Bytes for the offline images
	 */
	private long									fOfflineFileSize				= -1;

	private static final ListenerList				fOfflineReloadEventListeners	= new ListenerList(
																							ListenerList.IDENTITY);

	/**
	 * State if the map provider can be toggled in the map
	 */
	private boolean									fCanBeToggled;

	//
	// Profile map provider values
	//

	/**
	 * alpha values for the map provider, 100 is opaque, 0 is transparent
	 */
	private int										fProfileAlpha					= 100;

	private boolean									fIsProfileTransparentColors		= false;
	private int[]									fProfileTransparentColor		= null;

	/**
	 * when <code>true</code> the color black is transparent
	 */
	private boolean									fIsProfileBlackTransparent;

	private boolean									fIsProfileBrightness;
	private int										fProfileBrightnessValue;
	private MapViewPortData							fMapViewPort;

	public static void addOfflineInfoListener(final IOfflineInfoListener listener) {
		fOfflineReloadEventListeners.add(listener);
	}

	public static void addTileListener(final ITileListener tileListener) {
		fTileListeners.add(tileListener);
	}

	public static void fireTileEvent(final TileEventId tileEventId, final Tile tile) {
		for (final Object listener : fTileListeners.getListeners()) {
			final ITileListener tileListener = (ITileListener) listener;
			tileListener.tileEvent(tileEventId, tile);
		}
	}

	public static TileCache getErrorTiles() {
		return fErrorTiles;
	}

	public static TileCache getTileCache() {
		return fTileCache;
	}

	public static ListenerList getTileListeners() {
		return fTileListeners;
	}

	public static LinkedBlockingDeque<Tile> getTileWaitingQueue() {
		return fTileWaitingQueue;
	}

	public static void removeOfflineInfoListener(final IOfflineInfoListener listener) {
		if (listener != null) {
			fOfflineReloadEventListeners.remove(listener);
		}
	}

	public static void removeTileListener(final ITileListener tileListener) {
		if (tileListener != null) {
			fTileListeners.remove(tileListener);
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * </pre>
	 */
	public MP() {

		fProjection = new Mercator();

		initializeMapWithZoomAndSize(fMaxZoomLevel, fTileSize);

	}

	public boolean canBeToggled() {
		return fCanBeToggled;
	}

	/**
	 * Checks if a tile is displayed in the map viewport.
	 * 
	 * @param tile
	 *            Tile which is checked
	 * @return Returns <code>true</code> when the tile is displayed in the current map viewport.
	 */
	public boolean checkViewPort(final Tile tile) {

		// check zoom level
		if (tile.getZoom() != fMapViewPort.mapZoomLevel) {
			return false;
		}

		// check position
		final int tileX = tile.getX();
		final int tileY = tile.getY();

		if (tileX >= fMapViewPort.tilePosMinX
				&& tileX <= fMapViewPort.tilePosMaxX
				&& tileY >= fMapViewPort.tilePosMinY
				&& tileY <= fMapViewPort.tilePosMaxY) {

			return true;
		}

		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		final MP mapProvider = (MP) super.clone();

		if (this instanceof MPProfile) {

			/*
			 * a map profile contains all map providers which are not a map profile, clone all of
			 * them in the clone constructor
			 */

		} else {

			mapProvider.fImageFormat = new String(fImageFormat);

			mapProvider.fFavoritePosition = new GeoPosition(fFavoritePosition == null
					? new GeoPosition(0.0, 0.0)
					: fFavoritePosition);

			mapProvider.fLastUsedPosition = new GeoPosition(fLastUsedPosition == null
					? new GeoPosition(0.0, 0.0)
					: fLastUsedPosition);
		}

		return mapProvider;
	}

	public int compareTo(final Object otherObject) {

		final MP otherMapProvider = (MP) otherObject;

		if (this instanceof MPPlugin && otherMapProvider instanceof MPPlugin) {

			return fMapProviderName.compareTo(otherMapProvider.getName());

		} else {

			if (this instanceof MPPlugin) {
				return -1;
			}
			if (otherMapProvider instanceof MPPlugin) {
				return 1;
			}

			return fMapProviderName.compareTo(otherMapProvider.getName());
		}
	}

	private void createErrorImage() {

		final Display display = Display.getDefault();

		display.syncExec(new Runnable() {
			public void run() {

				final int tileSize = getTileSize();

				fErrorImage = new Image(display, tileSize, tileSize);

				final Color bgColor = new Color(display, Map.DEFAULT_BACKGROUND_RGB);
				final GC gc = new GC(getErrorImage());
				{
					gc.setBackground(bgColor);
					gc.fillRectangle(0, 0, tileSize, tileSize);

					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.drawString(Messages.geoclipse_extensions_loading_failed, 5, 5);
				}
				gc.dispose();
				bgColor.dispose();
			}
		});
	}

	private void createLoadingImage() {

		final Display display = Display.getDefault();

		display.syncExec(new Runnable() {
			public void run() {

				final int tileSize = getTileSize();

				fLoadingImage = new Image(display, tileSize, tileSize);

				final Color bgColor = new Color(display, Map.DEFAULT_BACKGROUND_RGB);
				final GC gc = new GC(getLoadingImage());
				{
					gc.setBackground(bgColor);
					gc.fillRectangle(0, 0, tileSize, tileSize);

					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.drawString(Messages.geoclipse_extensions_loading, 5, 5);
				}
				gc.dispose();
				bgColor.dispose();
			}
		});
	}

	/**
	 * In this method the implementing Factroy can dispose all of its temporary images and other SWT
	 * objects that need to be disposed.
	 */
	public void disposeAllImages() {

		if (fTileImageCache != null) {
			fTileImageCache.dispose();
		}

		if (fLoadingImage != null) {
			fLoadingImage.dispose();
		}

		if (fErrorImage != null) {
			fErrorImage.dispose();
		}
	}

	public void disposeTileImages() {
		fTileImageCache.dispose();
	}

	public void disposeTiles() {
		fTileCache.removeAll();
		fErrorTiles.removeAll();
		fTileImageCache.dispose();
	}

	/**
	 * Is called directly after the tile was created and before other tile action are done.<br>
	 * <br>
	 * Default implementation do nothing but can be overwritten to do additional initialization like
	 * setting custom data with {@link Tile#setData(Object)}
	 * 
	 * @param tile
	 */
	public void doPostCreation(final Tile tile) {
	// default does nothing
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MP)) {
			return false;
		}

		final MP other = (MP) obj;
		if (fMapProviderId == null) {
			if (other.fMapProviderId != null) {
				return false;
			}
		} else if (!fMapProviderId.equals(other.fMapProviderId)) {
			return false;
		}

		return true;
	}

	private void fireOfflineReloadEvent(final MP mapProvider) {

		final Object[] allListeners = fOfflineReloadEventListeners.getListeners();
		for (final Object listener : allListeners) {
			((IOfflineInfoListener) listener).offlineInfoIsDirty(mapProvider);
		}
	}

	/**
	 * Convert a GeoPosition to a Point2D pixel coordinate in the world bitmap
	 * 
	 * @param geoPosition
	 *            a coordinate
	 * @param zoomLevel
	 *            the current zoom level
	 * @return a pixel location in the world bitmap
	 */
	public org.eclipse.swt.graphics.Point geoToPixel(final GeoPosition geoPosition, final int zoomLevel) {
		return fProjection.geoToPixel(geoPosition, zoomLevel, this);
	}

	/**
	 * @return Returns a custom tile key, default returns <code>null</code>
	 */
	String getCustomTileKey() {
		return null;
	}

	public int getDefaultZoomLevel() {
		return fDefaultZoomLevel;
	}

	public String getDescription() {
		return fDescription;
	}

	/**
	 * @return Returns the color which is used to dim the map images
	 */
	public RGB getDimColor() {
		return fDimmingColor;
	}

	/**
	 * @return Returns the alpha value which is used to dim the map images, default value is not to
	 *         dim the map.
	 */
	public int getDimLevel() {
		return fDimmingAlphaValue;
	}

	public double getDistance(final GeoPosition position1, final GeoPosition position2, final int zoom) {
		return fProjection.getHorizontalDistance(position1, position2, zoom, this);
	}

	public Image getErrorImage() {

		if (fErrorImage == null || fErrorImage.isDisposed()) {
			createErrorImage();
		}

		return fErrorImage;
	}

	/**
	 * @return Returns the {@link ExecutorService} which contains 20 threads to load or create map
	 *         images
	 */
	private ExecutorService getExecutor() {

		if (fExecutorService != null) {
			return fExecutorService;
		}

		/*
		 * create thread pool, this is synched only once until the executor is created
		 */
		EXECUTOR_LOCK.lock();
		{
			try {

				// check again
				if (fExecutorService != null) {
					return fExecutorService;
				}

				final ThreadFactory threadFactory = new ThreadFactory() {

					private int	fCount	= 0;

					public Thread newThread(final Runnable r) {

						final String threadName = "tile-pool-" + fCount++; //$NON-NLS-1$

						final Thread thread = new Thread(r, threadName);

						thread.setPriority(Thread.MIN_PRIORITY);
						thread.setDaemon(true);

						return thread;
					}
				};

				fExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);

			} finally {
				EXECUTOR_LOCK.unlock();
			}
		}

		return fExecutorService;
	}

	public GeoPosition getFavoritePosition() {
		return fFavoritePosition;
	}

	public int getFavoriteZoom() {
		return fFavoriteZoom;
	}

	/**
	 * @return Returns a unique id for the map provider
	 */
	public String getId() {
		return fMapProviderId;
	}

	public String getImageFormat() {
		return fImageFormat;
	}

	public GeoPosition getLastUsedPosition() {
		return fLastUsedPosition;
	}

	public int getLastUsedZoom() {
		return fLastUsedZoom;
	}

	public Image getLoadingImage() {

		if (fLoadingImage == null || fLoadingImage.isDisposed()) {
			createLoadingImage();
		}

		return fLoadingImage;
	}

	/**
	 * @param zoom
	 * @return
	 */
	public double getLongitudeDegreeWidthInPixels(final int zoom) {
		return fLongitudeDegreeWidthInPixels[zoom];
	}

	/**
	 * @param zoom
	 * @return
	 */
	public double getLongitudeRadianWidthInPixels(final int zoom) {
		return fLongitudeRadianWidthInPixels[zoom];
	}

	/**
	 * @param zoom
	 * @return
	 */
	public Point2D getMapCenterInPixelsAtZoom(final int zoom) {
		return fMapCenterInPixelsAtZoom[zoom];
	}

	/**
	 * @param zoom
	 * @return
	 */
	private int getMapSizeInTiles(int zoom) {

		// ensure array bounds, this is Math.min() inline
		final int b = fMapWidthInTilesAtZoom.length - 1;
		zoom = (zoom <= b) ? zoom : b;

		return fMapWidthInTilesAtZoom[zoom];
	}

	/**
	 * @return Returns the size of the map at the given zoom in tiles (num tiles tall by num tiles
	 *         wide)
	 */
	public Dimension getMapTileSize(final int zoom) {
		final int mapTileSize = getMapSizeInTiles(zoom);
		return new Dimension(mapTileSize, mapTileSize);
	}

	/**
	 * @return
	 */
	public int getMaximumZoomLevel() {
		return fMaxZoomLevel;
	}

	public int getMaxZoomLevel() {
		return fMaxZoomLevel;
	}

	/**
	 * @return
	 */
	public int getMinimumZoomLevel() {
		return fMinZoomLevel;
	}

	public int getMinZoomLevel() {
		return fMinZoomLevel;
	}

	/**
	 * @return Returns the name of the map provider which is displayed in the UI
	 */
	public String getName() {
		return fMapProviderName;
	}

	public int getOfflineFileCounter() {
		return fOfflineFileCounter;
	}

	public long getOfflineFileSize() {
		return fOfflineFileSize;
	}

	/**
	 * @return Returns the folder where tile files will be cached relativ to the common offline
	 *         image path
	 */
	public String getOfflineFolder() {
		return fOfflineFolder;
	}

	int getProfileAlpha() {
		return fProfileAlpha;
	}

	int getProfileBrightness() {
		return fProfileBrightnessValue;
	}

	int[] getProfileTransparentColors() {
		return fProfileTransparentColor;
	}

	public Projection getProjection() {
		return fProjection;
	}

	/**
	 * Returns the tile that is located at the given tilePoint for this zoom. For example, if
	 * getMapSize() returns 10x20 for this zoom, and the tilePoint is (3,5), then the appropriate
	 * tile will be located and returned.<br>
	 * <br>
	 * The image for the tile is checked if it's available, if not the loading of the image is
	 * started.
	 * 
	 * @param tilePoint
	 * @param zoom
	 * @return
	 */
	public Tile getTile(int tilePositionX, final int tilePositionY, final int zoom) {

		/*
		 * create tile key, wrap the tiles horizontally --> mod the x with the max width and use
		 * that
		 */
//		final int numTilesWidth = (int) getMapSize(zoom).getWidth();
		final int numTilesWidth = getMapSizeInTiles(zoom);

		if (tilePositionX < 0) {
			tilePositionX = numTilesWidth - (Math.abs(tilePositionX) % numTilesWidth);
		}
		tilePositionX = tilePositionX % numTilesWidth;

		final String tileKey = Tile.getTileKey(//
				this,
				zoom,
				tilePositionX,
				tilePositionY,
				null,
				getCustomTileKey(),
				fProjection.getId());

		/*
		 * check if tile is available in the tile cache and the tile image is available
		 */
		Tile tile = fTileCache.get(tileKey);

		if (tile != null) {

			// tile is available

			// check tile image
			if (tile.isImageValid()) {

				/*
				 * tile image is available, this is the shortest path to check if an image for a
				 * tile position is availabe
				 */

				return tile;
			}

			// check loading state
			if (tile.isLoading()) {
				return tile;
			}

			// check if the old implementation was not correctly transfered to the cache with error tiles
			if (tile.isLoadingError()) {
				StatusUtil.showStatus("Internal error: Tile with loading error should not be in the tile cache 1: " //$NON-NLS-1$
						+ tile.getTileKey(), null);
				return tile;
			}

			// tile image is not available until now
		}

		/*
		 * check if the tile has a loading error
		 */
		final Tile errorTile = fErrorTiles.get(tileKey);
		if (errorTile != null) {

			// tiles with an error do not have an image

			// check if the old implementation was not correctly transfered to the cache with error tiles
			if (tile != null) {
				StatusUtil.showStatus("Internal error: Tile with loading error should not be in the tile cache 2: " //$NON-NLS-1$
						+ tile.getTileKey(), null);
			}

			return errorTile;
		}

		/*
		 * create new tile
		 */
		if (tile == null) {

			// tile is not being loaded, create a new tile 

			tile = new Tile(this, zoom, tilePositionX, tilePositionY, null);
			tile.setBoundingBoxEPSG4326();

			doPostCreation(tile);

			/*
			 * keep tiles in the cache, tiles with loading errors will be transferred to the tile
			 * cache with loading errors, this is done in the TileImageLoader
			 */
			fTileCache.add(tileKey, tile);
		}

		/*
		 * now we have a tile, get tile image from the image cache
		 */
		Image cachedTileImage = null;

		final boolean useOfflineImage = isUseOfflineImage();
		if (useOfflineImage) {
			cachedTileImage = fTileImageCache.getTileImage(tile);
		}

		if (cachedTileImage == null) {

			// an image is not available, start loading it

			if (isTileValid(tilePositionX, tilePositionY, zoom)) {

				// set state if an offline image for the current tile is available
				if (useOfflineImage) {
					fTileImageCache.setOfflineImageAvailability(tile);
				}

				// LOAD/CREATE image
				putTileInWaitingQueue(tile);
			}

		} else {

			// set image from the cache into the tile

			tile.setMapImage(cachedTileImage);
		}

		return tile;
	}

	public TileImageCache getTileImageCache() {
		return fTileImageCache;
	}

	/**
	 * @param fullPath
	 *            File system path on the local file system where the tile path is appended
	 * @param zoomLevel
	 * @param y
	 * @param x
	 * @param tile
	 * @return Returns the path for a tile when it's saved in the file system or <code>null</code>
	 *         when this features is not supported
	 */
	public abstract IPath getTileOSPath(String fullPath, Tile tile);

	/**
	 * @return Tile painter which is painting a tile or <code>null</code> when the tile is loaded
	 *         from a url
	 */
	public ITilePainter getTilePainter() {
		return null;
	}

	/**
	 * The size of tiles for this factory. Tiles must be square.
	 * 
	 * @return the size of the tiles in pixels. All tiles must be square. A return value of 256, for
	 *         example, means that each tile will be 256 pixels wide and tall
	 */
	public int getTileSize() {
		return fTileSize;
	}

	/**
	 * Returns the tile url for the specified tile at the specified zoom level. By default it will
	 * generate a tile url using the base url and parameters specified in the constructor. Thus if
	 * 
	 * <PRE>
	 * baseURl = http://www.myserver.com/maps?version=0.1 
	 * xparam = x 
	 * yparam = y 
	 * zparam = z 
	 * tilepoint = [1,2]
	 * zoom level = 3
	 * </PRE>
	 * 
	 * then the resulting url would be:
	 * 
	 * <pre>
	 * http://www.myserver.com/maps?version=0.1&amp;x=1&amp;y=2&amp;z=3
	 * </pre>
	 * 
	 * Note that the URL can be a <CODE>file:</CODE> url.<br>
	 * <br>
	 * This method will be ignored when the map provider is an instance of {@link ITileLoader}. <br>
	 * 
	 * @param tile
	 * @return a valid url to load the tile
	 */
	public String getTileUrl(final Tile tile) {
		return null;
	}

	/**
	 * Gets the URL of a tile.
	 * 
	 * @param tile
	 * @throws java.net.URISyntaxException
	 * @return
	 * @throws Exception
	 */
	public URL getTileURLEncoded(final Tile tile) throws Exception {

		final String urlString = getTileUrl(tile);

		if (urlString == null) {
			final Exception e = new Exception();
			StatusUtil.log(NLS.bind(Messages.DBG041_Error_InvalidUrlNull, tile.getTileKey()), e);
			throw e;
		}

		final String encodedUrl;

		URL url;

		try {
			encodedUrl = Util.encodeSpace(urlString);

			// keep url for logging
			tile.setUrl(urlString);

			url = new URL(encodedUrl);

		} catch (final MalformedURLException e) {
			StatusUtil.log(NLS.bind(Messages.DBG042_Error_InvalidUrl, urlString, tile.getTileKey()), e);
			throw e;
		}

		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fMapProviderId == null) ? 0 : fMapProviderId.hashCode());
		return result;
	}

	public void initializeMapSize(final int tileSize) {
		initializeMapWithZoomAndSize(fMaxZoomLevel, tileSize);
	}

	private void initializeMapWithZoomAndSize(final int maxZoom, final int tileSize) {

		fTileSize = tileSize;

		// map width (in pixel) is one tile at zoomlevel 0
		int devMapSize = tileSize;

		final int mapArrayLength = maxZoom + 1;

		fLongitudeDegreeWidthInPixels = new double[mapArrayLength];
		fLongitudeRadianWidthInPixels = new double[mapArrayLength];

		fMapCenterInPixelsAtZoom = new Point2D.Double[mapArrayLength];
		fMapWidthInTilesAtZoom = new int[mapArrayLength];

		// get map values for each zoom level
		for (int z = 0; z <= maxZoom; ++z) {

			// how wide is each degree of longitude in pixels
			fLongitudeDegreeWidthInPixels[z] = (double) devMapSize / 360;

			// how wide is each radian of longitude in pixels
			fLongitudeRadianWidthInPixels[z] = devMapSize / (2.0 * Math.PI);

			final int devMapSize2 = devMapSize / 2;

			fMapCenterInPixelsAtZoom[z] = new Point2D.Double(devMapSize2, devMapSize2);
			fMapWidthInTilesAtZoom[z] = devMapSize / tileSize;

			devMapSize *= 2;
		}
	}

	private void initializeZoomLevel(final int minZoom, final int maxZoom) {

		fMinZoomLevel = minZoom;
		fMaxZoomLevel = maxZoom;

		initializeMapWithZoomAndSize(fMaxZoomLevel, fTileSize);
	}

//	/**
//	 * @param offlineImagePath
//	 * @return Path where tile files will are cached relative to the offline image path
//	 */
//	public abstract IPath getTileOSPathFolder(final String offlineImagePath);

	boolean isProfileBrightness() {
		return fIsProfileBrightness;
	}

	boolean isProfileTransparentBlack() {
		return fIsProfileBlackTransparent;
	}

	boolean isProfileTransparentColors() {
		return fIsProfileTransparentColors;
	}

	/**
	 * @returns Return <code>true</code> if this point in <em>tiles</em> is valid at this zoom
	 *          level. For example, if the zoom level is 0 (zoomed all the way out, there is only
	 *          one tile), x,y must be 0,0
	 */
	public boolean isTileValid(final int x, final int y, final int zoomLevel) {

		// check if off the map to the top or left
		if (x < 0 || y < 0) {
			return false;
		}

		// check if off the map to the right
		if (getMapCenterInPixelsAtZoom(zoomLevel).getX() * 2 <= x * fTileSize) {
			return false;
		}

		// check if off the map to the bottom
		if (getMapCenterInPixelsAtZoom(zoomLevel).getY() * 2 <= y * fTileSize) {
			return false;
		}

		// check if out of zoom bounds
		if (zoomLevel < getMinimumZoomLevel() || zoomLevel > getMaximumZoomLevel()) {
			return false;
		}

		return true;
	}

	public boolean isUseOfflineImage() {
		return fUseOfflineImage;
	}

	/**
	 * Convert a pixel in the world bitmap at the specified zoom level into a GeoPosition
	 * 
	 * @param pixelCoordinate
	 *            a Point2D representing a pixel in the world bitmap
	 * @param zoom
	 *            the zoom level of the world bitmap
	 * @return the converted GeoPosition
	 */
	public GeoPosition pixelToGeo(final Point pixelCoordinate, final int zoom) {
		return fProjection.pixelToGeo(pixelCoordinate, zoom, this);
	}

	/**
	 * Put one tile into the tile image waiting queue
	 * 
	 * @param tile
	 * @throws InterruptedException
	 */
	private void putOneTileInWaitingQueue(final Tile tile) throws InterruptedException {

		tile.setLoading(true);

		fTileWaitingQueue.add(tile);

		// create loading task
		final Future<?> future = getExecutor().submit(new TileImageLoader());

		// keep loading task
		tile.setFuture(future);

		fireTileEvent(TileEventId.TILE_IS_QUEUED, tile);
	}

	/**
	 * Put all tiles into a queue to load/create the tile image
	 * 
	 * @param tile
	 */
	private void putTileInWaitingQueue(final Tile tile) {

		// prevent to load it more than once
		if (tile.isLoading()) {
			return;
		}

		try {

			putOneTileInWaitingQueue(tile);

			if (tile.isOfflimeImageAvailable() == false) {

				final ArrayList<Tile> tileChildren = tile.createTileChildren();
				if (tileChildren != null) {

					// this is a parent child, put all child tiles into the loading queue

					if (tileChildren.size() == 0) {

						/*
						 * there are no child tiles available, this can happen when the zoom factor
						 * does not support the map providers or when child tiles have an loading
						 * error
						 */

						// set loading error into the parent tile
						tile.setLoadingError(Messages.TileInfo_Error_NoMapProvider);
					}

					for (final Tile tileChild : tileChildren) {
						putOneTileInWaitingQueue(tileChild);
					}
				}
			}

		} catch (final Exception ex) {
			StatusUtil.log(ex.getMessage(), ex);
		}
	}

	public void resetAll(final boolean keepTilesWithLoadingError) {

		RESET_LOCK.lock();
		{
			try {

				fTileWaitingQueue.clear();
				fTileCache.stopLoadingTiles();

				if (keepTilesWithLoadingError == false) {
					fErrorTiles.removeAll();
				}

				fTileCache.removeAll();
				fTileImageCache.dispose();

			} finally {
				RESET_LOCK.unlock();
			}
		}

		fireTileEvent(TileEventId.TILE_RESET_QUEUES, null);
	}

	public synchronized void resetOverlays() {

		fTileWaitingQueue.clear();
		fTileCache.stopLoadingTiles();

		fTileCache.resetOverlays();
		fErrorTiles.resetOverlays();

		fireTileEvent(TileEventId.TILE_RESET_QUEUES, null);
	}

	public void resetParentTiles() {

		RESET_LOCK.lock();
		{
			try {

				fTileWaitingQueue.clear();
				fTileCache.stopLoadingTiles();

				fErrorTiles.removeParentTiles();

				fTileCache.removeAll();
				fTileImageCache.dispose();

			} finally {
				RESET_LOCK.unlock();
			}
		}
	}

	public void resetTileImageAvailability() {
		fTileCache.resetTileImageAvailability();
	}

	public void setCanBeToggled(final boolean canBeToggled) {
		fCanBeToggled = canBeToggled;
	}

	public void setDefaultZoomLevel(final int defaultZoomLevel) {
		fDefaultZoomLevel = defaultZoomLevel;
	}

	public void setDescription(final String fDescription) {
		this.fDescription = fDescription;
	}

	public void setDimLevel(final int dimLevel, final RGB dimColor) {

		// check if dimming value is modified
		if (fDimmingAlphaValue == dimLevel && fDimmingColor == dimColor) {
			// dimming value is not modified
			return;
		}

		// set new dim level/color
		fDimmingAlphaValue = dimLevel;
		fDimmingColor = dimColor;

		// dispose all cached images
		disposeTileImages();
	}

	public void setFavoritePosition(final GeoPosition fFavoritePosition) {
		this.fFavoritePosition = fFavoritePosition;
	}

	public void setFavoriteZoom(final int favoriteZoom) {
		fFavoriteZoom = favoriteZoom;
	}

	public void setId(final String mapProviderId) {

		fMapProviderId = mapProviderId;

		/*
		 * !!! very importand !!!
		 * the factory id must be set in the superclass to make the tile factory
		 * info unique, otherwise factorId is null and all created custom tile factory infos cannot
		 * be distinguished with the equals/hashcode methods
		 */
		//		super.setFactoryId(factoryId);
	}

	public void setImageFormat(final String imageFormat) {
		fImageFormat = imageFormat;
	}

	void setIsProfileBrightness(final boolean isBrightness) {
		fIsProfileBrightness = isBrightness;
	}

	void setIsProfileTransparentBlack(final boolean isBlackTransparent) {
		fIsProfileBlackTransparent = isBlackTransparent;
	}

	void setIsProfileTransparentColors(final boolean isTransColors) {
		fIsProfileTransparentColors = isTransColors;
	}

	public void setLastUsedPosition(final GeoPosition position) {
		fLastUsedPosition = position;
	}

	public void setLastUsedZoom(final int zoom) {
		fLastUsedZoom = zoom;
	}

	public void setMapViewPort(final MapViewPortData mapViewPort) {
		fMapViewPort = mapViewPort;
	}

	public void setName(final String mapProviderName) {
		fMapProviderName = mapProviderName;
	}

	public void setOfflineFileCounter(final int offlineFileCounter) {
		fOfflineFileCounter = offlineFileCounter;
	}

	public void setOfflineFileSize(final long offlineFileSize) {
		fOfflineFileSize = offlineFileSize;
	}

	/**
	 * Sets the folder where offline images are saved, this folder is relativ to the common offline
	 * folder path
	 * 
	 * @param offlineFolder
	 */
	public void setOfflineFolder(final String offlineFolder) {
		fOfflineFolder = offlineFolder;
	}

	void setProfileAlpha(final int alpha) {
		fProfileAlpha = alpha;
	}

	void setProfileBrightness(final int brightnessValue) {
		fProfileBrightnessValue = brightnessValue;
	}

	void setProfileTransparentColors(final int[] transColors) {
		fProfileTransparentColor = transColors;
	}

	public void setStateToReloadOfflineCounter() {

		if (fOfflineFileCounter != OFFLINE_INFO_NOT_READ) {

			fOfflineFileCounter = OFFLINE_INFO_NOT_READ;
			fOfflineFileSize = OFFLINE_INFO_NOT_READ;

			fireOfflineReloadEvent(this);
		}
	}

	/**
	 * Sets the tile image size and updates the internal datastructures.
	 * 
	 * @param tileSize
	 */
	public void setTileSize(final int tileSize) {
		initializeMapSize(tileSize);
	}

	public void setUseOfflineImage(final boolean useOfflineImage) {
		fUseOfflineImage = useOfflineImage;
	}

	/**
	 * Sets the min/max zoom levels which this map provider supports and updates the internal
	 * datastructures.
	 * 
	 * @param minZoom
	 * @param maxZoom
	 */
	public void setZoomLevel(final int minZoom, final int maxZoom) {
		initializeZoomLevel(minZoom, maxZoom);
	}

	@Override
	public String toString() {
		return fMapProviderName + "(" + fMapProviderId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
