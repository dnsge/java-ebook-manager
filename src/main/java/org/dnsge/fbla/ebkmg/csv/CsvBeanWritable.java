package org.dnsge.fbla.ebkmg.csv;

public interface CsvBeanWritable {
    String[] asCsvLine();
    String[] csvHeaders();
}
