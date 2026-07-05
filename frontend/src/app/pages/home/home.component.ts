import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { AccountsActions } from '../../accounts/accounts.actions';
import { selectAccountsStatus, selectAllAccounts } from '../../accounts/accounts.reducer';
import { AuthActions } from '../../auth/auth.actions';
import { selectUser } from '../../auth/auth.reducer';

@Component({
  selector: 'app-home',
  imports: [DecimalPipe],
  template: `
    <h1>Home</h1>
    @if (user(); as u) {
      <p>Logged in as {{ u.username }} (id {{ u.id }})</p>
    }
    <button type="button" (click)="logout()">Log out</button>

    <h2>Accounts</h2>
    @switch (status()) {
      @case ('loading') {
        <p>Loading accounts…</p>
      }
      @case ('error') {
        <p>Failed to load accounts.</p>
      }
      @case ('loaded') {
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Currency</th>
              <th>Balance</th>
            </tr>
          </thead>
          <tbody>
            @for (account of accounts(); track account.id) {
              <tr (click)="openAccount(account.id)">
                <td>{{ account.id }}</td>
                <td>{{ account.currency }}</td>
                <td>{{ account.balance | number: '1.2-2' }}</td>
              </tr>
            } @empty {
              <tr>
                <td colspan="3">No accounts yet.</td>
              </tr>
            }
          </tbody>
        </table>
      }
    }
  `,
})
export class HomeComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  readonly user = this.store.selectSignal(selectUser);
  readonly accounts = this.store.selectSignal(selectAllAccounts);
  readonly status = this.store.selectSignal(selectAccountsStatus);

  ngOnInit(): void {
    this.store.dispatch(AccountsActions.loadAccounts());
  }

  openAccount(id: number): void {
    this.router.navigate(['/accounts', id]);
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
