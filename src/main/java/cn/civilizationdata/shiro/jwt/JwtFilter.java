package cn.civilizationdata.shiro.jwt;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {
    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest req = WebUtils.toHttp(request);
        HttpServletResponse resp = WebUtils.toHttp(response);
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            resp.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.onPreHandle(request, response, mappedValue);
    }

    /**
     * 状态是否登录(JwtToken)
     */
    @Override
    protected boolean isLoginAttempt(String authzHeader) {
        return authzHeader.split(" ").length == 2;
    }

    /**
     * 生成JwtToken
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        return new JwtToken(this.getAuthzHeader(request).split(" ")[1]);
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        try {
            HttpServletResponse resp = WebUtils.toHttp(response);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您尚未登陆或状态已失效");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
