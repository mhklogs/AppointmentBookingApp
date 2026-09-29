# AppointmentBookingApp — Market Analysis

> **Evidence base.** This document was researched on 2026-09-29 from vendor pricing pages,
> published analyst figures and the owner's market-review work (2026-09-26). No number
> here is invented. Where a figure could not be independently verified it is marked
> **[TO BE VALIDATED]**; verify it before the document is used in an investor or
> grant setting. Sources are listed in §8.

## 1. Product in one sentence

> Slottr — a Firebase-backed Android appointment-booking app (Kotlin, Material 3) with admin dashboard, booking, waitlist and reminders.

## 2. Problem statement

- **Who feels the problem:** Independent clinics, salons, tutors and home-service providers.
- **What they do today instead:** manual processes, spreadsheets, rented SaaS — see §4.
- **Cost of the status quo:** measurable in lost revenue / manual labor overhead
  **[TO BE VALIDATED for this specific segment]**.

## 3. Market definition

| Field | Value |
| --- | --- |
| Category | Appointment scheduling (SMB / professional services) |
| Geographic scope | Global (primary: Pakistan / English-speaking markets) |
| Target segment / persona | Independent clinics, salons, tutors and home-service providers |
| Estimated total addressable market | US online-booking/admissions platforms were a multi-$1B category; bookings segment commoditized **[TO BE VALIDATED — cite a specific figure]** |
| Serviceable addressable market | Depends on distribution reach; **[TO BE VALIDATED]** |
| Beachhead segment | Independent clinics, salons, tutors and home-service providers |

## 4. Demand signals

> SMB booking software is a proven, high-intent category; Google search demand dominated by incumbent names

| Signal | Evidence | Status |
| --- | --- | --- |
| Category demand | Mature/validated category with well-funded entrants | Confirmed |
| Competitive floor | Incumbent pricing and free tiers are public and low | Confirmed (see §5) |
| Own sales/usage data | Not instrumented in this repo | **[TO BE MEASURED]** |

## 5. Competitive landscape

| Competitor | Entry price (2026) | Positioning | Weakness we can exploit |
| --- | --- | --- | --- |
| **Calendly** | Free – $12–16/user/mo | Solo scheduling, wide integrations | No waitlist/check-in, no per-business branding |
| **Acuity (Squarespace)** | $14–$46/mo | Robust client-self-serve | Monthly cost builds |
| **Square Appointments** | Free – ~$29+/mo | POS + bookings tied to payments | US-centric payments lock-in |
| **Setmore** | Free – ~$9/user/mo | Simple bookings | No offline capability |
| **SimplyBook.me** | From ~$9.9/mo | Custom bookings + extras | Feature sprawl |

## 6. Differentiation

Grounded in what this build actually does (see `06-architecture.md`):

- **Distinctive capability in code:** Client-delivered, branded booking app (offline-capable Kotlin app, Firebase-backed, built-in reminder scheduling and waitlist) shipped as a signed APK; incumbents are multi-tenant SaaS you rent, not own.
- **Capability a competitor would need to replicate:** proxy of the build's core path.
- **Why defensible:** depth of vertical fit and delivery ownership, not a generic dashboard.

## 7. Risks

| Risk | Likelihood | Impact | Mitigation |
| --- | --- | --- | --- |
| Category commoditized / incumbent floor falling | Medium–High | Medium | Position on differentiation above, not price |
| Unverified market figures | High | High | Keep `[TO BE VALIDATED]` markers until sourced |
| Claims ahead of code (demo vs. shipped) | Medium | High | Keep README/copy aligned with the source tree |

## 8. Sources

Accessed 2026-09-29; vendor pricing changes — re-verify before any pricing decision.

- https://calendly.com/pricing
- https://squareup.com/us/en/appointments
- https://setmore.com/pricing
- https://simplybook.me/en/pricing
- https://www.acuityscheduling.com/pricing/
