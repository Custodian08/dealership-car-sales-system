package com.dealership.service;

import com.dealership.domain.Sale;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;
import com.dealership.dto.SaleSummaryDto;

@Service
public class PdfService {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.20"); // 20%

    public byte[] generateSaleContract(Sale sale) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font[] ru = loadRuFonts();
            Font titleFont = ru[0];
            Font labelFont = ru[1];
            Font normalFont = ru[2];

            Paragraph title = new Paragraph("Договор купли-продажи автомобиля", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12f);
            doc.add(title);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            doc.add(new Paragraph("Дата договора: " + sale.getSaleDate().format(dtf), normalFont));
            doc.add(new Paragraph("Продавец: " + safe(sale.getSalespersonUsername()), normalFont));
            doc.add(Chunk.NEWLINE);

            // Customer block
            doc.add(new Paragraph("Покупатель", labelFont));
            PdfPTable cust = new PdfPTable(2);
            cust.setWidths(new float[]{1.5f, 3f});
            cust.setWidthPercentage(100);
            addRow(cust, "ФИО/Название", sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName());
            addRow(cust, "Email", safe(sale.getCustomer().getEmail()));
            addRow(cust, "Телефон", safe(sale.getCustomer().getPhone()));
            addRow(cust, "Адрес", safe(sale.getCustomer().getAddress()));
            cust.setSpacingAfter(10f);
            doc.add(cust);

            // Vehicle block
            doc.add(new Paragraph("Автомобиль", labelFont));
            PdfPTable veh = new PdfPTable(2);
            veh.setWidths(new float[]{1.5f, 3f});
            veh.setWidthPercentage(100);
            addRow(veh, "VIN", safe(sale.getVehicle().getVin()));
            addRow(veh, "Марка", safe(sale.getVehicle().getMake()));
            addRow(veh, "Модель", safe(sale.getVehicle().getModel()));
            addRow(veh, "Год", String.valueOf(sale.getVehicle().getYear()));
            veh.setSpacingAfter(10f);
            doc.add(veh);

