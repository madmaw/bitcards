package com.darklanders.bitcards.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.darklanders.bitcards.android.engine.Engine;
import com.darklanders.bitcards.android.engine.device.ClientApiAdapter;
import com.darklanders.bitcards.android.engine.device.socket.SocketDeviceClientAdapterStub;
import com.darklanders.bitcards.android.engine.device.socket.SocketDeviceSource;
import com.darklanders.bitcards.common.Instruction;
import com.journeyapps.barcodescanner.BarcodeView;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Chris on 23/04/2016.
 */
public class BitCardsActivity extends Activity {

    private static final String PREFS_ID = "bitcards";
    private static final String INSTALL_ID_KEY = "installId";

    private ClientView clientView;
    private ClientApi clientApi;

    private SocketDeviceSource deviceSource;
    private SocketDeviceClientAdapterStub deviceClientAdapter;

    private Engine engine;

    private String installId;

    private BarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        /*
        barcodeView = (BarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(new BarcodeCallback(){
            @Override
            public void barcodeResult(BarcodeResult barcodeResult) {

            }

            @Override
            public void possibleResultPoints(List<ResultPoint> list) {

            }
        });
        */

        this.clientView = new ClientView(this);
        this.deviceSource = new SocketDeviceSource();

        this.engine = new Engine(this.deviceSource);

        String defaultInstallId = UUID.randomUUID().toString();
        this.installId = getSharedPreferences(PREFS_ID, 0).getString(INSTALL_ID_KEY, defaultInstallId);
        if( this.installId.equals(defaultInstallId) ) {
            getSharedPreferences(PREFS_ID, 0).edit().putString(INSTALL_ID_KEY, defaultInstallId).commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.deviceSource.start();

            // create the device
            this.deviceClientAdapter = new SocketDeviceClientAdapterStub(this, this.installId);
            ClientApi localClientApi = new ClientApiAdapter(deviceClientAdapter);


            this.clientApi = localClientApi;

            this.clientView.start(localClientApi);
            this.clientView.setInstruction(Instruction.Disconnected);


            this.deviceClientAdapter.start();


        } catch( Exception ex ) {
            Log.e(BitCardsActivity.class.getSimpleName(), "unable to start socket", ex);
        }

    }

    @Override
    protected void onPause() {

        this.clientView.stop();
        this.clientApi.close();
        try {
            this.deviceSource.stop();
        } catch( IOException ex ) {
            Log.e(BitCardsActivity.class.getSimpleName(), "unable to stop socket", ex);
        }
        this.deviceClientAdapter.stop();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
