package com.sprend.himeji

import com.sprend.himeji.original.OriginalParcelExcelWriter
import com.sprend.himeji.refactored.SentParcelsExcelWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import kotlin.math.max

class ParcelExcelWriterTest
{
	companion object
	{
		@BeforeAll
		@JvmStatic
		fun loadTranslations()
		{
			I18n.loadTranslatedStrings("src/main/resources/text.properties.xml")
		}
	}

	abstract class ExporterTestSuite(private val exporter: ParcelExcelExporter)
	{
		private fun row() = ParcelRow(
			filename = "test.txt",
			fileSizeInBytes = 123456789L,
			uploadFinishTimeUtc = DateTimeUtc("2026-09-25 13:14:15"),
			expireTimeUtc = DateTimeUtc("2026-10-02 13:14:15"),
			message = "Test message",
			hasSuccessfulDownload = true
		)

		private fun excel(rows: List<ParcelRow>, language: Language = Language.English): XSSFWorkbook
		{
			val output = ByteArrayOutputStream()
			exporter.write(rows, output, I18n(language))
			return XSSFWorkbook(output.toByteArray().inputStream())
		}

		@Test
		fun fileNameIsTranslatedWithTheSenderAndTheRangeOfSentDates()
		{
			val rows = listOf(row(), row().copy(uploadFinishTimeUtc = DateTimeUtc("2026-07-01 08:00:00")), row().copy(uploadFinishTimeUtc = null))
			assertEquals("Sent with Sprend by user@example.com - 2026-07-01 to 2026-09-25.xlsx",
				exporter.fileName("user@example.com", rows, I18n(Language.English)))
		}

		@Test
		fun fileNameUsesTodayWhenThereIsNothingSent()
		{
			assertEquals("Sent with Sprend by user@example.com - 2026-09-28 to 2026-09-28.xlsx",
				exporter.fileName("user@example.com", emptyList(), I18n(Language.English), DateTimeUtc("2026-09-28 23:30:00")))
		}

		@Test
		fun excelKeepsValueTypesAndHasAFilterOverAllColumns()
		{
			excel(listOf(row(), row().copy(hasSuccessfulDownload = false))).use { workbook ->
				val sheet = workbook.getSheetAt(0)
				assertEquals("Sent files", sheet.sheetName)
				assertEquals(6, sheet.getRow(0).lastCellNum.toInt())
				assertEquals("File size (bytes)", sheet.getRow(0).getCell(2).stringCellValue)
				val cells = sheet.getRow(1)
				assertTrue(DateUtil.isCellDateFormatted(cells.getCell(0)))
				assertEquals(LocalDateTime.of(2026, 9, 25, 13, 14, 15), cells.getCell(0).localDateTimeCellValue)
				assertEquals(row().filename, cells.getCell(1).stringCellValue)
				assertEquals("123456789", DataFormatter().formatCellValue(cells.getCell(2)))
				assertEquals(LocalDateTime.of(2026, 10, 2, 13, 14, 15), cells.getCell(3).localDateTimeCellValue)
				assertEquals("Test message", cells.getCell(4).stringCellValue)
				assertEquals("YES", cells.getCell(5).stringCellValue)
				assertEquals("NO", sheet.getRow(2).getCell(5).stringCellValue)
				assertEquals("A1:F3", sheet.ctWorksheet.autoFilter.ref)
			}
		}

		@Test
		fun excelShowsFileSizeInRawBytes()
		{
			val sizes = listOf(500L, 59_920L, 1_048_575L, 5_368_709_120L)
			val shown = excelFileSizeCells(sizes).map { DataFormatter().formatCellValue(it) }
			assertEquals(listOf("500", "59920", "1048575", "5368709120"), shown)
		}

		private fun excelFileSizeCells(sizes: List<Long>) =
			excel(sizes.map { row().copy(fileSizeInBytes = it) }).use { workbook ->
				(1..sizes.size).map { workbook.getSheetAt(0).getRow(it).getCell(2) }
			}

		@Test
		fun excelLeavesCellsEmptyForMissingValues()
		{
			val empty = row().copy(filename = null, fileSizeInBytes = null, uploadFinishTimeUtc = null, message = null)
			excel(listOf(empty)).use { workbook ->
				val cells = workbook.getSheetAt(0).getRow(1)
				for (index in listOf(0, 1, 2, 4))
				{
					assertEquals(CellType.BLANK, cells.getCell(index).cellType)
				}
				assertEquals("YES", cells.getCell(5).stringCellValue)
			}
		}

		@Test
		fun excelShowsTheNoExpiryLabelForPermanentStorage()
		{
			excel(listOf(row().copy(expireTimeUtc = SysConstants.StorageTimePermanentDate))).use { workbook ->
				assertEquals("No expiry", workbook.getSheetAt(0).getRow(1).getCell(3).stringCellValue)
			}
		}

		@Test
		fun everyLanguageHasTranslatedHeadersAndFileName()
		{
			for (language in Language.entries)
			{
				val fileName = exporter.fileName("user@example.com", listOf(row()), I18n(language))
				assertFalse(fileName.startsWith("sent-files."))
				assertTrue(fileName.contains("user@example.com") && fileName.contains("2026-09-25"))
				excel(emptyList(), language).use { workbook ->
					for (cell in workbook.getSheetAt(0).getRow(0))
					{
						assertFalse(cell.stringCellValue.startsWith("sent-files."))
					}
				}
			}
		}

		@Test
		fun columnIsAsWideAsItsWidestTextUpToTheLimit()
		{
			excel(listOf(row().copy(filename = "f".repeat(40), message = "m".repeat(500)))).use { workbook ->
				val sheet = workbook.getSheetAt(0)
				assertEquals((40 + OriginalParcelExcelWriter.ColumnPadding) * 256, sheet.getColumnWidth(1))
				assertEquals(OriginalParcelExcelWriter.MaxColumnWidth * 256, sheet.getColumnWidth(4))
				val downloadedHeader = sheet.getRow(0).getCell(5).stringCellValue
				val expectedWidth = max(OriginalParcelExcelWriter.MinColumnWidth, downloadedHeader.length + OriginalParcelExcelWriter.ColumnPadding)
				assertEquals(expectedWidth * 256, sheet.getColumnWidth(5))
			}
		}

		@Test
		fun rowIsAsTallAsItsTallestText()
		{
			excel(listOf(row().copy(message = "a\nb\nc"), row())).use { workbook ->
				val sheet = workbook.getSheetAt(0)
				assertEquals(3 * sheet.defaultRowHeightInPoints, sheet.getRow(1).heightInPoints)
				assertEquals(sheet.defaultRowHeightInPoints, sheet.getRow(2).heightInPoints)
			}
		}
	}

	@Nested
	inner class OriginalParcelExcelWriterTests : ExporterTestSuite(OriginalParcelExcelWriter)

	@Nested
	inner class RefactoredSentParcelsExcelWorkbook : ExporterTestSuite(SentParcelsExcelWorkbook)
}
