##  Project Context
A national immigration authority has begun rolling out a digital service to support
immigration status checks carried out by authorised organisations and government
agencies. The initiative replaces paper-based confirmation processes that were slow,
difficult to audit, and prone to misuse.
The authority remains the sole owner of immigration and visa records. These records
continue to be maintained within existing government systems and are not modified by
the verification service itself. Instead, the service provides controlled access to current
status information and returns outcomes appropriate to the requesting party.
Early development work is underway, and an initial prototype is required to explore how
the service could behave in practice. During this stage, simplified representations of
complex inputs may be used where necessary. For example, document-based
interactions may involve manually entering passport or permit identifiers rather than
capturing or scanning physical documents, while preserving the same validation and
authorisation intent.


Verification and Identity Inputs
Organisations interact with immigration information under different operational
circumstances, leading to two verification routes.
In many everyday situations, individuals initiate verification themselves. Someone
seeking employment or accommodation may request a time-limited share code and
provide it to an employer or landlord together with their date of birth. Share codes allow
eligibility to be confirmed without revealing login credentials or unnecessary personal
data. Codes may be scoped by purpose and expire after a defined period, giving
individuals control over when and how their information is accessed.
In other situations, verification is initiated by authorised authorities during operational
duties. Border control officers or law enforcement personnel may rely on official travel
or identity documents presented during an interaction. Passport details or immigration
permit identifiers may therefore form the basis of a request. Access to this route is
restricted to recognised roles and requires stronger validation and auditing safeguards.
Although both approaches rely on the same immigration records, they differ in who
initiates the request, what inputs are supplied, and the level of authorisation required.
Requesting Organisations and Lawful Purpose
Organisations do not interact with immigration records directly. Authorised
organisations access the service through controlled endpoints provided by the
authority.
For employer, landlord, or education-related checks, requests typically include the
individual’s share code and date of birth together with organisation identity details and
an official organisation email address. Before processing, the requester must confirm
that the check is being performed for a lawful purpose, that returned information will be
handled in accordance with data protection obligations, and that the request is not
discriminatory or unauthorised. Requests that fail these conditions are rejected in a
consistent and predictable manner.
Authority-driven verification follows similar principles: the requesting authority must be
recognised, the purpose must be valid, and interactions must remain auditable and
privacy-aware.
Verification Behaviour
The service performs controlled verification rather than immigration data management.
For each request, current status information is retrieved and defined rules are applied
to determine an outcome appropriate to requester role and declared purpose.
An employer may receive confirmation of right-to-work eligibility, while a landlord
receives confirmation relevant to accommodation eligibility. Border control or law
enforcement interactions may require outcomes related to entry permission or status
validity. In all cases, only the minimum necessary information is disclosed.
Operational consistency is essential. Identical inputs should produce identical
outcomes. The system must therefore handle situations such as expired or invalid share
codes, purpose mismatches, date-of-birth discrepancies, incomplete document
information, unauthorised organisations, and missing confirmations in a predictable
manner. Failure responses must avoid exposing unnecessary personal data or internal
system behaviour.
Incoming requests undergo validation before verification takes place. This includes
checking share code format and expiry, matching identity information where required,
validating document inputs, confirming requester authorisation, and ensuring declared
purposes are appropriate. These rules must apply consistently regardless of system
load or the number of participating organisations.
Auditability, Privacy, and Operational Oversight
Because verification decisions may later be questioned or investigated, key actions
must be recorded. Share code generation events, verification attempts, validation
failures, and outcomes contribute to an audit trail supporting accountability and quality
assurance while minimising unnecessary exposure of personal data.
As participation grows, operational teams also require visibility into how the service is
being used across organisations. An analytics capability has therefore been proposed to
provide aggregated oversight without exposing unnecessary personal information.
Examples include identifying which organisations are requesting checks, how
frequently requests occur over time, and the stated purpose for which share codes are
generated, such as employment checks, accommodation checks, or other permitted
purposes.
These insights support governance, capacity planning, and misuse detection while
remaining consistent with privacy obligations.
2. Task
   There are two required deliverables:
