package com.sprend.himeji;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

import static com.sprend.himeji.Language.English;

public class I18n
{
	private static final String SameAsInEnglish = "[Same as in English]";
	private static final String DefaultTranslationResource = "/text.properties.xml";

	public static Properties translatedStrings = loadTranslatedStrings(DefaultTranslationResource);

	private final Language language;

	public I18n(String codeStr)
	{
		language = Language.Companion.forgivingParse(codeStr);
	}

	public I18n(Language lang)
	{
		this.language = lang != null ? lang : English;
	}

	public String get(String key, Object... args)
	{
		return gt(language, key, args);
	}

	public Language lang()
	{
		return language;
	}

	public boolean isEnglish()
	{
		return lang() == English;
	}

	public String getLangCode()
	{
		return language.getCode();
	}

	public Locale getLocale()
	{
		return language.getLocale();
	}

	public static String gt(Language lang, String keyOrMessage, Object... args)
	{
		if (keyOrMessage == null || keyOrMessage.trim().isEmpty())
			return "Parameter must not be empty: keyOrMessage";
		if (lang == null)
			return keyOrMessage;

		if (translatedStrings == null) return keyOrMessage;

		String translation = getTranslation(keyOrMessage, lang, 0, translatedStrings);
		MessageFormat messageFormat = new MessageFormat(escapeSingleQuotes(translation), lang.getLocale());
		return messageFormat.format(args);
	}

	public static String getTranslation(String keyOrMessage, Language lang, int nestingDepth, Properties transStrings)
	{
		if (nestingDepth >= 5)
		{
			return "Max nesting depth reached";
		}
		if (keyOrMessage == null) return null;
		keyOrMessage = keyOrMessage.trim();
		String completeKey = keyOrMessage + "." + lang.code();
		String translation = transStrings.getProperty(completeKey);
		if (translation == null || translation.isEmpty() ||
				  translation.equalsIgnoreCase(SameAsInEnglish))
		{
			translation = getStringInEnglish(keyOrMessage, transStrings);
			if (translation == null)
			{
				translation = keyOrMessage;
			}
		}
		translation = getNestedTranslations(translation, lang, nestingDepth, transStrings);
		return translation;
	}

	private static String getNestedTranslations(String translation, Language lang, int nestingDepth, Properties transStrings)
	{
		if (translation == null) return null;
		String open = "{{";
		String close = "}}";
		int openIdx;
		while ((openIdx = translation.indexOf(open)) != -1)
		{
			int closeIdx = translation.indexOf(close, openIdx + open.length());
			if (closeIdx == -1) break;
			String subkey = translation.substring(openIdx + open.length(), closeIdx).trim();
			String subTranslation;
			if (!subkey.isEmpty())
			{
				subTranslation = getTranslation(subkey, lang, nestingDepth + 1, transStrings);
			}
			else
			{
				subTranslation = "Missing subkey";
			}
			translation = translation.substring(0, openIdx) + subTranslation + translation.substring(closeIdx + close.length());
		}
		return translation;
	}

	public static String getStringInEnglish(String key)
	{
		return getStringInEnglish(key, translatedStrings);
	}

	public static String getStringInEnglish(String key, Properties transStrings)
	{
		if (transStrings == null || key == null) return null;
		String completeKey = key + "." + English.code();
		String translation = transStrings.getProperty(completeKey);
		if (translation == null || translation.isEmpty() || translation.equalsIgnoreCase(SameAsInEnglish))
			return null;
		else
			return translation;
	}

	public static boolean walksAndQuacksLikeAKey(String keyOrMessage)
	{
		return keyOrMessage != null && keyOrMessage.contains(".") && !keyOrMessage.startsWith(".") && !keyOrMessage.endsWith(".") && !keyOrMessage.contains(" ");
	}

	public static Properties loadTranslatedStrings(String pathOrResource)
	{
		Properties properties = new Properties();
		if (pathOrResource == null) return properties;
		try
		{
			InputStream in = null;
			File file = new File(pathOrResource);
			if (file.exists())
			{
				in = new FileInputStream(file);
			}
			else
			{
				String resourcePath = pathOrResource.startsWith("/") ? pathOrResource : "/" + pathOrResource;
				in = I18n.class.getResourceAsStream(resourcePath);
			}
			if (in != null)
			{
				try (InputStream is = in)
				{
					properties.loadFromXML(is);
				}
			}
		}
		catch (Exception ignored)
		{
		}
		return properties;
	}

	private static String escapeSingleQuotes(String str)
	{
		if (str == null) return "";
		return str.replace("'", "''");
	}
}
