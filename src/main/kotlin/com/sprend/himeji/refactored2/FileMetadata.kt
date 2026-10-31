package com.sprend.himeji.refactored2

import com.sprend.himeji.DateTimeUtc

/**
 * Parcel seems as leaking terminology from transfer context.
 * Row indicates intended place of usage.
 * FileMetadata name is agnostic from both intended usage and transfer coupling
 * Integrated in larger frame Parcel may be more custom and appropriate name.
 * Here I'd suggest this
 */
data class FileMetadata(
	val filename: String?,
	val fileSizeInBytes: Long?,
	val uploadFinishTimeUtc: DateTimeUtc?,
	val expireTimeUtc: DateTimeUtc?,
	val message: String?,
	val hasSuccessfulDownload: Boolean,
)
