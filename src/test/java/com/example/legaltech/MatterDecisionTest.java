package com.example.legaltech;

public final class MatterDecisionTest {
    public static void main(String[] args) {
        MatterIntake.Decision decision = new MatterIntake().decide(
                new LegalMatter("M-test", "Learner", false, "2026-11-12"));
        if (decision.readyForDelivery()) throw new AssertionError("unsigned matter must not be delivered");
        if (!decision.followUpRequired()) throw new AssertionError("open deadline must require follow-up");
        System.out.println("MatterDecisionTest passed");
    }
}
