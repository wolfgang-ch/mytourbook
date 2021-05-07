/*******************************************************************************
 * Copyright (C) 2005, 2021 Wolfgang Schramm and Contributors
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
package net.tourbook.map2.action;

import java.util.Map;

import net.tourbook.Images;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.color.ThemeUtil;
import net.tourbook.common.util.Util;
import net.tourbook.map2.view.Map2View;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

public class ActionHandler_OpenView_Map2 extends AbstractHandler implements IElementUpdater {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {

      Util.showView(Map2View.ID, true);

      return null;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public void updateElement(final UIElement uiElement, final Map parameters) {

      // set themed icon

      uiElement.setIcon(TourbookPlugin.getImageDescriptor(
            ThemeUtil.getThemedImageName(Images.Map2)));
   }
}