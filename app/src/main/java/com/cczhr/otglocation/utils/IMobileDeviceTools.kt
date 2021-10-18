package com.cczhr.otglocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * @author cczhr
 * @description  库指令
 * @since 2021/2/22
 */
class IMobileDeviceTools {
    val logStr = MutableLiveData<String>()
    var fixedThreadPool: ExecutorService = Executors.newFixedThreadPool(10)
    private val lib = "lib"
    private val bin = "bin"
    private val saveFilePath = "/data/local/tmp"
    val systemLibPath = "/system/lib"
    var process: Process? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    var os: DataOutputStream? = null

    @Volatile
    var isKilling = false

    companion object {
        var DEVICE_PATH: String =
            Application.context.getExternalFilesDir(null)!!.absolutePath + File.separator + "drivers"

    }

    fun killUsbmuxd(deviceNode: String = "") {
        if (!isKilling) {
            isKilling = true
            SystemClock.sleep(1500)
            val killSystemMtp =
                if (deviceNode.isNotEmpty()) "kill `lsof  -t $deviceNode`\n" else deviceNode
            val killPort =
                "kill  `netstat -tunlp  | grep 27015|awk '{print $7} '|awk -F '/' '{print $1}'`"
            runCommand("$killSystemMtp.$saveFilePath/usbmuxd -X -v -f\n$killPort")
            SystemClock.sleep(2000)//保证进程杀死 休眠一下
            isKilling = false
        }

    }

    fun String.addLogStr() {
        logStr.value = this
    }