1. A Design and Evaluation Document, and
2. A Code Package containing your implementation and associated tests.
   Your submission must demonstrate a coherent relationship between:
   • architectural design decisions supported by appropriate analysis of the scenario
   • verification strategy
   • evaluation of system quality
   The implementation must be developed as a console application using Java or
   Python, using file-based persistence for storing and retrieving system data.
   The code structure must be consistent with the architectural design described in your
   document.
   The implementation is expected to demonstrate representative system behaviour rather
   than a complete production system.
3. Deliverables
   Deliverable 1: Design and Evaluation Document
   Submit a structured report (.docx or .pdf).
   The document should demonstrate clear reasoning linking the scenario to architectural,
   verification, and evaluation decisions.
   Your document should include:
   System reasoning and architecture
   • identification of key behaviours, constraints, and assumptions necessary to
   justify architectural design decisions, rather than exhaustive functional or
   requirements modelling
   • a clear architectural design describing component responsibilities and data flow
   • justification of design decisions in relation to scalability, reliability, and
   maintainability considerations.
   At least one architectural diagram is required and must clearly communicate system
   structure, component responsibilities, and data or interaction flow.
   Functional test design (Black-box testing)
   Provide functional test cases derived from system behaviour.
   Appropriate black-box testing techniques should be applied where relevant.
   Test cases should demonstrate consideration of both normal and edge scenarios.
   Performance evaluation approach
   Provide an API-Level Performance Test Specification describing how system
   performance would be evaluated under load by an independent engineering team,
   assuming the system exposes API endpoints.
   The specification should define:
   • performance-critical usage scenarios
   • workload assumptions
   • measurable performance expectations
   • how results would be interpreted
   Implementation of load testing is not required.
   Deliverable 2: Code Package
   You must submit a single zipped source folder containing your complete code
   package.
   The code package must include:
   Implementation
   A runnable console application written in Java or Python using file-based persistence.
   The implementation must reflect the architectural structure described in Deliverable 1.
   Unit testing (White-box testing)
   Unit tests must demonstrate testing informed by program structure and logic. They
   should include both normal and failure or edge-case behaviour where applicable.
   Where complex validation logic, decision paths, or error handling behaviour exists,
   students should apply appropriate white-box testing considerations when selecting test
   cases.
   Evidence of this reasoning should be provided within the code package (for example
   through test comments, documentation, or a short testing README explaining the
   intent of selected tests).
   Logging for observability and traceability
   Your implementation must include logging supporting observability and traceability of
   system behaviour.
   You should use a logging mechanism appropriate to your chosen language and
   demonstrate control over what events and contextual information are recorded, rather
   than relying solely on framework default logging behaviour.
   Run instructions
   Provide concise instructions allowing a marker to:
   • run the application
   • execute tests
   • view logging output
4. Assessment Criteria
   The marking rubric below explains how the submission will be assessed.
   Fail Pass
   0-29% 30-39% 40-49% 50-59% 60-69% 70-79% 80-100%
1. Functionality,
   Observability, and
   Implementation
   Quality (30 Marks)
   Demonstrating a
   working console
   implementation
   aligned with the
   proposed
   architecture,
   producing reliable
   outcomes and
   supporting
   observable and
   traceable operation
   through logging.
   Application does
   not run, produces
   incorrect
   outcomes, or key
   functionality is
   missing. Logging
   absent or unusable.
   Runs inconsistently
   or supports only
   limited scenarios.
   Persistence
   unreliable or
   behaviour
   unpredictable.
   Logging minimal or
   unclear.
   Basic behaviours
   implemented but
   limited robustness.
   Some invalid or
   edge conditions not
   handled. Logging
   present but limited
   traceability.
   Runnable
   implementation
   supporting
   representative
   behaviours with
   generally correct
   outcomes. File
   persistence
   functions correctly.
   Logging records key
   events at a basic
   level.
   Reliable behaviour
   across normal and
   failure scenarios.
   Deterministic
   handling evident.
   Logging enables
   tracing of major
   actions or requests.
   Implementation
   broadly consistent
   with architecture.
   Strong
   implementation
   quality with
   disciplined
   validation
   behaviour and
   predictable
   outcomes. Logging
   meaningfully
   supports
   understanding of
   system behaviour
   and failure
   conditions.
   Exceptional
   robustness and
   coherence.
   Implementation
   clearly reflects
   architectural intent.
   Logging provides
   clear traceability of
   system actions and
   outcomes across
   workflows.
