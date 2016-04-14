package be.vergauwen.simon.androidretaindata.core.data

import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import be.vergauwen.simon.androidretaindata.core.service.GithubAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataRepositoryCall(private val githubAPI: GithubAPI) {


  var result: Response<List<GithubRepo>>? = null
  var call: Call<List<GithubRepo>>? = null

  fun getRepos(reload: Boolean, callback: Callback<List<GithubRepo>>) {
    if (reload) {
      result = null
      call = null
    }

    if (result == null || call == null) {
      githubAPI.getReposCall().enqueue(object : Callback<List<GithubRepo>> {
        override fun onResponse(call: Call<List<GithubRepo>>?,
            response: Response<List<GithubRepo>>?) {
          this@DataRepositoryCall.call = call
          this@DataRepositoryCall.result = response
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