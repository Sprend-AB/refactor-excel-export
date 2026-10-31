package com.sprend.himeji.excel

import com.sprend.himeji.DateTimeUtc
import org.apache.poi.ss.usermodel.Cell
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

internal sealed interface WorkbookCellValue
{
	data object Empty : WorkbookCellValue
	data class Text(val value: String) : WorkbookCellValue
	data class LongNumber(val value: Long) : WorkbookCellValue
	data class DateTime(val value: DateTimeUtc) : WorkbookCellValue

	fun writeTo(cell: Cell)
	{
		when (this)
		{
			Empty -> Unit
			is Text -> cell.setCellValue(value)
			is LongNumber -> cell.setCellValue(value.toDouble())
			is DateTime -> cell.setCellValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(value.epochMillis), ZoneOffset.UTC))
		}
	}

	companion object
	{
		fun text(value: String?): WorkbookCellValue = value?.let(::Text) ?: Empty
		fun longNumber(value: Long?): WorkbookCellValue = value?.let(::LongNumber) ?: Empty
		fun dateTime(value: DateTimeUtc?): WorkbookCellValue = value?.let(::DateTime) ?: Empty
	}
}
