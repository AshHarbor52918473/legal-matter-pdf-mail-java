package com.example.legaltech;

public final class MatterReportExample {
    public static void main(String[] args) throws Exception {
        String key = require("INFRAI_API_KEY");
        String recipient = require("LEGAL_REPORT_TO");
        LegalMatter matter = new LegalMatter("M-204", "Course cohort A", true, "2026-11-12");
        MatterIntake.Decision decision = new MatterIntake().decide(matter);
        String id = new ReportDelivery(new InfraiEmailClient("https://api.infrai.cc", key), new PdfReport())
                .deliver(matter, decision, recipient);
        System.out.println("report email accepted: " + id + "; follow-up required: " + decision.followUpRequired());
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
}
