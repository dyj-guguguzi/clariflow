## ADDED Requirements

### Requirement: Create clarification
The system SHALL allow adding a clarification question to a work item with severity level (HIGH/MEDIUM/LOW). Default status MUST be UNRESOLVED.

#### Scenario: Add HIGH severity clarification
- **WHEN** POST /api/work-items/WI-001/clarifications with question text and severity=HIGH
- **THEN** system returns 201 with clarification record, status=UNRESOLVED

#### Scenario: Add clarification to non-existent work item
- **WHEN** POST /api/work-items/NONEXISTENT/clarifications
- **THEN** system returns 404 with error code WF-001

### Requirement: List clarifications
The system SHALL return all clarification questions for a given work item, ordered by creation time.

#### Scenario: List clarifications for work item
- **WHEN** GET /api/work-items/WI-001/clarifications
- **THEN** system returns 200 with array of clarification records including severity, status, and answer

#### Scenario: List empty clarifications
- **WHEN** GET /api/work-items/WI-001/clarifications and no clarifications exist
- **THEN** system returns 200 with empty array

### Requirement: Resolve clarification
The system SHALL allow resolving a clarification by providing an answer. Status MUST change from UNRESOLVED to RESOLVED, and resolvedAt timestamp MUST be set.

#### Scenario: Resolve clarification with answer
- **WHEN** PUT /api/work-items/WI-001/clarifications/1/resolve with answer text
- **THEN** system returns 200 with updated clarification, status=RESOLVED, resolvedAt set, answer populated

#### Scenario: Resolve non-existent clarification
- **WHEN** PUT /api/work-items/WI-001/clarifications/999/resolve
- **THEN** system returns 404 with error code WF-004
