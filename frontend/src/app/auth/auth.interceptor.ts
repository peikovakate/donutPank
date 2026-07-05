import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthActions } from './auth.actions';
import { selectToken } from './auth.reducer';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const token = store.selectSignal(selectToken)();
  const isApiRequest = req.url.startsWith(environment.apiBaseUrl);

  const authReq =
    token && isApiRequest && !req.headers.has('Authorization')
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((err: unknown) => {
      if (
        err instanceof HttpErrorResponse &&
        err.status === 401 &&
        isApiRequest &&
        !req.url.endsWith('/auth/login')
      ) {
        store.dispatch(AuthActions.logout());
      }
      return throwError(() => err);
    }),
  );
};
