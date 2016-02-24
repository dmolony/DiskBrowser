package com.bytezone.diskbrowser.utilities;

class DateTime
{
  private static String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                                    "Sep", "Oct", "Nov", "Dec" };
  private static String[] days = { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
                                  "Friday", "Saturday" };

  private final int second;
  private final int minute;
  private final int hour;
  private final int year;
  private final int day;
  private final int month;
  private final int weekDay;

  public DateTime (byte[] buffer, int ptr)
  {
    second = buffer[ptr] & 0xFF;
    minute = buffer[++ptr] & 0xFF;
    hour = buffer[++ptr] & 0xFF;
    year = buffer[++ptr] & 0xFF;
    day = buffer[++ptr] & 0xFF;
    month = buffer[++ptr] & 0xFF;
    ++ptr;     // empty
    weekDay = buffer[++ptr] & 0xFF;
  }

  public String format ()
  {
    return String.format ("%02d:%02d:%02d %s %d %s %d", hour, minute, second, days[weekDay],
                          day, months[month], year);
  }

  @Override
  public String toString ()
  {
    return "DateTime [second=" + second + ", minute=" + minute + ", hour=" + hour + ", year="
          + year + ", day=" + day + ", month=" + month + ", weekDay=" + weekDay + "]";
  }
}