package com.yutakainoue.painto.presentation.home

import android.net.Uri
import com.yutakainoue.painto.core.mvi.UiIntent

/**
 * ホーム画面のユーザーアクション（インテント）を定義するsealedクラス
 *
 * MVIアーキテクチャにおけるIntent部分として、
 * ユーザーがホーム画面で実行可能な全てのアクションを列挙する。
 * 型安全性を保証し、処理の漏れを防ぐためにsealedクラスを使用。
 */
sealed class HomeIntent : UiIntent {
    /**
     * フォトピッカーを開くアクション
     *
     * ユーザーが「写真を選択」ボタンをタップした時に発行される。
     * システムのフォトピッカーUIを表示するためのトリガー。
     */
    data object OpenPhotoPicker : HomeIntent()

    /**
     * フォトピッカーを閉じるアクション
     *
     * ユーザーがフォトピッカーをキャンセルまたはスワイプで閉じた時に発行される。
     * フォトピッカーの状態をリセットするために使用。
     */
    data object ClosePhotoPicker : HomeIntent()

    /**
     * 写真を読み込むアクション
     *
     * ユーザーがフォトピッカーから写真を選択した時に発行される。
     * 選択された写真のURIを受け取り、プロジェクトとして読み込む処理を開始。
     *
     * @param uri 選択された写真のURI
     */
    data class LoadPhoto(val uri: Uri) : HomeIntent()

    /**
     * エラーメッセージを表示するアクション
     *
     * 処理中にエラーが発生した時に発行される。
     * エラーダイアログを表示するために使用。
     *
     * @param message 表示するエラーメッセージ
     */
    data class ShowError(val message: String) : HomeIntent()

    /**
     * エラーダイアログを閉じるアクション
     *
     * ユーザーがエラーダイアログのOKボタンをタップした時に発行される。
     * エラー状態をクリアしてUIを通常状態に戻す。
     */
    data object DismissError : HomeIntent()

    /**
     * ローディング状態を設定するアクション
     *
     * 非同期処理の開始/終了時に発行される。
     * UIのローディングインジケーターの表示/非表示を制御。
     *
     * @param loading ローディング状態（true: 表示、false: 非表示）
     */
    data class SetLoading(val loading: Boolean) : HomeIntent()
}