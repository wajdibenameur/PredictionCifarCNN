import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth-guard';
import { AdminGuard } from './guards/admin-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register').then(m => m.Register)
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/dashboard/dashboard/dashboard').then(m => m.Dashboard),
    children: [
      {
        path: 'prediction',
        loadComponent: () => import('./components/dashboard/prediction/prediction').then(m => m.Prediction)
      },
      {
        path: 'admin',
        canActivate: [AdminGuard],
        loadComponent: () => import('./components/dashboard/admin/admin').then(m => m.Admin)
      },
      {
        path: '',
        redirectTo: 'prediction',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
