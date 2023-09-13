/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.base.utils;

import io.innospots.base.exception.ResourceException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ExcelUtil
 *
 * @author Wren
 * @date 2022/9/15-7:53
 */
public class ExcelUtil {

    public static final String EXTENSION_2003 = "xls";
    public static final String EXTENSION_2007 = "xlsx";

    /**
     * 导出excel数据
     *
     * @param title
     * @param path
     * @param titleArray
     * @param cloumArray
     * @param list
     * @throws IOException
     */
    public static void export(String title, String path, String[] titleArray, String[] cloumArray, List<Map<String, Object>> list) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(title);
        HSSFCellStyle styleBg = getFont(wb, 0);
        HSSFCellStyle styleBg1 = getFont(wb, 1);
        HSSFRow row = sheet.createRow(0);
        row.setHeightInPoints(20);

        for (int i = 0; i < titleArray.length; i++) {
            HSSFCell celltitle = row.createCell(i);// 列
            celltitle.setCellValue(titleArray[i]);// 设置单元格的值
            celltitle.setCellStyle(styleBg1);
            sheet.setColumnWidth(i, (titleArray[i].length() + 20) * 256);
        }
        int rowNum = 1;
        for (Map<String, Object> map : list) {
            row = sheet.createRow(rowNum);// 创建行
            row.setHeightInPoints(30);
            for (int j = 0; j < cloumArray.length; j++) {
                HSSFCell celltitle = row.createCell(j); // 创建列
                Object obj = map.get(cloumArray[j]);
                celltitle.setCellValue(obj == null ? "" : obj + "");
                celltitle.setCellStyle(styleBg);
            }
            rowNum++;
        }
        File file = new File(path);
        if (!file.getParentFile().exists()) {// 先创建目录和文件
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileOutputStream fout = new FileOutputStream(path);
        wb.write(fout);
        fout.close();
    }

    /**
     * Excel样式
     *
     * @param wb
     * @param mark
     * @return
     */
    public static HSSFCellStyle getFont(HSSFWorkbook wb, int mark) {
        HSSFCellStyle styleBg = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        if (mark == 1) {
            font.setBold(true);
//            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//            font.setColor(HSSFColor.WHITE.index);
            font.setColor(Font.COLOR_NORMAL);
            styleBg.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            styleBg.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        styleBg.setFont(font);
        styleBg.setVerticalAlignment(VerticalAlignment.CENTER); // 创建一个居中格式
        styleBg.setAlignment(HorizontalAlignment.CENTER);// 水平
        styleBg.setWrapText(true);
        return styleBg;
    }

    public static String getFileExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            return fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }


    /**
     * 导入excel数据
     *
     * @param file
     * @param titleLength
     * @return
     * @throws IOException
     */
    public static List<List<Object>> importExcel(File file, int titleLength) throws IOException {
        String fileName = file.getName();
        String extension = getFileExtension(fileName);
        if (EXTENSION_2003.equalsIgnoreCase(extension)) {
            return read2003Excel(file, titleLength);
        } else if (EXTENSION_2007.equalsIgnoreCase(extension)) {
            return read2007Excel(file, titleLength);
        } else {
            throw new IOException("不支持的文件类型");
        }
    }

    /**
     * 导入excel数据
     *
     * @param fileName
     * @param inputStream
     * @param titleLength
     * @return
     * @throws IOException
     */
    public static List<List<Object>> importExcel(String fileName, InputStream inputStream, int titleLength) throws IOException {
        String extension = getFileExtension(fileName);
        if (EXTENSION_2003.equalsIgnoreCase(extension)) {
            return read2003Excel(inputStream, titleLength);
        } else if (EXTENSION_2007.equalsIgnoreCase(extension)) {
            return read2007Excel(inputStream, titleLength);
        } else {
            throw ResourceException.buildFileTypeException(ExcelUtil.class, "不支持的文件类型:" + extension);
        }
    }

    private static List<List<Object>> read2003Excel(File file, int titleLength) throws IOException {
        return read2003Excel(new FileInputStream(file), titleLength);
    }

    /**
     * 读取 office 2003 excel
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static List<List<Object>> read2003Excel(InputStream inputStream, int titleLength) throws IOException {
        List<List<Object>> list = new LinkedList<List<Object>>();
        HSSFWorkbook hwb = new HSSFWorkbook(inputStream);
        HSSFSheet sheet = hwb.getSheetAt(0);
        Object value = null;
        HSSFRow row = null;
        HSSFCell cell = null;
        int length = sheet.getRow(titleLength).getLastCellNum(); // 获取标题的长度
        for (int i = sheet.getFirstRowNum(); i <= sheet.getPhysicalNumberOfRows(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            List<Object> linked = new LinkedList<Object>();
            for (int j = 0; j < length; j++) {
                cell = row.getCell(j);
                if (cell == null) {
                    linked.add("");
                    continue;
                }
                DecimalFormat df = new DecimalFormat("0");// 格式化 number String 字符
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
                DecimalFormat nf = new DecimalFormat("0");// 格式化数字
                switch (cell.getCellType()) {
                    case STRING:
                        // System.out.println(i+"行"+j+" 列 is String type");
                        value = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        // System.out.println(i+"行"+j+" 列 is Number type ; DateFormt:"+cell.getCellStyle().getDataFormatString());
                        if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                            value = df.format(cell.getNumericCellValue());
                        } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                            value = nf.format(cell.getNumericCellValue());
                            //value = cell.getNumericCellValue()+"";
                        } else {
                            value = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                        }
                        break;
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    case BLANK:
                        value = "";
                        break;
                    default:
                        value = cell.toString();
                }
                // 注释这里之后空值也可以添加
				/*if (value == null || "".equals(value)) {
					continue;
				} */
                linked.add(value);
            }
            list.add(linked);
        }
        return list;
    }

    /**
     * 读取Office 2007 excel
     */
    private static List<List<Object>> read2007Excel(File file, int titleLength) throws IOException {
        return read2007Excel(new FileInputStream(file), titleLength);

    }

    private static List<List<Object>> read2007Excel(InputStream inputStream, int titleLength) throws IOException {
        List<List<Object>> list = new LinkedList<List<Object>>();
        // 构造 XSSFWorkbook 对象，strPath 传入文件路径
        XSSFWorkbook xwb = new XSSFWorkbook(inputStream);
        // 读取第一章表格内容
        XSSFSheet sheet = xwb.getSheetAt(0);
        Object value = null;
        XSSFRow row = null;
        XSSFCell cell = null;
        int length = sheet.getRow(titleLength).getLastCellNum();// 获取标题的长度
        for (int i = sheet.getFirstRowNum(); i <= sheet.getPhysicalNumberOfRows(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            List<Object> linked = new LinkedList<Object>();
            for (int j = 0; j < length; j++) {
                cell = row.getCell(j);
                if (cell == null) {
                    linked.add("");
                    continue;
                }
                DecimalFormat df = new DecimalFormat("0");// 格式化 number String 字符
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
                DecimalFormat nf = new DecimalFormat("0");// 格式化数字
                switch (cell.getCellType()) {
                    case STRING:
                        value = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                            value = df.format(cell.getNumericCellValue());
                        } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                            value = nf.format(cell.getNumericCellValue());
                            //value = cell.getNumericCellValue()+"";
                        } else {
                            value = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                        }
                        break;
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    case BLANK:
                        value = "";
                        break;
                    default:
                        value = cell.toString();
                }
                // 注释这里之后空值也可以添加
				/*if (value == null || "".equals(value)) {
					continue;
				} */
                linked.add(value);
            }
            list.add(linked);
        }
        return list;
    }
}
