## ADDED Requirements

### Requirement: Work item list display
The frontend SHALL display a list of work items with id, title, type, priority, status, assignee, and risk level. Clicking an item MUST load its detail into the right panel.

#### Scenario: Initial page load shows work item list
- **WHEN** user opens the frontend page
- **THEN** the left panel displays work item list fetched from GET /api/work-items

#### Scenario: Click item shows detail
- **WHEN** user clicks a work item in the list
- **THEN** the right panel displays full work item detail fetched from GET /api/work-items/{id}

### Requirement: Status transition trigger
The frontend SHALL display available status transition buttons based on the current work item status, and SHALL allow the user to trigger a transition with a reason.

#### Scenario: Show valid transition buttons
- **WHEN** user views a work item with status DRAFT
- **THEN** only the ANALYZING transition button is displayed and enabled

#### Scenario: Execute transition
- **WHEN** user clicks a transition button and confirms
- **THEN** the system executes POST /api/work-items/{id}/transitions and refreshes the detail view

#### Scenario: Blocked transition error display
- **WHEN** a transition returns 422 (WF-002 or WF-003)
- **THEN** the frontend displays the error message from the API response

### Requirement: Clarification management UI
The frontend SHALL support viewing clarification questions and adding new ones for the selected work item.

#### Scenario: View clarifications in detail panel
- **WHEN** user selects a work item with clarifications
- **THEN** the right panel displays all clarification questions with severity, status, and answer

#### Scenario: Add new clarification
- **WHEN** user fills in question text and severity in the clarification form
- **THEN** POST /api/work-items/{id}/clarifications is called and the list refreshes

### Requirement: AI analysis display
The frontend SHALL provide a button to trigger AI analysis and SHALL display the structured result (summary, risks, suggestions) in the detail panel.

#### Scenario: Trigger AI analysis from UI
- **WHEN** user clicks "AI Analysis" button for a work item
- **THEN** POST /api/work-items/{id}/ai-analysis is called and results are displayed in the analysis section

#### Scenario: AI analysis result structure
- **WHEN** AI analysis completes
- **THEN** the frontend displays summary, risk items with severity, and suggestion clarifications in separate sections
