import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType, rootEffectsInit } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, exhaustMap, filter, map, switchMap, tap } from 'rxjs/operators';
import { AuthActions } from './auth.actions';
import { AuthService } from './auth.service';
import { clearToken, readToken, saveToken } from './token-storage';

function toErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    return err.error?.detail ?? err.error?.title ?? `Login failed (${err.status})`;
  }
  return 'Login failed';
}

export const login$ = createEffect(
  (actions$ = inject(Actions), auth = inject(AuthService)) =>
    actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({ username, password }) =>
        auth.login(username, password).pipe(
          switchMap(({ token }) =>
            auth.me(token).pipe(map((user) => AuthActions.loginSuccess({ token, user }))),
          ),
          catchError((err: unknown) => of(AuthActions.loginFailure({ error: toErrorMessage(err) }))),
        ),
      ),
    ),
  { functional: true },
);

// Rehydration: if a token survived in localStorage, re-validate it against
// /users/me to repopulate the user. A 401 is handled by the interceptor
// (logout); any other failure also logs out rather than keeping a half-open session.
export const init$ = createEffect(
  (actions$ = inject(Actions), auth = inject(AuthService)) =>
    actions$.pipe(
      ofType(rootEffectsInit),
      map(() => readToken()),
      filter((token): token is string => token !== null),
      switchMap((token) =>
        auth.me().pipe(
          map((user) => AuthActions.sessionRestored({ token, user })),
          catchError(() => of(AuthActions.logout())),
        ),
      ),
    ),
  { functional: true },
);

export const persistToken$ = createEffect(
  (actions$ = inject(Actions)) =>
    actions$.pipe(
      ofType(AuthActions.loginSuccess),
      tap(({ token }) => saveToken(token)),
    ),
  { functional: true, dispatch: false },
);

export const clearTokenOnLoginFailure$ = createEffect(
  (actions$ = inject(Actions)) =>
    actions$.pipe(
      ofType(AuthActions.loginFailure),
      tap(() => clearToken()),
    ),
  { functional: true, dispatch: false },
);

export const loginRedirect$ = createEffect(
  (actions$ = inject(Actions), router = inject(Router)) =>
    actions$.pipe(
      ofType(AuthActions.loginSuccess),
      tap(() => router.navigateByUrl('/')),
    ),
  { functional: true, dispatch: false },
);

export const logout$ = createEffect(
  (actions$ = inject(Actions), router = inject(Router)) =>
    actions$.pipe(
      ofType(AuthActions.logout),
      tap(() => {
        clearToken();
        router.navigateByUrl('/login');
      }),
    ),
  { functional: true, dispatch: false },
);
