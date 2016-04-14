# Android Retain Data

As a follow up to <a href="https://github.com/nomisRev/AndroidCleanMVP">AndroidCleanMVP</a>, this demo shows a way to retain data over a config change, or other use cases.

The decision on how you retain data, or when you refresh it depends on the use case. This is merely an example of how it can be done. This demo is not a showcase for a database setup, so I chose to retain the data in memory.

## DataRepository

* Like mentioned in <a href="https://github.com/nomisRev/AndroidCleanMVP">AndroidCleanMVP</a> the `Presenter` is not responsible for retaining data, and thus we have a central decoupled `DataRepository` that takes care of retrieving data for us. How `DataRepository` this, is of no interest for our `Presener`.

 **side note** This pattern could also be used in a non MVP architecture, as a central source of retrieving/retaining data could be usefull to limit network traffic.

* This demo uses rx to solve this problem, but the repo includes a `DataRepository` class (tests included) that works with callbacks instead of rx for those who're not using rx. And both will be explained below

<img src="/datarepo.png" alt="Data repository">

* The `DataRepository` works as a middle man between our `Presenter` and the API/Backend. Depending on the usecase it should handle all decisions and return the desired data. Possibly you can decide that data expires 5 minutes after receiving it, in that case if 5 minutes have passed and you query the `DataRepository` for data it will make a network call before providing you with the data.

* In our example, the `DataRepository` will only make a network call when it has no previous result to return or if we force him to reload the data from network.

### DataRepository

* The implementation is pretty straight forward, but let's break it down anyway
* `DataRepository` has only one method `getData(reload: Boolean, callback: retrofit2.Callback<List<GithubRepo>>)`. `getData()` takes to parameters, a boolean which represents if the data should `reload` from network. And a callback interface on which we call `onSuccess` or `onFailure` to return the result.
* We use the `retrofit2.Callback` to emulate the same behavior as if `Retrofit` was doing the network call everytime you call `getData`
* In case `reload` is true, we reset the previous result.
* In case there is no previous result or if it was reset, we do a network call. If it succeeds, we propagate the result to `callback.onResponse`, if it fails we propagate the result to `callback.onFailure`

```
class DataRepository(private val githubAPI: GithubAPI) {

  private var result: Response<List<GithubRepo>>? = null
  private var call: Call<List<GithubRepo>>? = null

  fun getData(reload: Boolean, callback: Callback<List<GithubRepo>>) {
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
          callback.onResponse(call, result)
        }

        override fun onFailure(call: Call<List<GithubRepo>>?, t: Throwable?) {
          callback.onFailure(call, t)
        }
      })
    } else {
      callback.onResponse(call, result)
    }
  }
}
```

### RxDataRepository

 * You are reading this so you want to use Rx ＼（＾▽＾）／
 * It all begins with `ConnectableObservable`:
 
 > A ConnectableObservable resembles an ordinary Observable, except that it does not begin emitting items when it is subscribed to, but only when its connect() method is called. In this way you can wait for all intended Subscribers to Observable.subscribe() to the Observable before the Observable begins emitting items.

<img src="/ConnectableObservable.png" alt="Connectable Observable" width="640" height="510">

* In above image you can see that when using a `ConnectableObservable` the subscribers receive the same emmited items, and that the `Observable` does not start emitting after until `.connect()` is called.

* Clearing the previous result means clear the `ConnectableObservable` in order to do this we have to `unsubscribe` the subscribers, otherwise these would be subscribed to a now `null` `Observable` which we'd prefer to avoid. We can do this by calling the `connect(Action1<? super Subscription> connection)` method of `ConnectableObservable` and `unsubscribe` the `connection`, (reference: http://reactivex.io/RxJava/javadoc/rx/observables/ConnectableObservable.html)

* So now that we can clear the `Obsersable` we can now force the network call by clearing it, and in case there is no result we'll do the network call. (Same flow as with callbacks)
* In order to cache the result, which is `List<GithubRepo>` we simply get the `Observable` from `Retrofit` and chain it with `first()` and `replay()`.
* `first()` simply returns an Observable that emits only the very first item emitted by the source Observable, or notifies of an NoSuchElementException if the source Observable is empty. Since we know that our network call will result in 1 `List<GithubRepo` this exactly what we need. **In case your network call returns multiple results, don't use this operator**
* `replay()` this is where all the magic happens.

> If you apply the Replay operator to an Observable before you convert it into a connectable Observable, the resulting connectable Observable will always emit the same complete sequence to any future observers, even those observers that subscribe after the connectable Observable has begun to emit items to other subscribed observers.

* So because of our `replay()` operator, every subscriber will receive the same result we received from our network call.
* After we assembeled the `ConnectableObservable` we desire with the operators we want. We can call `connect()` which now triggers the network call.

**IMPORTANT** Since retrofit does not apply `.subscribeOn(Schedulers.io())` on the `Observable` it returns the network call occurs on the main thread by default. So if you not apply it before calling `connect()` the network call will occur on the main thread resulting in a `NetworkOnMainThreadException`.

```
class RxDataRepository(private val githubAPI: GithubAPI) {

  private var request: ConnectableObservable<List<GithubRepo>>? = null

  fun getData(reload: Boolean): Observable<List<GithubRepo>> {
    if (reload) {
      clearData()
    }

    if (request == null) {
      request = githubAPI.getRepos().subscribeOn(Schedulers.io()).first().replay()
      request!!.connect() //start doing the background call
    }

    return request!!
  }

  private fun clearData() {
    request?.connect { it.unsubscribe() }
    request = null
  }
}
```
