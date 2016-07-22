package be.vergauwen.simon.androidretaindata.ui

import be.vergauwen.simon.androidretaindata.core.data.RxDataRepository
import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import be.vergauwen.simon.androidretaindata.core.rx.Transformers
import be.vergauwen.simon.himurakotlin.*
import rx.Observable
import rx.Subscriber
import javax.inject.Inject

/**
 * Created by Simon Vergauwen. github.com/nomisRev
 */

class MainPresenter @Inject constructor(private val transfomers: Transformers, private val dataRepository: RxDataRepository) : MVPRxPresenter<MainContract.View>(), MainContract.Presenter<MainContract.View> {

    override fun loadRepos(reload: Boolean) {
        add(dataRepository.getData(reload)
                .compose(transfomers.applyIOSchedulers<List<GithubRepo>>())
                .flatMap { Observable.from(it) },
                { error -> view?.showError(error) }, { githubRepo -> view?.addRepo(githubRepo) })
    }
}