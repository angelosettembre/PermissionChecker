/* Copyright (C) 2016 Julian Andres Klode <jak@jak-linux.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.isislab.settembre.privacychecker.privacyLeaks;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class. This is serialized as JSON using read() and write() methods.
 *
 */
public class Configuration {
    public static final Gson GSON = new Gson();
    static final int VERSION = 1;
    public int version = 1;
    public boolean autoStart;
    public Hosts hosts = new Hosts();
    public DnsServers dnsServers = new DnsServers();
    public Whitelist whitelist = new Whitelist();
    public boolean showNotification = true;
    public boolean nightMode;
    public boolean watchDog = false;
    public boolean ipV6Support = true;

    public static Configuration read(Reader reader) throws IOException {              //Metodo utilizzato per la lettura all'interno del file json
        Log.d("DEBUG","metodo read Configuration");

        Configuration config = GSON.fromJson(reader, Configuration.class);             //Lettura dal fie json

        if (config.whitelist.items.isEmpty()) {
            config.whitelist = new Whitelist();
            config.whitelist.items.add("com.android.vending");
            Log.d("DEBUG","config.whitelist.items.isEmpty()");

        }

        if (config.version > VERSION)
            throw new IOException("Unhandled file format version");

        return config;
    }

    public void write(Writer writer) throws IOException {
        GSON.toJson(this, writer);
    }

    public static class Item {
        public static final int STATE_IGNORE = 2;
        public static final int STATE_DENY = 0;
        public static final int STATE_ALLOW = 1;
        public String title;
        public String location;
        public int state;

        public boolean isDownloadable() {
            return location.startsWith("https://") || location.startsWith("http://");
        }

        @Override
        public String toString() {
            return "Item{" +
                    "title='" + title + '\'' +
                    ", location='" + location + '\'' +
                    ", state=" + state +
                    '}';
        }
    }

    public static class Hosts {
        public boolean enabled;
        public boolean automaticRefresh = false;
        public List<Item> items = new ArrayList<>();
    }

    public static class DnsServers {
        public boolean enabled;
        public List<Item> items = new ArrayList<>();
    }

    public static class Whitelist {
        /**
         * All apps use the VPN.
         */
        public static final int DEFAULT_MODE_ON_VPN = 0;
        /**
         * No apps use the VPN.
         */
        public static final int DEFAULT_MODE_NOT_ON_VPN = 1;
        /**
         * System apps (excluding browsers) do not use the VPN.
         */
        public static final int DEFAULT_MODE_INTELLIGENT = 2;

        public boolean showSystemApps;
        /**
         * The default mode to put apps in, that are not listed in the lists.
         */
        public int defaultMode = DEFAULT_MODE_ON_VPN;
        /**
         * Apps that should not be allowed on the VPN
         */
        public List<String> items = new ArrayList<>();
        /**
         * Apps that should be on the VPN
         */
        public List<String> itemsOnVpn = new ArrayList<>();


        /**
         * Returns an intent for opening a website, used for finding
         * web browsers. Extracted method for mocking.
         */
        Intent newBrowserIntent() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://isabrowser.dns66.jak-linux.org/"));
            return intent;
        }
    }
}
