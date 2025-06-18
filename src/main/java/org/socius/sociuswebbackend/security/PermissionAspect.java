package org.socius.sociuswebbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);
    private final AuthenticationService authenticationService;

    /**
     * Phương thức này sẽ được gọi trước khi thực hiện các phương thức có annotation @RequirePermission.
     * Nó sẽ kiểm tra quyền truy cập của người dùng dựa trên các quyền được yêu cầu.
     *
     * @param joinPoint         Điểm nối của AOP, chứa thông tin về phương thức đang được gọi.
     * @param requirePermission Annotation chứa thông tin về quyền cần kiểm tra.
     * @return Kết quả của phương thức nếu người dùng có quyền truy cập, ngược lại sẽ ném ra AccessDeniedException.
     * @throws Throwable Nếu có lỗi xảy ra trong quá trình thực thi phương thức.
     */
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        logger.debug("Kiểm tra quyền truy cập cho {}.{} với yêu cầu: {}", className, methodName, requirePermission);

        try {
            // Validate annotation
            validateRequirePermissionAnnotation(requirePermission, methodName);

            HttpServletRequest request = getCurrentRequest();

            // Get user permissions once
            UserPermissionsDto userPermissions = authenticationService.getCurrentUserPermissions(request);
            if (userPermissions == null || userPermissions.getPermissions() == null) {
                throw new AccessDeniedException(SecurityConstants.AUTHENTICATION_REQUIRED_MESSAGE);
            }

            String[] requiredPermissions = requirePermission.value();
            RequirePermission.LogicalOperator operator = requirePermission.operator();

            boolean hasPermission = checkPermissions(userPermissions.getPermissions(), requiredPermissions, operator);

            if (!hasPermission) {
                String deniedMessage = String.format(SecurityConstants.ACCESS_DENIED_MESSAGE, String.join(", ", requiredPermissions));
                throw new AccessDeniedException(deniedMessage);
            }

            logger.debug("Quyền truy cập cho {}.{} được xác nhận", className, methodName);
            return joinPoint.proceed();

        } catch (AccessDeniedException e) {
            logger.warn("Quyền truy cập bị từ chối cho {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi kiểm tra quyền truy cập cho {}.{}: {}", className, methodName, e.getMessage(), e);
            throw new AccessDeniedException("Lỗi hệ thống khi kiểm tra quyền hạn");
        }
    }

    private void validateRequirePermissionAnnotation(RequirePermission requirePermission, String methodName) {
        if (requirePermission == null || requirePermission.value() == null || requirePermission.value().length == 0) {
            logger.warn("Cấu hình quyền hạn không hợp lệ cho phương thức: {}", methodName);
            throw new AccessDeniedException("Cấu hình quyền hạn không hợp lệ");
        }
    }

    private boolean checkPermissions(Set<String> userPermissions, String[] requiredPermissions, RequirePermission.LogicalOperator operator) {
        if (operator == RequirePermission.LogicalOperator.AND) {
            return Arrays.stream(requiredPermissions).allMatch(userPermissions::contains);
        } else {
            return Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest();
        } catch (IllegalStateException e) {
            throw new AccessDeniedException(SecurityConstants.REQUEST_CONTEXT_ERROR_MESSAGE + ": " + e.getMessage());
        }
    }
}
