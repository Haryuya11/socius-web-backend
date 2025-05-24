DROP TABLE IF EXISTS positions CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS periods CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS employment_details CASCADE;
DROP TABLE IF EXISTS employment_history CASCADE;
DROP TABLE IF EXISTS salary_history CASCADE;
DROP TABLE IF EXISTS teams CASCADE;
DROP TABLE IF EXISTS targets CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS performance_reviews CASCADE;
DROP TABLE IF EXISTS peer_votes CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS notification_recipients CASCADE;
DROP TABLE IF EXISTS employee_ranking CASCADE;
DROP TABLE IF EXISTS login_history CASCADE;
DROP TABLE IF EXISTS app_settings CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS conversation_members CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS message_status CASCADE;
DROP TABLE IF EXISTS unread_counts CASCADE;

CREATE TABLE
    positions
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    departments
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    roles
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    permissions
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    role_permissions
(
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE
    periods
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)                                                         NOT NULL,
    type        VARCHAR(10) CHECK (type IN ('daily', 'weekly', 'monthly', 'yearly')) NOT NULL,
    start_date  DATE                                                                 NOT NULL CHECK (start_date <= CURRENT_DATE),
    end_date    DATE                                                                 NOT NULL CHECK (end_date >= start_date),
    status      VARCHAR(10) CHECK (status IN ('active', 'inactive'))                 NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Bảng users chỉ lưu thông tin cá nhân
CREATE TABLE
    users
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE CHECK (email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        ),
    birth_date   DATE         NOT NULL CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'),
    image_url    TEXT,
    gender       VARCHAR(10) CHECK (gender IN ('male', 'female')),
    nationality  VARCHAR(100),
    phone_number VARCHAR(15) CHECK (phone_number ~ '^[0-9]{10,15}$'),
    hire_date    DATE         NOT NULL CHECK (hire_date <= CURRENT_DATE),
    address      TEXT,
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    teams
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL UNIQUE,
    leader_id  UUID UNIQUE,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Bảng thông tin việc làm hiện tại
CREATE TABLE
    employment_details
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID                                                                       NOT NULL UNIQUE,
    position_id    UUID                                                                       NOT NULL,
    department_id  UUID                                                                       NOT NULL,
    team_id        UUID,
    role_id        UUID                                                                       NOT NULL,
    start_date     DATE                                                                       NOT NULL CHECK (start_date <= CURRENT_DATE),
    salary         DECIMAL(10, 2) CHECK (salary >= 0)                                         NOT NULL,
    working_status VARCHAR(10) CHECK (working_status IN ('active', 'inactive', 'terminated')) NOT NULL,
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES positions (id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Bảng lịch sử việc làm
CREATE TABLE
    employment_history
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID                               NOT NULL,
    position_id   UUID                               NOT NULL,
    department_id UUID                               NOT NULL,
    team_id       UUID,
    role_id       UUID                               NOT NULL,
    start_date    DATE                               NOT NULL CHECK (start_date <= CURRENT_DATE),
    end_date      DATE                               NOT NULL CHECK (end_date >= start_date),
    salary        DECIMAL(10, 2) CHECK (salary >= 0) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES positions (id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Bảng lịch sử lương
CREATE TABLE
    salary_history
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID                                        NOT NULL,
    previous_salary DECIMAL(10, 2) CHECK (previous_salary >= 0) NOT NULL,
    new_salary      DECIMAL(10, 2) CHECK (new_salary >= 0)      NOT NULL,
    effective_date  DATE                                        NOT NULL,
    reason          VARCHAR(255),
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE
    targets
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    deadline    DATE         NOT NULL,
    status      VARCHAR(10) CHECK (
        status IN ('pending', 'completed', 'failed', 'in_progress')
        )                    NOT NULL,
    assigned_to UUID         NULL,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE
    tasks
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    deadline    DATE         NOT NULL CHECK (deadline > CURRENT_DATE),
    status      VARCHAR(10) CHECK (
        status IN ('pending', 'completed', 'failed', 'in_progress')
        )                    NOT NULL,
    assigned_to UUID         NULL,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE
    performance_reviews
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    period_id   UUID NOT NULL,
    rating      DECIMAL(2, 1) CHECK (
        rating >= 0
            AND rating <= 10
        )            NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (period_id) REFERENCES periods (id) ON DELETE CASCADE
);

CREATE TABLE
    peer_votes
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    voter_id          UUID                                                      NOT NULL,
    voted_employee_id UUID                                                      NOT NULL,
    period_id         UUID                                                      NOT NULL,
    reason            TEXT,
    vote_type         VARCHAR(10) CHECK (vote_type IN ('positive', 'negative')) NOT NULL,
    created_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (voted_employee_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (period_id) REFERENCES periods (id) ON DELETE CASCADE
);

CREATE TABLE
    employee_ranking
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID                                                                                          NOT NULL,
    period_id   UUID                                                                                          NOT NULL,
    rank        DECIMAL(2, 1)                                                                                 NOT NULL,
    criteria    VARCHAR(20) CHECK (criteria IN ('performance', 'peer_vote', 'attendance', 'task_completion')) NOT NULL,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (period_id) REFERENCES periods (id) ON DELETE CASCADE
);

