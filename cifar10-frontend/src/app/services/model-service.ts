import { Injectable } from '@angular/core';

export interface Model {
  id: string;
  name: string;
  description: string;
  status: 'running' | 'stopped' | 'error';
  accuracy: number;
  lastUpdated: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ModelService {
  private models: Model[] = [
    {
      id: 'cifar10-cnn',
      name: 'CIFAR-10 CNN',
      description: 'Réseau de neurones convolutif pour la classification CIFAR-10',
      status: 'running',
      accuracy: 85.2,
      lastUpdated: new Date('2024-01-15')
    }
  ];

  getModels(): Model[] {
    return this.models;
  }

  getModelStatus(modelId: string): Promise<{ status: string }> {
    return new Promise(resolve => {
      setTimeout(() => {
        const model = this.models.find(m => m.id === modelId);
        resolve({ status: model?.status || 'unknown' });
      }, 500);
    });
  }
}