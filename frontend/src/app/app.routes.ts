import { Routes } from '@angular/router';
import { authGuard, unauthenticatedGuard } from './auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [unauthenticatedGuard],
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'accounts/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/account/account.component').then((m) => m.AccountComponent),
  },
  {
    path: 'payment-orders/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/payment-order/payment-order.component').then((m) => m.PaymentOrderComponent),
  },
  { path: '**', redirectTo: '' },
];
