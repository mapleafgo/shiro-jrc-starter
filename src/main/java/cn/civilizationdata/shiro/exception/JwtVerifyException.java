package cn.civilizationdata.shiro.exception;

import org.apache.shiro.authc.IncorrectCredentialsException;

public class JwtVerifyException extends IncorrectCredentialsException {
    public JwtVerifyException(String message) {
        super(message);
    }

    public JwtVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
