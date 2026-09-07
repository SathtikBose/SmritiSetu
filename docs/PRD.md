# Product Requirements Document — SmritiSetu

**AI-Based Cognitive Gaming and Memory Assistance Platform for Elderly Dementia Patients in North Eastern Region (NER)**

| | |
|---|---|
| **PS Code** | SIH26003 |
| **Sponsoring Ministry** | Ministry of Development of North Eastern Region (MDoNER) |
| **Track** | Software |
| **Document Version** | v1.0 — Combined (Demo + Full Solution) |
| **Related Docs** | `SystemDesign.md` (demo architecture) · `SmritiSetu-System-Design-Full.md` (production architecture) |

---

## 1. Purpose of This Document

This PRD defines **what SmritiSetu is, who it's for, and what it must do** — across two delivery phases:

- **Phase 1 — Demo (Internal Hackathon):** built in 1–1.5 weeks, vibe-coded, precisely covering every requirement in the official PS at demo-appropriate fidelity.
- **Phase 2 — Full Solution (SIH Final, target December):** the production-grade version, extending Phase 1 with real scale, security, and clinical rigor.

Every requirement below is tagged with which phase delivers it, so the team always knows what "done" means at each stage.

---

## 2. Background & Problem Statement

North Eastern Region (NER) is seeing a gradual rise in age-related cognitive disorders like dementia among the elderly. Remote and rural families face major barriers to specialized neurological care and cognitive therapy due to limited healthcare infrastructure and geography. Patients experience memory decline, confusion, anxiety, and social isolation; caregivers struggle with continuous monitoring and engagement. There is currently no affordable, culturally inclusive, AI-enabled digital therapeutic solution built specifically for elderly individuals in NER.

**Core problem:** Elderly dementia patients in NER lack accessible, engaging, and clinically-safe cognitive stimulation tools — and their caregivers lack visibility into how the patient is doing.

---

## 3. Goals

| Goal | Success looks like |
|---|---|
| Provide safe, adaptive cognitive stimulation | AI adjusts difficulty without ever overwhelming the patient |
| Make the platform usable by elderly NER users specifically | Voice + regional language + culturally familiar visuals, not a generic translated app |
| Give caregivers real visibility | A caregiver can answer "how is my patient doing, and why" in under 30 seconds in-app |
| Work despite poor connectivity | Core gameplay is not blocked by lack of internet |
| Support daily life, not just games | Reminders reduce missed medicines/hydration/appointments |

### Non-Goals (explicitly out of scope, both phases)
- Clinical diagnosis of dementia or its severity — this is a stimulation/engagement tool, not a diagnostic device.
- Replacing a neurologist, therapist, or medical professional.
- Real-time emergency response (e.g., fall detection, panic button) — not part of this PS.

---

## 4. Users & Personas

| Persona | Description | Primary needs |
|---|---|---|
| **Patient** | Elderly individual (60+) with mild-to-moderate dementia symptoms, living in NER, possibly with limited literacy or digital familiarity | Extremely simple UI, large touch targets, audio guidance, familiar visuals/language, non-stressful pacing |
| **Caregiver** | Family member or healthcare worker responsible for the patient's daily care | Quick progress visibility, understanding *why* the app is doing what it's doing, ability to set reminders, peace of mind |

---

## 5. Functional Requirements

Each requirement is mapped to the exact PS clause it satisfies, and tagged by phase.

### 5.1 Cognitive Games
| Requirement | Phase 1 (Demo) | Phase 2 (Full) |
|---|---|---|
| Memory improvement game | ✅ Memory Match | ✅ Same, expanded variants |
| Attention & concentration game | ✅ Sequence Recall | ✅ Same, expanded variants |
| Daily routine recall game | ✅ Daily Reasoning (NER context) | ✅ Same, expanded scenario library |
| Pattern/object recognition | ✅ Folded into Memory Match | ✅ Dedicated game mode |
| Emotional/mental engagement | ✅ Via familiar scenarios/visuals | ✅ Dedicated engagement scoring & content variety |

### 5.2 AI Adaptive Difficulty
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Adapts difficulty to performance | ✅ Rule-based, bounded, evaluated every 5 levels | ✅ Same rules initially, evolving to trained model |
| Adapts to cognitive condition | ✅ Via trend detection across windows | ✅ Enriched with clinician-tagged condition data (optional caregiver input) |
| Explainable/transparent decisions | ✅ Logged reasoning, shown to caregiver | ✅ Same, plus historical trend charts |
| ML-trained model | ❌ Not in demo (no data yet) | ✅ Supervised model (XGBoost/LightGBM) trained on accumulated `DIFFICULTY_LOG` data once real usage exists |

### 5.3 Multilingual & Voice
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Multilingual interaction | ✅ 3 languages (English, Hindi, Assamese) | ✅ 10+ NER languages via content-delivery service |
| Voice-assisted interaction | ✅ Voice **output** only (TTS reads prompts aloud) | ✅ Voice output **and input** (speech-to-text answers) |
| Culturally familiar themes/visuals/sounds | ✅ NER-contextual art direction, Assamese as flagship | ✅ Per-state theming across all supported NER languages |

### 5.4 Reminders
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Medicine reminders | ✅ Local scheduled notification | ✅ Server-scheduled push with delivery confirmation |
| Hydration reminders | ✅ Local scheduled notification | ✅ Same, adaptive timing based on activity |
| Daily activity reminders | ✅ Local scheduled notification | ✅ Same |
| Medical appointment reminders | ✅ Caregiver-set, local notification | ✅ Integration-ready with appointment systems (future) |

