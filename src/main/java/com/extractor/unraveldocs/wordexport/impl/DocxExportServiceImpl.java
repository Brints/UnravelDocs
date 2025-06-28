package com.extractor.unraveldocs.wordexport.impl;

import com.extractor.unraveldocs.wordexport.interfaces.DocxExportService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocxExportServiceImpl implements DocxExportService {

    @Override
    public ByteArrayInputStream generateDocxFromText(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String[] paragraphs = text.split("\\r?\\n");

            for (String p : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(p);
            }

            document.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
