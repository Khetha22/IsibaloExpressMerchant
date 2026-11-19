package za.co.hbtechinc.isibalo.platform

import android.content.Context
import za.co.hbtechinc.isibalo.data.MethodTotal
import za.co.hbtechinc.isibalo.util.PdfReportGenerator
import java.io.File

/**
 * Multiplatform abstraction for PDF generation and document printing across Android, Desktop, and iOS.
 */
interface PlatformPdfEngine {
    fun generateGenericReportPdf(title: String, subtitle: String, rows: List<Pair<String, String>>): File?
    fun generateFinancialStatementPdf(
        totalRevenueCents: Long,
        totalExpensesCents: Long,
        netProfitCents: Long,
        stockValuationCents: Long,
        outstandingTabsCents: Long,
        paymentTotals: List<MethodTotal>
    ): File?

    fun sharePdf(pdfFile: File, title: String = "Isibalo Report")
    fun printPdf(pdfFile: File, jobName: String = "Isibalo Report Print")
}

class AndroidPdfEngine(private val context: Context) : PlatformPdfEngine {
    override fun generateGenericReportPdf(
        title: String,
        subtitle: String,
        rows: List<Pair<String, String>>
    ): File? {
        return PdfReportGenerator.generateGenericReportPdf(context, title, subtitle, rows)
    }

    override fun generateFinancialStatementPdf(
        totalRevenueCents: Long,
        totalExpensesCents: Long,
        netProfitCents: Long,
        stockValuationCents: Long,
        outstandingTabsCents: Long,
        paymentTotals: List<MethodTotal>
    ): File? {
        return PdfReportGenerator.generateFinancialStatementPdf(
            context,
            totalRevenueCents,
            totalExpensesCents,
            netProfitCents,
            stockValuationCents,
            outstandingTabsCents,
            paymentTotals
        )
    }

    override fun sharePdf(pdfFile: File, title: String) {
        PdfReportGenerator.sharePdf(context, pdfFile, title)
    }

    override fun printPdf(pdfFile: File, jobName: String) {
        PdfReportGenerator.printPdf(context, pdfFile, jobName)
    }
}
