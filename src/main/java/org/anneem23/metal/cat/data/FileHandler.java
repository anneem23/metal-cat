package org.anneem23.metal.cat.data;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FileHandler Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #FileHandler(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class FileHandler {

    public static boolean fileExists(URI url) {
        if (url != null) {
            String[] parts = url.toString().split("/");
            String path = "/tmp/" + parts[parts.length - 1];
            return Files.exists(Paths.get(path));
        }
        return false;
    }

    public static String download(URI url) throws IOException {
        System.out.println("downloading " + url);
        String[] parts = url.toString().split("/");
        String path = "/tmp/" + parts[parts.length - 1];

        FileUtils.copyURLToFile(url.toURL(), new File(path));

        return path;
    }
}
