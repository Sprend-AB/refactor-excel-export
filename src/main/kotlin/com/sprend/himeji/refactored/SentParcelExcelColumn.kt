package com.sprend.himeji.refactored

import com.sprend.himeji.ParcelRow
import org.apache.poi.ss.usermodel.CellStyle

internal class SentParcelExcelColumn(
	val headerTranslationKey: String,
	val cellStyle: CellStyle,
	val createValue: (ParcelRow) -> WorkbookCellValue,
)
