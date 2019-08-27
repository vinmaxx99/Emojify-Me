package com.deepanshu.vinamra.emojifyme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


public class Emojifier {

    private static final double SMILING_PROB_THRESHOLD= .10;
    private static final double EYE_OPEN_THRESHOLD= .5;
    private static final float EMOJI_SCALE_FACTOR= .9f;
    //private HashMap<String,Emoji> mp=;
   // private Map<String,Emoji> map=new HashMap<String, Emoji>();


    static Bitmap detectfaces(Context context, Bitmap bitmap){
        Timber.d(" timber start building DETECTOR");
        FaceDetector detector=new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
//        detector.setProcessor(
//                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
//                        .build());
        Timber.d(" timber END building DETECTOR");
        Bitmap resultBitmap = bitmap;
        if(detector.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            Timber.d(" timber START DETECTING FACES DETECTOR");
            SparseArray<Face> faces = detector.detect(frame);
            Timber.d(" timber END DETECTING FACES DETECTOR");


            Timber.d("size of faces" + faces.size());
            //  Toast.makeText(context,"number of faces detected = "+faces.size(),Toast.LENGTH_LONG).show();
            if (faces.size() == 0) {
                Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();
            } else {
                for (int i = 0; i < faces.size(); i++) {
                    Face face = faces.valueAt(i);
                    // getProbability(face);
                    Emoji emo = whichEmoji(face);
                    Bitmap emojibitmap;
                    switch (emo) {
                        case SMILE:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                            break;

                        case RIGHT_WINK:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
                            break;

                        case LEFT_WINK:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
                            break;

                        case CLOSED_EYE_SMILE:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
                            break;

                        case FROWN:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
                            break;

                        case LEFT_WINK_FROWN:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
                            break;

                        case RIGHT_WINK_FROWN:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
                            break;

                        case CLOSED_EYE_FROWN:
                            emojibitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
                            break;
                        default:
                            emojibitmap = null;
                            Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_LONG).show();
                    }

                    resultBitmap = addBitmapToFace(resultBitmap, emojibitmap, face);
                }
            }
        }else{
            Toast.makeText(context,"detector failed",Toast.LENGTH_SHORT).show();
        }
       detector.release();
       return resultBitmap;
    }

    private static Emoji whichEmoji(Face face) {


       // Log.d("smiling prob","smiling prob = "+face.getIsSmilingProbability());
       // Log.d("left eye open ","left eye = "+face.getIsLeftEyeOpenProbability());
       // Log.d("right eye open","right eye = "+face.getIsRightEyeOpenProbability());
        Timber.d("whichEmoji: smilingProb = " + face.getIsSmilingProbability());
        Timber.d("whichEmoji: leftEyeOpenProb = "
                + face.getIsLeftEyeOpenProbability());
        Timber.d("whichEmoji: rightEyeOpenProb = "
                + face.getIsRightEyeOpenProbability());

        boolean isSmiling= face.getIsSmilingProbability()>SMILING_PROB_THRESHOLD;
        boolean isLeftOpen= face.getIsLeftEyeOpenProbability()>EYE_OPEN_THRESHOLD;
        boolean isRightOpen= face.getIsRightEyeOpenProbability()>EYE_OPEN_THRESHOLD;

        Log.d("TAG VALUE","face parameters  " + isSmiling + " " +isLeftOpen+" "+ isRightOpen);
        Timber.d("hey timber");
        Timber.asTree();

        Emoji emoji;

        if(isSmiling){
            if(isLeftOpen){
                if(isRightOpen){
                    emoji=Emoji.SMILE;
                }else{
                    emoji=Emoji.RIGHT_WINK;
                }
            }else{
                if(isRightOpen) {
                    emoji = Emoji.LEFT_WINK;
                }else{
                    emoji = Emoji.CLOSED_EYE_SMILE;
                }
            }
        }else{
            if(isLeftOpen){
                if(isRightOpen){
                    emoji = Emoji.FROWN;
                }else{
                    emoji = Emoji.RIGHT_WINK_FROWN;
                }
            }else{
                if(isRightOpen){
                    emoji = Emoji.LEFT_WINK_FROWN;
                }else{
                    emoji = Emoji.CLOSED_EYE_FROWN;
                }
            }
        }


      //  Log.d("TAG VALUE " ,"face parameters " + emoji.name());
        return emoji;

    }

    private enum Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }



}
