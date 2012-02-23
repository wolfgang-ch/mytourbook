/*******************************************************************************
 * Copyright (C) 2005, 2012  Wolfgang Schramm and Contributors
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
package net.tourbook.photo;

import java.io.File;
import java.util.ArrayList;

import net.tourbook.application.TourbookPlugin;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.ui.TreeViewerItem;
import net.tourbook.ui.UI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This folder viewer is from org.eclipse.swt.examples.fileviewer but with many modifications.
 */
class PicDirFolder {

	private final IPreferenceStore	_prefStore		= TourbookPlugin.getDefault().getPreferenceStore();

	private PicDirImages			_picDirImages;

	private boolean					_isShowFileFolder;

	private File					_selectedFolder	= null;
	private TVIFolderRoot			_rootItem;

	/*
	 * UI controls
	 */
	private Display					_display;

	private TreeViewer				_folderViewer;

	private static final class FolderComparer implements IElementComparer {

		@Override
		public boolean equals(final Object a, final Object b) {

			if (a == b) {
				return true;
			}

			if (a instanceof TVIFolderFolder && b instanceof TVIFolderFolder) {

				final TVIFolderFolder item1 = (TVIFolderFolder) a;
				final TVIFolderFolder item2 = (TVIFolderFolder) b;

				final String folder1Name = item1._treeItemFolder.getName();
				final String folder2Name = item2._treeItemFolder.getName();

				return folder1Name.equals(folder2Name);
			}
			return false;
		}

		@Override
		public int hashCode(final Object element) {

//			final TVIFolderFolder item1 = (TVIFolderFolder) element;
//			final String folderName = item1._treeItemFolder.getName();
//
//			return folderName.hashCode();

			return element.hashCode();
		}
	}

	private class FolderContentProvicer implements ITreeContentProvider {

		public void dispose() {}

		public Object[] getChildren(final Object parentElement) {

			/*
			 * force to get children so that the user can see if a folder can be expanded or not
			 */

			return ((TreeViewerItem) parentElement).getFetchedChildrenAsArray();
		}

		public Object[] getElements(final Object inputElement) {
			return _rootItem.getFetchedChildrenAsArray();
		}

		public Object getParent(final Object element) {
			return ((TreeViewerItem) element).getParentItem();
		}

		public boolean hasChildren(final Object element) {
			return ((TreeViewerItem) element).hasChildren();
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
	}

	PicDirFolder(final PicDirImages picDirImages) {
		_picDirImages = picDirImages;
	}

	void createActions() {
		
//		_actionExpandSelection = new ActionExpandSelection(this);
//		_actionCollapseAll = new ActionCollapseAll();
//		_actionCollapseOthers = new ActionCollapseOthers(this);
	}

	void createUI(final Composite parent) {

		_display = parent.getDisplay();

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(container);
		{
			createUI_10_TreeView(container);
		}

		// update UI from pref store
		_folderViewer.getTree().setLinesVisible(_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));
		updateColors();

	}

	private void createUI_10_TreeView(final Composite parent) {

		/*
		 * create tree layout
		 */

		final Composite layoutContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults()//
				.grab(true, true)
				.hint(200, 100)
				.applyTo(layoutContainer);

		final TreeColumnLayout treeLayout = new TreeColumnLayout();
		layoutContainer.setLayout(treeLayout);

		/*
		 * create viewer
		 */
		final Tree tree = new Tree(layoutContainer, SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

		tree.setHeaderVisible(false);

		_folderViewer = new TreeViewer(tree);

		_folderViewer.setContentProvider(new FolderContentProvicer());
		_folderViewer.setComparer(new FolderComparer());
		_folderViewer.setUseHashlookup(true);

		_folderViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {

				// expand/collapse current item
				final Object selection = ((IStructuredSelection) _folderViewer.getSelection()).getFirstElement();

				final TreeViewerItem treeItem = (TreeViewerItem) selection;

				if (_folderViewer.getExpandedState(treeItem)) {
					_folderViewer.collapseToLevel(treeItem, 1);
				} else {

					if (treeItem.hasChildren()) {
						_folderViewer.expandToLevel(treeItem, 1);
					}
				}
			}
		});