    fun release() {
        try {
            stopUsbmuxd {
                killUsbmuxd()
                fixedThreadPool.shutdownNow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopUsbmuxd(disconnect: () -> Unit) {
        fixedThreadPool.execute {
            try {
                //killUsbmuxd()
                successResult?.close()
                errorResult?.close()
                os?.close()
                process?.errorStream?.close()
                process?.inputStream?.close()
                process?.outputStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Application.context.runMainThread {
                disconnect.invoke()
            }

        }

    }


    fun mountImage(
        version: String,
        driver1Path: String,
        driver2Path: String,
        developerImg: (status: Boolean) -> Unit
    ) {
        runCommand(
            "mkdir -p /sdcard/lockdown/drivers/$version\n" +
                    "mv -f $driver1Path  /sdcard/lockdown/drivers/$version\n" +
                    "mv -f $driver2Path  /sdcard/lockdown/drivers/$version\n" +
                    ".${saveFilePath}/ideviceimagemounter /sdcard/lockdown/drivers/$version/DeveloperDiskImage.dmg  /sdcard/lockdown/drivers/$version/DeveloperDiskImage.dmg.signature",
            isFinish = {
                runCommand(".${saveFilePath}/ideviceimagemounter -l", {
                    if (!it.contains("Status", true)) {
                        developerImg.invoke(
                            !it.contains(
                                "ERROR",
                                true
                            ) && !it.contains(
                                "ImageSignature[0]", true
                            ) && !it.contains("No device found", true)
                        )
                    }
                }
                )
            }
        )

    }

    fun startUsbmuxd(
        deviceNode: String,
        fd:Int,
        connect: () -> Unit,
        mag: (msg: String) -> Unit,
        version: (msg: String) -> Unit,
        deviceName: (msg: String) -> Unit,
        developerImg: (status: Boolean) -> Unit
    ) {
        fixedThreadPool.execute {
            try {
                if (isKilling)
                    return@execute
                killUsbmuxd(deviceNode)
                process = Runtime.getRuntime().exec("su", arrayOf("LD_LIBRARY_PATH=$saveFilePath"))
                successResult = BufferedReader(InputStreamReader(process!!.inputStream))
                errorResult = BufferedReader(InputStreamReader(process!!.errorStream))
                os = DataOutputStream(process!!.outputStream)
                os?.write(".$saveFilePath/usbmuxd -v -f -d $fd".toByteArray())
                os?.writeBytes("\n")
                os?.flush()
                os?.close()
                fixedThreadPool.execute {
                    try {
                        var line: String?
                        while (errorResult!!.readLine().also { line = it } != null) {
                            line?.let {
                                Application.context.runMainThread {
                                    mag(it)
                                    if (it.contains(
                                            "Finished preflight on device",
                                            true
                                        ) || it.contains("is_device_connected", true)
                                    ) {
                                        connect.invoke()
                                        runCommand(
                                            ".${saveFilePath}/ideviceinfo -k DeviceName",
                                            { dName ->
                                                deviceName.invoke(dName)

                                                runCommand(
                                                    ".${saveFilePath}/ideviceinfo -k ProductVersion",
                                                    { pVersion ->
                                                        version.invoke(pVersion)

                                                        runCommand(
                                                            ".${saveFilePath}/ideviceimagemounter /sdcard/lockdown/drivers/$pVersion/DeveloperDiskImage.dmg  /sdcard/lockdown/drivers/$pVersion/DeveloperDiskImage.dmg.signature",
                                                            isFinish = {

                                                                runCommand(
                                                                    ".${saveFilePath}/ideviceimagemounter -l",
                                                                    {
                                                                        if (!it.contains(
                                                                                "Status",
                                                                                true
                                                                            )
                                                                        ) {
                                                                            developerImg.invoke(
                                                                                !it.contains(
                                                                                    "ERROR",
                                                                                    true
                                                                                ) && !it.contains(
                                                                                    "ImageSignature[0]",
                                                                                    true
                                                                                ) && !it.contains(
                                                                                    "No device found",
                                                                                    true
                                                                                )
                                                                            )
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        )
                                                    })
                                            })
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }


                }
                fixedThreadPool.execute {
                    try {
                        var line: String?
                        while (successResult!!.readLine().also { line = it } != null) {
                            line?.let {
                                mag("$it\n")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                process!!.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    fun resetLocation(isFinish: (() -> Unit)) {
        runCommand(".${saveFilePath}/idevicesetlocation reset", isFinish = isFinish)
    }


    fun modifyLocation(lat: Double, lon: Double, isFinish: (() -> Unit)) {
        runCommand(".${saveFilePath}/idevicesetlocation -- $lat  $lon", isFinish = isFinish)
    }

    @SuppressLint("SdCardPath")
    fun installLib(context: Context, isSuccess: ((status: Boolean) -> Unit)) {
        val assetManager: AssetManager = context.assets
        val libSavePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$lib"
        val binSavePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$bin"
        val libPermission = StringBuilder()
        assetManager.list(lib)?.forEach {
            val fileName = "${lib}/$it"
            libPermission.append("chmod 644 $saveFilePath/$it\n")
            assetManager.open(fileName).saveFilesDir(libSavePath, it)

        }
        assetManager.list(bin)?.forEach {
            val fileName = "${bin}/$it"
            assetManager.open(fileName).saveFilesDir(binSavePath, it)
        }
        runCommand(
            "mkdir -p /sdcard/lockdown\n" +
                    "mkdir -p /sdcard/lockdown/drivers\n" +
                    "cp -rf $libSavePath/* $saveFilePath\n" +
                    "cp -rf $binSavePath/* $saveFilePath\n" +
                    "$libPermission" +
                    "chmod 777 -R $saveFilePath", isFinish = {
                isSuccess.invoke(checkInstallLib(context))
            })


    }

    fun uninstallLib(context: Context, isFinish: (() -> Unit)) {
        val assetManager: AssetManager = context.getAssets()
        val deleteCommand = StringBuilder()
        assetManager.list(lib)?.forEach {
            deleteCommand.append("rm -f $saveFilePath/$it\n")

        }
        assetManager.list(bin)?.forEach {
            deleteCommand.append("rm -f $saveFilePath/$it\n")
        }
        runCommand(
            "${deleteCommand}rm -f $saveFilePath/usbmuxd.pid\n" +
                    "rm -rf /sdcard/lockdown", isFinish = isFinish
        )
    }

    fun checkInstallLib(context: Context): Boolean {
        val assetManager: AssetManager = context.getAssets()
        assetManager.list(lib)?.forEach {
            if (!File("$saveFilePath/$it").exists())
                return false

        }
        assetManager.list(bin)?.forEach {
            if (!File("$saveFilePath/$it").exists())
                return false

        }
        return true
    }

    fun isRoot(isRoot: (value: Boolean) -> Unit) {
        try {
            val process: Process = Runtime.getRuntime().exec("su")
            process.outputStream.run {
                this.flush()
                this.close()
            }
            val code = process.waitFor()
            isRoot(code == 0)
        } catch (e: Exception) {
            isRoot(false)
        }
    }


    private fun runCommand(
        cmd: String,
        input: ((str: String) -> Unit)? = null,
        error: ((str: String) -> Unit)? = null,
        isFinish: (() -> Unit)? = null
    ) {
        fixedThreadPool.execute {
            try {
                val successResult: BufferedReader
                val errorResult: BufferedReader
                val os: DataOutputStream
                val process: Process =
                    Runtime.getRuntime().exec("su", arrayOf("LD_LIBRARY_PATH=$saveFilePath"))
                successResult = BufferedReader(InputStreamReader(process.inputStream))
                errorResult = BufferedReader(InputStreamReader(process.errorStream))
                os = DataOutputStream(process.outputStream)
                os.write(cmd.toByteArray())
                os.writeBytes("\n")
                os.flush()
                os.writeBytes("exit\n")
                os.flush()
                os.close()
                fixedThreadPool.execute {
                    var line: String?
                    try {
                        while (successResult.readLine().also { line = it } != null) {
                            line?.let {
                                Application.context.runMainThread {
                                    it.addLogStr()
                                    input?.invoke(it)
                                }
                            }

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            successResult.close()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                fixedThreadPool.execute {
                    var line: String?
                    try {
                        while (errorResult.readLine().also { line = it } != null) {
                            line?.let {
                                Application.context.runMainThread {
                                    it.addLogStr()
                                    error?.invoke(it)

                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            errorResult.close()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                process.waitFor()
                Application.context.runMainThread {
                    isFinish?.invoke()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                process?.destroy()
            }

        }
    }
}
