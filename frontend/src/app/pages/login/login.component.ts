import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AuthActions } from '../../auth/auth.actions';
import { selectError } from '../../auth/auth.reducer';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <h1>Login</h1>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <label>
        Username
        <input type="text" formControlName="username" autocomplete="username" />
      </label>
      <label>
        Password
        <input type="password" formControlName="password" autocomplete="current-password" />
      </label>
      <button type="submit" [disabled]="form.invalid">Log in</button>
    </form>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
  `,
})
export class LoginComponent {
  private readonly store = inject(Store);

  readonly error = this.store.selectSignal(selectError);

  readonly form = inject(NonNullableFormBuilder).group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.valid) {
      this.store.dispatch(AuthActions.login(this.form.getRawValue()));
    }
  }
}
