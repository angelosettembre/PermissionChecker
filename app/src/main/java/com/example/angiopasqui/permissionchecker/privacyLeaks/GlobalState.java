package com.example.angiopasqui.permissionchecker.privacyLeaks;

import java.security.KeyPair;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Created by Angiopasqui on 17/10/2017.
 */
//CLASSE CONTENTE VARIABILI USATE IN TUTTA L'APP, CHE POSSONO CAMBIARE A RUN-TIME

public class GlobalState {

    public static volatile long BUILD_TIME = -1;
    public static volatile int CHARGING_STATE = -1;
    public static volatile int CURRENT_TAB = 0;
    public static volatile int REMAINING_BATTERY_CAPACITY = -1;
    public static volatile boolean RESTART = true;
    public static volatile int SCREEN_STATE = -1;
    public static volatile boolean STOP = false;
    public static volatile int TAB_LEAKS_FILTER = 0;
    public static volatile boolean TLS_CA_INSTALLED = false;
    public static volatile boolean TLS_RUNNING = false;
    public static volatile boolean TLS_STOP = false;
    public static volatile boolean TUN_HAS_BYTES = false;
    public static volatile boolean VPN_ENABLED = false;
    //public static volatile Map<String, FlowEntry> activeTcpFlowMap = new ConcurrentHashMap();
    //public static volatile Map<String, FlowEntry> activeUdpFlowMap = new ConcurrentHashMap();
    //public static volatile ConcurrentHashMap<String, TrafficProfileEntryInfo> appTrafficInfoCache = new ConcurrentHashMap();
   //public static CaCertificate ca = null;
    //public static volatile Map<String, PassiveDNSTrafficEntry> dnsTrafficCache = new ConcurrentHashMap();
    public static int fileDescriptorCount = 0;
    public static Semaphore flowPrintLogcatSemaphore = new Semaphore(1);
    public static Semaphore flowPrintSdCardSemaphore = new Semaphore(1);
    public static volatile String haystackPackageName;
    public static volatile ConcurrentHashMap<String, KeyPair> keyMap = new ConcurrentHashMap();
    public static KeyStore keyStore = null;
    public static volatile int lastConnectivity = -1;
    public static volatile boolean launching_vpn = false;
    //public static MaxMindDriver maxMind = null;
    public static volatile long numberApps = -1;
    public static volatile long numberFlows = -1;
    public static volatile long numberLeaks = -1;
    public static volatile long numberTrackers = -1;
    //public static volatile Map<String, FlowEntry> offlineTcpFlowMap = new ConcurrentHashMap();
    //public static volatile Map<String, FlowEntry> offlineUdpFlowMap = new ConcurrentHashMap();
    //public static volatile Map<String, AppPermissionsEntry> packageInfoMap = new ConcurrentHashMap();
    //public static volatile List<HaystackPackageInfo> packageList = null;
    public static volatile String perfLogContents = "";
    //public static volatile Map<String, PrivacyEntry> privayLeaks = new ConcurrentHashMap();
    public static volatile boolean start_error = false;
    public static volatile List<String> tlsBlacklist = new ArrayList();
    public static volatile Map<String, Integer> tlsProxyCount = new ConcurrentHashMap();
    //public static volatile Map<String, TLSEntry> tlsSessions = new ConcurrentHashMap();
    public static volatile UUID vpn_session_uuid = null;
}
