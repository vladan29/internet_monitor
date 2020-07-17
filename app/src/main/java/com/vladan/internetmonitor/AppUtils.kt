package com.vladan.internetmonitor

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList

/**
 * Created by vladan on 7/17/2020
 */
object AppUtils {

    @JvmStatic
    fun executeCmd(cmd: String): ArrayList<String>? {
        val result: ArrayList<String> = ArrayList<String>()

        try {
            val process: Process = Runtime.getRuntime().exec(cmd)
            val stdInput = BufferedReader(InputStreamReader(process.inputStream))

            var line = stdInput.readLine()
            while (line != null) {
                result.add("\n $line")
                process.waitFor()
                line = stdInput.readLine()
            }

            stdInput.close()
            process.destroy()

            return result
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}