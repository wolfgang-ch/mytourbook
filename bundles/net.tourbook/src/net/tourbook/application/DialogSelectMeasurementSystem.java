/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.application;

import net.tourbook.Messages;
import net.tourbook.measurement_system.MeasurementSystem;
import net.tourbook.measurement_system.MeasurementSystem_Manager;
import net.tourbook.measurement_system.System_DayTime;
import net.tourbook.measurement_system.System_Distance;
import net.tourbook.measurement_system.System_Elevation;
import net.tourbook.measurement_system.System_Height;
import net.tourbook.measurement_system.System_Length;
import net.tourbook.measurement_system.System_Pressure_Atmosphere;
import net.tourbook.measurement_system.System_LengthSmall;
import net.tourbook.measurement_system.System_Temperature;
import net.tourbook.measurement_system.System_Weight;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

class DialogSelectMeasurementSystem extends Dialog {

   private Combo _comboSystem_Profile;
   private Combo _comboSystemOptiop_DayTime;
   private Combo _comboSystemOptiop_Distance;
   private Combo _comboSystemOptiop_Elevation;
   private Combo _comboSystemOptiop_Height_Body;
   private Combo _comboSystemOptiop_Length;
   private Combo _comboSystemOptiop_Length_Small;
   private Combo _comboSystemOptiop_Pressure_Atmosphere;
   private Combo _comboSystemOptiop_Temperature;
   private Combo _comboSystemOptiop_Weight;

   protected DialogSelectMeasurementSystem(final Shell parentShell) {
      super(parentShell);
   }

   @Override
   public boolean close() {

      final int activeSystemProfileIndex = _comboSystem_Profile.getSelectionIndex();

      MeasurementSystem_Manager.setActiveSystemProfileIndex(activeSystemProfileIndex, false);

      return super.close();
   }

   @Override
   protected void configureShell(final Shell shell) {
      super.configureShell(shell);
      shell.setText(Messages.App_Dialog_FirstStartupSystem_Title);
   }

   @Override
   protected void createButtonsForButtonBar(final Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
   }

   @Override
   protected Control createDialogArea(final Composite parent) {

      final Composite ui = createUI(parent);

      enableControls();
      fillSystemControls();

      // select default system which is the metric (first) profile in the first startup
      _comboSystem_Profile.select(0);
      onSystemProfile_Select();

      return ui;
   }

   private Composite createUI(final Composite parent) {

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
      GridLayoutFactory.swtDefaults().margins(10, 10).numColumns(1).applyTo(container);
      {
         {
            // label: measurement system

            final Label label = new Label(container, SWT.NONE);
            GridDataFactory.fillDefaults().applyTo(label);
            label.setText(Messages.App_Dialog_FirstStartupSystem_Label_System);
         }

         createUI_10_MeasurementSystem_Data(container);

         {
            // label: info

            final Label label = new Label(container, SWT.NONE);
            GridDataFactory.fillDefaults().indent(0, 15).applyTo(label);
            label.setText(Messages.App_Dialog_FirstStartupSystem_Label_Info);
         }
      }

      return container;
   }

   private void createUI_10_MeasurementSystem_Data(final Composite parent) {

      final GridDataFactory gridData_Combo = GridDataFactory.fillDefaults().grab(true, false);
      final GridDataFactory gridData_Label = GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER);

