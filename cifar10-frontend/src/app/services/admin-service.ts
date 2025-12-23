import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
export interface User {
  id: number;
  username: string;
  email: string;
  active: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface Stats {
  total_users: number;
  active_users: number;
  inactive_users: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiBaseUrl + '/admin';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`);
  }

  toggleUserActive(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${id}/toggle-active`, {});
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${id}`);
  }

  getStats(): Observable<Stats> {
    return this.http.get<Stats>(`${this.apiUrl}/stats`);
  }
}