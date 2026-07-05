import { createFeature, createReducer, on } from '@ngrx/store';
import { AuthActions } from './auth.actions';
import { User } from './auth.models';
import { readToken } from './token-storage';

export interface AuthState {
  token: string | null;
  user: User | null;
  error: string | null;
}

// The token is rehydrated from localStorage; the user is re-fetched from
// GET /users/me by the init$ effect (see auth.effects.ts).
export const initialAuthState: AuthState = {
  token: readToken(),
  user: null,
  error: null,
};

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialAuthState,
    on(AuthActions.login, (state): AuthState => ({ ...state, error: null })),
    on(
      AuthActions.loginSuccess,
      AuthActions.sessionRestored,
      (state, { token, user }): AuthState => ({ ...state, token, user, error: null }),
    ),
    on(
      AuthActions.loginFailure,
      (state, { error }): AuthState => ({ ...state, token: null, user: null, error }),
    ),
    on(AuthActions.logout, (): AuthState => ({ token: null, user: null, error: null })),
  ),
});

export const {
  selectAuthState,
  selectToken,
  selectUser,
  selectError,
} = authFeature;
