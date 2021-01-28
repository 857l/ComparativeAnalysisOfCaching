package com.crafttalk.chat.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.domain.interactors.AuthInteractor
import com.crafttalk.chat.domain.interactors.ChatMessageInteractor
import com.crafttalk.chat.domain.interactors.CustomizingChatBehaviorInteractor
import com.crafttalk.chat.domain.interactors.FileInteractor
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatParams.timeDelayed
import javax.inject.Inject

class ChatViewModel
@Inject constructor(
    private val authChatInteractor: AuthInteractor,
    private val chatMessageInteractor: ChatMessageInteractor,
    private val fileInteractor: FileInteractor,
    private val customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor,
    private val context: Context
) : BaseViewModel() {

    val messages: LiveData<PagedList<MessageModel>> by lazy {
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel>  = LivePagedListBuilder<Int, MessageModel>(
            chatMessageInteractor.getAllMessages().map<MessageModel> { (messageModelMapper(it, context)) },
            PAGE_SIZE
        )
        pagedListBuilder.build()
    }

    val internetConnectionState: MutableLiveData<InternetConnectionState> = MutableLiveData()
    val displayableUIObject = MutableLiveData(DisplayableUIObject.NOTHING)

    private val internetConnectionListener = object : ChatInternetConnectionListener {
        override fun connect() { internetConnectionState.postValue(InternetConnectionState.HAS_INTERNET) }
        override fun failConnect() { internetConnectionState.postValue(InternetConnectionState.NO_INTERNET) }
        override fun lossConnection() { internetConnectionState.postValue(InternetConnectionState.NO_INTERNET) }
        override fun reconnect() { internetConnectionState.postValue(InternetConnectionState.RECONNECT) }
    }
    private val chatEventListener = object : ChatEventListener {
        override fun operatorStartWriteMessage() { displayableUIObject.postValue(DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE) }
        override fun operatorStopWriteMessage()  { displayableUIObject.postValue(DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE) }
    }

    init {
        customizingChatBehaviorInteractor.setInternetConnectionListener(internetConnectionListener)
        customizingChatBehaviorInteractor.goToChatScreen()

        Handler().postDelayed({
            authChatInteractor.logIn(
                successAuthUi = { displayableUIObject.postValue(DisplayableUIObject.CHAT) },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                firstLogInWithForm = { displayableUIObject.value = DisplayableUIObject.FORM_AUTH },
                chatEventListener = chatEventListener
            )
        }, timeDelayed)
    }

    override fun onCleared() {
        super.onCleared()
        customizingChatBehaviorInteractor.leaveChatScreen()
    }

    fun registration(vararg args: String) {
        Handler().postDelayed({
            authChatInteractor.logIn(
                visitor = Visitor.map(args),
                successAuthUi = { displayableUIObject.postValue(DisplayableUIObject.CHAT) },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                chatEventListener = chatEventListener
            )
        }, timeDelayed)
    }

    fun reload() {
        Handler().postDelayed({
            authChatInteractor.logIn(
                successAuthUi = { displayableUIObject.postValue(DisplayableUIObject.CHAT) },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                chatEventListener = chatEventListener
            )
        }, timeDelayed)
    }

    fun openFile(context: Context, fileUrl: String) {
        val intentView = Intent(Intent.ACTION_VIEW).apply {
            data = fileUrl.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val intentChooser = Intent.createChooser(intentView, context.getString(R.string.string_chooser_open_file_action_view))
        if (intentView.resolveActivity(context.packageManager) != null) {
            context.startActivity(intentChooser)
        }
    }

    fun openImage(activity: Activity, imageUrl: String) {
        ShowImageDialog.Builder(activity)
            .setUrl(imageUrl)
            .setType(TypeFile.IMAGE)
            .show()
    }

    fun openGif(activity: Activity, gifUrl: String) {
        ShowImageDialog.Builder(activity)
            .setUrl(gifUrl)
            .setType(TypeFile.GIF)
            .show()
    }

    fun selectAction(actionId: String) {
        launchIO {
            chatMessageInteractor.selectActionInMessage(
                actionId,
                {},
                {}
            )
        }
    }

    fun updateData(idKey: Long, height: Int, width: Int) {
        launchIO {
            chatMessageInteractor.updateSizeMessage(
                idKey,
                height,
                width,
                {},
                {}
            )
        }
    }

    fun sendMessage(message: String) {
        launchIO {
            chatMessageInteractor.sendMessage(
                message,
                {},
                {
                    handleError(it)
                }
            )
        }
    }

    fun sendFile(file: File) {
        launchIO {
            fileInteractor.uploadFile(file, {}, {})
        }
    }

    fun sendFiles(fileList: List<File>) {
        launchIO {
            fileInteractor.uploadFiles(fileList, {}, {})
        }
    }

    fun sendImage(bitmap: Bitmap) {
        launchIO {
            fileInteractor.uploadImage(bitmap, {}, {})
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }

}