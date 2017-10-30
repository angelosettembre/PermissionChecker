package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.app.NotificationManager;
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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by passet on 19/10/2017.
 */

public class LocalVpnServiceTest extends VpnService implements Handler.Callback {

    private static final String VPN_ADDRESS = "192.168.0.1";
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static String TAG = "DEBUG";
    private Thread mThread;
    private ParcelFileDescriptor vpnInterface = null;
    private PendingIntent pendingIntent;
    private static LocalVpnServiceTest instance;
    private Handler mHandler;
    Builder builder = new Builder();
    static SharedPreferences preferences ;
    static SharedPreferences.Editor editor ;


    private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    private Selector udpSelector;
    private Selector tcpSelector;


    @Override
    public void onCreate() {
        if(GlobalState.VPN_ENABLED) {
            super.onCreate();

            Log.d(TAG, "onCreate Servizio VPN");

            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LocalVpnService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        }
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
            instance = this;

            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //a. Configure the TUN and get the interface.
                        Log.d("DEBUG", "runThread");

                        builder.addAddress("10.0.8.1", 24);                               //Configurazione VPN
                        builder.addRoute(VPN_ROUTE, 0);
                        builder.setMtu(16000);                                              //Metodo che permette di settare il numero massimo di trasmissioni sull'interfaccia VPN --> 16000
                        //builder.addDnsServer("8.8.8.8");
                        vpnInterface = builder.setSession(getString(R.string.app_name)).establish();                //Lancio VPN



                        udpSelector = Selector.open();
                        tcpSelector = Selector.open();
                        deviceToNetworkUDPQueue = new ConcurrentLinkedQueue<>();
                        deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<>();
                        networkToDeviceQueue = new ConcurrentLinkedQueue<>();

                        executorService = Executors.newFixedThreadPool(5);
                        executorService.submit(new UDPInput(networkToDeviceQueue, udpSelector));
                        executorService.submit(new UDPOutput(deviceToNetworkUDPQueue, udpSelector, LocalVpnServiceTest.getInstance()));
                        executorService.submit(new TCPInput(networkToDeviceQueue, tcpSelector));
                        executorService.submit(new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, LocalVpnServiceTest.getInstance()));
                        executorService.submit(new VPNRunnable(vpnInterface.getFileDescriptor(),                //Creazione thread
                                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "MyVpnRunnable");


            mThread.start();
            mHandler.sendEmptyMessage(R.string.connected);

            return START_STICKY;
        }
        return START_NOT_STICKY;

    }

    private static class VPNRunnable implements Runnable {

        private static final String TAG = VPNRunnable.class.getSimpleName();

        private FileDescriptor vpnFileDescriptor;

        private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
        private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

        public VPNRunnable(FileDescriptor vpnFileDescriptor, ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue,
                           ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue,
                           ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue) {

            this.vpnFileDescriptor = vpnFileDescriptor;
            this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
            this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
            this.networkToDeviceQueue = networkToDeviceQueue;
        }

        @Override
        public void run()
        {
            Log.i(TAG, "VPNRunnable Started");

            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

            try
            {
                ByteBuffer bufferToNetwork = null;
                boolean dataSent = true;
                boolean dataReceived;
                while (!Thread.interrupted())
                {
                    if (dataSent)
                        bufferToNetwork = ByteBufferPool.acquire();    //Restituisce dimensione buffer
                    else
                        bufferToNetwork.clear();

                    // TODO: Block when not connected
                    int readBytes = vpnInput.read(bufferToNetwork);      //Lettura dall'interfaccia vpn
                    if (readBytes > 0) {
                        Log.d(TAG,"readbytes >0");
                        dataSent = true;
                        bufferToNetwork.flip();

                        //TCP_IP TCP_debug = new TCP_IP(bufferToNetwork);
                        //TCP_debug.debug();
                        //String destIP = TCP_debug.getDestination();
                        Packet packet = new Packet(bufferToNetwork);

                        /*try{
                            InetAddress address = InetAddress.getByName(destIP);
                            System.out.println("host " + address.getHostAddress());
                            System.out.println("host name " + address.getHostName());
                        }catch (UnknownHostException e){
                            e.printStackTrace();
                            continue;
                        }*/

                        Log.d(TAG,"PACCHETTO COMPLETO"+packet.toString());
                        Log.d(TAG,"Source address :"+packet.ip4Header.sourceAddress.toString()+" Destination address "+packet.ip4Header.destinationAddress.toString());
                        if (packet.isUDP())
                        {

                            deviceToNetworkUDPQueue.offer(packet);

                        }
                        else if (packet.isTCP())
                        {

                            deviceToNetworkTCPQueue.offer(packet);
                        }
                        else
                        {
                            Log.d(TAG, "Unknown packet type");
                            Log.d(TAG, packet.ip4Header.toString());
                            dataSent = false;
                        }

                    }
                    else
                    {
                        dataSent = false;
                    }

                    ByteBuffer bufferFromNetwork = networkToDeviceQueue.poll();
                    if (bufferFromNetwork != null)
                    {
                        bufferFromNetwork.flip();
                        while (bufferFromNetwork.hasRemaining()) {
                                vpnOutput.write(bufferFromNetwork);
                        }
                        dataReceived = true;

                        ByteBufferPool.release(bufferFromNetwork);
                    }
                    else
                    {
                        dataReceived = false;
                    }

                    // TODO: Sleep-looping is not very battery-friendly, consider blocking instead
                    // Confirm if throughput with ConcurrentQueue is really higher compared to BlockingQueue
                    if (!dataSent && !dataReceived)
                        Thread.sleep(10);
                }
            }
            catch (InterruptedException e)
            {
                Log.i(TAG, "Stopping");
            }
            catch (IOException e)
            {
                Log.w(TAG, e.toString(), e);
            }
            finally
            {
                closeResources(vpnInput, vpnOutput);
            }
        }
    }

    private static void closeResources(Closeable... resources)
    {
        for (Closeable resource : resources)
        {
            try
            {
                resource.close();
            }
            catch (IOException e)
            {
                // Ignore
            }
        }
    }

    //METODO CHE RESTITUISCE L'ISTANZA DELL'GGETTO LocalVpn
    public static LocalVpnServiceTest getInstance() {
        return instance;
    }

    public void stopVPN(){
        Log.d("DEBUG","stopVPN");

        editor.putString("vpn abilitata","no");
        editor.commit();

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
        if(mThread!=null){
            mThread.interrupt();
            executorService.shutdownNow();
            cleanup();
        }
        super.onDestroy();
    }

    private void cleanup()
    {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        ByteBufferPool.clear();
        closeResources(udpSelector, tcpSelector, vpnInterface);
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


