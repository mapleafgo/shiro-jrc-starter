package cn.civilizationdata.shiro.casbin;

import cn.civilizationdata.shiro.casbin.annotation.RequiresCasbin;
import cn.civilizationdata.shiro.casbin.aop.CasbinAnnotationsAuthorizingMethodInterceptor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class CasbinAdvisor extends StaticMethodMatcherPointcutAdvisor {
    public CasbinAdvisor() {
        setAdvice(new CasbinAnnotationsAuthorizingMethodInterceptor());
    }

    @Override
    public boolean matches(@NonNull Method method, Class<?> targetClass) {
        Method m = method;

        if (checkAnnotation(m)) {
            return true;
        }

        if (targetClass != null) {
            try {
                m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                return checkAnnotation(m) || checkAnnotation(targetClass);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return false;
    }

    private <T extends AnnotatedElement> boolean checkAnnotation(T m) {
        return AnnotationUtils.findAnnotation(m, RequiresCasbin.class) != null;
    }
}
