package be.vergauwen.simon.androidretaindata.core.data

import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import be.vergauwen.simon.androidretaindata.core.service.GithubAPI
import rx.Observable
import rx.observables.ConnectableObservable
import rx.schedulers.Schedulers

class DataRepository(private val githubAPI: GithubAPI) {

  private var request: ConnectableObservable<List<GithubRepo>>? = null

  fun getRepos(reload: Boolean): Observable<List<GithubRepo>> {
    if (reload) {
      clearData()
    }

    if (request == null) {
      val networkCall = githubAPI.getRepos().subscribeOn(Schedulers.io())
      request = networkCall.first().replay()
      request!!.connect() //start doing the background call
    }

    return request!!
  }

  private fun clearData(){
    request?.connect { it.unsubscribe() }
    request = null
  }
}