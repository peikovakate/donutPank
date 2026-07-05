import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Account } from './accounts.models';

export const AccountsActions = createActionGroup({
  source: 'Accounts',
  events: {
    'Load Accounts': emptyProps(),
    'Load Accounts Success': props<{ accounts: Account[] }>(),
    'Load Accounts Failure': props<{ error: string }>(),
  },
});
