package com.boco.framework.export.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class WebUtils
{
  private static Log log = LogFactory.getLog(WebUtils.class);

  public static List getBeanListForRequest(HttpServletRequest request, Class beanClass)
  {
    List values = new ArrayList();
    getBeanListValue(values, beanClass, "", request);
    return values;
  }

  

  private static void getBeanListValue(List values, Class beanClass, String fieldName, HttpServletRequest request)
  {
    Field[] fields = getFieldsFromClass(beanClass);
    Map map = new HashMap();
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      String name = field.getName();
      if (field.getType().getName().indexOf("boco") > 0) {
        if ((fieldName != null) && (!"".equals(fieldName))) {
          if (fieldName.indexOf(".") > 0) {
            continue;
          }
          name = fieldName + "." + name;
        }
        List fieldValues = new ArrayList();
        getBeanListValue(fieldValues, field.getType(), name, request);
        map.put(field.getName(), fieldValues);
      }
      else {
        String valueName = "";
        if ((fieldName != null) && (!"".equals(fieldName))) {
          valueName = fieldName + "." + name;
        }
        else {
          valueName = name;
        }
        String[] fieldvalues = request.getParameterValues(valueName);
        if (fieldvalues != null) {
          List fieldValues = new ArrayList();
          CollectionUtils.addAll(fieldValues, fieldvalues);
          map.put(field.getName(), fieldValues);
        }
      }
    }

    Set fildValueSet = map.entrySet();
    if (fildValueSet.size() == 0) {
      return;
    }
    int count = 0;
    for (Iterator iterator = fildValueSet.iterator(); iterator.hasNext(); ) {
      Map.Entry fieldEntry = (Map.Entry)iterator.next();
      List fildValueList = (List)fieldEntry.getValue();
      if (fildValueList.size() == 0) {
        continue;
      }
      count = fildValueList.size();
      break;
    }
    for (int i = 0; i < count; i++) {
      try {
        values.add(beanClass.newInstance());
      }
      catch (InstantiationException e) {
        log.error(e.getMessage(), e);
      }
      catch (IllegalAccessException e) {
        log.error(e.getMessage(), e);
      }
    }
//    Set fildNameSet = map.keySet();
//    for (int i = 0; i < count; i++)
//      for (Iterator iterator = fildNameSet.iterator(); iterator.hasNext(); )
//        try {
//          String fName = (String)iterator.next();
//          List fValue = (List)map.get(fName);
//          if (fValue.isEmpty()) {
//            continue;
//          }
////          BeanUtils.setProperty(values.get(i), fName, fValue.get(i));
//        }
//        catch (IllegalAccessException e) {
//          log.error(e.getMessage(), e);
//        }
//        catch (InvocationTargetException e) {
//          log.error(e.getMessage(), e);
//        }
  }

  private static Field[] getFieldsFromClass(Class beanClass)
  {
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

  private static Field[] concat(Field[] left, Field[] right) {
    Field[] target = new Field[left.length + right.length];
    System.arraycopy(left, 0, target, 0, left.length);
    System.arraycopy(right, 0, target, left.length, right.length);
    return target;
  }

  public static String decode(String s, Boolean b) {
    String str = "";
    if (s == null)
      return str;
    try
    {
      str = URLDecoder.decode(s, "UTF-8");
      if (b.booleanValue())
        str = "%" + str + "%";
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
    return str;
  }
  public static String returnBuyApply(String productModelName, String materialName) {
    if ((materialName == null) || ("".equals(materialName))) {
      return productModelName;
    }
    return materialName;
  }

  public static Map getParameterMap(HttpServletRequest request)
  {
    Map properties = request.getParameterMap();

    Map returnMap = new HashMap();
    Iterator entries = properties.entrySet().iterator();

    String name = "";
    String value = "";
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry)entries.next();
      name = (String)entry.getKey();
      Object valueObj = entry.getValue();
      if (valueObj == null) {
        value = "";
      } else if ((valueObj instanceof String[])) {
        String[] values = (String[])valueObj;
        for (int i = 0; i < values.length; i++) {
          value = values[i] + ",";
        }
        value = value.substring(0, value.length() - 1);
      } else {
        value = valueObj.toString();
      }
      returnMap.put(name, value);
    }
    return returnMap;
  }

  public static double doubleSub(double subtractor, double subtracted)
  {
    BigDecimal b1 = new BigDecimal(Double.toString(subtractor));
    BigDecimal b2 = new BigDecimal(Double.toString(subtracted));
    return b1.subtract(b2).doubleValue();
  }
}