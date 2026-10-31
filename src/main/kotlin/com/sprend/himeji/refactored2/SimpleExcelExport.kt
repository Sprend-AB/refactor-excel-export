package com.sprend.himeji.refactored2

import com.sprend.himeji.DateTimeUtc
import com.sprend.himeji.I18n
import com.sprend.himeji.Language
import com.sprend.himeji.ParcelExcelExporter
import com.sprend.himeji.ParcelRow
import com.sprend.himeji.SysConstants
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.reflect.KProperty1

private const val RowWindowSize = 100
private const val MinColumnWidth = 10
private const val MaxColumnWidth = 80
private const val ColumnPadding = 3
private const val WidthUnitsPerCharacter = 256
private const val DateFormat = "yyyy-mm-dd hh:mm:ss"

//explicit order and translation keys property references don't require kotlin-reflect
private val MetadataColumns: List<Pair<KProperty1<FileMetadata, *>, String>> = listOf(
	FileMetadata::uploadFinishTimeUtc to "upload-finished",
	FileMetadata::filename to "filename",
	FileMetadata::fileSizeInBytes to "file-size-in-bytes",
	FileMetadata::expireTimeUtc to "expires",
	FileMetadata::message to "message",
	FileMetadata::hasSuccessfulDownload to "downloaded",
)

private enum class CellStyleKind
{ Header, Text, Number, Date, Status }

private fun createCellStyle(workbook: SXSSFWorkbook, kind: CellStyleKind): CellStyle =
	workbook.createCellStyle().apply {
		alignment = HorizontalAlignment.CENTER
		verticalAlignment = VerticalAlignment.CENTER
		when (kind)
		{
			CellStyleKind.Header -> setFont(workbook.createFont().apply { bold = true })
			CellStyleKind.Text ->
			{
				alignment = HorizontalAlignment.LEFT
				wrapText = true
			}
			CellStyleKind.Number -> alignment = HorizontalAlignment.RIGHT
			CellStyleKind.Date -> dataFormat = workbook.createDataFormat().getFormat(DateFormat)
			CellStyleKind.Status -> wrapText = true
		}
	}

fun writeFileMetadataXlsx(files: List<FileMetadata>, output: OutputStream, i18n: I18n = I18n(Language.English))
{
	//no use syntax to avoid indent level across 50 lines
	val workbook = SXSSFWorkbook(RowWindowSize)
	workbook.isCompressTempFiles = true
	val sheet = workbook.createSheet(i18n.get("sent-files.export-sheet"))
	val formatter = DataFormatter(i18n.locale)

	//fancy repetition avoiding through map instance
	val styles = CellStyleKind.entries.associateWith { createCellStyle(workbook, it) }

	fun formatCell(file: FileMetadata, property: KProperty1<FileMetadata, *>, cell: Cell? = null): String
	{
		val styleKind = when (property)
		{
			FileMetadata::filename, FileMetadata::message -> CellStyleKind.Text
			FileMetadata::fileSizeInBytes -> CellStyleKind.Number
			FileMetadata::hasSuccessfulDownload -> CellStyleKind.Status
			else -> CellStyleKind.Date
		}
		val style: CellStyle = styles.getValue(styleKind)
		cell?.cellStyle = style

		val rawValue = property.get(file) //file.fileName
		val value = when
		{
			property == FileMetadata::expireTimeUtc && rawValue == SysConstants.StorageTimePermanentDate ->
				i18n.get("sent-files.export-no-expiry")
			rawValue is Boolean -> i18n.get(if (rawValue) "common.yes" else "common.no").uppercase()
			else -> rawValue
		}
		return when (value)
		{
			null -> ""
			is String ->
			{
				cell?.setCellValue(value)
				value
			}
			is Long ->
			{
				cell?.setCellValue(value.toDouble())
				formatter.formatRawCellContents(value.toDouble(), style.dataFormat.toInt(), style.dataFormatString)
			}
			is DateTimeUtc ->
			{
				val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(value.epochMillis), ZoneOffset.UTC)
				cell?.setCellValue(date)
				formatter.formatRawCellContents(DateUtil.getExcelDate(date), style.dataFormat.toInt(), style.dataFormatString)
			}
			else -> error("Unsupported cell value: ${value.javaClass.name}")
		}
	}

	val widths = IntArray(MetadataColumns.size)
	for ((index, column) in MetadataColumns.withIndex())
	{
		val (property, translationKey) = column
		var width = i18n.get("sent-files.export-column.$translationKey").length
		for (file in files)
		{
			for (line in formatCell(file, property).lines())
			{
				width = maxOf(width, line.length)
			}
		}
		widths[index] = (width + ColumnPadding).coerceIn(MinColumnWidth, MaxColumnWidth)
	}

	val header = sheet.createRow(0)
	for ((index, column) in MetadataColumns.withIndex())
	{
		val (_, translationKey) = column
		header.createCell(index).apply {
			setCellValue(i18n.get("sent-files.export-column.$translationKey"))
			cellStyle = styles.getValue(CellStyleKind.Header)
		}
		sheet.setColumnWidth(index, widths[index] * WidthUnitsPerCharacter)
	}

	for ((rowIndex, file) in files.withIndex())
	{
		val row = sheet.createRow(rowIndex + 1)
		var heightInLines = 1
		for ((columnIndex, column) in MetadataColumns.withIndex())
		{
			val (property, _) = column
			val text = formatCell(file, property, row.createCell(columnIndex))
			val charactersPerLine = widths[columnIndex] - ColumnPadding
			val lines = text.lines().sumOf { maxOf(1, (it.length + charactersPerLine - 1) / charactersPerLine) }
			heightInLines = maxOf(heightInLines, lines)
		}
		if (heightInLines > 1) row.heightInPoints = heightInLines * sheet.defaultRowHeightInPoints
	}

	sheet.createFreezePane(0, 1)
	sheet.setAutoFilter(CellRangeAddress(0, files.size, 0, MetadataColumns.lastIndex))
	workbook.write(output)
	workbook.close()
}

fun writeFileMetadataXlsx(files: List<FileMetadata>, path: String, i18n: I18n = I18n(Language.English))
{
	val outputFile = File(path)
	outputFile.outputStream().use { output ->
		writeFileMetadataXlsx(files, output, i18n)
	}
	println("Generated ${outputFile.absolutePath} successfully.")
}

object SimpleExcelExport : ParcelExcelExporter
{
	override fun write(rows: List<ParcelRow>, output: OutputStream, i18n: I18n)
	{
		val files = rows.map { row ->
			FileMetadata(
				filename = row.filename,
				fileSizeInBytes = row.fileSizeInBytes,
				uploadFinishTimeUtc = row.uploadFinishTimeUtc,
				expireTimeUtc = row.expireTimeUtc,
				message = row.message,
				hasSuccessfulDownload = row.hasSuccessfulDownload,
			)
		}
		writeFileMetadataXlsx(files, output, i18n)
	}

	override fun fileName(username: String, rows: List<ParcelRow>, i18n: I18n, todayUtc: DateTimeUtc): String
	{
		val sentTimesUtc = rows.mapNotNull { it.uploadFinishTimeUtc }
		val fromUtc = sentTimesUtc.minByOrNull { it.epochMillis } ?: todayUtc
		val toUtc = sentTimesUtc.maxByOrNull { it.epochMillis } ?: todayUtc
		val name = i18n.get("sent-files.export-file-name", username, fromUtc.toUtcDateString(), toUtc.toUtcDateString())
		return "$name.xlsx"
	}
}
