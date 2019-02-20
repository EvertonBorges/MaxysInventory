package com.maxys.maxysinventory.secondaryActivities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.TipoRetornoIntent;

import me.dm7.barcodescanner.core.CameraUtils;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

public class BarCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView zXingScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zXingScannerView = new ZXingScannerView(this);
        setContentView(zXingScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        zXingScannerView.setResultHandler(this);

        zXingScannerView.setAutoFocus(true); // Foco automático?
        zXingScannerView.setFormats(listOf(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13));
        zXingScannerView.setBorderColor(Color.RED); // Mudar a cor dos traços da borda
        zXingScannerView.setLaserColor(Color.YELLOW); // Mudar cor do laser (faixa no meio da tela)
        //zXingScannerView.setMaskColor(Color.TRANSPARENT); // Para mudar cor de fundo.
        if( Build.MANUFACTURER.equals("HUAWEI") ){ // Marca HUAWEI necessita desse tratamento.
            zXingScannerView.setAspectTolerance(0.5f);
        }
        zXingScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        zXingScannerView.stopCamera();

        Camera camera = CameraUtils.getCameraInstance();
        if (camera != null) {
            camera.release();
        }
    }

    @Override
    public void handleResult(Result result) {
        zXingScannerView.resumeCameraPreview(this);
        if (result != null) {
            if (!result.getText().isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("barcodeResultado", result.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    // Descobre se a câmera possui o recurso de flash.
    private boolean isFlashSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void enableFlash(Context context, boolean status) {
        if (isFlashSupported(context)) {
            zXingScannerView.setFlash(status);
        }
    }

}