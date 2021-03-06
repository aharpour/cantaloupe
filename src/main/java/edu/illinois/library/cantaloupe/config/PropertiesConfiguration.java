package edu.illinois.library.cantaloupe.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Class wrapping a Commons PropertyConfiguration instance for writing
 * properties files retaining structure and comments.
 */
class PropertiesConfiguration extends FileConfiguration implements Configuration {

    private org.apache.commons.configuration.PropertiesConfiguration commonsConfig =
            new org.apache.commons.configuration.PropertiesConfiguration();

    public void clear() {
        commonsConfig.clear();
    }

    public boolean getBoolean(String key) {
        return commonsConfig.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return commonsConfig.getBoolean(key, defaultValue);
    }

    public double getDouble(String key) {
        return commonsConfig.getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        return commonsConfig.getDouble(key, defaultValue);
    }

    public float getFloat(String key) {
        return commonsConfig.getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        return commonsConfig.getFloat(key, defaultValue);
    }

    public int getInt(String key) {
        return commonsConfig.getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        return commonsConfig.getInt(key, defaultValue);
    }

    public Iterator<String> getKeys() {
        return commonsConfig.getKeys();
    }

    public long getLong(String key) {
        return commonsConfig.getLong(key);
    }

    public long getLong(String key, long defaultValue) {
        return commonsConfig.getLong(key, defaultValue);
    }

    public Object getProperty(String key) {
        return commonsConfig.getProperty(key);
    }

    public String getString(String key) {
        return commonsConfig.getString(key);
    }

    public String getString(String key, String defaultValue) {
        return commonsConfig.getString(key, defaultValue);
    }

    public synchronized void reload() {
        final File configFile = getFile();
        if (configFile != null) {
            try {
                if (commonsConfig != null) {
                    System.out.println("Reloading config file: " + configFile);
                } else {
                    System.out.println("Loading config file: " + configFile);
                }
                commonsConfig = new org.apache.commons.configuration.
                        PropertiesConfiguration(configFile);
            } catch (org.apache.commons.configuration.ConfigurationException e) {
                // The logger may not have been initialized yet, as it depends
                // on a working configuration. (Also, we don't want to
                // introduce a dependency on the logger.)
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Saves the configuration to the file returned by
     * {@link #getFile()}, if available, or does nothing if not.
     *
     * @throws IOException
     */
    public synchronized void save() throws IOException {
        try {
            commonsConfig.save();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public synchronized void setProperty(String key, Object value) {
        commonsConfig.setProperty(key, value);
    }

}
