package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.content.Context;
import android.net.Uri;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Utility class for working with files.
 */

public final class FileHelper {

    /**
     * Try open the file with {@link Context#openFileInput(String)}, falling back to a file of
     * the same name in the assets.
     */
    public static InputStream openRead(Context context, String filename) throws IOException {
        try {
            return context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            return context.getAssets().open(filename);
        }
    }

    /**
     * Write to the given file in the private files dir, first renaming an old one to .bak
     *
     * @param context  A context
     * @param filename A filename as for @{link {@link Context#openFileOutput(String, int)}}
     * @return See @{link {@link Context#openFileOutput(String, int)}}
     * @throws IOException See @{link {@link Context#openFileOutput(String, int)}}
     */
    public static OutputStream openWrite(Context context, String filename) throws IOException {
        File out = context.getFileStreamPath(filename);

        // Create backup
        out.renameTo(context.getFileStreamPath(filename + ".bak"));

        return context.openFileOutput(filename, Context.MODE_PRIVATE);
    }

    private static Configuration readConfigFile(Context context, String name, boolean defaultsOnly) throws IOException {
        Log.d("DEBUG","metodo readConfigFile FileHelper");

        InputStream stream;            //Apertura file
        if (defaultsOnly) {
            stream = context.getAssets().open(name);
            if(stream != null){
                Log.d("DEBUG","FILE TROVATOOOOO");
            }
        }

        else {
            stream = FileHelper.openRead(context, name);
            if(stream != null){
                Log.d("DEBUG","FILE TROVATOOOOO");
            }
        }
        return Configuration.read(new InputStreamReader(stream));
    }

    public static Configuration loadCurrentSettings(Context context) {
        Log.d("DEBUG","metodo loadCurrentSettings FileHelper");

        try {
            return readConfigFile(context, "settings.json", false);
        } catch (Exception e) {
            return loadPreviousSettings(context);
        }
    }

    public static Configuration loadPreviousSettings(Context context) {
        try {
            return readConfigFile(context, "settings.json.bak", false);
        } catch (Exception e) {
            return loadDefaultSettings(context);
        }
    }

    public static Configuration loadDefaultSettings(Context context) {
        try {
            Log.d("DEBUG","metodo loadDefaultSettings FileHelper");

            return readConfigFile(context, "settings.json", true);
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeSettings(Context context, Configuration config) {
        Log.d("FileHelper", "writeSettings: Writing the settings file");
        try {
            Writer writer = new OutputStreamWriter(FileHelper.openWrite(context, "settings.json"));
            config.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a file where the item should be downloaded to.
     *
     * @param context A context to work in
     * @param item    A configuration item.
     * @return File or null, if that item is not downloadable.
     */
    public static File getItemFile(Context context, Configuration.Item item) {
        if (item.isDownloadable()) {
            try {
                Log.d("DEBUG","RETURN getItemFile new File(context.getExternalFilesDir(null), java.net.URLEncoder.encode(item.location, \"UTF-8\"));");
                return new File(context.getExternalFilesDir(null), java.net.URLEncoder.encode(item.location, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Apertura elemento del file
     * @param context
     * @param item    Elemento da aprire
     * @return
     * @throws FileNotFoundException
     */
    //12
    public static InputStreamReader openItemFile(Context context, Configuration.Item item) throws FileNotFoundException {
        Log.d("DEBUG","metodo openItemFile FileHelper");

        if (item.location.startsWith("content://")) {
            try {
                Log.d("DEBUG","RETURN item.location.startsWith(\"content://\")");
                return new InputStreamReader(context.getContentResolver().openInputStream(Uri.parse(item.location)));
            } catch (SecurityException e) {
                Log.d("FileHelper", "openItemFile: Cannot open", e);
                throw new FileNotFoundException(e.getMessage());
            }
        } else {
            File file = getItemFile(context, item);
            if (file == null)
                System.out.print("FILE VUOTOOOOOOoooooooo");
            if (file == null)
                return null;
            if (item.isDownloadable()) {
                Log.d("DEBUG","RETURN new InputStreamReader(new SingleWriterMultipleReaderFile(getItemFile(context, item)).openRead())");
                return new InputStreamReader(new SingleWriterMultipleReaderFile(getItemFile(context, item)).openRead());
            }
            Log.d("DEBUG","RETURN FileReader(getItemFile(context, item))");

            return new FileReader(getItemFile(context, item));
        }
    }

    /**
     * Wrapper around {@link Os#poll(StructPollfd[], int)} that automatically restarts on EINTR
     * While post-Lollipop devices handle that themselves, we need to do this for Lollipop.
     *
     * @param fds     Descriptors and events to wait on
     * @param timeout Timeout. Should be -1 for infinite, as we do not lower the timeout when
     *                retrying due to an interrupt
     * @return The number of fds that have events
     * @throws ErrnoException See {@link Os#poll(StructPollfd[], int)}
     */
    public static int poll(StructPollfd[] fds, int timeout) throws ErrnoException, InterruptedException {
        while (true) {
            if (Thread.interrupted())
                throw new InterruptedException();
            try {
                return Os.poll(fds, timeout);
            } catch (ErrnoException e) {
                if (e.errno == OsConstants.EINTR)
                    continue;
                throw e;
            }
        }
    }

    public static FileDescriptor closeOrWarn(FileDescriptor fd, String tag, String message) {
        try {
            if (fd != null)
                Os.close(fd);
        } catch (ErrnoException e) {
            Log.e(tag, "closeOrWarn: " + message, e);
        } finally {
            return null;
        }
    }

    public static <T extends Closeable> T closeOrWarn(T fd, String tag, String message) {
        try {
            if (fd != null)
                fd.close();
        } catch (Exception e) {
            Log.e(tag, "closeOrWarn: " + message, e);
        } finally {
            return null;
        }
    }
}
