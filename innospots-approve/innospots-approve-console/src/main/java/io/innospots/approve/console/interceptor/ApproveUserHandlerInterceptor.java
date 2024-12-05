package io.innospots.approve.console.interceptor;

import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.service.SysUserReadService;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.CCH;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/5
 */
@Slf4j
public class ApproveUserHandlerInterceptor implements HandlerInterceptor {

    private SysUserReadService userReadService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (userReadService == null) {
            userReadService = BeanContextAwareUtils.getBean(SysUserReadService.class);
        }
        UserInfo userInfo = this.userReadService.getUserInfo(CCH.userId());
        ApproveHolder.setLoginUser(userInfo);
        log.info("approve user interceptor user info:{}",userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ApproveHolder.remove();
    }
}
