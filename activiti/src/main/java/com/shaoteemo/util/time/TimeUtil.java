package com.shaoteemo.util.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author shaoteemo
 * @Date 2021/8/11
 *
 * @since 0.1
 */
public class TimeUtil {

    /**
     * 年月日格式
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时分秒毫秒格式
     */
    private static final String TIME_PATTERN_MILL = "HH:mm:ss.SSS";

    /**
     * 时分秒格式
     */
    private static final String TIME_PATTERN = "HH:mm:ss";

    private static final String BLANK = " ";

    private static final String CHINA_BEIJING_OFFSET = "+08:00";

    /**
     * 一种复合时间格式:yyyy-MM-dd HH:mm:ss.SSS
     */
    private static final String STANDARD_PATTERN = DATE_PATTERN + BLANK + TIME_PATTERN_MILL;

    /**
     * 获取当前日期时间
     *
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime getDateTimeNow() {
        return getDateTimeNow(ZoneId.systemDefault());
    }

    /**
     * 获取指定时区的日期时间
     *
     * @param zoneId 时间偏移量
     * @return --
     */
    public static LocalDateTime getDateTimeNow(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }

    /**
     * 获取当前事件 24小时制
     *
     * @return {@link LocalTime}
     */
    public static LocalTime getTimeNow() {
        return getDateTimeNow().toLocalTime();
    }

    /**
     * 获取当前的日期
     *
     * @return {@link LocalDate}
     */
    public static LocalDate getDateNow() {
        return getDateTimeNow().toLocalDate();
    }

    /**
     * 获取格式化后的当前时间
     * <p/>
     * 格式化后的格式为：yyyy-MM-dd HH:mm:ss.SSS --> 1970-12-11 23:48:59.123
     *
     * @return {@link String}
     */
    public static String getFormatDateTime() {
        return getFormatDateTime(STANDARD_PATTERN);
    }


    /**
     * 获取当前自定义格式时间
     *
     * @param pattern 格式化模板
     * @return 对应模板的时间字符串
     */
    public static String getFormatDateTime(String pattern) {
        return getFormatDateTime(getDateTimeNow(), pattern);
    }

    /**
     * 格式化一个时间
     *
     * @param dateTime 时间
     * @param pattern 格式
     * @return --
     */
    public static String getFormatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 格式化后的格式为：yyyy-MM-dd HH:mm:ss.SSS --> 1970-12-11 23:48:59.123
     *
     * @param date type of {@link Date} time
     * @return format result
     */
    public static String getFormatDateTime(Date date) {
        return getDateFormat(date, STANDARD_PATTERN);
    }

    public static String getFormatDateTime(Date date, String pattern) {
        return getDateFormat(date, pattern);
    }


    /**
     * 获取Long的格式化时间
     *
     */
    public static String getFormatDateTime(long date) {
        return getFormatDateTime(date, STANDARD_PATTERN);
    }

    public static String getFormatDateTime(long date, String pattern) {
        return getFormatDateTime(date, pattern, ZoneId.systemDefault());
    }

    public static String getFormatDateTime(long date, ZoneId zoneId) {
        return getFormatDateTime(date, STANDARD_PATTERN, zoneId);
    }

