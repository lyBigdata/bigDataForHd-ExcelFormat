package cn.hadoop.liuyu.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/*
 * 先来实现一个Excel的解析类,解析Excel格式的数据
 */
public class ExcelParser {
	private static final Log LOG = LogFactory.getLog(ExcelParser.class);
	private StringBuilder currentString = null;
	private long bytesRead = 0;

	/*
	 * ExcelParser类自定义parseExcelData方法，解析Excel表格数据。
	 */
	public String parseExcelData(InputStream is) {
		try {
			HSSFWorkbook workbook = new HSSFWorkbook(is);

			// Taking first sheet from the workbook
			HSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			currentString = new StringBuilder();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();

				/*
				 * 通过StringBuilder类，将解析出的行内数据以'\t'拼接，每行以'\n'拼接，
				 * 然后返回拼接字符串
				 */
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						bytesRead++;
						currentString.append(cell.getBooleanCellValue() + "\t");
						break;

					case Cell.CELL_TYPE_NUMERIC:
						bytesRead++;
						currentString.append(cell.getNumericCellValue() + "\t");
						break;

					case Cell.CELL_TYPE_STRING:
						bytesRead++;
						currentString.append(cell.getStringCellValue() + "\t");
						break;
					}
				}
				currentString.append("\n");
			}
			is.close();
		} catch (IOException e) {
			LOG.error("IO Exception : File not found " + e);
		}
		return currentString.toString();
	}

	public long getBytesRead() {
		return bytesRead;
	}
}
