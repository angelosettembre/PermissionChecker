package com.isislab.settembre.privacychecker.privacyLeaks;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import android.widget.Toast;

import com.isislab.settembre.privacychecker.R;


import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.IpPacket;

import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by passet on 19/10/2017.
 */

public class LocalVpnService extends VpnService implements Handler.Callback,DnsPacketProxy.EventLoop {

    private static final String VPN_ADDRESS = "192.168.0.1";
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static String TAG = "DEBUG";
    private Thread mThread;
    private ParcelFileDescriptor vpnInterface = null;
    private PendingIntent pendingIntent;
    private static LocalVpnService instance;
    private Handler mHandler;
    static SharedPreferences preferences ;
    static SharedPreferences.Editor editor ;
    static Notification.Builder builder;
    static NotificationManager notificationManager;


    /* If we had a successful connection for that long, reset retry timeout */
    private static final long RETRY_RESET_SEC = 60;
    private static final int MIN_RETRY_TIME = 5;
    private static final int MAX_RETRY_TIME = 2 * 60;
    private final VpnService vpnService = this;
    private final VpnWatchdog vpnWatchDog = new VpnWatchdog();
    private FileDescriptor mInterruptFd = null;
    private FileDescriptor mBlockFd = null;
    private final DnsPacketProxy dnsPacketProxy = new DnsPacketProxy(this);
    private final Queue<byte[]> deviceWrites = new LinkedList<>();
    private final WospList dnsIn = new WospList();
    private static final int DNS_MAXIMUM_WAITING = 1024;
    private static final long DNS_TIMEOUT_SEC = 10;
    public static Configuration config;

    private  ParcelFileDescriptor  fileDescriptor;
    final ArrayList<InetAddress> upstreamDnsServers = new ArrayList<>();
    public static ArrayList<AppLeak> listaAppLeaks;
    public static int countPacket;
    public static int countBlocked;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate Servizio VPN");

        listaAppLeaks = new ArrayList<AppLeak>();
        countPacket = 0;
        countBlocked = 0;

        if(GlobalState.VPN_ENABLED) {
            super.onCreate();

            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LocalVpnService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

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

            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dnsPacketProxy.initialize(vpnService, upstreamDnsServers);
                        vpnWatchDog.initialize(FileHelper.loadCurrentSettings(vpnService).watchDog);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }


                    int retryTimeout = MIN_RETRY_TIME;