CREATE TABLE
    notifications
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100)                                              NOT NULL,
    sender_id   UUID                                                      NOT NULL,
    message     TEXT                                                      NOT NULL,
    expiry_date DATE                                                      NOT NULL,
    type        VARCHAR(10) CHECK (type IN ('info', 'reminder', 'error')) NOT NULL,
    is_urgent   BOOLEAN                                                   NOT NULL,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE
    notification_recipients
(
    notification_id UUID NOT NULL,
    user_id         UUID NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    read_at         TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (notification_id, user_id)
);

CREATE TABLE
    account
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    last_login          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    is_active           BOOLEAN          DEFAULT TRUE,
    is_default_password BOOLEAN          DEFAULT TRUE,
    password            TEXT NOT NULL,
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE login_history
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    login_time  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    ip_address  VARCHAR(45),
    device_info TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (user_id, login_time) -- Đảm bảo mỗi user chỉ có một lần đăng nhập tại một thời điểm nhất định
);

CREATE TABLE app_settings
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Bảng cuộc trò chuyện
CREATE TABLE conversations
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255),
    type       VARCHAR(20) NOT NULL CHECK (type IN ('DIRECT', 'GROUP')),
    image_url  TEXT,
    created_by UUID        NOT NULL REFERENCES users (id),
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Bảng thành viên cuộc trò chuyện
CREATE TABLE conversation_members
(
    conversation_id UUID NOT NULL REFERENCES conversations (id),
    user_id         UUID NOT NULL REFERENCES users (id),
    joined_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    left_at         TIMESTAMP,
    role            VARCHAR(20) DEFAULT 'MEMBER' CHECK (role IN ('MEMBER', 'ADMIN')),
    PRIMARY KEY (conversation_id, user_id)
);

-- Bảng tin nhắn
CREATE TABLE messages
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id    UUID NOT NULL REFERENCES conversations (id),
    sender_id          UUID NOT NULL REFERENCES users (id),
    content            TEXT NOT NULL,
    message_type       VARCHAR(20)      DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'AUDIO', 'VIDEO')),
    media_url          TEXT,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    is_edited          BOOLEAN          DEFAULT FALSE,
    is_deleted         BOOLEAN          DEFAULT FALSE,
    media_cleaned_up   BOOLEAN          DEFAULT FALSE,
    file_size          BIGINT,
    file_original_name TEXT,
    file_content_type  TEXT
);

-- Bảng trạng thái đọc tin nhắn
CREATE TABLE message_status
(
    message_id UUID NOT NULL REFERENCES messages (id),
    user_id    UUID NOT NULL REFERENCES users (id),
    is_read    BOOLEAN DEFAULT FALSE,
    read_at    TIMESTAMP,
    PRIMARY KEY (message_id, user_id)
);

-- Bảng theo dõi tin chưa đọc
CREATE TABLE unread_counts
(
    conversation_id      UUID NOT NULL REFERENCES conversations (id),
    user_id              UUID NOT NULL REFERENCES users (id),
    unread_count         INT DEFAULT 0,
    last_read_message_id UUID REFERENCES messages (id),
    PRIMARY KEY (conversation_id, user_id)
);

--Constraints
--Không có hai period trùng name, type, start_date, end_date.
ALTER TABLE periods
    ADD CONSTRAINT unique_period UNIQUE (name, type, start_date, end_date);

--Mỗi performance_review là duy nhất giữa employee_id, reviewer_id, period_id.
ALTER TABLE performance_reviews
    ADD CONSTRAINT unique_performance_review UNIQUE (employee_id, reviewer_id, period_id);

