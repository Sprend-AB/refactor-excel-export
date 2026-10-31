package com.sprend.himeji.original

import com.sprend.himeji.DateTimeUtc
import com.sprend.himeji.I18n
import com.sprend.himeji.ParcelExcelExporter
import com.sprend.himeji.ParcelRow
import com.sprend.himeji.SysConstants
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.max

object OriginalParcelExcelWriter : ParcelExcelExporter
{
	const val Extension = "xlsx"
	private const val RowWindowSize = 100
	private const val ColumnWidthUnitsPerChar = 256
	internal const val MinColumnWidth = 10
	internal const val MaxColumnWidth = 80
	internal const val ColumnPadding = 3
	private const val DateTextLength = 19
	private const val SheetNameKey = "sent-files.export-sheet"

	internal class Column(val i18nKey: String, val style: (Styles) -> CellStyle, val value: (ParcelRow, I18n) -> Any?)
	{
		fun header(i18n: I18n): String = i18n.get("sent-files.export-column.$i18nKey")

		fun write(cell: Cell, value: Any?, styles: Styles)
		{
			cell.cellStyle = style(styles)
			when (value)
			{
				null -> {}
				is Long -> cell.setCellValue(value.toDouble())
				is DateTimeUtc -> cell.setCellValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(value.epochMillis), ZoneOffset.UTC))
				is String -> cell.setCellValue(value)
				else -> throw IllegalArgumentException("No cell writer for " + value.javaClass.name)
			}
		}
	}

	private val Columns = listOf(
		Column("upload-finished", Styles::date) { row, _ -> row.uploadFinishTimeUtc },
		Column("filename", Styles::leftTextAligned) { row, _ -> row.filename },
		Column("file-size-in-bytes", Styles::rightNumberAligned) { row, _ -> row.fileSizeInBytes },
		Column("expires", Styles::date) { row, i18n -> if (row.expireTimeUtc == SysConstants.StorageTimePermanentDate) i18n.get("sent-files.export-no-expiry") else row.expireTimeUtc },
		Column("message", Styles::leftTextAligned) { row, _ -> row.message },
		Column("downloaded", Styles::text) { row, i18n -> i18n.get(if (row.hasSuccessfulDownload) "common.yes" else "common.no").uppercase() },
	)

	override fun write(rows: List<ParcelRow>, output: OutputStream, i18n: I18n)
	{
		SXSSFWorkbook(RowWindowSize).use { workbook ->
			workbook.isCompressTempFiles = true
			val styles = Styles(workbook)
			val sheet = workbook.createSheet(i18n.get(SheetNameKey))
			sheet.createFreezePane(0, 1)
			val widths = columnWidths(rows, i18n)
			addHeaders(sheet, styles, i18n, widths)
			addCells(sheet, styles, rows, i18n, widths)
			sheet.setAutoFilter(CellRangeAddress(0, rows.size, 0, Columns.lastIndex))
			workbook.write(output)
		}
	}

	private fun columnWidths(rows: List<ParcelRow>, i18n: I18n): IntArray =
		IntArray(Columns.size) { index ->
			val column = Columns[index]
			val widestText = rows.fold(column.header(i18n).length) { widest, row ->
				max(widest, displayLines(column.value(row, i18n)).maxOfOrNull { it.length } ?: 0)
			}
			(widestText + ColumnPadding).coerceIn(MinColumnWidth, MaxColumnWidth)
		}

	private fun addHeaders(sheet: SXSSFSheet, styles: Styles, i18n: I18n, widths: IntArray)
	{
		val header = sheet.createRow(0)
		Columns.forEachIndexed { index, column ->
			header.createCell(index).apply {
				setCellValue(column.header(i18n))
				cellStyle = styles.header
			}
			sheet.setColumnWidth(index, widths[index] * ColumnWidthUnitsPerChar)
		}
	}

	private fun addCells(sheet: SXSSFSheet, styles: Styles, rows: List<ParcelRow>, i18n: I18n, widths: IntArray)
	{
		rows.forEachIndexed { rowIndex, row ->
			val sheetRow = sheet.createRow(rowIndex + 1)
			var tallestCell = 1
			Columns.forEachIndexed { index, column ->
				val value = column.value(row, i18n)
				column.write(sheetRow.createCell(index), value, styles)
				tallestCell = max(tallestCell, lineCount(value, widths[index] - ColumnPadding))
			}
			if (tallestCell > 1)
			{
				sheetRow.heightInPoints = tallestCell * sheet.defaultRowHeightInPoints
			}
		}
	}

	internal fun lineCount(value: Any?, charsPerLine: Int): Int =
		displayLines(value).sumOf { line -> max(1, (line.length + charsPerLine - 1) / charsPerLine) }

	private fun displayLines(value: Any?): List<String> =
		when (value)
		{
			null -> emptyList()
			is Long -> listOf("%,d".format(value))
			is DateTimeUtc -> listOf("0".repeat(DateTextLength))
			is String -> value.lines()
			else -> throw IllegalArgumentException("No cell writer for " + value.javaClass.name)
		}

	override fun fileName(username: String, rows: List<ParcelRow>, i18n: I18n, todayUtc: DateTimeUtc): String
	{
		val sentTimesUtc = rows.mapNotNull { it.uploadFinishTimeUtc }
		val fromUtc = sentTimesUtc.minByOrNull { it.epochMillis } ?: todayUtc
		val toUtc = sentTimesUtc.maxByOrNull { it.epochMillis } ?: todayUtc
		val name = i18n.get("sent-files.export-file-name", username, fromUtc.toUtcDateString(), toUtc.toUtcDateString())
		return "$name.$Extension"
	}
}
