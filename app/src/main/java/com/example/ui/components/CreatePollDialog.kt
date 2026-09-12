package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun CreatePollDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>, isAnonymous: Boolean) -> Unit
) {
    if (!visible) return

    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var isAnonymous by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF192533))
                .border(1.dp, Color(0x3352B8FF), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Poll",
                        color = TelegramTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TelegramTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Poll question",
                    color = TelegramPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("Ask a question...", color = TelegramTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramPrimary,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = TelegramTextPrimary,
                        unfocusedTextColor = TelegramTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Poll options",
                    color = TelegramPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { options[index] = it },
                            placeholder = { Text("Option ${index + 1}", color = TelegramTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TelegramPrimary,
                                unfocusedBorderColor = Color(0x22FFFFFF),
                                focusedTextColor = TelegramTextPrimary,
                                unfocusedTextColor = TelegramTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (options.size > 2) {
                            IconButton(
                                onClick = { options.removeAt(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Option",
                                    tint = TelegramTextMuted
                                )
                            }
                        }
                    }
                }

                if (options.size < 8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { options.add("") }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(TelegramPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add option",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add an option",
                            color = TelegramPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Anonymous Voting",
                            color = TelegramTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Voters are kept secret",
                            color = TelegramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TelegramPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val validOptions = options.filter { it.isNotBlank() }
                        if (question.isNotBlank() && validOptions.size >= 2) {
                            onCreatePoll(question, validOptions, isAnonymous)
                            onDismiss()
                        }
                    },
                    enabled = question.isNotBlank() && options.filter { it.isNotBlank() }.size >= 2,
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Create Poll",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
