import { Component, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { AuthActions } from '../../auth/auth.actions';
import { selectUser } from '../../auth/auth.reducer';

@Component({
  selector: 'app-home',
  template: `
    <h1>Home</h1>
    @if (user(); as u) {
      <p>Logged in as {{ u.username }} (id {{ u.id }})</p>
    }
    <button type="button" (click)="logout()">Log out</button>
  `,
})
export class HomeComponent {
  private readonly store = inject(Store);

  readonly user = this.store.selectSignal(selectUser);

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
