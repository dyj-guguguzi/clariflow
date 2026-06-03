## ADDED Requirements

### Requirement: State transition rules
The system SHALL enforce a strict 6-state workflow with defined allowed transitions per state. The state machine MUST be the single source of truth defined in the WorkItemStatus enum.

States: DRAFT, ANALYZING, READY, IN_DEVELOPMENT, TESTING, COMPLETED

Allowed transitions:
- DRAFT → ANALYZING
- ANALYZING → READY, DRAFT
- READY → IN_DEVELOPMENT, ANALYZING
- IN_DEVELOPMENT → TESTING, READY
- TESTING → COMPLETED, IN_DEVELOPMENT
- COMPLETED → TESTING

#### Scenario: Valid forward transition
- **WHEN** POST /api/work-items/WI-001/transitions with targetStatus=ANALYZING (current status is DRAFT)
- **THEN** system returns 200 with updated work item, status changed to ANALYZING

#### Scenario: Valid rollback transition
- **WHEN** POST /api/work-items/WI-002/transitions with targetStatus=DRAFT (current status is ANALYZING)
- **THEN** system returns 200 with updated work item, status changed to DRAFT

#### Scenario: Invalid transition blocked
- **WHEN** POST /api/work-items/WI-001/transitions with targetStatus=READY (current status is DRAFT, not allowed)
- **THEN** system returns 422 with error code WF-002 (invalid transition)

#### Scenario: COMPLETED to TESTING allowed
- **WHEN** POST /api/work-items/WI-001/transitions with targetStatus=TESTING (current status is COMPLETED)
- **THEN** system returns 200 with updated work item, status changed to TESTING

### Requirement: Transition history recording
The system SHALL record every state transition in the work_item_transition table with fromStatus, toStatus, reason, operator, and timestamp.

#### Scenario: History recorded on transition
- **WHEN** a state transition is successfully executed
- **THEN** a new transition record is created in work_item_transition table

#### Scenario: Query transition history
- **WHEN** GET /api/work-items/WI-001/transitions
- **THEN** system returns 200 with array of transition records in chronological order

### Requirement: Optimistic locking on transition
The system SHALL use optimistic locking (version field) to prevent concurrent state transitions from conflicting.

#### Scenario: Concurrent transition conflict
- **WHEN** two simultaneous transition requests are made for the same work item
- **THEN** the first succeeds, the second returns 409 with error code WF-005

### Requirement: HIGH clarification blocks transition
The system SHALL block transitions to READY or IN_DEVELOPMENT when the work item has any unresolved HIGH-severity clarification.

#### Scenario: Block with unresolved HIGH clarification
- **WHEN** POST /api/work-items/WI-001/transitions with targetStatus=READY and the work item has an unresolved HIGH severity clarification
- **THEN** system returns 422 with error code WF-003

#### Scenario: Allow after resolving HIGH clarification
- **WHEN** all HIGH-severity clarifications are resolved AND POST /api/work-items/WI-001/transitions with targetStatus=READY
- **THEN** system returns 200 with successful transition
