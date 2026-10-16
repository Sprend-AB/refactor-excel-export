package com.sprend.himeji;

import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import static org.joda.time.format.DateTimeFormat.forPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeUtcTest
{
	@Test
	public void testThatMillisecondsAreStrippedAway()
	{
		assertEquals(0, DateTimeUtc.now().getDateTime().getMillisOfSecond());
		assertEquals(0, new DateTimeUtc("2011-11-23 12:58:02").getDateTime().getMillisOfSecond());
	}

	@Test
	public void testEquals()
	{
		assertEquals(new DateTimeUtc("2011-01-23 11:58:02"), new DateTimeUtc("2011-01-23 11:58:02"));
	}

	@Test
	public void testMariaDbStringFormat()
	{
		DateTimeFormatter mariaDbFormatter = forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

		assertEquals(mariaDbFormatter.print(DateTimeUtc.now().getDateTime()), DateTimeUtc.now().toMariaDbString());
	}
}
