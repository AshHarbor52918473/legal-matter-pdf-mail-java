package com.example.legaltech;

public final class MatterIntake {
    public Decision decide(LegalMatter matter) {
        boolean followUpRequired = !matter.signed() || matter.deadline() != null;
        boolean readyForDelivery = matter.signed();
        return new Decision(readyForDelivery, followUpRequired);
    }

    public record Decision(boolean readyForDelivery, boolean followUpRequired) {}
}
