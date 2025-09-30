package com.yutakainoue.painto.presentation.home

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.yutakainoue.painto.core.mvi.BaseViewModel
import com.yutakainoue.painto.data.model.PaintingProject
import com.yutakainoue.painto.domain.repository.PhotoRepository
import com.yutakainoue.painto.domain.repository.ProjectRepository
import com.yutakainoue.painto.domain.usecase.UndoRedoUseCase
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ホーム画面のViewModel
 *
 * MVIアーキテクチャにおけるViewModelとして、ホーム画面のビジネスロジックを担当。
 * 写真選択、プロジェクト作成、エラーハンドリングなどの機能を提供する。
 *
 * 主な責任:
 * - フォトピッカーの状態管理
 * - 選択された写真からのプロジェクト作成
 * - エラー状態の管理
 * - ローディング状態の制御
 *
 * @param photoRepository 写真関連の操作を行うリポジトリ
 * @param projectRepository プロジェクト状態を管理するリポジトリ
 * @param undoRedoUseCase Undo/Redo機能を提供するユースケース
 */
class HomeViewModel(
    private val photoRepository: PhotoRepository,
    private val projectRepository: ProjectRepository,
    private val undoRedoUseCase: UndoRedoUseCase
) : BaseViewModel<HomeState, HomeIntent>() {

    override fun createInitialState(): HomeState {
        return HomeState()
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.OpenPhotoPicker -> openPhotoPicker()
            is HomeIntent.ClosePhotoPicker -> closePhotoPicker()
            is HomeIntent.LoadPhoto -> loadPhoto(intent.uri)
            is HomeIntent.ShowError -> showError(intent.message)
            is HomeIntent.DismissError -> dismissError()
            is HomeIntent.SetLoading -> setLoading(intent.loading)
        }
    }

    private fun openPhotoPicker() {
        updateState { copy(isPhotoPickerOpen = true, errorMessage = null) }
    }

    private fun closePhotoPicker() {
        updateState { copy(isPhotoPickerOpen = false) }
    }

    private fun loadPhoto(uri: Uri) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, isPhotoPickerOpen = false) }
            try {
                val bitmap = photoRepository.loadImageFromUri(uri)
                if (bitmap != null) {
                    val project = PaintingProject(
                        id = UUID.randomUUID().toString(),
                        originalImage = bitmap,
                        editedImage = bitmap.config?.let { bitmap.copy(it, true) }
                    )

                    // プロジェクトをリポジトリに保存
                    projectRepository.setProject(project)

                    // Undo/Redoシステムを初期化
                    undoRedoUseCase.initialize(project)

                    updateState { copy(isLoading = false) }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "写真の読み込みに失敗しました"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = "写真の読み込みに失敗しました: ${e.message}"
                    )
                }
            }
        }
    }

    private fun showError(message: String) {
        updateState { copy(errorMessage = message) }
    }

    private fun dismissError() {
        updateState { copy(errorMessage = null) }
    }

    private fun setLoading(loading: Boolean) {
        updateState { copy(isLoading = loading) }
    }
}