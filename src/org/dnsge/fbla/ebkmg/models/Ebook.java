package org.dnsge.fbla.ebkmg.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Represents an ebook in a SQLite database
 *
 * @author Daniel Sage
 * @version 0.1
 */
@DatabaseTable(tableName = "ebooks")
public final class Ebook {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) private int id;
    @DatabaseField private String ebookName;
    @DatabaseField(unique = true) private String ebookCode;
    @DatabaseField(foreign = true, foreignAutoRefresh = true) private Student student;

    /**
     * Ebook constructor for ORMLite
     */
    public Ebook() { }

    /**
     * Ebook constructor based on book name and redemption code
     *
     * @param ebookName Name of the Ebook
     * @param ebookCode Redemption code of the Ebook
     */
    public Ebook(String ebookName, String ebookCode) {
        this.ebookName = ebookName;
        this.ebookCode = ebookCode;
    }

    /**
     * @return Database ID Key
     */
    public int getId() {
        return id;
    }

    public String getEbookCode() {
        return ebookCode;
    }

    public String getEbookName() {
        return ebookName;
    }

    public Student getStudent() {
        return student;
    }

    public void setEbookCode(String ebookCode) {
        this.ebookCode = ebookCode;
    }

    public void setEbookName(String ebookName) {
        this.ebookName = ebookName;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}