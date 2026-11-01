package com.example.legaltech;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PdfReport {
    public String create(LegalMatter matter, MatterIntake.Decision decision) {
        String text = "Matter " + matter.matterId() + " | learner " + matter.learnerName()
                + " | signed " + matter.signed() + " | deadline " + matter.deadline()
                + " | follow-up " + decision.followUpRequired();
        String pdf = "%PDF-1.4\n1 0 obj<<>>endobj\n2 0 obj<< /Length " + text.length()
                + " >>stream\n" + text + "\nendstream\nendobj\n%%EOF";
        return Base64.getEncoder().encodeToString(pdf.getBytes(StandardCharsets.US_ASCII));
    }
}