--Một peer_vote duy nhất giữa voter_id, voted_employee_id và period_id.
ALTER TABLE peer_votes
    ADD CONSTRAINT unique_peer_vote UNIQUE (voter_id, voted_employee_id, period_id);

--Một task chỉ được giao duy nhất cho 1 user
ALTER TABLE tasks
    ADD CONSTRAINT unique_assign_task UNIQUE (name, assigned_to);

--Một thông báo chỉ được gửi 1 lần  đến một user, một user chỉ có thể nhận được 1 thông báo 1 lần duy nhất
ALTER TABLE notification_recipients
    ADD CONSTRAINT unique_notification_recipient UNIQUE (notification_id, user_id);


INSERT INTO app_settings (setting_key, setting_value, description)
VALUES ('session_timeout', '30', 'Thời gian phiên làm việc (phút)'),
       ('session_extension_threshold', '2', 'Ngưỡng thời gian còn lại để gia hạn phiên (phút)'),
       ('default_user_password', '1', 'Mật khẩu mặc định cho tài khoản mới'),
       ('allowed_origins', 'http://localhost:3000,https://app.socius.com', 'Danh sách domain được phép truy cập API'),
       ('max_login_sessions', '1', 'Số phiên đăng nhập tối đa cho mỗi người dùng'),
       ('online.status.timeout.minutes', '5', 'Thời gian chờ trạng thái online của người dùng (phút)'),
       ('websocket.heartbeat.interval', '1', 'Khoảng thời gian gửi tín hiệu heartbeat của WebSocket (phút)')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value, description)
VALUES ('rabbitmq.message.ttl', '7', 'Thời gian sống của tin nhắn RabbitMQ (ngày)')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value, description)
VALUES ('websocket.time.to.first.message', '60000', 'Thời gian tối đa cho tin nhắn đầu tiên qua WebSocket (ms)'),
       ('websocket.heartbeat.send', '25000', 'Thời gian gửi heartbeat từ server đến client (ms)'),
       ('websocket.heartbeat.receive', '25000', 'Thời gian server chờ nhận heartbeat từ client (ms)')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value, description)
VALUES ('rabbitmq.prefetch.count', '10', 'Số lượng tin nhắn được gửi đến consumer cùng một lúc'),
       ('rabbitmq.concurrent.consumers', '3', 'Số lượng consumer đồng thời cho mỗi queue'),
       ('rabbitmq.max.concurrent.consumers', '10', 'Số lượng consumer tối đa cho mỗi queue'),
       ('rabbitmq.retry.max.attempts', '3', 'Số lần thử lại tối đa khi gửi tin nhắn'),
       ('rabbitmq.retry.initial.interval', '1000', 'Thời gian chờ giữa các lần thử lại (ms)'),
       ('rabbitmq.retry.multiplier', '2', 'Hệ số nhân cho thời gian chờ giữa các lần thử lại'),
       ('rabbitmq.retry.max.interval', '10000', 'Thời gian chờ tối đa giữa các lần thử lại (ms)'),
       ('chat.offline.messages.expiry.days', '7', 'Thời gian hết hạn tin nhắn ngoại tuyến (ngày)'),
       ('chat.offline.messages.max', '100', 'Số lượng tin nhắn ngoại tuyến tối đa cho mỗi người dùng'),
       ('rabbitmq.dlx.message.ttl', '86400000', 'Thời gian sống của tin nhắn trong Dead Letter Exchange (ms)'),
       ('rabbitmq.dlq.retry.window.minutes', '360',
        'Thời gian tối đa để gửi lại tin nhắn trong Dead Letter Queue (phút)'),
       ('websocket.disconnect.grace.seconds', '60', 'Thời gian chờ trước khi ngắt kết nối WebSocket (giây)'),
       ('message.file.cleanup.days', '30', 'Thời gian sống của các tệp đính kèm tin nhắn (ngày)'),
       ('session.cookie.max.age', '86400', 'Thời gian sống của cookie phiên (giây)'),
       ('session.cookie.secure', 'true', 'Đặt cookie phiên là bảo mật (chỉ gửi qua HTTPS)'),
       ('file.upload.max.size', '52428800', 'Kích thước tối đa của tệp tải lên (byte)'),
       ('websocket.heartbeat.timeout', '300000', 'Thời gian chờ heartbeat WebSocket (ms)'),
       ('rabbitmq.max.retries', '3', 'Số lần thử lại tối đa khi gửi tin nhắn RabbitMQ')
ON CONFLICT (setting_key) DO NOTHING;
