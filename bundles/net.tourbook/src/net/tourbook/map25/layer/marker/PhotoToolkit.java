package net.tourbook.map25.layer.marker;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.imgscalr.Scalr.Rotation;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.backend.canvas.Paint;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ClusterMarkerRenderer;
import org.oscim.layers.marker.ItemizedLayer;
//import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerRendererFactory;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.marker.MarkerSymbol.HotspotPlace;
import net.tourbook.map25.layer.marker.MarkerLayer.OnItemGestureListener;

import de.byteholder.geoclipse.map.Map;
import de.byteholder.geoclipse.map.Tile;
import net.tourbook.common.color.ColorUtil;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.Util;
import net.tourbook.data.TourData;
import net.tourbook.data.TourMarker;
import net.tourbook.map.bookmark.MapBookmark;
import net.tourbook.map25.Map25App;
import net.tourbook.map25.Map25ConfigManager;
import net.tourbook.map25.Map25View;
import net.tourbook.photo.ILoadCallBack;
import net.tourbook.photo.ImageQuality;
import net.tourbook.photo.Photo;
import net.tourbook.photo.PhotoImageCache;
import net.tourbook.photo.PhotoLoadManager;
import net.tourbook.photo.PhotoLoadingState;
import net.tourbook.photo.ImageUtils;


public class PhotoToolkit extends MarkerToolkit implements ItemizedLayer.OnItemGestureListener<MarkerItem> {


   private Bitmap _bitmapCluster;
   //private boolean _isBillboard;
   
   public MarkerSymbol _symbol;  //marker symbol, circle or star

   private Bitmap _bitmapPhoto;  //normaly the photo as Bitmap
   private Bitmap _BitmapClusterPhoto;  // The Bitmap when markers are clustered
   private ArrayList<Photo> _allPhotos;

   public MarkerRendererFactory _markerRendererFactory;
   
   public boolean _isMarkerClusteredLast;
   
   Display  _display;
   
   private Map25App _mapApp;
   
//   private int  _imageSize;
//   private static final String      STATE_PHOTO_PROPERTIES_IMAGE_SIZE      = "STATE_PHOTO_PROPERTIES_IMAGE_SIZE";       //$NON-NLS-1$
//   private IDialogSettings       _state;

   private class LoadCallbackImage implements ILoadCallBack {

      private Map25App         _mapApp;
      //private Map25View = _map25App.

      public LoadCallbackImage() {
         //_map25View = null;
         //_mapApp = null;
      }

      @Override
      public void callBackImageIsLoaded(final boolean isUpdateUI) {

//         debugPrint("???? PhotoToolkit: LoadCallbackImage"); //$NON-NLS-1$
         //updatePhotos(); has only updateUI...
         _mapApp.updateUI_PhotoLayer();
//         if (isUpdateUI == false) {
//            return;
//         }

      }
   }   
   
   
   public PhotoToolkit() {
      super(MarkerShape.CIRCLE);
      //debugPrint(" ?? PhotoToolkit + *** Constructor"); //$NON-NLS-1$
      final MarkerConfig config = Map25ConfigManager.getActiveMarkerConfig();

      loadConfig();
      
      _fillPainter.setStyle(Paint.Style.FILL);
      
      _bitmapCluster = createClusterBitmap(1);
      
      //_bitmapPhoto = createPhotoBitmap();
      _bitmapPhoto = createPoiBitmap(MarkerShape.CIRCLE);
      
      _BitmapClusterPhoto = createPoiBitmap(MarkerShape.CIRCLE); //must be replaced later, like MarkerToolkit
      
      _symbol = new MarkerSymbol(_bitmapPhoto, MarkerSymbol.HotspotPlace.BOTTOM_CENTER, false);
      
      _isMarkerClusteredLast = config.isMarkerClustered;

      
      _markerRendererFactory = new MarkerRendererFactory() {
         @Override
         public org.oscim.layers.marker.MarkerRenderer create(org.oscim.layers.marker.MarkerLayer markerLayer) {
            return new ClusterMarkerRenderer(markerLayer, _symbol, new ClusterMarkerRenderer.ClusterStyle(Color.WHITE, Color.BLUE)) {
               @Override
               protected Bitmap getClusterBitmap(int size) {
                  // Can customize cluster bitmap here
                  //debugPrint("??? Markertoolkit:  cluster size: " + size); //$NON-NLS-1$
                  _bitmapCluster = createClusterBitmap(size);
                  return _bitmapCluster;
               }
            };
         }
      };
   }

