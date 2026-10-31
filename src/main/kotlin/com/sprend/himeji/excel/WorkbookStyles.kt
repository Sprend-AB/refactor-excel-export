package com.sprend.himeji.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.streaming.SXSSFWorkbook

internal class WorkbookStyles(workbook: SXSSFWorkbook)
{
	private val centered: CellStyle = workbook.createCellStyle().apply {
		alignment = HorizontalAlignment.CENTER
		verticalAlignment = VerticalAlignment.CENTER
	}

	val centeredWrappedText: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		wrapText = true
	}

	val leftAlignedWrappedText: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centeredWrappedText)
		alignment = HorizontalAlignment.LEFT
	}

	val rightAlignedNumber: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		alignment = HorizontalAlignment.RIGHT
		dataFormat = workbook.createDataFormat().getFormat("0")
	}

	val header: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		setFont(workbook.createFont().apply { bold = true })
	}

	val centeredDateTime: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		dataFormat = workbook.createDataFormat().getFormat(DateFormat)
	}

	private companion object
	{
		const val DateFormat = "yyyy-mm-dd hh:mm:ss"
	}
}
