package org.dnsge.fbla.ebkmg.csv;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVExporter {

    /**
     * Writes a CSV file of objects to a path
     *
     * @param beans Objects to put in the file
     * @param writePath Path to write to
     * @throws IOException if something goes wrong
     */
    public static void writeCsvFromBeans(List<? extends CsvBeanWritable> beans, Path writePath) throws IOException {
        CSVWriter writer = new CSVWriter(Files.newBufferedWriter(writePath, StandardCharsets.UTF_8));
        List<String[]> allLines = new ArrayList<>();

        if (beans.size() > 0) {
            allLines.add(beans.get(0).csvHeaders());
        }

        beans.forEach(bean -> allLines.add(bean.asCsvLine()));

        writer.writeAll(allLines);
        writer.close();
    }

}
