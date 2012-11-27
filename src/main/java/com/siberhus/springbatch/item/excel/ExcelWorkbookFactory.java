package com.siberhus.springbatch.item.excel;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;

public interface ExcelWorkbookFactory {
	
	Workbook create(Resource resource) throws IOException;
	
	Workbook createAndRead(Resource resource) throws IOException;
	
}
