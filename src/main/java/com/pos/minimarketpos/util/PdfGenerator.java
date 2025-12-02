package com.pos.minimarketpos.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.pos.minimarketpos.dto.response.CashflowDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb EVEN_ROW_COLOR = new DeviceRgb(236, 240, 241);

    /**
     * Generate PDF file to disk
     */
    public static void generateCashflowPdf(String dest, List<CashflowDTO> data, String title) {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] pdfBytes = generateCashflowPdfBytes(data, title);
            fos.write(pdfBytes);
            log.info("PDF generated successfully: {}", dest);
        } catch (IOException e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF as byte array (for download response)
     */
    public static byte[] generateCashflowPdfBytes(List<CashflowDTO> data, String title) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Set margins
            document.setMargins(40, 40, 40, 40);

            // Add header
            addHeader(document, title);

            // Add summary section
            addSummary(document, data);

            // Add table
            addCashflowTable(document, data);

            // Add footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF bytes: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private static void addHeader(Document document, String title) {
        // Company name or logo area
        Paragraph companyName = new Paragraph("MINIMARKET POS")
                .setFontSize(24)
                .setBold()
                .setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(companyName);

        // Report title
        Paragraph reportTitle = new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        document.add(reportTitle);

        // Generated timestamp
        Paragraph timestamp = new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20);
        document.add(timestamp);

        // Separator line
        document.add(new Paragraph().setBorderBottom(new SolidBorder(HEADER_COLOR, 2)));
    }

    private static void addSummary(Document document, List<CashflowDTO> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;

        for (CashflowDTO dto : data) {
            if (dto.getAmount() != null) {
                if (dto.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
                    totalInflow = totalInflow.add(dto.getAmount());
                } else {
                    totalOutflow = totalOutflow.add(dto.getAmount().abs());
                }
            }
        }

        BigDecimal netCashflow = totalInflow.subtract(totalOutflow);

        // Summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(50))
                .setMarginTop(10)
                .setMarginBottom(20);

        addSummaryRow(summaryTable, "Total Records:", String.valueOf(data.size()));
        addSummaryRow(summaryTable, "Total Inflow:", formatCurrency(totalInflow));
        addSummaryRow(summaryTable, "Total Outflow:", formatCurrency(totalOutflow));
        addSummaryRow(summaryTable, "Net Cashflow:", formatCurrency(netCashflow),
                netCashflow.compareTo(BigDecimal.ZERO) >= 0);

        document.add(summaryTable);
    }

    private static void addSummaryRow(Table table, String label, String value) {
        addSummaryRow(table, label, value, true);
    }

    private static void addSummaryRow(Table table, String label, String value, boolean positive) {
        table.addCell(new Cell().add(new Paragraph(label).setBold())
                .setBorder(null));

        Paragraph valuePara = new Paragraph(value).setBold();
        if (!positive) {
            valuePara.setFontColor(ColorConstants.RED);
        }

        table.addCell(new Cell().add(valuePara).setBorder(null));
    }

    private static void addCashflowTable(Document document, List<CashflowDTO> data) {
        // Create table with appropriate column widths
        float[] columnWidths = {1.5f, 2f, 2f, 2f, 1.5f, 2f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth()
                .setMarginTop(10);

        // Add header cells
        addHeaderCell(table, "Date");
        addHeaderCell(table, "Reference");
        addHeaderCell(table, "Category");
        addHeaderCell(table, "Store");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Amount");

        // Add data rows
        if (data == null || data.isEmpty()) {
            table.addCell(new Cell(1, 6)
                    .add(new Paragraph("No data available").setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(EVEN_ROW_COLOR));
        } else {
            int rowIndex = 0;
            for (CashflowDTO dto : data) {
                boolean evenRow = rowIndex % 2 == 0;
                addDataRow(table, dto, evenRow);
                rowIndex++;
            }
        }

        document.add(table);
    }

    private static void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8);
        table.addHeaderCell(cell);
    }

    private static void addDataRow(Table table, CashflowDTO dto, boolean evenRow) {
        DeviceRgb bgColor = evenRow ? EVEN_ROW_COLOR : (DeviceRgb) ColorConstants.WHITE;

        // Date

        addDataCell(table, dto.getDate() != null ? dto.getDate().format(DATE_FORMATTER) : "-",
                bgColor, TextAlignment.CENTER);

        // Reference
        addDataCell(table, dto.getReference() != null ? dto.getReference() : "-",
                bgColor, TextAlignment.LEFT);

        // Category
        addDataCell(table, dto.getCategoryName() != null ? dto.getCategoryName() : "-",
                bgColor, TextAlignment.LEFT);

        // Store
        addDataCell(table, dto.getStoreName() != null ? dto.getStoreName() : "-",
                bgColor, TextAlignment.LEFT);

        // Status (Lunas)
        String status = dto.getLunas() != null && dto.getLunas() ? "Paid" : "Unpaid";
        DeviceRgb statusColor = dto.getLunas() != null && dto.getLunas()
                ? new DeviceRgb(46, 204, 113)
                : new DeviceRgb(231, 76, 60);
        Cell statusCell = new Cell()
                .add(new Paragraph(status).setFontColor(statusColor).setBold())
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(statusCell);

        // Amount
        String amountText = dto.getAmount() != null ? formatCurrency(dto.getAmount()) : formatCurrency(BigDecimal.ZERO);
        DeviceRgb amountColor = (DeviceRgb) (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) < 0
                        ? ColorConstants.RED
                        : ColorConstants.BLACK);
        Cell amountCell = new Cell()
                .add(new Paragraph(amountText).setFontColor(amountColor).setBold())
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
        table.addCell(amountCell);
    }

    private static void addDataCell(Table table, String text, DeviceRgb bgColor, TextAlignment alignment) {
        Cell cell = new Cell()
                .add(new Paragraph(text))
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment)
                .setPadding(5);
        table.addCell(cell);
    }

    private static void addFooter(Document document) {
        Paragraph footer = new Paragraph("\nThis is a computer-generated document. No signature is required.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(30);
        document.add(footer);
    }

    private static String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}