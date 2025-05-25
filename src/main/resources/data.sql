-- =============================================
-- PERMISSIONS - Quyền hạn trong hệ thống
-- =============================================
INSERT INTO permissions (id, name, description, created_at, updated_at)
VALUES ('1cbd37e7-8d37-49b8-b44f-6b2a59c16f93', 'ACCESS_ADMIN_PAGE', 'Truy cập trang quản trị', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('2a5d1c3e-bcb6-4c4a-b2f1-53f2eb74c899', 'MANAGE_USERS', 'Quản lý người dùng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('3f85d7a2-6cb9-4824-95da-7dc6f5894213', 'MANAGE_ROLES', 'Quản lý vai trò và quyền', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('46d7e1ab-d25b-49d7-8224-9f21c507b64c', 'MANAGE_DEPARTMENTS', 'Quản lý phòng ban', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('516c1f53-38dd-44fa-9d36-5f4f55519c8d', 'MANAGE_TEAMS', 'Quản lý nhóm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('6e9d7523-a52c-4b4c-b9ee-3104758bb343', 'MANAGE_POSITIONS', 'Quản lý vị trí công việc', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('7ac58320-ff91-45e4-82a6-ff685c0c5d1f', 'MANAGE_PERFORMANCE', 'Quản lý đánh giá hiệu suất', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('84f6c7be-d754-43d8-a363-783e1c88082b', 'VIEW_REPORTS', 'Xem báo cáo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('94eb6e9a-fbf4-4820-9203-5861b731054e', 'MANAGE_CONFIG', 'Quản lý cấu hình hệ thống', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('a1b3ec2d-3f42-40b3-9903-8a7d42f5deda', 'MANAGE_OWN_PROFILE', 'Quản lý hồ sơ cá nhân', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('b2c9f5a6-8e21-43f7-a1e2-b548dc6fb9e7', 'VIEW_TEAM_MEMBERS', 'Xem thành viên nhóm', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('c3d1e8b0-7fa3-409d-8c4e-ebc2f6a91234', 'VIEW_DEPARTMENT_MEMBERS', 'Xem thành viên phòng ban',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('d4e0f9c1-6b54-4a8d-9572-2da9e1a6b345', 'SEND_NOTIFICATIONS', 'Gửi thông báo', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('e5f6a0d2-5c65-4b9e-86f3-4cb0e2b7c456', 'MANAGE_TASKS', 'Quản lý nhiệm vụ', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('f6f7b1e3-4d76-4c0f-97a4-5dc1f3c8d567', 'MANAGE_TARGETS', 'Quản lý mục tiêu', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- =============================================
-- ROLES - Vai trò người dùng
-- =============================================
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES ('a7b9f5d2-c6e3-47b1-9f8d-4e5c6f7a8b9c', 'SUPER_ADMIN', 'Quản trị viên cao cấp với toàn quyền',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', 'ADMIN', 'Quản trị viên hệ thống', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', 'HR_MANAGER', 'Quản lý nhân sự', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'DEPARTMENT_HEAD', 'Trưởng phòng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', 'TEAM_LEADER', 'Trưởng nhóm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', 'EMPLOYEE', 'Nhân viên', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================
-- ROLE_PERMISSIONS - Gán quyền cho vai trò
-- =============================================
-- Super Admin có tất cả quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a7b9f5d2-c6e3-47b1-9f8d-4e5c6f7a8b9c', id
FROM permissions;

-- Admin có hầu hết quyền
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '1cbd37e7-8d37-49b8-b44f-6b2a59c16f93'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '2a5d1c3e-bcb6-4c4a-b2f1-53f2eb74c899'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '3f85d7a2-6cb9-4824-95da-7dc6f5894213'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '46d7e1ab-d25b-49d7-8224-9f21c507b64c'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '516c1f53-38dd-44fa-9d36-5f4f55519c8d'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '6e9d7523-a52c-4b4c-b9ee-3104758bb343'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '7ac58320-ff91-45e4-82a6-ff685c0c5d1f'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '84f6c7be-d754-43d8-a363-783e1c88082b'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', '94eb6e9a-fbf4-4820-9203-5861b731054e'),
       ('b8c0e6f3-d7f4-48c2-a0e9-5f6d7a8b9c0e', 'a1b3ec2d-3f42-40b3-9903-8a7d42f5deda');

-- HR Manager
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', '1cbd37e7-8d37-49b8-b44f-6b2a59c16f93'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', '2a5d1c3e-bcb6-4c4a-b2f1-53f2eb74c899'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', '7ac58320-ff91-45e4-82a6-ff685c0c5d1f'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', '84f6c7be-d754-43d8-a363-783e1c88082b'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', 'a1b3ec2d-3f42-40b3-9903-8a7d42f5deda'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', 'c3d1e8b0-7fa3-409d-8c4e-ebc2f6a91234'),
       ('c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', 'd4e0f9c1-6b54-4a8d-9572-2da9e1a6b345');

-- Department Head
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'a1b3ec2d-3f42-40b3-9903-8a7d42f5deda'),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'b2c9f5a6-8e21-43f7-a1e2-b548dc6fb9e7'),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'c3d1e8b0-7fa3-409d-8c4e-ebc2f6a91234'),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'd4e0f9c1-6b54-4a8d-9572-2da9e1a6b345'),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'e5f6a0d2-5c65-4b9e-86f3-4cb0e2b7c456'),
       ('d0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', 'f6f7b1e3-4d76-4c0f-97a4-5dc1f3c8d567');

