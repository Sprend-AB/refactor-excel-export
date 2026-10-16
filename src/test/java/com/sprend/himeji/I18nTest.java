package com.sprend.himeji;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.sprend.himeji.Language.English;
import static com.sprend.himeji.I18n.getTranslation;
import static org.junit.jupiter.api.Assertions.*;

public class I18nTest
{
	@Test
	public void turnIntoSupportedLanguageCode()
	{
		assertEquals("en", Language.Companion.forgivingParse("").code());
		assertEquals("en", Language.Companion.forgivingParse("e ").code());
		assertEquals("en", Language.Companion.forgivingParse(null).code());
		assertEquals("en", Language.Companion.forgivingParse("en").code());
		assertEquals("sv", Language.Companion.forgivingParse("sv").code());
		assertEquals("sv", Language.Companion.forgivingParse("sv-se").code());
		assertEquals("en", Language.Companion.forgivingParse("no").code());
		assertEquals("en", Language.Companion.forgivingParse("en-US,sv-fi;q=0.5").code());
		assertEquals("en", Language.Companion.forgivingParse("zh").code());
		assertEquals("en", Language.Companion.forgivingParse("ta-in,sv-fi;q=0.8,sv;q=0.6,no;q=0.4,en;q=0.2").code());
	}

	@Test
	public void walksAndQuacksLikeAKey()
	{
		assertTrue(I18n.walksAndQuacksLikeAKey("common.file-has-expired-long-explanation"));
		assertTrue(I18n.walksAndQuacksLikeAKey("upload-progress.errormessage.filenotspecified"));
		assertTrue(I18n.walksAndQuacksLikeAKey("a.b"));
		assertTrue(I18n.walksAndQuacksLikeAKey("a.b.c.d"));
	}

	@Test
	public void doesntWalkAndQuackLikeAKey()
	{
		assertFalse(I18n.walksAndQuacksLikeAKey("common.file-has-expired-long-explanation."));
		assertFalse(I18n.walksAndQuacksLikeAKey("File is marked as existing but is missing on disk."));
		assertFalse(I18n.walksAndQuacksLikeAKey("a.b."));
		assertFalse(I18n.walksAndQuacksLikeAKey("a. b"));
		assertFalse(I18n.walksAndQuacksLikeAKey("a.b "));
		assertFalse(I18n.walksAndQuacksLikeAKey(" a.b"));
		assertFalse(I18n.walksAndQuacksLikeAKey("a"));
		assertFalse(I18n.walksAndQuacksLikeAKey(".a"));
		assertFalse(I18n.walksAndQuacksLikeAKey("a."));
		assertFalse(I18n.walksAndQuacksLikeAKey(""));
		assertFalse(I18n.walksAndQuacksLikeAKey(null));
	}

	@Test
	public void nestedStrings()
	{
		Properties transStrings = new Properties();
		transStrings.put("test.homer.en", "Homer");
		transStrings.put("test.simpson.en", "Simpson");
		transStrings.put("test.homer-simpson.en", "{{test.homer}} {{test.simpson}}");
		transStrings.put("test.marge-is-the-wife-of-homer-simpson.en", "Marge is the wife of {{test.homer-simpson}}");

		assertEquals("Homer", getTranslation("test.homer", English, 0, transStrings));
		assertEquals("Homer Simpson", getTranslation("test.homer-simpson", English, 0, transStrings));
		assertEquals("Marge is the wife of Homer Simpson", getTranslation("test.marge-is-the-wife-of-homer-simpson", English, 0, transStrings));
	}

	@Test
	public void stopRecursionInNestedStrings()
	{
		Properties transStrings = new Properties();
		transStrings.put("test.recursive.en", "Repeat {{test.recursive}}");

		assertEquals("Repeat Repeat Repeat Repeat Repeat Max nesting depth reached", getTranslation("test.recursive", English, 0, transStrings));
	}
}
