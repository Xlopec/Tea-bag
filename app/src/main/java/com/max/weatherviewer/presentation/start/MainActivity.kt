package com.max.weatherviewer.presentation.start

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import com.max.weatherviewer.R
import com.max.weatherviewer.api.location.PermissionPublisher
import com.max.weatherviewer.api.location.PermissionResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

class MainActivity : AppCompatActivity(), KodeinAware {

    private val parentKodein by closestKodein()

    override val kodein: Kodein by retainedKodein {

        extend(parentKodein, allowOverride = true)
        import(weatherModule)

        bind<Activity>(tag = Activity::class) with provider { this@MainActivity }
    }

    private val state by instance<WeatherModel>()
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clicks = findViewById<Button>(R.id.button).clicks().map { Message.LoadButtonClicked }
        val userMessages: Observable<out Message> = Observable.merge(clicks, /*add other*/Observable.never())

        disposable += state(userMessages).observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        direct.instance<PermissionPublisher>().accept(PermissionResult(requestCode, permissions, grantResults))
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun render(state: State) {
        System.out.println(state)
        findViewById<TextView>(R.id.textView).text = state.toString()
    }

    /*private fun render(state: State.Loading) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Preview) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Failure) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }*/

}
