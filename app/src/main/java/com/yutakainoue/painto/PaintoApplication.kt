package com.yutakainoue.painto

import android.app.Application
import com.yutakainoue.painto.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Paintoアプリケーションのメインクラス
 *
 * アプリケーション全体の初期化処理を担当する。
 * 主にKoin（依存性注入フレームワーク）の初期化を行う。
 */
class PaintoApplication : Application() {

    /**
     * アプリケーション作成時に呼ばれる初期化メソッド
     *
     * Koinの依存性注入コンテナを設定し、
     * アプリ全体で使用するモジュールを登録する。
     */
    override fun onCreate() {
        super.onCreate()

        // Koin依存性注入フレームワークを開始
        startKoin {
            // Androidコンテキストを設定
            androidContext(this@PaintoApplication)
            // アプリケーション用のモジュールを読み込み
            modules(appModule)
        }
    }
}