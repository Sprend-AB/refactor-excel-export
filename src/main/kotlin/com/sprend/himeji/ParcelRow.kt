package com.sprend.himeji

data class ParcelRow(
	val filename: String?,
	val fileSizeInBytes: Long?,
	val uploadFinishTimeUtc: DateTimeUtc?,
	val expireTimeUtc: DateTimeUtc?,
	val message: String?,
	val hasSuccessfulDownload: Boolean,
)
