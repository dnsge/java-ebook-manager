package org.dnsge.fbla.ebkmg.csv;

import com.opencsv.CSVWriter;
import org.dnsge.fbla.ebkmg.Main;
import org.dnsge.fbla.ebkmg.db.Student;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVExporter {

    private static class CsvStudentWritable implements CsvBeanWritable {
        private String firstName;
        private String lastName;
        private String grade;
        private String studentId;

        CsvStudentWritable(Student input) {
            firstName = input.getFirstName();
            lastName = input.getLastName();
            grade = input.getGrade();
            studentId = input.getStudentId();
        }

        CsvStudentWritable(String firstName, String lastName, String grade, String studentId) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.grade = grade;
            this.studentId = studentId;
        }

        @Override
        public String[] asCsvLine() {
            return new String[]{firstName, lastName, grade, studentId};
        }

        @Override
        public String[] csvHeaders() {
            return new String[]{"firstName", "lastName", "grade", "studentId"};
        }
    }

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

        beans.forEach(bean -> {
            allLines.add(bean.asCsvLine());
        });

        writer.writeAll(allLines);
        writer.close();
    }

//    public static void writeCsvFromBeans(Collection<List<? extends CsvBeanWritable>> tables, Path writePath) throws IOException {
//        CSVWriter writer = new CSVWriter(Files.newBufferedWriter(writePath, StandardCharsets.UTF_8));
//        List<String[]> allLines = new ArrayList<>();
//
//        for (List<? extends CsvBeanWritable> table : tables) {
//            if (table.size() > 0) {
//                writer.writeNext(table.get(0).csvHeaders());
//            }
//            table.forEach(bean -> {
//                allLines.add(bean.asCsvLine());
//            });
//            allLines.add(new String[]{});
//        }
//        writer.writeAll(allLines);
//        writer.close();
//    }

//    public static void writeCsvFromBeans(List<? extends CsvBeanWritable>[] tables, Path writePath) throws IOException {
//        CSVWriter writer = new CSVWriter(Files.newBufferedWriter(writePath, StandardCharsets.UTF_8));
//        List<String[]> allLines = new ArrayList<>();
//
//        for (List<? extends CsvBeanWritable> table : tables) {
//            if (table.size() > 0) {
//                allLines.add(table.get(0).csvHeaders());
//            }
//            table.forEach(bean -> {
//                allLines.add(bean.asCsvLine());
//            });
//            allLines.add(new String[]{});
//        }
//        writer.writeAll(allLines);
//        writer.close();
//    }

    public static void main(String[] args) throws IOException {
        List<CsvStudentWritable> stus = new ArrayList<>();
        stus.add(new CsvStudentWritable("A", "L", "10", "101010"));
        stus.add(new CsvStudentWritable("B", "LFFF", "11", "6643143"));
        stus.add(new CsvStudentWritable("C", "Z", "12", "234"));
        stus.add(new CsvStudentWritable("D", "L, Q", "185", "645687"));
        stus.add(new CsvStudentWritable("E", "L\"tt", "0", "123"));

        File f = new File(Main.EBOOK_DIRECTORY, "test.csv");
        writeCsvFromBeans(stus, f.toPath());

    }

}
