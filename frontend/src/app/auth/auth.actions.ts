import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { User } from './auth.models';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    Login: props<{ username: string; password: string }>(),
    'Login Success': props<{ token: string; user: User }>(),
    'Login Failure': props<{ error: string }>(),
    // Same state effect as Login Success, but without the redirect to '/':
    // dispatched when the token from localStorage is re-validated on app init.
    'Session Restored': props<{ token: string; user: User }>(),
    Logout: emptyProps(),
  },
});
