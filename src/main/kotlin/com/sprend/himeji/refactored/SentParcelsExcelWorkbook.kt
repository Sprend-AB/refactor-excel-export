package com.sprend.himeji.refactored

import com.sprend.himeji.DateTimeUtc
import com.sprend.himeji.I18n
import com.sprend.himeji.ParcelExcelExporter
import com.sprend.himeji.ParcelRow
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.OutputStream
import kotlin.math.max

object SentParcelsExcelWorkbook : ParcelExcelExporter
{
	const val extension = "xlsx"
	const val contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

	private const val RowWindowSize = 100
	private const val ColumnWidthUnitsPerChar = 256
	internal const val MinColumnWidth = 10
	internal const val MaxColumnWidth = 80
	internal const val ColumnPadding = 3
	internal const val MaxRowHeightInPoints = 409.5f
	private const val DateTextLength = 19
	private const val SheetNameKey = "sent-files.export-sheet"

	override fun write(parcelHistory: List<ParcelRow>, output: OutputStream, i18n: I18n)
	{
		SXSSFWorkbook(RowWindowSize).use { workbook ->
			workbook.isCompressTempFiles = true

			val sheet = workbook.createSheet(i18n.get(SheetNameKey))
			sheet.createFreezePane(0, 1)

			val styles = WorkbookStyles(workbook)
			val excelColumns: List<SentParcelExcelColumn> = SentParcelExcelColumnFactory.createColumns(i18n, styles)

			val translatedHeaders = excelColumns.map { i18n.get(it.headerTranslationKey) }
			val widths = columnWidthsInChars(translatedHeaders, parcelHistory, excelColumns)

			writeHeaderRowAndSetColumnWidths(sheet, translatedHeaders, widths, styles)
			writeParcelRowsAndSetRowHeights(sheet, parcelHistory, widths, excelColumns)

			sheet.setAutoFilter(CellRangeAddress(0, parcelHistory.size, 0, translatedHeaders.lastIndex))
			workbook.write(output)
		}
	}

	private fun writeHeaderRowAndSetColumnWidths(sheet: Sheet, headers: List<String>, widths: IntArray, styles: WorkbookStyles)
	{
		val headerRow = sheet.createRow(0)
		headers.forEachIndexed { colIndex, headerTitle ->
			val cell = headerRow.createCell(colIndex)
			cell.setCellValue(headerTitle)
			cell.cellStyle = styles.header
			sheet.setColumnWidth(colIndex, widths[colIndex] * ColumnWidthUnitsPerChar)
		}
	}

	private fun writeParcelRowsAndSetRowHeights(sheet: Sheet, sentParcelList: List<ParcelRow>, widths: IntArray, columns: List<SentParcelExcelColumn>)
	{
		sentParcelList.forEachIndexed { rowIndex, parcel ->
			val sheetRow = sheet.createRow(rowIndex + 1)
			val values: List<WorkbookCellValue> = columns.map { it.createValue(parcel) }

			values.forEachIndexed { colIndex, value ->
				val cell = sheetRow.createCell(colIndex)
				cell.cellStyle = columns[colIndex].cellStyle
				value.writeTo(cell)
			}

			val tallestCell = values.withIndex().maxOf { (col, value) -> lineCount(value, widths[col] - ColumnPadding) }
			if (tallestCell > 1)
				sheetRow.heightInPoints = (tallestCell * sheet.defaultRowHeightInPoints).coerceAtMost(MaxRowHeightInPoints)
		}
	}

	private fun columnWidthsInChars(headers: List<String>, parcels: List<ParcelRow>, columns: List<SentParcelExcelColumn>): IntArray
	{
		val widths = IntArray(headers.size) { headers[it].length }
		for (parcel in parcels)
		{
			columns.forEachIndexed { index, column ->
				val value = column.createValue(parcel)
				widths[index] = max(widths[index], displayLines(value).maxOfOrNull { it.length } ?: 0)
			}
		}
		return IntArray(widths.size) { (widths[it] + ColumnPadding).coerceIn(MinColumnWidth, MaxColumnWidth) }
	}

	internal fun lineCount(value: WorkbookCellValue, charsPerLine: Int): Int =
		displayLines(value).sumOf { line -> max(1, (line.length + charsPerLine - 1) / charsPerLine) }

	private fun displayLines(value: WorkbookCellValue): List<String> =
		when (value)
		{
			WorkbookCellValue.Empty -> emptyList()
			is WorkbookCellValue.LongNumber -> listOf("%,d".format(value.value))
			is WorkbookCellValue.DateTime -> listOf("0".repeat(DateTextLength))
			is WorkbookCellValue.Text -> value.value.lines()
		}

	override fun fileName(username: String, rows: List<ParcelRow>, i18n: I18n, todayUtc: DateTimeUtc): String
	{
		val sentTimesUtc = rows.mapNotNull { it.uploadFinishTimeUtc }
		val fromUtc = sentTimesUtc.minByOrNull { it.epochMillis } ?: todayUtc
		val toUtc = sentTimesUtc.maxByOrNull { it.epochMillis } ?: todayUtc
		val name = i18n.get("sent-files.export-file-name", username, fromUtc.toUtcDateString(), toUtc.toUtcDateString())
		return "$name.$extension"
	}
}
