package org.pycontw.ccip.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.pycontw.ccip.R;
import org.pycontw.ccip.util.PreferenceUtil;

import java.io.IOException;

public class CaptureActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private CaptureManager capture;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        rootView = findViewById(android.R.id.content);

        DecoratedBarcodeView barcodeView = findViewById(R.id.barcode_view);

        capture = new CaptureManager(this, barcodeView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    public void selectFromGallery(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap;
            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();

            try {
                Result result = reader.decode(binaryBitmap);

                if (result != null && result.getText() != null) {
                    Intent intent = new Intent();
                    intent.putExtra(Intents.Scan.RESULT, result.getText());

                    setResult(RESULT_OK, intent);
                    finish();
                }
            } catch (NotFoundException e) {
                Snackbar.make(rootView, R.string.no_qr_code_found, Snackbar.LENGTH_SHORT).show();
            } catch (ChecksumException | FormatException e) {
                e.printStackTrace();
            }
        }
    }
}
