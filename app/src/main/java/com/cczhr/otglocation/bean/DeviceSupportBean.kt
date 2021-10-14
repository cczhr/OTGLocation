package com.cczhr.otglocation.bean

/**
 * @author cczhr
 * @description
 * @since 2021/6/10 09:33
 */
class DeviceSupportBean : ArrayList<DeviceSupportBeanItem>()

data class DeviceSupportBeanItem(
    val _links: Links,
    val download_url: String,
    val html_url: String,
    val name: String,
    val path: String,
    val sha: String,
    val size: Any,
    val type: String,
    val url: String
)

data class Links(
    val html: String,
    val self: String
)