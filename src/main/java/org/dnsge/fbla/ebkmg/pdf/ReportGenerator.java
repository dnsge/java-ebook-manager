package org.dnsge.fbla.ebkmg.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.dnsge.fbla.ebkmg.db.Student;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class to generate reports about Students
 *
 * @author Daniel Sage
 * @since 0.4
 * @version 0.1
 */
@SuppressWarnings("deprecation")
public class ReportGenerator {

    // 615 wide, 800 tall

    private final static int WIDTH = 615;
    private final static int HEIGHT = 800;
    private final static int HORZ_MARGIN = 50;
    private final static int VERT_MARGIN = 50;

    private final static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("MMMM dd, YYY");
    private final static SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("hh:mm aa");

    /**
     * Creates a non-custom report
     *
     * @param saveFile File to save the pdf to
     * @param students List of students to include in the report
     * @throws IOException if something goes wrong with fileio
     */
    public static void generateReport(File saveFile, List<Student> students) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage titlePage = new PDPage();
        PDPageContentStream contentStream = new PDPageContentStream(doc, titlePage);

        drawTitleHeader(contentStream);

        ArrayList<PDPage> pages = null;

        students = filterList(students, Student::hasEbook);

        if (students.size() > 0) {
            int heightAt = 651;
            int index = printManyStudents(contentStream, students, 0, heightAt);

            pages = new ArrayList<>();
            while (index < students.size()) {
                heightAt = HEIGHT - VERT_MARGIN - 12;
                PDPage newPage = new PDPage();
                PDPageContentStream newStream = new PDPageContentStream(doc, newPage);

                index = printManyStudents(newStream, students, index, heightAt);
                pages.add(newPage);
                newStream.close();
            }
        } else {
            contentStream.beginText();
            contentStream.newLineAtOffset(HORZ_MARGIN, 665);
            contentStream.showText("There are no students that are paired with an Ebook.");
            contentStream.endText();
        }

        contentStream.close();
        doc.addPage(titlePage);

        if (pages != null) {
            pages.forEach(doc::addPage);
        }

        doc.save(saveFile);
        doc.close();
    }

    /**
     * Prints out as many students that fit on a page as possible
     *
     * @param stream {@code PDPageContentStream} to print to
     * @param students List of students to include
     * @param startIndex start index of the students list
     * @param startHeight start height on the page
     * @return the index that the loop finished on
     * @throws IOException if something goes wrong with writing information
     */
    private static int printManyStudents(PDPageContentStream stream, List<Student> students, int startIndex, int startHeight) throws IOException {
        int i = startIndex;
        int rHeight = startHeight;
        while (rHeight > VERT_MARGIN && i < students.size()) {
            renderStudentInformation(stream, students.get(i), rHeight);
            i++;
            rHeight -= 20;
        }
        return i;
    }


    /**
     * Draws the headers on a report
     *
     * @param stream {@code PDPageContentStream} to print to
     * @throws IOException if something goes wrong with writing information
     * @apiNote Uses depreciated methods in pdfbox
     */
    private static void drawTitleHeader(PDPageContentStream stream) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(HORZ_MARGIN, 750 - 20);
        stream.setFont(PDType1Font.HELVETICA_BOLD, 30);
        stream.showText("Ebook Redemption Report");
        stream.endText();

        stream.drawLine(HORZ_MARGIN - 5, 715, WIDTH - HORZ_MARGIN + 5, 715);

        stream.beginText();
        stream.setLeading(20);
        stream.newLineAtOffset(HORZ_MARGIN, 691);
        stream.setFont(PDType1Font.HELVETICA, 12);

        Date now = new Date();

        stream.showText("Report generated on ");
        stream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        stream.showText(String.format("%s at %s", FORMAT_DATE.format(now), FORMAT_TIME.format(now)));
        stream.setFont(PDType1Font.HELVETICA, 12);
        stream.newLine();
        stream.endText();
    }

    /**
     * Renders a single line with student information
     *
     * @param stream {@code PDPageContentStream} to render to
     * @param stu {@link Student} object to render IF it has a book currently redeemed & paired
     * @param heightAt height to render at
     * @throws IOException if something goes wrong while rendering
     * @apiNote Uses depreciated methods in pdfbox
     */
    private static void renderStudentInformation(PDPageContentStream stream, Student stu, int heightAt) throws IOException {
        if (stu.getOwnedEbook() != null) {
            stream.beginText();
            stream.newLineAtOffset(HORZ_MARGIN, heightAt);
            stream.setFont(PDType1Font.COURIER, 12);

            String renderString = String.format("%s %s (%s) assigned book '%s' (code %s)",
                    stu.getFirstName(),
                    stu.getLastName(),
                    stu.getStudentId(),
                    stu.getOwnedEbook().getName(),
                    stu.getOwnedEbook().getCode()
            );

            if (renderString.length() > 71) {
                renderString = renderString.substring(0, 68) + "...";
            }

            stream.showText(renderString);
            stream.endText();
        }
    }

    /**
     * Filters a List based off of a Predicate
     *
     * @param inputList Input list to filter
     * @param predicate Predicate to test with
     * @param <T> Type of the items of the list
     * @return Filtered list
     */
    private static <T> List<T> filterList(List<T> inputList, Predicate<T> predicate) {
        List<T> r = new ArrayList<>();
        for (T item : inputList) {
            if (predicate.test(item)) {
                r.add(item);
            }
        }

        return r;
    }
}
