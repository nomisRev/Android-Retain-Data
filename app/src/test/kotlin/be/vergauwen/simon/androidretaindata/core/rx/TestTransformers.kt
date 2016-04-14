package be.vergauwen.simon.androidretaindata.core.rx

import rx.Observable
import rx.schedulers.Schedulers

class TestTransformers : Transformers {
  override fun <T> applyComputationSchedulers(): Observable.Transformer<T, T> =
      Observable.Transformer {
        it.subscribeOn(Schedulers.immediate()).observeOn(Schedulers.immediate())
      }

  override fun <T> applyIOSchedulers(): Observable.Transformer<T, T> =
      Observable.Transformer {
        it.subscribeOn(Schedulers.immediate()).observeOn(Schedulers.immediate())
      }
}