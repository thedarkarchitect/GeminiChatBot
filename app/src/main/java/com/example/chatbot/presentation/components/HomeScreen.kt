package com.example.chatbot.presentation.components

import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatbot.R
import com.example.chatbot.presentation.ChatUiEvent
import com.example.chatbot.presentation.ChatViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    pickedBitmap: @Composable () -> Bitmap?,
    picker: ActivityResultLauncher<PickVisualMediaRequest>,
    uri : MutableStateFlow<String>
) {
    Scaffold(
        topBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .height(55.dp)
                    .padding(horizontal = 16.dp)
            ){
                Text(
                    modifier = modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.app_name),
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        ChatScreen(
            paddingValues = it,
            pickedBitmap = pickedBitmap.invoke(),
            imagePicker = picker,
            state = uri
        )
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    pickedBitmap: Bitmap?,
    imagePicker: ActivityResultLauncher<PickVisualMediaRequest>,
    state : MutableStateFlow<String>
) {
    val chatViewModel = viewModel<ChatViewModel>()
    val chatState = chatViewModel.state.collectAsState().value

//    val pickedBitmap = getBitmap()/

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.Black),
            reverseLayout = true
        ){
            itemsIndexed(chatState.chatList){ _ , chat ->
                if(chat.isFromUser){
                    UserChatItem(
                        prompt = chat.prompt,
                        bitmap = chat.bitmap
                    )
                } else {
                    ModelChatItem(response = chat.prompt)
                }
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                if (pickedBitmap != null) {
                    Image(
                        modifier = modifier
                            .size(40.dp)
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        bitmap = pickedBitmap.asImageBitmap() ,
                        contentDescription = "picked image",
                        contentScale = ContentScale.Crop
                    )
                }

                Icon(
                    modifier = modifier
                        .size(40.dp)
                        .clickable {
                            imagePicker.launch(
                                PickVisualMediaRequest
                                    .Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    .build()
                            )
                        },
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = "Add Photo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = modifier.width(8.dp))

            TextField(
                modifier = modifier.weight(1f),
                value = chatState.prompt,
                onValueChange = {
                    chatViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                },
                placeholder = {
                    Text(text = "Type a prompt")
                }
            )

            Spacer(modifier = modifier.width(8.dp))

            Icon(
                modifier = modifier
                    .size(40.dp)
                    .clickable {
                        chatViewModel.onEvent(ChatUiEvent.SendPrompt(chatState.prompt, chatState.bitmap))
                        state.update { "" }
                    },
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "send prompt",
                tint = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
private fun ModelChatItem(
    modifier: Modifier = Modifier,
    response: String
) {
    Column(
        modifier = modifier
            .padding(
                end = 100.dp,
                bottom = 22.dp
            )
    ) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(16.dp),
            text = response,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
private fun UserChatItem(
    modifier: Modifier = Modifier,
    prompt: String,
    bitmap: Bitmap?
) {
    Column(
        modifier = modifier
            .padding(
                start = 100.dp,
                bottom = 22.dp
            )
    ) {
        bitmap?.let {
            Image(
                modifier = modifier
                    .fillMaxWidth() //lookout for
                    .height(260.dp)
                    .padding(bottom = 2.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                bitmap = it.asImageBitmap()
            )
        }

        Text(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            text = prompt,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