-- Team Leader
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', 'a1b3ec2d-3f42-40b3-9903-8a7d42f5deda'),
       ('e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', 'b2c9f5a6-8e21-43f7-a1e2-b548dc6fb9e7'),
       ('e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', 'd4e0f9c1-6b54-4a8d-9572-2da9e1a6b345'),
       ('e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', 'e5f6a0d2-5c65-4b9e-86f3-4cb0e2b7c456');

-- Employee
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', 'a1b3ec2d-3f42-40b3-9903-8a7d42f5deda'),
       ('f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', 'b2c9f5a6-8e21-43f7-a1e2-b548dc6fb9e7');

-- =============================================
-- DEPARTMENTS - Các phòng ban
-- =============================================
INSERT INTO departments (id, name, description, created_at, updated_at)
VALUES ('11cfb6e8-f843-4d91-b5c9-74e624b8186a', 'Ban Giám Đốc', 'Ban lãnh đạo công ty', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('22456a8b-990c-4310-8313-7f69c64e0e1a', 'Kế Toán & Tài Chính', 'Quản lý tài chính và sổ sách',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('33892e7c-ab4d-4927-9412-9825dabf0b1c', 'Nhân Sự', 'Quản lý và phát triển nguồn nhân lực', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('44cd57e1-8b93-4540-aec1-2435f76c669d', 'Công Nghệ Thông Tin', 'Phát triển và duy trì hệ thống công nghệ',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('55ef89f3-0dc4-4b61-a3af-7894d12de24e', 'Marketing', 'Tiếp thị và quảng bá thương hiệu', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('66ab34c2-1ef5-4782-ba9e-908712ab3c5f', 'Kinh Doanh', 'Phát triển kinh doanh và bán hàng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('77bc45d6-2fe6-4893-ca1f-019823ef4d6a', 'Dịch Vụ Khách Hàng', 'Chăm sóc và hỗ trợ khách hàng',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================
-- POSITIONS - Các vị trí công việc
-- =============================================
INSERT INTO positions (id, name, description, created_at, updated_at)
VALUES ('1a2e3456-789b-cdef-0123-456789abcdef', 'Giám Đốc Điều Hành', 'CEO - Người điều hành cao nhất',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('2b3f4567-890c-defa-1234-56789abcdef0', 'Giám Đốc Tài Chính', 'CFO - Quản lý tài chính', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('3c4a5678-901d-efab-2345-6789abcdef01', 'Giám Đốc Nhân Sự', 'HRD - Quản lý nhân sự', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('4d5a6789-012e-fabc-3456-789abcdef012', 'Giám Đốc Công Nghệ', 'CTO - Quản lý công nghệ', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('5e6a7890-123f-abcd-4567-89abcdef0123', 'Giám Đốc Marketing', 'CMO - Quản lý marketing', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('6f7a8901-234a-bcde-5678-9abcdef01234', 'Giám Đốc Kinh Doanh', 'CSO - Quản lý bán hàng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('7a8b9012-345a-cdef-6789-abcdef012345', 'Trưởng Phòng Nhân Sự', 'Quản lý nhân sự cấp trung', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('8a9b0123-456a-defa-7890-bcdef0123456', 'Trưởng Phòng IT', 'Quản lý CNTT cấp trung', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('9a0b1234-567a-efab-8901-cdef01234567', 'Trưởng Phòng Marketing', 'Quản lý marketing cấp trung',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('0a1b2345-678a-faba-9012-def012345678', 'Trưởng Phòng Kinh Doanh', 'Quản lý kinh doanh cấp trung',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('1b2a3456-789a-abaa-0123-ef0123456789', 'Kế Toán Trưởng', 'Quản lý kế toán', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('2a3b4567-890a-aaaa-1234-f01234567890', 'Nhân Viên Nhân Sự', 'Nhân viên nhân sự', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('3a4b5678-901a-aaba-2345-0123456789ab', 'Lập Trình Viên', 'Phát triển phần mềm', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('4a5b6789-012a-abaa-3456-123456789abc', 'Nhân Viên Marketing', 'Nhân viên tiếp thị', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('5a6b7890-123a-abaa-4567-23456789abcd', 'Nhân Viên Kinh Doanh', 'Nhân viên bán hàng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('6a7b8901-234a-abaa-5678-3456789abcde', 'Kế Toán Viên', 'Nhân viên kế toán', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('7a8b9012-345a-abaa-6789-456789abcdef', 'Nhân Viên CSKH', 'Chăm sóc khách hàng', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- =============================================
-- USERS - Dữ liệu người dùng
-- =============================================
INSERT INTO users (id, first_name, last_name, email, birth_date, gender, phone_number, nationality, hire_date, address,
                   created_at, updated_at)
VALUES ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'Nguyễn', 'Văn An', 'admin@socius.com', '1985-05-15', 'male',
        '0901234567', 'Việt Nam', '2020-01-01', 'Quận 1, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('bb22cc33-dd44-ee55-ff66-778899001122', 'Trần', 'Thị Bình', 'hr@socius.com', '1988-07-21', 'female',
        '0912345678', 'Việt Nam', '2020-02-01', 'Quận 2, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('cc33dd44-ee55-ff66-7788-99001122aa44', 'Lê', 'Văn Cường', 'cto@socius.com', '1980-03-11', 'male', '0923456789',
        'Việt Nam', '2020-01-15', 'Quận 3, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('dd44ee55-ff66-7788-9900-1122aa44bb55', 'Phạm', 'Thị Dung', 'cfo@socius.com', '1982-11-05', 'female',
        '0934567890', 'Việt Nam', '2020-01-20', 'Quận 5, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('ee55ff66-7788-9900-1122-aa44bb55cc66', 'Hoàng', 'Văn Em', 'marketing@socius.com', '1990-09-18', 'male',
        '0945678901', 'Việt Nam', '2020-03-01', 'Quận 7, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('ff667788-9900-1122-aa44-bb55cc66dd77', 'Ngô', 'Thị Phương', 'sales@socius.com', '1987-04-27', 'female',
        '0956789012', 'Việt Nam', '2020-03-15', 'Quận 10, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00778899-0011-22aa-44bb-55cc66dd77ee', 'Đỗ', 'Văn Giang', 'dev1@socius.com', '1992-12-03', 'male',
        '0967890123', 'Việt Nam', '2021-01-05', 'Quận Tân Bình, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('a0889900-1122-aa44-bb55-cc66dd77ee88', 'Vũ', 'Thị Hương', 'dev2@socius.com', '1993-02-16', 'female',
        '0978901234', 'Việt Nam', '2021-01-10', 'Quận Phú Nhuận, TP HCM', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- Sửa hh -> a0
       ('a1990011-22aa-44bb-55cc-66dd77ee8899', 'Đinh', 'Văn Inote', 'staff1@socius.com', '1995-06-30', 'male',
        '0989012345', 'Việt Nam', '2021-02-01', 'Quận Bình Thạnh, TP HCM', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- Sửa ii -> a1
       ('a2001122-aa44-bb55-cc66-dd77ee889900', 'Lý', 'Thị Judo', 'staff2@socius.com', '1994-08-14', 'female',
        '0990123456', 'Việt Nam', '2021-02-15', 'Quận Gò Vấp, TP HCM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Sửa jj -> a2

-- =============================================
-- TEAMS - Nhóm làm việc
-- =============================================
INSERT INTO teams (id, name, leader_id, created_at, updated_at)
VALUES ('a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', 'Development Team', 'cc33dd44-ee55-ff66-7788-99001122aa44',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6e7', 'HR Team', 'bb22cc33-dd44-ee55-ff66-778899001122', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8', 'Marketing Team', 'ee55ff66-7788-9900-1122-aa44bb55cc66',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('d4e5f6a7-b8c9-d0e1-f2a3-b4c5d6e7f8a9', 'Finance Team', 'dd44ee55-ff66-7788-9900-1122aa44bb55',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('e5f6a7b8-c9d0-e1f2-a3b4-c5d6e7f8a9b0', 'Sales Team', 'ff667788-9900-1122-aa44-bb55cc66dd77', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- =============================================
-- PERIODS - Kỳ đánh giá
-- =============================================
INSERT INTO periods (id, name, type, start_date, end_date, status, description, created_at, updated_at)
VALUES ('aef98761-dc23-ba45-0987-6543fe21dc09', 'Quý 1/2023', 'monthly', '2023-01-01', '2023-03-31', 'active',
        'Kỳ đánh giá Q1 năm 2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('bf009872-ed34-cb56-1098-7654ff32ed10', 'Quý 2/2023', 'monthly', '2023-04-01', '2023-06-30', 'active',
        'Kỳ đánh giá Q2 năm 2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================
-- EMPLOYMENT_DETAILS - Chi tiết việc làm hiện tại
-- =============================================
INSERT INTO employment_details (id, user_id, position_id, department_id, team_id, role_id, start_date, salary,
                                working_status, created_at, updated_at)
VALUES ('a12bc34d-56ef-78ab-90cd-ef12ab34cd56', 'aa11bb22-cc33-dd44-ee55-ff6677889900',
        '1a2e3456-789b-cdef-0123-456789abcdef', '11cfb6e8-f843-4d91-b5c9-74e624b8186a', NULL,
        'a7b9f5d2-c6e3-47b1-9f8d-4e5c6f7a8b9c', '2020-01-01', 50000000, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('b23cd45e-67fa-89bc-01de-fa23bc45de67', 'bb22cc33-dd44-ee55-ff66-778899001122',
        '3c4a5678-901d-efab-2345-6789abcdef01', '33892e7c-ab4d-4927-9412-9825dabf0b1c',
        'b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6e7', 'c9d1f7f4-e8f5-49f3-b1f0-6f7f8f9f0f10', '2020-02-01', 35000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('c34de56f-78fa-90cd-12ef-fa34cd56ef78', 'cc33dd44-ee55-ff66-7788-99001122aa44',
        '4d5a6789-012e-fabc-3456-789abcdef012', '44cd57e1-8b93-4540-aec1-2435f76c669d',
        'a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', 'd0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', '2020-01-15', 40000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('d45ef67a-89fa-01de-23fa-fa45de67fa89', 'dd44ee55-ff66-7788-9900-1122aa44bb55',
        '2b3f4567-890c-defa-1234-56789abcdef0', '22456a8b-990c-4310-8313-7f69c64e0e1a',
        'd4e5f6a7-b8c9-d0e1-f2a3-b4c5d6e7f8a9', 'd0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', '2020-01-20', 38000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('e56fa78a-90fa-12ef-34fa-fa56ef78fa90', 'ee55ff66-7788-9900-1122-aa44bb55cc66',
        '5e6a7890-123f-abcd-4567-89abcdef0123', '55ef89f3-0dc4-4b61-a3af-7894d12de24e',
        'c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8', 'd0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', '2020-03-01', 37000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('f67fa89a-01fa-23fa-45fa-fa67fa89fa01', 'ff667788-9900-1122-aa44-bb55cc66dd77',
        '6f7a8901-234a-bcde-5678-9abcdef01234', '66ab34c2-1ef5-4782-ba9e-908712ab3c5f',
        'e5f6a7b8-c9d0-e1f2-a3b4-c5d6e7f8a9b0', 'd0e2f8f5-f6f7-49f4-c2f1-7f8f9f0f1f21', '2020-03-15', 36000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('a78fa90a-12fa-34fa-56fa-fa78fa90fa12', '00778899-0011-22aa-44bb-55cc66dd77ee',
        '3a4b5678-901a-aaba-2345-0123456789ab', '44cd57e1-8b93-4540-aec1-2435f76c669d',
        'a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', 'e1f3f9f6-f7f8-49f5-d3f2-8f9f0f1f2f32', '2021-01-05', 20000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('a89fa01a-23fa-45fa-67fa-fa89fa01fa23', 'a0889900-1122-aa44-bb55-cc66dd77ee88',
        '3a4b5678-901a-aaba-2345-0123456789ab', '44cd57e1-8b93-4540-aec1-2435f76c669d',
        'a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6', 'f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', '2021-01-10', 18000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Sửa user_id
       ('a90fa12a-34fa-56fa-78fa-fa90fa12fa34', 'a1990011-22aa-44bb-55cc-66dd77ee8899',
        '4a5b6789-012a-abaa-3456-123456789abc', '55ef89f3-0dc4-4b61-a3af-7894d12de24e',
        'c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8', 'f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', '2021-02-01', 15000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Sửa user_id
       ('a01fa23a-45fa-67fa-89fa-fa01fa23fa45', 'a2001122-aa44-bb55-cc66-dd77ee889900',
        '5a6b7890-123a-abaa-4567-23456789abcd', '66ab34c2-1ef5-4782-ba9e-908712ab3c5f',
        'e5f6a7b8-c9d0-e1f2-a3b4-c5d6e7f8a9b0', 'f2f4f0f7-f8f9-49f6-e4f3-9f0f1f2f3f43', '2021-02-15', 15000000,
        'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Sửa user_id

-- =============================================
-- ACCOUNT - Thông tin tài khoản đăng nhập
-- =============================================
-- Mật khẩu mặc định: Password@123 (đã hash với BCrypt)
INSERT INTO account (id, user_id, password, is_active, is_default_password, created_at, updated_at)
VALUES ('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'aa11bb22-cc33-dd44-ee55-ff6677889900',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e', 'bb22cc33-dd44-ee55-ff66-778899001122',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f', 'cc33dd44-ee55-ff66-7788-99001122aa44',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a', 'dd44ee55-ff66-7788-9900-1122aa44bb55',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b', 'ee55ff66-7788-9900-1122-aa44bb55cc66',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c', 'ff667788-9900-1122-aa44-bb55cc66dd77',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', '00778899-0011-22aa-44bb-55cc66dd77ee',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, true, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e', 'a0889900-1122-aa44-bb55-cc66dd77ee88',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, true, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f', 'a1990011-22aa-44bb-55cc-66dd77ee8899',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, true, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('0d1e2f3a-4b5c-6d7e-8f9a-0b1c2d3e4f5a', 'a2001122-aa44-bb55-cc66-dd77ee889900',
        '$2a$10$omcdy1jgJzxJK7eqoYftCuvJf.bpA1G4Cysn0Xzz6Eq2/NRDKLoNG', true, true, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);



-- Create direct conversations between users
INSERT INTO conversations (id, name, type, created_by, created_at, updated_at)
VALUES
    ('c1fd6e8a-da91-4dec-8821-5c4c076f1838', NULL, 'DIRECT', 'aa11bb22-cc33-dd44-ee55-ff6677889900', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('c2d85e9b-fa56-4571-bc40-e90e15523c22', NULL, 'DIRECT', 'bb22cc33-dd44-ee55-ff66-778899001122', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', NULL, 'DIRECT', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'IT Department Group', 'GROUP', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'Management Group', 'GROUP', 'aa11bb22-cc33-dd44-ee55-ff6677889900', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add members to direct conversations
INSERT INTO conversation_members (conversation_id, user_id, joined_at, role)
VALUES
    -- Conversation 1: Admin and HR Manager
    ('c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'aa11bb22-cc33-dd44-ee55-ff6677889900', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'bb22cc33-dd44-ee55-ff66-778899001122', CURRENT_TIMESTAMP, 'MEMBER'),

    -- Conversation 2: HR Manager and CTO
    ('c2d85e9b-fa56-4571-bc40-e90e15523c22', 'bb22cc33-dd44-ee55-ff66-778899001122', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c2d85e9b-fa56-4571-bc40-e90e15523c22', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, 'MEMBER'),

    -- Conversation 3: CTO and Developer 1
    ('c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', '00778899-0011-22aa-44bb-55cc66dd77ee', CURRENT_TIMESTAMP, 'MEMBER');

-- Add members to group conversations
INSERT INTO conversation_members (conversation_id, user_id, joined_at, role)
VALUES
    -- IT Department Group
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, 'ADMIN'),
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', '00778899-0011-22aa-44bb-55cc66dd77ee', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'a0889900-1122-aa44-bb55-cc66dd77ee88', CURRENT_TIMESTAMP, 'MEMBER'),

    -- Management Group
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'aa11bb22-cc33-dd44-ee55-ff6677889900', CURRENT_TIMESTAMP, 'ADMIN'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'bb22cc33-dd44-ee55-ff66-778899001122', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'cc33dd44-ee55-ff66-7788-99001122aa44', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'dd44ee55-ff66-7788-9900-1122aa44bb55', CURRENT_TIMESTAMP, 'MEMBER'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'ee55ff66-7788-9900-1122-aa44bb55cc66', CURRENT_TIMESTAMP, 'MEMBER');

-- Add messages to direct conversations
-- Add messages to direct conversations
INSERT INTO messages (id, conversation_id, sender_id, content, message_type, created_at, updated_at)
VALUES
    -- Conversation 1: Admin and HR Manager
    ('a1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6', 'c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'aa11bb22-cc33-dd44-ee55-ff6677889900', 'Chào Bình, hôm nay có báo cáo nhân sự chưa?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('b2b3c4d5-e6f7-a8b9-c0d1-e2f3a4b5c6d7', 'c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'bb22cc33-dd44-ee55-ff66-778899001122', 'Dạ có anh, em đã gửi qua email rồi ạ.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '5 minutes'),
    ('c3c4d5e6-f7a8-b9c0-d1e2-f3a4b5c6d7e8', 'c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'aa11bb22-cc33-dd44-ee55-ff6677889900', 'Tốt, cảm ơn em.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '10 minutes'),

    -- Conversation 2: HR Manager and CTO
    ('d4d5e6f7-a8b9-c0d1-e2f3-a4b5c6d7e8f9', 'c2d85e9b-fa56-4571-bc40-e90e15523c22', 'bb22cc33-dd44-ee55-ff66-778899001122', 'Anh Cường, em cần thông tin về team IT cho báo cáo quý.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('e5e6f7a8-b9c0-d1e2-f3a4-b5c6d7e8f9a0', 'c2d85e9b-fa56-4571-bc40-e90e15523c22', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'OK Bình, chiều nay anh gửi cho em.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '30 minutes'),

    -- Conversation 3: CTO and Developer 1
    ('f6f7a8b9-c0d1-e2f3-a4b5-c6d7e8f9a0b1', 'c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'Giang, tiến độ dự án tới đâu rồi?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
    ('a7a8b9c0-d1e2-f3a4-b5c6-d7e8f9a0b1c2', 'c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', '00778899-0011-22aa-44bb-55cc66dd77ee', 'Dạ, em hoàn thành được khoảng 80% rồi anh.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '5 minutes'),
    ('b8b9c0d1-e2f3-a4b5-c6d7-e8f9a0b1c2d3', 'c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'Tốt, cố gắng hoàn thành đúng deadline nhé.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '10 minutes'),
    ('c9c0d1e2-f3a4-b5c6-d7e8-f9a0b1c2d3e4', 'c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', '00778899-0011-22aa-44bb-55cc66dd77ee', 'Vâng ạ.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '15 minutes');

-- Add messages to group conversations
INSERT INTO messages (id, conversation_id, sender_id, content, message_type, created_at, updated_at)
VALUES
    -- IT Department Group
    ('d0d1e2f3-a4b5-c6d7-e8f9-a0b1c2d3e4f5', 'c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'Chào cả team, hôm nay 2h chiều họp sprint nhé.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
    ('e1e2f3a4-b5c6-d7e8-f9a0-b1c2d3e4f5a6', 'c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', '00778899-0011-22aa-44bb-55cc66dd77ee', 'Vâng anh.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '5 minutes'),
    ('f2f3a4b5-c6d7-e8f9-a0b1-c2d3e4f5a6b7', 'c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'a0889900-1122-aa44-bb55-cc66dd77ee88', 'Em có mặt ạ.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '8 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '8 minutes'),
    ('a3a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8', 'c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'Nhớ chuẩn bị cập nhật tiến độ các task nhé.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '15 minutes'),

    -- Management Group
    ('b4b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'aa11bb22-cc33-dd44-ee55-ff6677889900', 'Kính chào ban lãnh đạo, cuộc họp quý sẽ diễn ra vào thứ 6 tuần này.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('c5c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'dd44ee55-ff66-7788-9900-1122aa44bb55', 'Tôi sẽ chuẩn bị báo cáo tài chính.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '30 minutes'),
    ('d6d7e8f9-a0b1-c2d3-e4f5-a6b7c8d9e0f1', 'c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'ee55ff66-7788-9900-1122-aa44bb55cc66', 'Phòng Marketing sẽ có bài trình bày về chiến lược Q2.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '45 minutes'),
    ('e7e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2', 'c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'cc33dd44-ee55-ff66-7788-99001122aa44', 'IT Department sẽ update tình hình các dự án công nghệ.', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '1 hour');

-- Add message with file attachment
INSERT INTO messages (id, conversation_id, sender_id, content, message_type, file_url, file_size, file_original_name, file_content_type, created_at, updated_at)
VALUES
    ('f8f9a0b1-c2d3-e4f5-a6b7-c8d9e0f1a2b3', 'c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'bb22cc33-dd44-ee55-ff66-778899001122', 'Báo cáo nhân sự Q1/2023', 'FILE', '/uploads/hr_report_q1_2023.pdf', 1258000, 'hr_report_q1_2023.pdf', 'application/pdf', CURRENT_TIMESTAMP - INTERVAL '20 hours', CURRENT_TIMESTAMP - INTERVAL '20 hours'),
    ('a9a0b1c2-d3e4-f5a6-b7c8-d9e0f1a2b3c4', 'c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', '00778899-0011-22aa-44bb-55cc66dd77ee', 'Project screenshot', 'IMAGE', '/uploads/project_screenshot.png', 358000, 'project_screenshot.png', 'image/png', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours');

-- Set message status (read/unread)
INSERT INTO message_status (message_id, user_id, is_read, read_at)
VALUES
    -- Conversation 1 messages
    ('a1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6', 'bb22cc33-dd44-ee55-ff66-778899001122', true, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 minutes'),
    ('b2b3c4d5-e6f7-a8b9-c0d1-e2f3a4b5c6d7', 'aa11bb22-cc33-dd44-ee55-ff6677889900', true, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '7 minutes'),
    ('c3c4d5e6-f7a8-b9c0-d1e2-f3a4b5c6d7e8', 'bb22cc33-dd44-ee55-ff66-778899001122', true, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '12 minutes'),

    -- Conversation 2 messages
    ('d4d5e6f7-a8b9-c0d1-e2f3-a4b5c6d7e8f9', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '5 minutes'),
    ('e5e6f7a8-b9c0-d1e2-f3a4-b5c6d7e8f9a0', 'bb22cc33-dd44-ee55-ff66-778899001122', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '35 minutes'),

    -- Conversation 3 messages - some unread
    ('f6f7a8b9-c0d1-e2f3-a4b5-c6d7e8f9a0b1', '00778899-0011-22aa-44bb-55cc66dd77ee', true, CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '3 minutes'),
    ('a7a8b9c0-d1e2-f3a4-b5c6-d7e8f9a0b1c2', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '6 minutes'),
    ('b8b9c0d1-e2f3-a4b5-c6d7-e8f9a0b1c2d3', '00778899-0011-22aa-44bb-55cc66dd77ee', false, NULL),
    ('c9c0d1e2-f3a4-b5c6-d7e8-f9a0b1c2d3e4', 'cc33dd44-ee55-ff66-7788-99001122aa44', false, NULL),

    -- Group conversations - some messages unread by some members
    ('d0d1e2f3-a4b5-c6d7-e8f9-a0b1c2d3e4f5', '00778899-0011-22aa-44bb-55cc66dd77ee', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '2 minutes'),
    ('d0d1e2f3-a4b5-c6d7-e8f9-a0b1c2d3e4f5', 'a0889900-1122-aa44-bb55-cc66dd77ee88', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '3 minutes'),
    ('e1e2f3a4-b5c6-d7e8-f9a0-b1c2d3e4f5a6', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '6 minutes'),
    ('e1e2f3a4-b5c6-d7e8-f9a0-b1c2d3e4f5a6', 'a0889900-1122-aa44-bb55-cc66dd77ee88', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '7 minutes'),
    ('f2f3a4b5-c6d7-e8f9-a0b1-c2d3e4f5a6b7', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '9 minutes'),
    ('f2f3a4b5-c6d7-e8f9-a0b1-c2d3e4f5a6b7', '00778899-0011-22aa-44bb-55cc66dd77ee', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '10 minutes'),
    ('a3a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8', '00778899-0011-22aa-44bb-55cc66dd77ee', true, CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '16 minutes'),
    ('a3a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8', 'a0889900-1122-aa44-bb55-cc66dd77ee88', false, NULL),

    -- Management group - some unread messages
    ('b4b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'bb22cc33-dd44-ee55-ff66-778899001122', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '5 minutes'),
    ('b4b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '10 minutes'),
    ('b4b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'dd44ee55-ff66-7788-9900-1122aa44bb55', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '15 minutes'),
    ('b4b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'ee55ff66-7788-9900-1122-aa44bb55cc66', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '20 minutes'),
    ('c5c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'aa11bb22-cc33-dd44-ee55-ff6677889900', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '32 minutes'),
    ('c5c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'bb22cc33-dd44-ee55-ff66-778899001122', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '33 minutes'),
    ('c5c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'cc33dd44-ee55-ff66-7788-99001122aa44', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '34 minutes'),
    ('c5c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'ee55ff66-7788-9900-1122-aa44bb55cc66', false, NULL);

-- Set unread counts
INSERT INTO unread_counts (conversation_id, user_id, unread_count, last_read_message_id)
VALUES
    -- Direct conversations
    ('c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'aa11bb22-cc33-dd44-ee55-ff6677889900', 0, 'c3c4d5e6-f7a8-b9c0-d1e2-f3a4b5c6d7e8'),
    ('c1fd6e8a-da91-4dec-8821-5c4c076f1838', 'bb22cc33-dd44-ee55-ff66-778899001122', 0, 'c3c4d5e6-f7a8-b9c0-d1e2-f3a4b5c6d7e8'),
    ('c2d85e9b-fa56-4571-bc40-e90e15523c22', 'bb22cc33-dd44-ee55-ff66-778899001122', 0, 'e5e6f7a8-b9c0-d1e2-f3a4-b5c6d7e8f9a0'),
    ('c2d85e9b-fa56-4571-bc40-e90e15523c22', 'cc33dd44-ee55-ff66-7788-99001122aa44', 0, 'e5e6f7a8-b9c0-d1e2-f3a4-b5c6d7e8f9a0'),
    ('c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', 'cc33dd44-ee55-ff66-7788-99001122aa44', 1, 'a7a8b9c0-d1e2-f3a4-b5c6-d7e8f9a0b1c2'),
    ('c38f4a2c-4d7b-4fae-8e91-b0c245d89d33', '00778899-0011-22aa-44bb-55cc66dd77ee', 1, 'a7a8b9c0-d1e2-f3a4-b5c6-d7e8f9a0b1c2'),

    -- Group conversations - IT Department
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'cc33dd44-ee55-ff66-7788-99001122aa44', 0, 'a3a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8'),
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', '00778899-0011-22aa-44bb-55cc66dd77ee', 0, 'a3a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8'),
    ('c4a16b3d-2e8c-4fc9-b532-c51a77de1a44', 'a0889900-1122-aa44-bb55-cc66dd77ee88', 1, 'f2f3a4b5-c6d7-e8f9-a0b1-c2d3e4f5a6b7'),

    -- Group conversations - Management
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'aa11bb22-cc33-dd44-ee55-ff6677889900', 0, 'e7e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'bb22cc33-dd44-ee55-ff66-778899001122', 0, 'e7e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'cc33dd44-ee55-ff66-7788-99001122aa44', 0, 'e7e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'dd44ee55-ff66-7788-9900-1122aa44bb55', 0, 'e7e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2'),
    ('c5b27c4e-3f9d-40da-c653-d62b88ef2b55', 'ee55ff66-7788-9900-1122-aa44bb55cc66', 1, 'd6d7e8f9-a0b1-c2d3-e4f5-a6b7c8d9e0f1');