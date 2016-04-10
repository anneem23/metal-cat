package org.anneem23.metal.cat.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * CommandLineHandler Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #CommandLineHandler(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class CommandLineHandler {

    private static final String BPM_CMD = "bpm";
    private static final String SOX_CMD = "sox %s -t raw -r 44100 -e float -c 1 -";

    private static String createSoxCmd(String filename) {
        return String.format(SOX_CMD, filename);
    }

    private static String createBpmCmd() {
        return BPM_CMD;
    }

    public static Double getBPM(String filename) {
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", createSoxCmd(filename) + " | " + createBpmCmd());
        try {
            Process process = builder.start();

            return Double.valueOf(getResult(process.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getResult(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            out.append(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
        return out.toString();
    }
}
