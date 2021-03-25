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
package net.tourbook.map25.action;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.map2.Messages;
import net.tourbook.map25.Map25View;

import org.eclipse.jface.action.Action;

public class ActionSynchMapWithTour extends Action {

	private Map25View _map25View;

	public ActionSynchMapWithTour(final Map25View mapView) {

		super(null, AS_CHECK_BOX);

		_map25View = mapView;

		setToolTipText(Messages.map_action_synch_with_tour);

      setImageDescriptor(TourbookPlugin.getImageDescriptor(Images.SyncWith_Tour));
      setDisabledImageDescriptor(TourbookPlugin.getImageDescriptor(Images.SyncWith_Tour_Disabled));
	}

	@Override
	public void run() {
		_map25View.actionSync_WithTour(isChecked());
	}

}
