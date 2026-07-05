import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, User } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, {
      username,
      password,
    });
  }

  // During the login flow the token is not yet in the store, so the
  // interceptor can't attach it — pass it explicitly in that case.
  me(token?: string): Observable<User> {
    return this.http.get<User>(`${environment.apiBaseUrl}/users/me`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
  }
}
