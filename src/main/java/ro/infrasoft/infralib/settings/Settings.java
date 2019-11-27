package ro.infrasoft.infralib.settings;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Clasa care se ocupa cu setari globale.
 */
public class Settings {
    private static String SETTINGS_FILE_LOCATION;
    private static HashMap<String, HashMap<String, String>> internalStore;

    static {
        String location = System.getenv("DOCUMENTA_HOME");
        if (location == null) {
            location = "C:/";
        }
        SETTINGS_FILE_LOCATION = location + "settings.txt";
    }

    public synchronized static String get(String core, String key) throws Exception {
        if (internalStore == null) {
            populateInternalStore();
        }

        if (!internalStore.containsKey(core))
            throw new Exception("CORE_KEY_NOT_FOUND: " + core + " (key was " + key + ")");

        HashMap<String, String> item = internalStore.get(core);

        if (!item.containsKey(key))
            throw new Exception("ITEM_KEY_NOT_FOUND: " + key + " (core was " + core + ")");

        return item.get(key);
    }

    public synchronized static void populateInternalStore(String location) throws Exception {
        internalStore = new HashMap<String, HashMap<String, String>>();

        // Mai intai citim fisierul
        File inputFile = new File(location);
        if (!inputFile.exists())
            throw new Exception("INPUT_FILE_NOT_EXIST");

        // Apoi generam org.json
        String fileContents = FileUtils.readFileToString(inputFile);
        JSONObject baseJson = new JSONObject(fileContents);

        // Se parseaza org.json, baza apoi itemii si se construieste harta interna
        Iterator baseKeys = baseJson.keys();
        while (baseKeys.hasNext()) {
            String baseKey = baseKeys.next().toString();
            JSONObject itemJson = baseJson.getJSONObject(baseKey);
            HashMap<String, String> itemValues = new HashMap<String, String>();

            Iterator itemKeys = itemJson.keys();
            while (itemKeys.hasNext()) {
                String itemKey = itemKeys.next().toString();
                String itemValue = itemJson.getString(itemKey);
                itemValues.put(itemKey, itemValue);
            }

            internalStore.put(baseKey, itemValues);
        }
    }

    public synchronized static void populateInternalStore() throws Exception {
        populateInternalStore(SETTINGS_FILE_LOCATION);
    }

    public static HashMap<String, HashMap<String, String>> getInternalStore() throws Exception {
        if (internalStore == null) {
            populateInternalStore();
        }

        return internalStore;
    }
}