2. Architectural
   Design for System
   Quality and
   Reliability (25
   Marks) Designing a
   coherent
   architecture
   supported by
   appropriate
   scenario analysis
   and justified design
   decisions.
   Architecture
   missing,
   incoherent, or
   unrelated to
   scenario
   constraints.
   Basic structure
   proposed but
   unclear
   responsibilities or
   flows. Limited
   reasoning.
   Plausible
   architecture but
   responsibilities or
   interactions
   unclear. Limited
   justification.
   Clear architecture
   with identifiable
   components and
   interactions. Some
   reasoning evident.
   Required diagram
   present.
   Well-structured
   architecture
   showing clear
   separation of
   responsibilities and
   consideration of
   relevant system
   constraints and
   quality attributes.
   Decisions
   reasonably
   justified.
   Strong reasoning
   demonstrating
   trade-offs affecting
   reliability,
   behaviour under
   load, validation
   flow, and oversight
   requirements.
   Architectural
   diagram
   communicates
   system operation
   clearly.
   Exceptional
   architectural clarity
   and judgement.
   Demonstrates
   insight into system
   risks, trade-offs,
   and behaviour
   under varying
   operational
   conditions.
   Decisions clearly
   grounded in
   scenario
   constraints.
3. Test Design and
   Verification
   Strategy (30
   Marks) Producing
   functional test
   Testing absent or
   unrelated to
   behaviour.
   Minimal tests with
   weak reasoning or
   little coverage. Unit
   testing largely
   absent.
   Functional tests
   present but limited
   technique use. Unit
   testing basic or
   focused mainly on
   Structured
   functional test
   cases covering
   main scenarios.
   Unit tests present
   Good coverage of
   normal and edge
   behaviour using
   appropriate black-
   box techniques.
   Strong verification
   strategy targeting
   critical behaviours
   and failure
   conditions. Black-
   Excellent
   verification thinking
   demonstrating
   deep
   understanding of
   cases using black-
   box techniques and
   demonstrating
   purposeful white-
   box unit testing
   within the
   implementation.
   normal behaviour
   scenarios.
   but limited white-
   box reasoning.
   Unit tests target
   validation or error
   paths where
   appropriate.
   box techniques
   clearly applied.
   White-box
   reasoning evident
   through targeted
   tests or
   documentation
   explaining logic
   coverage.
   behaviour and
   failure conditions.
   Functional tests
   well justified. Unit
   tests clearly
   informed by
   program logic and
   decision paths.
4. Performance
   Evaluation
   Strategy (15
   Marks) Defining
   how system
   performance would
   be evaluated under
   load using an API-
   level performance
   test specification.
   Missing or
   inappropriate
   performance
   evaluation.
   General discussion
   with little
   measurable
   definition.
   Some measurable
   expectations but
   unclear workload or
   interpretation.
   Reasonable
   performance plan
   with basic workload
   definition and
   evaluation
   approach.
   Clear performance
   scenarios with
   measurable
   expectations and
   sensible
   interpretation
   approach.
   Strong
   specification
   including realistic
   workload
   modelling, API
   request
   assumptions, and
   linkage to
   architecture.
   Exceptional
   evaluation plan
   demonstrating
   insight into
   performance
   bottlenecks,
   system risks, and
   interpretation of
   behaviour under
   load.
