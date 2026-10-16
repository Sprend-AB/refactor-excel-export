package com.sprend.himeji;

import org.jetbrains.annotations.NotNull;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.joda.time.format.DateTimeFormat.forPattern;

/**
 * Represents a date and time, including seconds but not beyond that, in the UTC time zone.
 */
public class DateTimeUtc implements Comparable<DateTimeUtc>, Serializable
{
   private static final long serialVersionUID = 955599271888518310L;

   public static final String DatePattern = "yyyy-MM-dd";

   /**
    * UtcDateHoursMinutesSecondsFormatter is used in toMariaDbString method. It's mandatory that we save Date in this format for database.
    */
   private static final DateTimeFormatter UtcDateHoursMinutesSecondsFormatter = forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
   private static final DateTimeFormatter UtcDateHoursMinutesFormatter = forPattern("yyyy-MM-dd HH:mm").withZoneUTC();
   private static final DateTimeFormatter CetDateHoursMinutesFormatter = forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CET")));
   private static final DateTimeFormatter UtcDateFormatter = forPattern(DatePattern).withZoneUTC();
   private static final DateTimeFormatter CetDateFormatter = forPattern(DatePattern).withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CET")));

   private final DateTime dateTime;

   public DateTimeUtc(long epochMillis)
   {
      dateTime = new DateTime(epochMillis, DateTimeZone.UTC);
   }

   public DateTimeUtc(DateTime dateTime)
   {
      this.dateTime = dateTime.withMillisOfSecond(0);
   }

   public DateTimeUtc(String dateTimeString)
   {
      this((dateTimeString.contains(" ") ? UtcDateHoursMinutesSecondsFormatter : UtcDateFormatter).parseDateTime(dateTimeString));
   }

   public static DateTimeUtc now()
   {
      return new DateTimeUtc(DateTime.now(DateTimeZone.UTC));
   }

   public static DateTimeUtc daysAgo(int days)
   {
      return now().minusDays(days);
   }

   public DateTime getDateTime()
   {
      return dateTime;
   }

   public Date toDate()
   {
      return dateTime.toDate();
   }

   @Override
   public boolean equals(Object o)
   {
      return o instanceof DateTimeUtc && dateTime.equals(((DateTimeUtc)o).dateTime);
   }

   @Override
   public int compareTo(@NotNull DateTimeUtc compareWith)
   {
      return this.getDateTime().compareTo(compareWith.getDateTime());
   }

   public DateTimeUtc withTime(int hourOfDay, int minuteOfHour)
   {
      return withTime(hourOfDay, minuteOfHour, 0);
   }

   public DateTimeUtc withTime(int hourOfDay, int minuteOfHour, int secondOfMinute)
   {
      return new DateTimeUtc(dateTime.withTime(hourOfDay, minuteOfHour, secondOfMinute, 0));
   }

   public int getSecondOfDay()
   {
      return dateTime.getSecondOfDay();
   }

   public DateTimeUtc minusYears(int numberOfYears)
   {
      return new DateTimeUtc(dateTime.minusYears(numberOfYears));
   }

   public DateTimeUtc plusYears(int numberOfYears)
   {
      return new DateTimeUtc(dateTime.plusYears(numberOfYears));
   }

   public DateTimeUtc minusMonths(int numberOfMonths)
   {
      return new DateTimeUtc(dateTime.minusMonths(numberOfMonths));
   }

   public DateTimeUtc plusWeeks(int numberOfWeeks)
   {
      return new DateTimeUtc(dateTime.plusWeeks(numberOfWeeks));
   }

   public DateTimeUtc plusDays(int numberOfDays)
   {
      return new DateTimeUtc(dateTime.plusDays(numberOfDays));
   }

   public DateTimeUtc minusDays(int numberOfDays)
   {
      return new DateTimeUtc(dateTime.minusDays(numberOfDays));
   }

   public DateTimeUtc plusHours(int numberOfHours)
   {
      return new DateTimeUtc(dateTime.plusHours(numberOfHours));
   }

