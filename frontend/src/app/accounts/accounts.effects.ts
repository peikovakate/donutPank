import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AccountsActions } from './accounts.actions';
import { AccountsService } from './accounts.service';

function toErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    return err.error?.detail ?? err.error?.title ?? `Failed to load accounts (${err.status})`;
  }
  return 'Failed to load accounts';
}

export const loadAccounts$ = createEffect(
  (actions$ = inject(Actions), accounts = inject(AccountsService)) =>
    actions$.pipe(
      ofType(AccountsActions.loadAccounts),
      switchMap(() =>
        accounts.getAccounts().pipe(
          map((accounts) => AccountsActions.loadAccountsSuccess({ accounts })),
          catchError((err: unknown) =>
            of(AccountsActions.loadAccountsFailure({ error: toErrorMessage(err) })),
          ),
        ),
      ),
    ),
  { functional: true },
);
