package com.songsy.core.web.filter;

import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

/**
 * 自定义编码拦截器
 * 解决get请求中文乱码问题
 * @author songshuiyang
 * @date 2018/2/10 16:43
 */
public class CustomEncodingFilter extends OncePerRequestFilter {
    private String encoding;

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 设置请求响应字符编码
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        // 传递给目标servlet或jsp的实际上是动态代理的对象，而不是原始的HttpServletRequest对象
        request = (HttpServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), new MyInvacationHandler(request));
        chain.doFilter(request, response);
    }

    class MyInvacationHandler implements InvocationHandler {
        private HttpServletRequest request;
        MyInvacationHandler(HttpServletRequest request){
            this.request=request;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getParameter":
                    String value = request.getParameter((String)args[0]);
                    try {
                        if(value != null){
                            value=new String(value.getBytes("ISO-8859-1"),encoding);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return value;
                case "getParameterValues":
                    String[] values = request.getParameterValues((String)args[0]);
                    if (values != null) {
                        for (int i = 0; i < values.length; i++) {
                            try {
                                values[i] = new String(values[i].getBytes("ISO-8859-1"),encoding);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return values;
                default:
                    return method.invoke(request, args);
            }
        }

    }
}
