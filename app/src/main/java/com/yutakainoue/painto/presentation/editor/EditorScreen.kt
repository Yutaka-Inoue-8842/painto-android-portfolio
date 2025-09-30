package com.yutakainoue.painto.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yutakainoue.painto.presentation.canvas.BrushToolPanel
import com.yutakainoue.painto.presentation.canvas.PaintingCanvas
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // エラーダイアログ
    state.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(EditorIntent.DismissError) },
            title = { Text("エラー") },
            text = { Text(error) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(EditorIntent.DismissError) }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top app bar
            TopAppBar(
                title = { Text("編集") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    // Undo/Redo buttons
                    IconButton(
                        onClick = { viewModel.sendIntent(EditorIntent.Undo) },
                        enabled = state.canUndo
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "元に戻す")
                    }
                    IconButton(
                        onClick = { viewModel.sendIntent(EditorIntent.Redo) },
                        enabled = state.canRedo
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "やり直し")
                    }

                    IconButton(onClick = { viewModel.sendIntent(EditorIntent.SaveToGallery) }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                    IconButton(onClick = { viewModel.sendIntent(EditorIntent.ShareImage) }) {
                        Icon(Icons.Default.Share, contentDescription = "共有")
                    }
                }
            )

            // Canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentProject = state.currentProject
                if (currentProject != null) {
                    PaintingCanvas(
                        bitmap = currentProject.editedImage,
                        strokes = currentProject.strokes,
                        currentBrushStyle = state.currentBrushStyle,
                        currentBrushSize = state.currentBrushStyle.strokeWidth,
                        currentPathPoints = state.currentPathPoints,
                        onStartDrawing = { x, y ->
                            viewModel.sendIntent(
                                EditorIntent.StartDrawing(
                                    x,
                                    y
                                )
                            )
                        },
                        onContinueDrawing = { x, y ->
                            viewModel.sendIntent(
                                EditorIntent.ContinueDrawing(
                                    x,
                                    y
                                )
                            )
                        },
                        onEndDrawing = { viewModel.sendIntent(EditorIntent.EndDrawing) },
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                } else {
                    Text(
                        text = "画像が読み込まれていません",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tool panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            ) {
                when (state.currentTool) {
                    Tool.Brush -> {
                        BrushToolPanel(
                            currentBrushStyle = state.currentBrushStyle,
                            onBrushTypeChange = {
                                viewModel.sendIntent(
                                    EditorIntent.ChangeBrushType(
                                        it
                                    )
                                )
                            },
                            onBrushSizeChange = {
                                viewModel.sendIntent(
                                    EditorIntent.ChangeBrushSize(
                                        it
                                    )
                                )
                            },
                            onColorChange = { viewModel.sendIntent(EditorIntent.ChangeBrushColor(it)) }
                        )
                    }

                    Tool.Filter -> {
                        // Filter panel
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "フィルタ",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { viewModel.sendIntent(EditorIntent.ApplyFilter(com.yutakainoue.painto.data.model.FilterType.VINTAGE)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ヴィンテージ")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.sendIntent(EditorIntent.ApplyFilter(com.yutakainoue.painto.data.model.FilterType.MONO)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("モノクロ")
                                }
                            }
                        }
                    }

                    else -> {
                        // Default tool panel
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "ツール",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(onClick = { /* TODO: Select brush tool */ }) {
                                    Icon(Icons.Default.Brush, contentDescription = "ブラシ")
                                }
                                IconButton(onClick = { /* TODO: Select eraser tool */ }) {
                                    Icon(Icons.Default.Delete, contentDescription = "消しゴム")
                                }
                                IconButton(onClick = { /* TODO: Select filter tool */ }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "フィルタ")
                                }
                            }
                        }
                    }
                }
            }

            // ローディングインジケータ
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

        }
    }
}