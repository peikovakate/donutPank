import { createFeature, createReducer, on } from '@ngrx/store';
import { AccountsActions } from './accounts.actions';
import { Account } from './accounts.models';

export type AccountsStatus = 'idle' | 'loading' | 'loaded' | 'error';

export interface AccountsState {
  accounts: Account[];
  status: AccountsStatus;
}

export const initialAccountsState: AccountsState = {
  accounts: [],
  status: 'idle',
};

export const accountsFeature = createFeature({
  name: 'accounts',
  reducer: createReducer(
    initialAccountsState,
    on(AccountsActions.loadAccounts, (state): AccountsState => ({ ...state, status: 'loading' })),
    on(
      AccountsActions.loadAccountsSuccess,
      (state, { accounts }): AccountsState => ({ ...state, accounts, status: 'loaded' }),
    ),
    on(
      AccountsActions.loadAccountsFailure,
      (state): AccountsState => ({ ...state, status: 'error' }),
    ),
  ),
});

export const {
  selectAccounts: selectAllAccounts,
  selectStatus: selectAccountsStatus,
} = accountsFeature;
