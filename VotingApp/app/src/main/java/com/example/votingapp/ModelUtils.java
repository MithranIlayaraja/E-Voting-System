package com.example.votingapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelUtils {

    public static MappedByteBuffer loadModelFile(Context context, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static Bitmap downloadBitmap(String urlString) throws IOException {
        URL url = new URL(urlString);
        return BitmapFactory.decodeStream(url.openConnection().getInputStream());
    }

    public static float[] getFaceEmbedding(Bitmap bitmap, Interpreter tfLite) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 160, 160, true);
        float[][][][] input = new float[1][160][160][3];
        for (int y = 0; y < 160; y++) {
            for (int x = 0; x < 160; x++) {
                int pixel = bitmap.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }

        float[][] embeddings = new float[1][128];
        tfLite.run(input, embeddings);
        return embeddings[0];
    }

    public static float calculateDistance(float[] emb1, float[] emb2) {
        float sum = 0f;
        for (int i = 0; i < emb1.length; i++) {
            float diff = emb1[i] - emb2[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }
}
