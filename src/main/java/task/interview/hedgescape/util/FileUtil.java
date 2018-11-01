package task.interview.hedgescape.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Utility class for file-based operations.
 */
public class FileUtil {

    /**
     * Reads the contents of a text file located in the resources folder.
     *
     * @param resourceName The resource file name.
     * @return File contents as a {@link String}.
     */
    public static String readResourceAsString(String resourceName) {
        String result = null;

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(resourceName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
