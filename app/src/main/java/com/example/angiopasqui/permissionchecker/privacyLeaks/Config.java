package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.os.Environment;

import java.io.File;


/**
 * Created by Angiopasqui on 17/10/2017.
 */

public class Config {
    public static final ClientType CLIENT_TYPE = ClientType.DEBUG;
    public static final int CONT_NIOREAD = 100;
    public static final int CONT_TUNREAD = 100;
    public static final long DEFAULT_AHOCORASICKWORKER_SLEEP = 200;
    public static final String DataDir = (Environment.getExternalStorageDirectory() + File.separator + "Haystack");
    public static final boolean FLOW_DEBUG = false;
    public static final long FLOW_DNS_WAIT_TIME = 10000;
    public static final String FLOW_TAG = "Haystack.Flow";
    public static final int IDLE_CYCLES_BACKGROUND = 250;
    public static final int IDLE_CYCLES_INTERACTIVE = 100;
    public static final int IDLE_SLEEP_BACKGROUND = 100;
    public static final int IDLE_SLEEP_INTERACTIVE = 1;
    public static final long INTER_PROC_PARSING_INTERVAL = 50;
    public static final long INTER_PROC_PARSING_INTERVAL_UDP = 100;
    public static final boolean IP_DEBUG = false;
    public static final int LEAKS_FILTER_APP = 0;
    public static final int LEAKS_FILTER_DOM = 1;
    public static final int LEAKS_FILTER_TIME = 2;
    public static final int MAXTRACKERS_UI = 5;
    public static final int MAX_CONTACTS_SIGNATURE = 500;
    public static final int MAX_FLOW_QUEUE = 50000;
    public static final long MAX_FLOW_SIZE = 20000;
    public static final int MAX_LOOPS_BEFORE_SLEEP = 5;
    public static final long MAX_PACKETS_PER_FLOW = 10000;
    public static final int MAX_PHONE_LOG_ENTRIES = 20;
    public static final int MAX_PROC_CACHE_SIZE = 1024;
    public static final int MAX_PROC_CACHE_SIZE_TCP = 4096;
    public static final int MAX_PROC_CACHE_SIZE_UDP = 4096;
    public static final long MAX_UPLOAD_INTERVAL_ONWIFI = 900000;
    public static final int MIN_STRING_LENGTH = 6;
    public static final long MIN_UPLOAD_IVAL = 300000;
    public static final int MTU = 16000;
    public static final boolean PARSE_PACKET = true;
    public static final boolean POLLING_DEBUG = false;
    public static final int POLLING_TIMEOUT = 10000;
    public static final String POST_SUFIX = "/panopticon";
    public static final boolean PRINT_PRIVACY_STATS = false;
    public static final boolean PRINT_TLS_ERRORS = false;
    public static final boolean PRINT_TLS_SESSIONS = false;
    public static final boolean PRINT_TRAFFIC_STATS = false;
    public static final boolean PRINT_UNIQUE_FLOWS_PER_APP = false;
    public static final long REFRESH_FRAGMENT_INTERVAL_LONG = 30000;
    public static final long REFRESH_FRAGMENT_INTERVAL_SHORT = 20000;
    public static volatile int RETRY_CONN_INTERVAL_MSEC = IDLE_CYCLES_BACKGROUND;
    public static volatile int RETRY_CONN_MAX_MSEC = 90000;
    public static final int SERVER_PORT = 80;
    public static final String SERVER_URL = "haystack.mobi";
    public static final int SLEEP_TRAFFIC_PARSER_TCP = 500;
    public static final int SLEEP_TRAFFIC_PARSER_UDP = 100;
    public static final int SMS_CONTAINER_MAX_ENTRIES = 25;
    public static final boolean SOCKET_DEBUG = false;
    public static final boolean TCP_DEBUG = false;
    public static final boolean TCP_FLOW_PRINT_LOGCAT = false;
    public static final boolean TCP_FLOW_PRINT_SDCARD = true;
    public static final long TIMEOUT_TCP_FLOW_ENTRY = 900000;
    public static final long TLS_BLACKLIST_RESET_TIME = 300000;
    //public static final AlgorithmID TLS_CA_ALGORITHM = AlgorithmID.sha256WithRSAEncryption;
    public static final String TLS_CA_CERT_ENTRY_NAME = "lumen-ca";
    public static final String TLS_CA_CERT_FILE = "lumen_ca_cert.pem";
    public static final String TLS_CA_COMMON_NAME = "Lumen Certificate Authority";
    public static final String TLS_CA_COUNTRY = "US";
    public static final String TLS_CA_EMAIL = "lumen@icsi.berkeley.edu";
    public static final String TLS_CA_KEYSTORE_FILE = "lumen_trust_store";
    public static final String TLS_CA_KEYSTORE_PASSWORD = "passphrase";
    public static final String TLS_CA_KEY_ALIAS = "lumen";
    public static final int TLS_CA_KEY_LENGTH = 2048;
    public static final String TLS_CA_ORGANIZATION = "ICSI";
    public static final String TLS_CA_ORG_UNIT = "Lumen";
    public static final boolean TLS_CA_PKCS12 = false;
    public static final String TLS_CA_STATE = "California";
    public static final boolean TLS_CERT_DEBUG = false;
    public static final boolean TLS_DEBUG = false;
    public static final int TLS_HANDSHAKE_MESSAGE_MAX_LEN = 20000;
    public static final boolean TLS_LOG_TRAFFIC = false;
    public static final boolean TLS_MATCHING = false;
    public static final boolean TLS_MODIFY = false;
    public static final int TLS_NO_PROXY_STEP = 2;
    public static final boolean TLS_WRITE_TRAFFIC_TO_FILE = false;
    public static final long TRAFFIC_ANALYZR_DB_FLUSH_TIMEOUT = 5000;
    public static final boolean UDP_DEBUG = false;
    public static final long UDP_IDLE_THRES = 15000;
    public static final int V4PROTOCOLIND = 9;
    public static final int V4PROTOCOLTCP = 6;
    public static final int V4PROTOCOLUDP = 17;
    public static final int V6PROTOCOLIND = 6;
    public static final int V6PROTOCOLTCP = 6;
    public static final int V6PROTOCOLUDP = 17;
    public static final BuildType buildType = BuildType.PRODUCTION;

    public enum BuildType {
        PRODUCTION,
        DEBUG,
        FLOW_CONTENTS
    }

    public enum ClientType {
        TAPPAS,
        COPPA,
        APPCENSUS,
        PUBLIC,
        DEBUG
    }
}

