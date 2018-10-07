package org.leoliu.framework;

import org.leoliu.framework.bean.Data;
import org.leoliu.framework.bean.Handler;
import org.leoliu.framework.bean.Param;
import org.leoliu.framework.bean.View;
import org.leoliu.framework.helper.BeanHelper;
import org.leoliu.framework.helper.ConfigHelper;
import org.leoliu.framework.helper.ControllerHelper;
import org.leoliu.framework.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化相关Helper类
        HelperLoader.init();
        //获取ServletContext对象用于注册Servlet
        ServletContext servletContext = config.getServletContext();
        //注册处理jsp的Servlet
        ServletRegistration jspServlte = servletContext.getServletRegistration("jsp");
        jspServlte.addMapping(ConfigHelper.getAppJspPath() + "*");
        //注册处理静态资源的默认Servlet
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取请求方法与请求路径
        String requestMethod = req.getMethod().toLowerCase();
        String requestPath = req.getPathInfo();
        //获取Action处理器
        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
        if (handler != null){
            //获取Controller类及其Bean对象
            Class<?> controllerClass = handler.getControllerClass();
            Object controllerBean = BeanHelper.getBean(controllerClass);
            //创建请求参数对象
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Enumeration<String> paramNames = req.getParameterNames();
            while (paramNames.hasMoreElements()){
                String paramName = paramNames.nextElement();
                String paramValue = req.getParameter(paramName);
                paramMap.put(paramName, paramValue);
                String body = CodeUtil.decodeURL(StreamUtil.getString(req.getInputStream()));
                if (StringUtil.isNotEmpty(body)){
                    String[] params = StringUtil.splitString(body, "&");
                    if (ArrayUtil.isNotEmpty(params)){
                        for (String param : params){
                            String[] array = StringUtil.splitString(param, "=");
                            if (ArrayUtil.isNotEmpty(array) && array.length == 2){
                                String Name = array[0];
                                String Value = array[1];
                                paramMap.put(Name, Value);
                            }
                        }
                    }
                }
                Param param = new Param(paramMap);
                //****调用Action方法
                Method actionMethod = handler.getActionMethod();
                Object result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
                //处理Action方法的返回值
                if (result instanceof View){
                    //返回JSP页面
                    View view = (View) result;
                    String path = view.getPath();
                    if (StringUtil.isNotEmpty(path)){
                        if (path.startsWith("/")){
                            resp.sendRedirect(req.getContextPath() + path);
                        } else {
                            Map<String, Object> model = view.getModel();
                            for (Map.Entry<String, Object> entry: model.entrySet()){
                                req.setAttribute(entry.getKey(), entry.getValue());
                            }
                            req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, resp);
                        }
                    }
                } else if (result instanceof Data){
                    //返回Json数据
                    Data data = (Data) result;
                    Object model = data.getModel();
                    if (model != null){
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        PrintWriter writer = resp.getWriter();
                        String json = JsonUtil.toJson(model);
                        writer.write(json);
                        writer.flush();
                        writer.close();
                    }
                }
            }
        }
    }
}
