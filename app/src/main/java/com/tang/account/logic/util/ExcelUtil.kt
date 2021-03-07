package com.tang.account.logic.util

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.tang.account.AccountApplication
import com.tang.account.model.Record
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.lang.Exception
import java.lang.StringBuilder

/*Excel工具单例类*/
object ExcelUtil {

    /*读入的record列表*/
    private val recordList = mutableListOf<Record>()

    /*类型所在excel中列的位置*/
    private var categoryPosition = 0

    /*金额所在excel中列的位置*/
    private var amountPosition = 0

    /*日期所在excel中列的位置*/
    private var datePosition = 0

    /*时间所在excel中列的位置*/
    private var timePosition = 0

    /*支付方式所在excel中列的位置*/
    private var wayPosition = 0

    /*详细信息所在excel中列的位置*/
    private var infoPosition = 0

    /**
     * 读取excel中的记录
     * @param uri excel文件uri
     * @return record列表
     */
    fun readExcel(uri: Uri): List<Record> {
        recordList.clear()
        val contentResolver = AccountApplication.context.contentResolver
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val workBook = XSSFWorkbook(inputStream)
            val sheetCount = workBook.numberOfSheets
            for (s in 0 until sheetCount) { // 读取每一张表中的数据
                val sheet = workBook.getSheetAt(s)
                val rowCount = sheet.physicalNumberOfRows
                val formulaEvaluator = workBook.creationHelper.createFormulaEvaluator()
                val titleRow = sheet.getRow(0)
                val cellsCount = titleRow.physicalNumberOfCells // 列数量
                val str = StringBuilder()
                for (c in 0 until cellsCount) { // 读取表头的内容
                    val value = getCellAsString(titleRow, c, formulaEvaluator)
                    str.append(value).append(" ")
                }
                val titles = str.split(" ")
                for (i in titles.indices) {
                    when (titles[i]) {
                        "金额" -> amountPosition = i
                        "日期" -> datePosition = i
                        "时间" -> timePosition = i
                        "支付方式" -> wayPosition = i
                        "详细" -> infoPosition = i
                        "类型" -> categoryPosition = i
                    }
                }
                for (r in 1 until rowCount) {  // 读取每一行的内容
                    val row = sheet.getRow(r)
                    val re = Record(
                        RecordTransformer.transform(
                            getCellAsString(
                                row,
                                categoryPosition,
                                formulaEvaluator
                            )
                        ),
                        getCellAsString(row, amountPosition, formulaEvaluator).toFloat(),
                        TimeUtil.getTimeStamp(
                            getCellAsString(
                                row,
                                datePosition,
                                formulaEvaluator
                            ) + getCellAsString(row, timePosition, formulaEvaluator)
                        ),
                        RecordTransformer.transform(
                            getCellAsString(
                                row,
                                wayPosition,
                                formulaEvaluator
                            )
                        ),
                        getCellAsString(row, infoPosition, formulaEvaluator)
                    )
                    re.id = recordList.size + 1L // 设置id
                    recordList.add(re)
                }
            }
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            recordList.clear()
        }
        recordList.sortWith(Record.TimeDesComparator())
        return recordList
    }

    /**
     * 获取某行的第某列的值
     * @param row 行数据
     * @param c 列位置
     * @param formulaEvaluator 解析器
     * @return 该位置的参数,String类型
     */
    private fun getCellAsString(row: Row, c: Int, formulaEvaluator: FormulaEvaluator): String {
        var value = ""
        val cell = row.getCell(c)
        val cellValue = formulaEvaluator.evaluate(cell)
        when (cellValue.cellType) {
            Cell.CELL_TYPE_NUMERIC -> { // 为数值类型
                val number = cellValue.numberValue
                val numberAsLong = number.toLong()
                if (number.compareTo(numberAsLong) != 0) { // 判断是否缺失小数位
                    value += number
                } else {
                    value += numberAsLong
                }
            }
            else -> value += cellValue.stringValue  // 字符串类型
        }
        return value
    }

    /**
     * 将记录写到excel中
     * @param name excel文件名
     * @param list 导出的record数据列表
     * @return 导出成功返回true,失败返回false
     */
    fun writeExcel(name: String, list: List<Record>): Boolean {
        val expensesList =
            list.filter { RecordTransformer.categoryExpenseList.contains(it.getCategory()) }
        val incomeList =
            list.filter { RecordTransformer.categoryIncomeList.contains(it.getCategory()) }
        val workBook = XSSFWorkbook()
        val expensesSheet = workBook.createSheet("支出")
        val incomeSheet = workBook.createSheet("收入")
        var row = expensesSheet.createRow(0)
        row.createCell(0).setCellValue("金额")
        row.createCell(1).setCellValue("日期")
        row.createCell(2).setCellValue("时间")
        row.createCell(3).setCellValue("支付方式")
        row.createCell(4).setCellValue("详细")
        row.createCell(5).setCellValue("类型") // 创建表头
        for (i in expensesList.indices) { // 写入数据到表中
            row = expensesSheet.createRow(i + 1)
            row.createCell(0).setCellValue(expensesList[i].getAmount().toString())
            row.createCell(1).setCellValue(TimeUtil.stampToDateForExcel(expensesList[i].getTime()))
            row.createCell(2).setCellValue(TimeUtil.stampToTimeForExcel(expensesList[i].getTime()))
            row.createCell(3).setCellValue(RecordTransformer.transform(expensesList[i].getWay()))
            row.createCell(4).setCellValue(expensesList[i].getInfo())
            row.createCell(5)
                .setCellValue(RecordTransformer.transform(expensesList[i].getCategory()))
        }
        row = incomeSheet.createRow(0)
        row.createCell(0).setCellValue("金额")
        row.createCell(1).setCellValue("日期")
        row.createCell(2).setCellValue("时间")
        row.createCell(3).setCellValue("支付方式")
        row.createCell(4).setCellValue("详细")
        row.createCell(5).setCellValue("类型") // 创建表头
        for (i in incomeList.indices) { // 写入数据到表中
            row = incomeSheet.createRow(i + 1)
            row.createCell(0).setCellValue(incomeList[i].getAmount().toString())
            row.createCell(1).setCellValue(TimeUtil.stampToDateForExcel(incomeList[i].getTime()))
            row.createCell(2).setCellValue(TimeUtil.stampToTimeForExcel(incomeList[i].getTime()))
            row.createCell(3).setCellValue(RecordTransformer.transform(incomeList[i].getWay()))
            row.createCell(4).setCellValue(incomeList[i].getInfo())
            row.createCell(5)
                .setCellValue(RecordTransformer.transform(incomeList[i].getCategory()))
        }
        try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            ) // 在Download目录下新建文件
            val uri = AccountApplication.context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
            if (uri != null) {
                val outputStream = AccountApplication.context.contentResolver.openOutputStream(uri)
                if (outputStream != null) { //写入文件
                    workBook.write(outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}