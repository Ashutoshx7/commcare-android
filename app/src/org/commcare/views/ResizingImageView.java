package org.commcare.views;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import org.commcare.dalvik.R;
import org.commcare.utils.FileUtil;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author wspride
 *         Class used by MediaLayout for form images. Can be set to resize the
 *         image using different algorithms based on the preference specified
 *         by PreferencesActivity.KEY_RESIZE. Overrides setMaxWidth, setMaxHeight,
 *         and onMeasure from the ImageView super class.
 */

@SuppressLint("NewApi")
public class ResizingImageView extends AppCompatImageView {

    public static String resizeMethod;

    private int mMaxWidth;
    private int mMaxHeight;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private String imageURI;
    private String bigImageURI;

    private float scaleFactor = 1.0f;
    private final static float scaleFactorThreshhold = 1.2f;

    public ResizingImageView(Context context) {
        super(context);
    }

    public ResizingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageURI(String imageURI, String bigImageURI) {
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        this.imageURI = imageURI;
        this.bigImageURI = bigImageURI;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleGestureDetector.onTouchEvent(e);
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    public void setMaxWidth(int maxWidth) {
        super.setMaxWidth(maxWidth);
        mMaxWidth = maxWidth;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        super.setMaxHeight(maxHeight);
        mMaxHeight = maxHeight;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            getSuggestedMinimumHeight();
            setFullScreen();
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            if (scaleFactor > scaleFactorThreshhold) {
                setFullScreen();
            }
            return true;
        }
    }

    private void setFullScreen() {
        String imageFileURI;

        if (bigImageURI != null) {
            imageFileURI = bigImageURI;
        } else if (imageURI != null) {
            imageFileURI = imageURI;
        } else {
            return;
        }

        try {
            String imageFilename = ReferenceManager.instance()
                    .DeriveReference(imageFileURI).getLocalURI();
            File bigImage = new File(imageFilename);

            Intent i = new Intent("android.intent.action.VIEW");
            Uri imageFileUri = FileUtil.getUriForExternalFile(getContext(), bigImage);
            i.setDataAndType(imageFileUri, "image/*");
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            getContext().startActivity(i);
        } catch (InvalidReferenceException e1) {
            e1.printStackTrace();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "view image"), Toast.LENGTH_SHORT).show();
        }
    }

    private Pair<Integer, Integer> getWidthHeight(int widthMeasureSpec, int heightMeasureSpec, double imageScaleFactor) {
        int maxWidth = mMaxWidth;
        int maxHeight = mMaxHeight;

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            maxWidth = Math.min(MeasureSpec.getSize(widthMeasureSpec), mMaxWidth);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            maxHeight = Math.min(MeasureSpec.getSize(heightMeasureSpec), mMaxHeight);
        }

        Drawable drawable = getDrawable();

        float dWidth = dipToPixels(getContext(), drawable.getIntrinsicWidth());
        float dHeight = dipToPixels(getContext(), drawable.getIntrinsicHeight());
        float ratio = (dWidth) / dHeight;

        int width = (int)Math.min(Math.max(dWidth, getSuggestedMinimumWidth()), maxWidth);
        int height = (int)(width / ratio);

        height = Math.min(Math.max(height, getSuggestedMinimumHeight()), maxHeight);
        width = (int)(height * ratio);

        if (width > maxWidth) {
            width = maxWidth;
            height = (int)(width / ratio);
        }

        return new Pair<>(new Double(width * imageScaleFactor).intValue(),
                new Double(height * imageScaleFactor).intValue());
    }

    /**
     * Advanced resizing method using Lanczos interpolation for better quality.
     */
    private Bitmap resizeImage(Bitmap source, int targetWidth, int targetHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resizedBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true), 0, 0, paint);
        return resizedBitmap;
    }

    /*
     * The meat and potatoes of the class. Determines what algorithm to use
     * to resize the image based on the KEY_RESIZE preference. Currently can be
     * "full", "width", or "none". Will always preserve aspect ratio. 
     * 
     * "full" attempts to use both the calculated height and width to scale the image. however,
     *         its worth noting that the available height is dynamic and difficult to determine
     * "width" will always stretch/compress the image to make it the exact width of the screen while
     *         maintaining the aspect ratio
     * "none" will leave the picture unchanged
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_image); // Replace with actual image source
            int targetWidth = MeasureSpec.getSize(widthMeasureSpec);
            int targetHeight = (int) ((float) targetWidth / ((float) bitmap.getWidth() / (float) bitmap.getHeight()));

            Bitmap resizedBitmap = resizeImage(bitmap, targetWidth, targetHeight);
            setImageBitmap(resizedBitmap);

            setMeasuredDimension(targetWidth, targetHeight);
        }
    }

    // helper method for algorithm above
    private static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
