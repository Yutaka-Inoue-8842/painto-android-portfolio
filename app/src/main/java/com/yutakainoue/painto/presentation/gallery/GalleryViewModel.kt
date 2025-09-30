package com.yutakainoue.painto.presentation.gallery

import androidx.lifecycle.viewModelScope
import com.yutakainoue.painto.core.mvi.BaseViewModel
import com.yutakainoue.painto.domain.repository.ProjectRepository
import kotlinx.coroutines.launch

/**
 * ギャラリー画面のViewModel
 *
 * MVIアーキテクチャにおけるViewModelとして、ギャラリー画面のビジネスロジックを担当。
 * 保存されたプロジェクトの一覧表示、選択、削除機能を提供する。
 *
 * 主な責任:
 * - ローカルデータベースからのプロジェクト一覧取得
 * - プロジェクトの選択とアクティブプロジェクト設定
 * - プロジェクトの削除処理
 * - エラー状態の管理
 * - ローディング状態の制御
 *
 * @param projectRepository プロジェクト状態を管理するリポジトリ
 */
class GalleryViewModel(
    private val projectRepository: ProjectRepository
) : BaseViewModel<GalleryState, GalleryIntent>() {

    init {
        // ViewModel初期化時にプロジェクト一覧を自動読み込み
        // ギャラリー画面表示時にユーザーがすぐにプロジェクト一覧を確認できるようにする
        sendIntent(GalleryIntent.LoadProjects)
    }

    override fun createInitialState(): GalleryState {
        return GalleryState()
    }

    override fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.LoadProjects -> loadProjects()
            is GalleryIntent.DeleteProject -> deleteProject(intent.projectId)
            is GalleryIntent.SelectProject -> selectProject(intent.projectId)
            is GalleryIntent.ShowError -> showError(intent.message)
            is GalleryIntent.DismissError -> dismissError()
            is GalleryIntent.SetLoading -> setLoading(intent.loading)
        }
    }

    /**
     * プロジェクト一覧読み込み処理
     *
     * ローカルデータベースから保存されたプロジェクト一覧を取得。
     * 将来的にRoomデータベースの実装で永続化されたプロジェクトを取得。
     * エラー発生時は適切なメッセージでユーザーに通知。
     */
    private fun loadProjects() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                // TODO: Room databaseからプロジェクト一覧を読み込み
                // 1. データベースからプロジェクトメタデータを取得
                // 2. サムネイル画像をロード
                // 3. ソート順（作成日時降順など）で並び替え
                // 現在は空のリストを返す（データベース未実装のため）
                updateState {
                    copy(
                        isLoading = false,
                        projects = emptyList()
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = "プロジェクトの読み込みに失敗しました: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * プロジェクト削除処理
     *
     * 指定されたIDのプロジェクトをローカルデータベースから永続的に削除。
     * 削除後はギャラリーの表示一覧からも除去される。
     * 削除操作は復元不可能なので、ユーザー確認後に実行される。
     *
     * @param projectId 削除対象プロジェクトの一意識別子
     */
    private fun deleteProject(projectId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                // TODO: Room databaseからプロジェクトを削除
                // 1. データベースからプロジェクトレコードを削除
                // 2. 関連するファイル（画像、ペイントデータなど）を削除
                // 3. UIのプロジェクト一覧から除去
                val updatedProjects = uiState.value.projects.filter { it.id != projectId }
                updateState {
                    copy(
                        isLoading = false,
                        projects = updatedProjects
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = "プロジェクトの削除に失敗しました: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * プロジェクト選択処理
     *
     * ギャラリーから選択されたプロジェクトを現在のアクティブプロジェクトとして設定。
     * 設定後はエディター画面で編集を続行できる状態になる。
     * プロジェクトが見つからない場合はエラーメッセージを表示。
     *
     * @param projectId 選択対象プロジェクトの一意識別子
     */
    private fun selectProject(projectId: String) {
        viewModelScope.launch {
            try {
                // 現在のプロジェクト一覧から指定されたIDのプロジェクトを検索
                val selectedProject = uiState.value.projects.find { it.id == projectId }
                if (selectedProject != null) {
                    // ProjectRepositoryを通じて選択されたプロジェクトを設定
                    // これにより複数のViewModelに状態変更が通知される
                    projectRepository.setProject(selectedProject)
                } else {
                    updateState { copy(errorMessage = "プロジェクトが見つかりません") }
                }
            } catch (e: Exception) {
                updateState {
                    copy(errorMessage = "プロジェクトの選択に失敗しました: ${e.message}")
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
     * ローディング状態設定処理
     *
     * 非同期処理の開始/終了時に呼び出される。
     * UIのローディングインジケーターの表示/非表示を制御。
     */
    private fun setLoading(loading: Boolean) {
        updateState { copy(isLoading = loading) }
    }
}