package org.socius.sociuswebbackend.config;

public final class PermissionConstants {
    // =================== USER & ACCOUNT MANAGEMENT ===================
    // Quản lý thông tin cá nhân và tài khoản đăng nhập
    public static final String USER_CREATE = "USER_CREATE";               // Tạo người dùng mới 
    public static final String USER_DELETE = "USER_DELETE";               // Xóa người dùng 
    public static final String USER_READ_OWN = "USER_READ_OWN";           // Đọc thông tin cá nhân của chính mình
    public static final String USER_READ_TEAM = "USER_READ_TEAM";         // Đọc thông tin người dùng trong nhóm
    public static final String USER_READ_DEPARTMENT = "USER_READ_DEPARTMENT"; // Đọc thông tin người dùng trong phòng ban
    public static final String USER_READ_ALL = "USER_READ_ALL";           // Đọc tất cả người dùng 
    public static final String USER_UPDATE_OWN = "USER_UPDATE_OWN";       // Sửa thông tin cá nhân của chính mình
    public static final String USER_UPDATE_ALL = "USER_UPDATE_ALL";       // Sửa tất cả người dùng 
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";       // Thay đổi mật khẩu của chính mình
    public static final String RESET_USER_PASSWORD = "RESET_USER_PASSWORD"; // Đặt lại mật khẩu cho người dùng khác 

    // =================== EMPLOYEE & HR MANAGEMENT ===================
    // Quản lý thông tin nhân sự, hợp đồng, lương...
    public static final String EMPLOYEE_CREATE = "EMPLOYEE_CREATE";       // Tạo hồ sơ nhân viên mới 
    public static final String EMPLOYEE_READ_ALL = "EMPLOYEE_READ_ALL";   // Đọc tất cả hồ sơ nhân viên 
    public static final String EMPLOYEE_TERMINATE = "EMPLOYEE_TERMINATE"; // Chấm dứt hợp đồng nhân viên 
    public static final String EMPLOYEE_TRANSFER_TEAM = "EMPLOYEE_TRANSFER_TEAM"; // Chuyển nhân viên sang team khác
    public static final String EMPLOYEE_TRANSFER_DEPARTMENT = "EMPLOYEE_TRANSFER_DEPARTMENT"; // Chuyển nhân viên sang phòng ban khác
    public static final String EMPLOYEE_TRANSFER_POSITION = "EMPLOYEE_TRANSFER_POSITION"; // Chuyển nhân viên sang vị trí khác
    public static final String EMPLOYEE_TRANSFER_ROLE = "EMPLOYEE_TRANSFER_ROLE"; // Chuyển nhân viên sang vai trò khác
    public static final String EMPLOYEE_TEAM_DELETE = "EMPLOYEE_TEAM_DELETE"; // Xóa nhân viên khỏi team
    public static final String EMPLOYEE_DEPARTMENT_DELETE = "EMPLOYEE_DEPARTMENT_DELETE"; // Xóa nhân viên khỏi phòng ban
    public static final String EMPLOYEE_POSITION_DELETE = "EMPLOYEE_POSITION_DELETE"; // Xóa nhân viên khỏi vị trí công việc
    public static final String EMPLOYEE_ROLE_DELETE = "EMPLOYEE_ROLE_DELETE"; // Xóa nhân viên khỏi vai trò

    public static final String EMPLOYEE_SALARY_VIEW_OWN = "EMPLOYEE_SALARY_VIEW_OWN"; // Xem lương của chính mình
    public static final String EMPLOYEE_SALARY_VIEW_ALL = "EMPLOYEE_SALARY_VIEW_ALL"; // Xem lương tất cả nhân viên
    public static final String EMPLOYEE_SALARY_UPDATE = "EMPLOYEE_SALARY_UPDATE"; // Cập nhật thông tin lương 
    public static final String EMPLOYEE_HISTORY_VIEW_OWN = "EMPLOYEE_HISTORY_VIEW_OWN"; // Xem lịch sử của chính mình
    public static final String EMPLOYEE_HISTORY_VIEW_ALL = "EMPLOYEE_HISTORY_VIEW_ALL"; // Xem lịch sử tất cả nhân viên

    // =================== RBAC (ROLE, PERMISSION) MANAGEMENT ===================
    public static final String ROLE_CREATE = "ROLE_CREATE";               // Tạo vai trò 
    public static final String ROLE_READ = "ROLE_READ";                   // Đọc thông tin vai trò
    public static final String ROLE_UPDATE = "ROLE_UPDATE";               // Sửa vai trò 
    public static final String ROLE_DELETE = "ROLE_DELETE";               // Xóa vai trò 
    public static final String ROLE_ASSIGN = "ROLE_ASSIGN";               // Gán vai trò cho nhân viên
    public static final String ROLE_GET_ALL = "ROLE_GET_ALL";             // Lấy danh sách tất cả vai trò
    public static final String ADD_PERMISSION_TO_ROLE = "ADD_PERMISSION_TO_ROLE"; // Thêm quyền vào vai trò
    public static final String REMOVE_PERMISSION_FROM_ROLE = "REMOVE_PERMISSION_FROM_ROLE"; // Xóa quyền khỏi vai trò

