package be.vergauwen.simon.androidretaindata

import android.app.Application
import be.vergauwen.simon.androidretaindata.core.di.ApplicationComponent
import be.vergauwen.simon.androidretaindata.core.di.ApplicationModule
import be.vergauwen.simon.androidretaindata.core.di.DaggerApplicationComponent
import be.vergauwen.simon.androidretaindata.core.di.ServiceModule

class KotlinApplication : Application(){

  val component by lazy { createComponent() }

  fun createComponent() : ApplicationComponent = DaggerApplicationComponent.builder()
      .serviceModule(ServiceModule())
      .applicationModule(ApplicationModule(this))
      .build()
}