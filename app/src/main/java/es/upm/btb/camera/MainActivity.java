package es.upm.btb.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private TextView tvCharsCam, tvLightIntensity;
    private Button btnReadLight;

    CameraManager cameraManager;

    String cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLightIntensity = findViewById(R.id.tvLightIntensity);
        tvCharsCam = findViewById(R.id.tvChars);
        btnReadLight = findViewById(R.id.btnReadLight);
        btnReadLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readLightLevel();

            }
        });


        // Obtener el servicio del administrador de la cámara
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Obtener la ID de la cámara trasera
            cameraId = cameraManager.getCameraIdList()[0];
            // Obtener las características de la cámara
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            // Obtener la sensibilidad a la luz (ISO) máxima de la cámara
            Integer maxIso = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE).getUpper();


            // Actualizar la vista con la intensidad de luz máxima
            tvCharsCam.setText("Max Light Intensity [orientation]: " + maxIso + " ["+cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION).toString()+"]");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        checkCameraPermission();



    }


    private CameraDevice cameraDevice;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            // Llamar al método para calcular la cantidad de luz
            Log.d("LightIntensity", "Llamar al método para calcular la cantidad de luz.");
            calculateLightIntensity();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d("LightIntensity", "Cerrar cámara.");
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e("LightIntensity", "Algo ha fallado con error[" + error + "].");
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId;
        Log.d("LightIntensity", "Abriendo cámara...");


        try {
            cameraId = cameraManager.getCameraIdList()[0];
            Log.d("LightIntensity", " Leyendo la camara con identificador [" + cameraId + "].");

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void calculateLightIntensity() {
        if (cameraDevice == null) {
            // La cámara no está disponible
            Log.e("LightIntensity", "La cámara no está disponible.");
            return;
        }

        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());

            // Obtener el valor de la intensidad de luz
            float lightIntensity = characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);

            // Hacer algo con el valor de la intensidad de luz
            Log.d("LightIntensity", "Intensity: " + lightIntensity);
            tvLightIntensity.setText("Intensity: " + lightIntensity);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // No se ha concedido el permiso de la cámara, se solicita
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            // Se tienen los permisos de la cámara, se abre la cámara
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de cámara concedido, se abre la cámara
                openCamera();
            } else {
                // Permiso de cámara denegado, se muestra un mensaje o se toma alguna acción adicional
            }
        }
    }

    private void readLightLevel() {
        try {
            float lightIntensity = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)
                    .floatValue();

            tvLightIntensity.setText("Intensity: " + lightIntensity);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


}
