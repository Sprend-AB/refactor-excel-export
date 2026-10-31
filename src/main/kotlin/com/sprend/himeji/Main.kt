package com.sprend.himeji

import com.sprend.himeji.original.ParcelExcelWriter
import com.sprend.himeji.refactored.SentParcelsExcelWorkbook
import com.sprend.himeji.refactored2.SimpleExcelExport
import java.io.File

object Main
{
	@JvmStatic
	fun main(args: Array<String>)
	{
		val sampleRows = listOf(
			ParcelMetaData(
				filename = "presentation-slides.pdf",
				fileSizeInBytes = 1024 * 1024 * 15L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-16 12:00:00"),
				expireTimeUtc = DateTimeUtc("2026-10-23 12:00:00"),
				message = "Here are the presentation slides for today's meeting.",
				hasSuccessfulDownload = true
			),
			ParcelMetaData(
				filename = "financial-report-q3.xlsx",
				fileSizeInBytes = 1024 * 512L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-15 09:30:00"),
				expireTimeUtc = SysConstants.StorageTimePermanentDate,
				message = "Final Q3 financial figures.",
				hasSuccessfulDownload = false
			),
			ParcelMetaData(
				filename = "design-assets.zip",
				fileSizeInBytes = 1024 * 1024 * 128L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-14 18:45:00"),
				expireTimeUtc = DateTimeUtc("2026-10-21 18:45:00"),
				message = "Updated UI design bundle with icons and fonts.",
				hasSuccessfulDownload = true
			)
		)

		val exporters: List<Pair<String, ParcelExcelExporter>> = listOf(
			"sample-export-original.xlsx" to ParcelExcelWriter,
			"sample-export-refactored.xlsx" to SentParcelsExcelWorkbook,
			"sample-export-refactored2.xlsx" to SimpleExcelExport,
		)

		for ((filename, exporter) in exporters)
		{
			val outputFile = File(filename)
			outputFile.outputStream().use { output ->
				exporter.write(sampleRows, output, I18n(Language.English))
			}
			println("Generated ${outputFile.absolutePath} successfully using ${exporter.javaClass.simpleName}.")
		}
	}
}
