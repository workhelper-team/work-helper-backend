package com.workhelper.domain.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.workhelper.domain.document.dto.DocumentDetailResponse;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class ComplaintPdfGenerator {
    private static final float MARGIN = 48;
    private static final float FONT_SIZE = 10;
    private static final float LINE_HEIGHT = 17;

    public byte[] generate(DocumentDetailResponse document) {
        try (PDDocument pdf = new PDDocument();
             InputStream fontStream = new ClassPathResource("fonts/NanumGothic-Regular.ttf").getInputStream()) {
            PDType0Font font = PDType0Font.load(pdf, fontStream);
            try (Writer writer = new Writer(pdf, font)) {
                writer.title("노동청 진정서");
                writer.section("진정인");
                JsonNode complainant = document.complainant();
                writer.field("성명", value(complainant, "name"));
                writer.field("생년월일", date(complainant, "birthDate"));
                writer.field("주소", value(complainant, "address"));
                writer.field("전화번호", value(complainant, "phone"));
                writer.field("휴대전화", value(complainant, "mobilePhone"));
                writer.field("이메일", value(complainant, "email"));
                writer.field("수신 여부", booleanValue(complainant, "receiveStatus"));

                writer.section("피진정인");
                JsonNode respondent = document.respondent();
                writer.field("사업장명", value(respondent, "companyName"));
                writer.field("성명", value(respondent, "name"));
                writer.field("전화번호", value(respondent, "phone"));
                writer.field("주소", value(respondent, "address"));
                writer.field("사업 형태", choice(respondent, "businessType", "BUSINESS", "일반사업", "CONSTRUCTION", "건설업"));
                writer.field("근로자 수", value(respondent, "employeeCount"));

                writer.section("근로 사실");
                JsonNode facts = document.facts();
                writer.field("입사일", date(facts, "hireDate"));
                writer.field("퇴사일", date(facts, "resignationDate"));
                writer.field("재직 상태", choice(facts, "employmentStatus", "EMPLOYED", "재직", "RESIGNED", "퇴사"));
                writer.field("업무 내용", value(facts, "jobDescription"));
                writer.field("급여일", value(facts, "payDay"));
                writer.field("근로계약 형태", choice(facts, "contractType", "WRITTEN", "서면", "VERBAL", "구두"));
                writer.field("미지급 임금", money(facts, "unpaidWages"));
                writer.field("미지급 퇴직금", money(facts, "unpaidSeverancePay"));
                writer.field("기타 미지급액", money(facts, "unpaidOtherAmount"));

                writer.section("진정 내용");
                DocumentDetailResponse.Content content = document.content();
                writer.field("관할 노동청", content == null ? "" : empty(content.targetLaborOffice()));
                writer.field("미지급 합계", content == null ? "" : money(content.totalUnpaidAmount()));
                writer.field("진정 사유", content == null ? "" : empty(content.claimReason()));
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate complaint PDF", e);
        }
    }

    private static String value(JsonNode node, String name) {
        if (node == null || node.path(name).isNull() || node.path(name).isMissingNode()) return "";
        return node.path(name).asText();
    }

    private static String empty(String value) {
        return value == null ? "" : value;
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

    private static String booleanValue(JsonNode node, String name) {
        if (node == null || !node.path(name).isBoolean()) return "";
        return node.path(name).booleanValue() ? "예" : "아니오";
    }

    private static String choice(JsonNode node, String name, String first, String firstLabel,
                                 String second, String secondLabel) {
        String value = value(node, name);
        if (value.equals(first)) return firstLabel;
        if (value.equals(second)) return secondLabel;
        return value;
    }

    private static String money(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.path(name);
        return value != null && value.isNumber() ? money(value.decimalValue()) : "";
    }

    private static String money(BigDecimal value) {
        return value == null ? "" : NumberFormat.getNumberInstance(Locale.KOREA).format(value) + "원";
    }

    private static final class Writer implements AutoCloseable {
        private final PDDocument document;
        private final PDType0Font font;
        private PDPageContentStream stream;
        private float y;

        private Writer(PDDocument document, PDType0Font font) throws IOException {
            this.document = document;
            this.font = font;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void title(String text) throws IOException {
            line(text, 17);
            y -= LINE_HEIGHT;
        }

        private void section(String text) throws IOException {
            y -= 8;
            line("■ " + text, 12);
            y -= 3;
        }

        private void field(String label, String value) throws IOException {
            String prefix = label + ": ";
            float width = PDRectangle.A4.getWidth() - MARGIN * 2;
            String[] paragraphs = value.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
            boolean first = true;
            for (String paragraph : paragraphs) {
                String line = first ? prefix : "";
                for (int offset = 0; offset < paragraph.length();) {
                    int codePoint = paragraph.codePointAt(offset);
                    String character = new String(Character.toChars(codePoint));
                    if (!line.isEmpty() && font.getStringWidth(line + character) * FONT_SIZE / 1000 > width) {
                        line(line, FONT_SIZE);
                        line = "";
                    }
                    line += character;
                    offset += Character.charCount(codePoint);
                }
                line(line, FONT_SIZE);
                first = false;
            }
        }

        private void line(String text, float size) throws IOException {
            if (y < MARGIN + LINE_HEIGHT) newPage();
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(text);
            stream.endText();
            y -= LINE_HEIGHT;
        }

        @Override
        public void close() throws IOException {
            if (stream != null) stream.close();
        }
    }
}
