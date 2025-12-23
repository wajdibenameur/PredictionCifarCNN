import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiBaseUrl + '/auth';
  private currentUserSubject = new BehaviorSubject<any>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(tap(response => this.setSession(response)));
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUser(): any {
    return JSON.parse(localStorage.getItem('user') || 'null');
  }

  isAdmin(): boolean {
    const user = this.getUser();
    return user?.roles?.includes('ROLE_ADMIN');
  }

  private setSession(authResult: AuthResponse): void {
    localStorage.setItem('token', authResult.token);
    localStorage.setItem('user', JSON.stringify({
      id: authResult.id,
      username: authResult.username,
      email: authResult.email,
      roles: authResult.roles
    }));
    this.currentUserSubject.next(this.getUser());
  }

  private loadUserFromStorage(): void {
    const user = this.getUser();
    if (user) {
      this.currentUserSubject.next(user);
    }
  }

  checkUsername(username: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/check-username/${username}`);
  }

  checkEmail(email: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/check-email/${email}`);
  }
}