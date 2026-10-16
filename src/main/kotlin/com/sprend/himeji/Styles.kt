package com.sprend.himeji

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.streaming.SXSSFWorkbook

internal class Styles(workbook: SXSSFWorkbook)
{
	val centered: CellStyle = workbook.createCellStyle().apply {
		alignment = HorizontalAlignment.CENTER
		verticalAlignment = VerticalAlignment.CENTER
	}

	val text: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		wrapText = true
	}

	val leftTextAligned: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(text)
		alignment = HorizontalAlignment.LEFT
	}

	val number: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
	}

	val rightNumberAligned: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(number)
		alignment = HorizontalAlignment.RIGHT
	}

	val header: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		setFont(workbook.createFont().apply { bold = true })
	}

	val date: CellStyle = workbook.createCellStyle().apply {
		cloneStyleFrom(centered)
		dataFormat = workbook.createDataFormat().getFormat(DateFormat)
	}

	private companion object
	{
		const val DateFormat = "yyyy-mm-dd hh:mm:ss"
	}
}