   public List<MarkerItem> createPhotoItemList(ArrayList<Photo> galleryPhotos){
      Map25App.debugPrint(" Phototoolkit createPhotoItemList: entering "); //$NON-NLS-1$
      List<MarkerItem> pts = new ArrayList<>();
      
      if (galleryPhotos == null) {
         Map25App.debugPrint(" Map25View: *** createPhotoItemList: galleriePhotos was null"); //$NON-NLS-1$
         return pts;
         }

      if (galleryPhotos.size() == 0) {
         Map25App.debugPrint(" Map25View: *** createPhotoItemList: galleriePhotos.size() was 0"); //$NON-NLS-1$
         return  pts;     
      }
      
      /*if (!_isShowPhoto) {
         Map25App.debugPrint(" Map25View: *** createPhotoItemList: photlayer is off");
         return pts;
      }*/
      
      _allPhotos = galleryPhotos;
      
      for (final  Photo photo : galleryPhotos) {
         int stars = 0;
         String starText = ""; //$NON-NLS-1$
         String photoName = ""; //$NON-NLS-1$
         UUID photoKey = UUID.randomUUID();

         stars = photo.ratingStars;
         //starText = "";
         switch (stars) {
         case 1:
            starText = " *"; //$NON-NLS-1$
         case 2:
            starText = " **"; //$NON-NLS-1$
         case 3:
            starText = " ***"; //$NON-NLS-1$
         case 4:
            starText = " ****"; //$NON-NLS-1$
         case 5:
            starText = " *****"; //$NON-NLS-1$
         }
         photoName = TimeTools.getZonedDateTime(photo.imageExifTime).format(TimeTools.Formatter_Time_S) + starText;       

         String photoDescription = "Ratingstars: " + Integer.toString(photo.ratingStars); //$NON-NLS-1$
         
         Double photoLat = 0.0;
         Double photoLon = 0.0;
         if(photo.isGeoFromExif &&
               Math.abs(photo.getImageMetaData().latitude) > Double.MIN_VALUE  &&
               Math.abs(photo.getImageMetaData().longitude) > Double.MIN_VALUE
               )
         {
//            Map25App.debugPrint("PhotoToolkit: *** createPhotoItemList: using exif geo");
            photoLat = photo.getImageMetaData().latitude;
            photoLon = photo.getImageMetaData().longitude;
         } else {
            Map25App.debugPrint("PhotoToolkit: *** createPhotoItemList: using tour geo"); //$NON-NLS-1$
            photoLat = photo.getTourLatitude();
            photoLon = photo.getTourLongitude();
         }
         
         //Map25App.debugPrint("PhotoToolkit: *** createPhotoItemList Name: " + photo.getImageMetaData().objectName + " Description: " + photo.getImageMetaData().captionAbstract);
         
         MarkerItem item = new MarkerItem(photoKey, photoName, photoDescription,
               new GeoPoint(photoLat, photoLon)
               );
         MarkerSymbol markerSymbol = createPhotoBitmapFromPhoto(photo, item);
        
         if (markerSymbol != null) {
            item.setMarker(markerSymbol);
         }        
         
         pts.add(item);
      }
      //_photo_pts = pts;
      _allPhotos = galleryPhotos; 
      
      return pts;   
   }
   
   
   public Image getPhotoImage(Photo photo, int thumbSize) {
      Image photoImage = null;
      Image scaledThumbImage = null;
      final ImageQuality requestedImageQuality = ImageQuality.THUMB;

      // check if image has an loading error
      final PhotoLoadingState photoLoadingState = photo.getLoadingState(requestedImageQuality);
      if (photoLoadingState != PhotoLoadingState.IMAGE_IS_INVALID) {
         //debugPrint("??? entering getPhotoImage"); //$NON-NLS-1$
         // image is not yet loaded

         // check if image is in the cache
         photoImage = PhotoImageCache.getImage(photo, requestedImageQuality);

         if ((photoImage == null || photoImage.isDisposed())
               && photoLoadingState == PhotoLoadingState.IMAGE_IS_IN_LOADING_QUEUE == false) {

            // the requested image is not available in the image cache -> image must be loaded

            final ILoadCallBack imageLoadCallback = new LoadCallbackImage();

            PhotoLoadManager.putImageInLoadingQueueThumbMap(photo, requestedImageQuality, imageLoadCallback);
         }

         if (photoImage != null){

            Rectangle imageBounds = photoImage.getBounds();
            final int originalImageWidth = imageBounds.width;
            final int originalImageHeight = imageBounds.height;  

            int imageWidth = originalImageWidth;
            int imageHeight = originalImageHeight;

            //final int thumbSize = PhotoLoadManager.IMAGE_SIZE_THUMBNAIL;//    PhotoLoadManager.IMAGE_SIZE_LARGE_DEFAULT;
            boolean isRotated = false;

            final Point bestSize = ImageUtils.getBestSize(imageWidth, imageHeight, thumbSize, thumbSize);
            Rotation thumbRotation = null;  
            if (isRotated == false) {
               isRotated = true;
               //thumbRotation = getRotation();
            }

            scaledThumbImage = ImageUtils.resize(
                  _display,
                  photoImage,
                  bestSize.x,
                  bestSize.y,
                  SWT.ON,
                  SWT.LOW,
                  thumbRotation);          

         }  else {
            scaledThumbImage = null;
         }
      }
      
      
      return scaledThumbImage;
   }
   
   
   /**
    * same as in TourMapPainter, but for 2.5D maps
    * @param photo
    * @return the bitmap
    */
   public  Bitmap getPhotoBitmap(final Photo photo) {
      Bitmap photoBitmap = null;

      // using photo image size of 2D map, not working yet
      //_imageSize = Util.getStateInt(_state, STATE_PHOTO_PROPERTIES_IMAGE_SIZE, Photo.MAP_IMAGE_DEFAULT_WIDTH_HEIGHT);
      // ensure that an image is displayed, it happend that image size was 0
      //debugPrint("??? getPhotoBitmap imageSize: " + photoBitmap.getWidth() + _imageSize);
      //final Image scaledThumbImage = getPhotoImage(photo, _imageSize);
      
      final Image scaledThumbImage = getPhotoImage(photo, PhotoLoadManager.IMAGE_SIZE_THUMBNAIL);

      if (scaledThumbImage != null) {
         try {
            //photoBitmap = CanvasAdapter.decodeBitmap(new ByteArrayInputStream(ImageUtils.formatImage(photoImage, org.eclipse.swt.SWT.IMAGE_BMP)));
            photoBitmap = CanvasAdapter.decodeBitmap(new ByteArrayInputStream(ImageUtils.formatImage(scaledThumbImage, org.eclipse.swt.SWT.IMAGE_BMP)));
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      return photoBitmap;
   }
   
   
   public MarkerSymbol createPhotoBitmapFromPhoto(Photo photo, MarkerItem item) {
      Bitmap bitmapImage = getPhotoBitmap(photo);
      MarkerSymbol bitmapPhoto = null;
      
      if (bitmapImage == null) {
         bitmapImage = _bitmapPhoto;
      }
      bitmapPhoto = createAdvanceSymbol(item, bitmapImage, true);
 
      return bitmapPhoto;
   }
 
   public void updatePhotos() {
      //net.tourbook.map25.Map25App.debugPrint("???? PhotoToolkit: Update Photos");
      _mapApp.updateUI_PhotoLayer();
   }

   public void showPhoto(Photo photo) {

      final Image image = getPhotoImage(photo, PhotoLoadManager.IMAGE_SIZE_LARGE_DEFAULT);
      if(image == null) {
         return;
      }

      final Display display = new Display();
      final Shell shell = new Shell(display);
      shell.setSize(image.getBounds().width, image.getBounds().height);
      shell.setText(photo.imageFileName);
      shell.setLayout(new FillLayout());
      Canvas canvas = new Canvas(shell, SWT.NONE);

      canvas.addPaintListener(new PaintListener() {
         public void paintControl(PaintEvent e) {
            e.gc.drawImage(image, 10, 10);
            image.dispose();
         }
      });

      shell.open();
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }

      display.dispose();
   }
   
   @Override
   public boolean onItemLongPress(int index, MarkerItem photoItem) {
      // TODO Auto-generated method stub
      //debugPrint(" ??????????? PhotoToolkit *** onItemLongPress(int index, MarkerItem photoItem): " + arg0 + " " + arg1);
      debugPrint(" ??????????? PhotoToolkit *** onItemLongPress(int index, MarkerItem photoItem): " + _allPhotos.get(index).imageFilePathName + " " + photoItem.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
   }

   @Override
   public boolean onItemSingleTapUp(int index, MarkerItem photoItem) {
      // TODO Auto-generated method stub
      debugPrint(" ??????????? PhotoToolkit *** onItemSingleTapUp(int index, MarkerItem photoItem): " + _allPhotos.get(index).imageFilePathName + " " + photoItem.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
      //showPhoto(_allPhotos.get(index));
      return false;
   }

   
}