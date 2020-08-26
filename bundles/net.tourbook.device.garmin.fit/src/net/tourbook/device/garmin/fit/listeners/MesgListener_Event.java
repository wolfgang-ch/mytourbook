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
package net.tourbook.device.garmin.fit.listeners;

import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventMesgListener;
import com.garmin.fit.EventType;

import java.util.List;

import net.tourbook.data.GearData;
import net.tourbook.data.TourTimerPause;
import net.tourbook.device.garmin.fit.FitData;

/**
 * Set gear data
 */
public class MesgListener_Event extends AbstractMesgListener implements EventMesgListener {

   List<GearData>       _gearData;
   List<TourTimerPause> _timerPauses;

   public MesgListener_Event(final FitData fitData) {

      super(fitData);

      _gearData = fitData.getGearData();
      _timerPauses = fitData.getTourTimerPauses();
   }

   @SuppressWarnings("incomplete-switch")
   @Override
   public void onMesg(final EventMesg mesg) {

      final Event event = mesg.getEvent();
      final EventType eventType = mesg.getEventType();
      if (event != null && event == Event.TIMER && eventType != null) {

         switch (eventType) {
         case STOP:
         case STOP_ALL:
            //TODO FB remove
            System.out.print("STOP:");
            System.out.println(mesg.getTimestamp());
            //Get total_timer_time field Units: s Comment: Exclude pauses

            final TourTimerPause tourTimerPause = new TourTimerPause();
            tourTimerPause.setStartTime(mesg.getTimestamp().getTimestamp() * 1000);
            _timerPauses.add(tourTimerPause);
            break;

         case START:
            //TODO FB remove
            System.out.print("START: ");
            System.out.println(mesg.getTimestamp());
            if (_timerPauses.size() > 0) {
               _timerPauses.get(_timerPauses.size() - 1).setEndTime(mesg.getTimestamp().getTimestamp() * 1000);
            }
            break;

         }
      }

      final Long gearChangeData = mesg.getGearChangeData();

      // check if gear data are available, it can be null
      if (gearChangeData != null) {

         // create gear data for the current time
         final GearData gearData = new GearData();

         final com.garmin.fit.DateTime garminTime = mesg.getTimestamp();

         // convert garmin time into java time
         final long garminTimeS = garminTime.getTimestamp();
         final long garminTimeMS = garminTimeS * 1000;
         final long javaTime = garminTimeMS + com.garmin.fit.DateTime.OFFSET;

         gearData.absoluteTime = javaTime;
         gearData.gears = gearChangeData;

         _gearData.add(gearData);
      }
   }

}
