package com.facechaos.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import java.util.Collections;
import java.util.function.Consumer;

/** Lifecycle-owned Camera2 preview, without copying preview frames into Java. */
final class SelfieCamera implements TextureView.SurfaceTextureListener {
    private final Activity activity;
    private final TextureView view;
    private final Consumer<String> status;
    private HandlerThread worker;
    private Handler handler;
    private CameraDevice device;
    private CameraCaptureSession session;
    private Surface surface;
    private Size size;
    private CameraCharacteristics characteristics;
    private boolean active, opening;
    private int generation, sensorRotation;
    private long lastFrame;
    boolean hasFreshFrame(){return active&&lastFrame!=0&&android.os.SystemClock.elapsedRealtime()-lastFrame<1500;}
    boolean hasFreshAnalysisFrame(){return active&&lastFrame!=0&&android.os.SystemClock.elapsedRealtime()-lastFrame<350;}
    SelfieCamera(Activity activity, TextureView view, Consumer<String> status) {
        this.activity = activity; this.view = view; this.status = status;
        view.setSurfaceTextureListener(this);
    }
    void start() {
        if (active) { if (view.isAvailable()) open(); return; }
        active = true; lastFrame=0; generation++;
        worker = new HandlerThread("FaceChaosCamera"); worker.start(); handler = new Handler(worker.getLooper());
        if (view.isAvailable()) open();
    }
    private void open() {
        if (!active || opening || device != null || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        CameraManager manager = activity.getSystemService(CameraManager.class);
        try {
            String front = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics info = manager.getCameraCharacteristics(id);
                Integer facing = info.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) { front = id; break; }
            }
            if (front == null) { status.accept("No front camera found."); return; }
            CameraCharacteristics info = manager.getCameraCharacteristics(front);
            characteristics=info;
            size=CameraQuality.preview(info);
            sensorRotation = info.get(CameraCharacteristics.SENSOR_ORIENTATION);
            configureTransform();
            opening = true;
            final int token = generation;
            manager.openCamera(front, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice camera) {
                    // Callbacks run on the UI thread, serializing lifecycle transitions.
                    if (!active || token != generation) { camera.close(); return; }
                    opening = false;
                    device = camera; connect(token);
                }
                @Override public void onDisconnected(CameraDevice camera) { camera.close(); if (token == generation) { device = null; opening = false; lastFrame=0; status.accept("Camera disconnected. Tap Enable camera to reconnect."); } }
                @Override public void onError(CameraDevice camera, int error) { camera.close(); if (token == generation) { device = null; opening = false; lastFrame=0; status.accept("Camera unavailable. Close other camera apps and retry."); } }
            }, new Handler(activity.getMainLooper()));
        } catch (Exception error) { opening = false; status.accept("Camera could not open. Tap Enable camera to retry."); }
    }
    private void connect(int token) {
        try {
            SurfaceTexture texture = view.getSurfaceTexture();
            if (texture == null) return;
            texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            surface = new Surface(texture);
            device.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession configured) {
                    if (!active || generation != token || device == null) { configured.close(); return; }
                    session = configured;
                    try {
                        CaptureRequest.Builder request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        request.addTarget(surface);
                        CameraQuality.apply(request,characteristics);
                        session.setRepeatingRequest(request.build(), null, handler);
                        status.accept("");
                    } catch (CameraAccessException | IllegalStateException error) { status.accept("Preview interrupted. Pause and reopen the app to retry."); }
                }
                @Override public void onConfigureFailed(CameraCaptureSession failed) { failed.close(); status.accept("Camera preview unavailable. Tap Enable camera to retry."); }
            }, new Handler(activity.getMainLooper()));
        } catch (Exception error) { status.accept("Camera preview could not start. Tap Enable camera to retry."); }
    }
    private void configureTransform() {
        if (size == null || view.getWidth() == 0 || view.getHeight() == 0) return;
        float w = view.getWidth(), h = view.getHeight();
        // TextureView already compensates sensor orientation (and the front-camera
        // preview mirror). Applying sensor rotation again turns the selfie sideways.
        // Undo its stretch in the naturally oriented coordinates, compensate only
        // display rotation, then center-crop uniformly. Do not mirror a second time.
        int display = activity.getWindowManager().getDefaultDisplay().getRotation() * 90;
        PreviewGeometry geometry = PreviewGeometry.calculate(size.getWidth(), size.getHeight(), w, h, sensorRotation, display);
        Matrix transform = new Matrix();
        transform.setScale(geometry.naturalWidth / w, geometry.naturalHeight / h);
        transform.postTranslate(-geometry.naturalWidth / 2f, -geometry.naturalHeight / 2f);
        transform.postRotate(geometry.displayCorrection);
        transform.postScale(geometry.zoom, geometry.zoom);
        transform.postTranslate(w / 2f, h / 2f);
        view.setTransform(transform);
    }
    void stop() {
        active = false; lastFrame=0; generation++; opening = false;
        if (session != null) { session.close(); session = null; }
        if (device != null) { device.close(); device = null; }
        if (surface != null) { surface.release(); surface = null; }
        if (worker != null) { worker.quitSafely(); worker = null; handler = null; }
    }
    @Override public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) { open(); }
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) { configureTransform(); }
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) { stop(); return true; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture texture) { lastFrame=android.os.SystemClock.elapsedRealtime(); }
}
