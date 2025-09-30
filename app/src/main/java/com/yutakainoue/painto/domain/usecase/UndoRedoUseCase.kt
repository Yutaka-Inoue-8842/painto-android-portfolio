package com.yutakainoue.painto.domain.usecase

import android.graphics.Bitmap
import com.yutakainoue.painto.data.model.PaintingProject

/**
 * Undo/Redo機能を提供するユースケースクラス
 *
 * Clean Architectureのドメイン層に位置し、編集操作の履歴管理を担当。
 * スタックベースの履歴管理で、メモリ効率とパフォーマンスのバランスを考慮。
 * 最大履歴数を制限してメモリ使用量を制御し、アプリの安定性を保証。
 * プロジェクトの深いコピーを作成して状態の独立性を保証。
 */
class UndoRedoUseCase {

    // Undo用の履歴スタック（過去の状態を時系列順で保存）
    private val undoStack = mutableListOf<PaintingProject>()

    // Redo用の履歴スタック（Undoで戻した状態を保存）
    private val redoStack = mutableListOf<PaintingProject>()

    // メモリ使用量制御のための最大履歴保存数（各スタックあたり）
    private val maxHistorySize = 20

    /**
     * 現在のプロジェクト状態を履歴に保存する
     *
     * 編集操作を実行する前に呼び出し、現在の状態をUndoスタックに保存。
     * プロジェクトの深いコピーを作成して、後の変更が履歴に影響しないようにする。
     * 新しい操作が実行されるとRedoスタックはクリアされる。
     *
     * @param project 保存するプロジェクト状態
     */
    fun saveState(project: PaintingProject) {
        // プロジェクトの深いコピーを作成（参照の独立性を保証）
        val stateCopy = project.copy(
            editedImage = project.editedImage?.copy(project.editedImage.config ?: Bitmap.Config.ARGB_8888, false)
        )

        // Undoスタックに状態を追加
        undoStack.add(stateCopy)

        // メモリ使用量制御: 最大履歴数を超えたら古い状態を削除
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirstOrNull()
        }

        // 新しい操作実行時はRedoスタックをクリア
        redoStack.clear()
    }

    /**
     * 直前の状態に戻すUndo操作を実行する
     *
     * Undoスタックから現在の状態を取り出し、Redoスタックに移動。
     * その後、Undoスタックの最新状態（一つ前の状態）を返す。
     * 最低一つの状態は必ず保持し、空のプロジェクトにはならないようにする。
     *
     * @return 一つ前のプロジェクト状態、戻せない場合はnull
     */
    fun undo(): PaintingProject? {
        // 最低一つの状態は必ず保持する（空のプロジェクト防止）
        if (undoStack.size <= 1) return null

        // 現在の状態をUndoスタックから取り出し
        val currentState = undoStack.removeLastOrNull() ?: return null

        // 取り出した状態をRedoスタックに移動（やり直し用）
        redoStack.add(currentState)

        // Redoスタックのサイズ制限
        if (redoStack.size > maxHistorySize) {
            redoStack.removeFirstOrNull()
        }

        // 新しい現在状態（一つ前の状態）を返す
        return undoStack.lastOrNull()
    }

    /**
     * Undoで戻した操作を再度適用するRedo操作を実行する
     *
     * Redoスタックから状態を取り出し、Undoスタックに戻す。
     * 取り出した状態が新しい現在状態になる。
     * Redoスタックが空の場合は何もしない。
     *
     * @return やり直し後のプロジェクト状態、やり直せない場合はnull
     */
    fun redo(): PaintingProject? {
        // Redoスタックから状態を取り出し
        val redoState = redoStack.removeLastOrNull() ?: return null

        // 取り出した状態をUndoスタックに戻す
        undoStack.add(redoState)

        // やり直した状態を返す
        return redoState
    }

    /**
     * Undo操作が可能かどうかをチェックする
     *
     * Undoスタックに2つ以上の状態がある場合にUndo可能。
     * （1つは現在状態、1つは戻る先の状態が必要）
     * UIのUndoボタンの有効/無効判定に使用。
     *
     * @return Undo可能な場合true、そうでなければfalse
     */
    fun canUndo(): Boolean = undoStack.size > 1

    /**
     * Redo操作が可能かどうかをチェックする
     *
     * Redoスタックに状態がある場合にRedo可能。
     * Undoで戻した状態がある時のみ、やり直しが可能。
     * UIのRedoボタンの有効/無効判定に使用。
     *
     * @return Redo可能な場合true、そうでなければfalse
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**
     * 全ての履歴をクリアする
     *
     * プロジェクト終了時や新しいプロジェクト開始時に呼び出される。
     * メモリリークを防止し、フレッシュな状態から新しい編集を開始できる。
     * Undo/Redoボタンも無効状態になる。
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    /**
     * 新しいプロジェクトでUndo/Redoシステムを初期化する
     *
     * プロジェクト作成時に呼び出され、初期状態を設定。
     * 既存の履歴を全てクリアし、新しいプロジェクトの状態を最初の履歴として保存。
     * この時点ではUndoは不可能（初期状態なので戻る先がない）。
     *
     * @param project 初期化するプロジェクト状態
     */
    fun initialize(project: PaintingProject) {
        // 全ての履歴をクリア
        clear()
        // 初期状態を保存
        saveState(project)
    }
}