### 5.5 Caregiver & Healthcare Worker Monitoring
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Progress dashboard | ✅ Recent attempts, XP, league | ✅ Full analytics: trend charts, comparisons over time |
| Activity-level monitoring | ✅ Basic level/session view | ✅ Detailed engagement & adherence metrics |
| "Why" transparency (AI reasoning) | ✅ Plain-language panel from `DIFFICULTY_LOG` | ✅ Same, with historical view and export |
| Multi-patient support | ❌ 1 caregiver ↔ 1 patient (demo limitation) | ✅ 1 caregiver ↔ many patients |
| Alerts for concerning patterns | ❌ Not in demo | ✅ e.g. sudden performance drop, missed reminders repeatedly |

### 5.6 Offline & Connectivity
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Works in low-connectivity areas | ✅ Local buffering, sync on reconnect | ✅ Same, plus robust conflict resolution |
| Out-of-order sync handling | ❌ Not in demo | ✅ Timestamp-based reconciliation logic |

### 5.7 Accessibility & UI
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Simple, elderly-friendly UI | ✅ Large targets, high contrast, minimal text, audio cues | ✅ Same, refined via usability testing with real elderly users |
| Mobile/tablet accessible | ✅ React Native, phone-first | ✅ Same, optimized tablet layouts |

### 5.8 Data Security & Patient Data Management
| Requirement | Phase 1 | Phase 2 |
|---|---|---|
| Encryption in transit/at rest | ✅ TLS + basic at-rest encryption | ✅ Full AES-256 at rest, key management |
| Authentication | ✅ JWT, role flag (patient/caregiver) | ✅ Full RBAC |
| Caregiver consent flow | ❌ Not in demo | ✅ Explicit consent capture entity + gating logic |
| Compliance-readiness | ❌ Not addressed in demo | ✅ Data protection review aligned to applicable health-data norms |

---

## 6. Non-Functional Requirements (Both Phases)

- **Bounded AI decisions:** difficulty must never jump more than a safe delta per adjustment window, in both phases — this is a hard safety constraint, not a performance optimization.
- **Low cognitive load:** no stress-inducing animations, aggressive timers, or failure penalties.
- **Cultural appropriateness:** content must be reviewed for NER-regional relevance, not just translated.
- **Performance:** game screens must respond within ~200ms of input on low-to-mid-range Android devices, since target users likely have older/budget phones.
- **Reliability under poor connectivity:** the core game loop must never hard-fail due to lack of network.

---

## 7. Two-Phase Architecture Summary

| | **Phase 1 — Demo** | **Phase 2 — Full Solution** |
|---|---|---|
| Backend | Single Spring Boot monolith, modular | 5+ microservices behind API gateway |
| Database | PostgreSQL only (JSONB for logs) | PostgreSQL + MongoDB + Redis + object storage |
| AI Engine | In-process rule-based scoring | Separate FastAPI service + retraining pipeline |
| Voice | Device-native TTS only | TTS + STT, cloud-enhanced multilingual support |
| Reminders | Local device notifications | Server-scheduled push (FCM) with confirmation |
| i18n | 3 languages, bundled JSON | 10+ languages, live content-delivery service |
| Caregiver model | 1:1 | Many-to-many with consent flow |
| Auth | Basic JWT + role flag | Full RBAC |

Full diagrams: see `SystemDesign.md` (Phase 1) and `SmritiSetu-System-Design-Full.md` (Phase 2).

---

## 8. Timeline

| Milestone | Target |
|---|---|
| Phase 1 demo build | 1–1.5 weeks (vibe-coded) |
| Internal hackathon | [insert date] |
| Post-internal feedback incorporation | +1–2 weeks |
| Real usage data collection begins | Ongoing from post-internal testing |
| AI model upgrade (rule-based → trained) | Weeks 3–6 post-internal |
| Caregiver multi-patient + consent flow | Weeks 4–7 post-internal |
| Selective service separation (AI engine split out) | Weeks 6–9 post-internal |
| Security/compliance hardening | Weeks 8–10 post-internal |
| Final polish & rehearsal | Final 1–2 weeks before December |
| **SIH Final** | December 2026 |

---

## 9. Success Metrics

| Metric | Phase 1 target | Phase 2 target |
|---|---|---|
| PS requirement coverage | 100% of requirements demoable at some fidelity | 100% at production fidelity |
| Live demo reliability | No crashes during judged demo | 99%+ uptime |
| Caregiver comprehension | Caregiver can explain "why difficulty changed" from the dashboard unaided | Same, validated with real caregivers |
| Offline resilience | No data loss during a simulated connectivity drop in demo | Verified conflict-free sync across real-world network gaps |

---

## 10. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Demo build runs out of time before all PS requirements are covered | Build plan already sequenced by priority (see `SystemDesign.md` build plan); cut from deferred list, never from PS-required list |
| Live demo breaks in front of judges | Reserve final days purely for integration + rehearsal, no new features added late |
| Judges probe gaps (voice input, consent flow, etc.) | Section 5 tables above are the source of truth for honest, prepared answers |
| Phase 2 scope creep delays December readiness | Timeline in Section 8 sequences upgrades by evaluator-visible impact (AI depth, caregiver features) before infrastructure sophistication (microservices, polyglot DBs) |

---

## 11. Open Questions

- Exact internal hackathon date and judging format (confirmed: both slide + live demo).
- Which additional NER languages to prioritize after Assamese for Phase 2.
- Whether appointment-reminder integration with any real healthcare system is expected by SIH final, or remains a standalone reminder feature.
