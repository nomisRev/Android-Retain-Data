package be.vergauwen.simon.androidretaindata.core.di

import be.vergauwen.simon.androidretaindata.core.data.DataRepository
import be.vergauwen.simon.androidretaindata.core.rx.Transformers
import dagger.Component

@ApplicationScope
@Component(modules = arrayOf(ApplicationModule::class,ServiceModule::class))
interface ApplicationComponent {
  val  transfomers : Transformers
  val dataRepository : DataRepository
}