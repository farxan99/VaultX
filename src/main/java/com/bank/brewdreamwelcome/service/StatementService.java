package com.bank.brewdreamwelcome.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.bank.brewdreamwelcome.repository.TransactionRepository;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Enterprise reporting service for generating Bank Statements.
 */
public class StatementService {
    
    public void generateStatement(int customerId, String accNo, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        document.open();
        
        // 1. Modern Header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph title = new Paragraph("VaultX Bank Statement", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Account Number: " + accNo));
        document.add(Chunk.NEWLINE);

        // 2. Transaction Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        // Header Row
        String[] headers = {"Date", "Description", "Type", "Amount"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(cell);
        }

        // Dummy Data (In production, this comes from TransactionRepository)
        // List<Transaction> txs = txRepo.findByAccount(accNo);
        // Add logic to iterate and add rows...

        document.add(table);
        document.close();
        
        AuditService.log("GENERATE_STATEMENT", "PDF Generated for Account: " + accNo);
    }
}
