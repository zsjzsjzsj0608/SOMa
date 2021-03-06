package cs437.som.util;

import cs437.som.SOMError;
import cs437.som.TrainableSelfOrganizingMap;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Reads a self-organizing map from a file.
 */
public class FileReader {
    private static final Pattern COLON_SPLIT = Pattern.compile(":");
    private TrainableSelfOrganizingMap tsom = null;

    /**
     * Begin reading a SOM from a file.
     *
     * This method reads the first line, which must be a map type specifier.
     * That type specifier is used to create an object of that map type
     * through reflection.  The remainder of the input processing is then
     * delegated to that SOM.
     *
     * @param input The input file.
     * @throws IOException if an I/O error occurs.
     */
    private FileReader(File input) throws IOException {
        BufferedReader isr = new BufferedReader(
                new InputStreamReader(new FileInputStream(input)));
        String[] kv = COLON_SPLIT.split(isr.readLine());
        if (kv.length != 2) {
            throw new SOMError(
                    "Input file is malformed: first line must be a map type statement.");
        }

        String className = "cs437.som.network." + kv[1].trim();
        try {
            Class<?> mapType = Class.forName(className);
            Method readMethod = mapType.getMethod("read", BufferedReader.class);
            tsom = (TrainableSelfOrganizingMap) readMethod.invoke(mapType, isr);
        } catch (ClassNotFoundException e) {
            throw new SOMError("Map type " + className + " cannot be found.");
        } catch (NoSuchMethodException e) {
            throw new SOMError("Map type " + className + " cannot be loaded from a file.");
        } catch (InvocationTargetException e) {
            throw new SOMError(e.getCause().getMessage());
        } catch (IllegalAccessException e) {
            throw new SOMError("Internal error while parsing: " + e.getMessage());
        }
    }

    /**
     * Read a SOM from a {@code File}.
     *
     * @param file The input file.
     * @return A SOM configured as specified in {@code file}.
     * @throws IOException if an I/O error occurs.
     */
    public static TrainableSelfOrganizingMap read(File file)
            throws IOException {
        FileReader fileReader = new FileReader(file);
        return fileReader.tsom;
    }

    /**
     * Read a SOM from a file.
     *
     * @param filename The path to the input file.
     * @return A SOM configured as specified in {@code file}.
     * @throws IOException if an I/O error occurs.
     */
    public static TrainableSelfOrganizingMap read(String filename)
            throws IOException {
        File file = new File(filename);
        return read(file);
    }

    @Override
    public String toString() {
        return "FileReader";
    }
}