            // Price block (assume gross includes VAT). VAT portion = gross * rate / (1 + rate)
            BigDecimal gross = sale.getPrice() != null ? sale.getPrice() : BigDecimal.ZERO;
            BigDecimal vatPortion = gross.multiply(VAT_RATE).divide(BigDecimal.ONE.add(VAT_RATE), 2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(vatPortion);

            doc.add(new Paragraph("Стоимость", labelFont));
            PdfPTable priceTable = new PdfPTable(2);
            priceTable.setWidths(new float[]{2f, 2f});
            priceTable.setWidthPercentage(100);
            addRow(priceTable, "Сумма без НДС", money(net));
            addRow(priceTable, "НДС 20% (включено)", money(vatPortion));
            addRow(priceTable, "Итого к оплате", money(gross));
            priceTable.setSpacingAfter(14f);
            doc.add(priceTable);

            doc.add(new Paragraph("Подписывая настоящий договор, покупатель соглашается приобрести автомобиль на указанных выше условиях.", normalFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            sign.setWidths(new float[]{1f, 1f});
            addRow(sign, "Подпись продавца", "________________________");
            addRow(sign, "Подпись покупателя", "________________________");
            doc.add(sign);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сформировать PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateSalesReport(List<Sale> sales, SaleSummaryDto summary, LocalDate from, LocalDate to) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font[] ru = loadRuFonts();
            Font titleFont = ru[0];
            Font bold = ru[1];
            Font normal = ru[2];

            Paragraph title = new Paragraph("Отчет по продажам", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8f);
            doc.add(title);

            String range = (from != null ? from.toString() : "-") + " — " + (to != null ? to.toString() : "-");
            doc.add(new Paragraph("Период: " + range, normal));
            doc.add(Chunk.NEWLINE);

            // Summary
            PdfPTable sum = new PdfPTable(2);
            sum.setWidths(new float[]{2f, 2f});
            sum.setWidthPercentage(40);
            sum.addCell(new Phrase("Количество продаж", bold));
            sum.addCell(new Phrase(String.valueOf(summary.totalSales()), normal));
            sum.addCell(new Phrase("Выручка", bold));
            sum.addCell(new Phrase(money(summary.totalRevenue()), normal));
            sum.setSpacingAfter(10f);
            doc.add(sum);

            // Table header
            PdfPTable t = new PdfPTable(9);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{2.2f, 2.5f, 1.6f, 2.2f, 1.1f, 2.6f, 3.2f, 1.6f, 2.2f});
            t.addCell(new Phrase("Дата", bold));
            t.addCell(new Phrase("VIN", bold));
            t.addCell(new Phrase("Марка", bold));
            t.addCell(new Phrase("Модель", bold));
            t.addCell(new Phrase("Год", bold));
            t.addCell(new Phrase("Покупатель", bold));
            t.addCell(new Phrase("Email", bold));
            t.addCell(new Phrase("Цена", bold));
            t.addCell(new Phrase("Продавец", bold));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Sale s : sales) {
                String customer = safe(s.getCustomer().getFirstName()) + " " + safe(s.getCustomer().getLastName());
                t.addCell(new Phrase(s.getSaleDate() != null ? s.getSaleDate().format(dtf) : "", normal));
                t.addCell(new Phrase(safe(s.getVehicle().getVin()), normal));
                t.addCell(new Phrase(safe(s.getVehicle().getMake()), normal));
                t.addCell(new Phrase(safe(s.getVehicle().getModel()), normal));
                t.addCell(new Phrase(String.valueOf(s.getVehicle().getYear()), normal));
                t.addCell(new Phrase(customer, normal));
                t.addCell(new Phrase(safe(s.getCustomer().getEmail()), normal));
                t.addCell(new Phrase(money(s.getPrice() != null ? s.getPrice() : BigDecimal.ZERO), normal));
                t.addCell(new Phrase(safe(s.getSalespersonUsername()), normal));
            }
            doc.add(t);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сформировать PDF отчета о продажах: " + e.getMessage(), e);
        }
    }

    private static void addRow(PdfPTable table, String key, String val) {
        Font[] ru = loadRuFonts();
        Font bold = ru[1];
        Font normal = ru[2];
        table.addCell(new Phrase(key, bold));
        table.addCell(new Phrase(val != null ? val : "", normal));
    }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString() + "";
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static Font[] loadRuFonts() {
        try (InputStream in = PdfService.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            if (in != null) {
                Path tmp = Files.createTempFile("dejavu", ".ttf");
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                BaseFont bf = BaseFont.createFont(tmp.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font title = new Font(bf, 16, Font.BOLD);
                Font bold = new Font(bf, 11, Font.BOLD);
                Font normal = new Font(bf, 11);
                return new Font[]{title, bold, normal};
            }
        } catch (Exception ignore) { }
        // Try common system fonts by OS (support Cyrillic)
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            java.util.List<String> candidates = new java.util.ArrayList<>();
            if (os.contains("win")) {
                candidates = java.util.List.of(
                        "C:/Windows/Fonts/arial.ttf",
                        "C:/Windows/Fonts/segoeui.ttf",
                        "C:/Windows/Fonts/tahoma.ttf"
                );
            } else if (os.contains("mac")) {
                candidates = java.util.List.of(
                        "/System/Library/Fonts/Supplemental/Arial.ttf",
                        "/Library/Fonts/Arial.ttf",
                        "/System/Library/Fonts/Supplemental/Tahoma.ttf"
                );
            } else {
                candidates = java.util.List.of(
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
                        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
                );
            }
            for (String p : candidates) {
                try {
                    BaseFont bf = BaseFont.createFont(p, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font title = new Font(bf, 16, Font.BOLD);
                    Font bold = new Font(bf, 11, Font.BOLD);
                    Font normal = new Font(bf, 11);
                    return new Font[]{title, bold, normal};
                } catch (Exception ignoreOne) { /* try next */ }
            }
        } catch (Exception ignore) { }
        // Fallback: Helvetica (может не поддерживать кириллицу)
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
        return new Font[]{title, bold, normal};
    }
}
