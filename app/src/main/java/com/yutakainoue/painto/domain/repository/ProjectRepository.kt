package com.yutakainoue.painto.domain.repository

import com.yutakainoue.painto.data.model.PaintingProject
import kotlinx.coroutines.flow.StateFlow

/**
 * プロジェクトの状態を管理するリポジトリインターフェース
 *
 * Clean Architectureのドメイン層で定義され、データ層で実装される。
 * MVIアーキテクチャにおけるシェアドステート管理の中心となる機能。
 * StateFlowを使用したリアクティブな状態管理で、複数のViewModel間でプロジェクト情報を共有。
 */
interface ProjectRepository {
    /**
     * 現在のプロジェクト状態を表すStateFlow
     *
     * 読み取り専用のリアクティブストリーム。
     * 複数のViewModelがこのストリームを監視し、プロジェクトの変更をリアルタイムで受け取る。
     * nullの場合はプロジェクトが未選択/未作成状態を表す。
     */
    val currentProject: StateFlow<PaintingProject?>

    /**
     * 新しいプロジェクトを設定する
     *
     * ホーム画面で写真を選択した時に呼び出される。
     * 新しいプロジェクトでStateFlowを更新し、複数のViewModelに状態変更を通知。
     *
     * @param project 設定するプロジェクトオブジェクト
     */
    fun setProject(project: PaintingProject)

    /**
     * 現在のプロジェクトをクリアする
     *
     * アプリ終了時や新しいプロジェクト開始前に呼び出される。
     * StateFlowをnullに設定して、全てのViewModelにクリア状態を通知。
     * メモリリーク防止と状態の一貫性維持のために重要。
     */
    fun clearProject()

    /**
     * 既存プロジェクトを更新する
     *
     * エディターでの編集操作（フィルター適用、描画、色調補正など）後に呼び出される。
     * 更新されたプロジェクトでStateFlowを更新し、複数のViewModelに変更を通知。
     * setProjectと同等の動作をするが、意味的に「更新」として分離。
     *
     * @param project 更新後のプロジェクトオブジェクト
     */
    fun updateProject(project: PaintingProject)

    /**
     * 現在プロジェクトが存在するかどうかをチェックする
     *
     * 画面遵移や処理分岐の判定に使用。
     * プロジェクトがない状態でエディター操作を防止するためのガード条件として活用。
     * StateFlowの値を直接チェックするよりもメソッド化することで可読性向上。
     *
     * @return プロジェクトが存在する場合true、そうでなければfalse
     */
    fun hasProject(): Boolean
}