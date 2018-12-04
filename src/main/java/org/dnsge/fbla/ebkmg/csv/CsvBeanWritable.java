package org.dnsge.fbla.ebkmg.csv;

/**
 * Describes a class that can be exported to a .csv file
 * through the {@link CSVExporter} class
 *
 * @author Daniel Sage
 * @version 0.1
 */
public interface CsvBeanWritable {

    /**
     * Describes the object as comma seperated values
     *
     * @return String array for each value
     */
    String[] asCsvLine();


    /**
     * Describes the object's comma seperated values (dumb implementation)
     *
     * @return String array for the header for each value
     */
    String[] csvHeaders();

}
