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
package net.tourbook.ui;

import java.io.File;

import net.tourbook.data.TourData;
import net.tourbook.util.PixelConverter;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;

public abstract class TableColumnFactory {

	public static final TableColumnFactory DB_STATUS = new TableColumnFactory() {

		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {

			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "dbStatus", SWT.CENTER); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_db_status_label);
			colDef.setColumnToolTipText(Messages.ColumnFactory_db_status_tooltip);
			colDef.setColumnWidth(20);

			return colDef;
		};
	};

	public static final TableColumnFactory TOUR_DATE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "tourdate", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnText(Messages.ColumnFactory_date);
			colDef.setColumnLabel(Messages.ColumnFactory_date_label);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(12));

			return colDef;
		};
	};
	
	public static final TableColumnFactory TOUR_START_TIME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "startTime", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_time_label);
			colDef.setColumnText(Messages.ColumnFactory_time);
			colDef.setColumnToolTipText(Messages.ColumnFactory_time_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

			return colDef;
		};
	};
	
	public static final TableColumnFactory TOUR_TYPE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "tourType", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_tour_type_label);
			colDef.setColumnToolTipText(Messages.ColumnFactory_tour_type_tooltip);
			colDef.setColumnWidth(18);

			return colDef;
		};
	};
	
	public static final TableColumnFactory TOUR_TITLE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "tourTitle", SWT.LEAD); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_tour_title_label);
			colDef.setColumnText(Messages.ColumnFactory_tour_title);
			colDef.setColumnToolTipText(Messages.ColumnFactory_tour_title_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(25));

			return colDef;
		};
	};
	
	public static final TableColumnFactory RECORDING_TIME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "recordingTime", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_recording_time_label);
			colDef.setColumnText(Messages.ColumnFactory_recording_time);
			colDef.setColumnToolTipText(Messages.ColumnFactory_recording_time_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
		
			return colDef;
		};
	};
	
	public static final TableColumnFactory DRIVING_TIME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "drivingTime", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_driving_time_label);
			colDef.setColumnText(Messages.ColumnFactory_driving_time);
			colDef.setColumnToolTipText(Messages.ColumnFactory_driving_time_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));

			return colDef;
		};
	};
	
	public static final TableColumnFactory DISTANCE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "distance", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_distance_label + " (" + UI.UNIT_LABEL_DISTANCE + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(UI.UNIT_LABEL_DISTANCE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_distance_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));

			return colDef;
		};
	};

	
	public static final TableColumnFactory SPEED = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "speed", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_speed_label);
			colDef.setColumnText(UI.UNIT_LABEL_SPEED);
			colDef.setColumnToolTipText(Messages.ColumnFactory_speed_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));

			return colDef;
		};
	};

	public static final TableColumnFactory PACE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "pace", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_pace_label);
			colDef.setColumnText(UI.UNIT_LABEL_PACE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_pace_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
			
			return colDef;
		};
	};

	public static final TableColumnFactory PULSE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "pulse", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_pulse_label);
			colDef.setColumnText(Messages.ColumnFactory_pulse);
			colDef.setColumnToolTipText(Messages.ColumnFactory_pulse_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory TEMPERATURE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "temperature", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_temperature_label);
			colDef.setColumnText(UI.UNIT_LABEL_TEMPERATURE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_temperature_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory CADENCE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "cadence", SWT.TRAIL); //$NON-NLS-1$
	
			colDef.setColumnLabel(Messages.ColumnFactory_cadence_label);
			colDef.setColumnText(Messages.ColumnFactory_cadence);
			colDef.setColumnToolTipText(Messages.ColumnFactory_cadence_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory GRADIENT = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "gradient", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_gradient_label);
			colDef.setColumnText(Messages.ColumnFactory_gradient);
			colDef.setColumnToolTipText(Messages.ColumnFactory_gradient_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(9));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory ALTITUDE_UP = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "altitudeUp", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_altitude_up_label  + " (" + UI.UNIT_LABEL_ALTITUDE + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(UI.UNIT_LABEL_ALTITUDE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_altitude_up_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
		
			return colDef;
		};
	};
	
	public static final TableColumnFactory ALTITUDE_UP_H = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final String unitLabel =UI.UNIT_LABEL_ALTITUDE+"/h"; //$NON-NLS-1$

			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "altitudeUpH", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_altitude_up_label  + " (" + unitLabel + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(unitLabel);
			colDef.setColumnToolTipText(Messages.ColumnFactory_altitude_up_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory ALTITUDE_DOWN = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "altitudeDown", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_altitude_down_label  + " (" + UI.UNIT_LABEL_ALTITUDE + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(UI.UNIT_LABEL_ALTITUDE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_altitude_down_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
		
			return colDef;
		};
	};

	public static final TableColumnFactory ALTITUDE_DOWN_H = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {

			final String unitLabel =UI.UNIT_LABEL_ALTITUDE+"/h"; //$NON-NLS-1$
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "altitudeDownH", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_altitude_down_label  + " (" + unitLabel + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(unitLabel);
			colDef.setColumnToolTipText(Messages.ColumnFactory_altitude_down_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory DEVICE_PROFILE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "deviceProfile", SWT.LEAD); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_profile_label);
			colDef.setColumnText(Messages.ColumnFactory_profile);
			colDef.setColumnToolTipText(Messages.ColumnFactory_profile_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
		
			colDef.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					cell.setText(((TourData) cell.getElement()).getDeviceModeName());
				}
			});

			return colDef;
		};
	};
	
	public static final TableColumnFactory TIME_INTERVAL = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "timeInterval", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_time_interval_label);
			colDef.setColumnText(Messages.ColumnFactory_time_interval);
			colDef.setColumnToolTipText(Messages.ColumnFactory_time_interval_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
			
			colDef.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					cell.setText(Integer.toString(((TourData) cell.getElement())
							.getDeviceTimeInterval()));
				}
			});
	
			return colDef;
		};
	};
	
	public static final TableColumnFactory DEVICE_NAME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "deviceName", SWT.LEAD); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_device_label);
			colDef.setColumnText(Messages.ColumnFactory_device);
			colDef.setColumnToolTipText(Messages.ColumnFactory_device_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
			
			colDef.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					cell.setText(((TourData) cell.getElement()).getDeviceName());
				}
			});
		
			return colDef;
		};
	};
	
	public static final TableColumnFactory IMPORT_FILE_PATH = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "importFilePath", SWT.LEAD); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_import_filepath_label);
			colDef.setColumnText(Messages.ColumnFactory_import_filepath);
			colDef.setColumnToolTipText(Messages.ColumnFactory_import_filepath_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));
			
			colDef.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final String importRawDataFile = ((TourData) cell.getElement()).importRawDataFile;
					if (importRawDataFile != null) {
						cell.setText(new File(importRawDataFile).getParentFile().getPath());
					}
				}
			});
		
			return colDef;
		};
	};
	
	public static final TableColumnFactory IMPORT_FILE_NAME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "importFileName", SWT.LEAD); //$NON-NLS-1$
			
			colDef.setColumnLabel(Messages.ColumnFactory_import_filename_label);
			colDef.setColumnText(Messages.ColumnFactory_import_filename);
			colDef.setColumnToolTipText(Messages.ColumnFactory_import_filename_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));
			
			colDef.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final String importRawDataFile = ((TourData) cell.getElement()).importRawDataFile;
					if (importRawDataFile != null) {
						cell.setText(new File(importRawDataFile).getName());
					}
				}
			});
	
			return colDef;
		};
	};

	public static final TableColumnFactory FIRST_COLUMN = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "firstColumn", SWT.LEAD); //$NON-NLS-1$
	
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(1));
			
			return colDef;
		};
	};

	public static final TableColumnFactory SEQUENCE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "sequence", SWT.TRAIL); //$NON-NLS-1$
	
			colDef.setColumnLabel(Messages.ColumnFactory_sequence_label);
			colDef.setColumnText(Messages.ColumnFactory_sequence);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory TOUR_TIME = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "tourTime", SWT.TRAIL); //$NON-NLS-1$
	
			colDef.setColumnLabel(Messages.ColumnFactory_tour_time_label);
			colDef.setColumnText(Messages.ColumnFactory_tour_time);
			colDef.setColumnToolTipText(Messages.ColumnFactory_tour_time_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(10));
			
			return colDef;
		};
	};
	
	public static final TableColumnFactory ALTITUDE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "altitude", SWT.TRAIL); //$NON-NLS-1$
		
			colDef.setColumnLabel(Messages.ColumnFactory_altitude_label  + " (" + UI.UNIT_LABEL_ALTITUDE + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			colDef.setColumnText(UI.UNIT_LABEL_ALTITUDE);
			colDef.setColumnToolTipText(Messages.ColumnFactory_altitude_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(8));
		
			return colDef;
		};
	};
	

	public static final TableColumnFactory LONGITUDE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "longitude", SWT.LEAD); //$NON-NLS-1$
			
			colDef.setColumnLabel(Messages.ColumnFactory_longitude_label);
			colDef.setColumnText(Messages.ColumnFactory_longitude);
			colDef.setColumnToolTipText(Messages.ColumnFactory_longitude_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));
			
			return colDef;
		};
	};

	public static final TableColumnFactory LATITUDE = new TableColumnFactory() {
		
		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "latitude", SWT.LEAD); //$NON-NLS-1$

			colDef.setColumnLabel(Messages.ColumnFactory_latitude_label);
			colDef.setColumnText(Messages.ColumnFactory_latitude);
			colDef.setColumnToolTipText(Messages.ColumnFactory_latitude_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));
			
			return colDef;
		};
	};
	
	
	public static final TableColumnFactory	TOUR_TAGS	= new TableColumnFactory() {

		@Override
		public TableColumnDefinition createColumn(final ColumnManager columnManager, final PixelConverter pixelConverter) {
			
			final TableColumnDefinition colDef = new TableColumnDefinition(columnManager, "tourTags", SWT.LEAD); //$NON-NLS-1$
			
			colDef.setColumnLabel(Messages.ColumnFactory_tour_tag_label);
			colDef.setColumnText(Messages.ColumnFactory_tour_tag_label);
			colDef.setColumnToolTipText(Messages.ColumnFactory_tour_tag_tooltip);
			colDef.setColumnWidth(pixelConverter.convertWidthInCharsToPixels(20));


			return colDef;
		};
	};
	
	public abstract TableColumnDefinition createColumn(ColumnManager columnManager, PixelConverter pixelConverter);
}
