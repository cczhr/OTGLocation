package com.cczhr.otglocation.bean

/**
 * @author cczhr
 * @description
 * @since 2021/6/10 09:33
 */
class DeviceSupportBean : ArrayList<DeviceSupportBeanItem>()

data class DeviceSupportBeanItem(
    val download_url: String,
    val name: String
)

