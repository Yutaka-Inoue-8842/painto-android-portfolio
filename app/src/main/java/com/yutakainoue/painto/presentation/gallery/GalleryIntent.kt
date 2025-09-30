package com.yutakainoue.painto.presentation.gallery

import com.yutakainoue.painto.core.mvi.UiIntent

/**
 * ギャラリー画面のユーザーアクション（インテント）を定義するsealedクラス
 *
 * MVIアーキテクチャにおけるIntent部分として、
 * ユーザーがギャラリー画面で実行可能な全てのアクションを列挙する。
 * プロジェクトの読み込み、選択、削除、エラーハンドリングなどを含む。
 */
sealed class GalleryIntent : UiIntent {
    /**
     * プロジェクト一覧読み込みアクション
     *
     * ギャラリー画面が表示された時やリフレッシュ時に発行。
     * ローカルデータベースから保存されたプロジェクト一覧を取得し、
     * UIに表示するためのデータをロード。
     */
    data object LoadProjects : GalleryIntent()

    /**
     * プロジェクト削除アクション
     *
     * ユーザーがプロジェクトの削除ボタンをタップした時に発行。
     * 指定されたIDのプロジェクトをローカルデータベースから永続的に削除。
     * 削除後はプロジェクト一覧からも除去される。
     *
     * @param projectId 削除対象プロジェクトの一意識別子
     */
    data class DeleteProject(val projectId: String) : GalleryIntent()

    /**
     * プロジェクト選択アクション
     *
     * ユーザーがギャラリー一覧からプロジェクトをタップした時に発行。
     * 選択されたプロジェクトを現在のアクティブプロジェクトとして設定し、
     * エディター画面への遷移を行う。
     *
     * @param projectId 選択対象プロジェクトの一意識別子
     */
    data class SelectProject(val projectId: String) : GalleryIntent()

    /**
     * エラー表示アクション
     *
     * 処理中にエラーが発生した時にシステムから発行。
     * ユーザーにエラー情報を伝えるためのアラートダイアログを表示。
     *
     * @param message 表示するエラーメッセージ
     */
    data class ShowError(val message: String) : GalleryIntent()

    /**
     * エラーダイアログを閉じるアクション
     *
     * ユーザーがエラーダイアログのOKボタンをタップした時に発行。
     * エラー状態をクリアしてUIを通常状態に戻す。
     */
    data object DismissError : GalleryIntent()

    /**
     * ローディング状態設定アクション
     *
     * 非同期処理の開始/終了時にシステムから発行。
     * UIのローディングインジケーターの表示/非表示を制御。
     *
     * @param loading ローディング状態（true: 表示、false: 非表示）
     */
    data class SetLoading(val loading: Boolean) : GalleryIntent()
}