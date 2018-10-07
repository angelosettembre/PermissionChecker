package com.isislab.settembre.privacychecker.privacyLeaks.db;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;


import com.isislab.settembre.privacychecker.privacyLeaks.Configuration;
import com.isislab.settembre.privacychecker.privacyLeaks.FileHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents hosts that are blocked.
 * <p>
 * This is a very basic set of hosts. But it supports lock-free
 * readers with writers active at the same time, only the writers
 * having to take a lock.
 */
public class RuleDatabase {

    private static final String TAG = "RuleDatabase";
    private static final RuleDatabase instance = new RuleDatabase();

    //oggeto di tipo AtomicReference che sarà aggiornato atomicamente quando l'oggetto di riferimento subisce qualche modifica
    final AtomicReference<HashSet<String>> blockedHosts = new AtomicReference<>(new HashSet<String>());
    HashSet<String> nextBlockedHosts = null;

    /**
     * Package-private constructor for instance and unit tests.
     */
    RuleDatabase() {

    }


    /**
     * Returns the instance of the rule database.
     */
    public static RuleDatabase getInstance() {
        return instance;
    }

    /**
     * Parse a single line in a hosts file
     *
     * @param line A line to parse
     * @return A host
     */
    //14
    @Nullable
    static String parseLine(String line) {

        int endOfLine = line.indexOf('#');

        if (endOfLine == -1)
            endOfLine = line.length();

        // Vengono tolti gli spazi
        while (endOfLine > 0 && Character.isWhitespace(line.charAt(endOfLine - 1)))
            endOfLine--;

        // The line is empty.
        if (endOfLine <= 0)
            return null;

        // Find beginning of host field
        int startOfHost = 0;

        if (line.regionMatches(0, "127.0.0.1", 0, 9) && (endOfLine <= 9 || Character.isWhitespace(line.charAt(9))))
            startOfHost += 10;
        else if (line.regionMatches(0, "::1", 0, 3) && (endOfLine <= 3 || Character.isWhitespace(line.charAt(3))))
            startOfHost += 4;
        else if (line.regionMatches(0, "0.0.0.0", 0, 7) && (endOfLine <= 7 || Character.isWhitespace(line.charAt(7))))
            startOfHost += 8;

        // Trim of space at the beginning of the host.
        while (startOfHost < endOfLine && Character.isWhitespace(line.charAt(startOfHost)))
            startOfHost++;

        // Reject lines containing a space
        for (int i = startOfHost; i < endOfLine; i++) {
            if (Character.isWhitespace(line.charAt(i)))
                return null;
        }

        if (startOfHost >= endOfLine)
            return null;

        return line.substring(startOfHost, endOfLine).toLowerCase(Locale.ENGLISH);
    }

    /**
     * Checks if a host is blocked. Si controlla se l'host è presente nell'insieme
     *
     * @param host A hostname
     * @return true if the host is blocked, false otherwise.
     */
    public boolean isBlocked(String host) {
        return blockedHosts.get().contains(host);
    }

    /**
     * Check if any hosts are blocked
     *
     * @return true if any hosts are blocked, false otherwise.
     */
    boolean isEmpty() {
        return blockedHosts.get().isEmpty();
    }

    /**
     * Load the hosts according to the configuration
     *
     * @param context A context used for opening files.
     * @throws InterruptedException Thrown if the thread was interrupted, so we don't waste time
     *                              reading more host files than needed.
     */
    public synchronized void initialize(Context context) throws InterruptedException {    //10   //Caricamento host
        Log.i("DEBUG", "metodo initialize RuleDatabase");

        Configuration config = FileHelper.loadCurrentSettings(context);     //Lettura dal file

        nextBlockedHosts = new HashSet<>(blockedHosts.get().size());

        Log.i(TAG, "Loading block list");

        if (!config.hosts.enabled) {
            Log.d(TAG, "loadBlockedHosts: Not loading, disabled.");
        } else {
            for (Configuration.Item item : config.hosts.items) {
                System.out.println("STAMPAAAA "+item.toString());
                if (Thread.interrupted()) {
                    throw new InterruptedException("Interrupted");
                }
                loadItem(context, item);                  //Caricamento host per ogni lista
            }
        }

        blockedHosts.set(nextBlockedHosts);   //viene settato con l'insieme di host bloccati
        Runtime.getRuntime().gc();
    }

    /**
     * Loads an item. An item can be backed by a file or contain a value in the location field.
     *
     * @param context Context to open files
     * @param item    The item to load.
     * @throws InterruptedException If the thread was interrupted.
     */
    private void loadItem(Context context, Configuration.Item item) throws InterruptedException {  //11
        Log.i("DEBUG", "metodo loadItem RuleDatabase");

        if (item.state == Configuration.Item.STATE_IGNORE)
            return;

        InputStreamReader reader;
        try {
            reader = FileHelper.openItemFile(context, item);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "loadItem: File not found: " + item.location);
            return;
        }

        if (reader == null) {
            addHost(item, item.location);
            Log.d("DEBUG","RuleDatabase reader==null");
            return;
        } else {
            loadReader(item, reader);        //Caricamento reader
        }
    }

    /**
     * Add a single host for an item.
     *
     * @param item The item the host belongs to
     * @param host The host
     */
    //15
    private void addHost(Configuration.Item item, String host) {

        // Single address to block
        if (item.state == Configuration.Item.STATE_ALLOW) {
            nextBlockedHosts.remove(host);
        } else if (item.state == Configuration.Item.STATE_DENY) {
            nextBlockedHosts.add(host);     //inserimento degli host all'interno dell'insieme
        }
    }

    /**
     * Permette la lettura degli host da ogni lista
     *
     * @param item   The configuration item referencing the file
     * @param reader A reader to read lines from
     * @throws InterruptedException If thread was interrupted
     */
    boolean loadReader(Configuration.Item item, Reader reader) throws InterruptedException {  //13
        Log.i("DEBUG", "metodo loadReader RuleDatabase");

        int count = 0;
        try {
            Log.d(TAG, "loadBlockedHosts: Reading: " + item.location);          //Lettura di un file hosts.txt
            try (BufferedReader br = new BufferedReader(reader)) {
                String line;
                while ((line = br.readLine()) != null) {           //Lettura di ogni host dal buffer
                    if (Thread.interrupted())
                        throw new InterruptedException("Interrupted");
                    String host = parseLine(line);               //Chiamata al metodo parser
                    if (host != null) {
                        count += 1;
                        addHost(item, host);                     //Chiamata metodo addHost per aggiungere l'host all'insieme di oggetti (HashSet)
                    }
                }
            }
            Log.d(TAG, "loadBlockedHosts: Loaded " + count + " hosts from " + item.location);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "loadBlockedHosts: Error while reading " + item.location + " after " + count + " items", e);
            return false;
        } finally {
            FileHelper.closeOrWarn(reader, TAG, "loadBlockedHosts: Error closing " + item.location);
        }
    }

}
