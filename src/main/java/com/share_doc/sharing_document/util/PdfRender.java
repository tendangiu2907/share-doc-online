package com.share_doc.sharing_document.util;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class PdfRender {

    /**
     * Lấy đuôi file từ tên file.
     */
    public static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) {
            return ""; // Không có đuôi file
        }
        return fileName.substring(lastIndex + 1);
    }

    /**
     * Chuyển file .docx sang PDF.
     */
    public static InputStream convertDocxToPdf(MultipartFile documentFile) throws Exception {
        try (XWPFDocument document = new XWPFDocument(documentFile.getInputStream())) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, outputStream);
            pdfDocument.open();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                pdfDocument.add(new Paragraph(paragraph.getText()));
            }

            pdfDocument.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    /**
     * Chuyển file .doc sang PDF.
     */
    public static InputStream convertDocToPdf(MultipartFile documentFile) throws Exception {
        try (HWPFDocument document = new HWPFDocument(documentFile.getInputStream())) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, outputStream);
            pdfDocument.open();

            Range range = document.getRange();
            pdfDocument.add(new Paragraph(range.text()));

            pdfDocument.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
}


