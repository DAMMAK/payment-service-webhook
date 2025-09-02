-- db/migration/V1__initial_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Merchants table
CREATE TABLE merchants (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

-- Webhook endpoints table
CREATE TABLE webhook_endpoints (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   merchant_id UUID NOT NULL REFERENCES merchants(id),
                                   url VARCHAR(500) NOT NULL,
                                   description TEXT,
                                   secret VARCHAR(255) NOT NULL,
                                   status VARCHAR(50) DEFAULT 'ACTIVE',
                                   api_version VARCHAR(10) DEFAULT 'v1',
                                   retry_enabled BOOLEAN DEFAULT true,
                                   max_retries INT DEFAULT 3,
                                   timeout_seconds INT DEFAULT 30,
                                   headers JSONB,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   version BIGINT DEFAULT 0
);

-- Webhook events table
CREATE TABLE webhook_events (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                event_type VARCHAR(100) NOT NULL,
                                provider VARCHAR(50) NOT NULL,
                                payload JSONB NOT NULL,
                                idempotency_key VARCHAR(255) UNIQUE,
                                status VARCHAR(50) DEFAULT 'PENDING',
                                source_ip VARCHAR(50),
                                metadata JSONB,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                version BIGINT DEFAULT 0
);

-- Webhook deliveries table
CREATE TABLE webhook_deliveries (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    webhook_event_id UUID NOT NULL REFERENCES webhook_events(id),
                                    webhook_endpoint_id UUID NOT NULL REFERENCES webhook_endpoints(id),
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

-- Event subscriptions table
CREATE TABLE event_subscriptions (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     webhook_endpoint_id UUID NOT NULL REFERENCES webhook_endpoints(id),
                                     event_type VARCHAR(100) NOT NULL,
                                     is_active BOOLEAN DEFAULT true,
                                     description VARCHAR(500),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     version BIGINT DEFAULT 0,
                                     UNIQUE(webhook_endpoint_id, event_type)
);

-- Merchant IP whitelist table
CREATE TABLE merchant_ip_whitelist (
                                       merchant_id UUID NOT NULL REFERENCES merchants(id),
                                       ip_address VARCHAR(50) NOT NULL,
                                       PRIMARY KEY (merchant_id, ip_address)
);

-- Create indexes
CREATE INDEX idx_webhook_endpoints_status ON webhook_endpoints(status);
CREATE INDEX idx_webhook_endpoints_merchant_id ON webhook_endpoints(merchant_id);
CREATE INDEX idx_webhook_events_status ON webhook_events(status);
CREATE INDEX idx_webhook_events_type ON webhook_events(event_type);
CREATE INDEX idx_webhook_events_idempotency ON webhook_events(idempotency_key);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_next_retry ON webhook_deliveries(next_retry_at);
CREATE INDEX idx_webhook_deliveries_event_endpoint ON webhook_deliveries(webhook_event_id, webhook_endpoint_id);
CREATE INDEX idx_event_subscriptions_active ON event_subscriptions(webhook_endpoint_id, is_active);

-- Create update trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_merchants_updated_at BEFORE UPDATE ON merchants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_webhook_endpoints_updated_at BEFORE UPDATE ON webhook_endpoints
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_webhook_events_updated_at BEFORE UPDATE ON webhook_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_webhook_deliveries_updated_at BEFORE UPDATE ON webhook_deliveries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_event_subscriptions_updated_at BEFORE UPDATE ON event_subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();