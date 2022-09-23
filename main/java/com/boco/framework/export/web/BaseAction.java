package com.boco.framework.export.web;

import java.lang.reflect.Field;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ClassUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;

public abstract class BaseAction extends DispatchAction
{
  public static final int TIMEOUT_SEC = 2000;
  public static final String TIMEOUT_DESC = "您操作太频繁,系统正在处理你的请求，请稍后再试！";
  public static final int ROWCOUNTS = 5;
  public static final String MESSAGETITLE = "页中:";
  public static final String SUCCESS = "success";
  public static final String FAILURE = "failure";
  public static final String DIRECTLY_MESSAGE_KEY = "message";
  public static final String DIRECTLY_ERROR_KEY = "error";
  protected static final String ERRORPATH = "/error.jsp";
  protected static final String BUSINESSERRORPATH = "/businessError.jsp";
  protected static final String MANAGERNOTFOUNDERRORPATH = "/managerNotFoundError.jsp";
  private String appPath = null;

  private String contextPath = "";

  public ActionForward dispatchMethod(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String name)
    throws Exception
  {
    name = setMethodName(name);

    setAppPath(request.getSession().getServletContext().getRealPath("/"));

    setContextPath(request.getContextPath());

    String methodName = (String)request.getSession()
      .getAttribute("methodName");
    String times = (String)request.getSession().getAttribute("times");
    if (times == null)
      times = "0";
    int intTimes = Integer.parseInt(times);
    if (methodName == null)
      methodName = "";
    if (name == null)
      name = "";
    if ((methodName.indexOf("save") > -1) || (name.indexOf("save") > -1)) {
      request.setAttribute("saveMessage", "操作成功！");
    }
    if ((methodName.indexOf("publish") > -1) || (name.indexOf("publish") > -1)) {
      request.setAttribute("saveMessage", "操作成功！");
    }
    if ((methodName.indexOf("init") > -1) || (methodName.indexOf("findProjectInfoByProjectId") > -1) || 
      (methodName.indexOf("bulletinAction") > -1)) {
      return super.dispatchMethod(mapping, form, request, response, name);
    }
    Long oldDate = (Long)request.getSession().getAttribute("date");
    if (methodName.equals(name)) {
      String clazzName = ClassUtils.getShortClassName(getClass());

      if ((clazzName.equals("BulletinAction")) && (methodName.equals("getInfo")))
      {
        if ((intTimes >= 6) && (oldDate != null) && 
          (System.currentTimeMillis() - oldDate.longValue() <= 2000L)) {
          request.setAttribute("message", "您操作太频繁,系统正在处理你的请求，请稍后再试！您在2秒内连续重复了" + 
            intTimes + 
            "次.");
          request.getSession().setAttribute("times", "0");
          return new ActionForward("/error.jsp");
        }
      }
    }
    intTimes++;
    request.getSession().setAttribute("methodName", name);
    request.getSession().setAttribute("times", String.valueOf(intTimes));
    request.getSession().setAttribute("date", 
      new Long(System.currentTimeMillis()));

    return super.dispatchMethod(mapping, form, request, response, name);
  }
  protected String setMethodName(String name) {
    return name;
  }

  protected Object getModel(ActionForm actionForm, String modelName)
  {
    return ((DynaActionForm)actionForm).get(modelName);
  }

  private Field[] getFieldsFromClass(Class beanClass) {
    Field[] fields = (Field[])null;
    fields = beanClass.getDeclaredFields();
    Class superclass = beanClass.getSuperclass();
    while (superclass != null) {
      Field[] tempFields = superclass.getDeclaredFields();
      fields = concat(fields, tempFields);
      superclass = superclass.getSuperclass();
    }
    return fields;
  }

  private Field[] concat(Field[] left, Field[] right) {
    Field[] target = new Field[left.length + right.length];
    System.arraycopy(left, 0, target, 0, left.length);
    System.arraycopy(right, 0, target, left.length, right.length);
    return target;
  }

  public void setAppPath(String appPath)
  {
    this.appPath = appPath;
  }

  public String getContextPath() {
    return this.contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }
}