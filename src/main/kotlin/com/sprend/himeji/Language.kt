package com.sprend.himeji

import java.util.*

enum class Language(val code: String)
{
	Croatian("hr"),
	English("en"),
	French("fr"),
	Italian("it"),
	Portuguese("pt"),
	Spanish("es"),
	Swedish("sv");

	val locale = Locale(code)

	init
	{
		require(code.length == 2) { "Language code must be exactly 2 characters long, got: $code" }
	}

	fun code(): String = code

	companion object
	{
		fun forgivingParse(codeStr: String?): Language
		{
			val code = codeStr?.trim()?.take(2)?.lowercase(Locale.getDefault())
			return code?.let { fromCode(it) } ?: English
		}

		fun fromCode(codeStr: String?): Language? = entries.find { it.code == codeStr }
	}
}
