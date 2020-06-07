/*******************************************************************************
 * Copyright (C) 2020 Frédéric Bard
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
package net.tourbook.common;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.eclipse.swt.widgets.Shell;

/**
 * A class to implement and use a File System in MyTourbook.
 * This file system can be used by the easy import manager.
 *
 * @author Frédéric Bard
 */
public abstract class TourbookFileSystem {

   private String FILE_SYSTEM_ID;
   public String  FILE_SYSTEM_FOLDER;

   public TourbookFileSystem(final String fileSystemId) {
      FILE_SYSTEM_ID = fileSystemId;
   }

   /**
    * Closes all the necessary resources of the file system.
    */
   protected abstract void close();

   /**
    * Copies a file from the file system to the local user's
    * file system
    *
    * @param filePath
    *           The absolute file path of the locally copied file
    * @return The {@link File} of the copied file.
    */
   protected abstract File copyFileLocally(String filePath);

   /**
    * Retrieves a list of {@link FileStore} for the file system.
    *
    * @return
    */
   public abstract Iterable<FileStore> getFileStore();

   /**
    * Gets the file system as an instance of {@link FileSystem}.
    *
    * @return
    */
   protected abstract FileSystem getFileSystem();

   public String getFileSystemFolder() {
      return FILE_SYSTEM_FOLDER;
   }

   /**
    * Gets the {@link Path} of a given folder in the file system.
    *
    * @param folderName
    * @return
    */
   protected abstract Path getfolderPath(String folderName);

   /**
    * Gets the unique identifier of the file system.
    *
    * @return
    */
   public String getId() {
      return FILE_SYSTEM_ID;
   }

   /**
    * Gets file system's preference page ID so it can be opened from a menu outside the cloud
    * plugin.
    *
    * @return
    */
   public abstract String getPreferencePageId();

   /**
    * Provides a way, for the user, to select a specific folder to be used in this file system.
    * The chosen folder path needs to be saved in {@link #FILE_SYSTEM_FOLDER}
    *
    * @param shell
    */
   public abstract String selectFileSystemFolder(Shell shell);
}
