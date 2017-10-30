package com.example.angiopasqui.permissionchecker.privacyLeaks;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Notification;

import com.example.angiopasqui.permissionchecker.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by passet on 19/10/2017.
 */

public class LocalVpnService extends VpnService implements Handler.Callback {

    private static final String VPN_ADDRESS = "192.168.0.1";
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static String TAG = "DEBUG";
    private Thread mThread;
    private ParcelFileDescriptor vpnInterface = null;
    private PendingIntent pendingIntent;
    private static LocalVpnService instance;
    private Handler mHandler;
    Builder builder = new Builder();
    static SharedPreferences preferences ;
    static SharedPreferences.Editor editor ;

    @Override
    public void onCreate() {
        if(GlobalState.VPN_ENABLED) {
            super.onCreate();
            /*if (mHandler == null) {        // The handler is only used to show messages.
                mHandler = new Handler(this);
            }*/

            Log.d(TAG, "onCreate Servizio VPN");

            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LocalVpnService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        /*if (this.mThread != null) {                                             //Se il thread è già avviato, viene fermato
            this.mThread.interrupt();
        }
        this.mThread = new Thread(this, "Privacy checker VPN");                 //Creazione nuovo thread
        this.mThread.setPriority(10);
        this.mThread.start();*/

    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Servizio VPN");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.putString("vpn abilitata","si");
        editor.commit();

        if(GlobalState.VPN_ENABLED) {
            if (mHandler == null) {        // The handler is only used to show messages.
                mHandler = new Handler(this);
            }

            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //a. Configure the TUN and get the interface.
                        Log.d("DEBUG", "runThread");

                        DatagramChannel tunnel = DatagramChannel.open();
                        protect(tunnel.socket());

                        tunnel.connect(new InetSocketAddress("10.0.0.1", 55555));

                        tunnel.configureBlocking(false);

                        Log.d(TAG,"indrizzo ip :"+getIPAddress(true));
                        builder.addAddress(getIPAddress(true),24);

                        //builder.addAddress("10.0.8.1", 32);                              //Configurazione VPN
                        builder.addRoute(VPN_ROUTE, 0);
                        builder.setMtu(16000);                                              //Metodo che permette di settare il numero massimo di trasmissioni sull'interfaccia VPN --> 16000

                        //builder.addDnsServer("8.8.8.8");
                        vpnInterface = builder.setSession(getString(R.string.app_name)).establish();                //Lancio VPN

                        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
                        ByteBuffer packet = ByteBuffer.allocate(32767);


                        FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

                        int timer = 0;
                        Log.d(TAG, "tunnel open:" + tunnel.isOpen() + " connected:" + tunnel.isConnected());

                        int length;
                        String destIP;


                        //e. Use a loop to pass packets.
                        while (true) {
                            //get packet with in
                            //put packet to tunnel
                            //get packet form tunnel
                            //return packet with out

                          /*  //  PRIMA SOLUZIONE
                            // Assume that we did not make any progress in this iteration.
                            boolean idle = true;
                            // Read the outgoing packet from the input stream.
                            int length = 0;
                            try {
                                length = in.read(packet.array());

                                if (length > 0) {

                                    Log.d(TAG, "got outgoing packet; length=" + length);
                                    TCP_IP TCP_debug = new TCP_IP(packet);
                                    TCP_debug.debug();
                                    destIP = TCP_debug.getDestination();

                                    InetAddress address = InetAddress.getByName(destIP);

                                    System.out.println("host " + address.getHostAddress());
                                    System.out.println("host name " + address.getHostName());

                                    // Write the outgoing packet to the tunnel.
                                    packet.limit(length);
                                    tunnel.write(packet);
                                    packet.clear();

                                    // There might be more outgoing packets.
                                    idle = false;

                                    // If we were receiving, switch to sending.
                                    if (timer < 1) {
                                        timer = 1;
                                    }
                                }

                                // Read the incoming packet from the mTunnel.
                                length = tunnel.read(packet);
                                if (length > 0) {

                                    Log.d(TAG, "got inbound packet; length=" + length);
                                    // Write the incoming packet to the output stream.

                                    out.write(packet.array(), 0, length);
                                    packet.clear();

                                    // There might be more incoming packets.
                                    idle = false;

                                    // If we were sending, switch to receiving.
                                    if (timer > 0) {
                                        timer = 0;
                                    }
                                }

                                // If we are idle or waiting for the network, sleep for a
                                // fraction of time to avoid busy looping.
                                if (idle) {
                                    Thread.sleep(100);

                                    // Increase the timer. This is inaccurate but good enough,
                                    // since everything is operated in non-blocking mode.
                                    timer += (timer > 0) ? 100 : -100;

                                    // We are receiving for a long time but not sending.
                                    if (timer < -15000) {
                                        // Switch to sending.
                                        timer = 1;
                                    }

                                    // We are sending for a long time but not receiving.
                                    if (timer > 20000) {
                                        //throw new IllegalStateException("Timed out");
                                        //Log.d(TAG,"receiving timed out? timer=" + timer);
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            */


                            //SECONDA SOLUZIONE
                            while ((length = in.read(packet.array())) > 0) {


                                packet.limit(length);
                                Log.d(TAG, "Total Length:" + tunnel.socket().getInetAddress());

                                tunnel.write(packet);
                                packet.flip();

                                TCP_IP TCP_debug = new TCP_IP(packet);
                                TCP_debug.debug();
                                destIP = TCP_debug.getDestination();

                                InetAddress address = InetAddress.getByName(destIP);
                                System.out.println("host " + address.getHostAddress());
                                System.out.println("host name " + address.getHostName());


                                out.write(packet.array(), 0, length);
                                packet.clear();

                                //Thread.sleep(100);
                            }


                            //sleep is a must
                            //Log.d("DEBUG","SLEEEP");
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (vpnInterface != null) {       //All'uscita del while, se la vpnInterface esiste, si mette a null
                                closeInterface();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, "MyVpnRunnable");

            instance = this;

            mThread.start();
            mHandler.sendEmptyMessage(R.string.connected);

            return START_STICKY;
        }
        return START_NOT_STICKY;

    }

    public void closeInterface()  {
        try {
            vpnInterface.close();
            Log.d("DEBUG","CHIUSURA INTERFACCIA");
            vpnInterface = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //METODO CHE RESTITUISCE L'ISTANZA DELL'GGETTO LocalVpn
    public static LocalVpnService getInstance() {

        return instance;
    }

    public void stopVPN(){
        Log.d("DEBUG","stopVPN");

        closeInterface();
        stopForeground(true);                //Elimina la notifica
        GlobalState.VPN_ENABLED = false;
        onDestroy();
    }



    @Override
    public boolean handleMessage(Message msg) {
        Toast.makeText(this, msg.what, Toast.LENGTH_SHORT).show();
        updateForegroundNotification(msg.what);
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d("DEBUG","onDestroy VPN");
        editor.putString("vpn abilitata","no");
        editor.commit();
        if(mThread!=null){
            mThread.interrupt();

        }
        super.onDestroy();
    }


    private void updateForegroundNotification(final int message) {
        Log.d("DEBUG","updateForeground");
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("VPN STATUS MONITOR");
        builder.setSmallIcon(R.drawable.shieldicon);
        builder.setContentText(getString(message));
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        Notification notify = builder.build();

        startForeground(1, notify);
    }
}


