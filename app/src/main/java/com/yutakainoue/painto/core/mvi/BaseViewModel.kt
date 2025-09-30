package com.yutakainoue.painto.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * すべてのViewModelのベースクラス
 *
 * Model-View-Intent (MVI) アーキテクチャパターンを実装するための基底クラス。
 * 状態管理とインテント処理の共通機能を提供する。
 *
 * @param S UIの状態を表す型（UiStateを実装する必要がある）
 * @param I ユーザーアクションを表す型（UiIntentを実装する必要がある）
 */
abstract class BaseViewModel<S : UiState, I : UiIntent> : ViewModel() {

    // 内部状態（プライベート）
    private val _uiState = MutableStateFlow(createInitialState())

    // 外部から観測可能な状態（読み取り専用）
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /**
     * 初期状態を作成する
     *
     * サブクラスで実装必須。ViewModelが作成された時の初期状態を返す。
     *
     * @return 初期状態のインスタンス
     */
    protected abstract fun createInitialState(): S

    /**
     * インテント（ユーザーアクション）を処理する
     *
     * サブクラスで実装必須。受け取ったインテントに応じて状態を更新する。
     *
     * @param intent 処理するインテント
     */
    protected abstract fun handleIntent(intent: I)

    /**
     * インテントを送信する
     *
     * UIから呼び出される公開メソッド。受け取ったインテントを処理に渡す。
     *
     * @param intent 送信するインテント
     */
    fun sendIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * 状態を完全に置き換える
     *
     * 新しい状態インスタンスで現在の状態を置き換える。
     *
     * @param newState 新しい状態
     */
    protected fun setState(newState: S) {
        _uiState.value = newState
    }

    /**
     * 状態を部分的に更新する
     *
     * 現在の状態を元に一部のプロパティを変更した新しい状態を作成する。
     * data classのcopyメソッドを使用して効率的に更新できる。
     *
     * @param transform 状態を変更する関数
     */
    protected fun updateState(transform: S.() -> S) {
        setState(uiState.value.transform())
    }
}

/**
 * UI状態を表すマーカーインターフェース
 *
 * すべてのUI状態クラスが実装すべきインターフェース。
 * 型安全性を保証するために使用される。
 */
interface UiState

/**
 * UIインテント（ユーザーアクション）を表すマーカーインターフェース
 *
 * すべてのユーザーアクションクラスが実装すべきインターフェース。
 * 型安全性を保証するために使用される。
 */
interface UiIntent