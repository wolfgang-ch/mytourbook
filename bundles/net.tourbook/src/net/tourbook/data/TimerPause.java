/*******************************************************************************
 * Copyright (C) 2020 Frédéric Bard and Contributors
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
package net.tourbook.data;

import java.io.Serializable;

public class TimerPause implements Serializable {

   private static final long serialVersionUID = 1L;

   private long _startTime;
   private long _endTime;

   public TimerPause(final long startTime, final long endTime) {
      _startTime = startTime;
      _endTime = endTime;
   }

   public long getEndTime() {
      return _endTime;
   }

   public long getPauseDuration() {
      return _endTime - _startTime;
   }

   public long getStartTime() {
      return _startTime;
   }

   public void setEndTime(final long endTime) {
      this._endTime = endTime;
   }

   public void setStartTime(final long startTime) {
      this._startTime = startTime;
   }
}