import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, User, Stats } from '../../../services/admin-service';
import { ModelService } from '../../../services/model-service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.html',
  styleUrls: ['./admin.css']
})
export class Admin implements OnInit {
  users: User[] = [];
  stats: Stats | null = null;
  models: any[] = [];
  isLoading = false;

  constructor(
    private adminService: AdminService,
    private modelService: ModelService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadStats();
    this.loadModels();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement utilisateurs:', error);
        this.isLoading = false;
      }
    });
  }

  loadStats(): void {
    this.adminService.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Erreur chargement stats:', error);
      }
    });
  }

  loadModels(): void {
    this.models = this.modelService.getModels();
  }

  toggleUserActive(user: User): void {
    this.adminService.toggleUserActive(user.id).subscribe({
      next: () => {
        user.active = !user.active;
      },
      error: (error) => {
        console.error('Erreur activation/désactivation:', error);
      }
    });
  }

  deleteUser(user: User): void {
    if (confirm(`Supprimer l'utilisateur ${user.username} ?`)) {
      this.adminService.deleteUser(user.id).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== user.id);
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
        }
      });
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'running': return 'bg-success';
      case 'stopped': return 'bg-warning';
      case 'error': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}