package com.sprend.himeji

import java.io.OutputStream

interface ParcelExcelExporter
{
	fun write(rows: List<ParcelMetaData>, output: OutputStream, i18n: I18n)

	fun fileName(
		username: String,
		rows: List<ParcelMetaData>,
		i18n: I18n,
		todayUtc: DateTimeUtc = DateTimeUtc.now()
	): String
}
