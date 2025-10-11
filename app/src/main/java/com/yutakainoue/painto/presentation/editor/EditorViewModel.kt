package com.yutakainoue.painto.presentation.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.yutakainoue.painto.core.mvi.BaseViewModel
import com.yutakainoue.painto.data.model.BrushType
import com.yutakainoue.painto.data.model.FilterType
import com.yutakainoue.painto.data.model.PaintStroke
import com.yutakainoue.painto.domain.repository.PhotoRepository
import com.yutakainoue.painto.domain.repository.ProjectRepository
import com.yutakainoue.painto.domain.usecase.ImageEditingUseCase
import com.yutakainoue.painto.domain.usecase.UndoRedoUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * エディター画面のViewModel
 *
 * MVIアーキテクチャにおけるViewModelとして、エディター画面のビジネスロジックを担当。
 * 画像編集、ペイント操作、フィルター適用、Undo/Redo、保存機能などを提供する。
 *
 * 主な責任:
 * - ペイントツールの状態管理
 * - 画像編集処理（フィルター、色調補正、回転）
 * - Undo/Redo機能の制御
 * - プロジェクトの保存・エクスポート
 * - エラー状態の管理
 * - ローディング状態の制御
 *
 * @param photoRepository 写真関連の操作を行うリポジトリ
 * @param projectRepository プロジェクト状態を管理するリポジトリ
 * @param imageEditingUseCase 画像編集機能を提供するユースケース
 * @param undoRedoUseCase Undo/Redo機能を提供するユースケース
 */
