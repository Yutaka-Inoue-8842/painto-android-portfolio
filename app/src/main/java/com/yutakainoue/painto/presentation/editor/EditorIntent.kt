package com.yutakainoue.painto.presentation.editor

import androidx.compose.ui.graphics.Color
import com.yutakainoue.painto.core.mvi.UiIntent
import com.yutakainoue.painto.data.model.BrushType
import com.yutakainoue.painto.data.model.FilterType

/**
 * エディター画面のユーザーアクション（インテント）を定義するsealedクラス
 *
 * MVIアーキテクチャにおけるIntent部分として、
 * ユーザーがエディター画面で実行可能な全てのアクションを列挙する。
 * ペイント操作、画像編集、Undo/Redo、保存操作などを含む。
 */
sealed class EditorIntent : UiIntent {

    // ペイント操作関連のアクション
    /**
     * 描画開始アクション
     *
     * ユーザーが画面にタッチして描画を開始した時に発行。
     * ペイントストロークの開始地点を記録し、描画状態を初期化。
     *
     * @param x 描画開始X座標
     * @param y 描画開始Y座標
     */
    data class StartDrawing(val x: Float, val y: Float) : EditorIntent()

    /**
     * 描画継続アクション
     *
     * ユーザーがタッチしたまま指を動かしている時に発行。
     * ペイントパスに新しい座標を追加して線を繋げる。
     *
     * @param x 描画中のX座標
     * @param y 描画中のY座標
     */
    data class ContinueDrawing(val x: Float, val y: Float) : EditorIntent()

    /**
     * 描画終了アクション
     *
     * ユーザーが指を離して描画を終了した時に発行。
     * ペイントストロークを確定し、Undo/Redoの状態を更新。
     */
    data object EndDrawing : EditorIntent()

    /**
     * ブラシサイズ変更アクション
     *
     * ユーザーがブラシのサイズを調整した時に発行。
     * スライダー操作やサイズセレクターから呂び出される。
     *
     * @param size 新しいブラシサイズ（ピクセル単位）
     */
    data class ChangeBrushSize(val size: Float) : EditorIntent()

    /**
     * ブラシ色変更アクション
     *
     * ユーザーがカラーピッカーから色を選択した時に発行。
     * 今後の描画操作に適用されるブラシ色を更新。
     *
     * @param color 新しいブラシ色
     */
    data class ChangeBrushColor(val color: Color) : EditorIntent()

    /**
     * ブラシタイプ変更アクション
     *
     * ユーザーがブラシツールパレットから異なるブラシタイプを選択した時に発行。
     * ペン、マーカー、ハイライター、消しゴムなどの切り替え。
     *
     * @param type 新しいブラシタイプ
     */
    data class ChangeBrushType(val type: BrushType) : EditorIntent()

    // 画像編集操作関連のアクション
    /**
     * フィルター適用アクション
     *
     * ユーザーがフィルターメニューから特定のフィルターを選択した時に発行。
     * ビンテージ、モノクロ、暖色系などの既定効果を画像に適用。
     *
     * @param filter 適用するフィルターの種類
     */
    data class ApplyFilter(val filter: FilterType) : EditorIntent()

    /**
     * 明度調整アクション
     *
     * ユーザーが明度調整スライダーを操作した時に発行。
     * 画像全体の明るさを増減して露出を調整。
     *
     * @param value 明度調整値（-1.0～1.0、負の値で暗く、正の値で明るく）
     */
    data class AdjustBrightness(val value: Float) : EditorIntent()

    /**
     * コントラスト調整アクション
     *
     * ユーザーがコントラスト調整スライダーを操作した時に発行。
     * 明暗の差を強めたり弱めたりして画像のメリハリを調整。
     *
     * @param value コントラスト調整値（-1.0～1.0、負の値で低コントラスト、正の値で高コントラスト）
     */
    data class AdjustContrast(val value: Float) : EditorIntent()

    /**
     * 彩度調整アクション
     *
     * ユーザーが彩度調整スライダーを操作した時に発行。
     * 色の鮮やかさを変更して、色彩豊かさを調整。
     *
     * @param value 彩度調整値（-1.0～1.0、負の値でモノクロに近づく、正の値で鮮やかに）
     */
    data class AdjustSaturation(val value: Float) : EditorIntent()

    /**
     * 色温度調整アクション
     *
     * ユーザーが色温度調整スライダーを操作した時に発行。
     * 画像全体の色味を暖色系または寒色系に調整。
     *
     * @param value 色温度調整値（-1.0～1.0、負の値で寒色系、正の値で暖色系）
     */
    data class AdjustTemperature(val value: Float) : EditorIntent()

    /**
     * 画像回転アクション
     *
     * ユーザーが回転ボタンをタップした時に発行。
     * 指定された角度で画像を回転させる。一般的に90度単位で使用。
     *
     * @param degrees 回転角度（正の値で時計回り、負の値で反時計回り）
     */
    data class RotateImage(val degrees: Float) : EditorIntent()

    // Undo/Redo操作関連のアクション
    /**
     * 元に戻すアクション
     *
     * ユーザーがUndoボタンをタップした時に発行。
     * 直前の編集操作を取り消して、一つ前の状態に戻る。
     */
    data object Undo : EditorIntent()

    /**
     * やり直しアクション
     *
     * ユーザーがRedoボタンをタップした時に発行。
     * Undoで取り消した操作を再度適用して、編集内容を復元。
     */
    data object Redo : EditorIntent()

    // 保存・共有操作関連のアクション
    /**
     * プロジェクト保存アクション
     *
     * ユーザーが「プロジェクトを保存」ボタンをタップした時に発行。
     * 編集中のプロジェクトをアプリ内のローカルストレージに保存。
     * 後で編集を再開できるようにする。
     */
    data object SaveProject : EditorIntent()

    /**
     * ギャラリー保存アクション
     *
     * ユーザーが「ギャラリーに保存」ボタンをタップした時に発行。
     * 編集後の画像をデバイスのフォトギャラリーにエクスポート。
     * 他のアプリでも閲覧・利用可能にする。
     */
    data object SaveToGallery : EditorIntent()

    /**
     * 画像共有アクション
     *
     * ユーザーが「共有」ボタンをタップした時に発行。
     * 編集後の画像を他のアプリやサービスで共有するためのSytem共有シートを起動。
     * SNSへの投稿、メール送信などが可能。
     */
    data object ShareImage : EditorIntent()

    // UI状態操作関連のアクション
    /**
     * エラー表示アクション
     *
     * 処理中にエラーが発生した時にシステムから発行。
     * ユーザーにエラー情報を伝えるためのアラートダイアログを表示。
     *
     * @param message 表示するエラーメッセージ
     */
    data class ShowError(val message: String) : EditorIntent()

    /**
     * エラーダイアログを閉じるアクション
     *
     * ユーザーがエラーダイアログのOKボタンをタップした時に発行。
     * エラー状態をクリアしてUIを通常状態に戻す。
     */
    data object DismissError : EditorIntent()

    /**
     * 成功ダイアログを閉じるアクション
     *
     * ユーザーが成功ダイアログのOKボタンをタップした時に発行。
     * 成功メッセージをクリアしてUIを通常状態に戻す。
     */
    data object DismissSuccess : EditorIntent()

    /**
     * ローディング状態設定アクション
     *
     * 非同期処理の開始/終了時にシステムから発行。
     * UIのローディングインジケーターの表示/非表示を制御。
     *
     * @param loading ローディング状態（true: 表示、false: 非表示）
     */
    data class SetLoading(val loading: Boolean) : EditorIntent()
}