    // =================== ORGANIZATION (TEAM, DEPARTMENT, POSITION) MANAGEMENT ===================
    public static final String TEAM_CREATE = "TEAM_CREATE";               // Tạo nhóm 
    public static final String TEAM_READ = "TEAM_READ";                   // Đọc thông tin nhóm
    public static final String TEAM_UPDATE = "TEAM_UPDATE";               // Sửa thông tin nhóm 
    public static final String TEAM_DELETE = "TEAM_DELETE";               // Xóa nhóm
    public static final String TEAM_ASSIGN = "TEAM_ASSIGN";              // Gán nhân viên vào nhóm
    public static final String TEAM_GET_ALL = "TEAM_GET_ALL";             // Lấy danh sách tất cả nhóm 

    public static final String DEPARTMENT_CREATE = "DEPARTMENT_CREATE";   // Tạo phòng ban 
    public static final String DEPARTMENT_READ = "DEPARTMENT_READ";       // Đọc thông tin phòng ban
    public static final String DEPARTMENT_UPDATE = "DEPARTMENT_UPDATE";   // Sửa thông tin phòng ban 
    public static final String DEPARTMENT_DELETE = "DEPARTMENT_DELETE";   // Xóa phòng ban
    public static final String DEPARTMENT_ASSIGN = "DEPARTMENT_ASSIGN";   // Gán nhân viên vào phòng ban
    public static final String DEPARTMENT_GET_ALL = "DEPARTMENT_GET_ALL"; // Lấy danh sách tất cả phòng ban 
    public static final String DEPARTMENT_VIEW_MEMBERS = "DEPARTMENT_VIEW_MEMBERS"; // Xem thành viên trong phòng ban

    public static final String POSITION_CREATE = "POSITION_CREATE";       // Tạo vị trí công việc 
    public static final String POSITION_READ = "POSITION_READ";           // Đọc thông tin vị trí công việc
    public static final String POSITION_UPDATE = "POSITION_UPDATE";       // Sửa thông tin vị trí công việc 
    public static final String POSITION_DELETE = "POSITION_DELETE";       // Xóa vị trí công việc
    public static final String POSITION_ASSIGN = "POSITION_ASSIGN";       // Gán nhân viên vào vị trí công việc
    public static final String POSITION_GET_ALL = "POSITION_GET_ALL";     // Lấy danh sách tất cả vị trí công việc
    public static final String POSITION_VIEW_MEMBERS = "POSITION_VIEW_MEMBERS"; // Xem thành viên trong vị trí công việc

    // =================== TASK MANAGEMENT ===================
    public static final String TASK_CREATE = "TASK_CREATE";               // Tạo task
    public static final String TASK_READ_OWN = "TASK_READ_OWN";           // Đọc task của chính mình
    public static final String TASK_READ_TEAM = "TASK_READ_TEAM";         // Đọc task của thành viên trong nhóm
    public static final String TASK_READ_ALL = "TASK_READ_ALL";           // Đọc tất cả task
    public static final String TASK_UPDATE_STATUS = "TASK_UPDATE_STATUS"; // Cập nhật trạng thái task
    public static final String TASK_TEAM_GET = "TASK_TEAM_GET";           // Lấy task của team

    // =================== NOTIFICATION MANAGEMENT ===================
    public static final String NOTIFICATION_CREATE = "NOTIFICATION_CREATE"; // Tạo thông báo 
    public static final String NOTIFICATION_READ_OWN = "NOTIFICATION_READ_OWN"; // Đọc thông báo của chính mình
    public static final String NOTIFICATION_GET_ALL = "NOTIFICATION_GET_ALL"; // Lấy tất cả thông báo

    // =================== CHAT & FILE MANAGEMENT ===================
    public static final String CHAT_ACCESS = "CHAT_ACCESS";               // Quyền truy cập và sử dụng tính năng chat
    public static final String CHATBOT_ACCESS = "CHATBOT_ACCESS";         // Truy cập chatbot
    public static final String FILE_UPLOAD_AVATAR = "FILE_UPLOAD_AVATAR"; // Tải lên ảnh đại diện
    public static final String FILE_ACCESS_ATTACHMENT = "FILE_ACCESS_ATTACHMENT"; // Tải lên/tải xuống file đính kèm
}