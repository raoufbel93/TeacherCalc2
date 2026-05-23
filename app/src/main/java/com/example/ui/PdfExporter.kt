package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    fun exportToPdf(
        context: Context,
        students: List<Student>,
        teacherName: String,
        subject: String,
        institution: String,
        academicYear: String,
        semester: String,
        maxScore: Double,
        reportType: String // "students" or "stats"
    ) {
        if (students.isEmpty()) {
            Toast.makeText(context, "لا توجد علامات لتصديرها!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val pdfDocument = PdfDocument()
            // A4 is 595 x 842 points (72 dpi)
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }

            val thPaint = Paint().apply {
                color = Color.WHITE
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val tdPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            val bgPaint = Paint().apply {
                color = Color.parseColor("#EAEAEA")
                style = Paint.Style.FILL
            }

            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#2980B9")
                style = Paint.Style.FILL
            }

            // Draw Header
            val semesterText = when(semester) {
                "1" -> "الفصل الأول"
                "2" -> "الفصل الثاني"
                "3" -> "الفصل الثالث"
                else -> semester
            }

            val cardTitle = if (reportType == "stats") "تقرير إحصائيات الامتحانات" else "تقرير كشف نقاط الطلاب"
            canvas.drawText(cardTitle, (pageWidth / 2).toFloat(), 50f, titlePaint)

            canvas.drawText("الأستاذ(ة): $teacherName", 545f, 90f, subtitlePaint)
            canvas.drawText("المادة: $subject", 545f, 110f, subtitlePaint)
            canvas.drawText("المؤسسة: $institution", 545f, 130f, subtitlePaint)

            canvas.drawText("السنة الدراسية: $academicYear", 200f, 90f, Paint(subtitlePaint).apply { textAlign = Paint.Align.LEFT })
            canvas.drawText("الفصل: $semesterText", 200f, 110f, Paint(subtitlePaint).apply { textAlign = Paint.Align.LEFT })

            canvas.drawLine(50f, 150f, 545f, 150f, Paint().apply { color = Color.BLACK; strokeWidth = 2f })

            if (reportType == "stats") {
                // Draw Stats section
                val totalStudents = students.size
                val passed = students.count { it.score >= (it.maxScore / 2) }
                val passRate = if (totalStudents > 0) (passed.toDouble() / totalStudents * 100) else 0.0
                val avg = if (totalStudents > 0) students.map { it.score / it.maxScore * maxScore }.average() else 0.0
                val highest = if (totalStudents > 0) students.maxOf { it.score } else 0.0
                val lowest = if (totalStudents > 0) students.minOf { it.score } else 0.0

                canvas.drawText("إحصائيات عامة للفصل الدراسي", 545f, 180f, Paint(titlePaint).apply { textSize = 16f; textAlign = Paint.Align.RIGHT; color = Color.parseColor("#2980B9") })

                var currentY = 210f
                val metrics = listOf(
                    "عدد الطلاب الكلي" to "$totalStudents طالب",
                    "المعدل العام للمجموعة" to String.format(Locale.US, "%.2f / %.1f", avg, maxScore),
                    "أعلى علامة مسجلة" to String.format(Locale.US, "%.2f", highest),
                    "أدنى علامة مسجلة" to String.format(Locale.US, "%.2f", lowest),
                    "عدد الناجحين" to "$passed طالب",
                    "نسبة النجاح العامة" to String.format(Locale.US, "%.2f%%", passRate)
                )

                for ((label, value) in metrics) {
                    canvas.drawRect(50f, currentY - 15f, 545f, currentY + 15f, bgPaint)
                    canvas.drawText(label, 530f, currentY + 5f, Paint(textPaint).apply { typeface = Typeface.DEFAULT_BOLD })
                    canvas.drawText(value, 60f, currentY + 5f, Paint(textPaint).apply { textAlign = Paint.Align.LEFT })
                    currentY += 35f
                }

                // Add grading category breakdown
                currentY += 20f
                canvas.drawText("توزيع التقديرات المستحقة للطلبة", 545f, currentY, Paint(titlePaint).apply { textSize = 15f; textAlign = Paint.Align.RIGHT; color = Color.parseColor("#2980B9") })
                currentY += 30f

                val excellent = students.count { it.score >= it.maxScore * 0.85 }
                val veryGood = students.count { it.score >= it.maxScore * 0.70 && it.score < it.maxScore * 0.85 }
                val good = students.count { it.score >= it.maxScore * 0.50 && it.score < it.maxScore * 0.70 }
                val acceptable = students.count { it.score >= it.maxScore * 0.40 && it.score < it.maxScore * 0.50 }
                val weak = students.count { it.score < it.maxScore * 0.40 }

                val breakdown = listOf(
                    "ممتاز (85% وأعلى)" to excellent,
                    "جيد جداً (من 70% إلى 85%)" to veryGood,
                    "جيد (من 50% إلى 70%)" to good,
                    "مقبول (من 40% إلى 50%)" to acceptable,
                    "ضعيف (أقل من 40%)" to weak
                )

                for ((label, count) in breakdown) {
                    canvas.drawLine(50f, currentY, 545f, currentY, linePaint)
                    canvas.drawText(label, 530f, currentY + 15f, textPaint)
                    canvas.drawText("$count طلاب", 60f, currentY + 15f, Paint(textPaint).apply { textAlign = Paint.Align.LEFT })
                    currentY += 25f
                }

            } else {
                // Table of Students
                canvas.drawRect(50f, 170f, 545f, 195f, headerBgPaint)

                // Headers (Arabic columns from right to left)
                canvas.drawText("الاسم الكامل", 440f, 187f, thPaint)
                canvas.drawText("العلامة المحصلة", 280f, 187f, thPaint)
                canvas.drawText("من أصل", 170f, 187f, thPaint)
                canvas.drawText("الحالة", 85f, 187f, thPaint)

                var currentY = 215f
                for ((index, student) in students.withIndex()) {
                    if (currentY > pageHeight - 50) {
                        break // Single-page simple print
                    }

                    // Background banding
                    if (index % 2 == 1) {
                        canvas.drawRect(50f, currentY - 15f, 545f, currentY + 10f, bgPaint)
                    }

                    val state = if (student.score >= (student.maxScore / 2)) "ناجح" else "راسب"

                    // Draw values
                    canvas.drawText(student.name, 440f, currentY, tdPaint)
                    canvas.drawText(String.format(Locale.US, "%.2f", student.score), 280f, currentY, tdPaint)
                    canvas.drawText(String.format(Locale.US, "%.0f", student.maxScore), 170f, currentY, tdPaint)
                    canvas.drawText(state, 85f, currentY, tdPaint)

                    canvas.drawLine(50f, currentY + 10f, 545f, currentY + 10f, linePaint)
                    currentY += 28f
                }
            }

            pdfDocument.finishPage(page)

            // Save the document to cache directory so we can share it easily
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "Mussahih_${reportType}_${simpleDateFormat.format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            // Trigger standard Share Intent
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التقرير عبر:"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل تصدير الملف: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
