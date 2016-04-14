package be.vergauwen.simon.androidretaindata.core.service

import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import rx.Observable

class MockGithubAPI : GithubAPI {

  var githubRepo = listOf(GithubRepo("test", "www.test.com", "test_desc"))

  override fun getRepos(): Observable<List<GithubRepo>> = Observable.just(githubRepo)
}