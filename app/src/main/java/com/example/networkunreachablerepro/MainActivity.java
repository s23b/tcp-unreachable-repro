package com.example.networkunreachablerepro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Network;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.StrictMode;
import android.widget.TextView;
import java.net.Socket;

import gotcp.Gotcp;

public class MainActivity extends AppCompatActivity {
    public final static String SSID = "<SSID>";
    public final static String WIFI_PASS = "<WIFI_PASS>";
    public final static String HOST = "192.168.0.1";
    public final static int PORT = 80;

    private ConnectivityManager connectivityManager;
    private TextView textView;

    static {
        System.loadLibrary("networkunreachablerepro");
    }

    private native void cDial(String host, int port);

    private void updateText(String text) {
        runOnUiThread(() -> textView.setText(text));
        Log.e("NetUnreach", text);
    }

    interface TestBody {
        public void run() throws Exception;
    }

    // Run the test body and print out the debug messages
    private void runTest(String name, TestBody body) {
        try {
            updateText(name + " - Dialing");
            body.run();
            updateText(name + " - Dial Successful");
        } catch (Exception e) {
            Log.e("NetUnreach", name + " - Dial failed", e);
            updateText(name + " - " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textView = findViewById(R.id.dialError);

        this.connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Allow network operations on the UI thread
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }

    public void onWifiConnect(View button) {
        try {
            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier
                    .Builder()
                    .setSsid(SSID)
                    .setWpa2Passphrase(WIFI_PASS)
                    .build();


            NetworkRequest networkRequest = new NetworkRequest
                    .Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(specifier)
                    .build();

            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                    updateText ("Connection - Available " + network.toString());
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    updateText("Connection - Unavailable");
                }
            });
        } catch (Exception e) {
            Log.e("NetUnreach", "Wifi connect failed", e);
        }
    }

    public void onJavaDialClick(View button) {
        runTest("Java", () -> new Socket(HOST, PORT).close());
    }

    public void onCDialClick(View button) {
        runTest("C", () -> cDial(HOST, PORT));
    }

    public void onGoDialClick(View button) {
        runTest("Go net", () -> Gotcp.netDial(HOST, PORT));
    }

    public void onCGoDialClick(View button) {
        runTest("Go cgo", () -> Gotcp.cGoDial(HOST, PORT));
    }

    public void onSyscallDialClick(View button) {
        runTest("Go syscall", () -> Gotcp.syscallDial(HOST, PORT));
    }
}