-- Test schema for repository tests
-- Create tables in dependency order to avoid foreign key constraint issues

CREATE TABLE merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    description VARCHAR(500),
    email VARCHAR(255) NOT NULL,
    webhook_secret VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE webhook_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    payload JSON,
    idempotency_key VARCHAR(255) UNIQUE,
    status VARCHAR(50) DEFAULT 'PENDING',
    source_ip VARCHAR(50),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE webhook_endpoints (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    url VARCHAR(500) NOT NULL,
    description TEXT,
    secret VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    api_version VARCHAR(10) DEFAULT 'v1',
    retry_enabled BOOLEAN DEFAULT true,
    max_retries INT DEFAULT 3,
    timeout_seconds INT DEFAULT 30,
    headers JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    webhook_event_id UUID NOT NULL,
    webhook_endpoint_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt_number INT DEFAULT 1,
    response_status INT,
    response_body TEXT,
    error_message TEXT,
    delivered_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    latency_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE event_subscriptions (
    id UUID PRIMARY KEY,
    webhook_endpoint_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(webhook_endpoint_id, event_type)
);

CREATE TABLE merchant_ip_whitelist (
    merchant_id UUID NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    PRIMARY KEY (merchant_id, ip_address)
);

-- Add foreign key constraints
ALTER TABLE webhook_endpoints ADD CONSTRAINT fk_webhook_endpoints_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id);
ALTER TABLE webhook_deliveries ADD CONSTRAINT fk_webhook_deliveries_event FOREIGN KEY (webhook_event_id) REFERENCES webhook_events(id);
ALTER TABLE webhook_deliveries ADD CONSTRAINT fk_webhook_deliveries_endpoint FOREIGN KEY (webhook_endpoint_id) REFERENCES webhook_endpoints(id);
ALTER TABLE event_subscriptions ADD CONSTRAINT fk_event_subscriptions_endpoint FOREIGN KEY (webhook_endpoint_id) REFERENCES webhook_endpoints(id);
ALTER TABLE merchant_ip_whitelist ADD CONSTRAINT fk_merchant_ip_whitelist_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id);

-- Create indexes
CREATE INDEX idx_webhook_status ON webhook_endpoints(status);
CREATE INDEX idx_merchant_id ON webhook_endpoints(merchant_id);
CREATE INDEX idx_webhook_events_status ON webhook_events(status);
CREATE INDEX idx_webhook_events_type ON webhook_events(event_type);
CREATE INDEX idx_webhook_events_idempotency ON webhook_events(idempotency_key);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_next_retry ON webhook_deliveries(next_retry_at);
CREATE INDEX idx_webhook_deliveries_event_endpoint ON webhook_deliveries(webhook_event_id, webhook_endpoint_id);
CREATE INDEX idx_event_subscriptions_active ON event_subscriptions(webhook_endpoint_id, is_active);