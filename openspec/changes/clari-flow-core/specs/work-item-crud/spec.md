## ADDED Requirements

### Requirement: Create work item
The system SHALL allow creating work items with title, description, type (STORY/BUG/TASK), priority (P0/P1/P2), assignee, tags, acceptance criteria, and risk level. The system MUST auto-assign status DRAFT and generate a unique ID with "WI-" prefix.

#### Scenario: Create a story work item
- **WHEN** POST /api/work-items with valid title, type=STORY, priority=P1
- **THEN** system returns 201 with the created work item, status=DRAFT, id starting with "WI-"

#### Scenario: Create with missing required fields
- **WHEN** POST /api/work-items without title or type
- **THEN** system returns 400 Bad Request with validation error message

### Requirement: List work items
The system SHALL return a paginated list of work items, filterable by type, priority, and status.

#### Scenario: List all work items
- **WHEN** GET /api/work-items without filters
- **THEN** system returns 200 with array of work item summaries (id, title, type, priority, status, assignee, riskLevel, updatedAt)

#### Scenario: Filter by type
- **WHEN** GET /api/work-items?type=STORY
- **THEN** system returns only STORY type work items

### Requirement: Get work item detail
The system SHALL return full work item details including clarifications and transition history by work item ID.

#### Scenario: Get existing work item detail
- **WHEN** GET /api/work-items/WI-001
- **THEN** system returns 200 with full work item data including clarifications array and transitions array

#### Scenario: Get non-existent work item
- **WHEN** GET /api/work-items/NONEXISTENT
- **THEN** system returns 404 with error code WF-001

### Requirement: Update work item
The system SHALL allow updating work item fields (title, description, type, priority, assignee, tags, acceptanceCriteria, riskLevel). The version field MUST be provided for optimistic locking.

#### Scenario: Update with matching version
- **WHEN** PUT /api/work-items/WI-001 with correct version field
- **THEN** system returns 200 with updated work item, version incremented by 1

#### Scenario: Update with stale version
- **WHEN** PUT /api/work-items/WI-001 with outdated version field
- **THEN** system returns 409 with error code WF-005 (version conflict)