                    while(true){
                        long connectTimeMillis = 0;
                        try {
                            connectTimeMillis = System.currentTimeMillis();

                            // If the function returns, that means it was interrupted
                            runVpn();                               //Chiamata metodo per eseguire la VPN

                            if (System.currentTimeMillis() - connectTimeMillis >= RETRY_RESET_SEC * 1000) {
                                Log.i(TAG, "Resetting timeout");
                                retryTimeout = MIN_RETRY_TIME;
                            }

                            try {
                                Thread.sleep((long) retryTimeout * 1000);
                            } catch (InterruptedException e) {
                                break;
                            }

                            if (retryTimeout < MAX_RETRY_TIME)
                                retryTimeout *= 2;

                        }catch (InterruptedException e){
                            e.printStackTrace();
                            break;

                        }catch (VpnNetworkException e){
                            Log.w(TAG, "Network exception in vpn thread, ignoring and reconnecting", e);

                        }catch (Exception e){
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

    private void runVpn() throws InterruptedException, ErrnoException, IOException, VpnNetworkException, PcapNativeException, TimeoutException, NotOpenException {
        Log.d("DEBUG","metodo runVpn LocalVpnService");
        // Allocate the buffer for a single packet.
        byte[] packet = new byte[32767];

        // A pipe we can interrupt the poll() call with by closing the interruptFd end
        FileDescriptor[] pipes = Os.pipe();
        mInterruptFd = pipes[0];
        mBlockFd = pipes[1];

        // Authenticate and configure the virtual network interface.

        // CHIAMATA AL METODO PER CONFIGURARE LA VPN
        try (ParcelFileDescriptor fd = configure()) {

            fileDescriptor = fd;
            // Read and write views of the tun device
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());    //Lettura inputStram
            FileOutputStream outFd = new FileOutputStream(fileDescriptor.getFileDescriptor());        //Lettura outputStream

            // We keep forwarding packets till something goes wrong.
            while (doOne(inputStream, outFd, packet)) ;    //Chiamata al metodo per il forwarding dei PACCHETTI
                                                            // si esce da questo ciclo quando si ferma la VPN

        } finally {
            mBlockFd = FileHelper.closeOrWarn(mBlockFd, TAG, "runVpn: Could not close blockFd");
        }
    }

    private ParcelFileDescriptor configure()throws VpnNetworkException{
        Log.d("DEBUG","metodo configure LocalVpnService");
        Log.d(TAG, "Configuring" + this);
        Configuration config = FileHelper.loadCurrentSettings(vpnService);  //Lettura settaggi dal file json

        // Get the current DNS servers before starting the VPN
        Set<InetAddress> dnsServers = getDnsServers(vpnService);          //Si ottengono i server
        Log.d(TAG, "Got DNS servers = " + dnsServers);

        // Configure a builder while parsing the parameters.
        VpnService.Builder builder = vpnService.new Builder();     //Creazione Builder VPN

        String format = null;

        // Determine a prefix we can use. These are all reserved prefixes for example
        // use, so it's possible they might be blocked.
        for (String prefix : new String[]{"192.0.2", "198.51.100", "203.0.113"}) {
            try {
                builder.addAddress(prefix + ".1", 24);    //Inserimento indirizzo
            } catch (IllegalArgumentException e) {
                continue;
            }

            format = prefix + ".%d";
            break;
        }

        Log.d("DEBUG","INDIRIZZO VPN: "+format);

        byte[] ipv6Template = new byte[]{32, 1, 13, (byte) (184 & 0xFF), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        if (format == null) {
            Log.d(TAG, "configure: Could not find a prefix to use, directly using DNS servers");
            builder.addAddress("192.168.50.1", 24);
        }

        // Add all knows DNS servers
        for (InetAddress addr : dnsServers) {
            try {
                newDNSServer(builder, format, ipv6Template, addr);    //Chiamata metodo newDNSServer
            } catch (Exception e) {
                Log.e(TAG, "configure: Cannot add server:", e);
            }
        }

        builder.setBlocking(true);

        // Allow applications to bypass the VPN
        builder.allowBypass();

        // Explictly allow both families, so we do not block
        // traffic for ones without DNS servers (issue 129).
        builder.allowFamily(OsConstants.AF_INET);
        builder.allowFamily(OsConstants.AF_INET6);


        // Create a new interface using the builder and save the parameters.
        // Creazione interfaccia
        ParcelFileDescriptor pfd = builder.setSession(getString(R.string.app_name)).establish();
        Log.i(TAG, "Configured");
        return pfd;
    }


    void newDNSServer(VpnService.Builder builder, String format, byte[] ipv6Template, InetAddress addr) throws UnknownHostException {
        Log.d("DEBUG","metodo newDNSServer AdVpnThread");

        // Optimally we'd allow either one, but the forwarder checks if upstream size is empty, so
        // we really need to acquire both an ipv6 and an ipv4 subnet.
        if (addr instanceof Inet6Address && ipv6Template == null) {                //Se l'indirizzo è IPV6
            Log.d(TAG, "newDNSServer: Ignoring DNS server " + addr);
        } else if (addr instanceof Inet4Address && format == null) {
            Log.d(TAG, "newDNSServer: Ignoring DNS server " + addr);
        } else if (addr instanceof Inet4Address) {                    //Se l'indirizzo è IPV4
            upstreamDnsServers.add(addr);                             //Inserisci indirizzo
            String alias = String.format(format, upstreamDnsServers.size() + 1);
            Log.d(TAG, "configure: Adding DNS Server " + addr + " as " + alias);
            builder.addDnsServer(alias);                            //Inserimento DNS Server nel Builder
            builder.addRoute(alias, 32);                            //Inserimento Route
            vpnWatchDog.setTarget(InetAddress.getByName(alias));
        } else if (addr instanceof Inet6Address) {                 //Se l'indirizzo è IPV6
            upstreamDnsServers.add(addr);
            ipv6Template[ipv6Template.length - 1] = (byte) (upstreamDnsServers.size() + 1);
            InetAddress i6addr = Inet6Address.getByAddress(ipv6Template);
            Log.d(TAG, "configure: Adding DNS Server " + addr + " as " + i6addr);
            builder.addDnsServer(i6addr);
            vpnWatchDog.setTarget(i6addr);
        }
    }

    private static Set<InetAddress> getDnsServers(Context context) throws VpnNetworkException {
        Log.d("DEBUG","metodo getDnsServers AdVpnThread");

        Set<InetAddress> out = new HashSet<>();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(VpnService.CONNECTIVITY_SERVICE);
        // Seriously, Android? Seriously?
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        if (activeInfo == null)
            throw new VpnNetworkException("No DNS Server");

        for (Network nw : cm.getAllNetworks()) {
            NetworkInfo ni = cm.getNetworkInfo(nw);
            if (ni == null || !ni.isConnected() || ni.getType() != activeInfo.getType()
                    || ni.getSubtype() != activeInfo.getSubtype())
                continue;
            for (InetAddress address : cm.getLinkProperties(nw).getDnsServers())
                out.add(address);
        }
        return out;
    }


    private boolean doOne(FileInputStream inputStream, FileOutputStream outFd, byte[] packet) throws IOException, ErrnoException, InterruptedException, VpnNetworkException, PcapNativeException, TimeoutException, NotOpenException {
        Log.d("DEBUG","metodo doOne LocalVpnService");

        StructPollfd deviceFd = new StructPollfd();
        deviceFd.fd = inputStream.getFD();
        deviceFd.events = (short) OsConstants.POLLIN;
        StructPollfd blockFd = new StructPollfd();
        blockFd.fd = mBlockFd;
        blockFd.events = (short) (OsConstants.POLLHUP | OsConstants.POLLERR);

        if (!deviceWrites.isEmpty())
            deviceFd.events |= (short) OsConstants.POLLOUT;

        StructPollfd[] polls = new StructPollfd[2 + dnsIn.size()];
        polls[0] = deviceFd;
        polls[1] = blockFd;
        {
            int i = -1;  //Vengono presi i socket
            for (WaitingOnSocketPacket wosp : dnsIn) {
                i++;
                StructPollfd pollFd = polls[2 + i] = new StructPollfd();
                pollFd.fd = ParcelFileDescriptor.fromDatagramSocket(wosp.socket).getFileDescriptor();
                pollFd.events = (short) OsConstants.POLLIN;
            }
        }

        Log.d(TAG, "doOne: Polling " + polls.length + " file descriptors");
        int result = FileHelper.poll(polls, vpnWatchDog.getPollTimeout());    //Chiamata al metodo poll per effettuare il polling dal
                                                                               //device, cioè si aspetta che arrivano i pacchetti

        if (result == 0) {
            Log.d("DEBUG","result==0 LocalVpnService");
            vpnWatchDog.handleTimeout();
            return true;
        }
        if (blockFd.revents != 0) {
            Log.i(TAG, "Told to stop VPN");
            return false;
        }
        // Need to do this before reading from the device, otherwise a new insertion there could
        // invalidate one of the sockets we want to read from either due to size or time out
        // constraints
        {
            int i = -1;
            Iterator<WaitingOnSocketPacket> iter = dnsIn.iterator();
            while (iter.hasNext()) {
                Log.d("DEBUG","SONO ANCORA NEL WHILE LocalVpnService");

                i++;
                WaitingOnSocketPacket wosp = iter.next();
                if ((polls[i + 2].revents & OsConstants.POLLIN) != 0) {
                    Log.d(TAG, "Read from DNS socket" + wosp.socket);
                    iter.remove();
                    handleRawDnsResponse(wosp.packet, wosp.socket);
                    wosp.socket.close();
                }
            }
        }
        if ((deviceFd.revents & OsConstants.POLLOUT) != 0) {
            Log.d(TAG, "Write to device");
            writeToDevice(outFd);
        }
        if ((deviceFd.revents & OsConstants.POLLIN) != 0) {
            Log.d(TAG, "Read from device");
            readPacketFromDevice(inputStream, packet);        //Chiamata metodo per leggere i pacchetti dal dispositivo
        }
        Log.d("DEBUG","return True metodo doOne");
        return true;
    }


    private void writeToDevice(FileOutputStream outFd) throws VpnNetworkException {
        try {
            outFd.write(deviceWrites.poll());
        } catch (IOException e) {
            // TODO: Make this more specific, only for: "File descriptor closed"
            throw new VpnNetworkException("Outgoing VPN output stream closed");
        }
    }

    /**
     * Permette la lettura dei pacchetti
     * @source https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag
     * @param inputStream
     * @param packet
     * @throws VpnNetworkException
     * @throws SocketException
     */
    //23
    private void readPacketFromDevice(FileInputStream inputStream, byte[] packet) throws VpnNetworkException, SocketException, PcapNativeException, NotOpenException, EOFException, TimeoutException {
        // Read the outgoing packet from the input stream.
        Log.d("DEBUG","metodo readPacketFromDevice LocalVpnService");
        PackageManager pm = this.getPackageManager();
        PackageInfo foregroundAppPackageInfo = null;

        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        AppLeak app = new AppLeak();
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(currentApp, 0);
            System.out.println("NOME PACKAGEEE"+foregroundAppPackageInfo.packageName);
            if(!foregroundAppPackageInfo.packageName.contains("privacychecker")) {
                app.setAppName(foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString());
                app.setIcon(pm.getApplicationIcon(foregroundAppPackageInfo.packageName));
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        Log.e("adapter", "Current App in foreground is: " + foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString());

        int length;

        try {
            length = inputStream.read(packet);    //Lettura dallo stream di input
        } catch (IOException e) {
            throw new VpnNetworkException("Cannot read frm device", e);
        }


        if (length == 0) {
            // TODO: Possibly change to exception
            Log.w(TAG, "Got empty packet!");
            return;
        }

        final byte[] readPacket = Arrays.copyOfRange(packet, 0, length);

        vpnWatchDog.handlePacket(readPacket);           //Chiamata metodo per la lettura del pacchetto
        String hostname = dnsPacketProxy.handleDnsRequest(readPacket);    //Chiamata metodo per la lettura del DNS del pacchetto

        boolean duplicate;

        if(!foregroundAppPackageInfo.packageName.contains("privacychecker") && hostname.contains(".")) {
            duplicate = false;

            for(AppLeak applicazione: listaAppLeaks){
                String compare = applicazione.getHostname().substring(6);

                if(applicazione.getAppName().equals(foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString())) {
                    if (applicazione.getHostname().substring(6).equals(hostname)) {
                        duplicate = true;
                        break;
                    }
                }
            }
            if(!duplicate) {
                if (dnsPacketProxy.getRuleDatabase().isBlocked(hostname.toLowerCase(Locale.ENGLISH))) {
                    app.setBlocked(true);
                    app.setHostname("Host: " + hostname);
                    countBlocked++;

                    try {
                        Drawable drawable = pm.getApplicationIcon(foregroundAppPackageInfo.packageName);
                        createNotifications(drawable,app.getAppName(),hostname);

                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }

                else {
                    app.setHostname("Host: " + hostname);
                    countPacket++;
                }

                listaAppLeaks.add(app);

            }

        }
    }

    public void createNotifications(Drawable d,String nameApp,String hostName){
        Log.d("DEBUG","METODO createNotifications");
        builder = new Notification.Builder(this);
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        builder.setSmallIcon(R.drawable.shieldicon)
                .setAutoCancel(true)
                .setContentTitle(nameApp)
                .setContentText("Host: "+hostName +"  BLOCCATO")
                .setContentIntent(pendingIntent);
        notificationManager.notify((int)System.currentTimeMillis(),builder.build());

    }

    public static int getCountBlocked(){
        return countBlocked;
    }

    public static int getCountPacket(){
        return countPacket;
    }


    public static ArrayList<AppLeak> getListaAppLeaks(){

        return listaAppLeaks;
    }


    private void handleRawDnsResponse(IpPacket parsedPacket, DatagramSocket dnsSocket) throws IOException {
        byte[] datagramData = new byte[1024];
        DatagramPacket replyPacket = new DatagramPacket(datagramData, datagramData.length);
        dnsSocket.receive(replyPacket);
        dnsPacketProxy.handleDnsResponse(parsedPacket, datagramData);
    }


    /**
     * Viene effettuato il forwarding dei pacchetti, devono essere inviati al DNS server reale
     * @param packet
     * @param requestPacket
     * @throws VpnNetworkException
     */
    @Override
    public void forwardPacket(DatagramPacket packet, IpPacket requestPacket)throws VpnNetworkException {
        Log.d("DEBUG","metodo forwardPacket LocalVpnService");

        DatagramSocket dnsSocket = null;
        try {
            // Packets to be sent to the real DNS server will need to be protected from the VPN
            dnsSocket = new DatagramSocket();

            vpnService.protect(dnsSocket);

            dnsSocket.send(packet);

            if (requestPacket != null)
                dnsIn.add(new WaitingOnSocketPacket(dnsSocket, requestPacket));
            else
                FileHelper.closeOrWarn(dnsSocket, TAG, "handleDnsRequest: Cannot close socket in error");
        } catch (IOException e) {
            FileHelper.closeOrWarn(dnsSocket, TAG, "handleDnsRequest: Cannot close socket in error");
            if (e.getCause() instanceof ErrnoException) {
                ErrnoException errnoExc = (ErrnoException) e.getCause();
                if ((errnoExc.errno == OsConstants.ENETUNREACH) || (errnoExc.errno == OsConstants.EPERM)) {
                    throw new VpnNetworkException("Cannot send message:", e);
                }
            }
            Log.w(TAG, "handleDnsRequest: Could not send packet to upstream", e);
            return;
        }
    }

    @Override
    public void queueDeviceWrite(IpPacket packet) {
        deviceWrites.add(packet.getRawData());
    }

    /**
     * Helper class holding a socket, the packet we are waiting the answer for, and a time
     */
    private static class WaitingOnSocketPacket {
        final DatagramSocket socket;
        final IpPacket packet;
        private final long time;

        WaitingOnSocketPacket(DatagramSocket socket, IpPacket packet) {
            this.socket = socket;
            this.packet = packet;
            this.time = System.currentTimeMillis();
        }

        long ageSeconds() {
            return (System.currentTimeMillis() - time) / 1000;
        }
    }


    private static class WospList implements Iterable<WaitingOnSocketPacket> {
        private final LinkedList<WaitingOnSocketPacket> list = new LinkedList<WaitingOnSocketPacket>();

        void add(WaitingOnSocketPacket wosp) {
            if (list.size() > DNS_MAXIMUM_WAITING) {
                Log.d(TAG, "Dropping socket due to space constraints: " + list.element().socket);
                list.element().socket.close();
                list.remove();
            }
            while (!list.isEmpty() && list.element().ageSeconds() > DNS_TIMEOUT_SEC) {
                Log.d(TAG, "Timeout on socket " + list.element().socket);
                list.element().socket.close();
                list.remove();
            }
            list.add(wosp);
        }

        public Iterator<WaitingOnSocketPacket> iterator() {
            return list.iterator();
        }

        int size() {
            return list.size();
        }

    }








    public void closeInterface()  {
        try {
            fileDescriptor.close();
            Log.d("DEBUG","CHIUSURA INTERFACCIA");

            fileDescriptor = null;

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
        countPacket = 0;
        mInterruptFd = FileHelper.closeOrWarn(mInterruptFd, TAG, "stopThread: Could not close interruptFd");
        mThread.interrupt();
        if (mThread != null && mThread.isAlive()) {
            Log.w(TAG, "stopThread: Could not kill VPN thread, it is still alive");
        }
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
            Log.d("DEBUG","thread interrotto");

        }
        super.onDestroy();
    }


    private void updateForegroundNotification(final int message) {
        Log.d("DEBUG","updateForeground");
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("PrivacyChecker");
        builder.setSmallIcon(R.drawable.shieldicon);
        builder.setContentText(getString(message));
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        Notification notify = builder.build();

        startForeground(1, notify);
    }


}


