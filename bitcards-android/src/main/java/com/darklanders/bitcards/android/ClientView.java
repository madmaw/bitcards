package com.darklanders.bitcards.android;

import android.app.Activity;
import android.hardware.*;
import android.util.Log;
import android.view.View;
import com.darklanders.bitcards.common.Instruction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.*;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chris on 25/04/2016.
 */
public class ClientView {

    private static final int[] TRANSIENT_VIEWS = {
            R.id.scan_draw,
            R.id.scan_ok,
            R.id.scan_play,
            R.id.scan_game,
            R.id.scan_receive,
            R.id.disconnected,
            R.id.user_error,
            R.id.hold
    };

    private class ClientViewFlasher extends Thread {

        private boolean running = true;
        private boolean viewVisible = true;

        @Override
        public void run() {
            while( this.running ) {
                long started = System.currentTimeMillis();
                long sleep;
                synchronized (ClientView.this) {
                    sleep = ClientView.this.sequence[ClientView.this.index % ClientView.this.sequence.length];
                    if( sleep > 0) {
                        final boolean on = ClientView.this.index % 2 == 0;
                        ClientView.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if( !viewVisible ^ on ) {
                                    barcodeView.setTorch(on);
                                    if( sequenceViewId != null ) {
                                        View view = ClientView.this.activity.findViewById(sequenceViewId);
                                        if( on ) {
                                            view.setVisibility(View.VISIBLE);
                                        } else {
                                            view.setVisibility(View.GONE);
                                        }
                                    }
                                    viewVisible = !on;
                                }
                            }
                        });
                    }
                    ClientView.this.index++;
                }
                long now = System.currentTimeMillis();
                long sleepFor = started - now + sleep;
                if( sleepFor > 0 ) {
                    try {
                        Thread.sleep(sleepFor);
                    } catch( Exception ex ) {
                        // do nothing
                    }
                }
            }
        }

        public void cancel() {
            this.running = false;
        }
    }

    private ClientApi clientApi;
    private Activity activity;

    private long[] sequence;
    private Integer sequenceViewId;
    private int index;
    private ClientViewFlasher flasher;
    private BarcodeView barcodeView;
    private boolean faceDown;

    private ClientApiListener clientApiListener;

    private View.OnClickListener onClickListener;

    private SensorEventListener sensorEventListener;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private Instruction currentInstruction;
    private boolean started;
    private boolean showCamera;

    public ClientView(Activity activity){
        this.activity = activity;
        this.clientApiListener = new ClientApiListener() {
            @Override
            public void onNewInstruction(Instruction instruction) {
                setInstruction(instruction);
            }
        };
        this.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                touch();
            }
        };
        this.sequence = new long[]{0, 1000};

        this.barcodeView = (BarcodeView) this.activity.findViewById(R.id.barcode_scanner);
        HashMap<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
        hints.put(DecodeHintType.TRY_HARDER, null);
        this.barcodeView.setDecoderFactory(
                new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.QR_CODE), hints, "UTF-8")
        );

        this.barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult barcodeResult) {
                Log.i(getClass().getSimpleName(), barcodeResult.getText());
                ClientView.this.clientApi.scan(barcodeResult.getText());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> list) {

            }
        });
        this.sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[2];
                if( value > 7 && faceDown ) {
                    configureCamera(false, false);
                } else if( value < -7 && !faceDown ) {
                    configureCamera(true, false);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        this.sensorManager = (SensorManager)this.activity.getSystemService(Activity.SENSOR_SERVICE);
        List<Sensor> sensors = this.sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        if( !sensors.isEmpty() ) {
            this.accelerometerSensor = sensors.get(0);
        }
        configureCamera(this.faceDown, true);
    }

    public void configureCamera(boolean faceDown, boolean force) {

        if( this.faceDown != faceDown || force ) {
            this.faceDown = faceDown;
            CameraSettings cameraSettings = new CameraSettings();
            Integer cameraId = null;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ^ faceDown || cameraId == null) {
                    cameraId = camIdx;
                }
            }

            if( cameraId != null ) {
                cameraSettings.setRequestedCameraId(cameraId);
            }
            cameraSettings.setAutoTorchEnabled(false);

            if( this.started ) {
                this.barcodeView.pause();
            }
            this.barcodeView.setCameraSettings(cameraSettings);
            if( this.started ) {
                this.barcodeView.resume();
            }
        }

    }

    public void setInstruction(final Instruction instruction) {
        if( instruction != this.currentInstruction ) {
            final ArrayList<Integer> toShow = new ArrayList<Integer>();
            long[] sequence;
            Integer sequenceViewId = null;
            switch(instruction) {
                default:
                case Hold:
                    toShow.add(R.id.hold);
                    sequence = new long[]{0, 1000};
                    break;
                case ScanOk:
                    toShow.add(R.id.scan_ok);
                    sequence = new long[]{0, 1000};
                    break;
                case Draw:
                    toShow.add(R.id.scan_draw);
                    sequence = new long[]{1000, 0};
                    sequenceViewId = R.id.scan_draw;
                    break;
                case Play:
                    toShow.add(R.id.scan_play);
                    sequence = new long[]{500, 500};
                    sequenceViewId = R.id.scan_play;
                    break;
                case Disconnected:
                    toShow.add(R.id.disconnected);
                    sequence = new long[]{200, 2000};
                    break;
                case NoGame:
                    toShow.add(R.id.scan_game);
                    sequence = new long[]{2000, 200};
                    sequenceViewId = R.id.scan_game;
                    break;
                case InvalidMove:
                    toShow.add(R.id.user_error);
                    sequence = new long[]{10, 10};
                    break;
            }
            setSequence(sequence, sequenceViewId);
            this.currentInstruction = instruction;
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // just hide everything, the turn on the bits we want
                    for( int viewId : TRANSIENT_VIEWS ) {
                        View view = ClientView.this.activity.findViewById(viewId);
                        view.setVisibility(View.INVISIBLE);
                    }
                    for( int viewId : toShow ) {
                        View view = ClientView.this.activity.findViewById(viewId);
                        view.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void setSequence(long[] sequence, Integer sequenceViewId) {
        synchronized (this) {
            this.sequenceViewId = sequenceViewId;
            this.sequence = sequence;
            this.index = 0;
        }
    }

    public void touch() {
        this.clientApi.tap();
    }

    public void start(ClientApi clientApi) {
        this.clientApi = clientApi;
        this.clientApi.addClientApiListener(this.clientApiListener);
        setInstruction(this.clientApi.getLastInstruction());
        this.flasher = new ClientViewFlasher();
        this.flasher.start();
        View view = this.activity.findViewById(R.id.main);
        view.setOnClickListener(this.onClickListener);

        this.sensorManager.registerListener(this.sensorEventListener, this.accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        this.started = true;
        this.barcodeView.resume();

    }

    public void stop() {
        this.flasher.cancel();
        this.flasher = null;
        this.clientApi.removeClientApiListener(this.clientApiListener);

        this.sensorManager.unregisterListener(this.sensorEventListener, this.accelerometerSensor);

        this.barcodeView.pause();
        this.started = false;
    }

}
