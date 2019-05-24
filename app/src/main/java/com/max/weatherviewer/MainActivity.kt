package com.max.weatherviewer

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein = Kodein {
        //todo android Kodein
        import(viewModelModule)
    }

    private val state by instance<ViewModel>()
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clicks = findViewById<Button>(R.id.button).clicks().map { UserAction.LoadButtonClicked }
        val userActions: Observable<out UserAction> = Observable.merge(clicks, /*add other*/Observable.never())

        disposable += state(userActions).observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    private fun render(state: State) {
        when (state) {
            is State.Loading -> render(state)
            is State.Preview -> render(state)
            is State.Failure -> render(state)
        }
    }

    private fun render(state: State.Loading) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Preview) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Failure) {
        Toast.makeText(this, state.toString(), Toast.LENGTH_SHORT).show()
    }

}
