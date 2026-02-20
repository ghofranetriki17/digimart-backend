package com.nexashop.api.controller.audit;

import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.audit.AuditLogResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.AuditLogUseCase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-events")
public class AuditLogController {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter PRINT_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int EXPORT_LIMIT = 10_000;

    private final AuditLogUseCase auditLogUseCase;

    public AuditLogController(AuditLogUseCase auditLogUseCase) {
        this.auditLogUseCase = auditLogUseCase;
    }

    @GetMapping("/paged")
    public PageResponse<AuditLogResponse> listAuditEvents(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                auditLogUseCase.listAuditEvents(request, tenantId, entityType, action, from, to),
                this::toResponse
        );
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) throws IOException {
        List<AuditLogUseCase.AuditLogEntry> entries = auditLogUseCase.listAuditEventsForExport(
                tenantId,
                entityType,
                action,
                from,
                to,
                EXPORT_LIMIT
        );
        byte[] bytes = toCsv(entries);
        String fileName = "audit-log-" + LocalDateTime.now().format(FILE_TS) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) throws IOException {
        List<AuditLogUseCase.AuditLogEntry> entries = auditLogUseCase.listAuditEventsForExport(
                tenantId,
                entityType,
                action,
                from,
                to,
                EXPORT_LIMIT
        );
        byte[] bytes = toPdf(entries);
        String fileName = "audit-log-" + LocalDateTime.now().format(FILE_TS) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private AuditLogResponse toResponse(AuditLogUseCase.AuditLogEntry entry) {
        return AuditLogResponse.builder()
                .id(entry.id())
                .tenantId(entry.tenantId())
                .tenantName(entry.tenantName())
                .entityType(entry.entityType())
                .entityId(entry.entityId())
                .action(entry.action())
                .beforeJson(entry.beforeJson())
                .afterJson(entry.afterJson())
                .actorUserId(entry.actorUserId())
                .actorEmail(entry.actorEmail())
                .actorName(entry.actorName())
                .correlationId(entry.correlationId())
                .occurredAt(entry.occurredAt())
                .createdAt(entry.createdAt())
                .build();
    }

    private byte[] toCsv(List<AuditLogUseCase.AuditLogEntry> entries) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(output, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader(
                                "id",
                                "occurredAt",
                                "action",
                                "entityType",
                                "entityId",
                                "tenantName",
                                "tenantId",
                                "actorUserId",
                                "actorName",
                                "actorEmail",
                                "correlationId",
                                "beforeJson",
                                "afterJson"
                        )
                        .build()
        )) {
            for (AuditLogUseCase.AuditLogEntry entry : entries) {
                printer.printRecord(
                        entry.id(),
                        formatDate(entry.occurredAt()),
                        entry.action(),
                        entry.entityType(),
                        entry.entityId(),
                        entry.tenantName(),
                        entry.tenantId(),
                        entry.actorUserId(),
                        entry.actorName(),
                        entry.actorEmail(),
                        entry.correlationId(),
                        entry.beforeJson(),
                        entry.afterJson()
                );
            }
        }
        return output.toByteArray();
    }

    private byte[] toPdf(List<AuditLogUseCase.AuditLogEntry> entries) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 36f;
            float y = page.getMediaBox().getHeight() - margin;
            float maxWidth = page.getMediaBox().getWidth() - (2 * margin);
            float lineHeight = 12f;

            PDPageContentStream stream = new PDPageContentStream(document, page);
            stream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            y = writeLine(stream, margin, y, "Audit Journal Export", lineHeight);
            stream.setFont(PDType1Font.HELVETICA, 9);
            y = writeLine(stream, margin, y, "Generated at: " + formatDate(LocalDateTime.now()), lineHeight);
            y = writeLine(stream, margin, y, "Rows: " + entries.size(), lineHeight);
            y -= lineHeight;

            for (AuditLogUseCase.AuditLogEntry entry : entries) {
                if (y < margin + (lineHeight * 8)) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    stream.setFont(PDType1Font.HELVETICA, 9);
                    y = page.getMediaBox().getHeight() - margin;
                }
                String summary = String.format(
                        "%s | %s | %s#%s | tenant=%s | actor=%s",
                        formatDate(entry.occurredAt()),
                        safe(entry.action()),
                        safe(entry.entityType()),
                        entry.entityId(),
                        safe(entry.tenantName(), String.valueOf(entry.tenantId())),
                        safe(entry.actorEmail(), String.valueOf(entry.actorUserId()))
                );
                y = writeWrapped(stream, margin, y, summary, maxWidth, lineHeight);
                y = writeWrapped(stream, margin, y, "before: " + compactJson(entry.beforeJson()), maxWidth, lineHeight);
                y = writeWrapped(stream, margin, y, "after: " + compactJson(entry.afterJson()), maxWidth, lineHeight);
                y -= lineHeight * 0.5f;
            }

            stream.close();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }
    }

    private float writeLine(PDPageContentStream stream, float x, float y, String text, float lineHeight) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
        return y - lineHeight;
    }

    private float writeWrapped(
            PDPageContentStream stream,
            float x,
            float y,
            String text,
            float maxWidth,
            float lineHeight
    ) throws IOException {
        String[] words = safe(text).split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.isEmpty() ? word : (line + " " + word);
            float width = PDType1Font.HELVETICA.getStringWidth(candidate) / 1000f * 9f;
            if (width > maxWidth && !line.isEmpty()) {
                y = writeLine(stream, x, y, line.toString(), lineHeight);
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (!line.isEmpty()) {
            y = writeLine(stream, x, y, line.toString(), lineHeight);
        }
        return y;
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(PRINT_TS);
    }

    private String compactJson(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String compact = value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();
        if (compact.length() > 220) {
            return compact.substring(0, 220) + "...";
        }
        return compact;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safe(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null ? "" : fallback;
    }
}
