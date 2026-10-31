package com.sprend.himeji.refactored

import com.sprend.himeji.I18n
import com.sprend.himeji.ParcelRow
import com.sprend.himeji.SysConstants

internal object SentParcelExcelColumnFactory
{
	fun createColumns(i18n: I18n, styles: WorkbookStyles): List<SentParcelExcelColumn> = listOf(
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.upload-finished",
			cellStyle = styles.centeredDateTime,
			createValue = { parcel -> WorkbookCellValue.dateTime(parcel.uploadFinishTimeUtc) },
		),
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.filename",
			cellStyle = styles.leftAlignedWrappedText,
			createValue = { parcel -> WorkbookCellValue.text(parcel.filename) },
		),
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.file-size-in-bytes",
			cellStyle = styles.rightAlignedNumber,
			createValue = { parcel -> WorkbookCellValue.longNumber(parcel.fileSizeInBytes) },
		),
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.expires",
			cellStyle = styles.centeredDateTime,
			createValue = { parcel ->
				if (parcel.expireTimeUtc == SysConstants.StorageTimePermanentDate)
					WorkbookCellValue.Text(i18n.get("sent-files.export-no-expiry"))
				else
					WorkbookCellValue.dateTime(parcel.expireTimeUtc)
			},
		),
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.message",
			cellStyle = styles.leftAlignedWrappedText,
			createValue = { parcel -> WorkbookCellValue.text(parcel.message) },
		),
		SentParcelExcelColumn(
			headerTranslationKey = "sent-files.export-column.downloaded",
			cellStyle = styles.centeredWrappedText,
			createValue = { parcel ->
				val key = if (parcel.hasSuccessfulDownload) "common.yes" else "common.no"
				WorkbookCellValue.Text(i18n.get(key).uppercase())
			},
		),
	)
}
