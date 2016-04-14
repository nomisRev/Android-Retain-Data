package be.vergauwen.simon.androidretaindata.core.rx

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class RxTransformers : Transformers {
  override fun <T> applyComputationSchedulers(): Observable.Transformer<T, T> =
      Observable.Transformer<T, T> {
        it.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
      }

  override fun <T> applyIOSchedulers(): Observable.Transformer<T, T> =
      Observable.Transformer<T, T> {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
      }
}