		_folderViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				onSelectFolder(event);
			}
		});

		/*
		 * create columns
		 */
		TreeViewerColumn tvc;
		TreeColumn tvcColumn;

		// column: os folder
		tvc = new TreeViewerColumn(_folderViewer, SWT.TRAIL);
		tvcColumn = tvc.getColumn();
		tvc.setLabelProvider(new StyledCellLabelProvider() {

			@Override
			public void update(final ViewerCell cell) {

				final Object element = cell.getElement();

				if (element instanceof TVIFolderFolder) {
					final TVIFolderFolder folderItem = (TVIFolderFolder) element;

					final StyledString styledString = new StyledString();

					styledString.append(folderItem._folderName);

					if (_isShowFileFolder) {

						// force that file list is loaded and number of files is available
						folderItem.hasChildren();

						final int folderCounter = folderItem.getFolderCounter();
						if (folderCounter > 0) {
							styledString.append(UI.SPACE2);
							styledString.append(Integer.toString(folderCounter), UI.PHOTO_FOLDER_STYLER);
						}

						final int fileCounter = folderItem.getFileCounter();
						if (fileCounter > 0) {
							styledString.append(UI.SPACE2);
							styledString.append(Integer.toString(fileCounter), UI.PHOTO_FILE_STYLER);
						}
					}

					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
			}
		});
		treeLayout.setColumnData(tvcColumn, new ColumnWeightData(100, true));
	}

	void fillActionBar() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Gets filesystem root entries
	 * 
	 * @return an array of Files corresponding to the root directories on the platform, may be empty
	 *         but not null
	 */
	private File[] getRootsSorted() {

		final File[] roots = File.listRoots();

		PicDirView.sortFiles(roots);

		return roots;

		/*
		 * On JDK 1.22 only...
		 */
		// return File.listRoots();

		/*
		 * On JDK 1.1.7 and beyond... -- PORTABILITY ISSUES HERE --
		 */
//		if (System.getProperty("os.name").indexOf("Windows") != -1) {
//
//			final ArrayList<File> list = new ArrayList<File>();
//
//			for (char i = 'c'; i <= 'z'; ++i) {
//
//				final File drive = new File(i + ":" + File.separator);
//
//				if (drive.isDirectory() && drive.exists()) {
//
//					list.add(drive);
//
//					if (initial && i == 'c') {
//						_selectedFolder = drive;
//						initial = false;
//					}
//				}
//			}
//
//			final File[] roots = list.toArray(new File[list.size()]);
//
//			PicDirView.sortFiles(roots);
//
//			return roots;
//
//		} else {
//
//			final File root = new File(File.separator);
//			if (initial) {
//				_selectedFolder = root;
//				initial = false;
//			}
//			return new File[] { root };
//		}
	}

	File getSelectedFolder() {
		return _selectedFolder;
	}

	Tree getTree() {
		return _folderViewer.getTree();
	}

	void handlePrefStoreModifications(final PropertyChangeEvent event) {

		final String property = event.getProperty();
		boolean isViewerRefresh = false;

		if (property.equals(ITourbookPreferences.VIEW_LAYOUT_CHANGED)) {

			_folderViewer.getTree().setLinesVisible(
					_prefStore.getBoolean(ITourbookPreferences.VIEW_LAYOUT_DISPLAY_LINES));

			isViewerRefresh = true;

		} else if (property.equals(ITourbookPreferences.PHOTO_VIEWER_PREF_STORE_EVENT)) {

			updateColors();

			isViewerRefresh = true;
		}

		if (isViewerRefresh) {

			_folderViewer.refresh();

			/*
			 * the tree must be redrawn because the styled text does not show with the new color
			 */
			_folderViewer.getTree().redraw();
		}
	}

	private void onSelectFolder(final SelectionChangedEvent event) {

		final TreeSelection treeSelection = (TreeSelection) event.getSelection();
		final TreePath[] treePaths = treeSelection.getPaths();
		if (treePaths.length == 0) {
			return;
		}
		final TreePath treePath = treePaths[0];
		if (treePath == null) {
			return;
		}

		final TVIFolderFolder tviFolder = (TVIFolderFolder) treePath.getLastSegment();
		final File selectedFolder = tviFolder._treeItemFolder;

		if (selectedFolder == null) {
			return;
		}

		// optimize, don't select again the same folder
		if (_selectedFolder != null && selectedFolder.equals(_selectedFolder)) {
			return;
		}

		_selectedFolder = selectedFolder;

		/*
		 * image gallery: displays the contents of the selected directory
		 */
		_picDirImages.showImages(selectedFolder);
	}

	void restoreFolder(final String folderPath) {

		BusyIndicator.showWhile(_display, new Runnable() {
			public void run() {

				// set root item
				_rootItem = new TVIFolderRoot(_folderViewer, getRootsSorted());

				_folderViewer.setInput(new Object());

				_picDirImages.showImages(_selectedFolder);

				// Remind everyone where we are in the filesystem
				File restoreFolder = _selectedFolder;
				_selectedFolder = null;

				if (folderPath != null) {
					final File folderFile = new File(folderPath);
					if (folderFile.isDirectory()) {
						restoreFolder = folderFile;
					}
				}

				final String restorePathName = restoreFolder.getAbsolutePath();

				final IPath restorePath = new Path(restorePathName);
				final IPath restoreRoot = new Path(restorePathName).removeFirstSegments(9999);

				final String[] folderSegments = restorePath.segments();
				final ArrayList<String> allFolderSegments = new ArrayList<String>();

				allFolderSegments.add(restoreRoot.toOSString());
				for (final String folderSegmentName : folderSegments) {
					allFolderSegments.add(folderSegmentName);
				}

				final ArrayList<TVIFolder> treePathItems = new ArrayList<TVIFolder>();
				TVIFolder folderSegmentItem = _rootItem;
				treePathItems.add(folderSegmentItem);

				// create tree path for each folder segment
				for (final String folderSegmentName : allFolderSegments) {

					boolean isPathSegmentAvailable = false;

					final ArrayList<TreeViewerItem> tviChildren = folderSegmentItem.getFetchedChildren();
					for (final TreeViewerItem tviChild : tviChildren) {

						final TVIFolderFolder childFolder = (TVIFolderFolder) tviChild;
						String childFolderName;

						if (childFolder._isRootFolder) {

							if (UI.IS_WIN) {
								// remove \ from device name
								childFolderName = childFolder._folderName.substring(0, 2);
							} else {
								childFolderName = childFolder._folderName;
							}

						} else {

							childFolderName = childFolder._folderName;
						}

						if (folderSegmentName.equals(childFolderName)) {

							isPathSegmentAvailable = true;

							treePathItems.add(childFolder);
							folderSegmentItem = childFolder;

							break;
						}
					}

					if (isPathSegmentAvailable == false) {
						// requested path is not available, select partial path in the viewer
						break;
					}
				}

				if (treePathItems.size() == 0) {
					// there is nothing which can be selected
					return;
				}

				final TVIFolder[] treePathArray = treePathItems.toArray(new TVIFolder[treePathItems.size()]);
				final TreePath treePath = new TreePath(treePathArray);
				final ITreeSelection selection = new TreeSelection(treePath);

				_folderViewer.setSelection(selection, true);
			}
		});
	}

	private void updateColors() {

		_isShowFileFolder = _prefStore.getBoolean(ITourbookPreferences.PHOTO_VIEWER_IS_SHOW_FILE_FOLDER);
		final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();

		final Tree tree = _folderViewer.getTree();

		tree.setForeground(colorRegistry.get(ITourbookPreferences.PHOTO_VIEWER_COLOR_FOREGROUND));
		tree.setBackground(colorRegistry.get(ITourbookPreferences.PHOTO_VIEWER_COLOR_BACKGROUND));
	}

}
