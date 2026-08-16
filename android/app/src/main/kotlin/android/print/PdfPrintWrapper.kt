package android.print

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor

object PdfPrintWrapper {
    fun print(
        adapter: PrintDocumentAdapter,
        printAttributes: PrintAttributes,
        fileDescriptor: ParcelFileDescriptor,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val cancellationSignal = CancellationSignal()
        
        adapter.onStart()
        adapter.onLayout(
            printAttributes,
            printAttributes,
            cancellationSignal,
            object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                    adapter.onWrite(
                        arrayOf(PageRange.ALL_PAGES),
                        fileDescriptor,
                        cancellationSignal,
                        object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<out PageRange>?) {
                                super.onWriteFinished(pages)
                                adapter.onFinish()
                                onComplete(true, null)
                            }

                            override fun onWriteFailed(error: CharSequence?) {
                                super.onWriteFailed(error)
                                adapter.onFinish()
                                onComplete(false, error?.toString() ?: "Write failed")
                            }

                            override fun onWriteCancelled() {
                                super.onWriteCancelled()
                                adapter.onFinish()
                                onComplete(false, "Cancelled")
                            }
                        }
                    )
                }

                override fun onLayoutFailed(error: CharSequence?) {
                    super.onLayoutFailed(error)
                    adapter.onFinish()
                    onComplete(false, "Layout failed: ${error ?: "unknown"}")
                }
            },
            Bundle()
        )
    }
}

