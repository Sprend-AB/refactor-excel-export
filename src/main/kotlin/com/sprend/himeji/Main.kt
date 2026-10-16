package com.sprend.himeji

import java.io.File

object Main
{
	@JvmStatic
	fun main(args: Array<String>)
	{
		val sampleRows = listOf(
			ParcelRow(
				filename = "presentation-slides.pdf",
				fileSizeInBytes = 1024 * 1024 * 15L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-16 12:00:00"),
				expireTimeUtc = DateTimeUtc("2026-10-23 12:00:00"),
				message = "Here are the presentation slides for today's meeting.",
				hasSuccessfulDownload = true
			),
			ParcelRow(
				filename = "financial-report-q3.xlsx",
				fileSizeInBytes = 1024 * 512L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-15 09:30:00"),
				expireTimeUtc = SysConstants.StorageTimePermanentDate,
				message = "Final Q3 financial figures.",
				hasSuccessfulDownload = false
			),
			ParcelRow(
				filename = "design-assets.zip",
				fileSizeInBytes = 1024 * 1024 * 128L,
				uploadFinishTimeUtc = DateTimeUtc("2026-10-14 18:45:00"),
				expireTimeUtc = DateTimeUtc("2026-10-21 18:45:00"),
				message = "Updated UI design bundle with icons and fonts.",
				hasSuccessfulDownload = true
			)
		)

		val outputFile = File("sample-export.xlsx")
		outputFile.outputStream().use { output ->
			ParcelExcelWriter.write(sampleRows, output, I18n(Language.English))
		}
		println("Generated ${outputFile.absolutePath} successfully.")
	}
}
