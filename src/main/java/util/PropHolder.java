package util;

import java.io.IOException;
import java.util.Properties;

public class PropHolder {
    private static Properties properties = null;

    public static synchronized Properties getProperties() throws IOException {
        if(properties == null){
            properties = new Properties();
            properties.load(PropHolder.class.getClassLoader().getResourceAsStream("datacollection.properties"));
        }
        return properties;
    }

}
