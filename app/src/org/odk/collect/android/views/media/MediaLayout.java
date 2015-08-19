package org.odk.collect.android.views.media;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.commcare.dalvik.R;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.Logger;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QRCodeEncoder;
import org.odk.collect.android.views.ResizingImageView;

import java.io.File;

/**
 * This layout is used anywhere we can have image/audio/video/text. TODO: It
 * would probably be nice to put this in a layout.xml file of some sort at some
 * point.
 *
 * @author carlhartung
 */
public class MediaLayout extends RelativeLayout {
    private static final String t = "AVTLayout";

    private TextView mView_Text;
    private VideoView mVideoView;
    private AudioButton mAudioButton;
    private ImageButton mVideoButton;
    private ResizingImageView mImageView;
    private TextView mMissingImage;

    public MediaLayout(Context c) {
        super(c);

        mView_Text = null;
        mAudioButton = null;
        mImageView = null;
        mMissingImage = null;
        mVideoButton = null;
    }

    public void setAVT(TextView text, String audioURI, String imageURI,
                       final String videoURI, final String bigImageURI) {
        setAVT(text, audioURI, imageURI, videoURI, bigImageURI, null);
    }

    public void setAVT(TextView text, String audioURI, String imageURI,
                       final String videoURI, final String bigImageURI, final String qrCodeContent) {
        setAVT(text, audioURI, imageURI, videoURI, bigImageURI, null, null);
    }

