package be.vergauwen.simon.androidretaindata.ui

import be.vergauwen.simon.androidretaindata.core.data.RxDataRepository
import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import be.vergauwen.simon.androidretaindata.core.rx.Transformers
import be.vergauwen.simon.himurakotlin.MVPPresenter
import rx.Observable
import rx.Subscriber
import javax.inject.Inject

class MainPresenter @Inject constructor(private val transfomers: Transformers, private val dataRepository: RxDataRepository)
: MVPPresenter<MainContract.View>(), MainContract.Presenter<MainContract.View> {

  override fun loadRepos(reload: Boolean) {
    dataRepository.getData(reload)
        .compose(
            transfomers.applyIOSchedulers<List<GithubRepo>>())
        .flatMap { Observable.from(it) }
        .subscribe(object : Subscriber<GithubRepo>() {
          override fun onNext(t: GithubRepo) {
            getView()?.addRepo(t)
          }

          override fun onError(e: Throwable) {
            getView()?.showError(e)
          }

          override fun onCompleted() {
          }
        })
  }
}