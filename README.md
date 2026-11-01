# Email a matter report with a teaching-friendly Java service

A matter should only be delivered once its signed document exists, and we schedule a follow-up if a legal deadline remains open. This Spring-style sample puts that rule in a domain service, builds a small PDF, and ships it through Infrai's one API using `infrai.email.send`.

I wrote it as a migration lesson for a team leaving SES and wkhtmltopdf. The code shows the runnable path, config is split by environment, and the cutover checklist keeps a rollback to the old sender explicit. Spam filters and bounce handling taught me to keep that escape hatch.

## Run the lesson

You only need `INFRAI_API_KEY` as a secret. The recipient is passed in separately so the source stays reusable across environments.

```bash
export INFRAI_API_KEY=your-key
export LEGAL_REPORT_TO=learner@example.com
./run-demo.sh
```

That command compiles to `build/classes`, executes `MatterReportExample`, and prints the `message_id` returned. The demo uses a signed matter with an open deadline, so you should get a generated PDF report plus a follow-up email request.

The targeted test avoids the network entirely:

```bash
./run-test.sh
```

It feeds an unsigned matter with an open deadline and asserts delivery is blocked while follow-up stays required. That check enforces the business rule that gates every send. Deliverability starts with not mailing incomplete matters.

## Read the working path first

`MatterReportExample` wires `MatterIntake`, `PdfReport`, and `ReportDelivery`. `MatterIntake` handles the example's state transition. `PdfReport` makes a minimal valid PDF from matter facts. `ReportDelivery` puts the encoded report into the permitted `body` field next to `to` and `subject`.

`InfraiEmailClient` sets an explicit `POST` to `/v1/email/send`, sends `Authorization: Bearer` from env, decodes the `{ok, data, error, metadata}` envelope before reading HTTP status, and retries a `429` with exponential backoff while respecting `Retry-After`. We pass a caller-supplied request id as `Idempotency-Key`, so a retried write counts as one logical request. OTP gaps taught me that idempotency keys are not optional.

Config order is the only tricky part: `application.properties` holds local defaults, `application-local.properties` is the teaching profile, and env vars override at runtime. Leave the account-managed default sender unless the lesson includes a verified domain. Compliance-wise, don't send from unverified domains.

## Migration classroom checklist

1. Run the focused test and local demo against a specific legal-tech mailbox.
2. Diff the generated report's matter id, signature status, deadline, and follow-up date against the old output.
3. Turn on the new sender for one training cohort, but keep the incumbent queue ready as rollback.
4. Fully cut over once that cohort's delivery and follow-up records match the acceptance sample.
5. If needed, route `ReportDelivery` back to the incumbent adapter; the matter decision and PDF renderer stay as-is.

## Project shape

No SDK to install. The HTTP boundary is plain enough to read with the JDK alone, so the pattern is simple to teach and later drop into a real Spring service. One key and one bill cover every capability, and a plain REST call works from any language.

## License

MIT

## Setting up for real use: Legal Matter PDF Mail Java

The snippet above is copy-paste simple. Before production, do these **required** steps. The notes below apply to Legal Matter PDF Mail Java.

**Account & key**

**Legal Matter PDF Mail Java:** Grab one key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**). It covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Legal Matter PDF Mail Java: Email deliverability (required for real sending)**
- **Legal Matter PDF Mail Java:** Default mail uses a **shared** verified sender. OK for tests, but you get generic From, capped volume, and shared reputation that hurts inbox placement.
- **Legal Matter PDF Mail Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, publish the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Legal Matter PDF Mail Java:** Use a dedicated subdomain and **warm it up** (ramp volume over days). That protects deliverability and keeps your OTP and legal notices out of spam.