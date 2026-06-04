-- ClariFlow WorkItem Flow DDL
-- H2 Database (MySQL compatibility mode)

-- WorkItem table
CREATE TABLE IF NOT EXISTS work_item (
    id                  VARCHAR(20)     PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    description         VARCHAR(2000),
    type                VARCHAR(20)     NOT NULL DEFAULT 'STORY',
    priority            VARCHAR(10)     NOT NULL DEFAULT 'P2',
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    assignee            VARCHAR(50),
    tags                VARCHAR(4000),
    acceptance_criteria VARCHAR(4000),
    risk_level          VARCHAR(10),
    version             INT             NOT NULL DEFAULT 1,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- WorkItemTransition table (state transition history)
CREATE TABLE IF NOT EXISTS work_item_transition (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    work_item_id    VARCHAR(20)     NOT NULL,
    from_status     VARCHAR(20)     NOT NULL,
    to_status       VARCHAR(20)     NOT NULL,
    reason          VARCHAR(500),
    operator        VARCHAR(50),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transition_work_item FOREIGN KEY (work_item_id) REFERENCES work_item(id)
);

-- Clarification table
CREATE TABLE IF NOT EXISTS clarification (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    work_item_id    VARCHAR(20)     NOT NULL,
    question        VARCHAR(1000)   NOT NULL,
    severity        VARCHAR(10)     NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20)     NOT NULL DEFAULT 'UNRESOLVED',
    answer          VARCHAR(2000),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP,
    CONSTRAINT fk_clarification_work_item FOREIGN KEY (work_item_id) REFERENCES work_item(id)
);

-- SysUser table
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(200)    NOT NULL,
    email           VARCHAR(100),
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
