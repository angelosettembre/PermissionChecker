package com.isislab.settembre.privacychecker.privacyLeaks;

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

    public static volatile boolean STOP = false;
    public static volatile boolean VPN_ENABLED = false;
    public static volatile boolean launching_vpn = false;
    public static volatile boolean start_error = false;

}
