package com.workhelper.domain.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.workhelper.domain.document.dto.DocumentDetailResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ComplaintPdfGenerator {
    private static final float PAGE_HEIGHT = 841;
    private static final float FIELD_SIZE = 9;
    private static final float REASON_SIZE = 9;
    private static final float REASON_LINE_HEIGHT = 14;
    private static final float REASON_X = 145;
    private static final float REASON_RIGHT = 496;
    private static final float REASON_TOP = 556;
    private static final float REASON_BOTTOM = 662;
    private static final float APPENDIX_MARGIN = 50;
    private static final float APPENDIX_TOP = 100;
    private static final float APPENDIX_BOTTOM = 50;

    public byte[] generate(DocumentDetailResponse document) {
        try (InputStream template = new ClassPathResource("pdf/complaint-template.pdf").getInputStream();
             PDDocument pdf = Loader.loadPDF(template.readAllBytes());
             InputStream fontStream = new ClassPathResource("fonts/NanumGothic-Regular.ttf").getInputStream()) {
            PDType0Font font = PDType0Font.load(pdf, fontStream);
            PDPage page = pdf.getPage(0);
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                JsonNode complainant = document.complainant();
                field(stream, font, value(complainant, "name"), 145, 257, 137, 1);
                field(stream, font, value(complainant, "address"), 145, 499, 158, 2);
                field(stream, font, value(complainant, "phone"), 145, 257, 178, 1);
                field(stream, font, value(complainant, "mobilePhone"), 345, 499, 178, 1);
                field(stream, font, value(complainant, "email"), 145, 499, 199, 1);
                check(stream, booleanValue(complainant, "receiveStatus"), 150, 200, 219);

                JsonNode respondent = document.respondent();
                field(stream, font, value(respondent, "name"), 145, 257, 279, 1);
                field(stream, font, value(respondent, "phone"), 345, 499, 279, 1);
                field(stream, font, value(respondent, "address"), 145, 499, 299, 2);
                if ("BUSINESS".equals(value(respondent, "businessType"))) mark(stream, 150, 322);
                if ("CONSTRUCTION".equals(value(respondent, "businessType"))) mark(stream, 220, 322);
                field(stream, font, value(respondent, "companyName"), 145, 499, 344, 1);
                field(stream, font, value(respondent, "address"), 145, 499, 367, 2);
                field(stream, font, value(respondent, "phone"), 145, 257, 393, 1);
                field(stream, font, value(respondent, "employeeCount"), 345, 499, 393, 1);

                JsonNode facts = document.facts();
                field(stream, font, date(facts, "hireDate"), 145, 257, 454, 1);
                field(stream, font, date(facts, "resignationDate"), 345, 499, 454, 1);
                field(stream, font, money(facts, "unpaidWages"), 145, 257, 475, 1);
                if ("RESIGNED".equals(value(facts, "employmentStatus"))) mark(stream, 350, 474);
                if ("EMPLOYED".equals(value(facts, "employmentStatus"))) mark(stream, 410, 474);
                field(stream, font, money(facts, "unpaidSeverancePay"), 145, 257, 495, 1);
                field(stream, font, money(facts, "unpaidOtherAmount"), 345, 499, 495, 1);
                field(stream, font, value(facts, "jobDescription"), 145, 499, 515, 1);
                field(stream, font, value(facts, "payDay"), 145, 257, 536, 1);
                if ("WRITTEN".equals(value(facts, "contractType"))) mark(stream, 350, 536);
                if ("VERBAL".equals(value(facts, "contractType"))) mark(stream, 410, 536);

                DocumentDetailResponse.Content content = document.content();
                if (content != null) {
                    laborOffice(stream, font, content.targetLaborOffice());
                }
            }

            String reason = document.content() == null ? null : document.content().claimReason();
            if (reason != null && !reason.isBlank()) {
                writeReason(pdf, font, reason);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate complaint PDF", e);
        }
    }

    private static void field(PDPageContentStream stream, PDType0Font font, String value,
                              float left, float right, float top, int maxLines) throws IOException {
        if (value == null || value.isBlank()) return;
        float size = FIELD_SIZE;
        List<String> lines = wrap(font, value.trim(), size, right - left);
        while (lines.size() > maxLines && size > 7) {
            size -= 0.5f;
            lines = wrap(font, value.trim(), size, right - left);
        }
        for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
            draw(stream, font, lines.get(i), size, left, PAGE_HEIGHT - top - (maxLines == 1 ? 4 : 1) - i * 9);
        }
    }

    private static void laborOffice(PDPageContentStream stream, PDType0Font font, String value) throws IOException {
        String text = laborOfficeDisplay(value);
        if (text.isEmpty()) return;
        float size = 10;
        float width = 170;
        while (size > 8.5f && font.getStringWidth(text) * size / 1000 > width) {
            size -= 0.5f;
        }
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(105, PAGE_HEIGHT - 725 - 4);
        float actualWidth = font.getStringWidth(text) * size / 1000;
        if (actualWidth > width) {
            stream.setHorizontalScaling(100 * width / actualWidth);
        }
        stream.showText(text);
        stream.endText();
    }

    private static String laborOfficeDisplay(String value) {
        if (value == null || value.isBlank()) return "";
        String text = value.strip();
        for (String suffix : List.of("고용노동지청", "고용노동청")) {
            if (text.endsWith(suffix) && text.length() > suffix.length()) {
                return text.substring(0, text.length() - suffix.length());
            }
        }
        return text;
    }
    // Draw two short strokes over the template's existing empty square. No checkmark glyph is needed.
    private static void mark(PDPageContentStream stream, float x, float top) throws IOException {
        float y = PAGE_HEIGHT - top;
        stream.setLineWidth(1.2f);
        stream.moveTo(x - 3, y);
        stream.lineTo(x, y - 3);
        stream.lineTo(x + 5, y + 5);
        stream.stroke();
    }

    private static void check(PDPageContentStream stream, Boolean value,
                              float yesX, float noX, float top) throws IOException {
        if (value == null) return;
        mark(stream, value ? yesX : noX, top);
    }

    private static void writeReason(PDDocument pdf, PDType0Font font, String reason) throws IOException {
        List<String> lines = wrap(font, reason, REASON_SIZE, REASON_RIGHT - REASON_X);
        int firstPageCapacity = (int) ((REASON_BOTTOM - REASON_TOP) / REASON_LINE_HEIGHT);
        int index = 0;
        try (PDPageContentStream stream = new PDPageContentStream(pdf, pdf.getPage(0),
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            while (index < lines.size() && index < firstPageCapacity) {
                draw(stream, font, lines.get(index++), REASON_SIZE, REASON_X,
                        PAGE_HEIGHT - REASON_TOP - 9 - (index - 1) * REASON_LINE_HEIGHT);
            }
        }
        while (index < lines.size()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);
            float height = page.getMediaBox().getHeight();
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page)) {
                draw(stream, font, "진정내용 별지", 14, APPENDIX_MARGIN, height - APPENDIX_MARGIN);
                float y = height - APPENDIX_TOP;
                while (index < lines.size() && y >= APPENDIX_BOTTOM + REASON_LINE_HEIGHT) {
                    draw(stream, font, lines.get(index++), REASON_SIZE, APPENDIX_MARGIN, y);
                    y -= REASON_LINE_HEIGHT;
                }
            }
        }
    }

    private static List<String> wrap(PDType0Font font, String text, float size, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        for (String paragraph : normalized.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (int offset = 0; offset < paragraph.length();) {
                int codePoint = paragraph.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                if (!line.isEmpty() && font.getStringWidth(line + character) * size / 1000 > width) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(character);
                offset += Character.charCount(codePoint);
            }
            lines.add(line.toString());
        }
        return lines;
    }

    private static void draw(PDPageContentStream stream, PDType0Font font, String text,
                             float size, float x, float y) throws IOException {
        if (text.isEmpty()) return;
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private static String value(JsonNode node, String name) {
        JsonNode found = node == null ? null : node.path(name);
        return found == null || found.isNull() || found.isMissingNode() ? "" : found.asText("");
    }

    private static Boolean booleanValue(JsonNode node, String name) {
        JsonNode found = node == null ? null : node.path(name);
        return found != null && found.isBoolean() ? found.booleanValue() : null;
    }

    private static String date(JsonNode node, String name) {
        String value = value(node, name);
        if (value.isBlank()) return "";
        try {
            LocalDate parsed = LocalDate.parse(value);
            return parsed.getYear() + ". " + parsed.getMonthValue() + ". " + parsed.getDayOfMonth() + ".";
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }

    private static String money(JsonNode node, String name) {
        JsonNode found = node == null ? null : node.path(name);
        return found != null && found.isNumber()
                ? NumberFormat.getNumberInstance(Locale.KOREA).format(found.decimalValue()) + "원" : "";
    }
}