   public DateTimeUtc minusHours(int numberOfHours)
   {
      return new DateTimeUtc(dateTime.minusHours(numberOfHours));
   }

   public DateTimeUtc plusMillis(int millis)
   {
      return new DateTimeUtc(dateTime.plusMillis(millis));
   }

   public DateTimeUtc plusMinutes(int numberOfMinutes)
   {
      return new DateTimeUtc(dateTime.plusMinutes(numberOfMinutes));
   }

   public DateTimeUtc minusMinutes(int numberOfMinutes)
   {
      return new DateTimeUtc(dateTime.minusMinutes(numberOfMinutes));
   }

   public String toMariaDbString()
   {
      return UtcDateHoursMinutesSecondsFormatter.print(dateTime);
   }

   public long intervalInSeconds(DateTimeUtc endTime)
   {
      return Math.abs(Seconds.secondsBetween(dateTime, endTime.dateTime).getSeconds());
   }

   public long intervalInDays(DateTimeUtc endTime)
   {
      return Math.abs(Days.daysBetween(dateTime, endTime.dateTime).getDays());
   }

   public int calendarDaysAgo()
   {
      return Days.daysBetween(toLocalDate(), LocalDate.now()).getDays();
   }

   @NotNull
   private LocalDate toLocalDate()
   {
      return new LocalDate(dateTime);
   }

   public boolean isInThePast()
   {
      return DateTime.now(DateTimeZone.UTC).isAfter(dateTime);
   }

   public boolean isInTheFuture()
   {
      return !isInThePast();
   }

   public boolean isBefore(DateTimeUtc otherDateTimeUtc)
   {
      return dateTime.isBefore(otherDateTimeUtc.dateTime);
   }

   public boolean willPassWithinMinutes(int minutes)
   {
      return isBefore(DateTimeUtc.now().plusMinutes(minutes));
   }

   public long getEpochMillis()
   {
      return dateTime.getMillis();
   }

   @Override
   public String toString()
   {
      return toHumanReadableUtcString();
   }

   public String toDateAndTimeString()
   {
      return UtcDateHoursMinutesSecondsFormatter.print(dateTime);
   }

   public String toHumanReadableUtcString()
   {
      return UtcDateHoursMinutesSecondsFormatter.print(dateTime) + " UTC";
   }

   public String toUtcDateHoursMinutesString()
   {
      return UtcDateHoursMinutesFormatter.print(dateTime);
   }

   public String toCetDateHoursMinutesString()
   {
      return CetDateHoursMinutesFormatter.print(dateTime);
   }

   public String toUtcDateString()
   {
      return UtcDateFormatter.print(dateTime);
   }

   public String toCetDateString()
   {
      return CetDateFormatter.print(dateTime);
   }

   public String toJavascriptFormattedDateTime()
   {
      return wrapUpForHtml("document.write(formatDateTime(" + dateTime.getMillis() + "));");
   }

   public String toJavascriptFormattedDateTimeWithSeconds()
   {
      return wrapUpForHtml("document.write(formatDateTimeWithSeconds(" + dateTime.getMillis() + "));");
   }

   public String toJavascriptFormattedTime()
   {
      return wrapUpForHtml("document.write(formatTime(" + dateTime.getMillis() + "));");
   }

   private String wrapUpForHtml(String javascriptCode)
   {
      return MessageFormat.format("<span title=''{0}''><script type='text/javascript'>{1}</script></span>", toHumanReadableUtcString(), javascriptCode);
   }

   public String getMonthName(Locale locale)
   {
      return dateTime.monthOfYear().getAsText(locale);
   }

   public int hourlyDiff(DateTimeUtc other)
   {

      return Hours.hoursBetween(dateTime, other.getDateTime()).getHours();
   }

   public int dayDiff(DateTimeUtc other)
   {
      return Days.daysBetween(dateTime, other.getDateTime()).getDays();
   }
}
