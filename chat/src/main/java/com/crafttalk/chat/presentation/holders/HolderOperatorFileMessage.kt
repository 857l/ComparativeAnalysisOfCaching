package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.FileMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorFileMessage(
    val view: View,
    private val clickHandler: (fileUrl: String) -> Unit
) : BaseViewHolder<FileMessageItem>(view), View.OnClickListener {
    private val contentContainer: View? = view.findViewById(R.id.content_container)

    private val fileIcon: ImageView? = view.findViewById(R.id.file_icon)
    private val fileName: TextView? = view.findViewById(R.id.file_name)
    private val fileSize: TextView? = view.findViewById(R.id.file_size)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var fileUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        fileUrl?.run(clickHandler)
    }

    override fun bindTo(item: FileMessageItem) {
        fileUrl = item.document.url

        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        // set width item and content
        fileIcon?.apply {
            setFileIcon()
            ChatAttr.getInstance().widthItemOperatorFileIconMessage?.let {
                layoutParams.width = it
            }
        }
        fileName?.setFileName(item.document, false)
        fileSize?.setFileSize(item.document, false)
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }

}