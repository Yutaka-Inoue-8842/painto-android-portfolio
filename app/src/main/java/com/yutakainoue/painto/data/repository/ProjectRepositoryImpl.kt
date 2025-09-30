package com.yutakainoue.painto.data.repository

import com.yutakainoue.painto.data.model.PaintingProject
import com.yutakainoue.painto.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * プロジェクトの状態を管理するリポジトリの実装クラス
 *
 * MVIアーキテクチャにおけるシェアドステート管理を担当。
 * StateFlowを使用してリアクティブな状態管理を実現し、
 * 複数のViewModel間でプロジェクト情報を共有。
 * メモリ内の一時的な状態管理のみで、永続化は別途実装。
 */
class ProjectRepositoryImpl : ProjectRepository {

    // 内部用の更新可能なStateFlow（privateで外部からの直接更新を防止）
    private val _currentProject = MutableStateFlow<PaintingProject?>(null)

    // 外部公開用の読み取り専用StateFlow（カプセル化で状態の一貫性を保証）
    override val currentProject: StateFlow<PaintingProject?> = _currentProject.asStateFlow()

    /**
     * 新しいプロジェクトを設定する処理
     *
     * ホーム画面で写真を選択した時に呼び出される。
     * 現在のプロジェクトを新しいプロジェクトで置き換え、
     * 複数のViewModelに状態変更を通知。
     *
     * @param project 設定するプロジェクトオブジェクト
     */
    override fun setProject(project: PaintingProject) {
        _currentProject.value = project
    }

    /**
     * 現在のプロジェクトをクリアする処理
     *
     * アプリ終了時や新しいプロジェクト開始前に呼び出される。
     * メモリリークを防止し、状態を初期化するための機能。
     * StateFlowによって複数のViewModelにクリア通知が送られる。
     */
    override fun clearProject() {
        _currentProject.value = null
    }

    /**
     * 既存プロジェクトを更新する処理
     *
     * エディターでの編集操作（フィルター適用、描画、色調補正など）後に呼び出される。
     * 新しいプロジェクト状態を反映し、複数のViewModelに変更を通知。
     * setProjectと同等の動作をするが、意味的に「更新」として分離。
     *
     * @param project 更新後のプロジェクトオブジェクト
     */
    override fun updateProject(project: PaintingProject) {
        _currentProject.value = project
    }

    /**
     * 現在プロジェクトが存在するかどうかをチェックする処理
     *
     * 画面遵移や処理分岐の判定に使用。
     * プロジェクトがない状態でエディター操作を防止するためのガード条件として活用。
     *
     * @return プロジェクトが存在する場合true、そうでなければfalse
     */
    override fun hasProject(): Boolean {
        return _currentProject.value != null
    }
}