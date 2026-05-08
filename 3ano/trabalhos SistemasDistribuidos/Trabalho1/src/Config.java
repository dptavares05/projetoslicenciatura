import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static Properties props;

    public static Properties get() throws IOException {
        if (props == null) {
            props = new Properties();

            // tenta carregar do classpath (src/config.properties)
            try (InputStream in = Config.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (in == null) {
                    throw new IOException("config.properties não encontrado no classpath.");
                }
                props.load(in);
            }
        }
        return props;
    }

    // helpers úteis
    public static String getString(String key) throws IOException {
        return get().getProperty(key);
    }

    public static int getInt(String key, int defaultValue) throws IOException {
        String v = get().getProperty(key);
        if (v == null)
            return defaultValue;
        return Integer.parseInt(v.trim());
    }
}
