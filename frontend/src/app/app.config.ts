import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideRouterStore, routerReducer } from '@ngrx/router-store';
import { provideStore } from '@ngrx/store';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { routes } from './app.routes';
import * as accountsEffects from './accounts/accounts.effects';
import { accountsFeature } from './accounts/accounts.reducer';
import * as authEffects from './auth/auth.effects';
import { authInterceptor } from './auth/auth.interceptor';
import { authFeature } from './auth/auth.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideStore({
      router: routerReducer,
      [authFeature.name]: authFeature.reducer,
      [accountsFeature.name]: accountsFeature.reducer,
    }),
    provideEffects(authEffects, accountsEffects),
    provideRouterStore(),
    provideCharts(withDefaultRegisterables()),
  ],
};
