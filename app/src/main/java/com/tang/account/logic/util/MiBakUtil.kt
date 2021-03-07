package com.tang.account.logic.util

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import com.tang.account.AccountApplication
import com.tang.account.model.Record
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.json.JSONObject
import java.io.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object MiBakUtil {

    private const val PAYMENT_FLAG = "key_payment_db"
    private const val STAMP_FLAG = "key_stamp"

    /*类型所在json中列的位置*/
    private var categoryPosition = 0

    /*金额所在json中列的位置*/
    private var amountPosition = 0

    /*时间所在json中列的位置*/
    private var timePosition = 0

    /*支付方式所在json中列的位置*/
    private var wayPosition = 0

    /*详细信息所在json中列的位置*/
    private var infoPosition = 0

    /*json中此条数据是否被标记为删除的变量的位置*/
    /*1表示被删除,0表示没有*/
    private var deleteStatusPosition = 0

    /*读入的record列表*/
    private val recordList = mutableListOf<Record>()

    /**
     * 读取MIUI备份文件中的数据
     * @param uri 文件uri
     * @return 读取的List
     */
    fun readMiBak(uri: Uri): List<Record> {
        recordList.clear()
        clearCache()
        val name =
            AccountApplication.context.contentResolver.query(uri, null, null, null, null)?.let {
                it.moveToFirst()
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                it.close()
                displayName
            }
        var result = true // 读入状态
        if (name != null && name.endsWith(".bak")) {
            result = result && loadMiBakToCache(uri)
            result = result && extractorMiBak()
            result = result && deTarTemp()
            result = result && parseData()
        } else {
            result = false
        }
        if (!result) {
            recordList.clear()
        }
        clearCache()
        return recordList
    }

    /**
     * 将bak文件加载到cache中并删除miui文件头
     *
     * @param uri 文件uri
     * @return 成功状态
     */
    private fun loadMiBakToCache(uri: Uri): Boolean {
        try {
            val inputStream = AccountApplication.context.contentResolver.openInputStream(uri)
            val fileName =
                AccountApplication.context.cacheDir.toString() + File.separator + "temp.bak" // 将修改后的文件写入cache
            val outputStream = FileOutputStream(File(fileName))
            var headLength = 0 // 文件头长度
            val androidHeadBytes =  // ANDROID.BACKUP
                byteArrayOf(65, 78, 68, 82, 79, 73, 68, 32, 66, 65, 67, 75, 85, 80)
            inputStream?.use {
                while (true) { // 识别安卓backup头文件位置
                    val tempByte = ByteArray(1)
                    var flag = true
                    for (i in 0..13) {
                        it.read(tempByte)
                        headLength++
                        if (androidHeadBytes[i] != tempByte[0]) {
                            flag = false
                            break
                        }
                    }
                    if (flag) {
                        break
                    }
                }
                outputStream.use { out ->
                    out.write(androidHeadBytes)
                    val data = ByteArray(10240)
                    var count = it.read(data, 0, data.size)
                    while (count != -1) {
                        out.write(data)
                        count = it.read(data, 0, data.size)
                    }
                    out.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 解包bak文件
     */
    private fun extractorMiBak(): Boolean {
        try {
            val path = AccountApplication.context.cacheDir.toString()
            val fileName = "${path + File.separator}temp.bak"
            AndroidBackupUtil.extractAsTar(
                fileName,
                "${path + File.separator}temp.tar",
                System.getenv("ABE_PASSWD")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 清除缓存
     */
    private fun clearCache() {
        val path = AccountApplication.context.cacheDir.toString()
        val fileList: Array<File?>? = File(path).listFiles()
        if (fileList != null) {
            for (f in fileList) {
                if (f != null) {
                    deleteDir(f)
                }
            }
        }
    }

    /**
     * 删除文件
     * @param file 删除的文件
     */
    private fun deleteDir(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            } else {
                val fileList: Array<File?>? = file.listFiles()
                if (fileList != null) {
                    for (f in fileList) {
                        if (f != null) {
                            deleteDir(f)
                        }
                    }
                }
                file.delete()
            }
        }
    }

    /**
     * 解压temp.tar
     */
    private fun deTarTemp(): Boolean {
        try {
            val path = AccountApplication.context.cacheDir.toString()
            val input =
                TarArchiveInputStream(FileInputStream(File("$path${File.separator}temp.tar")))
            var tarArchiveEntry = input.nextTarEntry
            input.use {
                while (tarArchiveEntry != null) {
                    val file = File(path + File.separator + tarArchiveEntry.name)
                    val parentFile = file.parentFile
                    if (!parentFile?.exists()!!) {
                        parentFile.mkdirs()
                    }
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val output = BufferedOutputStream(FileOutputStream(file))
                    val data = ByteArray(10240)
                    var count = input.read(data, 0, data.size)
                    output.use {
                        while (count != -1) {
                            output.write(data, 0, count)
                            count = input.read(data, 0, data.size)
                        }
                    }
                    tarArchiveEntry = input.nextTarEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 从解压后的文件中将数据解析出来
     */
    private fun parseData(): Boolean {
        val dataFile =
            File(
                AccountApplication.context.cacheDir.toString() +
                        File.separator + "apps" +
                        File.separator + "com.miui.personalassistant" +
                        File.separator + "miui_bak" +
                        File.separator + "_tmp_bak"
            )
        val dataStr = StringBuilder()
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(dataFile)))
            reader.use {
                var line: String? = it.readLine()
                while (line != null) {
                    dataStr.append(line)
                    line = it.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        val tempStr = dataStr.split("\"")
        var keyStamp = ""
        for (i in tempStr.indices) {
            if (PAYMENT_FLAG == tempStr[i]) { // 获取key_payment_db数据
                dataStr.clear()
                dataStr.append(tempStr[i + 8])
            }
            if (STAMP_FLAG == tempStr[i]) {
                keyStamp =
                    tempStr[i + 8] + "_db" // 时间戳值+"_db",解密的key
            }
        }
        var i = 0
        while (i < dataStr.length - 1) { // 读取会将换行读成两个字符,在这里修正
            if (dataStr[i] == '\\' && dataStr[i + 1] == 'n') {
                dataStr.deleteCharAt(i)
                dataStr.setCharAt(i, '\n')
            }
            i++
        }
        //上面是从文件中获取待解密数据和key
        val resultData = decryptData(dataStr.toString(), keyStamp)
        if (resultData.isNotEmpty()) {
            try {
                val jsonData = JSONObject(resultData)
                val tables = jsonData.getJSONArray("db_tables")
                var transactions = JSONObject()
                for (j in 0 until tables.length()) {
                    if (tables.getJSONObject(j).getString("table_name") == "transactions") {
                        transactions = tables.getJSONObject(j)
                        break
                    }
                }
                val tableColsName = transactions.getJSONArray("table_cols_name")
                val tableRows = transactions.getJSONArray("table_rows")
                for (j in 0 until tableColsName.length()) {
                    when (tableColsName[j].toString()) {
                        "amount_edit" -> amountPosition = j
                        "transaction_time_edit" -> timePosition = j
                        "methode_code_edit" -> wayPosition = j
                        "comment" -> infoPosition = j
                        "category_edit" -> categoryPosition = j
                        "deleted" -> deleteStatusPosition = j
                    }
                }
                for (j in 0 until tableRows.length()) {
                    val row = tableRows.getJSONArray(j)
                    val isDeleted = row[deleteStatusPosition].toString()
                    if (isDeleted == "0") {
                        val re = Record(
                            RecordTransformer.getCategoryByMIUIId(
                                row[categoryPosition].toString()
                            ),
                            row[amountPosition].toString().toFloat(),
                            row[timePosition].toString().toLong(),
                            RecordTransformer.getWayByMIUIId(row[wayPosition].toString())
                        )
                        if (row[infoPosition].toString() != "null") { // 判空操作
                            re.setInfo(row[infoPosition].toString())
                        }
                        re.id = recordList.size + 1L
                        recordList.add(re)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return false
        }
        return true
    }

    /**
     * 对data进行解密(采用miui_personal_assistant的解密方式)
     * @param data 待解密数据
     * @param key 密钥
     */
    private fun decryptData(data: String, key: String): String {
        try {
            val sAesIv =
                byteArrayOf(17, 19, 33, 35, 49, 51, 65, 67, 81, 83, 97, 102, 103, 104, 113, 114)
            val keyByteArray = key.toByteArray(Charsets.US_ASCII)
            val secretKeySpec = SecretKeySpec(keyByteArray, "AES")
            val cipher = Cipher.getInstance("AES/CTR/NoPadding")
            val ivParameterSpec = IvParameterSpec(sAesIv)
            cipher.init(2, secretKeySpec, ivParameterSpec)
            return String(cipher.doFinal(Base64.decode(data, 0)))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 对data进行加密(采用miui_personal_assistant的加密方式)
     * @param data 待加密数据
     * @param key 密钥
     */
    private fun encryptData(data: String, key: String): String {
        try {
            val sAesIv =
                byteArrayOf(17, 19, 33, 35, 49, 51, 65, 67, 81, 83, 97, 102, 103, 104, 113, 114)
            val keyByteArray = key.toByteArray(Charsets.US_ASCII)
            val secretKeySpec = SecretKeySpec(keyByteArray, "AES")
            val cipher = Cipher.getInstance("AES/CTR/NoPadding")
            val ivParameterSpec = IvParameterSpec(sAesIv)
            cipher.init(1, secretKeySpec, ivParameterSpec)
            return Base64.encodeToString(cipher.doFinal(data.toByteArray()), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
