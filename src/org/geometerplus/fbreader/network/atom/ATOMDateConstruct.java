/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network.atom;

import java.util.*;


class ATOMDateConstruct extends ATOMCommonAttributes {

	public int Year;
	public int Month;
	public int Day;
	public int Hour;
	public int Minutes;
	public int Seconds;
	public float SecondFraction;
	public int TZHour;
	public int TZMinutes;

	public ATOMDateConstruct(int year) {
		Year = year;
		Month = Day = Hour = Minutes = Seconds = TZHour = TZMinutes = 0;
		SecondFraction = 0.0f;
	}

	public ATOMDateConstruct(int year, int month, int day) {
		Year = year;
		Month = month;
		Day = day;
		Hour = Minutes = Seconds = TZHour = TZMinutes = 0;
		SecondFraction = 0.0f;
	}

	public ATOMDateConstruct(int year, int month, int day, int hour, int minutes, int seconds) {
		Year = year;
		Month = month;
		Day = day;
		Hour = hour;
		Minutes = minutes;
		Seconds = seconds;
		TZHour = TZMinutes = 0;
		SecondFraction = 0.0f;
	}

	public ATOMDateConstruct(int year, int month, int day, int hour, int minutes, int seconds, float sfract) {
		Year = year;
		Month = month;
		Day = day;
		Hour = hour;
		Minutes = minutes;
		Seconds = seconds;
		TZHour = TZMinutes = 0;
		SecondFraction = sfract;
	}

	public ATOMDateConstruct(int year, int month, int day, int hour, int minutes, int seconds, float sfract, int tzhour, int tzminutes) {
		Year = year;
		Month = month;
		Day = day;
		Hour = hour;
		Minutes = minutes;
		Seconds = seconds;
		TZHour = tzhour;
		TZMinutes = tzminutes;
		SecondFraction = sfract;
	}

