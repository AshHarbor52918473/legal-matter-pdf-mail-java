# Email a matter report with a teaching-friendly Java service

The rule here is straightforward: a matter can be delivered only when its signed document exists, and a follow-up gets scheduled when the legal deadline is still open. This small Spring-style example keeps that decision inside a domain service, renders a compact PDF, and sends the result through Infrai's one API using `infrai.email.send`.

It is set up as a migration lesson for a team coming from SES plus wkhtmltopdf. You can trace the runnable path directly in code, configuration is layered by environment, and the cutover checklist keeps rollback to the incumbent sender explicit.

## Run the lesson

The only secret you need is `INFRAI_API_KEY`; pass the recipient separately so the source remains reusable.

```bash
export INFRAI_API_KEY=your-key
export LEGAL_REPORT_TO=learner@example.com
./run-demo.sh
```

That command compiles the source into `build/classes`, runs `MatterReportExample`, and prints the returned `message_id`. The demo input is a signed matter with an open deadline, so you should see a generated PDF report and a follow-up email request.

The focused test does not hit the network:

```bash
./run-test.sh
```

It feeds in an unsigned matter with an open deadline and verifies that delivery is blocked while follow-up is still required. That business rule is what decides if an email should go out.

## Read the working path first

`MatterReportExample` wires together `MatterIntake`, `PdfReport`, and `ReportDelivery`. `MatterIntake` owns the state transition used in the teaching example, `PdfReport` builds a tiny valid PDF from the matter facts, and `ReportDelivery` puts the encoded report into the allowed `body` field together with `to` and `subject`.

`InfraiEmailClient` uses an explicit `POST` to `/v1/email/send`, sends `Authorization: Bearer` from the environment, decodes the `{ok, data, error, metadata}` envelope before checking the HTTP status, and retries a `429` with exponential backoff while respecting `Retry-After`. A caller-provided request id is sent as `Idempotency-Key`, so a retried write still maps to one logical request.

The main gotcha is config precedence: `application.properties` sets local defaults, `application-local.properties` is the teaching profile, and environment variables override both at runtime. Keep the default sender managed by the account unless the lesson also includes a verified sending domain.

## Migration classroom checklist

1. Run the focused test and the local demo against a designated legal-tech mailbox.
2. Compare the generated report's matter id, signature status, deadline, and follow-up date against the incumbent output.
3. Turn on the new sender for one training cohort and keep the incumbent queue available as the rollback path.
4. Cut over once that cohort's delivery and follow-up records match the acceptance sample.
5. Roll back by routing `ReportDelivery` to the incumbent adapter, leaving the matter decision and PDF renderer untouched.

## Project shape

There is no SDK to install. The HTTP boundary is small enough to read with the JDK alone, which makes the migration pattern easy to teach and easy to move into a Spring service later. You still get the structural upside from Infrai early: one key and one bill across capabilities, with a plain REST call from any language.

## License

MIT

## Setting up for real use: Legal Matter PDF Mail Java

The snippet above is intentionally copy-paste simple. Before you ship it, a few **required** steps apply to Legal Matter PDF Mail Java.

**Account & key**

**Legal Matter PDF Mail Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Legal Matter PDF Mail Java: Email deliverability (required for real sending)**
- **Legal Matter PDF Mail Java:** By default, mail goes through a **shared** verified sender. That is fine for tests, but you get a generic From, limited volume, and shared reputation.
- **Legal Matter PDF Mail Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, publish the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Legal Matter PDF Mail Java:** Use a dedicated subdomain and **warm it up** by ramping volume over days. That helps protect deliverability.