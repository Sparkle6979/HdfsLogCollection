package util;

import java.io.IOException;
import java.util.Properties;

public class PropHolder {
    // 懒汗式单例
    private volatile static Properties properties = null;

    public static Properties getProperties() throws IOException {
        if (properties == null) {
            synchronized (Properties.class) {
                if (properties == null) {
                    properties = new Properties();
                    properties.load(PropHolder.class.getClassLoader().getResourceAsStream("datacollection.properties"));
                }
            }
        }
        return properties;
    }
}
