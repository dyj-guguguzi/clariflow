## ADDED Requirements

### Requirement: AI analysis trigger
The system SHALL provide an endpoint to trigger AI analysis on a work item. The analysis result MUST be structured (summary, risks, suggestions), not free-form text.

#### Scenario: Trigger AI analysis on existing work item
- **WHEN** POST /api/work-items/WI-001/ai-analysis
- **THEN** system returns 200 with AIAnalysisResponse containing summary (string), risks (array of {level, description}), suggestions (array of strings), and analyzedAt (timestamp)

#### Scenario: AI analysis on non-existent work item
- **WHEN** POST /api/work-items/NONEXISTENT/ai-analysis
- **THEN** system returns 404 with error code WF-001

### Requirement: Mock AI service
The system SHALL include a Mock implementation of the AI analysis service that generates structured results based on work item title and description content.

#### Scenario: Mock generates summary based on work item title
- **WHEN** AI analysis is triggered for work item with title containing keywords
- **THEN** system returns a summary string that includes the work item title context

#### Scenario: Mock generates risk items
- **WHEN** AI analysis is triggered
- **THEN** system returns at least one risk item with level and description fields

#### Scenario: Mock generates suggestion clarifications
- **WHEN** AI analysis is triggered
- **THEN** system returns at least one suggestion string related to the work item domain

### Requirement: AI service interface abstraction
The AI analysis service SHALL be defined as an interface (AIAnalysisService) with a single analyze method, allowing Mock and real LLM implementations to be swapped via Spring dependency injection.

#### Scenario: Replace Mock with real LLM implementation
- **WHEN** a new implementation of AIAnalysisService is created and registered as a Spring bean
- **THEN** the system uses the new implementation without changing any controller or client code
