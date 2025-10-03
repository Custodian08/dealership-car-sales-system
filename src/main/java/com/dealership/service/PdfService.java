package com.dealership.service;

import com.dealership.domain.Sale;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph title = new Paragraph("Vehicle Sale Contract", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12f);
            doc.add(title);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            doc.add(new Paragraph("Contract Date: " + sale.getSaleDate().format(dtf), normalFont));
            doc.add(new Paragraph("Salesperson: " + safe(sale.getSalespersonUsername()), normalFont));
            doc.add(Chunk.NEWLINE);

            // Customer block
            doc.add(new Paragraph("Customer", labelFont));
            PdfPTable cust = new PdfPTable(2);
            cust.setWidths(new float[]{1.5f, 3f});
            cust.setWidthPercentage(100);
            addRow(cust, "Name", sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName());
            addRow(cust, "Email", safe(sale.getCustomer().getEmail()));
            addRow(cust, "Phone", safe(sale.getCustomer().getPhone()));
            addRow(cust, "Address", safe(sale.getCustomer().getAddress()));
            cust.setSpacingAfter(10f);
            doc.add(cust);

            // Vehicle block
            doc.add(new Paragraph("Vehicle", labelFont));
            PdfPTable veh = new PdfPTable(2);
            veh.setWidths(new float[]{1.5f, 3f});
            veh.setWidthPercentage(100);
            addRow(veh, "VIN", safe(sale.getVehicle().getVin()));
            addRow(veh, "Make", safe(sale.getVehicle().getMake()));
            addRow(veh, "Model", safe(sale.getVehicle().getModel()));
            addRow(veh, "Year", String.valueOf(sale.getVehicle().getYear()));
            veh.setSpacingAfter(10f);
            doc.add(veh);

            // Price block (assume gross includes VAT). VAT portion = gross * rate / (1 + rate)
            BigDecimal gross = sale.getPrice() != null ? sale.getPrice() : BigDecimal.ZERO;
            BigDecimal vatPortion = gross.multiply(VAT_RATE).divide(BigDecimal.ONE.add(VAT_RATE), 2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(vatPortion);

            doc.add(new Paragraph("Pricing", labelFont));
            PdfPTable priceTable = new PdfPTable(2);
            priceTable.setWidths(new float[]{2f, 2f});
            priceTable.setWidthPercentage(100);
            addRow(priceTable, "Net (excl. VAT)", money(net));
            addRow(priceTable, "VAT 20% (included)", money(vatPortion));
            addRow(priceTable, "Total (gross)", money(gross));
            priceTable.setSpacingAfter(14f);
            doc.add(priceTable);

            doc.add(new Paragraph("By signing this contract, the customer agrees to purchase the vehicle under the terms above.", normalFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            sign.setWidths(new float[]{1f, 1f});
            addRow(sign, "Salesperson Signature", "________________________");
            addRow(sign, "Customer Signature", "________________________");
            doc.add(sign);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateSalesReport(List<Sale> sales, SaleSummaryDto summary, LocalDate from, LocalDate to) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph title = new Paragraph("Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8f);
            doc.add(title);

            String range = (from != null ? from.toString() : "-") + " to " + (to != null ? to.toString() : "-");
            doc.add(new Paragraph("Date range: " + range, normal));
            doc.add(Chunk.NEWLINE);

            // Summary
            PdfPTable sum = new PdfPTable(2);
            sum.setWidths(new float[]{2f, 2f});
            sum.setWidthPercentage(40);
            sum.addCell(new Phrase("Total sales count", bold));
            sum.addCell(new Phrase(String.valueOf(summary.totalSales()), normal));
            sum.addCell(new Phrase("Total revenue", bold));
            sum.addCell(new Phrase(money(summary.totalRevenue()), normal));
            sum.setSpacingAfter(10f);
            doc.add(sum);

            // Table header
            PdfPTable t = new PdfPTable(9);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{2.2f, 2.5f, 1.6f, 2.2f, 1.1f, 2.6f, 3.2f, 1.6f, 2.2f});
            t.addCell(new Phrase("Date", bold));
            t.addCell(new Phrase("VIN", bold));
            t.addCell(new Phrase("Make", bold));
            t.addCell(new Phrase("Model", bold));
            t.addCell(new Phrase("Year", bold));
            t.addCell(new Phrase("Customer", bold));
            t.addCell(new Phrase("Email", bold));
            t.addCell(new Phrase("Price", bold));
            t.addCell(new Phrase("Salesperson", bold));

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
            throw new RuntimeException("Failed to generate sales report PDF: " + e.getMessage(), e);
        }
    }

    private static void addRow(PdfPTable table, String key, String val) {
        table.addCell(new Phrase(key, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        table.addCell(new Phrase(val != null ? val : "", FontFactory.getFont(FontFactory.HELVETICA, 11)));
    }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString() + "";
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
