package com.siberhus.springbatch.item.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;

import com.siberhus.springbatch.item.file.UnknownFileTypeException;

public class DefaultExcelWorkbookFactory implements ExcelWorkbookFactory {
	
	@Override
	public Workbook create(Resource resource) throws IOException {
		String ext = getFileExtension(resource);
		Workbook wb = null;
		if("XLS".equals(ext)){
			wb = new HSSFWorkbook();
		}else if("XLSX".equals(ext)){
			wb = new XSSFWorkbook();
		}else{
			throw new UnknownFileTypeException("Support only *.xls and *.xlsx");
		}
		return wb;
	}
	
	@Override
	public Workbook createAndRead(Resource resource) throws IOException {
		String ext = getFileExtension(resource);
		InputStream in = resource.getInputStream();
		Workbook wb = null;
		if("XLS".equals(ext)){
			wb = new HSSFWorkbook(in);
		}else if("XLSX".equals(ext)){
			wb = new XSSFWorkbook(in);
		}else{
			throw new UnknownFileTypeException("Support only *.xls and *.xlsx");
		}
		IOUtils.closeQuietly(in);
		return wb;
	}
	
	private String getFileExtension(Resource resource){
		String ext = FilenameUtils.getExtension(resource.getFilename());
		ext = ext.toUpperCase();
		return ext;
	}
	
	public static void main(String[] args) throws Exception{
		
		InputStream in = new FileInputStream("D:\\My Jobs\\SiberHus\\Workspaces\\Eclipse\\Grails\\applications\\leadmgr\\data\\test.xlsx");
		Workbook wb = new XSSFWorkbook(in);
		in.close();
		Sheet s = wb.getSheetAt(0);
		
//		System.out.println(s.getRow(0).getCell(0).getStringCellValue());
		
		Row r = s.getRow(0);
		System.out.println(r.getFirstCellNum());
		System.out.println(r.getLastCellNum());
		for(int i=r.getFirstCellNum();i<r.getLastCellNum();i++){
			System.out.println(r.getCell(i));
		}
	}
	

}
