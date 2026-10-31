package com.sprend.himeji.refactored

import com.sprend.himeji.ParcelMetaData
import org.apache.poi.ss.usermodel.CellStyle

internal class SentParcelExcelColumn(
	val headerTranslationKey: String,
	val cellStyle: CellStyle,
	val createValue: (ParcelMetaData) -> WorkbookCellValue,
)