	public static ATOMDateConstruct parse(String str) {
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minutes = 0;
		int seconds = 0;
		float secondFraction = 0.0f;
		int tzHour = 0;
		int tzMinutes = 0;

		final int len = str.length();

		if (len != 4 && len != 7 && len != 10 && len != 17 && len != 20 && len < 22) {
			return null;
		}

		int num = 0, sign = 1;
		float fnum = 0.0f, fmult = 0.1f;
		int start, end, log;
		char ch;
		end = 4; start = 0; log = 0;
		while (start < len) {
			ch = str.charAt(start++);
			if (!Character.isDigit(ch)) {
				return null;
			}
			num = 10 * num + ((int) (ch - '0'));
			fnum += fmult * ((int) (ch - '0'));
			fmult *= 0.1f;
			if (start == end) {
				switch (log) {
				case 0: year = num; break;
				case 1: month = num; break;
				case 2: day = num; break;
				case 3: hour = num; break;
				case 4: minutes = num; break;
				case 5: seconds = num; break;
				case 6: secondFraction = fnum; break;
				case 7: tzHour = sign * num; break;
				case 8: tzMinutes = sign * num; break;
				default: return null;
				}
				num = 0; fnum = 0.0f; fmult = 0.1f;
				if (start == len) return new ATOMDateConstruct(year, month, day, hour, minutes, seconds, secondFraction, tzHour, tzMinutes);
				switch (log) {
				case 0:
				case 1:
					if (str.charAt(start++) != '-') return null;
					end = start + 2;
					break;
				case 2:
					if (str.charAt(start++) != 'T') return null;
					end = start + 2;
					break;
				case 3:
				case 7:
					if (str.charAt(start++) != ':') return null;
					end = start + 2;
					break;
				case 4:
					ch = str.charAt(start++);
					if (ch == ':') {
						end = start + 2;
					} else if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						log += 2;
						end = start + 2;
					} else if (ch == 'Z') {
						return new ATOMDateConstruct(year, month, day, hour, minutes, seconds, secondFraction, tzHour, tzMinutes);
					} else return null;
					break;
				case 5:
					ch = str.charAt(start++);
					if (ch == '.') {
						end = start;
						while (Character.isDigit(str.charAt(++end))) /* NOP */;
					} else if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						log += 1;
						end = start + 2;
					} else if (ch == 'Z') {
						return new ATOMDateConstruct(year, month, day, hour, minutes, seconds, secondFraction, tzHour, tzMinutes);
					} else return null;
					break;
				case 6:
					ch = str.charAt(start++);
					if (ch == '+' || ch == '-') {
						sign = (ch == '-') ? -1 : 1;
						end = start + 2;
					} else if (ch == 'Z') {
						return new ATOMDateConstruct(year, month, day, hour, minutes, seconds, secondFraction, tzHour, tzMinutes);
					} else return null;
					break;
				//case 8:
				default: return null;
				}
				++log;
			}
		}
		return null;
	}

	private static void appendChars(StringBuilder buffer, char ch, int count) {
		while (count-- > 0) {
			buffer.append(ch);
		}
	}

	public final String getDateTime(boolean brief) {
		StringBuilder timezone = new StringBuilder("Z");
		if (TZMinutes != 0 || TZHour != 0) {
			int tzminnum = TZMinutes;
			int tzhournum = TZHour;
			char sign;
			if (tzhournum == 0) {
				sign = (tzminnum >= 0) ? '+' : '-';
			} else {
				sign = (tzhournum > 0) ? '+' : '-';
				if (tzhournum > 0 && tzminnum < 0) {
					--tzhournum;
					tzminnum = 60 + tzminnum;
				} else if (tzhournum < 0 && tzminnum > 0) {
					++tzhournum;
					tzminnum = 60 - tzminnum;
				}
			}
			String tzmin = String.valueOf(tzminnum < 0 ? -tzminnum : tzminnum);
			String tzhour = String.valueOf(tzhournum < 0 ? -tzhournum : tzhournum);
			timezone.append(sign);
			appendChars(timezone, '0', 2 - tzhour.length());
			timezone.append(tzhour);
			timezone.append(':');
			appendChars(timezone, '0', 2 - tzmin.length());
			timezone.append(tzmin);
		}

		StringBuilder time = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		if (SecondFraction >= 0.01f) {
			int sfrnum = Math.round(100 * SecondFraction);
			String sfr = String.valueOf(sfrnum);
			time.append('.');
			appendChars(time, '0', 2 - sfr.length());
			time.append(sfr);
		}
		if (!brief || time.length() != 0 || Seconds != 0) {
			String sec = String.valueOf(Seconds);
			temp.append(':');
			appendChars(temp, '0', 2 - sec.length());
			temp.append(sec);
			time.insert(0, temp.toString());
			temp.delete(0, temp.length());
		}
		if (!brief || time.length() != 0 || Hour != 0 || Minutes != 0 || timezone.length() > 1) {
			String hour = String.valueOf(Hour);
			String min = String.valueOf(Minutes);
			appendChars(temp, '0', 2 - hour.length());
			temp.append(hour);
			temp.append(':');
			appendChars(temp, '0', 2 - min.length());
			temp.append(min);
			time.insert(0, temp.toString());
			temp.delete(0, temp.length());
		}

		StringBuilder date = new StringBuilder();
		if (!brief || time.length() != 0 || Day != 0) {
			String day = String.valueOf(Day);
			date.append('-');
			appendChars(date, '0', 2 - day.length());
			date.append(day);
		}
		if (!brief || date.length() != 0 || Month != 0) {
			String month = String.valueOf(Month);
			temp.append('-');
			appendChars(temp, '0', 2 - month.length());
			temp.append(month);
			date.insert(0, temp.toString());
			temp.delete(0, temp.length());
		}

		String year = String.valueOf(Year);
		appendChars(temp, '0', 4 - year.length());
		temp.append(year);
		date.insert(0, temp.toString());
		temp.delete(0, temp.length());

		if (!brief || time.length() != 0) {
			date.append('T');
			date.append(time.toString());
			date.append(timezone.toString());
		}
		return date.toString();
	}

	@Override
	public String toString() {
		return getDateTime(false);
	}
};