    public static String getFormatDateTime(long date, String pattern, ZoneId zoneId) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), zoneId);
        return getFormatDateTime(time, pattern);
    }

    /**
     * 获取当前格式化的日期
     * <p/>
     * 格式化后的日期：yyyy-MM-dd --> 1970-01-01
     *
     * @return --
     */
    public static String getFormatDate() {
        return getFormatDateTime(DATE_PATTERN);
    }

    public static String getFormatTime(String pattern) {
        return getTimeNow().format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 获取当前时间的毫秒数
     *
     * @return --
     */
    public static long getDateTimeMill() {
        return getDateTimeMill(getDateTimeNow());
    }

    /**
     * 获取某一时间的毫秒数
     *
     * @param dateTime 时间
     * @return --
     */
    public static long getDateTimeMill(LocalDateTime dateTime) {
        return getDateTimeMill(dateTime, getSystemZoneOffset());
    }

    public static long getDateTimeMill(LocalDateTime dateTime, String zoneOffset) {
        return dateTime.toInstant(ZoneOffset.of(zoneOffset)).toEpochMilli();
    }

    /**
     * 转换成{@link LocalDateTime}
     *
     * @param date 时间的毫秒数
     * @return --
     */
    public static LocalDateTime parseToLocalDateTime(long date) {
        return parseToLocalDateTime(date, getSystemZoneOffset());
    }

    /**
     * 转换成{@link LocalDateTime}
     * @param date 时间的毫秒数
     * @param zoneId 时间偏移量
     * @return --
     */
    public static LocalDateTime parseToLocalDateTime(long date, String zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.of(zoneId));
    }

    /**
     * 将一个Date转换成LocalDateTime
     * @param date Date
     * @return --
     */
    public static LocalDateTime parseToLocalDateTime(Date date) {
        return parseToLocalDateTime(date, getSystemZoneOffset());
    }

    /**
     * 将一个带有偏移量Date转换成LocalDateTime
     * 格式:
     * <ul>
     * <li>{@code Z} - for UTC
     * <li>{@code +h}
     * <li>{@code +hh}
     * <li>{@code +hh:mm}
     * <li>{@code -hh:mm}
     * <li>{@code +hhmm}
     * <li>{@code -hhmm}
     * <li>{@code +hh:mm:ss}
     * <li>{@code -hh:mm:ss}
     * <li>{@code +hhmmss}
     * <li>{@code -hhmmss}
     * </ul>
     * @param date 时间
     * @param zoneId 偏移量
     * @return --
     */
    public static LocalDateTime parseToLocalDateTime(Date date, String zoneId) {
        String offsetDate = getOffsetDate(date, ZoneOffset.of(zoneId));
        return parseDateTime(offsetDate);
    }

    /**
     * 获取带时间偏移量的Date的时间字符串
     *
     * @param date   时间
     * @param zoneId 偏移量
     * @return --
     */
    public static String getOffsetDate(Date date, ZoneId zoneId) {
        return getOffsetDate(date, zoneId, STANDARD_PATTERN);
    }


    public static String getOffsetDate(Date date, ZoneId zoneId, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone(zoneId));
        return format.format(date);
    }

    /**
     * 解析一个字符串为LocalDateTime
     * <p/>
     * 该方法只接受格式为：yyyy-MM-dd HH:mm:ss.SSS
     * @param text 字符串
     * @return --
     */
    public static LocalDateTime parseDateTime(String text) {
        return parseDateTime(text, STANDARD_PATTERN);
    }

    /**
     * 解析一个字符串为LocalDateTime
     *
     * @param text    字符串
     * @param pattern 格式
     * @return --
     */
    public static LocalDateTime parseDateTime(String text, String pattern) {
        return LocalDateTime.parse(text, getDateTimePattern(pattern));
    }

    /**
     * 获取本月的最后一天
     * @param localDateTime 日期时间
     * @return 最后一天的日期
     */
    public static int monthLastDay(LocalDateTime localDateTime){
        LocalDate date = localDateTime.toLocalDate();
        return date.lengthOfMonth();
    }

    public static int monthLastDay(Date date){
        LocalDateTime time = parseToLocalDateTime(date);
        return monthLastDay(time);
    }

    public static int monthLastDay(long date){
        LocalDateTime time = parseToLocalDateTime(date);
        return monthLastDay(time);
    }

    /**
     * 获取当前月份所在季度
     * @param localDateTime 时间
     * @return 返回当前的季度数1-4.如果传入的月份有误则返回-1
     */
    public static int monthOfQuarter(LocalDateTime localDateTime){
        int month = localDateTime.getMonth().getValue();
        switch (month){
            case 1: case 2: case 3: return 1;
            case 4: case 5: case 6: return 2;
            case 7: case 8: case 9: return 3;
            case 10: case 11: case 12: return 4;
        }
        return -1;
    }

    public static int monthOfQuarter(Date date){
        LocalDateTime localDateTime = parseToLocalDateTime(date);
        return monthOfQuarter(localDateTime);
    }

    public static int monthOfQuarter(long date){
        LocalDateTime localDateTime = parseToLocalDateTime(date);
        return monthOfQuarter(localDateTime);
    }

    /**
     * 获取今天是星期几
     * @param localDateTime 时间
     * @return 星期枚举
     */
    public static DayOfWeek dayOfWeek(LocalDateTime localDateTime){
        return localDateTime.getDayOfWeek();
    }

    public static DayOfWeek dayOfWeek(Date date){
        LocalDateTime time = parseToLocalDateTime(date);
        return dayOfWeek(time);
    }

    public static DayOfWeek dayOfWeek(long date){
        LocalDateTime time = parseToLocalDateTime(date);
        return dayOfWeek(time);
    }

    /**
     * 判断是否为周末
     * <p/>
     * 注意：此处的周末定义为周六和周日
     * @param localDateTime 时间
     * @return true:周末，false:非周末
     */
    public static boolean isWeekend(LocalDateTime localDateTime){
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        switch (dayOfWeek){
            case SATURDAY:
            case SUNDAY:
                return true;
        }
        return false;
    }

    public static boolean isWeekend(Date date){
        return isWeekend(parseToLocalDateTime(date));
    }

    public static boolean isWeekend(long date){
        return isWeekend(parseToLocalDateTime(date));
    }

    /**
     * 润年判断
     * @param localDateTime 时间
     * @return true：闰年、false：平年
     */
    public static boolean isLeapYear(LocalDateTime localDateTime){
        int year = localDateTime.getYear();
        return leapYearCul(year);
    }

    public static boolean isLeapYear(Date date){
        return isLeapYear(parseToLocalDateTime(date));
    }

    public static boolean isLeapYear(long date){
        return isLeapYear(parseToLocalDateTime(date));
    }

    /**
     * 闰年计算
     * @param year 年份
     * @return --
     */
    private static boolean leapYearCul(int year){
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0 && year % 3200 != 0) || year % 172800 == 0;
    }

    /**
     * 将Date时间类型转换成为匹配格式字符串
     *
     * @param date    时间
     * @param pattern 格式
     * @return result
     */
    private static String getDateFormat(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 返回当前系统时间偏移量
     *
     * @return --
     */
    private static String getSystemZoneOffset() {
        return ZoneOffset.systemDefault().getRules().getOffset(getDateTimeNow()).toString();
    }

    /**
     * 获取本地语言时间日期格式
     * @param pattern 格式
     * @return --
     */
    private static DateTimeFormatter getDateTimePattern(String pattern) {
        return getDateTimePattern(pattern, Locale.getDefault());
    }

    /**
     * 获取指定语言时间日期格式
     * @param pattern 格式
     * @param locale 语言环境
     * @return --
     */
    private static DateTimeFormatter getDateTimePattern(String pattern, Locale locale) {
        return DateTimeFormatter.ofPattern(pattern, locale);
    }

}
