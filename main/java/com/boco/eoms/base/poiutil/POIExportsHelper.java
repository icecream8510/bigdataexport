package com.boco.eoms.base.poiutil;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class POIExportsHelper {
    
    private Long cellCount = null;
    
    public POIExportsHelper(int i) {
        cellCount = new Long(i);
    }
    
    public POIExportsHelper() {
        
    }
    
    // 是否需要新加附件与mathNumber配合使用
    private Boolean flag = Boolean.FALSE;
    
    // 请求编号用于限制重复下载
    private String mathNumber = "";
    
    // 开始时间
    private long startMillis;
    
    // SQL执行时间
    private String sqlTime = "";
    
    // 数据读取时间
    private String dataTime = "";
    
    // 数据写入时间
    private String writeTime = "";
    
    // 总耗时时间
    private String countTime = "";
    
    // 完成时间
    private String finishTime = "";
    
    // 数据总量
    private int totalCount = 0;
    
    // 文件数总量
    private int filesCount = 0;
    
    /**
     * jdbc获取连接
     * 
     * @param xlsFileName
     *            文件名称
     * @param colName
     *            查询字段名,列名
     * @param strSQL
     *            查询SQL
     * @throws Exception
     */
    public void exportJDBCSXSSFWorkbook(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String xlsFileName,
                                        String[] colName,
                                        String strSQL) throws Exception {
        
        mathNumber = request.getParameter("mathNumber");
        
        if (StringUtils.isNotEmpty(mathNumber) && getAttachment(request,
                                                                response,
                                                                xlsFileName)) {
            return;
        }
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis(); // 开始时间
        startMillis = startTime;
        System.out.println("   startTime   " + startTime);
        try {
            // 使用jdbc链接数据库
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                        ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(100);
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
            
            // 查询SQL
            rs = stmt.executeQuery(strSQL);
            // 完成查询时间
            long finishedTime = System.currentTimeMillis();
            System.out.println("   finishedTime   " + finishedTime);
            sqlTime = new BigDecimal(finishedTime - startTime).divide(new BigDecimal(1000),
                                                                      2,
                                                                      BigDecimal.ROUND_HALF_UP) + "秒";
            
            if (rs != null && rs.getMetaData() != null) {
                createFile(request, response, xlsFileName, colName, rs);
            }
        }
        finally {
            // 第六步：关闭资源
            try {
                this.close(rs, stmt, conn);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    //对数字不进行文本格式化
    public void exportJDBCSXSSFWorkbookSpecial(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String xlsFileName,
                                        String[] colName,
                                        List numColList,
                                        String strSQL) throws Exception {
        
        mathNumber = request.getParameter("mathNumber");
        
        if (StringUtils.isNotEmpty(mathNumber) && getAttachment(request,
                                                                response,
                                                                xlsFileName)) {
            return;
        }
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis(); // 开始时间
        startMillis = startTime;
        System.out.println("   startTime   " + startTime);
        try {
            // 使用jdbc链接数据库
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                        ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(100);
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
            
            // 查询SQL
            rs = stmt.executeQuery(strSQL);
            // 完成查询时间
            long finishedTime = System.currentTimeMillis();
            System.out.println("   finishedTime   " + finishedTime);
            sqlTime = new BigDecimal(finishedTime - startTime).divide(new BigDecimal(1000),
                                                                      2,
                                                                      BigDecimal.ROUND_HALF_UP) + "秒";
            
            if (rs != null && rs.getMetaData() != null) {
                createFile(request, response, xlsFileName, colName, numColList,rs);
            }
        }
        finally {
            // 第六步：关闭资源
            try {
                this.close(rs, stmt, conn);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private void createFile(HttpServletRequest request,
                            HttpServletResponse response,
                            String xlsFileName,
                            String[] colName,
                            ResultSet rs) throws SQLException,
                                         FileNotFoundException,
                                         IOException,
                                         Exception {
        long startTime = System.currentTimeMillis(); // 开始时间
        String filePath = ""; // 输出文件
        List filePathList = new ArrayList();
        BigDecimal totalMoney = BigDecimal.ZERO;
        
        ResultSetMetaData rsmd = rs.getMetaData();
        // 内存中只创建100个对象，写临时文件，当超过100条，就将内存中不用的对象释放。
        SXSSFWorkbook wb = new SXSSFWorkbook(100); // 关键语句
        Sheet sheet = null; // 工作表对象
        Row nRow = null; // 行对象
        Cell nCell = null; // 列对象
        
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        int fileCount = 1; // 文件个数
        int filePage = 1; // 多少行拆分文件
        int fileNo = 0; // 多少行拆分文件
        int sheetNo = 0; // 多少行拆分Sheet
        int pageRowNo = 1; // 页行号
        
        Boolean isempty = Boolean.TRUE;
        while (rs.next()) {
            totalCount++;
            isempty = Boolean.FALSE;
            // Thread.sleep(1); //休息一下，防止对CPU占用，其实影响不大
            if (fileNo != 0 && fileNo % getFileNo() == 0) {
                filePath = getBaseurl() + xlsFileName
                           + "_"
                           + (fileCount++)
                           + ".xlsx"; // 输出文件
                createFile(filePath);
                FileOutputStream fOut = new FileOutputStream(filePath);
                filePathList.add(filePath);
                wb.write(fOut);
                fOut.flush(); // 刷新缓冲区
                fOut.close();
                sheetNo = 0;
                wb = new SXSSFWorkbook(100); // 关键语句
                filePage++;
                
            }
            // 打印300000条后切换到下个工作表，可根据需要自行拓展，2百万，3百万...数据一样操作，只要不超过1048576就可以
            if (sheetNo % getSheetNo() == 0) {
                System.out.println("Current Sheet:" + filePage
                                   + "-"
                                   + sheetNo
                                   / getSheetNo());
                sheet = wb.createSheet(xlsFileName + "("
                                       + (sheetNo / getSheetNo() + 1)
                                       + ")工作簿");// 建立新的sheet对象
                sheet = wb.getSheetAt(sheetNo / getSheetNo()); // 动态指定当前的工作表
                pageRowNo = 1; // 每当新建了工作表就将当前工作表的行号重置为0
                
                nRow = sheet.createRow(0); // 新建行对象
                for (int c = 0; c < colName.length; c++) {
                    if (colName[c] != null && !"null".equals(colName[c])
                        && !"".equals(colName[c])) {
                        if (c == 0) {
                            nCell = nRow.createCell(c);
                            // 增加序号列
                            nCell.setCellValue("序号");
                            nCell.setCellStyle(style);
                        }
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue(colName[c].split("-")[1]);
                        nCell.setCellStyle(style);
                    }
                }
            }
            fileNo++;
            sheetNo++;
            nRow = sheet.createRow(pageRowNo); // 新建行对象
            // 打印每行，每行有6列数据 rsmd.getColumnCount()==6 --- 列属性的个数
            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                if (j == 1) {
                    nCell = nRow.createCell(0);
                    nCell.setCellValue(fileNo);
                }
                nCell = nRow.createCell(j);
                nCell.setCellValue(rs.getString(j));
                
                try {
                    if (cellCount != null && j == cellCount
                        && rs.getString(j) != null) {
                        totalMoney = totalMoney.add(new BigDecimal(rs.getString(j)));
                    }
                }
                catch (Exception e) {
                    
                }
            }
            pageRowNo++;
            if (fileNo % 10000 == 0) {
                System.out.println("row no: " + fileNo);
            }
        }
        System.out.println(" totalCount  " + totalCount);
        // 判断为空时创建文件
        if (isempty) {
            sheet = wb.createSheet(xlsFileName + "(" + (1) + ")工作簿");// 建立新的sheet对象
            nRow = sheet.createRow(0); // 新建行对象
            for (int c = 0; c < colName.length; c++) {
                if (colName[c] != null && !"null".equals(colName[c])
                    && !"".equals(colName[c])) {
                    if (c == 0) {
                        nCell = nRow.createCell(c);
                        // 增加序号列
                        nCell.setCellValue("序号");
                        nCell.setCellStyle(style);
                    }
                    nCell = nRow.createCell(c + 1);
                    nCell.setCellValue(colName[c].split("-")[1]);
                    nCell.setCellStyle(style);
                }
            }
        }
        
        if (cellCount != null) {
            nRow = sheet.createRow(pageRowNo); // 新建行对象
            for (int c = 0; c < colName.length; c++) {
                if (colName[c] != null && !"null".equals(colName[c])
                    && !"".equals(colName[c])) {
                    // 合计字段
                    if (c == (cellCount - 2)) {
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue("合计");
                        nCell.setCellStyle(style);
                    }
                    else if (c == (cellCount - 1)) {
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue(totalMoney.toString());
                        nCell.setCellStyle(style);
                    }
                    else {
                        if (nRow != null) {
                            nCell = nRow.createCell(c + 1);
                            nCell.setCellValue("");
                        }
                        
                    }
                }
            }
        }
        // 完成查询时间
        System.out.println("SQL执行查询时间 : " + sqlTime);
        
        long finishedTime = System.currentTimeMillis(); // 处理完成时间
        dataTime = new BigDecimal(finishedTime - startTime).divide(new BigDecimal(1000),
                                                                   2,
                                                                   BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("完成文件读取时间 : " + dataTime);
        
        startTime = System.currentTimeMillis(); // 开始时间
        filesCount = fileCount;
        filePath = getBaseurl() + xlsFileName + "_" + (fileCount++) + ".xlsx"; // 输出文件
        filePathList.add(filePath);
        createFile(filePath);
        FileOutputStream fOut = new FileOutputStream(filePath);
        wb.write(fOut);
        fOut.flush(); // 刷新缓冲区
        fOut.close();
        
        long stopTime = System.currentTimeMillis(); // 写文件时间
        writeTime = new BigDecimal(stopTime - startTime).divide(new BigDecimal(1000),
                                                                2,
                                                                BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("完成文件写入时间 :  " + writeTime);
        
        this.downAttachment(request,
                            response,
                            xlsFileName,
                            filePathList,
                            startTime);
    }
    
    private void createFile(HttpServletRequest request,
                            HttpServletResponse response,
                            String xlsFileName,
                            String[] colName,
                            List numColList,
                            ResultSet rs) throws SQLException,
                            FileNotFoundException,
                            IOException,
                            Exception {
        long startTime = System.currentTimeMillis(); // 开始时间
        String filePath = ""; // 输出文件
        List filePathList = new ArrayList();
        BigDecimal totalMoney = BigDecimal.ZERO;
        
        ResultSetMetaData rsmd = rs.getMetaData();
        // 内存中只创建100个对象，写临时文件，当超过100条，就将内存中不用的对象释放。
        SXSSFWorkbook wb = new SXSSFWorkbook(100); // 关键语句
        Sheet sheet = null; // 工作表对象
        Row nRow = null; // 行对象
        Cell nCell = null; // 列对象
        
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        int fileCount = 1; // 文件个数
        int filePage = 1; // 多少行拆分文件
        int fileNo = 0; // 多少行拆分文件
        int sheetNo = 0; // 多少行拆分Sheet
        int pageRowNo = 1; // 页行号
        
        Boolean isempty = Boolean.TRUE;
        while (rs.next()) {
            totalCount++;
            isempty = Boolean.FALSE;
            // Thread.sleep(1); //休息一下，防止对CPU占用，其实影响不大
            if (fileNo != 0 && fileNo % getFileNo() == 0) {
                filePath = getBaseurl() + xlsFileName
                        + "_"
                        + (fileCount++)
                        + ".xlsx"; // 输出文件
                createFile(filePath);
                FileOutputStream fOut = new FileOutputStream(filePath);
                filePathList.add(filePath);
                wb.write(fOut);
                fOut.flush(); // 刷新缓冲区
                fOut.close();
                sheetNo = 0;
                wb = new SXSSFWorkbook(100); // 关键语句
                filePage++;
                
            }
            // 打印300000条后切换到下个工作表，可根据需要自行拓展，2百万，3百万...数据一样操作，只要不超过1048576就可以
            if (sheetNo % getSheetNo() == 0) {
                System.out.println("Current Sheet:" + filePage
                                   + "-"
                                   + sheetNo
                                   / getSheetNo());
                sheet = wb.createSheet(xlsFileName + "("
                        + (sheetNo / getSheetNo() + 1)
                        + ")工作簿");// 建立新的sheet对象
                sheet = wb.getSheetAt(sheetNo / getSheetNo()); // 动态指定当前的工作表
                pageRowNo = 1; // 每当新建了工作表就将当前工作表的行号重置为0
                
                nRow = sheet.createRow(0); // 新建行对象
                for (int c = 0; c < colName.length; c++) {
                    if (colName[c] != null && !"null".equals(colName[c])
                            && !"".equals(colName[c])) {
                        if (c == 0) {
                            nCell = nRow.createCell(c);
                            // 增加序号列
                            nCell.setCellValue("序号");
                            nCell.setCellStyle(style);
                        }
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue(colName[c].split("-")[1]);
                        nCell.setCellStyle(style);
                    }
                }
            }
            fileNo++;
            sheetNo++;
            nRow = sheet.createRow(pageRowNo); // 新建行对象
            // 打印每行，每行有6列数据 rsmd.getColumnCount()==6 --- 列属性的个数
            for (int j = 1; j < rsmd.getColumnCount(); j++) {
                if (j == 1) {
                    nCell = nRow.createCell(0);
                    nCell.setCellValue(fileNo);  
                }
                nCell = nRow.createCell(j);
                 
                if(numColList.contains(colName[j-1])){
                    DataFormat df = wb.createDataFormat(); // 此处设置数据格式
                    style.setDataFormat(df.getFormat("#,##0.00"));
                    nCell.setCellValue(rs.getDouble(j));
                }else{
                    nCell.setCellValue(rs.getString(j));
                }
               
                
                try {
                    if (cellCount != null && j == cellCount
                            && rs.getString(j) != null) {
                        totalMoney = totalMoney.add(new BigDecimal(rs.getString(j)));
                    }
                }
                catch (Exception e) {
                    
                }
            }
            pageRowNo++;
            if (fileNo % 10000 == 0) {
                System.out.println("row no: " + fileNo);
            }
        }
        System.out.println(" totalCount:  " + totalCount);
        // 判断为空时创建文件
        if (isempty) {
            sheet = wb.createSheet(xlsFileName + "(" + (1) + ")工作簿");// 建立新的sheet对象
            nRow = sheet.createRow(0); // 新建行对象
            for (int c = 0; c < colName.length; c++) {
                if (colName[c] != null && !"null".equals(colName[c])
                        && !"".equals(colName[c])) {
                    if (c == 0) {
                        nCell = nRow.createCell(c);
                        // 增加序号列
                        nCell.setCellValue("序号");
                        nCell.setCellStyle(style);
                    }
                    nCell = nRow.createCell(c + 1);
                    nCell.setCellValue(colName[c].split("-")[1]);
                    nCell.setCellStyle(style);
                }
            }
        }
        
        if (cellCount != null) {
            nRow = sheet.createRow(pageRowNo); // 新建行对象
            for (int c = 0; c < colName.length; c++) {
                if (colName[c] != null && !"null".equals(colName[c])
                        && !"".equals(colName[c])) {
                    // 合计字段
                    if (c == (cellCount - 2)) {
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue("合计");
                        nCell.setCellStyle(style);
                    }
                    else if (c == (cellCount - 1)) {
                        nCell = nRow.createCell(c + 1);
                        nCell.setCellValue(totalMoney.toString());
                        nCell.setCellStyle(style);
                    }
                    else {
                        if (nRow != null) {
                            nCell = nRow.createCell(c + 1);
                            nCell.setCellValue("");
                        }
                        
                    }
                }
            }
        }
        // 完成查询时间
        System.out.println("SQL执行查询时间 : " + sqlTime);
        
        long finishedTime = System.currentTimeMillis(); // 处理完成时间
        dataTime = new BigDecimal(finishedTime - startTime).divide(new BigDecimal(1000),
                                                                   2,
                                                                   BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("完成文件读取时间 : " + dataTime);
        
        startTime = System.currentTimeMillis(); // 开始时间
        filesCount = fileCount;
        filePath = getBaseurl() + xlsFileName + "_"+mathNumber+"_" + (fileCount++) + ".xlsx"; // 输出文件
        filePathList.add(filePath);
        createFile(filePath);
        FileOutputStream fOut = new FileOutputStream(filePath);
        wb.write(fOut);
        fOut.flush(); // 刷新缓冲区
        fOut.close();
        
        long stopTime = System.currentTimeMillis(); // 写文件时间
        writeTime = new BigDecimal(stopTime - startTime).divide(new BigDecimal(1000),
                                                                2,
                                                                BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("完成文件写入时间 :  " + writeTime);
        
        this.downAttachment(request,
                            response,
                            xlsFileName,
                            filePathList,
                            startTime);
    }
    
    /**
     * @param request
     * @param response
     * @throws Exception
     */
    public Boolean getAttachment(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String xlsFileName) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis(); // 开始时间
        String filePath = getBaseurl() + startTime + ".xlsx"; // 输出文件
        try {
            // 使用jdbc链接数据库
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                        ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(100);
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
            
            String mathNumber = request.getParameter("mathNumber");
            
            List filePathList = new ArrayList();
            // 查询SQL
            if (StringUtils.isNotEmpty(mathNumber)) {
                String strSQL = " select distinct ATTACH_PATH f1,ATTACH_PATH f2,upload_date f3 from taw_system_down_log where CONTENT='" + mathNumber+"'"
                                + " order by upload_date asc ";
                rs = stmt.executeQuery(strSQL);
                ResultSetMetaData rsmd = rs.getMetaData();
                while (rs.next()) {
                    if (rs.getString(1) != null) {
                        filePathList.add(rs.getString(1));
                        flag = Boolean.TRUE;
                    }
                }
            }
            if (filePathList.size() > 0) {
                this.downAttachment(request,
                                    response,
                                    xlsFileName,
                                    filePathList,
                                    startTime);
            }
        }
        finally {
            // 第六步：关闭资源
            try {
                this.close(rs, stmt, conn);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
    
    /**
     * 下载文件
     */
    public void downAttachment(HttpServletRequest request,
                               HttpServletResponse response,
                               String xlsFileName,
                               List filePathList,
                               long stopTime) throws Exception {
        String path = "";
        if (filePathList.size() > 1) {
            long startTime = System.currentTimeMillis(); // 开始时间
            path = getBaseurl() + startTime + ".zip"; // 输出文件
            // 完成本地rar的文件压缩
            FileToRarUtils ftr = new FileToRarUtils(path);
            String localPath = ftr.compress(filePathList, path);
            // 完成上传SFTP服务器工作
        }
        else if (filePathList.size() == 1) {
            path = (String) filePathList.get(0);
        }
        if (File.separatorChar == '/') {
            path = StringUtils.replace(path, "\\", "/");
        }
        else {
            path = StringUtils.replace(path, "/", "\\");
        }
        File file = new File(path);
        System.out.println("path=" + path);
        
        if (filePathList.size() == 1) {
            response.reset();
            response.setContentType("application/x-msdownload;charset=GBK");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                               "attachment;filename=" + URLEncoder.encode(xlsFileName + ".xlsx",
                                                                          "UTF-8")
                                       + ";charset=UTF-8");
            // log.error(realPath);
        }
        else {
            response.reset();
            response.setContentType("application/x-msdownload;charset=GBK");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                               "attachment;filename=" + URLEncoder.encode(xlsFileName + ".zip",
                                                                          "UTF-8")
                                       + ";charset=UTF-8");
            // log.error(realPath);
        }
        
        long finishedTime = System.currentTimeMillis(); // 写文件时间
        countTime = new BigDecimal(finishedTime - startMillis).divide(new BigDecimal(1000),
                                                                      2,
                                                                      BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("总耗时时间 :  " + countTime);
        
        String msg = "本次导出数据共计:" + totalCount
                     + "行,拆分"
                     + filesCount
                     + "个文件,其中查询耗时:"
                     + sqlTime
                     + ",数据组装耗时:"
                     + dataTime
                     + ",文件写入耗时:"
                     + writeTime
                     + ",总耗时:"
                     + countTime
                     + ";\r\n";
        if (getByteToM(file.length()) > getMZise() || getIsSave()) {
            updateAttachment(request,
            		         xlsFileName,
                             filePathList,
                             getByteToM(file.length()),
                             path);
            if (getByteToM(file.length()) > getMZise()) {
                msg += "您的导出文件已经生成,文件大小共计(" + getByteToM(file.length())
                       + "M)超出缓存最大配置("
                       + getMZise()
                       + "M),对于超大数据导出在功能菜单《大数据导出》也有导出记录!\r\n";
                /*
                 * throw new BusinessException("您的数据导出已经生成,文件大小共计(" +
                 * byteToM(file.length()) + "M)超出内存最大限制(" + getMZise() +
                 * "M),请通过页面《导出下载》或菜单《大数据导出》功能下载!");
                 */
            }
        }
        request.getSession().setAttribute("poiReportMsg", msg);
        request.getSession().removeAttribute("exportFlag");
        
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int len = 0;
        OutputStream out = response.getOutputStream();
        while ((len = bufferedInputStream.read(buf)) > 0)
            out.write(buf, 0, len);
        out.close();
        // response.wait();
        finishedTime = System.currentTimeMillis(); // 写文件时间
        finishTime = new BigDecimal(finishedTime - stopTime).divide(new BigDecimal(1000),
                                                                    2,
                                                                    BigDecimal.ROUND_HALF_UP) + "秒";
        System.out.println("完成文件下载时间 :  " + finishTime);
    }
    
    /**
     * jdbc获取连接
     * 
     * @param xlsFileName
     *            文件名称
     * @param filePathList
     * @param fileSizeM
     * @param colName
     *            查询字段名,列名
     * @param strSQL
     *            查询SQL
     */
    private void updateAttachment(HttpServletRequest request,
    		                      String xlsFileName,
                                  List filePathList,
                                  double fileSizeM,
                                  String path) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 使用jdbc链接数据库
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                                        ResultSet.CONCUR_UPDATABLE);
            String fileName = "";
            if (filePathList.size() == 1) {
                fileName = xlsFileName + ".xlsx";
            }
            else {
                fileName = xlsFileName + ".zip";
            }
//        	TawSystemSessionForm sessionform = (TawSystemSessionForm) request.getSession().getAttribute("sessionform");
    		String userId = request.getParameter("userid");
    		String userName = request.getParameter("username");
            if (!flag) {
            	String id = String.valueOf(System.currentTimeMillis());
                String strSQL = "";
                strSQL += " Insert into taw_system_down_log(ID, ATTACH_TOPIC, UPLOADER_ID, ATTACHMENT_IMPORTANCE_ID,AUTHOR, ATTACH_NAME, ATTACH_PATH, UPLOAD_DATE,ATTCHMENT_SIZE,CONTENT ) ";
                strSQL += " Values ";
                strSQL += " ('"+id+"', '" + xlsFileName
                          + "','"
                          + userId
                          + "', 1,'"
                          + userName
                          + "', '"
                          + fileName
                          + "', '"
                          + path
                          + "', sysdate,"
                          + fileSizeM
                          + ", '"
                          + mathNumber
                          + "' ) ";
                System.out.println(strSQL);
                // 查询SQL
                stmt.execute(strSQL);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // 第六步：关闭资源
            try {
                this.close(rs, stmt, conn);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    // 执行关闭流的操作
    private void close(ResultSet rs, Statement stmt, Connection conn) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
    
    private void createFile(String filePath) {
        if (File.separatorChar == '/') {
            filePath = StringUtils.replace(filePath, "\\", "/");
        }
        else {
            filePath = StringUtils.replace(filePath, "/", "\\");
        }
        File rarFile = new File(filePath);
        System.out.println("文件全路径："+rarFile.getAbsolutePath());
        if (!rarFile.exists()) {
            File parent = rarFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                rarFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Connection getConnection() throws InstantiationException,
                                      IllegalAccessException,
                                      ClassNotFoundException,
                                      SQLException {
        Connection conn;
        // 使用jdbc链接数据库
        Class.forName(JDBCPropertiesHelper.getProperty("hibernate.connection.driver_class"))
             .newInstance();
        String url = JDBCPropertiesHelper.getProperty("hibernate.connection.url");
        String user = JDBCPropertiesHelper.getProperty("hibernate.connection.username");
        String password = JDBCPropertiesHelper.getProperty("hibernate.connection.password");
        // 获取数据库连接
        conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
    
    /**
     * @param byteZise
     * @return
     */
    private double getByteToM(long byteZise) {
        BigDecimal mZise = new BigDecimal(0);
        if (byteZise > 0) {
            BigDecimal kbZise = new BigDecimal(byteZise).divide(new BigDecimal(1000),
                                                                2,
                                                                BigDecimal.ROUND_HALF_UP);
            System.out.println("file.length()  " + kbZise + "KB");
            if (kbZise.intValue() > 1000) {
                mZise = kbZise.divide(new BigDecimal(1000),
                                      2,
                                      BigDecimal.ROUND_HALF_UP);
                System.out.println("file.length()  " + mZise + "M");
            }
        }
        return mZise.doubleValue();
    }
    
    /**
     * #临时文件存放路径(请定期删除建议周期1周-1月预计硬盘耗损1T)
     * 
     * @return
     */
    private String getBaseurl() {
        String baseurl = "/attachment/report_poi/";
        try {
            baseurl = POIPropertiesHelper.getProperty("report.baseurl");
        }
        catch (Exception e) {
            
        }
        return baseurl;
    }
    
    /**
     * #多少数据量拆分文件:默认200000
     * 
     * @return
     */
    private int getFileNo() {
        int fileNo = 200000;
        
        try {
            String fileNoStr = POIPropertiesHelper.getProperty("report.fileNo")
                                                  .trim();
            if (StringUtils.isNotEmpty(fileNoStr)) {
                fileNo = (new Integer(fileNoStr)).intValue();
            }
        }
        catch (Exception e) {
            
        }
        return fileNo;
    }
    
    /**
     * #多少行拆分Sheet:默认50000
     * 
     * @return
     */
    private int getSheetNo() {
        int sheetNo = 50000;
        try {
            String sheetNoStr = POIPropertiesHelper.getProperty("report.sheetNo")
                                                   .trim();
            if (StringUtils.isNotEmpty(sheetNoStr)) {
                sheetNo = (new Integer(sheetNoStr)).intValue();
            }
        }
        catch (Exception e) {
            
        }
        return sheetNo;
    }
    
    /**
     * #多少M生成附件下载 :默认20
     * 
     * @return
     */
    private int getMZise() {
        int mZise = 20;
        try {
            String mZiseStr = POIPropertiesHelper.getProperty("report.mZise")
                                                 .trim();
            if (StringUtils.isNotEmpty(mZiseStr)) {
                mZise = (new Integer(mZiseStr)).intValue();
            }
        }
        catch (Exception e) {
            
        }
        return mZise;
    }
    
    /**
     * 是否存储导出附件
     * 
     * @return
     */
    private boolean getIsSave() {
        
        Boolean isSave = Boolean.FALSE;
        try {
            String isSaveStr = POIPropertiesHelper.getProperty("report.isSave")
                                                  .trim();
            if (StringUtils.isNotEmpty(isSaveStr)) {
                isSave = (new Boolean(isSaveStr));
            }
        }
        catch (Exception e) {
            
        }
        return isSave;
    }
}
