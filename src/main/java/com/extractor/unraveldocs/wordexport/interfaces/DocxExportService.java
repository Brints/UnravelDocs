package com.extractor.unraveldocs.wordexport.interfaces;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface DocxExportService {
    ByteArrayInputStream generateDocxFromText(String text) throws IOException;
}
