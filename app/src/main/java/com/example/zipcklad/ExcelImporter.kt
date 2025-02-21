package com.example.zipcklad

import android.content.Context
import org.apache.poi.ss.usermodel.*
import java.io.InputStream

class ExcelImporter(private val context: Context) {
    fun importFromStream(inputStream: InputStream): List<ZIPItemEntity> {
        val items = mutableListOf<ZIPItemEntity>()

        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        for (row in sheet) {
            if (row.rowNum == 0) continue // Пропускаем заголовок

            val quantityCell = row.getCell(2)
            val quantity = parseQuantity(quantityCell)

            items.add(
                ZIPItemEntity(
                    name = getCellValue(row.getCell(0)),
                    partNumber = getCellValue(row.getCell(1)),
                    quantity = quantity,
                    location = getCellValue(row.getCell(3))
                )
            )
        }
        workbook.close()
        return items

    }
    private fun getCellValue(cell: Cell?): String {
        return cell?.toString()?.trim() ?: ""
    }

    private fun parseQuantity(cell: Cell?): Int {
        return when {
            cell == null -> 0
            cell.cellType == CellType.NUMERIC -> cell.numericCellValue.toInt()
            else -> {
                val value = cell.toString()
                    .replace(",", ".")
                    .replace(" ", "")
                    .replace("[^\\d.]".toRegex(), "")

                (value.toDoubleOrNull() ?: 0.0).toInt()
            }
        }
    }
    private fun parseRow(row: Row): ZIPItemEntity? {
        return try {
            ZIPItemEntity(
                name = row.getCell(0).toString().trim().takeIf { it.isNotEmpty() }
                    ?: return null,
                partNumber = row.getCell(1).toString().trim().takeIf { it.isNotEmpty() }
                    ?: return null,
                quantity = row.getCell(2).numericCellValue.toInt(),
                location = row.getCell(3).toString().trim().takeIf { it.isNotEmpty() }
                    ?: "Не указано"
            )
        } catch (e: Exception) {
            null
        }
    }
}
