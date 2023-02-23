package com.example.tfp;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class snapActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;
    private TextureView mTextureView;
    private ImageView imgView;
    private ImageButton btnRecapture;
    private ImageButton btnScan;
    private ImageButton btnCopy;
    private TextView txtResult;
    private TextView txtScanned;
    private LinearLayout linearLayout;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };
    private int totalRotation;
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mImageSize;
    private ImageReader mImageReader;

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new snapActivity.ImageSaver(reader.acquireLatestImage()));
                }
            };
    private class ImageSaver implements Runnable{
        private final Image mImage;
        public ImageSaver(Image image){
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                mImage.close();
                if (fileOutputStream != null){
                    try {
                        fileOutputStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            Bitmap bitmap = BitmapFactory.decodeFile(mImageFileName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeCamera();
                    mTextureView.setVisibility(View.GONE);

                    try {
                        ExifInterface exif = new ExifInterface(mImageFileName);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        Matrix matrix = new Matrix();
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                matrix.postRotate(90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                matrix.postRotate(180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                matrix.postRotate(270);
                                break;
                        }

                        Bitmap rotatedImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        imgView.setImageBitmap(rotatedImage);

                        String fileName = mImageFileName.substring(0, mImageFileName.lastIndexOf(".")) + ".jpg";
                        File file = new File(fileName);

                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            rotatedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(getApplicationContext(), UcropperActivity.class);
                        intent.putExtra("ImageData", "file://" + file.getAbsolutePath());
                        startActivityForResult(intent, 100);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

    }
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallBack = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult captureResult){
            switch (mCaptureState){
                case STATE_PREVIEW:
                    //do nothing
                    break;
                case STATE_WAIT_LOCK:
                    mCaptureState = STATE_PREVIEW;
                    Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                        startStillCaptureRequest();
                    }
                    break;
            }
        }
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }
    };
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private ImageButton mStillImageButton;
    private ImageButton mGalleryImageButton;
    private File mImageFolder;
    private String mImageFileName;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    TextRecognizer textRecognizer;
    ActivityResultLauncher<String> cropImage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 101){
            String result = data.getStringExtra("CROP");
            Uri uri = data.getData();
            if (result!=null){
                uri = Uri.parse(result);
            }
            imgView.setImageURI(uri);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);

        createImageFolder();





        final CustomProgressDialog dialog = new CustomProgressDialog(snapActivity.this);

        cropImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
           Intent intent = new Intent(getApplicationContext(),UcropperActivity.class);
           intent.putExtra("ImageData",result.toString());
           mTextureView.setVisibility(View.GONE);
           startActivityForResult(intent,100);
        });


        mTextureView = (TextureView) findViewById(R.id.textureView);
        imgView = (ImageView) findViewById(R.id.imgScan);
        txtResult= (TextView) findViewById(R.id.txtResult);
        txtScanned= (TextView) findViewById(R.id.txtScanned);
        linearLayout = findViewById(R.id.LinearText);


        mGalleryImageButton = (ImageButton) findViewById(R.id.cameraGalleryButton);
        mGalleryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                mTextureView.setVisibility(View.GONE);
                mGalleryImageButton.setVisibility(View.GONE);
                mStillImageButton.setVisibility(View.GONE);
                imgView.setVisibility(View.VISIBLE);
                btnScan.setVisibility(View.VISIBLE);
                btnRecapture.setVisibility(View.VISIBLE);

                cropImage.launch("image/*");
            }
        });

        btnScan = (ImageButton) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgView.setVisibility(View.GONE);
                txtResult.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);

                Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
                InputImage image = InputImage.fromBitmap(bitmap,0);

                dialog.setTitle("İşleniyor. Lütfen bekleyiniz.");
                dialog.show();

                textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                Task<Text> result = textRecognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {

                                        List<Text.TextBlock> blocks = visionText.getTextBlocks();
                                        StringBuilder resultText = new StringBuilder();
                                        for(Text.TextBlock block : blocks){
                                            resultText.append(block.getText());
                                            resultText.append("\n");
                                        }
                                        txtResult.setText(resultText.toString());
                                        dialog.cancel();
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });
            }

        });

        mStillImageButton = (ImageButton) findViewById(R.id.cameraImageButton);
        mStillImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 5000);
                lockFocus();
                mStillImageButton.setVisibility(View.GONE);
                mGalleryImageButton.setVisibility(View.GONE);
                btnRecapture.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.VISIBLE);
                btnScan.setVisibility(View.VISIBLE);

            }
        });
        btnRecapture = (ImageButton) findViewById(R.id.btnRecapture);
        btnRecapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStillImageButton.setVisibility(View.VISIBLE);
                mGalleryImageButton.setVisibility(View.VISIBLE);
                btnRecapture.setVisibility(View.GONE);
                mTextureView.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.GONE);
                btnScan.setVisibility(View.GONE);
                btnCopy.setVisibility(View.GONE);
                txtResult.setVisibility(View.GONE);
                txtResult.setText(null);
                connectCamera();
            }
        });
        btnCopy = (ImageButton) findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = txtResult.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied to clipboard.", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied to clipboard.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Application will not run without camera services.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundThread();
        closeCamera();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),rotatedWidth,rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(),ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId,mCameraDeviceStateCallBack,mBackgroundHandler);
                }
                else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(this, "Video app required access to camera.", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }else{
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
            }

        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    private void startPreview(){
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface,mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewCaptureSession = session;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null,mBackgroundHandler);
                            }catch (CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Unable to setup camera preview.", Toast.LENGTH_SHORT).show();

                        }
                    },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest(){
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,totalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    createImageFileName();

                }
            };
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(),stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeCamera(){
        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
    private void startBackgroundThread(){
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }
    private void stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height){
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices){
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height){
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0){
            return Collections.min(bigEnough,new snapActivity.CompareSizeByArea());
        }else{
            return choices[0];
        }
    }
    private void createImageFolder(){
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile,"Visionear");
        if (!mImageFolder.exists()){
            mImageFolder.mkdirs();
        }
    }
    private File createImageFileName(){
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = null;
        try {
            imageFile = File.createTempFile(prepend,".jpg",mImageFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }
    private void lockFocus(){
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(),mPreviewCaptureCallBack,mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
}