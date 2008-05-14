/*******************************************************************************
 * Copyright (C) 2001, 2008  Wolfgang Schramm and Contributors
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
package net.tourbook.tag;

import net.tourbook.data.TourTag;
import net.tourbook.tour.TreeViewerItem;

public class TVITourTag extends TreeViewerItem {

	private TourTag	fTourTag;

	public TVITourTag(final TourTag tourTag) {
		fTourTag = tourTag;
	}

	@Override
	protected void fetchChildren() {

	}

	public TourTag getTourTag() {
		return fTourTag;
	}

	@Override
	protected void remove() {

	}

}
