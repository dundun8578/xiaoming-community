CREATE TABLE IF NOT EXISTS note_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    sent TINYINT NOT NULL DEFAULT 0,
    attempts INT NOT NULL DEFAULT 0,
    available_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_outbox_pending (sent, available_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


INSERT INTO note_outbox(note_id,event_type,sent,attempts,available_at,create_time)
SELECT n.id,'UPDATE',0,0,NOW(),NOW()
FROM note n WHERE n.is_deleted=0 AND n.status=1
AND NOT EXISTS (SELECT 1 FROM note_outbox o WHERE o.note_id=n.id);
