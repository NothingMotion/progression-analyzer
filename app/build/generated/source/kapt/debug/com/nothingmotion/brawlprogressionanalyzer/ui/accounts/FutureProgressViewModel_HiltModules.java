package com.nothingmotion.brawlprogressionanalyzer.ui.accounts;

import androidx.lifecycle.ViewModel;

import com.nothingmotion.brawlprogressionanalyzer.ui.future_account.FutureProgressViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap;
import dagger.hilt.codegen.OriginatingElement;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.multibindings.StringKey;
import java.lang.String;

@OriginatingElement(
    topLevelClass = FutureProgressViewModel.class
)
public final class FutureProgressViewModel_HiltModules {
  private FutureProgressViewModel_HiltModules() {
  }

  @Module
  @InstallIn(ViewModelComponent.class)
  public abstract static class BindsModule {
    private BindsModule() {
    }

    @Binds
    @IntoMap
    @StringKey("com.nothingmotion.brawlprogressionanalyzer.ui.accounts.FutureProgressViewModel")
    @HiltViewModelMap
    public abstract ViewModel binds(FutureProgressViewModel vm);
  }

  @Module
  @InstallIn(ActivityRetainedComponent.class)
  public static final class KeyModule {
    private KeyModule() {
    }

    @Provides
    @IntoSet
    @HiltViewModelMap.KeySet
    public static String provide() {
      return "com.nothingmotion.brawlprogressionanalyzer.ui.accounts.FutureProgressViewModel";
    }
  }
}