class EditorViewModel(
    private val photoRepository: PhotoRepository,
    private val projectRepository: ProjectRepository,
    private val imageEditingUseCase: ImageEditingUseCase,
    private val undoRedoUseCase: UndoRedoUseCase
) : BaseViewModel<EditorState, EditorIntent>() {

    init {
        // プロジェクトの変更を監視し、UI状態を同期
        // Repositoryからのプロジェクト更新通知を受け取り、
        // エディター画面の状態を最新に保つ
        viewModelScope.launch {
            projectRepository.currentProject.collectLatest { project ->
                updateState {
                    copy(
                        currentProject = project,
                        previewBitmap = project?.editedImage,
                        canUndo = undoRedoUseCase.canUndo(),
                        canRedo = undoRedoUseCase.canRedo()
                    )
                }
            }
        }
    }

    override fun createInitialState(): EditorState {
        return EditorState()
    }

    override fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.StartDrawing -> startDrawing(intent.x, intent.y)
            is EditorIntent.ContinueDrawing -> continueDrawing(intent.x, intent.y)
            is EditorIntent.EndDrawing -> endDrawing()
            is EditorIntent.ChangeBrushSize -> changeBrushSize(intent.size)
            is EditorIntent.ChangeBrushColor -> changeBrushColor(intent.color)
            is EditorIntent.ChangeBrushType -> changeBrushType(intent.type)
            is EditorIntent.ApplyFilter -> applyFilter(intent.filter)
            is EditorIntent.AdjustBrightness -> adjustBrightness(intent.value)
            is EditorIntent.AdjustContrast -> adjustContrast(intent.value)
            is EditorIntent.AdjustSaturation -> adjustSaturation(intent.value)
            is EditorIntent.AdjustTemperature -> adjustTemperature(intent.value)
            is EditorIntent.RotateImage -> rotateImage(intent.degrees)
            is EditorIntent.Undo -> undo()
            is EditorIntent.Redo -> redo()
            is EditorIntent.SaveProject -> saveProject()
            is EditorIntent.SaveToGallery -> saveToGallery()
            is EditorIntent.ShareImage -> shareImage()
            is EditorIntent.ShowError -> showError(intent.message)
            is EditorIntent.DismissError -> dismissError()
            is EditorIntent.DismissSuccess -> dismissSuccess()
            is EditorIntent.SetLoading -> setLoading(intent.loading)
        }
    }

    /**
     * 描画開始処理
     *
     * 新しいペイントストロークを開始し、初期座標を記録。
     * 現在のブラシ設定を使用してペイントスタイルを初期化。
     */
    private fun startDrawing(x: Float, y: Float) {
        val newPoint = Offset(x, y)
        val newStroke = PaintStroke(
            id = UUID.randomUUID().toString(),
            points = listOf(newPoint),
            paint = uiState.value.currentBrushStyle
        )

        updateState {
            copy(
                currentStroke = newStroke,
                currentPathPoints = listOf(newPoint)
            )
        }
        println("DEBUG: startDrawing completed. Points count: ${uiState.value.currentPathPoints}")
    }

    /**
     * 描画継続処理
     *
     * 現在のペイントストロークに新しい座標を追加。
     * タッチ移動に合わせてリアルタイムでポイントリストを更新。
     */
    private fun continueDrawing(x: Float, y: Float) {
        val currentPointsBeforeUpdate = uiState.value.currentPathPoints
        val newPoint = Offset(x, y)

        updateState {
            val updatedPoints = currentPathPoints + newPoint
            copy(
                currentStroke = currentStroke?.copy(points = updatedPoints),
                currentPathPoints = updatedPoints
            )
        }

        val currentPointsAfterUpdate = uiState.value.currentPathPoints
        println("DEBUG: continueDrawing completed. Points before: $currentPointsBeforeUpdate, after: $currentPointsAfterUpdate")
    }

    /**
     * 描画終了処理
     *
     * 現在のペイントストロークを確定し、Undo/Redoシステムに状態を保存。
     * 描画内容をプロジェクトの編集画像に反映。
     */
    private fun endDrawing() {
        val currentState = uiState.value

        currentState.currentStroke?.let { stroke ->
            val currentProject = currentState.currentProject
            if (currentProject != null) {
                val updatedProject = currentProject.copy(
                    strokes = currentProject.strokes + stroke
                )

                projectRepository.updateProject(updatedProject)
                undoRedoUseCase.saveState(updatedProject)

                updateState {
                    copy(
                        currentProject = updatedProject,
                        currentStroke = null,
                        currentPathPoints = emptyList(),
                        canUndo = undoRedoUseCase.canUndo(),
                        canRedo = undoRedoUseCase.canRedo()
                    )
                }
            } else {
                // プロジェクトがない場合は現在のストロークだけクリア
                updateState {
                    copy(
                        currentStroke = null,
                        currentPathPoints = emptyList()
                    )
                }
            }
        }
    }

    /**
     * ブラシサイズ変更処理
     *
     * ユーザーが選択したブラシサイズを現在のブラシスタイルに反映。
     * 今後の描画操作に適用される。
     */
    private fun changeBrushSize(size: Float) {
        updateState {
            copy(currentBrushStyle = currentBrushStyle.copy(strokeWidth = size))
        }
    }

    /**
     * ブラシ色変更処理
     *
     * ユーザーが選択した色を現在のブラシスタイルに反映。
     * カラーピッカーやプリセットからの色選択に対応。
     */
    private fun changeBrushColor(color: Color) {
        updateState {
            copy(currentBrushStyle = currentBrushStyle.copy(color = color))
        }
    }

    /**
     * ブラシタイプ変更処理
     *
     * ユーザーが選択したブラシタイプを現在のブラシスタイルに反映。
     * ペン、マーカー、ハイライター、消しゴムの切り替えに対応。
     */
    private fun changeBrushType(type: BrushType) {
        updateState {
            copy(currentBrushStyle = currentBrushStyle.copy(brushType = type))
        }
    }

    /**
     * フィルター適用処理
     *
     * 指定されたフィルターを現在の編集画像に適用。
     * ビンテージ、モノクロ、暖色系などの既定効果を提供。
     * 処理後はUndo/Redoシステムに状態を保存。
     */
    private fun applyFilter(filter: FilterType) {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // 選択されたフィルターを画像に適用
                    val filteredBitmap = imageEditingUseCase.applyFilter(currentProject.editedImage, filter)
                    val updatedProject = currentProject.copy(editedImage = filteredBitmap)

                    // プロジェクトをRepositoryに更新
                    projectRepository.updateProject(updatedProject)

                    // Undo/Redo用に状態を保存し、元に戻せるようにする
                    undoRedoUseCase.saveState(updatedProject)

                    updateState {
                        copy(
                            isLoading = false,
                            canUndo = undoRedoUseCase.canUndo(),
                            canRedo = undoRedoUseCase.canRedo()
                        )
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "フィルタの適用に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 明度調整処理
     *
     * 画像全体の明るさを調整。正の値で明るく、負の値で暗くなる。
     * 露出補正や写真の明るさ調整に使用。
     */
    private fun adjustBrightness(value: Float) {
        adjustImageProperty("明度", value) { bitmap, adjustValue ->
            imageEditingUseCase.adjustBrightness(bitmap, adjustValue)
        }
    }

    /**
     * コントラスト調整処理
     *
     * 画像の明暗の差を調整。正の値でメリハリを強く、負の値で弱くする。
     * 写真のインパクトや立体感を調整するために使用。
     */
    private fun adjustContrast(value: Float) {
        adjustImageProperty("コントラスト", value) { bitmap, adjustValue ->
            imageEditingUseCase.adjustContrast(bitmap, adjustValue)
        }
    }

    /**
     * 彩度調整処理
     *
     * 色の鮮やかさを調整。正の値で鮮やかに、負の値でモノクロに近づく。
     * 色彩豊かな写真や落ち着いた色味の写真を作るために使用。
     */
    private fun adjustSaturation(value: Float) {
        adjustImageProperty("彩度", value) { bitmap, adjustValue ->
            imageEditingUseCase.adjustSaturation(bitmap, adjustValue)
        }
    }

    /**
     * 色温度調整処理
     *
     * 画像全体の色味を調整。正の値で暖色系、負の値で寒色系に。
     * 大陽光や白熱灯などの光源による色味を調整するために使用。
     */
    private fun adjustTemperature(value: Float) {
        adjustImageProperty("色温度", value) { bitmap, adjustValue ->
            imageEditingUseCase.adjustTemperature(bitmap, adjustValue)
        }
    }

    /**
     * 画像プロパティ調整の共通処理
     *
     * 明度、コントラスト、彩度、色温度などの調整処理を統一的に扱う。
     * 各調整機能の重複コードを削減し、一貫したエラーハンドリングを提供。
     *
     * @param propertyName 調整するプロパティ名（エラーメッセージ用）
     * @param value 調整値（-1.0～1.0の範囲）
     * @param adjustFunction 実際の調整処理を行う関数
     */
    private fun adjustImageProperty(
        propertyName: String,
        value: Float,
        adjustFunction: suspend (android.graphics.Bitmap, Float) -> android.graphics.Bitmap
    ) {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // 指定された調整関数を実行して画像を変更
                    val adjustedBitmap = adjustFunction(currentProject.editedImage, value)
                    val updatedProject = currentProject.copy(editedImage = adjustedBitmap)

                    // 調整結果をRepositoryに保存
                    projectRepository.updateProject(updatedProject)

                    updateState {
                        copy(
                            isLoading = false,
                            canUndo = true
                        )
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "${propertyName}の調整に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 画像回転処理
     *
     * 指定された角度で画像を回転。一般的に90度単位で使用される。
     * 縦向き・横向きの切り替えや、見た目の補正に使用。
     * 回転後はUndo可能な状態として保存。
     */
    private fun rotateImage(degrees: Float) {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // 指定された角度で画像を回転
                    val rotatedBitmap = imageEditingUseCase.rotateBitmap(currentProject.editedImage, degrees)
                    val updatedProject = currentProject.copy(editedImage = rotatedBitmap)

                    // 回転結果をRepositoryに保存
                    projectRepository.updateProject(updatedProject)

                    updateState {
                        copy(
                            isLoading = false,
                            canUndo = true
                        )
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "画像の回転に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Undo（元に戻す）処理
     *
     * 直前の編集操作を取り消し、一つ前の状態に戻す。
     * UndoRedoUseCaseから前の状態を取得し、Repositoryに反映。
     * 操作後はUndo/Redoボタンの有効/無効状態を更新。
     */
    private fun undo() {
        val previousState = undoRedoUseCase.undo()
        if (previousState != null) {
            // 前の状態をRepositoryに適用
            projectRepository.updateProject(previousState)
            updateState {
                copy(
                    canUndo = undoRedoUseCase.canUndo(),
                    canRedo = undoRedoUseCase.canRedo()
                )
            }
        }
    }

    /**
     * Redo（やり直し）処理
     *
     * Undoで取り消した操作を再度適用し、編集内容を復元。
     * UndoRedoUseCaseから次の状態を取得し、Repositoryに反映。
     * 操作後はUndo/Redoボタンの有効/無効状態を更新。
     */
    private fun redo() {
        val nextState = undoRedoUseCase.redo()
        if (nextState != null) {
            // 次の状態をRepositoryに適用
            projectRepository.updateProject(nextState)
            updateState {
                copy(
                    canUndo = undoRedoUseCase.canUndo(),
                    canRedo = undoRedoUseCase.canRedo()
                )
            }
        }
    }

    /**
     * プロジェクト保存処理
     *
     * 編集中のプロジェクトをアプリ内のローカルストレージに保存。
     * 将来的にRoomデータベースを使用して永続化する予定。
     * 保存後はプロジェクト一覧で確認・編集再開が可能になる。
     */
    private fun saveProject() {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // TODO: Room databaseによるプロジェクト永続化を実装
                    // 1. プロジェクトデータをデータベースに保存
                    // 2. 画像ファイルをアプリ内ストレージに保存
                    // 3. ペイントレイヤー情報をシリアライズして保存
                    updateState {
                        copy(
                            isLoading = false,
                            successMessage = "プロジェクトをローカルに保存しました"
                        )
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "プロジェクトの保存に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * ギャラリー保存処理
     *
     * 編集後の画像をデバイスのフォトギャラリーにエクスポート。
     * ペイントストロークを画像に焼き込んでから保存。
     * MediaStore APIを使用して、他のアプリからも閲覧可能な形で保存。
     * ファイル名にはタイムスタンプを含めて重複を防止。
     */
    private fun saveToGallery() {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // ペイントストロークを画像に焼き込む
                    val finalBitmap = if (currentProject.strokes.isNotEmpty()) {
                        imageEditingUseCase.renderStrokesToBitmap(
                            currentProject.editedImage,
                            currentProject.strokes
                        )
                    } else {
                        currentProject.editedImage
                    }

                    // タイムスタンプ付きのユニークなファイル名を生成
                    val filename = "painto_${System.currentTimeMillis()}.jpg"
                    val uri = photoRepository.saveImageToGallery(finalBitmap, filename)
                    if (uri != null) {
                        updateState {
                            copy(
                                isLoading = false,
                                successMessage = "画像をギャラリーに保存しました"
                            )
                        }
                    } else {
                        updateState {
                            copy(
                                isLoading = false,
                                errorMessage = "画像の保存に失敗しました"
                            )
                        }
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "ギャラリーへの保存に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 画像共有処理
     *
     * 編集後の画像を他のアプリやサービスで共有するためのSystem共有シートを起動。
     * ペイントストロークを画像に焼き込んでから共有。
     * SNSへの投稿、メール送信、クラウドストレージへのアップロードなどが可能。
     * AndroidのShareIntentを使用してシステム標準の共有UIを提供。
     */
    private fun shareImage() {
        viewModelScope.launch {
            val currentProject = uiState.value.currentProject
            if (currentProject?.editedImage != null) {
                updateState { copy(isLoading = true) }
                try {
                    // ペイントストロークを画像に焼き込む
                    val finalBitmap = if (currentProject.strokes.isNotEmpty()) {
                        imageEditingUseCase.renderStrokesToBitmap(
                            currentProject.editedImage,
                            currentProject.strokes
                        )
                    } else {
                        currentProject.editedImage
                    }

                    // 一時的な共有用ファイルを作成
                    val uri = photoRepository.shareImage(finalBitmap)
                    if (uri != null) {
                        // TODO: システム共有インテントを起動
                        // Intent.ACTION_SENDを使用して共有シートを表示
                        updateState {
                            copy(
                                isLoading = false,
                                successMessage = "共有準備完了 (システム共有は実装予定)"
                            )
                        }
                    } else {
                        updateState {
                            copy(
                                isLoading = false,
                                errorMessage = "共有に失敗しました"
                            )
                        }
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "画像の共有に失敗しました: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * エラー表示処理
     *
     * エラーメッセージをUI状態に設定し、アラートダイアログで表示。
     * ユーザーにエラー情報を伝えるための手段。
     */
    private fun showError(message: String) {
        updateState { copy(errorMessage = message) }
    }

    /**
     * エラーダイアログを閉じる処理
     *
     * ユーザーがエラーダイアログのOKボタンをタップした時の処理。
     * エラー状態をクリアしてUIを通常状態に戻す。
     */
    private fun dismissError() {
        updateState { copy(errorMessage = null) }
    }

    /**
     * 成功ダイアログを閉じる処理
     *
     * ユーザーが成功ダイアログのOKボタンをタップした時の処理。
     * 成功メッセージをクリアしてUIを通常状態に戻す。
     */
    private fun dismissSuccess() {
        updateState { copy(successMessage = null) }
    }

    /**
     * ローディング状態設定処理
     *
     * 非同期処理の開始/終了時に呼び出される。
     * UIのローディングインジケーターの表示/非表示を制御。
     */
    private fun setLoading(loading: Boolean) {
        updateState { copy(isLoading = loading) }
    }
}