package com.boco.framework.export.web;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.boco.eoms.base.poiutil.POIExportsHelper;

public class ExportExcelAction extends BaseAction
{
  
  /**
   * 导出中间表
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
    public ActionForward export(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
      throws Exception
    {

    	String sql = request.getParameter("sql");
    	String colNames = request.getParameter("colName");
    	String sheetName = request.getParameter("sheetName");
    	String[] colName = colNames.split(",");
		String[] numCol = new String[]{};
		List numColList = Arrays.asList(numCol);
		POIExportsHelper poiExportsUtils = new POIExportsHelper();
        poiExportsUtils.exportJDBCSXSSFWorkbookSpecial(request,
                                                response,
                                                sheetName,
                                                colName,
                                                numColList,
                                                sql);
        
        
		return null;
    }
}