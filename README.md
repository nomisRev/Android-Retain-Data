# Android Retain Data

As a follow up to <a href="https://github.com/nomisRev/AndroidCleanMVP">AndroidCleanMVP</a>, this demo shows a way to retain data over a config change, or other use cases.

The decision on how you retain data, or when you refresh it depends on the use case. This is merely an example of how it can be done. This demo is not a showcase for a database setup, so I chose to retain the data in memory.

## DataRepository

Like mentioned in <a href="https://github.com/nomisRev/AndroidCleanMVP">AndroidCleanMVP</a> the `Presenter` is not responsible for retaining data, and thus we have a central decoupled `DataRepository` that takes care of retrieving data for us. How `DataRepository` this, is of no interest for our `Presener`.

**side note** This pattern could also be used in a non MVP architecture, as a central source of retrieving/retaining data could be usefull to limit network traffic.

This demo uses rx to solve this problem, but the repo includes a `DataRepository` class (tests included) that works with callbacks instead of rx for those who're not using rx. And both will be explained below

<img src="/datarepo.png" alt="Data repository">

The `DataRepository` works as a middle man between our `Presenter` and the API/Backend. Depending on the usecase it should handle all decisions and return the desired data. Possibly you can decide that data expires 5 minutes after receiving it, in that case if 5 minutes have passed and you query the `DataRepository` for data it will make a network call before providing you with the data.

In our example, the `DataRepository` will only make a network call when it has no previous result to return or if we force him to reload the data from network.

### DataRepository

Time for some code.

```
class DataRepository(private val githubAPI: GithubAPI) {
  
  private var result: Response<List<GithubRepo>>? = null
  private var call: Call<List<GithubRepo>>? = null

  fun getRepos(reload: Boolean, callback: Callback<List<GithubRepo>>) {
    if (reload) {
      result = null
      call = null
    }

    if (result == null || call == null) {
      githubAPI.getReposCall().enqueue(object : Callback<List<GithubRepo>> {
        override fun onResponse(call: Call<List<GithubRepo>>?,
            response: Response<List<GithubRepo>>?) {
          this@DataRepository.call = call
          this@DataRepository.result = response
          callback.onResponse(githubAPI.getReposCall(),result)
        }

        override fun onFailure(call: Call<List<GithubRepo>>?, t: Throwable?) {
          callback.onFailure(call,t)
        }
      })
    } else {
      callback.onResponse(githubAPI.getReposCall(),result)
    }
  }
}
```

### RxDataRepository

```
class RxDataRepository(private val githubAPI: GithubAPI) {

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
```
