package be.vergauwen.simon.androidretaindata.ui

import android.os.Bundle
import android.util.Log
import be.vergauwen.simon.androidretaindata.KotlinApplication
import be.vergauwen.simon.androidretaindata.R
import be.vergauwen.simon.androidretaindata.core.model.GithubRepo
import be.vergauwen.simon.himurakotlin.MVPDaggerActivity

class MainActivity : MVPDaggerActivity<MainContract.View, MainPresenter, MainComponent>(), MainContract.View {

  override fun createComponent(): MainComponent = DaggerMainComponent.builder().applicationComponent(
      (application as KotlinApplication).component).build()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    //Least complex example. SavedInstance == null --> config change
    presenter.loadRepos(savedInstanceState==null)
  }

  override fun addRepo(repo: GithubRepo) {
    Log.v("MainActivity", repo.name)
  }

  override fun showError(t: Throwable) {
    Log.e("MainActivity", t.message)
  }
}