      final SelectionAdapter profileListener = new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            onSystemProfile_Select();
         }
      };

      final Composite container = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().grab(false, false).applyTo(container);
      GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
      {
         {
            /*
             * Measurement system
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_System);
            gridData_Label.applyTo(label);

            // combo
            _comboSystem_Profile = new Combo(container, SWT.READ_ONLY);
            _comboSystem_Profile.addSelectionListener(profileListener);
            gridData_Combo.applyTo(_comboSystem_Profile);

            // spacer
            new Label(container, SWT.NONE);
         }
         {
            /*
             * Distance
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Distance);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Distance = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Distance);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Distance_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Length
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Length);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Length = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Length);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Length_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Small length
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Length_Small);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Length_Small = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Length_Small);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Length_Small_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Elevation
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Elevation);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Elevation = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Elevation);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Elevation_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Height
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Height);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Height_Body = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Height_Body);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Height_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Temperature
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Temperature);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Temperature = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Temperature);

            new Label(container, SWT.NONE);
         }
         {
            /*
             * Daytime
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_DayTime);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_DayTime = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_DayTime);

            new Label(container, SWT.NONE);
         }
         {
            /*
             * Weight
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Weight);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Weight = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Weight);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Weight_Info);
            gridData_Label.applyTo(labelInfo);
         }
         {
            /*
             * Atmospheric pressure
             */

            // label
            final Label label = new Label(container, SWT.NONE);
            label.setText(Messages.Pref_System_Label_Pressure_Atmosphere);
            gridData_Label.applyTo(label);

            // combo
            _comboSystemOptiop_Pressure_Atmosphere = new Combo(container, SWT.READ_ONLY);
            gridData_Combo.applyTo(_comboSystemOptiop_Pressure_Atmosphere);

            // label: info
            final Label labelInfo = new Label(container, SWT.NONE);
            labelInfo.setText(Messages.Pref_System_Label_Pressure_Atmosphere_Info);
            gridData_Label.applyTo(labelInfo);
         }
      }
   }

   private void enableControls() {

      _comboSystemOptiop_DayTime.setEnabled(false);
      _comboSystemOptiop_Distance.setEnabled(false);
      _comboSystemOptiop_Elevation.setEnabled(false);
      _comboSystemOptiop_Height_Body.setEnabled(false);
      _comboSystemOptiop_Length.setEnabled(false);
      _comboSystemOptiop_Length_Small.setEnabled(false);
      _comboSystemOptiop_Pressure_Atmosphere.setEnabled(false);
      _comboSystemOptiop_Temperature.setEnabled(false);
      _comboSystemOptiop_Weight.setEnabled(false);
   }

   private void fillSystemControls() {

      for (final MeasurementSystem systemProfile : MeasurementSystem_Manager.getCurrentProfiles()) {
         _comboSystem_Profile.add(systemProfile.getName());
      }

      for (final System_Pressure_Atmosphere system : MeasurementSystem_Manager.getAllSystem_AtmosphericPressures()) {
         _comboSystemOptiop_Pressure_Atmosphere.add(system.getLabel());
      }

      for (final System_DayTime system : MeasurementSystem_Manager.getAllSystem_DayTime()) {
         _comboSystemOptiop_DayTime.add(system.getLabel());
      }

      for (final System_Distance systemDistance : MeasurementSystem_Manager.getAllSystem_Distances()) {
         _comboSystemOptiop_Distance.add(systemDistance.getLabel());
      }

      for (final System_Elevation systemElevation : MeasurementSystem_Manager.getAllSystem_Elevations()) {
         _comboSystemOptiop_Elevation.add(systemElevation.getLabel());
      }

      for (final System_Height systemHeight : MeasurementSystem_Manager.getAllSystem_Heights()) {
         _comboSystemOptiop_Height_Body.add(systemHeight.getLabel());
      }

      for (final System_Length systemElevation : MeasurementSystem_Manager.getAllSystem_Length()) {
         _comboSystemOptiop_Length.add(systemElevation.getLabel());
      }

      for (final System_LengthSmall systemElevation : MeasurementSystem_Manager.getAllSystem_SmallLength()) {
         _comboSystemOptiop_Length_Small.add(systemElevation.getLabel());
      }

      for (final System_Temperature systemTemperature : MeasurementSystem_Manager.getAllSystem_Temperatures()) {
         _comboSystemOptiop_Temperature.add(systemTemperature.getLabel());
      }

      for (final System_Weight systemWeight : MeasurementSystem_Manager.getAllSystem_Weights()) {
         _comboSystemOptiop_Weight.add(systemWeight.getLabel());
      }

   }

   private void onSystemProfile_Select() {

      final int activeSystemProfileIndex = _comboSystem_Profile.getSelectionIndex();
      final MeasurementSystem selectedSystemProfile = MeasurementSystem_Manager.getCurrentProfiles().get(activeSystemProfileIndex);

// SET_FORMATTING_OFF
      _comboSystemOptiop_DayTime             .select(MeasurementSystem_Manager.getSystemIndex_DayTime(selectedSystemProfile));
      _comboSystemOptiop_Distance            .select(MeasurementSystem_Manager.getSystemIndex_Distance(selectedSystemProfile));
      _comboSystemOptiop_Elevation           .select(MeasurementSystem_Manager.getSystemIndex_Elevation(selectedSystemProfile));
      _comboSystemOptiop_Height_Body         .select(MeasurementSystem_Manager.getSystemIndex_Height(selectedSystemProfile));
      _comboSystemOptiop_Length              .select(MeasurementSystem_Manager.getSystemIndex_Length(selectedSystemProfile));
      _comboSystemOptiop_Length_Small        .select(MeasurementSystem_Manager.getSystemIndex_Length_Small(selectedSystemProfile));
      _comboSystemOptiop_Pressure_Atmosphere .select(MeasurementSystem_Manager.getSystemIndex_Pressure_Atmosphere(selectedSystemProfile));
      _comboSystemOptiop_Temperature         .select(MeasurementSystem_Manager.getSystemIndex_Temperature(selectedSystemProfile));
      _comboSystemOptiop_Weight              .select(MeasurementSystem_Manager.getSystemIndex_Weight(selectedSystemProfile));
// SET_FORMATTING_ON
   }
}