    public void setAVT(TextView text, String audioURI, String imageURI,
                       final String videoURI, final String bigImageURI,
                       final String qrCodeContent, String inlineVideoURI) {
        mView_Text = text;

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams =
            new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        imageParams.addRule(CENTER_IN_PARENT);
        RelativeLayout.LayoutParams videoParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams topPaneParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout topPane = new RelativeLayout(this.getContext());
        topPane.setId(2342134);

        this.addView(topPane, topPaneParams);

        if (audioURI != null) {
            mAudioButton = new AudioButton(getContext(), audioURI, true);
             // random ID to be used by the relative layout.
            mAudioButton.setId(3245345);
        }

        // Then set up the video button
        if (videoURI != null) {
            mVideoButton = new ImageButton(getContext());
            mVideoButton.setImageResource(android.R.drawable.ic_media_play);
            mVideoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String videoFilename = "";
                    try {
                        videoFilename =
                            ReferenceManager._().DeriveReference(videoURI).getLocalURI();
                    } catch (InvalidReferenceException e) {
                        Log.e(t, "Invalid reference exception");
                        e.printStackTrace();
                    }

                    File videoFile = new File(videoFilename);
                    if (!videoFile.exists()) {
                        // We should have a video clip, but the file doesn't exist.
                        String errorMsg =
                            getContext().getString(R.string.file_missing, videoFilename);
                        Log.e(t, errorMsg);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent i = new Intent("android.intent.action.VIEW");
                    i.setDataAndType(Uri.fromFile(videoFile), "video/*");
                    try {
                        String uri = Uri.fromFile(videoFile).getPath().replaceAll("^.*\\/", "");
                        Logger.log("media", "start " + uri);
                        getContext().startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(),
                            getContext().getString(R.string.activity_not_found, "view video"),
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mVideoButton.setId(234982340);
        }

        // Add the audioButton and videoButton (if applicable) and view
        // (containing text) to the relative layout.
        if (mAudioButton != null && mVideoButton == null) {
            audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            topPane.addView(mAudioButton, audioParams);
        } else if (mAudioButton == null && mVideoButton != null) {
            videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
            topPane.addView(mVideoButton, videoParams);
        } else if (mAudioButton != null && mVideoButton != null) {
            audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            videoParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
            topPane.addView(mAudioButton, audioParams);
            topPane.addView(mVideoButton, videoParams);
        } else {
            //Audio and Video are both null, let text bleed to right
            textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        boolean textVisible = (mView_Text.getVisibility() != GONE);
        if (textVisible) {
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            topPane.addView(mView_Text, textParams);
        }

        // Now set up the image view
        String errorMsg = null;

        View imageView= null;
        if(inlineVideoURI != null) {
            String error = null;
            try {
                final String imageFilename = ReferenceManager._().DeriveReference(inlineVideoURI).getLocalURI();

//                MediaPlayer mp = new MediaPlayer();
//                mp.setDataSource(imageFilename);
//                mp.prepare();
//
//                int preferredVideoWidth = mp.getVideoWidth();
//                int preferredVideoHeight = mp.getVideoHeight();

                //Not sure if this is useful yet
                int[] maxBounds = getMaxImageViewBounds();

                File videoFile = new File(imageFilename);
                if(!videoFile.exists()) {
                    error = "No video file found at: " + imageFilename;
                } else {
                    //NOTE: This has odd behavior when you have a text input on the screen
                    //since clicking the video view to bring up controls has weird effects.
                    //since we shotgun grab the focus for the input widget.

                    final MediaController ctrl = new MediaController(this.getContext());

                    mVideoView = new VideoView(this.getContext());
                    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            ctrl.show();
                        }
                    });
                    mVideoView.setVideoPath(imageFilename);
                    mVideoView.setMediaController(ctrl);
                    ctrl.setAnchorView(mVideoView);

                    //These surprisingly get re-jiggered as soon as the video is loaded, so we
                    //just want to give it the _max_ bounds, it'll pick the limiter and shrink
                    //itself when it's ready.
                    imageParams.width = maxBounds[0];
                    imageParams.height = maxBounds[1];

                    imageView = mVideoView;
                }

            }catch(InvalidReferenceException ire) {
                Log.e(t, "invalid video reference exception");
                ire.printStackTrace();
                error = "Invalid reference: " + ire.getReferenceString();
            }

            if(error != null) {
                mMissingImage = new TextView(getContext());
                mMissingImage.setText(errorMsg);
                mMissingImage.setPadding(10, 10, 10, 10);
                mMissingImage.setId(234873453);
                imageView = mMissingImage;

            }

        }
        else if(qrCodeContent != null ) {
            Bitmap image;
            Display display =
                    ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                            .getDefaultDisplay();

            //see if we're doing a new QR code display
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();

            int minimumDim = Math.min(screenWidth,  screenHeight);

            try {
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrCodeContent,minimumDim);

                image = qrCodeEncoder.encodeAsBitmap();

                mImageView = new ResizingImageView(getContext());
                mImageView.setPadding(10, 10, 10, 10);
                mImageView.setAdjustViewBounds(true);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImageView.setImageBitmap(image);
                mImageView.setId(23423534);

                imageView = mImageView;
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else if (imageURI != null) {
            try {
                int[] maxBounds = getMaxImageViewBounds();

                //If we didn't get an image yet, try for a norm
                final String imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                final File imageFile = new File(imageFilename);
                if (imageFile.exists()) {
                    Bitmap b = null;
                    try {
                        Display display =
                                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                                        .getDefaultDisplay();


                        int screenWidth = display.getWidth();
                        int screenHeight = display.getHeight();
                        b =
                            FileUtils
                                    .getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                    } catch (OutOfMemoryError e) {
                        errorMsg = "ERROR: " + e.getMessage();
                    }

                    if (b != null) {

                        ImageView mImageView = new ImageView(getContext());;

                        //config specially if we need to do a custom resize
                        if(ResizingImageView.resizeMethod.equals("full") || ResizingImageView.resizeMethod.equals("half")
                                || ResizingImageView.resizeMethod.equals("width")){
                            mImageView = new ResizingImageView(getContext(), imageURI, bigImageURI);
                            mImageView.setAdjustViewBounds(true);
                            mImageView.setMaxWidth(maxBounds[0]);
                            mImageView.setMaxHeight(maxBounds[1]);
                        }

                        mImageView.setPadding(10, 10, 10, 10);
                        mImageView.setImageBitmap(b);
                        mImageView.setId(23423534);
                        imageView = mImageView;
                    } else if (errorMsg == null) {
                        // An error hasn't been logged and loading the image failed, so it's likely
                        // a bad file.
                        errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                    }
                } else {
                    // An error hasn't been logged. We should have an image, but the file doesn't
                    // exist.
                    errorMsg = getContext().getString(R.string.file_missing, imageFile);
                }

                if (errorMsg != null) {
                    // errorMsg is only set when an error has occured
                    Log.e(t, errorMsg);
                    mMissingImage = new TextView(getContext());
                    mMissingImage.setText(errorMsg);
                    mMissingImage.setPadding(10, 10, 10, 10);
                    mMissingImage.setId(234873453);
                    imageView = mMissingImage;
                }
            } catch (InvalidReferenceException e) {
                Log.e(t, "image invalid reference exception");
                e.printStackTrace();
            }
        }

        if(imageView != null) {
            RelativeLayout parent = this;
            imageParams.addRule(RelativeLayout.BELOW, topPane.getId());
            if (mAudioButton != null) {
                if (!textVisible) {
                    imageParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
                    parent = topPane;
                }
            }
            if (mVideoButton != null) {
                if (!textVisible) {
                    imageParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
                    parent = topPane;
                }
            }
            parent.addView(imageView, imageParams);
        }
    }

    /**
     * @return The appropriate max size of an image view pane in this widget. returned as an int
     * array of [width, height]
     */
    private int[] getMaxImageViewBounds() {
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        int maxWidth = metrics.widthPixels;
        int maxHeight = metrics.heightPixels;

        // subtract height for textviewa and buttons, if present
        if(mView_Text != null){
            maxHeight = maxHeight - mView_Text.getHeight();
        } if(mVideoButton != null){
            maxHeight = maxHeight - mVideoButton.getHeight();
        } else if(mAudioButton != null){
            maxHeight = maxHeight - mAudioButton.getHeight();
        }

        // reduce by third for safety
        return new int[] {maxWidth, (2 * maxHeight)/3};
    }

    /**
     * This adds a divider at the bottom of this layout. Used to separate
     * fields in lists.
     */
    public void addDivider(ImageView v) {
        RelativeLayout.LayoutParams dividerParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (mImageView != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mImageView.getId());
        } else if (mMissingImage != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mMissingImage.getId());
        } else if (mVideoButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mVideoButton.getId());
        } else if (mAudioButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
        } else if (mView_Text != null) {
            // No picture
            dividerParams.addRule(RelativeLayout.BELOW, mView_Text.getId());
        } else {
            Log.e(t, "Tried to add divider to uninitialized ATVWidget");
            return;
        }
        addView(v, dividerParams);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE) {
            if (mAudioButton != null) {
                mAudioButton.endPlaying();
            }
        }
    }
}
