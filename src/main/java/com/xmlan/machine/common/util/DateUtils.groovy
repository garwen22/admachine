package com.xmlan.machine.common.util

import org.apache.commons.lang3.time.DateFormatUtils

/**
 * 日期工具类
 * <p>
 * Created by Ayakura Yuki on 2017/4/24.
 */
final class DateUtils {
    /**
     * 获取当前日期：返回格式为 yyyy-MM-dd 的日期
     *
     * @return 格式化后的时间，如 2017-04-24
     */
    static String getDate() {
        return getDate('yyyy-MM-dd')
    }

    /**
     * 获取当前日期：获取指定格式的日期
     *
     * @param pattern 日期格式，允许为 yyyy-MM-dd 或 HH:mm:ss 或 E 等等
     * @return 格式化后的时间，如 2017-04-24、14:33:32、星期一
     */
    static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern)
    }

    /**
     * 获取当前时间：返回格式为 HH:mm:ss 的日期
     *
     * @return 格式化后的时间，如 14:33:32
     */
    static String getTime() {
        return FormatDate(new Date(), 'HH:mm:ss')
    }

    /**
     * 获取当前日期时间：返回格式为 yyyy-MM-dd HH:mm:ss 的日期
     *
     * @return 格式化后的时间，如 2017-04-24 14:33:32
     */
    static String getDateTime() {
        return FormatDate(new Date(), 'yyyy-MM-dd HH:mm:ss')
    }

    /**
     * 获取当前年份
     *
     * @return 如 2017
     */
    static String getYear() {
        return FormatDate(new Date(), 'yyyy')
    }

    /**
     * 获取当前月份
     *
     * @return 如 04
     */
    static String getMonth() {
        return FormatDate(new Date(), 'MM')
    }

    /**
     * 获取当前日
     *
     * @return 如 24
     */
    static String getDay() {
        return FormatDate(new Date(), 'dd')
    }

    /**
     * 获取当前星期
     *
     * @return 如 星期一
     */
    static String getWeek() {
        return FormatDate(new Date(), 'E')
    }

    /**
     * 格式化日期：获取指定或默认格式的时间
     *
     * @param date 原始的时间
     * @param pattern 日期格式，允许为空，或 yyyy-MM-dd 或 HH:mm:ss 或 E。默认格式为 yyyy-MM-dd。
     * @return 格式化后的时间，如 2017-04-24、14:33:32、星期一
     */
    private static String FormatDate(Date date, Object... pattern) {
        if (pattern != null && pattern.length > 0) {
            return DateFormatUtils.format(date, pattern[0].toString())
        } else {
            return DateFormatUtils.format(date, 'yyyy-MM-dd')
        }

    }

    /**
     * 格式化日期时间，格式为 yyyy-MM-dd HH:mm:ss
     *
     * @param date 原始的日期
     * @return 格式化后的时间，如2017-04-24 14:33:32
     */
    static String FormatDateTime(Date date) {
        return FormatDate(date, 'yyyy-MM-dd HH:mm:ss')
    }

    /**
     * 获取过去了多少天
     *
     * @param date Date元数据
     * @return 天
     */
    static long PastDays(Date date) {
        def t = new Date().time - date.time
        return t / (24 * 60 * 60 * 1000)
    }

    /**
     * 获取过去的小时
     *
     * @param date Date元数据
     * @return 小时
     */
    static long PastHour(Date date) {
        def t = new Date().time - date.time
        return t / (60 * 60 * 1000)
    }

    /**
     * 获取过去的分钟
     *
     * @param date Date元数据
     * @return 分钟
     */
    static long PastMinutes(Date date) {
        def t = new Date().time - date.time
        return t / (60 * 1000)
    }

}
