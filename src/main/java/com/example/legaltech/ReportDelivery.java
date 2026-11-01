package com.example.legaltech;

import java.io.IOException;

public final class ReportDelivery {
    private final InfraiEmailClient email;
    private final PdfReport pdf;

    public ReportDelivery(InfraiEmailClient email, PdfReport pdf) { this.email = email; this.pdf = pdf; }

    public String deliver(LegalMatter matter, MatterIntake.Decision decision, String recipient) throws IOException, InterruptedException {
        if (!decision.readyForDelivery()) throw new IllegalStateException("signed document is required");
        String body = "Signed PDF report (base64):\n" + pdf.create(matter, decision);
        return email.send(recipient, "Matter " + matter.matterId() + " report", body, "matter-" + matter.matterId());
    }
}
